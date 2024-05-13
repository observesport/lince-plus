<#import "/spring.ftl" as spring/><#-- <@spring.message "front.home.label.legal.1.title"/>-->
<#assign txt_breadcrumb><@spring.message "front.layout.label.scenes"/></#assign>

<#import "macro/helper.ftl" as helper>
<#import "macro/components.ftl" as components>
<@layout>
    <@helper.breadcrumb currentPage="${txt_breadcrumb!''}"/>
<div class="row" data-auto-height="true">
    <div class="col-lg-12 col-md-12 col-sm-12  col-xs-12">
        <@helper.portlet captionTitle="Video" icon="icon-control-play">
            <div class="row" data-auto-height="true">
                <div class="col-lg-8 col-md-8 col-sm-8 col-xs-8">
                    <div>
                        <a href="#" class="pull-left" id="eventWriter">
                            <span class="label label-warning">
                                <i class="fa fa-floppy-o" aria-hidden="true"></i>&#160;<@spring.message "front.scenes.saveCurrentPosition"/>
                            </span>
                        </a>
                    </div>
                    <@components.videoGrid playlistItems=playlist/>
                </div>
                <div class="col-lg-4 col-md-4 col-sm-4 col-xs-4">
                    <div class="mt-element-ribbon bg-steel" style="padding: 0px !important;">
                        <div class="ribbon ribbon-right ribbon-clip ribbon-shadow ribbon-round ribbon-color-info uppercase">
                            <div class="ribbon-sub ribbon-clip ribbon-right"></div>
                            <@spring.message "front.scenes.detectedScenes"/>
                        </div>
                        <div class="ribbon-content">
                            <div id="videoRegisterContainer">
                                <@components.registerDataTable categories=categories videoRegister=videoRegister showCategories=false/>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </@helper.portlet>
    </div>
</div>
</@layout>
<script type="text/javascript">
    let eventWriter = "#eventWriter";
    let modalID = "#sceneData";
    let VIDEO_PLAYER_CLASS = ".video-js";
    let videoPlayerIDs = [];
    let videoJS_markers = [];
    let $videoPlayers = [];
    let videoPlay = function () {
        if (jQuery) {
            $videoPlayers.each(function (index) {
                console.log("play");
                videojs(this).play();
            });
        }
    };
    let videoSetTime = function (time) {
        if (jQuery) {
            console.log("currentTime" + time);
            $videoPlayers.each(function (index) {
                videojs( this).currentTime(time);
            });
        }
    };
    let videoPause = function () {
        if (jQuery) {
            console.log("pause");
            $videoPlayers.each(function (index) {
                videojs(this).pause();
            });
        }
    };
    let videoStop = function () {
        if (jQuery) {
            $videoPlayers.each(function (index) {
                console.log("stop");
                //this.currentTime = 0;
                videojs( this).currentTime(0);
                videojs(this).pause();
            });
        }
    };
    let videoPlaybackRateTo = function (rate) {
        if (jQuery) {
            $videoPlayers.each(function (index) {
                videojs(this).playbackRate(rate);
            });
        }
    };
    let videoSoundTo = function (q) {
        if (jQuery) {
            if (q >= 0 && q <= 1) {
                $videoPlayers.each(function (index) {
                    videojs(this).volume(q);
                });
            }
        }
    };
    let setVideoMarkers = function (videojsElement) {
        if (videojsElement.markers && videojsElement.markers.destroy) {
            videojsElement.markers.destroy();
        }
        videojsElement.markers({
            markerTip: {
                display: true,
                text: function (marker) {
                    return marker.text.toUpperCase();
                }
            },
            breakOverlay: {
                display: true,
                displayTime: 2,
                text: function (marker) {
                    return '<@spring.message "front.scenes.scene"/>  ' + marker.text;
                }
            },
            onMarkerReached: function (marker) {
                console.log(marker);
            },
            markers: videoJS_markers
        });
    };
    let videoSincronize = function () {
        if (jQuery) {
            if ($videoPlayers && $videoPlayers.length > 1) {
                videoSetTime(videojs($videoPlayers[0]).currentTime());
            }
        }
    };
    let loadVideoJSScenes = function () {
        if (jQuery) {
            $.linceApp.global.doAjax('/register/get/', 'GET', null, function (result) {
                videoJS_markers = [];
                result.forEach(function (element) {
                    videoJS_markers.push({
                        time: element.videoTime,
                        text: (!element.name) ? "t(" + element.videoTime + ")" : element.name
                    })
                });
                console.log(videoJS_markers);
                $videoPlayers.each(function (index) {
                    setVideoMarkers(videojs(this));
                });
            }, function (e) {
                console.log("error al cargar escenas")
            });
        }
    };
    let setSceneTableActions = function () {
        $(".btn-playscene").click(function (el) {
            let currentTime = $(this).data("time");
            console.log("-letsplay " + currentTime);
            videoSetTime(parseFloat(currentTime.toString().replace(',', '.')));
        });
        $(".btn-deletescene").click(function (el) {
            let jData = {};
            jData.moment = $(this).data("time").toString().replace(",", ".");
            console.log("--letsremove " + jData.moment);
            $.when($.linceApp.global.doAjax('/register/clear/', 'POST', jData, function (e) {
                toastr.success('<@spring.message "front.scenes.deleteOK"/>:' + jData.moment, '<@spring.message "front.scenes.delete"/>');
            }, function (e) {
                toastr.error('<@spring.message "front.scenes.deleteKO"/>', 'Error');
            })).done(function (data1) {
                loadScenesComponent();
                return true;
            });
        });
        //setKeyFrames();
    };
    let loadScenesComponent = function () {
        console.log("load scenes");
        $.linceApp.global.doAjax('/component/currentRegisteredScenes/', 'GET', null, function (result) {
            $("#videoRegisterContainer").html(result.responseText);
            setSceneTableActions();
            loadVideoJSScenes();
        }, function (e) {
            toastr.error('<@spring.message "front.scenes.loadKO"/>', '<@spring.message "front.scenes.load"/>');
        });
    };
    let openEditScene = function (btn) {
        $(modalID).modal('show');
        let $btn = $(btn);
        let currentElement = {
            id: $btn.data('id'),
            moment: $btn.data('moment'),
            name: $btn.data('name'),
            description: $btn.data('description')
        };
        setEditInputs(currentElement);
    };
    let setEditInputs = function (currentElement) {
        if (currentElement) {
            $("#scene_id").val(currentElement.id);
            $("#scene_t").val(currentElement.moment);
            $("#scene_name").val(currentElement.name);
            $("#scene_description").val(currentElement.description);
        }
    };
    let saveEditScene = function () {
        let currentElement = {
            id: $("#scene_id").val(),
            moment: $("#scene_t").val().toString().replace(",", "."),
            name: $("#scene_name").val(),
            description: $("#scene_description").val()
        };
        console.log(currentElement);
        $(modalID).modal('hide');
        setEditInputs(null);
        $.when($.linceApp.global.doAjax('/register/set/' + currentElement.id, 'POST', currentElement, function (e) {
            toastr.success('<@spring.message "front.scenes.saveOK"/>', '<@spring.message "front.scenes.save"/>');
        }, function (e) {
            toastr.error('<@spring.message "front.scenes.saveKO"/>: ' + currentElement.id, '<@spring.message "front.scenes.save"/>');
        })).done(function (data1) {
            let $btn = $("#scene-" + currentElement.id);
            $btn.data('moment', currentElement.moment);
            $btn.data('name', currentElement.name);
            $btn.data('description', currentElement.description);
            return true;
        });
    };

    jQuery(document).ready(function () {
        $videoPlayers = $(VIDEO_PLAYER_CLASS);
        let saveCurrentCriteriaSelection = function () {
            videoPause();
            let jData = {};
            jData.moment = videojs($videoPlayers[0]).currentTime();
            $.when($.linceApp.global.doAjax('/register/pushscene', 'POST', jData, function (e) {
                toastr.success('<@spring.message "front.scenes.saveOK"/>', '<@spring.message "front.scenes.save"/>');
                loadScenesComponent();
            }, function (e) {
                toastr.error('<@spring.message "front.scenes.saveKO"/>: ' + jData, '<@spring.message "front.scenes.save"/>');
                loadScenesComponent();
            })).done(function (data1) {
                videoPlay();
                return true;
            });
        };

        // initialize video.js
        $(".video-js").each(function (index) {
            videoPlayerIDs.push(this.id);
            let video = videojs(this);
        });
        loadVideoJSScenes();//load the marker plugin
        $(eventWriter).on("click", function (e) {
            e.preventDefault();
            saveCurrentCriteriaSelection();
        });
        setSceneTableActions();

        //grid swaper
        $(".btn-videogrid").click(function (el) {
            $(".btn-videogrid").removeClass("green");
            $(this).addClass("green");
            let selectedVideoCols = $(this).data("colval");
            $(".col-video").removeClass("col-sm-6 col-sm-4 col-sm-12").addClass(selectedVideoCols);
            let numVideos = 0;
            switch (selectedVideoCols) {
                case "col-sm-12":
                    numVideos = 1;
                    break;
                case "col-sm-6":
                    numVideos = 2;
                    break;
                case "col-sm-4":
                    numVideos = 3;
            }
            $.linceApp.especific.setVideoGridSize(numVideos);
        });
    });

</script>