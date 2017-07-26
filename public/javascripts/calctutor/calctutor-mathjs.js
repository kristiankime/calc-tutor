if(!CALC){
    var CALC = {};
}

if(!CALC.mathJS){
    CALC.mathJS = {};
}

CALC.mathJS.rejectLogsWithBaseUsingX = function(node){
    var nodeIsLog = function (n) { return n.type === 'FunctionNode' && (n.name === 'ln' || n.name === 'log'); };
    var hasBase = function (n) { return n.args.length > 1; };
    var toBase = function (n) { return n.args[1]; };
    var nodeUsesX = function (n) { return n.type == 'SymbolNode' && n.name === 'x'; };
    var usesX = function (nodes) { return nodes.filter(nodeUsesX).length > 0; };

    // Note we are using both _.filter and mathjsnode.filter
    var logs = node.filter(nodeIsLog);
    var logsWithBases = _.filter(logs, hasBase);
    var bases = _.map(logsWithBases, toBase);
    var basesUsingX = _.filter(bases, usesX);
    var doesAnyBaseUseX = _.reduce(basesUsingX, function(a, b){ return a || b; }, false);

    return doesAnyBaseUseX;
};

CALC.mathJS.rejectRootsUsingX = function(node){
    var nodeIsRoot = function (n) { return n.type === 'FunctionNode' && n.name === 'nthRoot'; };
    var hasNth = function (n) { return n.args.length > 1; };
    var toNth = function (n) { return n.args[1]; };
    var nodeUsesX = function (n) { return n.type == 'SymbolNode' && n.name === 'x'; };
    var usesX = function (nodes) { return nodes.filter(nodeUsesX).length > 0; };

    // Note we are using both _.filter and mathjsnode.filter
    var roots = node.filter(nodeIsRoot);
    var rootsWithNth = _.filter(roots, hasNth);
    var nths = _.map(rootsWithNth, toNth);
    var rootsUsingX = _.filter(nths, usesX);
    var doesAnyRootUseX = _.reduce(rootsUsingX, function(a, b){ return a || b; }, false);

    return doesAnyRootUseX;
};

CALC.mathJS.rejectOddX = function(node) { return false || CALC.mathJS.rejectLogsWithBaseUsingX(node) || CALC.mathJS.rejectRootsUsingX(node) };

CALC.mathJS.functionOfXInputs = {
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

CALC.mathJS.functionOfXParser = ARTC.mathJS.buildParser(CALC.mathJS.functionOfXInputs.functions, CALC.mathJS.functionOfXInputs.operators, CALC.mathJS.functionOfXInputs.symbols, CALC.mathJS.rejectOddX);


CALC.mathJS.constantInputs = {
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
            "e": "<exponentiale/>"
        },
        allowAny : false
    }
};

CALC.mathJS.constantParser = ARTC.mathJS.buildParser(CALC.mathJS.constantInputs.functions, CALC.mathJS.constantInputs.operators, CALC.mathJS.constantInputs.symbols);

