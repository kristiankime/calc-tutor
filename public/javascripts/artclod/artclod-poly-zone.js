if (!ARTC) {
    var ARTC = {};
}

if (!ARTC.mathJS) {
    ARTC.mathJS = {};
}

if (!ARTC.mathJS.infinity) {
    ARTC.mathJS.infinity = {};
}

ARTC.mathJS.infinity.positiveInfinityStrings = [];
ARTC.mathJS.infinity.negativeInfinityStrings = [];
ARTC.mathJS.infinity.allInfinityStrings = [];
ARTC.mathJS.infinity.allInfinityStrRegex = [];
(function(){
    var infinityStrings = ["infinity", "inf", "∞"];

    var positiveNoSpace = ["", "+", "pos", "positive"];
    var positiveStrings = [];
    positiveNoSpace.forEach(function(p){
        positiveStrings.push(p);
        positiveStrings.push(p + " ");
    });

    var negativeNoSpace = [    "-", "neg", "negative"];
    var negativeStrings = [];
    negativeNoSpace.forEach(function(n){
        negativeStrings.push(n);
        negativeStrings.push(n + " ");
    });

    positiveStrings.forEach(function(p){
        infinityStrings.forEach(function(i){
            ARTC.mathJS.infinity.positiveInfinityStrings.push(p + i);
        })
    })

    negativeStrings.forEach(function(n){
        infinityStrings.forEach(function(i){
            ARTC.mathJS.infinity.negativeInfinityStrings.push(n + i);
        })
    })

    ARTC.mathJS.infinity.allInfinityStrings = ARTC.mathJS.infinity.positiveInfinityStrings.concat(ARTC.mathJS.infinity.negativeInfinityStrings);

    ARTC.mathJS.infinity.allInfinityStrings.forEach(function(i){ // here we replace + with \+ for regex use
        ARTC.mathJS.infinity.allInfinityStrRegex.push( i.replace(/\+/g, "\\+") );
    });
})();

(function(){
    var num = "[-+]?[0-9]*\\.?[0-9]+";
    // === numOrInf === match a (non scientific notation) Double or words like "infinite"
    var numOrInf = "(?:";
    ARTC.mathJS.infinity.allInfinityStrRegex.forEach(function(i){ numOrInf += i + "|"; });
    numOrInf += num + ")";
    // === numOrInf ===
    var w = "[\\s]*"
    var numGroup = w + "(" + numOrInf + ")" + w;
    var full = "^\\(" + numGroup + "," + numGroup + "\\)$";
    ARTC.mathJS.polyIntervalRegex = new RegExp(full);
})();

/*
 * Parse string to an int only if the entire string is an integer (modulo whitespace)
 */
ARTC.mathJS.parsePureInt = function(text, radix) {
    if(typeof text !== "string") { return NaN; }
    var trim = text.trim();
    var test = /^[+-]?[0-9]*$/.test(trim);
    if( ! test ) { return NaN; }
    return parseInt(text, radix);
}

/*
 * Parse comma separated integers
 */
ARTC.mathJS.string2IntArray = function(text) {
    if(typeof text !== "string") { return { success : false, array : [] }; }
    if(text === "") { return { success : true, array : [] }; }

    var split = text.split(",")
    var success = true;
    var roots = [];
    split.forEach(function (v) {
        var parse =  ARTC.mathJS.parsePureInt(v, 10)
        roots.push( parse );
        if(isNaN(parse)) { success = false; }
    });

    var array = (success ? roots : [])
    return { success : success, array : array };
}

/*
 *
 */
ARTC.mathJS.parsePolyZones = function(scaleIn, rootsText, numRoots, min, max) {
    var scale = (typeof scaleIn === "number" ) ? scaleIn : ARTC.mathJS.parsePureInt(scaleIn);
    if(isNaN(scale)) { return { success : false }; }

    var roots = ARTC.mathJS.string2IntArray(rootsText);
    if(!roots.success) { return { success : false}; }

    if(numRoots === undefined) { numRoots = 12; }
    if(min === undefined) { min = -10; }
    if(max === undefined) { max = 10; }

    // Don't allow more than numRoots roots
    if(roots.array.length > numRoots) { return { success : false}; }

    // Don't allow roots that are too large or small
    var badRoots = false;
    roots.array.forEach(function(r){ if (r > max || r < min ){ badRoots = true; } });
    if(badRoots) { return { success : false}; }

    return ARTC.mathJS.polyZones(scale, roots);
}

/*
 *
 */
ARTC.mathJS.polyZones = function(scale, rootsObj) {
    if(isNaN(scale)) { return { success: false }; }

    var roots = rootsObj.array;

    var node = scale.toString();
    roots.forEach(function(e){ node += " * (x - " + e + ")"; })

    var nodeNoScale = "C";
    roots.forEach(function(e){ nodeNoScale += " * (x - " + e + ")"; })

    var func = function(x) {
        var ret = scale;
        roots.forEach(function(e){ ret *= x - e; })
        return ret;
    }

    return {
        success: true,
        nodeText: node,
        node: math.parse(node),
        nodeNoScaleText: nodeNoScale,
        nodeNoScale: math.parse(nodeNoScale),
        roots: roots,
        func: func
    }
}

ARTC.mathJS.parsePolyIntervals = function(intervalsStr) {
    var ints = ARTC.mathJS.polyIntervals(intervalsStr);
    if(!ints.success){ return {success: false}; }
    if(ARTC.mathJS.polyIntervalsOverlap(ints.intervals)) { return {success: false}; }
    return ints;
}

/*
 * Parse comma separated integer intervels "(3,8),(9,20)" etc
 * into a javascript object (or fail)
 * the result is of the form { success: boolean, intervals: [{ success: boolean, lower: number, upper: number }] }
 */
ARTC.mathJS.polyIntervals = function(intervalsStr) {
    if(typeof intervalsStr !== "string") { return {success: false}; }
    if(intervalsStr === "") { return {success: false}; }

    var regex = /\)[\s]*,/;
    var splitMinusParen = intervalsStr.split(regex);
    var split = [];
    for(var i=0; i<(splitMinusParen.length-1); i++) {
        split.push(splitMinusParen[i] + ")");
    }
    split.push(splitMinusParen[splitMinusParen.length-1]);

    var intervals = [];
    for(var i=0; i < split.length; i++) {
        var interval = ARTC.mathJS.polyInterval(split[i]);
        if(interval.success) {
            intervals.push(interval);
        } else {
            return {success: false};
        }
    }
    return {success: true, intervals: intervals}
}

/*
 * Parse integer intervals eg "(2,3)" into javascript object (or fail)
 * The result is of the form { success: boolean, lower: number, upper: number }
 */
ARTC.mathJS.polyInterval = function(intervalStr) {
    if(typeof intervalStr !== "string") { { success: false }; }

    var match = ARTC.mathJS.polyIntervalRegex.exec(intervalStr.trim().toLowerCase());

    if(match) {
        var lower = ARTC.mathJS.doubleParse(match[1]);
        var upper = ARTC.mathJS.doubleParse(match[2]);
        if(lower < upper) {
            return { success: true, lower: lower, upper: upper}
        }
    }
    return { success: false }
}


ARTC.mathJS.polyIntervalsOverlap = function(ints) {
    if(ints.length == 0 || ints.length == 1) {
        return false;
    } else {
        var sort = ints.slice().sort(function(a, b) { return a.lower - b.lower; });

        for(var i = 0; i < sort.length -1; i++) {
            var a = sort[i];
            var b = sort[i+1];
            if(ARTC.mathJS.polyIntervalOverlap(a, b)) {
                return true;
            }
        }
        return false;
    }
}


ARTC.mathJS.polyIntervalOverlap = function(a, b) {
    return (a.lower >= b.lower && a.lower  < b.upper) ||
           (a.upper >  b.lower && a.upper <= b.upper) ||
           (b.lower >= a.lower && b.lower  < a.upper) ||
           (b.upper >  a.lower && b.upper <= a.upper);
}

/*
 * Parse string to an int only if the entire string is an integer (modulo whitespace)
 */
ARTC.mathJS.doubleParse = function(text) {
    if(typeof text !== "string") { return NaN; }
    var trim = text.trim().toLowerCase();

    if(ARTC.mathJS.infinity.positiveInfinityStrings.indexOf(trim) !== -1) {
        return Number.POSITIVE_INFINITY;
    } else if(ARTC.mathJS.infinity.negativeInfinityStrings.indexOf(trim) !== -1) {
        return Number.NEGATIVE_INFINITY;
    }

    var ret = parseFloat(trim);
    return ret;
}


/*
 * Assuming a one argument (numeric) function that outputs a number
 * take a guess at the maximum value on a range.
 */
ARTC.mathJS.guessFunctionMax = function(func, lower, upper, segments) {
    // Set initial max
    var max = func(lower);
    max = Math.max(max, func(upper));

    var s = (segments ? segments : 10);// number of segments
    var diff = (upper - lower) / s;
    for(var i=1; i < s; i++) {
        max = Math.max(max, func(lower + (i * diff)));
    }
    return max;
}

/*
 * Assuming a one argument (numeric) function that outputs a number
 * take a guess at the minimum value on a range.
 */
ARTC.mathJS.guessFunctionMin = function(func, lower, upper, segments) {
    // Set initial min
    var min = func(lower);
    min = Math.min(min, func(upper));

    var s = (segments ? segments : 10);// number of segments
    var diff = (upper - lower) / s;
    for(var i=1; i < s; i++) {
        min = Math.min(min, func(lower + (i * diff)));
    }
    return min;
}