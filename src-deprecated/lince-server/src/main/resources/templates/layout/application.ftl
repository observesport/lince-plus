<#macro layout>
<!DOCTYPE html>
<!--[if IE 8]>
<html lang="en" class="ie8 no-js"> <![endif]-->
<!--[if IE 9]>
<html lang="en" class="ie9 no-js"> <![endif]-->
<!--[if !IE]><!-->
<html lang="en">
<!--<![endif]-->
<!-- BEGIN HEAD -->
<head>
    <meta charset="utf-8"/>
    <title>Lince Plus - Observational software</title>
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta content="width=device-width, initial-scale=1" name="viewport"/>
    <meta content="Lince - Observation tool for research" name="description"/>
    <meta content="Alberto Soto Fernandez" name="author"/>
    <!-- BEGIN GLOBAL MANDATORY STYLES -->
    <link href="http://fonts.googleapis.com/css?family=Open+Sans:400,300,600,700&subset=all" rel="stylesheet"
          type="text/css"/>
    <link href="global/plugins/font-awesome/css/font-awesome.min.css" rel="stylesheet" type="text/css"/>
    <link href="global/plugins/simple-line-icons/simple-line-icons.min.css" rel="stylesheet" type="text/css"/>
    <link href="global/plugins/bootstrap/css/bootstrap.min.css" rel="stylesheet" type="text/css"/>
    <link href="global/plugins/uniform/css/uniform.default.css" rel="stylesheet" type="text/css"/>
    <link href="global/plugins/bootstrap-switch/css/bootstrap-switch.min.css" rel="stylesheet" type="text/css"/>
    <link href="global/plugins/bootstrap-toastr/toastr.min.css" rel="stylesheet" type="text/css"/>
    <!-- END GLOBAL MANDATORY STYLES -->
    <!-- BEGIN THEME GLOBAL STYLES -->
    <link href="global/css/components.min.css" rel="stylesheet" type="text/css"/>
    <link href="global/css/components-md.min.css" rel="stylesheet" id="style_components" type="text/css"/>
    <link href="global/css/plugins-md.min.css" rel="stylesheet" type="text/css"/>
    <!-- END THEME GLOBAL STYLES -->
    <link href="plugin/videojs/video-js.min.css" rel="stylesheet">
    <!-- include videojs marker src !-->
    <link href="plugin/videojs/plugins/videojs.markers.min.css" rel="stylesheet">
    <!-- BEGIN PAGE LEVEL PLUGINS -->
    <link href="global/plugins/datatables/datatables.min.css" rel="stylesheet" type="text/css" />
    <link href="global/plugins/datatables/plugins/bootstrap/datatables.bootstrap.css" rel="stylesheet" type="text/css" />
    <link href="global/plugins/codemirror/lib/codemirror.css" rel="stylesheet" type="text/css" />
    <!-- END PAGE LEVEL PLUGINS -->

    <!-- BEGIN THEME LAYOUT STYLES -->
    <link href="global/layout/css/layout.min.css" rel="stylesheet" type="text/css"/>
    <link href="global/layout/css/dark.min.css" rel="stylesheet" type="text/css" id="style_color"/>
    <link href="global/layout/css/custom.min.css" rel="stylesheet" type="text/css"/>
    <link href="global/css/todo.min.css" rel="stylesheet" type="text/css">
    <link href="js/react/video-react.css" rel="stylesheet" type="text/css"/>
    <link href="css/linceThemeAdapter.css" rel="stylesheet" type="text/css"/>
    <!--ojo-->
    <#--<link href="css/linceCustom.css" rel="stylesheet" type="text/css"/>-->
    <!-- END THEME LAYOUT STYLES -->
    <link rel="shortcut icon" href="favicon.ico"/>
</head>
<!-- END HEAD -->
<body class="page-header-static page-sidebar-closed-hide-logo page-container-bg-solid page-md">
    <#include "header.ftl">
<!-- BEGIN HEADER & CONTENT DIVIDER -->
<div class="clearfix"></div>
<!-- END HEADER & CONTENT DIVIDER -->
<!-- BEGIN CONTAINER -->
<div class="page-container">
    <#include "sidebar.ftl">
    <!-- BEGIN CONTENT -->
    <div class="page-content-wrapper">
        <!-- BEGIN CONTENT BODY -->
        <div class="page-content">
            <#nested>
        </div>
        <!-- END CONTENT BODY -->
    </div>
    <!-- END CONTENT -->
    <#include "qsidebar.ftl">
</div>
<!-- END CONTAINER -->
<!-- BEGIN FOOTER -->
<div class="page-footer">
    <div class="page-footer-inner"> 2020 &copy; Lince observation tool by Alberto Soto Fernández.
    </div>
    <div class="scroll-to-top">
        <i class="icon-arrow-up"></i>
    </div>
</div>
<!-- END FOOTER -->
<!--[if lt IE 9]>
<script src="global/plugins/respond.min.js"></script>
<script src="global/plugins/excanvas.min.js"></script>
<![endif]-->
<!-- BEGIN CORE PLUGINS -->
<script src="global/plugins/jquery.min.js" type="text/javascript"></script>
<!--script src="https://code.jquery.com/jquery-3.0.0.js"></script>
<script src="https://code.jquery.com/jquery-migrate-3.0.0.js"></script-->
<script src="global/plugins/bootstrap/js/bootstrap.min.js" type="text/javascript"></script>
<script src="global/plugins/js.cookie.min.js" type="text/javascript"></script>
<script src="global/plugins/bootstrap-hover-dropdown/bootstrap-hover-dropdown.min.js" type="text/javascript"></script>
<script src="global/plugins/bootstrap-toastr/toastr.min.js" type="text/javascript"></script>
<script src="global/plugins/jquery-slimscroll/jquery.slimscroll.min.js" type="text/javascript"></script>
<script src="global/plugins/jquery.blockui.min.js" type="text/javascript"></script>
<script src="global/plugins/uniform/jquery.uniform.min.js" type="text/javascript"></script>
<script src="global/plugins/bootstrap-switch/js/bootstrap-switch.min.js" type="text/javascript"></script>
<!-- END CORE PLUGINS -->
<!--script src="plugin/popcorn/popcorn-complete.js" type="text/javascript"></script-->
<script src="plugin/videojs/ie8/videojs-ie8.min.js"></script>
<script src="plugin/videojs/video.min.js"></script>
<script src="plugin/videojs/plugins/videojs-markers.min.js"></script>
<!-- BEGIN THEME GLOBAL SCRIPTS -->
<script src="global/scripts/app.min.js" type="text/javascript"></script>
<!-- END THEME GLOBAL SCRIPTS -->
<!-- BEGIN PAGE LEVEL PLUGINS -->
<script src="global/scripts/datatable.js" type="text/javascript"></script>
<script src="global/plugins/datatables/datatables.min.js" type="text/javascript"></script>
<script src="global/plugins/datatables/plugins/bootstrap/datatables.bootstrap.js" type="text/javascript"></script>
<script src="global/plugins/codemirror/lib/codemirror.js" type="text/javascript"></script>
<script src="global/plugins/codemirror/mode/r/r.js" type="text/javascript"></script>

<!--script src="../assets/pages/scripts/table-datatables-editable.min.js" type="text/javascript"></script-->
<!-- END PAGE LEVEL PLUGINS -->
<!-- BEGIN THEME LAYOUT SCRIPTS -->
<script src="global/layout/scripts/layout.min.js" type="text/javascript"></script>
<script src="global/layout/scripts/demo.min.js" type="text/javascript"></script><#--
<script src="global/layout/scripts/quick-sidebar-lince.js" type="text/javascript"></script>-->
<!-- END THEME LAYOUT SCRIPTS -->
<script src="js/linceApp.global.js" type="text/javascript"></script>
<script src="js/linceApp.especific.js" type="text/javascript"></script>
<script src="/js/react/sidebar-bundle.js" type="text/javascript"></script>
<script type="text/javascript">
    jQuery(document).ready(function () {
        $.linceApp.especific.onFinishLoadOperations();
    });
</script>
<!-- Global site tag (gtag.js) - Google Analytics -->
<script async src="https://www.googletagmanager.com/gtag/js?id=UA-78568633-3"></script>
<script>
    window.dataLayer = window.dataLayer || [];
    function gtag(){dataLayer.push(arguments);}
    gtag('js', new Date());

    gtag('config', 'UA-78568633-3');
</script>

</body>
</html>
</#macro>