<#import "../macro/helper.ftl" as helper>
<@layout>
<!-- BEGIN PAGE HEADER-->
    <@helper.breadcrumb currentPage="Dummy"/>
<!-- END PAGE HEADER-->
<div class="row" data-auto-height="true">
    <div class="col-lg-6 col-md-6 col-sm-12  col-xs-12">
        <@helper.portlet captionTitle="Configuración de categorías observacionales" icon="icon-control-play">
            <div id="categoryEditor"></div>
            <button type="button" class="btn btn-info btn-getValue-categories">Info</button>
        </@helper.portlet>
        <@helper.portlet captionTitle="Configuración de categorías observacionales" icon="icon-control-play">
            <div id="jsonEditor"></div>
            <button type="button" class="btn btn-info btn-getValue-json">Info</button>
        </@helper.portlet>
    </div>
    <div class="col-md-6 col-sm-6">
        <@helper.portlet captionTitle="Configuración de categorías observacionales" icon="icon-control-play">
            <table id="example_1" class="table table-striped table-hover table-bordered" cellspacing="0" width="100%">
                <thead>
                <tr>
                    <th>Name</th>
                    <th>Position</th>
                    <th>Office</th>
                    <th>Extn.</th>
                    <th>Start date</th>
                </tr>
                </thead>
                <!--tfoot>
                <tr>
                    <th>Name</th>
                    <th>Position</th>
                    <th>Office</th>
                    <th>Extn.</th>
                    <th>Start date</th>
                </tr>
                </tfoot-->
            </table>
            <table id="example_2" class="table table-striped table-hover table-bordered display" cellspacing="0"
                   width="100%">
                <thead>
                <tr>
                    <th>Name</th>
                    <th>Position</th>
                    <th>Office</th>
                    <th>Extn.</th>
                    <th></th>
                </tr>
                </thead>
            </table>
        </@helper.portlet>
    </div>
</div>
<div id="btnTemplate" style="display: none;">
    <div class="btn-group btn-group-xs btn-group-solid">
        <button type="button" class="btn red btn-playscene" data-type="play" style="margin-right: 0px!important;">
            <i class="fa fa-share"></i>
        </button>
        <button type="button" class="btn green btn-editscene" data-type="scene" style="margin-right: 0px!important;">
            <i class="fa fa-edit"></i></button>
        <button type="button" class="btn blue btn-deletescene" data-type="delete" style="margin-right: 0px!important;">
            <i class="fa fa-trash-o"></i>
        </button>
    </div>
</div>
</@layout>
<!-- json editor plugins -->
<link rel="stylesheet" href="plugin/bootstrap-gtreetable.min.css">
<link rel="stylesheet" href="plugin/jquery-ui/jquery-ui.min.css">
<link rel="stylesheet" href="https://cdn.jsdelivr.net/sceditor/1.4.3/themes/default.min.css">
<script src="js/jquery.browser.min.js"></script>
<script src="js/jsoneditor.min.js"></script>
<script src="https://cdn.jsdelivr.net/sceditor/1.4.3/jquery.sceditor.min.js"></script>
<script src="https://cdn.jsdelivr.net/sceditor/1.4.3/jquery.sceditor.bbcode.min.js"></script>
<script src="plugin/jquery-ui/jquery-ui.min.js"></script>
<script src="plugin/bootstrap-gtreetable.js"></script>
<script src="plugin/languages/bootstrap-gtreetable.es.js"></script>
<!-- -->
<script type="text/javascript">
    // https://editor.datatables.net/examples/styling/bootstrap
    // https://datatables.net/examples/api/add_row.html
    // expansion de celda
    // https://datatables.net/examples/api/row_details.html
    // reload
    // https://datatables.net/reference/api/ajax.reload()
    // nested datatables
    // http://jsfiddle.net/headwinds/zz3cH/
    $(document).ready(function () {
        $('#example_1').DataTable({
            "ajax": "/datatable/example",
            //"rowId": "id"
            "columns": [
                {"data": "id"},
                {"data": "code"},
                {"data": "name"},
                {"data": "description"},
                {"data": "level"}
            ]
        });
        // generated content
        // https://datatables.net/examples/ajax/null_data_source.html
        // click event
        // https://datatables.net/examples/advanced_init/events_live.html
        var table = $('#example_2').DataTable({
            "ajax": "/datatable/example",
            "columnDefs": [
                {"targets": 0, "data": "name"},
                {"targets": 1, "data": "id"},
                {"targets": 2, "data": "code"},
                {"targets": 3, "data": "name"},
                {
                    "targets": -1,
                    "data": null,
                    "sortable": false,
                    "searchable": false,
                    "defaultContent": $("#btnTemplate").html()
                }
            ]
        });
        $('#example_2 tbody').on('click', 'button', function () {
            var button = $(this);
            var data = table.row(button.parents('tr')).data();
            console.log("action " + button.data('type') + data.id + "'s level is: " + data.level);
            if (confirm("da candela?")) {
                table.ajax.reload();
            }
        });

        //json editor
        // Set the default CSS theme and icon library globally
        JSONEditor.defaults.theme = 'bootstrap3';
        JSONEditor.defaults.options.iconlib = "fontawesome4";
        JSONEditor.plugins.sceditor.emoticonsEnabled = false;
        var jsonEditor = new JSONEditor(jQuery('#jsonEditor')[0], {
            // Enable fetching schemas via ajax
            ajax: true,
            // The schema for the editor
            schema: {
                $ref: "scheme/programacion.json"
                //format: "grid"
            },
            disable_edit_json: true,
            disable_properties: true
        });
        $(".btn-getValue-json").click(function (e) {
            console.log(jsonEditor.getValue());
        });
        var jsonEditor = new JSONEditor(jQuery('#categoryEditor')[0], {
            // Enable fetching schemas via ajax
            ajax: true,
            // The schema for the editor
            schema: {
                $ref: "scheme/categoryCollection.json"
                //format: "grid"
            },
            disable_edit_json: true,
            disable_properties: true,
            disableHeader: true
        });
        $(".btn-getValue-categories").click(function (e) {
            console.log(jsonEditor.getValue());
        });
    });
</script>