if (!ARTC) {
    var ARTC = {};
}

/**
 * Parses a string of semi colon separated values.
 * If the string is empty or has commas this is an error.
 * If possible the string is interpreted as a math statement by the parser.
 * If this cannot be done the string is simply trimmed and interpreted as a name.
 *
 * @param string should be in the format V1;V2;V3... ie "3.1; A; 3*pi"
 * @returns An object with the following fields
 * success = a boolean indicating if this can be parsed
 * values = if success if false this is an empty array other an array of the objects of the form { render : xxx, full : yyy }
 *    render is intended to be something that can be displayed in html. full is either the parse math object or the original string.
 * error = this is an optional parameter that indicates why there was a failure to parse
 */
ARTC.string2Sequence = function(string, parser) {
    if(!string) {
        return { success : false, values : [], error : "input was falsey [" + string + "]"}
    } else if (typeof string !== 'string'){
        return { success : false, values : [], error : "input was not a string [" + string + "]"}
    } else if(string.indexOf(",") !== -1) {
        return { success : false, values : [], error : "input had commas [" + string + "]"}
    }

    var split = string.split(";");
    var values = _.map(split, function(s){
        var mathResult = parser(s);
        if(mathResult.success) {
            return {
                type   : "math",
                render : ARTC.mathJax.tex(mathResult.node.toTex()),
                full   : mathResult
            };
        }
        return {
            type   : "string",
            render : s.trim(),
            full   : s
        };
    });
    return { success : true, values : values };
}

