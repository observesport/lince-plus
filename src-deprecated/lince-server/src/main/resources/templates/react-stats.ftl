<#import "/spring.ftl" as spring/><#-- <@spring.message "front.home.label.legal.1.title"/>-->
<#import "macro/helper.ftl" as helper>
<#import "macro/components.ftl" as components>
<#assign txt_header><@spring.message "front.label.stats"/></#assign>
<#assign txt_h2><@spring.message "front.label.stats.basic"/></#assign>
<@layout>
    <@helper.breadcrumb currentPage="${txt_header}"/>
<div class="row" data-auto-height="true">
    <div class="col-lg-12 col-md-12 col-sm-12  col-xs-12">
        <@helper.portlet captionTitle="${txt_h2}" icon="icon-control-play">
           <div id="stats-app"></div>
        </@helper.portlet>
    </div>
</div>
<script src="/js/react/stats-bundle.js" type="text/javascript"></script>
</@layout>