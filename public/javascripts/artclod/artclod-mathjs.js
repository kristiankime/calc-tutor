if(!ARTC){
    var ARTC = {};
}

ARTC.parenMatch = function(text, startParenIndex) {
    // Some input checkig
    if(typeof text !== "string") { throw "text was not a string [" + text + "]"; }
    if(typeof startParenIndex !== "number") { throw "initial paren index was not a number [" + startParenIndex + "]"; }
    if( text.charAt(startParenIndex) !== "(" ) { throw "character at index was not open paren, text=[" + text + "]"  + " index=[" + startParenIndex + "] character=[" + text.charAt(startParenIndex) + "]"; }

    var parenCount = 1;
    for(var i = startParenIndex + 1; i < text.length; i++) {
        switch(text.charAt(i)) {
            case "(": parenCount = parenCount + 1; break;
            case ")": parenCount = parenCount - 1; break;
        }
        if(parenCount === 0) { return i+1; }
    }
    return -1;
}

if(!ARTC.mathJS){
    ARTC.mathJS = {};
}

ARTC.mathJS.node2FunctionOfX = function(mathJSNode) {
    var code = mathJSNode.compile(math);
    var func = function(x) {
        var scope = { x : x };
        return code.eval(scope);
    };
    func(0); // This will throw if the function string cannot be evaluated
    return func;
};

ARTC.mathJS.text2FunctionOfX = function(mathText) {
    var node = math.parse(mathText)
    return  ARTC.mathJS.node2FunctionOfX(node);
};

/*
 * mathjs only parse functions raised to powers if the power comes after
 * the closing paren.
 *
 * for exampe mathjs does not parse cos^2(x) it has to be written as cos(x)^2
 *
 * This preprocessor changes the string so mathjs can understand it.
 */
ARTC.mathJS.prepFuncPow = function(text, funcs) {

    var prepFuncPowOnce = function(text, func) {
        if(typeof text !== "string") { throw "text was not a string [" + text + "]"; }
        if(typeof func !== "string") { throw "func was not a string [" + func + "]"; }

        var re = new RegExp(func + "(\\^[0-9]+)\\(");
        var match = re.exec(text);

        if(match != null) {
            var powerText = match[1];
            var openParenIndex = match.index + func.length + powerText.length;
            var closeParenIndex = ARTC.parenMatch(text, openParenIndex);
            if(closeParenIndex !== -1) {
                var preText = text.substring(0, match.index);
                var inParenText = text.substring(openParenIndex, closeParenIndex);
                var postText = text.substring(closeParenIndex);
                return { updated : true, text : preText + "(" + func + inParenText + powerText + ")" + postText };
            }
        }
        return { updated : false, text : text };
    }

    var prepFuncPowLoop = function(text, func) {
        if(typeof text !== "string") { throw "text was not a string [" + text + "]"; }
        if(typeof func !== "string") { throw "func was not a string [" + func + "]"; }

        var ret = { updated : true, text : text };
        while(ret.updated) {
            ret = prepFuncPowOnce(ret.text, func);
        }
        return ret;
    }

    if(typeof text !== "string") { throw "text was not a string [" + text + "]"; }
    if(!Array.isArray(funcs)) { throw "funcs was not an array [" + funcs + "]"; }

    var ret = text;
    for(var i = 0; i < funcs.length; i++) {
        ret = prepFuncPowLoop(ret, funcs[i]).text;
    }
    return ret;
}

/*
 * ARTC.buildMathJSParser is a builder for parsers.
 * The main goal of these parsers turn a MathJS style string into Content MathML.
 *
 * ARTC.buildMathJSParser requires an "function", "operator" and "symbol" map.
 * These maps specify which subset of MathJS is legal in the parser and what content MathML the Mathjs should map to.
 * See ARTC.mathJSDefaults for example of these maps.
 *
 * The parser returns an object with the following format:
 * {
 *   success: A boolean indicating if everything went well,
 *   content: a string with the content MathML if success is true or "" otherwise,
 *   mathJSNode: the MathJS node version of the input string on success or an empty object otherwise,
 *   error: an empty object on success or the error on failure
 * }
 */
ARTC.mathJS.buildParser = (function(){
    // Copied from artclod_arg_map.js so as to not require an import
    var argMapCreate = function(map) {
        return function (key, num_args) {
            if (typeof num_args !== 'undefined') {
                var argsKey = key + "#" + num_args
                if (map.hasOwnProperty(argsKey)) {
                    return map[argsKey];
                }
            }
            return map[key];
        };
    }

    // Put <apply> tag around elements
    var applyWrap = function(operator, elements, parseNode) {
        var ret = "<apply> " + operator + " ";
        var len = elements.length;
        for (var i = 0; i < len; i++) {
            var n = elements[i];
            ret += parseNode(n) + " ";
        }
        ret += "</apply>";
        return ret;
    }

    // The main function
    return function(functions, operators, symbols, rejectFunc){
        // If we don't have values passed in use defaults
        var functionsSafe = ARTC.mathJS.parserDefaults.functions;
        if(functions){ functionsSafe = functions; }

        var operatorsSafe = ARTC.mathJS.parserDefaults.operators;
        if(operators){ operatorsSafe = operators; }

        var symbolsSafe = ARTC.mathJS.parserDefaults.symbols;
        if(symbols){ symbolsSafe = symbols; }

        var rejectFuncSafe = function(node){ return false; };
        if(rejectFunc){ rejectFuncSafe = rejectFunc; }

        // ==============  Function Handling ==============
        var fncMap = argMapCreate(functionsSafe);

        var functionNodeFunction = function(node){
            var fnc = fncMap(node.name, node.args.length)
            if(!fnc) { throw { message: "Error finding function for FunctionNode with name " + node.name + " and # args = " + node.args.length } }
            return fnc(node, parseNode);
        }

        // ==============  Operator Handling ==============
        var opMap = argMapCreate(operatorsSafe);

        var operatorNodeFunction = function(node){
            var op = opMap(node.op)
            if(!op) { throw { message: "Error finding operator for OperatorNode with op " + node.op } }
            return applyWrap(op, node.args, parseNode);
        }

        // ============== Symbol Handling ===========
        var symMap = argMapCreate(symbolsSafe.map);

        var symbolNodeFunction = function(node){
            var sym = symMap(node.name)
            if(sym) { return sym }
            if(!sym && !symbols.allowAny) { throw { message: "Error in SymbolNode, any not allowed, and nothing specified for " + node.name } }
            return "<ci> " + node.name + " </ci>";
        }

        // ============== Constant Handling ===========
        var constantNodeFunction = function(node){
            switch(node.valueType){
                case 'undefined' : throw { message: "Error, received an undefined node " + node };
                default          : return "<cn> " + node.value + " </cn>"; // TODO parse down to cn type here
            }
        }

        // ============== Full Parsing ===========
        var parseNode = function(node) {
            switch (node.type) {
                case 'OperatorNode': return operatorNodeFunction(node);
                case 'ConstantNode': return constantNodeFunction(node);
                case 'SymbolNode':   return symbolNodeFunction(node);
                case 'FunctionNode': return functionNodeFunction(node);
                default:             throw { message: "Error, unknown node type " + node };
            }
        }

        var ret = function(string) {
            try {

                if(string.length > 0 && string.charAt(0) === "+") { throw "function can't start with +"; }

                // ====
                // This turns cos^2(x) into cos(x)^2 which can be parsed by mathjs
                var funcs = [];
                for(var key in functionsSafe){
                    if (functionsSafe.hasOwnProperty(key)) {
                        funcs.push(key.split("#")[0]);
                    }
                }
                var stringPrepped = ARTC.mathJS.prepFuncPow(string, funcs);
                // =====

                var mathJSNode = math.parse(stringPrepped);



                var reject = rejectFuncSafe(mathJSNode);
                if(reject){ throw reject; }

                return {
                    success: true,
                    node: mathJSNode,
                    content: '<math xmlns="http://www.w3.org/1998/Math/MathML"> ' + parseNode(mathJSNode) + ' </math>',
                    mathJSNode: mathJSNode,
                    error: {}
                };
            } catch (e) {
                return {
                    success: false,
                    node: {},
                    content: "",
                    mathJSNode: {},
                    error: e
                }
            }
        }
        ret.functions = functionsSafe;
        ret.operators = operatorsSafe;
        ret.symbols = symbolsSafe;
        ret.rejectFunc = rejectFuncSafe;
        return ret
    }
}());

/*
 * Helpful defaults for use with ARTC.buildMathJSParser
 */
ARTC.mathJS.parserDefaults = {
    // All functions here take (node, parseNode)
    functions: {
        "cos#1"     : function(n, pN){ return "<apply> <cos/> " + pN(n.args[0]) + " </apply>"; },
        "sin#1"     : function(n, pN){ return "<apply> <sin/> " + pN(n.args[0]) + " </apply>"; },
        "tan#1"     : function(n, pN){ return "<apply> <tan/> " + pN(n.args[0]) + " </apply>"; },
        "sec#1"     : function(n, pN){ return "<apply> <sec/> " + pN(n.args[0]) + " </apply>"; },
        "csc#1"     : function(n, pN){ return "<apply> <csc/> " + pN(n.args[0]) + " </apply>"; },
        "cot#1"     : function(n, pN){ return "<apply> <cot/> " + pN(n.args[0]) + " </apply>"; },
        "sqrt#1"    : function(n, pN){ return "<apply> <root/> " + pN(n.args[0]) + " </apply>"; },
        "nthRoot#2" : function(n, pN){ return "<apply> <root/> <degree> " + pN(n.args[1]) + " </degree> " + pN(n.args[0]) + " </apply>"; },
        "ln#1"      : function(n, pN){ return "<apply> <ln/> " + pN(n.args[0]) + " </apply>"; }, // Note ln needs to be added to mathjs manually via math.import({ln: math.log});
        "log#1"     : function(n, pN){ return "<apply> <ln/> " + pN(n.args[0]) + " </apply>"; },
        "log#2"     : function(n, pN){ return "<apply> <log/> <logbase> " + pN(n.args[1]) + " </logbase> " + pN(n.args[0]) + " </apply>"; },
        "pow#2"     : function(n, pN){ return "<apply> <power/> " + pN(n.args[0]) + " " + pN(n.args[1]) + " </apply>"; },
        "exp#1"     : function(n, pN){ return "<apply> <power/> <exponentiale/> " + pN(n.args[0]) + " </apply>"; }
    },
    operators : {
        "+" : "<plus/>",
        "-" : "<minus/>",
        "*" : "<times/>",
        "/" : "<divide/>",
        "^" : "<power/>"
    },
    symbols : {
        map: {
            "pi": "<pi/>",
            "e": "<exponentiale/>",
            "x": "<ci> x </ci>"
        },
        allowAny : false
    }
};


ARTC.mathJS.defaultParser = ARTC.mathJS.buildParser();