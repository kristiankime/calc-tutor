
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
        // the replace is an artifact of my encoding.  Maybe I will use markdown instead.
        $(element).html(valueUnwrapped.replace(/\n/g, '<br>'));
        MathJax.Hub.Queue(["Typeset",MathJax.Hub,element]);
    }
};

// https://stackoverflow.com/questions/18016718/using-knockout-js-how-do-bind-a-date-property-to-a-html5-date-picker#18058410
ko.bindingHandlers.datetimeLocalPicker = {
    init: function (element, valueAccessor, allBindingsAccessor, viewModel) {
        // Register change callbacks to update the model if the control changes.
        ko.utils.registerEventHandler(element, "change", function () {
            var value = valueAccessor();
            value(new Date(element.value));
        });
    },
    // Update the control whenever the view model changes
    update: function (element, valueAccessor, allBindingsAccessor, viewModel) {
        var value = valueAccessor();
        if(value().isValid()) {
            element.value = value().toLocalISOStringNoZ();
        } else {
            var now = new Date();
            now.setMinutes(0, 0, 0); // doesn't need to be accurate down to the millisecond...
            value(now);
            element.value = now.toLocalISOStringNoZ();
        }
    }
};

// ko.bindingHandlers.datetimeLocalPicker = {
//     init: function (element, valueAccessor, allBindingsAccessor, viewModel) {
//         // Register change callbacks to update the model if the control changes.
//         ko.utils.registerEventHandler(element, "change", function () {
//             var value = valueAccessor();
//             value(new Date(element.value));
//         });
//     },
//     // Update the control whenever the view model changes
//     update: function (element, valueAccessor, allBindingsAccessor, viewModel) {
//         var value = valueAccessor();
//         element.value = value().toLocalISOStringNoZ();
//     }
// };


// https://stackoverflow.com/questions/7704268/formatting-rules-for-numbers-in-knockoutjs#7705174
ko.bindingHandlers.numericText = {
    update: function(element, valueAccessor, allBindingsAccessor) {
        var value = ko.utils.unwrapObservable(valueAccessor());
        var precision = ko.utils.unwrapObservable(allBindingsAccessor().precision) || ko.bindingHandlers.numericText.defaultPrecision;
        var formattedValue = value.toFixed(precision);

        ko.bindingHandlers.text.update(element, function() { return formattedValue; });
    },
    defaultPrecision: 2
};

ko.bindingHandlers.percentageToTextDifficulty = {
    update: function(element, valueAccessor, allBindingsAccessor) {
        var value = ko.utils.unwrapObservable(valueAccessor());
        var diffText = "5-Very Hard";
        if(value >= .95) {
            diffText = "1-Very Easy";
        } else if(value >= .85) {
            diffText = "2-Easy";
        } else if(value >= .75) {
            diffText = "3-Medium";
        } else if(value >= .5) {
            diffText = "4-Hard";
        } else {
            diffText = "5-Very Hard";
        }
        ko.bindingHandlers.text.update(element, function() { return diffText; });
    }
};