/**
 * Created by Lorenzo on 23/02/2016.
 *
 * Requires:
 *
 *    Nothing
 *
 * Description: this file contains all globals functions that other plugins can use
 *
 */
'use strict';
String.prototype.format = function (args) {
    var newStr = this;
    for (var key in args) {
        newStr = newStr.replace('{' + key + '}', args[key]);
    }
    return newStr;
};
String.prototype.float = function() {
    return parseFloat(this.replace(',', '.'));
};
(function ($) {
    if (!$.linceApp) {
        $.linceApp = {};
    }
    $.linceApp.global = {

        /**
         * Print on console.log() with styles
         *
         * Levels:
         *
         *    0: Error       // Red
         *    1: Warning      // Yellow
         *    2: Success     // Green
         *    3: Info        // Blue
         *    other: Info    // Blue
         *
         * Formats:
         *
         *    showMessage(var);                           // Prints a simple var with default color
         *    showMessage(number, var, ..);               // Prints all vars with 1º color
         *    showMessage(number, var, number);           // Prints var and number with 1º color
         *    showMessage(number, var, number, var);      // Prints 1º var with 1º color and 2º var with 2º color
         *    showMessage(number, var, number, var, var); // Prints 1º var with 1º color, 2º var with 2º color, 3º var with 1º color
         *
         * Usage:
         *
         *    base.showMessage = function(){
     *        dwecProject.gFunctions.showMessage("PluginName", arguments);
     *    };
         *
         * @param pluginName
         * @param args[]
         */
        showMessage: function (pluginName, args) {


            function getColor(level) {

                switch (level) {

                    // Error
                    case 0:
                        return 'color: #AA0000;';
                    // Waring
                    case 1:
                        return 'color: #FFEA00;';
                    // Success
                    case 2:
                        return 'color: #004A00;';
                    // Info
                    case 3:
                        return 'color: #000AA0;';

                }
            }

            function print(level, text) {

                switch (level) {

                    // Error
                    case 0:
                        console.error(text);
                        break;
                    // Waring
                    case 1:
                        console.warn(text);
                        break;
                    // Success
                    case 2:
                        console.debug('%c' + text, 'color: #004A00;');
                        break;
                    // Info
                    case 3:
                        console.info('%c' + text, 'color: #000AA0;');
                        break;
                    // Object
                    case 4:
                        console.dir(text);
                        break;
                }
            }


            var ini = 0;
            var defaultC = 3;

            try {
                if (typeof (args[0]) === 'number') {
                    defaultC = args[0];
                    ini++;
                }

                console.groupCollapsed('%cdwecProject.' + pluginName + ':', getColor(defaultC));

                if (typeof (args) !== 'object') {
                    print(defaultC, args);
                }

                for (; ini < args.length; ini++) {

                    if (typeof (args[ini]) === 'number' && (typeof (args[ini + 1]) !== 'object' || ini === 0)) {
                        print(args[ini], args[ini + 1]);
                        ini++;
                    } else if (typeof (args[ini]) === 'object') {
                        print(4, args[ini]);
                    } else if (!args[ini]) {
                        print(defaultC, 'undefined')
                    } else {
                        print(defaultC, args[ini]);
                    }

                }

            } catch (e) {
                console.error(e);
            }
            console.groupEnd();
        },

        /**
         * Return hexa random color
         * @returns {string}
         */
        getRandomColor: function () {
            return "#" + ((1 << 24) * Math.random() | 0).toString(16);
        },

        /**
         * Return the language of browser
         * @returns {string}
         */
        getBrowserLanguage: function () {
            return window.navigator.language;
        },

        /**
         * Return a new random name
         * @param name
         * @returns {*}
         */
        getRandomName: function () {
            return Math.floor(Math.random() * 500000);
        }

        ,

        /**
         * Replace {id} by {id: "dwecProject1231"}
         * @param str
         * @param col
         * @returns {XML|string|void|*|{js, css}|{css, less}}
         */
        format: function (str, col) {
            col = typeof col === 'object' ? col : Array.prototype.slice.call(arguments, 1);
            return str.replace(/\{\{|\}\}|\{(\w+)\}/g, function (m, n) {
                if (m == "{{") {
                    return "{";
                }
                if (m == "}}") {
                    return "}";
                }
                return col[n];
            });
        }
        ,
        storeLocal: function (k, v) {
            var c = JSON.stringify(v);
            localStorage.setItem(k, c);
        },
        loadLocal: function (k) {
            var storedData = localStorage.getItem(k);
            var data = [];
            if (storedData) {
                data = JSON.parse(storedData);
            }
            return data;
        },
        doAjax: function (url, method, sendData, fOnSuccessCallback, fOnErrorCallback) {

            var showMessage = $.linceApp.global.showMessage.bind();

            if (!fOnSuccessCallback)
                fOnSuccessCallback = function () {
                };

            if (!fOnErrorCallback)
                fOnErrorCallback = function () {
                };

            var callBackFunctions = {

                200: function (response) {
                    fOnSuccessCallback(response);
                },
                201: function (response) {
                    fOnSuccessCallback(response);
                },
                204: function (response) {
                    fOnSuccessCallback();
                },
                400: function (response) {
                    showMessage(0, "400", response);
                    fOnErrorCallback();
                },
                404: function (response) {
                    showMessage(0, "404", response);
                    fOnErrorCallback();
                },
                500: function (response) {
                    showMessage(0, "500", response);
                    fOnErrorCallback();
                }
            };
            $.ajax({
                headers: {
                    'Accept': 'application/json',
                    'Content-Type': 'application/json'
                },
                url: url,
                method: method,
                dataType: 'json',
                data: (sendData) ? JSON.stringify(sendData) : "",
                statusCode: callBackFunctions
            });
        }
    }
})(window.jQuery);
