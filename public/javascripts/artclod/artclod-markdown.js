if (!ARTC) {
    var ARTC = {};
}

// https://github.com/showdownjs/showdown
ARTC.markdown = function (text) {
    var converter = new showdown.Converter();


    // store all the matches we find here
    var replaces = {};
    // We're looking for this
    var pattern = /\$g\$.*?\$g\$/

    // Our initial variables
    var i = 0;
    var index = "$g" + i + "g$";
    var res = text.match(pattern, index);
    res = (res == null ? null : res[0]);
    text = text.replace(pattern, index);

    while(res != null) { // stop when we fail to find a match
        // We have a match so update matches
        replaces[index] = res;

        // Find the next potential match
        res = text.match(pattern, index);
        res = (res == null ? null : res[0]);
        text = text.replace(pattern, index);

        // update the index
        i++;
        index = "$g" + i + "g$";
    }

    // console.log(replaces);

    var markdown = converter.makeHtml(text);

    return markdown;
}
