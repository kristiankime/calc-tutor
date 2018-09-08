if (!ARTC) {
    var ARTC = {};
}

/**
 * Parses a string of semi colon separated numbers into an array of numbers.
 *
 * @param string should be in the format N1;N2;N3... ie "3.4; 5; 7.8"
 * @returns An object with the following fields
 * success = a boolean indicating if this can be parsed
 * values = an array of the values or empty on failure
 * error = this is an optional parameter that indicates why there was a failure to parse
 */
ARTC.string2Numbers = function (string) {
    if(!string) {
        return { success : false, values : [], error : "string was falsey" + string }
    }
    var numStrArray = string.split(";");
    var values = [];
    for(var i=0; i<numStrArray.length; i++) {
        try {
            values.push(parseFloat(numStrArray[i].trim().replace(/,/g, '')))
        } catch (e) {
            return { success : false, values : [], error : e }
        }
    }
    return { success : true, values : values }
}