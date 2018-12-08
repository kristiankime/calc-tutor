
var mathMLValues = function(values) {
    return _.map(values, function(value){
        return ARTC.mathml.trim(value.full.content);
    });
}

test("ARTC.string2Numbers: empty fails", function() {
    equal(ARTC.string2Numbers("", CALC.mathJS.constantParser).success, false);
});

test("ARTC.string2Numbers: 1 succeeds", function() {
    deepEqual(mathMLValues(ARTC.string2Numbers("1", CALC.mathJS.constantParser).values), ["<cn> 1 </cn>"]);
});

test("ARTC.string2Numbers: 1;2 succeeds", function() {
    deepEqual(mathMLValues(ARTC.string2Numbers("1;2", CALC.mathJS.constantParser).values), ["<cn> 1 </cn>", "<cn> 2 </cn>"]);
});

test("ARTC.string2Numbers: parses numbers separated by ;", function() {
    deepEqual(mathMLValues(ARTC.string2Numbers("1;3;2", CALC.mathJS.constantParser).values), ["<cn> 1 </cn>", "<cn> 3 </cn>", "<cn> 2 </cn>"]);
});

test("ARTC.string2Numbers: numbers can have periods (eg 2.1)", function() {
    deepEqual(mathMLValues(ARTC.string2Numbers("1000.21;3000;2000.1", CALC.mathJS.constantParser).values), ["<cn> 1000.21 </cn>", "<cn> 3000 </cn>", "<cn> 2000.1 </cn>"]);
});

test("ARTC.string2Numbers: numbers can be special constants (eg e)", function() {
    deepEqual(mathMLValues(ARTC.string2Numbers("1; e; pi", CALC.mathJS.constantParser).values), ["<cn> 1 </cn>", "<exponentiale/>", "<pi/>"]);
});

test("ARTC.string2Numbers: numbers can be computed constants (eg e)", function() {
    deepEqual(mathMLValues(ARTC.string2Numbers("2 * 2; e; 2 pi", CALC.mathJS.constantParser).values), ["<apply> <times/> <cn> 2 </cn> <cn> 2 </cn> </apply>", "<exponentiale/>", "<apply> <times/> <cn> 2 </cn> <pi/> </apply>"]);
});


test("ARTC.string2Numbers: arbitrary text fails", function() {
    deepEqual(ARTC.string2Numbers("1;3aa;2.1", CALC.mathJS.constantParser).success, false);
});