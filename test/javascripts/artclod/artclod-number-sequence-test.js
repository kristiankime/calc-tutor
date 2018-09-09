
test("ARTC.string2Numbers: empty fails", function() {
    equal(ARTC.string2Numbers("").success, false);
});

test("ARTC.string2Numbers: 1 succeeds", function() {
    deepEqual(ARTC.string2Numbers("1").values, [1]);
});

test("ARTC.string2Numbers: 1;2 succeeds", function() {
    deepEqual(ARTC.string2Numbers("1;2").values, [1, 2]);
});

test("ARTC.string2Numbers: parses numbers separated by ;", function() {
    deepEqual(ARTC.string2Numbers("1;3;2.1").values, [1, 3, 2.1]);
});

test("ARTC.string2Numbers: numbers can periods", function() {
    deepEqual(ARTC.string2Numbers("1000.21;3000;2000.1").values, [1000.21, 3000, 2000.1]);
});

test("ARTC.string2Numbers: numbers can contain commas and periods", function() {
    deepEqual(ARTC.string2Numbers("1,000.21;3,000;2,000.1").values, [1000.21, 3000, 2000.1]);
});

test("ARTC.string2Numbers: fails to parse random text ", function() {
    deepEqual(ARTC.string2Numbers("1;3aa;2.1").success, false);
});