
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