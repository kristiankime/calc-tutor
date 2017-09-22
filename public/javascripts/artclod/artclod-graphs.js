//http://stackoverflow.com/questions/6525538/convert-utc-date-time-to-local-date-time-using-javascript
if (!ARTC) {
    var ARTC = {};
}

ARTC.insertGraphO = function (params) {
    ARTC.insertGraph(params.id, params.func, params.glider, params.xMin, params.xMax, params.yMin, params.yMax, params.xPixSize, params.yPixSize);
};

ARTC.insertGraph = function (id, func, glider, xMin, xMax, yMin, yMax, xPixSize, yPixSize) {
    func      = typeof func      !== 'undefined' ? func      : function(x){return x;};
    glider    = typeof glider    !== 'undefined' ? glider    : false;
    xMin      = typeof xMin      !== 'undefined' ? xMin      : -11;
    xMax      = typeof xMax      !== 'undefined' ? xMax      :  11;
    yMin      = typeof yMin      !== 'undefined' ? yMin      : -11;
    yMax      = typeof yMax      !== 'undefined' ? yMax      :  11;
    xPixSize  = typeof xPixSize  !== 'undefined' ? xPixSize  : 300;
    yPixSize  = typeof yPixSize  !== 'undefined' ? yPixSize  : 300;

    var xRange = xMax - xMin;
    var yRange = yMax - yMin;

    console.log(func);
    console.log(glider);
    console.log(xMin);
    console.log(xMax);
    console.log(yMin);
    console.log(yMax);
    console.log(xPixSize);
    console.log(yPixSize);

    var mathF;
    try {
        mathF = ARTC.mathJS.text2FunctionOfX(func);
    } catch(e) {
        mathF = function(x){return 0;};
    }
    console.log(mathF);

    // var board = JXG.JSXGraph.initBoard(id,{originX:50, originY:250, unitX:50, unitY:10, axis:true}); board.create('point',[1,5]);
    // board.create('point',[1,5]);

    var board = JXG.JSXGraph.initBoard(id, {axis: true, boundingbox:[xMin,yMax,xMax,yMin], originX: xPixSize/2, originY: yPixSize/2, unitX: xPixSize / xRange, unitY: yPixSize / yRange, showCopyright: false});
    board.suspendUpdate();
    var g = board.create('functiongraph', [mathF, xMin, xMax], {strokeWidth: 3});

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