<#import "macro/helper.ftl" as helper>
<#import "/spring.ftl" as spring/> <#-- <@spring.message "front.home.label.legal.1.title"/>-->
<#assign txt_header_categories><@spring.message "front.label.categories"/></#assign>
<#assign txt_header2_categories><@spring.message "front.categories.header"/></#assign>
<#assign txt_loadError><@spring.message "front.categories.loadError"/></#assign>
<#assign txt_saveOK><@spring.message "front.categories.saveOK.header"/></#assign>
<#assign txt_saveOK_body><@spring.message "front.categories.saveOK.body"/></#assign>
<#assign txt_saveKO><@spring.message "front.categories.saveKO.header"/></#assign>
<#assign txt_saveKO_body><@spring.message "front.categories.saveKO.body"/></#assign>
<#if locale!="en">
    <#assign json_scheme_config = "scheme/categoryCollection_"+locale+".json"/>
<#else>
    <#assign json_scheme_config = "scheme/categoryCollection.json"/>
</#if>
<@layout>
    <@helper.breadcrumb currentPage="${txt_header_categories}"/>
<div class="row" data-auto-height="true">
    <div class="col-lg-12 col-md-12 col-sm-12  col-xs-12">
        <@helper.portlet captionTitle="${txt_header2_categories}" icon="icon-control-play">
            <button type="button" class="btn btn-info btn-getValue-categories btn-saveCategoryData" data-gen="false">
                <@spring.message "front.label.save"/>
            </button>
            <div id="categoryEditor"></div>
            <button type="button" class="btn btn-info btn-getValue-categories btn-saveCategoryData" data-gen="false">
                <@spring.message "front.label.save"/>
            </button>
        </@helper.portlet>
    </div>
</div>
</@layout>
<!-- json editor plugins -->
<script src="js/jquery.browser.min.js"></script>
<script src="js/jsoneditor.js"></script>
<!-- -->
<script type="text/javascript">
    var loadCategories;
    var currentCriteria;
    var jsonEditor;
    jQuery(document).ready(function () {
        JSONEditor.defaults.theme = 'bootstrap3';
        JSONEditor.defaults.options.iconlib = "fontawesome4";
        JSONEditor.plugins.sceditor.emoticonsEnabled = false;
        //http://jeremydorn.com/json-editor/
        //https://rawgit.com/jdorn/json-editor/master/examples/advanced.html
        //https://github.com/jdorn/json-editor
        jsonEditor = new JSONEditor(jQuery('#categoryEditor')[0], {
            ajax: true,// Enable fetching schemas via ajax
            schema: {$ref: "${json_scheme_config}", format: "table"},// The schema for the editor
            disable_edit_json: true,
            disable_properties: true,
            disable_collapse: true,
            disableHeader: true,
            startval: currentCriteria, // Seed the form with a starting value
            no_additional_properties: true, // Disable additional properties
            required_by_default: true // Require all properties by default
        });
        loadCategories = function () {
            $.linceApp.global.doAjax('/datatable/categories', 'GET', null, function (result) {
                currentCriteria = result.data;
                jsonEditor.setValue(currentCriteria);
                console.log(currentCriteria);
            }, function (e) {
                console.log("error al cargar escenas");
            });
        };
        jsonEditor.on('ready', function () {
            loadCategories();
        });
        $(".btn-saveCategoryData").click(function (e) {
            var currentElement = {data: jsonEditor.getValue()};
            //console.log(currentElement);
            var doGen = true;//$(this).data("gen");
            //console.log(doGen);
            $.when($.linceApp.global.doAjax('/categories/saveAll/' + doGen, 'POST', currentElement, function (e) {
                toastr.success('${txt_saveOK}', '${txt_saveOK_body}');
                jsonEditor.setValue(e.data);
            }, function (e) {
                toastr.error('${txt_saveKO}', '${txt_saveKO_body}');
            })).done(function (data1) {
                return true;
            });
        });
    });
</script>