<#import "macro/helper.ftl" as helper>
<#import "macro/components.ftl" as components>
<@layout>
    <@helper.breadcrumb currentPage="Reproducción y análisis"/>
<div class="row" data-auto-height="true">
    <div class="col-lg-12 col-md-12 col-sm-12  col-xs-12">
        <@helper.portlet captionTitle="Video" icon="icon-control-play">
           <div id="sandbox-app"></div>
        </@helper.portlet>
    </div>
</div>
<#--<script src="/js/react/sandbox-bundle.js" type="text/javascript"></script>-->
</@layout>