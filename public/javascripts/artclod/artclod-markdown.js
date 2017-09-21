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
    // initial index
    var i = 0;
    var index = "$g" + i + "$";
    // initial potential match
    var match = text.match(pattern, index);
    match = (match == null ? null : match[0]); // we only want the first match (if it exists)
    text = text.replace(pattern, index);

    while(match != null) { // stop when we fail to find a match
        // We have a match so update matches
        replaces[index] = match;

        // Find the next potential match
        match = text.match(pattern, index);
        match = (match == null ? null : match[0]); // we only want the first match (if it exists)
        text = text.replace(pattern, index);

        // update the index
        i++;
        index = "$g" + i + "$";
    }

    var markdown = converter.makeHtml(text);
    var ret = markdown;

    for (var key in replaces) {
        if (replaces.hasOwnProperty(key)) {
            var value = replaces[key];
            var id = 'insert_' + key.substr(1, key.length-2);
            var rep = "<div id='" + id + "' style='width:100px; height:100px;'/> " + value + " <script> ARTC.insertGraph('" + id + "') </script>";
            ret = ret.replace(key, rep);
        }
    }

    console.log(ret);
    return ret;
}
