
// http://knockoutjs.com/documentation/extenders.html
ko.extenders.substitute = function(target, options) {
    //create a writable computed observable to intercept writes to our observable
    var result = ko.pureComputed({

        // always return the original observables value
        read: target,

        // replace bad characters
        write: function(newValue) {
            var current = target();

            var valueToWrite = newValue;
            var valueToWrite = valueToWrite.replace("−", "-"); // Note the replaced minus here is the unicode minus

            //only write if it changed
            if (valueToWrite !== current) {
                target(valueToWrite);
            } else {
                //if the new value is the same, but a different value was written, force a notification for the current field
                if (newValue !== current) {
                    target.notifySubscribers(valueToWrite);
                }
            }
        }
    }).extend({ notify: 'always' });

    //initialize with current value to make sure it is rounded appropriately
    result(target());

    //return the new computed observable
    return result;
};


// https://stackoverflow.com/questions/19304643/mathjax-knockout-js-subscription
ko.bindingHandlers.mathjax = {
    update: function(element, valueAccessor, allBindingsAccessor, viewModel, bindingContext) {
        // This will be called once when the binding is first applied to an element,
        // and again whenever the associated observable changes value.
        // Update the DOM element based on the supplied values here.
        var value = valueAccessor(), allBindings = allBindingsAccessor();

        var valueUnwrapped = ko.unwrap(value);
        // the replace is an artefact of my encoding.  Maybe I will use markdown instead.
        $(element).html(valueUnwrapped.replace(/\n/g, '<br>'));
        MathJax.Hub.Queue(["Typeset",MathJax.Hub,element]);
    }
};