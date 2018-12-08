if (!ARTC) {
    var ARTC = {};
}

if(!ARTC.mathml) {
    ARTC.mathml = {};
}

ARTC.mathml.trim = function(mathMLString) {
    var ret = mathMLString;
    var ret = ret.replace('<math xmlns="http://www.w3.org/1998/Math/MathML">', '');
    var ret = ret.replace('<math>', '');
    var ret = ret.replace('</math>', '');
    return ret.trim();
}
