
test("ARTC.mathJS.prepFuncPow: no match return initial string", function() {
    equal(ARTC.mathJS.prepFuncPow("sin(x)", ["cos"]), "sin(x)");
});

test("ARTC.mathJS.prepFuncPow: only matches integer powers", function() {
    equal(ARTC.mathJS.prepFuncPow("cos^3.1(x)", ["cos"]), "cos^3.1(x)");
});

test("ARTC.mathJS.prepFuncPow: match puts power outside function call", function() {
    equal(ARTC.mathJS.prepFuncPow("cos^2(x)", ["cos"]), "(cos(x)^2)");
});

test("ARTC.mathJS.prepFuncPow: match puts power outside function call, extra string at front", function() {
    equal(ARTC.mathJS.prepFuncPow("3+cos^2(x)", ["cos"]), "3+(cos(x)^2)");
});

test("ARTC.mathJS.prepFuncPow: match puts power outside function call, extra string at end", function() {
    equal(ARTC.mathJS.prepFuncPow("cos^2(x)+3", ["cos"]), "(cos(x)^2)+3");
});

test("ARTC.mathJS.prepFuncPow: match works on all listed functions", function() {
    equal(ARTC.mathJS.prepFuncPow("cos^2(x)+sin^3(x+2)", ["cos", "sin"]), "(cos(x)^2)+(sin(x+2)^3)");
});

test("ARTC.mathJS.prepFuncPow: match works on nested functions", function() {
    equal(ARTC.mathJS.prepFuncPow("cos^2(sin^3(x+2))", ["cos", "sin"]), "(cos((sin(x+2)^3))^2)");
});


var testFunctionsEqual = function(f1, f2) {
    // console.log("check equals");
    for(var i = 0; i < 20; i = i + .1) {
        // console.log(f1(i) + " ?= " + f2(i));

        if(f1(i) != f2(i)) { return false; }
    }
    return true;
}

test("ARTC.mathJS.text2FunctionOfX: works on simple function", function() {
    equal(testFunctionsEqual(ARTC.mathJS.text2FunctionOfX("x"), function(x){return x;}), true);
});

test("ARTC.mathJS.text2FunctionOfX: works on pieicewise with one part", function() {
    equal(testFunctionsEqual(ARTC.mathJS.text2FunctionOfX("{{ x }}"), function(x){return x;}), true);
});

test("ARTC.mathJS.text2FunctionOfX: works on pieicewise with two parts", function() {
    equal(testFunctionsEqual(ARTC.mathJS.text2FunctionOfX("{{ x, if x < 5 // x+1 }}"), function(x){ if(x<5){return x}else{return x+1;}}), true);
});

test("ARTC.mathJS.text2FunctionOfX: works on pieicewise with three parts", function() {
    equal(testFunctionsEqual(ARTC.mathJS.text2FunctionOfX("{{ x, if x < 5 // x+1, if x < 10 // x+2 }}"), function(x){ if(x<5){return x}else if(x<10){return x+1}else{return x+2;}}), true);
});

test("ARTC.mathJS.text2FunctionOfX: pieicewise <=", function() {
    equal(testFunctionsEqual(ARTC.mathJS.text2FunctionOfX("{{ x, if x < 5 // x+1, if x <= 10 // x+2 }}"), function(x){ if(x<5){return x}else if(x<=10){return x+1}else{return x+2;}}), true);
});

test("ARTC.mathJS.text2FunctionOfX: pieicewise with fractions", function() {
    equal(testFunctionsEqual(ARTC.mathJS.text2FunctionOfX("{{ x, if x < .5 // x+1, if x < 10.5 // x+2 }}"), function(x){ if(x<.5){return x}else if(x<10.5){return x+1}else{return x+2;}}), true);
});