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

if (!Date.prototype.plusDays) {
    (function() {
        Date.prototype.plusDays = function(days) {
            var copy = new Date(this.getTime());
            copy.setDate(copy.getDate() + days);
            return copy;
        };
    }());
}

if (!Date.prototype.toUTCStringNoZ) {
    (function() {
        function pad(number) {
            if (number < 10) {
                return '0' + number;
            }
            return number;
        }

        Date.prototype.toUTCStringNoZ = function() {
            return this.getFullYear()
                + '-' + pad(this.getUTCMonth() + 1)
                + '-' + pad(this.getUTCDate())
                + 'T' + pad(this.getUTCHours())
                + ':' + pad(this.getUTCMinutes())
                + ':' + pad(this.getUTCSeconds())
                + '.' + (this.getUTCMilliseconds() / 1000).toFixed(3).slice(2, 5)
                /* + 'Z' */ ;
        };
    }());
}

if (!Date.prototype.toUTCSecStringNoZ) {
    (function() {
        function pad(number) {
            if (number < 10) {
                return '0' + number;
            }
            return number;
        }

        Date.prototype.toUTCSecStringNoZ = function() {
            return this.getFullYear()
                + '-' + pad(this.getUTCMonth() + 1)
                + '-' + pad(this.getUTCDate())
                + 'T' + pad(this.getUTCHours())
                + ':' + pad(this.getUTCMinutes())
                + ':' + pad(this.getUTCSeconds());
        };
    }());
}