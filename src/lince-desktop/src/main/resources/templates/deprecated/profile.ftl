<#import "macro/helper.ftl" as helper>
<#import "/spring.ftl" as spring/>
<#assign txt_profile><@spring.message "front.label.profile"/></#assign>
<#assign txt_profile_msg><@spring.message "front.label.profile.msg"/></#assign>
<#assign txt_profile_custom><@spring.message "front.label.profile.custom"/></#assign>
<#if locale!="en">
    <#assign profile_scheme_config = "scheme/userProfile_"+locale+".json"/>
    <#assign user_scheme_config = "scheme/userSettings_"+locale+".json"/>
<#else>
    <#assign profile_scheme_config = "scheme/userProfile.json"/>
    <#assign user_scheme_config = "scheme/userSettings.json"/>
</#if>
<@layout>

    <@helper.breadcrumb currentPage="${txt_profile}_${locale}"/>
<div class="row" data-auto-height="true">
    <div class="col-lg-12 col-md-12 col-sm-12  col-xs-12 ">
        <button type="button" id="btn-SaveProfile" class="btn btn-info btn-saveData pull-right">
            <@spring.message "front.label.saveAll"/>
        </button>
    </div>
    <div class="col-lg-6 col-md-6 col-sm-6  col-xs-6">
        <@helper.portlet captionTitle="${txt_profile_msg}" icon="icon-control-play">
            <div id="profileEditor"></div>
        </@helper.portlet>
    </div>
    <div class="col-lg-6 col-md-6 col-sm-6  col-xs-6">
        <@helper.portlet captionTitle="${txt_profile_custom}" icon="icon-control-play">
            <div id="settingsEditor"></div>
        </@helper.portlet>
    </div>
</div>
</@layout>
<!-- json editor plugins -->
<script src="js/jquery.browser.min.js"></script>
<script src="js/jsoneditor.js"></script>
<!-- -->
<script type="text/javascript">
    jQuery(document).ready(function () {
        JSONEditor.defaults.theme = 'bootstrap3';
        JSONEditor.defaults.options.iconlib = "fontawesome4";
        JSONEditor.plugins.sceditor.emoticonsEnabled = false;
        JSONEditor.defaults.grid_columns = 6;
        //USER DATA management
        var currentUserData;
        var userEditor;
        var settingsEditor;
        $.linceApp.global.doAjax('/profile/get', 'GET', null, function (result) {
            currentUserData = result.data[0];
            console.log(currentUserData);
            userEditor = new JSONEditor(jQuery('#profileEditor')[0], {
                ajax: true,// Enable fetching schemas via ajax
                schema: {$ref: "${profile_scheme_config}", format: "grid"},
                // The schema for the editor
                disable_edit_json: true,
                disable_properties: true,
                disable_collapse: true,
                disableHeader: true,
                grid_columns: 6,
                startval: currentUserData, // Seed the form with a starting value
                no_additional_properties: true, // Disable additional properties
                required_by_default: true // Require all properties by default
            });
            settingsEditor = new JSONEditor(jQuery('#settingsEditor')[0], {
                ajax: true,// Enable fetching schemas via ajax
                schema: {$ref: "${user_scheme_config}", format: "table"},
                // The schema for the editor
                disable_edit_json: true,
                disable_properties: true,
                disable_collapse: true,
                disableHeader: true,
                startval: currentUserData, // Seed the form with a starting value
                no_additional_properties: true, // Disable additional properties
                required_by_default: true // Require all properties by default
            });
        }, function (e) {
            console.log("error al cargar escenas");
        });
        //SAVE ALL
        $(".btn-saveData").click(function (e) {
            //hacemos un submit con todo
            var currentElement = userEditor.getValue();
            var additionalData = settingsEditor.getValue();
            currentElement.fps = additionalData.fps;
            currentElement.fps = additionalData.numVideos;
            console.log(currentElement);
            $.when($.linceApp.global.doAjax('/profile/save/', 'POST', currentElement, function (e) {
                toastr.success('Datos de actualizados', 'Datos guardados');
            }, function (e) {
                toastr.error('Ocurre un error al guardar los datos. ', 'Datos NO guardados');
            })).done(function (data1) {
                return true;
            });
        });
    });
</script>