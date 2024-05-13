<#import "../macro/helper.ftl" as helper>
<#import "/spring.ftl" as spring/>
<#macro modal id="basic" modalTitle="" htmlContent="" onSave="">
<div class="modal fade" id="${id!}" tabindex="-1" role="basic" aria-hidden="true">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-hidden="true"></button>
                <h4 class="modal-title">${modalTitle!}</h4>
            </div>
            <div class="modal-body">
            ${htmlContent!}
            </div>
            <div class="modal-footer">
                <button type="button" class="btn dark btn-outline" data-dismiss="modal">Close</button>
                <button type="button" class="btn green" onclick="${onSave!"#"}">Save</button>
            </div>
        </div>
        <!-- /.modal-content -->
    </div>
    <!-- /.modal-dialog -->
</div>
</#macro>
<#macro registerDataTable categories videoRegister showCategories=true>
<div class="table-scrollable plugin-data-scroller">
    <table class="table table-striped table-bordered table-advance table-hover ">
        <thead>
        <tr>
            <th>
                <i class="fa fa-odnoklassniki"></i> <@spring.message "front.label.action"/>
            </th>
            <th>Id</th>
            <#if showCategories>
                <th>
                    <i class="fa fa-calendar-check-o"></i> <@spring.message "front.label.date"/>
                </th>
            </#if>
            <th class="hidden-xs">
                <i class="fa fa-hourglass-o"></i> <@spring.message "front.label.time"/>
            </th>
            <th class="hidden-xs">
                <i class="fa fa-television"></i> Frame
            </th>
            <#if showCategories>
                <#list categories as item>
                    <th>${item.code!item.name!""}</th>
                </#list>
            </#if>
        </tr>
        </thead>
        <tbody id="videoRegisterContainer">
            <#list videoRegister as videoItem>
            <tr>
                <td width="100">
                    <div class="btn-group btn-group-xs btn-group-solid">
                        <button type="button" class="btn red btn-playscene" data-time="${videoItem.videoTime!}"
                                style="margin-right: 0px!important;">
                            <i class="fa fa-share"></i>
                        </button>
                        <button type="button" class="btn green btn-editscene"
                                id="scene-${videoItem.id!}"
                                data-id="${videoItem.id!}"
                                data-moment="${videoItem.videoTime!}"
                                data-name="${videoItem.name!}"
                                data-description="${videoItem.description!}"
                                data-toggle="modal" onclick="openEditScene(this);"
                                style="margin-right: 0px!important;">
                            <i class="fa fa-edit"></i></button>
                        <button type="button" class="btn blue btn-deletescene" data-time="${videoItem.videoTime!}"
                                style="margin-right: 0px!important;">
                            <i class="fa fa-trash-o"></i>
                        </button>
                    </div>
                </td>
                <td>${videoItem.id!}</td>
                <#if showCategories>
                    <td class="highlight">
                        <#if videoItem.saveDate??>${videoItem.saveDate?string('dd/MM/yy HH:mm:ss')}</#if>
                    </td>
                </#if>
                <td class="hidden-xs"> ${videoItem.videoTimeTxt!videoItem.videoTime!} s</td>
                <td> ${videoItem.frames!videoItem.videoTime!} fr.</td>
                <#if showCategories>
                    <#list categories as item>
                        <td>
                            <#list videoItem.register as crit>
                                <#if item.id?? && crit.parent??>
                                    <#if crit.parent == item.id>
                                    ${crit.code!crit.name!""}
                                    </#if>
                                </#if>
                            </#list>
                        </td>
                    </#list>
                </#if>
            </tr>
            <#else>
            <tr>
                <td>Aún no hay registros</td>
            </tr>
            </#list>
        </tbody>
    </table>
    <#assign htmlContent>
        <form role="form" class="form-horizontal">
            <div class="form-body">
                <div class="form-group form-md-line-input">
                    <label class="col-md-2 control-label" for="scene_id">Id</label>
                    <div class="col-md-10">
                        <div class="col-md-5">
                            <input class="form-control input-xsmall " id="scene_id" placeholder="" type="text" readonly>
                        </div>
                        <label class="col-md-2">T</label>
                        <div class="col-md-3">
                            <input class="form-control input-xsmall" id="scene_t" placeholder="" type="text" readonly>
                            <div class="form-control-focus"></div>
                        </div>
                    </div>
                </div>
                <div class="form-group form-md-line-input">
                    <label class="col-md-2 control-label" for="scene_name"><@spring.message "front.label.name"/></label>
                    <div class="col-md-10">
                        <input class="form-control" id="scene_name" placeholder="Introduce nombre de escena"
                               type="text">
                        <div class="form-control-focus"></div>
                    </div>
                </div>
                <div class="form-group form-md-line-input">
                    <label class="col-md-2 control-label" for="scene_description"><@spring.message "front.label.description"/></label>
                    <div class="col-md-10">
                        <input class="form-control" id="scene_description" placeholder="Introduce descripción"
                               type="text">
                        <div class="form-control-focus"></div>
                    </div>
                </div>
            </div>
        </form>
    </#assign>
    <@modal id="sceneData" htmlContent=htmlContent modalTitle="Edición de escena" onSave="saveEditScene();"/>
</div>
</#macro>


<#macro videoComponent id="lincePlayer" file="">
<div class="videoPlayer" id="remote${id!"lincePlayer"}" style="width: 100%">

</div>
<div class="flow_player">
    <video class="video-container" controls="" width="100%" id="${id!"lincePlayer"}">
        <source src="/getVideo/test">
        Your browser does not support the video element.
    </video>
</div>
<div class="center-block videoPlayerButtons bg-grey-steel" style="width: 100%">
    <input type="file" id="videoFileLoader" class="hidden" accept="video/*"/>
    <div class="btn-group">
        <button type="button" class="btn btn-default" id="btn-Play">
            <i class="fa fa-play"></i> Play
        </button>
        <button type="button" class="btn btn-default" id="btn-Pause">
            <i class="fa fa-pause"></i> Pause
        </button>
        <button type="button" class="btn btn-default" id="btn-Stop">
            <i class="fa fa-stop"></i> Stop
        </button>
    </div>
    <div class="btn-group pull-right">
        <button type="button" class="btn btn-default" id="btn-SpeedDown">&#160;-&#160;</button>
        <button type="button" class="btn btn-default" id="btn-Speed0">
            <span><i class="fa fa-clock-o"></i>(x<span id="current_speed">1</span>)</span>
        </button>
        <button type="button" class="btn btn-default" id="btn-SpeedUp">&#160;+&#160;</button>
    </div>
</div>
</#macro>

<#macro videoComponentGlobalButtons>
<div class="center-block videoPlayerButtons">
    <span class="caption-subject font-green-sharp bold uppercase"><@spring.message "front.label.syncronizedActions"/></span>
    <div class="btn-group">
        <button type="button" class="btn btn-default" id="btn-Play" onclick="videoPlay();">
            <i class="fa fa-play"></i>
        </button>
        <button type="button" class="btn btn-default" id="btn-Pause" onclick="videoPause();">
            <i class="fa fa-pause"></i>
        </button>
        <button type="button" class="btn btn-default" id="btn-Stop" onclick="videoStop();">
            <i class="fa fa-stop"></i>
        </button>
        <div class="btn-group">
            <button type="button" class="btn btn-default" id="btn-Chrono"
                    data-toggle="dropdown" aria-haspopup="true"
                    aria-expanded="false">
                <i class="fa fa-clock-o"></i> <span class="caret"></span>
            </button>
            <ul class="dropdown-menu">
                <li><a href="#" onclick="videoPlaybackRateTo(0.5);">0.5 x</a></li>
                <li><a href="#" onclick="videoPlaybackRateTo(1);">1 x</a></li>
                <li><a href="#" onclick="videoPlaybackRateTo(2);">2 x</a></li>
            </ul>
        </div>
        <button type="button" class="btn btn-default" id="btn-Mute"
                onclick="videoSoundTo(0);">
            <i class="fa fa-bell-slash"></i> Mute
        </button>
        <button type="button" class="btn btn-default" id="btn-Sincro"
                onclick="videoSincronize();">
            <i class="fa fa-refresh"></i> Sync
        </button>
    </div>
    <div class="btn-group pull-right">
        <span class="font-green-sharp bold uppercase"><i class="fa fa-tasks"></i></span>
        <!--TODO: Reducir bloque de código(son las 2:00)-->
        <#if num_videos?? && num_videos=="col-sm-12">
            <button type="button" class="btn btn-default btn-videogrid green" data-colval="col-sm-12">1</button>
        <#else>
            <button type="button" class="btn btn-default btn-videogrid " data-colval="col-sm-12">1</button>
        </#if>
        <#if num_videos?? && num_videos=="col-sm-6">
            <button type="button" class="btn btn-default btn-videogrid green" data-colval="col-sm-6">2</button>
        <#else>
            <button type="button" class="btn btn-default btn-videogrid" data-colval="col-sm-6">2</button>
        </#if>
        <#if num_videos?? && num_videos=="col-sm-4">
            <button type="button" class="btn btn-default btn-videogrid green" data-colval="col-sm-4">3</button>
        <#else>
            <button type="button" class="btn btn-default btn-videogrid" data-colval="col-sm-4">3</button>
        </#if>
    </div>

</div>
</#macro>

<#macro videoJSComponent url="" type="">
<video class="video-container video-js vjs-default-skin vjs-fluid"
       controls preload="none" data-setup='{ "loop": true , "playbackRates": [0.1,0.2,0.4,0.6,0.8,1,2,4,6]}'>
    <source src="${url!"/getVideo/test"}" type="${type!"video/mp4"}">
    <p class="vjs-no-js">To view this video please enable JavaScript, and consider upgrading toa web browser that
        <a href="http://videojs.com/html5-video-support/" target="_blank">supports HTML5 video</a>
    </p>
</video>
</#macro>

<#macro videoGrid playlistItems>
<#--
<ul>
    <#list playlistItems?keys as id>
        <li>${id} = ${playlistItems[id]}</li>
    </#list>
</ul>
-->
<div class="videoPlayer" style="padding-top: 20px;">
    <div class="row">
        <div class="col-sm-12">
            <@videoComponentGlobalButtons/>
        </div>
    </div>
    <div class="row">
        <#list playlistItems?keys as key>
            <div class="col-video ${num_videos!"col-sm-6"}">
                <@videoJSComponent url="/getVideo/"+key type=playlistItems[key]/>
            </div>
        </#list>
    </div>
</div>
</#macro>