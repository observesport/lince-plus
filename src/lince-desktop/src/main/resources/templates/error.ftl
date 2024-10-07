<#import "macro/helper.ftl" as helper>
<#import "/spring.ftl" as spring/><#-- <@spring.message "front.home.label.legal.1.title"/>-->
<@layout>
<style type="text/css">
    .page-404 .number, .page-500 .number {
        letter-spacing: -10px;
        line-height: 128px;
        font-size: 128px;
        font-weight: 300
    }

    .page-404 .details, .page-500 .details {
        margin-left: 40px;
        display: inline-block
    }

    .page-404 {
        text-align: center
    }

    .page-404 .number {
        position: relative;
        top: 35px;
        display: inline-block;
        margin-top: 0;
        margin-bottom: 10px;
        color: #7bbbd6;
        text-align: right
    }

    .page-404-full-page .page-404, .page-500-full-page .page-500 {
        margin-top: 100px
    }

    .page-404 .details {
        padding-top: 0;
        text-align: left
    }

    .page-500 {
        text-align: center
    }

    .page-500 .number {
        display: inline-block;
        color: #ec8c8c;
        text-align: right
    }

    .page-500 .details {
        text-align: left
    }

    .page-404-full-page {
        overflow-x: hidden;
        padding: 20px;
        margin-bottom: 20px;
        background-color: #fafafa !important
    }

    .page-404-full-page .details input {
        background-color: #fff
    }

    .page-500-full-page {
        overflow-x: hidden;
        padding: 20px;
        background-color: #fafafa !important
    }

    .page-500-full-page .details input {
        background-color: #fff
    }

    .page-404-3 {
        background: #000 !important
    }

    .page-404-3 .page-inner img {
        right: 0;
        bottom: 0;
        z-index: -1;
        position: absolute
    }

    .page-404-3 .error-404 {
        color: #fff;
        text-align: left;
        padding: 70px 20px 0
    }

    .page-404-3 h1 {
        color: #fff;
        font-size: 130px;
        line-height: 160px
    }

    .page-404-3 h2 {
        color: #fff;
        font-size: 30px;
        margin-bottom: 30px
    }

    .page-404-3 p {
        color: #fff;
        font-size: 16px
    }

    @media (max-width: 480px) {
        .page-404 .details, .page-404 .number, .page-500 .details, .page-500 .number {
            text-align: center;
            margin-left: 0
        }

        .page-404-full-page .page-404 {
            margin-top: 30px
        }

        .page-404-3 .error-404 {
            text-align: left;
            padding-top: 10px
        }

        .page-404-3 .page-inner img {
            right: 0;
            bottom: 0;
            z-index: -1;
            position: fixed
        }
    }
</style>
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