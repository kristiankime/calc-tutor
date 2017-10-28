if (!ARTC) {
    var ARTC = {};
}

ARTC.insertGraphO = function (params) {
    ARTC.insertGraph(params.id, params.func, params.glider, params.xMin, params.xMax, params.yMin, params.yMax, params.xPixSize, params.yPixSize, params.axis);
};

ARTC.insertGraph = function (id, func, glider, xMin, xMax, yMin, yMax, xPixSize, yPixSize, axis) {
    func      = typeof func      !== 'undefined' ? func      : function(x){return x;};
    glider    = typeof glider    !== 'undefined' ? glider    : false;
    xMin      = typeof xMin      !== 'undefined' ? xMin      : -11;
    xMax      = typeof xMax      !== 'undefined' ? xMax      :  11;
    yMin      = typeof yMin      !== 'undefined' ? yMin      : -11;
    yMax      = typeof yMax      !== 'undefined' ? yMax      :  11;
    axis      = typeof axis      !== 'undefined' ? axis      : true; // https://www.intmath.com/cg3/jsxgraph-axes-ticks-grids.php
    // xPixSize  = typeof xPixSize  !== 'undefined' ? xPixSize  : 300;
    // yPixSize  = typeof yPixSize  !== 'undefined' ? yPixSize  : 300;

    if(xMin > xMax) { xMin = xMax -1; }
    if(yMin > yMax) { yMin = yMax -1; }
    // if(xPixSize < 1) { xPixSize = 1; }
    // if(yPixSize < 1) { yPixSize = 1; }

    var xRange = xMax - xMin;
    var yRange = yMax - yMin;

    // console.log("ARTC.insertGraph");
    // console.log("func=" + func
    //     + " glider=" + glider
    //     + " xMin=" + xMin
    //     + " xMax" + xMax
    //     + " yMin=" + yMin
    //     + " yMax=" + yMax
    //     + " xPixSize=" + xPixSize
    //     + " yPixSize=" + yPixSize
    //     + " axis=" + axis
    // );

    var mathF;
    try {
        mathF = ARTC.mathJS.text2FunctionOfX(func);
    } catch(e) {
        mathF = function(x){return 0;};
    }
    // console.log(mathF);

    // var board = JXG.JSXGraph.initBoard(id,{originX:50, originY:250, unitX:50, unitY:10, axis:true}); board.create('point',[1,5]);
    // board.create('point',[1,5]);

        // originX: xPixSize/2, originY: yPixSize/2,

    // https://www.intmath.com/cg3/jsxgraph-coding-summary.php
    // http://www.onemathematicalcat.org/JSXGraphDocs/generalAttributes.htm

    var boardSettings = {
        axis: axis,
        boundingbox:[xMin,yMax,xMax,yMin],
        // originX: xPixSize/2,
        // originY: yPixSize/2,
        // unitX: xPixSize / xRange,
        // unitY: yPixSize / yRange,
        zoom :  { factorX: 1.25, factorY: 1.25, wheel: true, needshift: true, eps: 0.1 },
        pan : { enabled:true, needshift: false}, // shift panning
        showNavigation : true,
        showCopyright : false};

    // console.log("ARTC.insertGraph");
    // console.log(boardSettings);

    var board = JXG.JSXGraph.initBoard(id, boardSettings);

    board.suspendUpdate();

    var g = board.create('functiongraph', [mathF, xMin, xMax], {strokeWidth: 3}); // http://jsxgraph.uni-bayreuth.de/docs/symbols/Functiongraph.html

    if(glider) {
        var glider = board.create('glider', [g]);

        var getMouseCoords = function(e, i) { // http://jsxgraph.uni-bayreuth.de/wiki/index.php/Browser_event_and_coordinates
            var cPos = board.getCoordsTopLeftCorner(e, i),
                absPos = JXG.getPosition(e, i),
                dx = absPos[0]-cPos[0],
                dy = absPos[1]-cPos[1];

            return new JXG.Coords(JXG.COORDS_BY_SCREEN, [dx, dy], board);
        }

        var down = function(e)  {
            var i;
            if (e[JXG.touchProperty]) {
                i = 0; // index of the finger that is used to extract the coordinates
            }
            var coords = getMouseCoords(e, i);

            glider.moveTo([coords.usrCoords[1], coords.usrCoords[2]]);
        }

        board.on('down', down);
    }
    board.unsuspendUpdate();

    return board;
};