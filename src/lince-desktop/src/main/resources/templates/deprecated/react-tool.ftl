<#import "/spring.ftl" as spring/><#-- <@spring.message "front.home.label.legal.1.title"/>-->
<#import "macro/helper.ftl" as helper>
<#import "macro/components.ftl" as components>
<#assign txt_bread><@spring.message "front.label.tool.header"/></#assign>
<#assign txt_header><@spring.message "front.label.tool.title"/></#assign>
<@layout>
    <@helper.breadcrumb currentPage="${txt_bread}"/>
<div class="row" data-auto-height="true">
    <div class="col-lg-12 col-md-12 col-sm-12  col-xs-12">
        <@helper.portlet captionTitle="${txt_header}" icon="icon-control-play">
            <div id="toolConfig-app"></div>
        </@helper.portlet>
    </div>
</div>
<script src="/js/react/toolConfig-bundle.js" type="text/javascript"></script>
</@layout>