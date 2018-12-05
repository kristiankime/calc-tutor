// https://knockoutjs.com/documentation/extenders.html
ko.extenders.numeric = function(target, precision) {
    //create a writable computed observable to intercept writes to our observable
    var result = ko.pureComputed({
        read: target,  //always return the original observables value
        write: function(newValue) {
            var current = target(),
                roundingMultiplier = Math.pow(10, precision),
                newValueAsNum = isNaN(newValue) ? 0 : +newValue,
                valueToWrite = Math.round(newValueAsNum * roundingMultiplier) / roundingMultiplier;

            //only write if it changed
            if (valueToWrite !== current) {
                target(valueToWrite);
            } else {
                //if the rounded value is the same, but a different value was written, force a notification for the current field
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

ko.extenders.numericFunc = function(target, precisionFunction) {
    //create a writable computed observable to intercept writes to our observable
    var result = ko.pureComputed({
        read: target,  //always return the original observables value
        write: function(newValue) {
            var current = target(),
                roundingMultiplier = Math.pow(10, precisionFunction()),
                newValueAsNum = isNaN(newValue) ? 0 : +newValue,
                valueToWrite = Math.round(newValueAsNum * roundingMultiplier) / roundingMultiplier;

            //only write if it changed
            if (valueToWrite !== current) {
                target(valueToWrite);
            } else {
                //if the rounded value is the same, but a different value was written, force a notification for the current field
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

// https://www.codeproject.com/tips/793259/%2fTips%2f793259%2fUsing-Observable-And-Computed-for-Validation-in-Kn
ko.validation.rules['isLargerThan'] = {
    validator: function (val, otherVal) {
        otherVal = otherVal(); // important to use otherValue(), because ko.computed returns a function, and the value is inside that function
        return parseFloat(val) > parseFloat(otherVal);
    },
    message: 'This need to be larger than the First Number '
};

ko.validation.rules['isLessThan'] = {
    validator: function (val, otherVal) {
        otherVal = otherVal(); // important to use otherValue(), because ko.computed returns a function, and the value is inside that function
        return parseFloat(val) < parseFloat(otherVal);
    },
    message: 'This need to be less than the First Number '
};


ko.validation.registerExtenders();

// https://stackoverflow.com/questions/12301991/knockout-sanitize-numbers

// https://stackoverflow.com/questions/17048468/make-an-input-only-numeric-type-on-knockout
// ko.bindingHandlers.numeric = {
//     init: function (element, valueAccessor) {
//         $(element).on("keydown", function (event) {
//             // Allow: backspace, delete, tab, escape, and enter
//             if (event.keyCode == 46 || event.keyCode == 8 || event.keyCode == 9 || event.keyCode == 27 || event.keyCode == 13 ||
//                 // Allow: Ctrl+A
//                 (event.keyCode == 65 && event.ctrlKey === true) ||
//                 // Allow: . ,
//                 (event.keyCode == 188 || event.keyCode == 190 || event.keyCode == 110) ||
//                 // Allow: home, end, left, right
//                 (event.keyCode >= 35 && event.keyCode <= 39)) {
//                 // let it happen, don't do anything
//                 return;
//             }
//             else {
//                 // Ensure that it is a number and stop the keypress
//                 if (event.shiftKey || (event.keyCode < 48 || event.keyCode > 57) && (event.keyCode < 96 || event.keyCode > 105)) {
//                     event.preventDefault();
//                 }
//             }
//         });
//     }
// };


