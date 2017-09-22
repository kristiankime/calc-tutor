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

        // update the index
        i = i+1;
        index = "$g" + i + "$";

        // Find the next potential match
        match = text.match(pattern, index);
        match = (match == null ? null : match[0]); // we only want the first match (if it exists)
        text = text.replace(pattern, index);
    }

    // console.log(replaces);
    // console.log(text);

    var markdown = converter.makeHtml(text);
    var ret = markdown;

    for (var key in replaces) {
        if (replaces.hasOwnProperty(key)) {
            var valueRaw = replaces[key];
            var valueRaw = "{" + valueRaw.substr(3, valueRaw.length - 6) + "}"
            try {
                var value = JSON.parse(valueRaw);
                var id = 'insert_' + key.substr(1, key.length - 2);
                var rep = "<div id='" + id + "' class='inline-block' style='width:100px; height:100px;'/> <script> ARTC.insertGraph('" + id + "') </script>";
                ret = ret.replace(key, rep);
            } catch(e) {
                ret = ret.replace(key, "Could not parse [" + valueRaw + "] as graph data json");
            }
        }
    }

    MathJax.Hub.Queue(["Typeset",MathJax.Hub]);
    console.log(ret);
    return ret;
}
