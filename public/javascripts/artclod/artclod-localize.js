//http://stackoverflow.com/questions/6525538/convert-utc-date-time-to-local-date-time-using-javascript
if (!ARTC) {
    var ARTC = {};
}

ARTC.localize = function (t) {
    document.write(moment(new Date(t + " UTC")).format("M/D/YYYY h:mm:ss a"));
}