<#macro sayHi>hi!</#macro>
<#macro pageHeader >
<h3 class="page-title" style="margin-top: 5px;"> Lince
    <small>Multi-platform sport analysis software</small>
</h3>
</#macro>
<#macro breadcrumb homepage="Inicio" link="/" currentPage="Selección de video">
<div class="page-bar">
    <ul class="page-breadcrumb">
        <li>
            <i class="icon-home"></i>
            <a href="${link!"/"}">${homepage!"Inicio"}</a>
            <i class="fa fa-angle-right"></i>
        </li>
        <li>
            <span>${currentPage!}</span>
        </li>
    </ul>
    <div class="page-toolbar">
        <@pageHeader/>
        <div class="btn-group pull-right">
        </div>
    </div>
</div>
</#macro>

<#macro icoButton title="Notification" id="" code="" class="" href="javascript:;" badge="" onclick="" additionalClass="" dataGroup="">
<a href="${href!"#"}" class="icon-btn ${additionalClass!""}" data-code="${code!""}" data-id="${id!""}" data-group="${dataGroup!""}">
    <#if class?has_content><i class="${class!"fa fa-bullhorn"}"></i></#if>
    <div>${code!title!""} <#--if code?? && code!=""> (${code!""})&#160; </#if--></div>
    <#if badge?has_content><span class="badge badge-danger"> ${badge!"0"} </span></#if>
</a>
</#macro>

<#macro portlet captionTitle="Lince" captionAction="" captionButtons="" icon="icon-globe" bodyId="" portletId="">
<!-- BEGIN PORTLET-->
<div class="portlet light" <#if portletId?has_content>id="${portletId}"</#if>>
    <div class="portlet-title tabbable-line">
        <div class="caption">
            <i class="${icon!"icon-globe"} font-green-sharp"></i>
            <span class="caption-subject font-green-sharp bold uppercase">${captionTitle!}</span>
        </div>
        <div class="tools">
            <a href="javascript:;" class="fullscreen"> </a>
        </div>
        <div class="actions">
        ${captionAction!""}
        <#--<a class="btn btn-circle btn-icon-only btn-default fullscreen" href="#" data-original-title="" title=""> </a>-->
        </div>
    ${captionButtons!}
    <#--ul class="nav nav-tabs">
        <li class="active"><a href="#tab_1_1" class="active" data-toggle="tab"> Todos </a></li>
    </ul-->
    </div>
    <div class="portlet-body" <#if bodyId?has_content>id="${bodyId}"</#if>>
        <#nested>
    </div>
</div>
<!-- END PORTLET-->
</#macro>

<#--
 color: red-haze, green-sharp
-->
<#macro progressBox title="TOTAL" icon="icon-pie-chart" progressLabel="progress" progressValue="10" color="red-haze">
<div class="dashboard-stat2 ">
    <div class="display">
        <div class="number">
            <h3 class="font-${color!"green-sharp"}">
                <span data-counter="counterup" data-value="7800">${progressValue!"0"}</span>
                <small class="font-${color!"green-sharp"}">%</small>
            </h3>
            <small>${title!""}</small>
        </div>
        <div class="icon">
            <i class="${icon!"icon-pie-chart"}"></i>
        </div>
    </div>
    <div class="progress-info">
        <div class="progress">
            <span style="width: ${progressValue!"0"}%;"
                  class="progress-bar progress-bar-success ${color!"green-sharp"}">
                <span class="sr-only">${progressValue!"0"}% ${progressLabel!"progress"}</span>
            </span>
        </div>
        <div class="status">
            <div class="status-title"> ${progressLabel!"progress"}</div>
            <div class="status-number"> ${progressValue!"0"}%</div>
        </div>
    </div>
</div>
</#macro>

<#macro ribbon>
<div class="portlet light">
    <div class="mt-element-ribbon bg-grey-steel">
        <div class="ribbon ribbon-vertical-left ribbon-color-warning uppercase">
            <div class="ribbon-sub ribbon-bookmark"></div>
            <i class="fa fa-star"></i>
        </div>
        <p class="ribbon-content">Duis mollis, est non commodo luctus, nisi erat porttitor
            ligula</p>
    </div>
</div>
</#macro>

<#macro mtElementList>
<div class="mt-element-list">
    <div class="mt-list-head list-simple ext-1 font-white bg-green-sharp">
        <div class="list-head-title-container">
            <!--div class="list-date">Nov 8, 2015</div-->
            <h3 class="list-title">Simple List</h3>
        </div>
    </div>
    <div class="mt-list-container list-simple ext-1">
        <ul>
            <li class="mt-list-item done">
                <!--div class="list-icon-container">
                    <i class="icon-check"></i>
                </div-->
                <!--div class="list-datetime"> 8 Nov </div-->
                <div class="list-item-content">
                    <h3 class="uppercase">
                        <a href="javascript:;">Concept Proof</a>
                    </h3>
                </div>
            </li>
            <li class="mt-list-item">
                <!--div class="list-icon-container">
                    <i class="icon-close"></i>
                </div-->
                <!--div class="list-datetime"> 8 Nov </div-->
                <div class="list-item-content">
                    <h3 class="uppercase">
                        <a href="javascript:;">Listings Feature</a>
                    </h3>
                </div>
            </li>
            <li class="mt-list-item">
                <!--div class="list-icon-container">
                    <i class="icon-close"></i>
                </div-->
                <!--div class="list-datetime"> 8 Nov </div-->
                <div class="list-item-content">
                    <h3 class="uppercase">
                        <a href="javascript:;">Listings Feature</a>
                    </h3>
                </div>
            </li>
        </ul>
    </div>
</div>
</#macro>

<#macro wellBox title="">
<div class="well center-block">
    <#if title??>
        <h4><b>${title!""}</b></h4>
    </#if>
    <#nested>
</div>
</#macro>

<#--
default,primary,success, danger, warning
-->
<#macro label content="" type="default" id="">
<span class="label label-${type!"default"}" id="${id!""}">${content!""}</span>
</#macro>

<#-- info, success, -->
<#macro note type="success">
<div class="note note-${type!"success"}">
    <#nested>
</div>
</#macro>

<#macro alert title="Info" closable=true type="info">
<div class="alert alert-block alert-${type!"info"} fade in">
    <#if closable>
        <button type="button" class="close" data-dismiss="alert"></button>
    </#if>
    <h4 class="alert-heading">${title!""}</h4>
    <#nested>
</div>
</#macro>