if (!ARTC) {
    var ARTC = {};
}

if(!ARTC.mathJax) {
    ARTC.mathJax = {};
}

// Duplicate of underscore code so as not to import an entire library for one function.
ARTC.isFunction = function(obj) { return !!(obj && obj.constructor && obj.call && obj.apply); };

/*
 * Wrapper functions for "math" values that are put into ARTC.mathJax.updateById or ARTC.mathJax.updateByElem
 */
ARTC.mathJax.tex = function(tex){ return '$$' + tex + '$$'; };
ARTC.mathJax.asccii = function(asciimath){ return '`' + asciimath + '`'; };
ARTC.mathJax.mathml = function(mathML){ return '<script type="math\/mml"> ' + mathML + ' <\/script>'; };

ARTC.mathJax.updateByElem = (function() {
    /*
     * Update an HTML element with a string in content MathML format and then have MathJax render it.
     *
     * NOTE: This function will overwrite the inner HTML of an element.
     *
     * This functional is designed not to throw any errors but to call a callback function
     * with a status object indicating success or failure. If no callback is given errors are ignored.
     *
     * Arguments
     *   elem - The element to update
     *   math - The Math to put into the element. this will replace the innerHTML of the element so it must
     *          be something that MathJax can process.
     *          For convenience ARTC.mathJax.tex will wrap TeX properly,
     *          ARTC.mathJax.asccii will wrap AsciiMath
     *          ARTC.mathJax.mathml will wrap MathML (Presentation or Content)
     *   callback - function that will be called after the updated completes, it is called with finished object
     *
     * The finished object has three attributes
     *
     * success - a boolean indicating if everything went well
     * reason - a string which can be one of:
     *          "success" -> Indicating success, this is the only string where the success attribute will be true
     *          "elem falsey" -> The elem was falsey
     *          "math falsey" -> The math was falsey
     * details - an empty string on success or an error object given more details about the failure
     */
    var update = function (elem, math, callback) {
        var safeCallback = function () {  /* noop */; };
        if (ARTC.isFunction(callback)) {
            safeCallback = callback;
        }

        if (!elem) {
            safeCallback({ success: false, reason: "elem falsey", details: "elem was " + elem });
            return;
        }

        if (!math) {
            safeCallback({ success: false, reason: "math falsey", details: "math was " + math });
            return;
        }

        var oldDisplay = elem.style.display;
        if(oldDisplay == "none") {
            MathJax.Hub.Queue(
                function () {elem.innerHTML = math;},
                [ "Typeset", MathJax.Hub, elem ],
                safeCallback({ success: true, reason: "success", details: ""})
            );
        } else {
            MathJax.Hub.Queue(
                function () {elem.style.display='none'},
                function () {elem.innerHTML = math;},
                [ "Typeset", MathJax.Hub, elem ],
                function () {elem.style.display=oldDisplay},
                safeCallback({ success: true, reason: "success", details: ""})
            );
        }
    }

    return update;
}());

ARTC.mathJax.updateById = (function() {
    /*
     * Update an HTML element with a string in content MathML format and then have MathJax render it.
     *
     * NOTE: This function will overwrite the inner HTML of an element.
     *
     * This functional is designed not to throw any errors but to call a callback function
     * with a status object indicating success or failure. If no callback is given errors are ignored.
     *
     * Arguments
     *   id - The id of the element to update
     *   math - The Math to put into the element. this will replace the innerHTML of the element so it must
     *          be something that MathJax can process.
     *          For convenience ARTC.mathJax.tex will wrap TeX properly,
     *          ARTC.mathJax.asccii will wrap AsciiMath
     *          ARTC.mathJax.mathml will wrap MathML (Presentation or Content)
     *   callback - function that will be called after the updated completes, it is called with finished object
     *
     * The finished object has three attributes
     *
     * success - a boolean indicating if everything went well
     * reason - a string which can be one of:
     *          "success" -> Indicating success, this is the only string where the success attribute will be true
     *          "id falsey" -> The id was falsey
     *          "math falsey" -> The math was falsey
     *          "elem falsey" -> Could not find an element associated with the given id
     * details - an empty string on success or an error object given more details about the failure
     */
    var update = function (id, math, callback) {
        if (!id) {
            safeCallback({ success: false, reason: "id falsey", details: "id was " + id });
            return;
        }

        var elem = document.getElementById(id);

        ARTC.mathJax.updateByElem(elem, math, callback);
    }

    return update;
}());
