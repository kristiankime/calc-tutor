
test("ARTC.mathJS.string2IntArray: can parse a single int", function() {
	deepEqual(ARTC.mathJS.string2IntArray("1"), { success: true, array: [1] });
});

test("ARTC.mathJS.string2IntArray: can parse a multiple ints", function() {
    deepEqual(ARTC.mathJS.string2IntArray("1, 2, 3"), { success: true, array: [1, 2, 3] });
});

test("ARTC.mathJS.string2IntArray: parsing an empty string returns empty roots", function() {
    deepEqual(ARTC.mathJS.string2IntArray(""), { success: true, array: [] });
});

test("ARTC.mathJS.string2IntArray: parsing decimals fails", function() {
    deepEqual(ARTC.mathJS.string2IntArray("2, 2.3"), { success: false, array: [] });
});

test("ARTC.mathJS.string2IntArray: parsing random string fails", function() {
    deepEqual(ARTC.mathJS.string2IntArray("asdfs, 2sdf 6.7"), { success: false, array: [] });
});

// ================
test("ARTC.mathJS.parsePureInt: 1", function() {
	equal(ARTC.mathJS.parsePureInt("1"), 1)
});

test("ARTC.mathJS.parsePureInt: -12", function() {
	equal(ARTC.mathJS.parsePureInt("-12"), -12)
});

test("ARTC.mathJS.parsePureInt: gibberish is NaN", function() {
	equal( isNaN(ARTC.mathJS.parsePureInt("adsf")), true)
});

test("ARTC.mathJS.parsePureInt: 12.1232 is NaN", function() {
	equal( isNaN(ARTC.mathJS.parsePureInt("12.1232")), true)
});


// ================
test("ARTC.mathJS.guessFunctionMax: correct if max is at upper", function() {
	equal(ARTC.mathJS.guessFunctionMax(function(x){return x;}, -10,5,15), 5)
});

test("ARTC.mathJS.guessFunctionMax: correct if max is at lower", function() {
	equal(ARTC.mathJS.guessFunctionMax(function(x){return -x;}, -6,9,15), 6)
});

test("ARTC.mathJS.guessFunctionMax: correct if max is in the middle", function() {
	equal(ARTC.mathJS.guessFunctionMax(function(x){return -(x*x) + 4;}, -5,5,10), 4)
});


// ================
test("ARTC.mathJS.guessFunctionMin: correct if min is at lower", function() {
	equal(ARTC.mathJS.guessFunctionMin(function(x){return x;}, -10,5,15), -10)
});

test("ARTC.mathJS.guessFunctionMin: correct if min is at upper", function() {
	equal(ARTC.mathJS.guessFunctionMin(function(x){return -x;}, -6,9,15), -9)
});

test("ARTC.mathJS.guessFunctionMin: correct if max is in the middle", function() {
	equal(ARTC.mathJS.guessFunctionMin(function(x){return (x*x) - 4;}, -5,5,10), -4)
});

// ================
test("ARTC.mathJS.polyZones: no roots yields constant", function() {
	var zones = ARTC.mathJS.parsePolyZones("1", "")
    equal(zones.nodeText, "1");
	equal(zones.func(10), 1);
	equal(zones.func(20), 1);
});

test("ARTC.mathJS.polyZones: one roots yields linear", function() {
	var zones = ARTC.mathJS.parsePolyZones("2", "3")
    equal(zones.nodeText, "2 * (x - 3)");
	equal(zones.func(10), 14);
	equal(zones.func(20), 34);
});

test("ARTC.mathJS.polyZones: multiple roots", function() {
	var zones = ARTC.mathJS.parsePolyZones("1", "-3,0,1")
    equal(zones.nodeText, "1 * (x - -3) * (x - 0) * (x - 1)");
	equal(zones.func(-1), 4);
	equal(zones.func(2), 10);
});

test("ARTC.mathJS.polyZones: gibberish scale yields failure", function() {
	var zones = ARTC.mathJS.parsePolyZones("asdf", "1,2")
    equal(zones.success, false);
});

test("ARTC.mathJS.polyZones: gibberish roots yields failure", function() {
	var zones = ARTC.mathJS.parsePolyZones("1", "sdaf")
    equal(zones.success, false);
});

test("ARTC.mathJS.polyZones: to many roots yields failure", function() {
	var zones = ARTC.mathJS.parsePolyZones("1", "1,2,3,4,5,6,7,8,9,10,11,12,13")
	equal(zones.success, false);
});

test("ARTC.mathJS.polyZones: not root should be greater than 10", function() {
	var zones = ARTC.mathJS.parsePolyZones("1", "1,2,12,2")
	equal(zones.success, false);
});

test("ARTC.mathJS.polyZones: not root should be less than -10", function() {
	var zones = ARTC.mathJS.parsePolyZones("1", "1,2,-12,2")
	equal(zones.success, false);
});


// ================
test("ARTC.mathJS.polyInterval: matches numbers in parens", function() {
	var interval = ARTC.mathJS.polyInterval("(1,2)")
	deepEqual(interval, { success: true, lower: 1, upper: 2 } )
});

test("ARTC.mathJS.polyInterval: matches even with white space", function() {
	var interval = ARTC.mathJS.polyInterval(" ( 1 ,   2 )")
	deepEqual(interval, { success: true, lower: 1, upper: 2 } )
});

test("ARTC.mathJS.polyInterval: matches infinites", function() {
	var interval = ARTC.mathJS.polyInterval("(-Inf, Inf)")
	deepEqual(interval, { success: true, lower: Number.NEGATIVE_INFINITY, upper: Number.POSITIVE_INFINITY } )
});

test("ARTC.mathJS.polyInterval: fails on gibberish", function() {
	var interval = ARTC.mathJS.polyInterval("(1,sd2)")
    equal(interval.success, false);
});

test("ARTC.mathJS.polyInterval: fails with extra parens", function() {
	var interval = ARTC.mathJS.polyInterval("(1,2))")
    equal(interval.success, false);
});


// ================
test("ARTC.mathJS.polyIntervals: matches numbers in parens", function() {
	var intervals = ARTC.mathJS.polyIntervals("(1,2), (3,4)")
	deepEqual(intervals.intervals, [{ success: true, lower: 1, upper: 2 }, { success: true, lower: 3, upper: 4 }] )
});

test("ARTC.mathJS.polyIntervals: whitespace is ignored", function() {
	var intervals = ARTC.mathJS.polyIntervals(" (1 ,2 ), (  3,4) ")
	deepEqual(intervals.intervals, [{ success: true, lower: 1, upper: 2 }, { success: true, lower: 3, upper: 4 }] )
});

test("ARTC.mathJS.polyIntervals: fails in gibberish", function() {
	var intervals = ARTC.mathJS.polyIntervals(" (1 ,2 )sadf, (  3,4) ")
	equal(intervals.success, false)
});

test("ARTC.mathJS.polyIntervals: fails for an extra paren", function() {
	var intervals = ARTC.mathJS.polyIntervals("(1,2)), (3,4)")
	equal(intervals.success, false)
});

// ================
test("ARTC.mathJS.polyIntervalOverlap: return false if a is completely before b" , function() { equal(ARTC.mathJS.polyIntervalOverlap( { lower: 1, upper: 2 }, { lower: 3, upper: 4 }), false ) });
test("ARTC.mathJS.polyIntervalOverlap: return false if b is completely before a" , function() { equal(ARTC.mathJS.polyIntervalOverlap( { lower: 3, upper: 4 }, { lower: 1, upper: 2 }), false ) });
test("ARTC.mathJS.polyIntervalOverlap: return false if a touches the end of b"   , function() { equal(ARTC.mathJS.polyIntervalOverlap( { lower: 2, upper: 3 }, { lower: 1, upper: 2 }), false ) });
test("ARTC.mathJS.polyIntervalOverlap: return false if b touches the end of a"   , function() { equal(ARTC.mathJS.polyIntervalOverlap( { lower: 1, upper: 2 }, { lower: 2, upper: 3 }), false ) });
test("ARTC.mathJS.polyIntervalOverlap: return false if a touches the start of b" , function() { equal(ARTC.mathJS.polyIntervalOverlap( { lower: 1, upper: 2 }, { lower: 2, upper: 3 }), false ) });
test("ARTC.mathJS.polyIntervalOverlap: return false if b touches the start of a" , function() { equal(ARTC.mathJS.polyIntervalOverlap( { lower: 2, upper: 3 }, { lower: 1, upper: 2 }), false ) });
test("ARTC.mathJS.polyIntervalOverlap: return true if a lower == b lower"        , function() { equal(ARTC.mathJS.polyIntervalOverlap( { lower: 1, upper: 2 }, { lower: 1, upper:20 }), true ) });
test("ARTC.mathJS.polyIntervalOverlap: return true if b lower == a lower"        , function() { equal(ARTC.mathJS.polyIntervalOverlap( { lower: 1, upper:20 }, { lower: 1, upper: 2 }), true ) });
test("ARTC.mathJS.polyIntervalOverlap: return true if a upper == b upper"        , function() { equal(ARTC.mathJS.polyIntervalOverlap( { lower:-1, upper: 2 }, { lower: 1, upper: 2 }), true ) });
test("ARTC.mathJS.polyIntervalOverlap: return true if b upper == a upper"        , function() { equal(ARTC.mathJS.polyIntervalOverlap( { lower: 1, upper: 2 }, { lower:-1, upper: 2 }), true ) });
test("ARTC.mathJS.polyIntervalOverlap: return true if a contains b"              , function() { equal(ARTC.mathJS.polyIntervalOverlap( { lower: 1, upper:10 }, { lower: 3, upper: 4 }), true ) });
test("ARTC.mathJS.polyIntervalOverlap: return true if b contains a"              , function() { equal(ARTC.mathJS.polyIntervalOverlap( { lower: 3, upper: 4 }, { lower: 1, upper:10 }), true ) });
test("ARTC.mathJS.polyIntervalOverlap: return true if a upper in b"              , function() { equal(ARTC.mathJS.polyIntervalOverlap( { lower: 3, upper: 7 }, { lower: 5, upper:10 }), true ) });
test("ARTC.mathJS.polyIntervalOverlap: return true if b upper in a"              , function() { equal(ARTC.mathJS.polyIntervalOverlap( { lower: 5, upper:10 }, { lower: 3, upper: 7 }), true ) });
test("ARTC.mathJS.polyIntervalOverlap: return true if a lower in b"              , function() { equal(ARTC.mathJS.polyIntervalOverlap( { lower: 3, upper: 7 }, { lower: 1, upper: 5 }), true ) });
test("ARTC.mathJS.polyIntervalOverlap: return true if b lower in a"              , function() { equal(ARTC.mathJS.polyIntervalOverlap( { lower: 1, upper: 5 }, { lower: 3, upper: 7 }), true ) });


// ================
test("ARTC.mathJS.polyIntervalsOverlap: return false if no overlap", function() {
	equal(ARTC.mathJS.polyIntervalsOverlap([{lower:1, upper:2}, {lower:3, upper:4}]), false)
});

test("ARTC.mathJS.polyIntervalsOverlap: return true if overlap", function() {
	equal(ARTC.mathJS.polyIntervalsOverlap([{lower:3, upper:6}, {lower:1, upper:2}, {lower:3, upper:4}]), true)
});
