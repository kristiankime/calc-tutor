if (!ARTC) {
    var ARTC = {};
}

// https://github.com/showdownjs/showdown
ARTC.markdown = function (idPrefix, text) {
    var converter = new showdown.Converter({"tables" : true});

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

    var markdown = converter.makeHtml(text);
    var ret = markdown;

    for (var key in replaces) {
        if (replaces.hasOwnProperty(key)) {
            var valueRaw = replaces[key];
            var valueRaw = "{" + valueRaw.substr(3, valueRaw.length - 6) + "}"
            try {
                var value = JSON.parse(valueRaw);
                var id = idPrefix + '_' + key.substr(1, key.length - 2);
                value["id"] = id;

                var width = value.xPixSize;
                width  = typeof width  !== 'undefined' ? width  : 200;
                value.xPixSize = width;

                var height = value.yPixSize;
                height  = typeof height  !== 'undefined' ? height  : 200;
                value.yPixSize = height;

                var rep = "<div id='" + id + "' class='inline-block' style='width:" + width + "px; height:" + height + "px;'></div> <script> ARTC.insertGraph(" + JSON.stringify(value) + ") </script>";
                ret = ret.replace(key, rep);
            } catch(e) {
                ret = ret.replace(key, "Could not parse [" + valueRaw + "] as graph data json");
            }
        }
    }

    return ret;
}

ARTC.setupDetails = function(model, idPrefix, raw, html) {
    // don't allow questionable characters in raw
    var toEnhance = model[raw];
    model[raw] = toEnhance.extend({ substitute : {} });

    // Create the computed Description Html from the raw
    model[html] = ko.pureComputed({
        read: function () {
            return ARTC.markdown(idPrefix, model[raw]());
        },
        write: function(value) {
            // NOTE: this is a hack so that the call to ko.mapping.fromJS will not overwrite this observable with a string
        },
        owner: model});
}