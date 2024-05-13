<#import "../macro/helper.ftl" as helper>
<@layout>
    <@helper.breadcrumb currentPage="Categorías"/>
<div class="row" data-auto-height="true">
    <div class="col-lg-12 col-md-12 col-sm-12  col-xs-12">
        <@helper.portlet captionTitle="Configuración de categorías observacionales" icon="icon-control-play">

            <table id="w0" class="gtreetable table">
                <thead>
                <tr>
                    <th width="100%">Criterios y categorías</th>
                </tr>
                </thead>
            </table>
        </@helper.portlet>
    </div>
</div>
</@layout>
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
<script type="text/javascript">
    jQuery(document).ready(function () {
        jQuery('#w0').gtreetable({
            "source": function (id) {
                console.log("source by {id}" + id);
                return {
                    type: 'GET',
                    url: '/categories/get/' + id,
                    dataType: 'json',
                    error: function (XMLHttpRequest) {
                        console.log(XMLHttpRequest.status + ': ' + XMLHttpRequest.responseText);
                    }
                };
            }, "onSave": function (oNode) {
                var uri = !oNode.isSaved() ? '/categories/save/' : '/categories/update/' + oNode.getId();
                console.log("pista que voy");
                try {
                    if (oNode.getParents().length > 1) {
                        //means that it is a second level node, lets replace it
                        oNode.parent = oNode.getParents()[0].parent;
                    }
                } catch (e) {
                    console.log("error searching node hierarchy!")
                }
                var postData = {
                    "parent": oNode.getParent(),
                    "name": oNode.getName(),
                    "position": oNode.getInsertPosition(),
                    "related": oNode.getRelatedNodeId()
                };
                //'http://gtreetable2.gilek.net/web/index.php?r=site%2FnodeCreate'
                // : URI('http://gtreetable2.gilek.net/web/index.php?r=site%2FnodeUpdate').addSearch({'id': oNode.getId()}).toString();
                console.log("Saving node to uri:" + uri);
                console.log(postData);
                return {
                    contentType: 'application/json; charset=utf-8',
                    dataType: 'json',
                    type: 'POST',
                    url: uri,
                    data: JSON.stringify(postData),
                    cache: false, // Force requested pages not to be cached by the browser
                    processData: false, // Avoid making query string instead of JSON
                    success: function (req) {
                        console.log("TODO: Lanzar un refresh de pagina por toda la cara!!");
                    },
                    error: function (XMLHttpRequest) {
                        console.log(XMLHttpRequest.status + ': ' + XMLHttpRequest.responseText);
                    }
                };
            }, "onDelete": function (oNode) {
                var uri = '/categories/delete/' + oNode.getId();
                //URI('http://gtreetable2.gilek.net/web/index.php?r=site%2FnodeDelete').addSearch({'id': oNode.getId()}).toString();
                console.log("delete node via :" + uri);
                return {
                    contentType: 'application/json; charset=utf-8',
                    type: 'POST',
                    url: uri,
                    error: function (XMLHttpRequest) {
                        console.log(XMLHttpRequest.status + ': ' + XMLHttpRequest.responseText);
                    }
                };
            }, "onMove": function (oSource, oDestination, position) {
                var uri = '/categories/move/' + oSource.getId();
                var postData = {
                    //TODO: como preservamos el id?
                    "related": oDestination.getId(),
                    "position": position
                };
                //URI('http://gtreetable2.gilek.net/web/index.php?r=site%2FnodeMove').addSearch({'id': oSource.getId()}).toString();
                console.log("Move via" + uri);
                console.log(postData);
                return {
                    contentType: 'application/json; charset=utf-8',
                    dataType: 'json',
                    type: 'POST',
                    url: uri,
                    data: JSON.stringify(postData),
                    cache: false, // Force requested pages not to be cached by the browser
                    processData: false, // Avoid making query string instead of JSON
                    success: function (req) {
                        console.log("TODO: Lanzar un refresh de pagina por toda la cara!!");
                        window.location.reload(true);
                    },
                    error: function (XMLHttpRequest) {
                        console.log(XMLHttpRequest.status + ': ' + XMLHttpRequest.responseText);
                    }
                };
            },
            "language": "es",
            "manyroots": true,
            "draggable": true,
            "inputWidth": "300px"
        });

        /*
        var currentInfo = {
            name: "John Smith",
            age: 35,
            gender: "male",
            location: {
                city: "San Francisco",
                state: "California"
            },
            pets: [
                {
                    name: "Spot",
                    type: "dog",
                    fixed: true
                },
                {
                    name: "Whiskers",
                    type: "cat",
                    fixed: false
                }
            ]
        };
        var editor = new JSONEditor(currentElem,{
            // Enable fetching schemas via ajax
            ajax: true,
            // The schema for the editor
            schema: {
                $ref: "scheme/person.json",
                format: "grid"
            },
            // Seed the form with a starting value
            startval: currentInfo
        });
        */
        //JSONEditor.defaults.iconlib = 'fontawesome4';
        /*
        var editor = new JSONEditor(jQuery('#categoryContainer')[0], {
            schema: {
                type: "object",
                title: "Criterios y categorias",
                properties: {
                    criteria: {
                        type: "string"
                    },
                    description: {
                        type: "string"
                    }
                }
            }
        });
        */
    });
</script>