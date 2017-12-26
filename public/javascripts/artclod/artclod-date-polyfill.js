// https://stackoverflow.com/questions/30528482/using-javascripts-date-toisostring-and-ignore-timezone#30528821
if (!Date.prototype.toLocalISOStringNoZ) {
    (function() {
        function pad(number) {
            if (number < 10) {
                return '0' + number;
            }
            return number;
        }

        Date.prototype.toLocalISOStringNoZ = function() {
            return this.getFullYear() +
                '-' + pad(this.getMonth() + 1) +
                '-' + pad(this.getDate()) +
                'T' + pad(this.getHours()) +
                ':' + pad(this.getMinutes()) +
                ':' + pad(this.getSeconds()) +
                '.' + (this.getMilliseconds() / 1000).toFixed(3).slice(2, 5)
                /* + 'Z' */ ;
        };
    }());
}

// https://stackoverflow.com/questions/1353684/detecting-an-invalid-date-date-instance-in-javascript
if (!Date.prototype.isValid) {
    (function() {
        Date.prototype.isValid = function() {
            return !isNaN(this.getTime());
        };
    }());
}