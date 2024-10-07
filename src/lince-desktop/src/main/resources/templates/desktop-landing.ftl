<#import "macro/helper.ftl" as helper>
<#import "/spring.ftl" as spring/><#-- <@spring.message "front.home.label.legal.1.title"/>-->
<@layout>
<!-- BEGIN PAGE HEADER-->
    <@helper.breadcrumb currentPage="Error"/>
<!-- END PAGE HEADER-->
<div class="row" data-auto-height="true">
    <div class="col-md-12 page-404">
        <div class="number font-green"> 404</div>
        <div class="details">
            <h3><@spring.message "front.404.title"/></h3>
            <p><@spring.message "front.404.body"/>
                <br>
                <a href="/"><@spring.message "front.404.mainLink"/></a>
            </p>
        </div>
    </div>
</div>
</@layout>