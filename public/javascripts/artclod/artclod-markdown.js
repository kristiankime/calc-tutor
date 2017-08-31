if (!ARTC) {
    var ARTC = {};
}

// https://github.com/showdownjs/showdown
ARTC.markdown = function (text) {
    var converter = new showdown.Converter();
    return converter.makeHtml(text);
}
