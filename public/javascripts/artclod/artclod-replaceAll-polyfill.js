// http://stackoverflow.com/questions/1144783/replacing-all-occurrences-of-a-string-in-javascript
if (!String.prototype.replaceAll) {
    (function() {
        String.prototype.replaceAll = function(search, replacement) {
            var target = this;
            return target.replace(new RegExp(search, 'g'), replacement);
        };
    })();
}