<#import "/spring.ftl" as spring/><#-- <@spring.message "front.home.label.legal.1.title"/>-->
<#import "macro/helper.ftl" as helper>
<#import "macro/components.ftl" as components>
<#macro noCategories>
    <@helper.wellBox title="No hay categorías">
    Puedes generar tus categorías o cargarlas en la opción "Configurar" del menú.
    </@helper.wellBox>
</#macro>
<#macro buttonContainer>
<div class="ribbon-content">
    <#list categories as item>
        <div class="center-block">
            <h4 class="text-center bg-yellow-gold bg-font-yellow-gold"
                style="margin-top: 5px !important;margin-bottom: 0px !important;">
            ${item.name!""} (Cod ${item.code!"NotDefined"})
            </h4>
            <#list item.innerCategories as cat>
                <@helper.icoButton title=cat.name id=cat.id!"" code=cat.code additionalClass='btn-cri cat-group'+item.id!"" dataGroup=item.id!""/>
            <#else>
                No existen categorías para este criterio
            </#list>
        </div>
    <#else>
        <@noCategories/>
    </#list>
</div>
</#macro>
<#macro buttonAccordionContainer>
<#-- Accordion version -->
<div class="ribbon-content panel-group accordion" id="accordion1">
    <#list categories as item>
        <div class="panel panel-default">
            <div class="panel-heading" id="panel-heading-${item.id!""}"> <!-- data-group=item.id -->
                <h4 class="panel-title">
                    <a class="accordion-toggle accordion-toggle-styled collapsed" data-toggle="collapse" data-parent="#accordion1"
                       href="#collapse_${item?index}"> ${item.name!""} (Cod ${item.code!"NotDefined"})</a>
                </h4>
            </div>
            <div id="collapse_${item?index}" class="panel-collapse collapse">
                <div class="panel-body" style="height:140px; overflow-y:auto;padding-top: 0px;padding-bottom: 0px;">
                    <#list item.innerCategories as cat>
                        <@helper.icoButton title=cat.name id=cat.id!"" code=cat.code additionalClass='btn-cri cat-group'+item.id!"" dataGroup=item.id!""/>
                    <#else>
                        No existen categorías para este criterio
                    </#list>
                </div>
            </div>
        </div>
    <#else>
        <@noCategories/>
    </#list>
</div>
</#macro>
<#assign nopadding="padding-left: 0px!important;padding-right: 0px!important;"/>
<@layout>
<#--@helper.pageHeader/-->
    <@helper.breadcrumb currentPage="Reproducción y análisis"/>
<div class="row" data-auto-height="true">
    <div class="col-lg-12 col-md-12 col-sm-12  col-xs-12">
        <@helper.portlet captionTitle="Video" icon="icon-control-play">
            <div class="row" data-auto-height="true">
                <div class="col-lg-7 col-md-7 col-sm-12  col-xs-12" style="${nopadding}">
                    <@components.videoGrid playlistItems=playlist/>
                </div>
                <div class="col-lg-5 col-md-5 col-sm-12  col-xs-12" style="${nopadding}">
                    <div class="mt-element-ribbon bg-<#--grey--->steel" style="padding-top: 5px !important;">
                        <a href="#" class="pull-left hidden" id="eventWriter">
                            <span class="label label-warning">
                                <i class="fa fa-floppy-o" aria-hidden="true"></i>&#160;Guardar cambios
                            </span>
                        </a>
                        <div class="ribbon ribbon-right ribbon-clip ribbon-shadow ribbon-round ribbon-color-info uppercase">
                            <div class="ribbon-sub ribbon-clip ribbon-right"></div>
                            Criterios y categorías
                        </div>
                        <#if categories?size <= 6 >
                            <@buttonContainer/>
                        <#else>
                            <@buttonAccordionContainer/>
                        </#if>
                    </div>
                </div>
            </div>
            <!-- BEGIN Portlet PORTLET-->
            <div class="row" data-auto-height="true">
                <div class="col-lg-12 col-md-12 col-sm-12  col-xs-12" style="${nopadding}">
                    <div class="portlet box <#--yellow-gold-->" style="margin-bottom: 0px !important;">
                        <div class="portlet-body">
                            <div class="scroller" style="height:210px">
                                <div id="videoRegisterContainer">
                                    <@components.registerDataTable categories=categories videoRegister=videoRegister showCategories=true/>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            <!-- END Portlet PORTLET-->
        </@helper.portlet>
    </div>
</div>
</@layout>
<script type="text/javascript">
    const eventWriter = "#eventWriter";
    const modalID = "#sceneData";
    const VIDEO_PLAYER_CLASS = ".video-js";
    const CLS_SAVED_SELECTION = "bg-green-jungle bg-font-green-jungle";
    const CLS_CURRENT_SELECTION = "bg-yellow-crusta bg-font-yellow-crusta";
    const CLS_ELEM_BTN_CRITERIA = "btn-cri";
    var videoPlayerIDs = [];
    var videoJS_markers = [];
    var $videoPlayers = [];
    var videoPlay = function () {
        if (jQuery) {
            $videoPlayers.each(function (index) {
                console.log("play");
                videojs(this).play();
            });
        }
    };
    var videoSetTime = function (time) {
        if (jQuery) {
            console.log("currentTime" + time);
            $videoPlayers.each(function (index) {
                videojs(this).currentTime(time);
            });
        }
    };
    var videoPause = function () {
        if (jQuery) {
            console.log("pause");
            $videoPlayers.each(function (index) {
                videojs(this).pause();
            });
        }
    };
    var videoStop = function () {
        if (jQuery) {
            $videoPlayers.each(function (index) {
                console.log("stop");
                //this.currentTime = 0;
                videojs(this).currentTime(0);
                videojs(this).pause();
            });
        }
    };
    var videoPlaybackRateTo = function (rate) {
        if (jQuery) {
            $videoPlayers.each(function (index) {
                videojs(this).playbackRate(rate);
            });
        }
    };
    var videoSoundTo = function (q) {
        if (jQuery) {
            if (q >= 0 && q <= 1) {
                $videoPlayers.each(function (index) {
                    videojs(this).volume(q);
                });
            }
        }
    };
    var setVideoMarkers = function (videojsElement) {
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
                    return "Escena " + marker.text;
                }
            },
            onMarkerReached: function (marker) {
                console.log(marker);
            },
            markers: videoJS_markers
        });
    };
    var videoSincronize = function () {
        if (jQuery) {
            if ($videoPlayers && $videoPlayers.length > 1) {
                videoSetTime(videojs($videoPlayers[0]).currentTime());
            }
        }
    };
    var loadVideoJSScenes = function () {
        if (jQuery) {
            $.linceApp.global.doAjax('/register/get/', 'GET', null, function (result) {
                console.log("register ajax get");
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
    var setSceneTableActions = function () {
        $(".btn-playscene").click(function (el) {
            var currentTime = $(this).data("time");
            console.log("-letsplay " + currentTime);
            videoSetTime(parseFloat(currentTime.replace(",", ".")));
        });
        $(".btn-deletescene").click(function (el) {
            var jData = {};
            jData.moment = $(this).data("time").replace(",", ".");
            console.log("--letsremove " + jData.moment);
            $.when($.linceApp.global.doAjax('/register/clear/', 'POST', jData, function (e) {
                toastr.success('Registro(s) en el instante ' + jData.moment + ' eliminados correctamente', 'Eliminación de datos');
            }, function (e) {
                toastr.error('Datos NO eliminados', 'Error');
            })).done(function (data1) {
                loadScenesComponent();
                return true;
            });
        });
        //setKeyFrames();
    };
    var loadScenesComponent = function () {
        console.log("load scenes");
        $.linceApp.global.doAjax('/component/currentRegisteredScenes/', 'GET', null, function (result) {
            $("#videoRegisterContainer").html(result.responseText);
            setSceneTableActions();
            loadVideoJSScenes();
        }, function (e) {
            toastr.error('Ocurre un error al guardar los registros', 'Cargando datos');
        });
    };
    var openEditScene = function (btn) {
        $(modalID).modal('show');
        var $btn = $(btn);
        var currentElement = {
            id: $btn.data('id'),
            moment: $btn.data('moment'),
            name: $btn.data('name'),
            description: $btn.data('description')
        };
        setEditInputs(currentElement);
    };
    var setEditInputs = function (currentElement) {
        if (currentElement) {
            $("#scene_id").val(currentElement.id);
            $("#scene_t").val(currentElement.moment);
            $("#scene_name").val(currentElement.name);
            $("#scene_description").val(currentElement.description);
        }
    };
    var saveEditScene = function () {
        var currentElement = {
            id: $("#scene_id").val(),
            moment: $("#scene_t").val().replace(",", "."),
            name: $("#scene_name").val(),
            description: $("#scene_description").val()
        };
        console.log(currentElement);
        $(modalID).modal('hide');
        setEditInputs(null);
        $.when($.linceApp.global.doAjax('/register/set/' + currentElement.id, 'POST', currentElement, function (e) {
            toastr.success('Datos de escena actualizados', 'Datos guardados');
        }, function (e) {
            toastr.error('Ocurre un error al guardar los datos: ' + currentElement.id, 'Datos NO guardados');
        })).done(function (data1) {
            var $btn = $("#scene-" + currentElement.id);
            $btn.data('moment', currentElement.moment);
            $btn.data('name', currentElement.name);
            $btn.data('description', currentElement.description);
            return true;
        });
    };
    var setRegisterAction = function () {
        $(".btn-playscene").click(function (el) {
            var currentTime = $(this).data("time");
            console.log("-letsplay " + currentTime);
            videoSetTime(parseFloat(currentTime.replace(",", ".")));
        });
        $(".btn-deletescene").click(function (el) {
            var currentTime = $(this).data("time");
            console.log("--letsremove " + currentTime);
            $.when($.linceApp.global.doAjax('/register/clear/' + currentTime, 'GET', null, function (e) {
                toastr.success('Registro(s) en el instante ' + currentTime + ' eliminados correctamente', 'Eliminación de datos');
                loadCurrentSavedCriteria();
            }, function (e) {
                toastr.error('Datos NO eliminados', 'Error');
            })).done(function (data1) {
                return true;
            });
        });
    };
    var loadCurrentSavedCriteria = function () {
        $.linceApp.global.doAjax('/component/currentRegisteredDataTable/', 'GET', null, function (result) {
            $("#videoRegisterContainer").html(result.responseText);
            setRegisterAction();
        }, function (e) {
            toastr.error('Ocurre un error al guardar los registros', 'Cargando datos');
        });
    };
    var saveCurrentCriteriaSelection = function () {
        videoPause();
        console.log("saveCurrentCriteriaAction");
        var elemSelected = $("a.bg-yellow-crusta");
        var arrData = [];
        elemSelected.each(function (elem) {
            arrData.push({"id": parseInt($(this).data('id')), "code": $(this).data('code')});
        });
        var currentElement = {
            //id: $("#scene_id").val(),
            moment: videojs($videoPlayers[0]).currentTime(),
            //name: $("#scene_name").val(),
            //description: $("#scene_description").val(),
            categories: arrData
        };
        $.when($.linceApp.global.doAjax('/register/setMomentData/', 'POST', currentElement, function (e) {
            toastr.success('Selección de criterios guardada', 'Datos guardados');
        }, function (e) {
            toastr.error('Ocurre un error al guardar los datos: ' + arrData, 'Datos NO guardados');
        })).done(function (data1) {
            $("a." + CLS_ELEM_BTN_CRITERIA).removeClass(CLS_CURRENT_SELECTION);
            $(eventWriter).hide();
            loadCurrentSavedCriteria();
            videoPlay();
            return true;
        });
    };
    var setCriteriaAction = function (elem) {
        //header menu color: $elem.parent().parent().prev().addClass(CLS_CURRENT_SELECTION)
        //console.log("criteriaAction");
        var $elem = $(elem);
        if ($elem.hasClass(CLS_ELEM_BTN_CRITERIA)) {
            var currentGroup = $elem.data('group');
            var eParentHeader = $("#panel-heading-" + currentGroup);
            var isActived = false;
            //console.log($elem);
            if ($elem.hasClass(CLS_CURRENT_SELECTION)) {
                isActived = true;
                console.log("is already actived-");
                eParentHeader.removeClass(CLS_CURRENT_SELECTION);
            }
            $(".cat-group" + currentGroup).removeClass(CLS_CURRENT_SELECTION);
            if (!isActived) {
                eParentHeader.addClass(CLS_CURRENT_SELECTION);
                $elem.toggleClass(CLS_CURRENT_SELECTION);
               // $("#panel-heading-" + currentGroup).removeClass(CLS_CURRENT_SELECTION);
            }
            $(eventWriter).show().removeClass("hidden");
        }

        //si existe alguno con current_selection, es que no se ha salvado, asi que mostramos el botón de guardar
        //var need2Save = $("." + CLS_CURRENT_SELECTION);
    };
    jQuery(document).ready(function () {
        $videoPlayers = $(VIDEO_PLAYER_CLASS);
        // initialize video.js
        $(".video-js").each(function (index) {
            videoPlayerIDs.push(this.id);
            var video = videojs(this);
        });
        loadVideoJSScenes();//load the marker plugin
        $(eventWriter).on("click", function (e) {
            e.preventDefault();
            saveCurrentCriteriaSelection();
        });
        setSceneTableActions();
        //grid swaper: Modifys Video behaviour
        $(".btn-videogrid").click(function (el) {
            $(".btn-videogrid").removeClass("green");
            $(this).addClass("green");
            var selectedVideoCols = $(this).data("colval");
            $(".col-video").removeClass("col-sm-6 col-sm-4 col-sm-12").addClass(selectedVideoCols);
            var numVideos = 0;
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
        $('a').on("click", function () {
            videoPause();
            setCriteriaAction(this);
        });
    });

</script>