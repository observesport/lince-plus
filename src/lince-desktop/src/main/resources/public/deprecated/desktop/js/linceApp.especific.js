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
(function ($) {
    if (!$.linceApp) {
        $.linceApp = {};
    }
    $.linceApp.especific = {
        //static vars for session parameter
        options: {
            menuExpand: "menuExpanded"
            , videoSize: "videoSize"
        },
        //static vars for helper methods
        constants: {
            menuToggleClass: "page-sidebar-closed"
            , menuToggleLabels: "page-sidebar-menu-closed"
        },
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
        //onclick for sidebar size
        setMenuSize: function (e) {
            var elem = $("body");
            var isActive = elem.hasClass($.linceApp.especific.constants.menuToggleClass);
            //console.log("status:" + isActive);
            $.linceApp.especific.storeLocal($.linceApp.especific.options.menuExpand, isActive);
        },
        //video player num simultaneos selection
        setVideoGridSize: function (numVideos) {
            console.log("Setting video grid" + numVideos);
            $.when($.linceApp.global.doAjax('/session/set/video_view/' + numVideos, 'GET', null, function (e) {
                console.log("selectedVideoCols:" + numVideos);
            }, function (e) {
                console.log(e);
            })).done(function (data1) {
                return true;
            });
        },
        //after loading customization
        onFinishLoadOperations: function () {
            var isExpand = $.linceApp.especific.loadLocal($.linceApp.especific.options.menuExpand);
            if (!isExpand) {
                //collapse menu if set
                $("body").toggleClass($.linceApp.especific.constants.menuToggleClass);
                $("ul.page-sidebar-menu").toggleClass($.linceApp.especific.constants.menuToggleLabels);
            }
        }
    }
})(window.jQuery);
