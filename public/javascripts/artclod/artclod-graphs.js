if (!ARTC) {
    var ARTC = {};
}

ARTC.insertGraph = function (inParams) {
    var params = Object.create(inParams);
    // ========================================================
    // Setup parameters for the board
    // ========================================================
    // Helpful links for JSX graph settings
    // https://www.intmath.com/cg3/jsxgraph-coding-summary.php
    // http://www.onemathematicalcat.org/JSXGraphDocs/generalAttributes.htm
    // https://www.intmath.com/cg3/jsxgraph-axes-ticks-grids.php

    // ==============
    // Params that are external to JSXGraph
    // ==============

    // html element id to use
    var id = params.id;
    if(typeof id === 'undefined') { throw "id was not defined" }

    // Should we add a glider
    var glider = typeof params.glider !== 'undefined' ? params.glider : false;

    // Function (of x) to display
    var func = typeof params.func !== 'undefined' ? params.func : "1";
    var mathF;
    try {
        mathF = ARTC.mathJS.text2FunctionOfX(func);
    } catch(e) {
        mathF = function(x){return 0;};
    }

    // ==============
    // Params that overwrite JSXGraph settings
    // ==============

    // Here we have a nicer way to do boundingbox
    var useMinMax = false;
    var xMin      = typeof params.xMin === 'undefined' ? -11 : useMinMax=true; params.xMin;
    var xMax      = typeof params.xMax === 'undefined' ?  11 : useMinMax=true; params.xMax;
    var yMin      = typeof params.yMin === 'undefined' ? -11 : useMinMax=true; params.yMin;
    var yMax      = typeof params.yMax === 'undefined' ?  11 : useMinMax=true; params.yMax;
    if(xMin > xMax) { xMin = xMax -1; }
    if(yMin > yMax) { yMin = yMax -1; }
    if(useMinMax) { // Create boundingbox from minmax
        params.boundingbox = [xMin, yMax, xMax, yMin];
    }

    // Set up a reasonable default zoom
    var zoom = typeof params.zoom !== 'undefined' ? params.zoom : { factorX: 1.25, factorY: 1.25, wheel: true, needshift: true, eps: 0.1 };
    params.zoom = zoom;

    // Set up a reasonable default pan
    var pan = typeof params.pan !== 'undefined' ? params.pan : { enabled:true, needshift: false}; // shift panning
    params.pan = pan;

    // default to showNavigation on
    var showNavigation = typeof params.showNavigation !== 'undefined' ? params.showNavigation : true;
    params.showNavigation = showNavigation;

    // default to copyright off
    var showCopyright = typeof params.showCopyright !== 'undefined' ? params.showCopyright : false;
    params.showCopyright = showCopyright;

    // default to axis on
    var axis = typeof params.axis !== 'undefined' ? params.axis : true;
    params.axis = axis;


    // var boardSettings = {
    //     axis: axis,
    //     boundingbox:[xMin,yMax,xMax,yMin],
    //     // originX: xPixSize/2,
    //     // originY: yPixSize/2,
    //     // unitX: xPixSize / xRange,
    //     // unitY: yPixSize / yRange,
    //     zoom :  { factorX: 1.25, factorY: 1.25, wheel: true, needshift: true, eps: 0.1 },
    //     pan : { enabled:true, needshift: false}, // shift panning
    //     showNavigation : true,
    //     showCopyright : false
    // };

    console.log("ARTC.insertGraph");
    console.log(params);


    // ========================================================
    // Actually create the board
    // ========================================================
    var board = JXG.JSXGraph.initBoard(id, params);

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