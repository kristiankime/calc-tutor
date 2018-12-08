if (!ARTC) {
    var ARTC = {};
}

/**
 * Parses a string of semi colon separated numbers into an array of numbers into mathml.
 *
 * @param string should be in the format N1;N2;N3... ie "3.4; 5; 7.8"
 * @returns An object with the following fields
 * success = a boolean indicating if this can be parsed
 * values = an array of the values or empty on failure
 * error = this is an optional parameter that indicates why there was a failure to parse
 */
ARTC.string2Numbers = function (string, parser) {
    if(!string) {
        return { success : false, values : [], error : "input was falsey [" + string + "]"}
    } else if (typeof string !== 'string'){
        return { success : false, values : [], error : "input was not a string [" + string + "]"}
    } else if(string.indexOf(",") !== -1) {
        return { success : false, values : [], error : "input had commas [" + string + "]"}
    }

    var split = string.split(";");
    var parsedAll = true;
    var values = _.map(split, function(s){
        var mathResult = parser(s);
        if(mathResult.success) {
            return {
                type   : "math",
                render : ARTC.mathJax.tex(mathResult.node.toTex()),
                full   : mathResult
            };
        } else {
            parsedAll = false;
            return {
                type: "failure",
                render: "",
                full: ""
            };
        }
    });

    if(!parsedAll) {
        return { success : false, values : [], error : "was unable to parse all elements as constants [" + string + "]"}
    }

    return { success : true, values : values };
}