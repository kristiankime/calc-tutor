var mathMLValues = function(values) {
    return _.map(values, function(value){
        if(value.full.content) {
            return ARTC.mathml.trim(value.full.content);
        } else {
            return ARTC.mathml.trim(value.full);
        }
    });
}

test("ARTC.string2Numbers: empty fails", function() {
    equal(ARTC.string2Sequence("", CALC.mathJS.constantParser).success, false);
});

test("ARTC.string2Numbers: 1 succeeds", function() {
    deepEqual(mathMLValues(ARTC.string2Sequence("1", CALC.mathJS.constantParser).values), ["<cn> 1 </cn>"]);
});

test("ARTC.string2Numbers: 1;2 succeeds", function() {
    deepEqual(mathMLValues(ARTC.string2Sequence("1;2", CALC.mathJS.constantParser).values), ["<cn> 1 </cn>", "<cn> 2 </cn>"]);
});

test("ARTC.string2Numbers: parses numbers separated by ;", function() {
    deepEqual(mathMLValues(ARTC.string2Sequence("1;3;2", CALC.mathJS.constantParser).values), ["<cn> 1 </cn>", "<cn> 3 </cn>", "<cn> 2 </cn>"]);
});

test("ARTC.string2Numbers: numbers can have periods (eg 2.1)", function() {
    deepEqual(mathMLValues(ARTC.string2Sequence("1000.21;3000;2000.1", CALC.mathJS.constantParser).values), ["<cn> 1000.21 </cn>", "<cn> 3000 </cn>", "<cn> 2000.1 </cn>"]);
});

test("ARTC.string2Numbers: arbitrary text is read as a name", function() {
    deepEqual(mathMLValues(ARTC.string2Sequence("1;3aa;2.1", CALC.mathJS.constantParser).values), ["<cn> 1 </cn>", "3aa", "<cn> 2.1 </cn>"]);
});