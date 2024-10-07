<#import "../macro/helper.ftl" as helper>
<#import "../macro/components.ftl" as components>
<@layout>
    <@helper.breadcrumb currentPage="Reproducción y análisis"/>
<div class="row" data-auto-height="true">
    <div class="col-lg-12 col-md-12 col-sm-12  col-xs-12">
        <@helper.portlet captionTitle="Toma jeroma" icon="icon-control-play">
            <ul>
                <#list valores as item>
                    <li>${item}</li>
                </#list>
                <#if></#if>
            </ul>

        </@helper.portlet>
    </div>
</div>
</@layout>