<#import "/spring.ftl" as spring/><#-- <@spring.message "front.home.label.legal.1.title"/>-->
<#import "macro/helper.ftl" as helper>
<#import "macro/components.ftl" as components>
<#assign txt_main><@spring.message "front.label.result_header"/></#assign>
<@layout>
    <@helper.breadcrumb currentPage="${txt_main}"/>
<div class="row" data-auto-height="true">
    <div class="col-lg-12 col-md-12 col-sm-12  col-xs-12">
        <div id="results-app"></div>
    </div>
</div>
<script src="/js/react/results-bundle.js" type="text/javascript"></script>
</@layout>