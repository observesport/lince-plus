<#import "macro/helper.ftl" as helper>
<#import "/spring.ftl" as spring/>
<#assign txt_aboutLince><@spring.message "front.home.label.advice"/></#assign>
<#assign txt_legalNotes><@spring.message "front.home.label.legal"/></#assign>
<#assign txt_aboutLince_1h><@spring.message "front.home.label.advice.1.title"/></#assign>
<#assign txt_aboutLince_1b><@spring.message "front.home.label.advice.1.msg"/></#assign>
<#assign txt_aboutLince_2h><@spring.message "front.home.label.advice.2.title"/></#assign>
<#assign txt_aboutLince_2b><@spring.message "front.home.label.advice.2.msg"/></#assign>
<#assign txt_aboutLince_3h><@spring.message "front.home.label.advice.3.title"/></#assign>
<#assign txt_aboutLince_3b><@spring.message "front.home.label.advice.3.msg"/></#assign>
<#assign txt_aboutLince_4h><@spring.message "front.home.label.advice.4.title"/></#assign>
<#assign txt_aboutLince_4b><@spring.message "front.home.label.advice.4.msg"/></#assign>
<#assign txt_aboutLince_5h><@spring.message "front.home.label.advice.5.title"/></#assign>
<#assign txt_aboutLince_5b><@spring.message "front.home.label.advice.5.msg"/></#assign>
<#assign ls = [
{"name":"${txt_aboutLince_1h}", "description":"${txt_aboutLince_1b}"},
{"name":"${txt_aboutLince_2h}", "description":"${txt_aboutLince_2b}"},
{"name":"${txt_aboutLince_3h}", "description":"${txt_aboutLince_3b}"},
{"name":"${txt_aboutLince_4h}", "description":"${txt_aboutLince_4b}"},
{"name":"${txt_aboutLince_5h}", "description":"${txt_aboutLince_5b}"}
]>
<@layout>
<!-- BEGIN PAGE HEADER-->
    <@helper.breadcrumb currentPage="Principal"/>
<!-- END PAGE HEADER-->
<div class="row" data-auto-height="true">
    <div class="col-lg-6 col-md-6 col-sm-12  col-xs-12">
        <div class="about-image"
             style="background: url('img/homeObserver.jpg') center no-repeat;height: 250px;width: 100%;"></div>
        <br/>
        <@helper.portlet captionTitle="${txt_aboutLince}" icon="icon-compass">
            <div class="row">
                <div class="col-md-3 col-sm-3">
                    <div class="btn-group btn-group-circle">
                        <button type="button" class="btn btn-default btn-next-advice">
                            <i class="fa fa-arrow-right" aria-hidden="true"></i></button>
                    </div>
                    <span id="advice-counter">1/${(ls?size)}</span>
                </div>
                <div class="col-md-9 col-sm-9">
                    <#list ls as i>
                        <div class="advice <#if i?index==0>show<#else>hidden</#if>"
                             id="advice-${(i?index)+1}" data-count="${(i?index)+1}">
                            <h3>${i.name}</h3>
                            <p>${i.description}</p>
                        </div>
                    </#list>
                </div>
            </div>
        </@helper.portlet>
    </div>
    <div class="col-md-6 col-sm-6">
        <@helper.portlet captionTitle="${txt_legalNotes}">
            <div class="panel-group accordion" id="accordion1">
                <div class="panel panel-default">
                    <div class="panel-heading">
                        <h4 class="panel-title">
                            <a class="accordion-toggle collapsed" data-toggle="collapse" data-parent="#accordion1"
                               href="#collapse_1" aria-expanded="true">
                                <@spring.message "front.home.label.legal.1.title"/>
                            </a>
                        </h4>
                    </div>
                    <div id="collapse_1" class="panel-collapse collapse in" aria-expanded="true">
                        <div class="panel-body"
                             style="height:360px; overflow-y:auto;">
                            <p>
                                <@spring.message "front.home.label.legal.1.msg_1"/>
                                <a href="https://developer.mozilla.org//en-US/docs/Web/HTML/Supported_media_formats#Browser_compatibility"
                                   target="_blank">
                                    <@spring.message "front.label.here"/>
                                </a>
                            </p>
                            <p><@spring.message "front.home.label.legal.1.msg_2"/></p>
                            <p><@spring.message "front.home.label.legal.1.msg_3"/>
                                <a href="http://easyhtml5video.com/index.html" target="_blank">EasyHtml5video</a></p>
                            <p>
                        </div>
                    </div>
                </div>
                <div class="panel panel-default">
                    <div class="panel-heading">
                        <h4 class="panel-title">
                            <a class="accordion-toggle" data-toggle="collapse" data-parent="#accordion1"
                               href="#collapse_2" aria-expanded="false">
                                <@spring.message "front.home.label.legal.2.title"/>
                            </a>
                        </h4>
                    </div>
                    <div id="collapse_2" class="panel-collapse collapse" aria-expanded="false" style="">
                        <div class="panel-body" style="height:360px; overflow-y:auto;">
                            <p><@spring.message "front.home.label.legal.2.msg.1"/></p>
                            <p><@spring.message "front.home.label.legal.2.msg.2"/></p>
                            <p><@spring.message "front.home.label.legal.2.msg.3"/></p>
                            <p><@spring.message "front.home.label.legal.2.msg.4"/></p>
                        </div>
                    </div>
                </div>
            </div>
        </@helper.portlet>
    </div>
</div>
</@layout>
<script type="text/javascript">
    jQuery(document).ready(function () {
        /**
         * Dummy code for searching next advice
         */
        $(".btn-next-advice").click(function (e) {
            var elem = $("div.advice.show");
            var current = elem.data("count");
            var max = $(".advice").size();
            var next = (current + 1) % max;
            if (next == 0) {
                next = max;
            }
            elem.removeClass("show");
            $("div.advice:not([class*='hidden'])").addClass("hidden");
            $("div#advice-" + next).addClass("show");
            $("div#advice-" + next).removeClass("hidden");
            $("#advice-counter").html(next + " / " + max);
        });
    });
</script>