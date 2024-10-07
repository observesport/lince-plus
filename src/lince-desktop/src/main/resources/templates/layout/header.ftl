<#import "/spring.ftl" as spring/>
<!-- BEGIN HEADER -->
<div class="page-header navbar navbar-static-top">
    <!-- BEGIN HEADER INNER -->
    <div class="page-header-inner ">
        <!-- BEGIN LOGO -->
        <div class="page-logo">
            <a href="/">
                <img src="/global/brand/logoHeader-mid.png" alt="logo" class="logo-default" style=""/>
            </a>
            <div class="menu-toggler sidebar-toggler" onclick="$.linceApp.especific.setMenuSize(this);">
                <!-- DOC: Remove the above "hide" to enable the sidebar toggler button on header -->
            </div>
        </div>
        <!-- END LOGO -->
        <!-- BEGIN RESPONSIVE MENU TOGGLER -->
        <a href="javascript:;" class="menu-toggler responsive-toggler" data-toggle="collapse"
           data-target=".navbar-collapse"></a>
        <!-- END RESPONSIVE MENU TOGGLER -->
        <!-- BEGIN PAGE ACTIONS -->
        <!-- DOC: Remove "hide" class to enable the page header actions -->
        <div class="page-actions">
            <span class="hidden-sm hidden-xs" style="color: #fff !important;">Lince Plus ${lince_version!""}</span>
            <div class="btn-group">
                <button type="button" class="btn btn-circle btn-outline default dropdown-toggle" data-toggle="dropdown">
                    <span class="hidden-sm hidden-xs"><@spring.message "lang.label"/></span>&nbsp;
                    <i class="fa fa-angle-down"></i>
                </button>
                <ul class="dropdown-menu" role="menu">
                    <li>
                        <a href="?lang=en"><@spring.message "lang.en"/></a>
                    </li>
                    <li>
                        <a href="?lang=es"><@spring.message "lang.es"/></a>
                    </li>
                </ul>
            <#-- <button type="button" class="btn btn-circle btn-outline default"
                     onclick="location.href='?lang=es'">
                 <span class="hidden-sm hidden-xs"><@spring.message "lang.es"/></span>&nbsp;
             </button>
             <button type="button" class="btn btn-circle btn-outline default"
                     onclick="location.href='?lang=en'">
                 <span class="hidden-sm hidden-xs"><@spring.message "lang.en"/></span>&nbsp;
             </button>-->
            <#-- <a href="?lang=en">
             <@spring.message "lang.eng"/>
             </a>-->

            </div>
            <div class="btn-group">
                <button type="button" class="btn btn-circle btn-outline default"
                        onclick="location.href='/profile'">
                    <span class="hidden-sm hidden-xs"><@spring.message "front.layout.label.info"/><#--Información del proyecto&nbsp;--></span>&nbsp;
                </button>
            </div>
            <div class="btn-group">
                <button type="button" class="btn btn-circle btn-outline default dropdown-toggle" data-toggle="dropdown">
                    <i class="fa fa-plus"></i>&nbsp;
                    <span class="hidden-sm hidden-xs"><@spring.message "front.layout.label.categories"/></span>&nbsp;
                    <i class="fa fa-angle-down"></i>
                </button>
                <ul class="dropdown-menu" role="menu">
                    <li>
                        <a href="#"
                           onclick="toastr.info('Estamos preparando la funcionalidad para ficheros desde web, en poco podras disfrutar más. Utiliza la opción de menu que encontrarás en LinceApp', 'En desarrollo');">
                            <i class="icon-docs"></i> Cargar categorías </a>
                    </li>
                    <li>
                        <a href="#"
                           onclick="toastr.info('Estamos preparando la funcionalidad para ficheros desde web, en poco podras disfrutar más. Utiliza la opción de menu que encontrarás en LinceApp', 'En desarrollo');">
                            <i class="icon-share"></i> Exportar categorías </a>
                    </li>
                </ul>
            </div>
            <div class="btn-group">
                <button type="button" class="btn btn-circle btn-outline default dropdown-toggle" data-toggle="dropdown">
                    <i class="fa fa-plus"></i>&nbsp;
                    <span class="hidden-sm hidden-xs"><@spring.message "front.layout.label.data"/>&nbsp;</span>&nbsp;
                    <i class="fa fa-angle-down"></i>
                </button>
                <ul class="dropdown-menu" role="menu">
                    <li>
                        <a href="#"
                           onclick="toastr.info('Estamos preparando la funcionalidad para ficheros desde web, en poco podras disfrutar más. Utiliza la opción de menu que encontrarás en LinceApp', 'En desarrollo');">
                            <i class="icon-docs"></i> Cargar registros </a>
                    </li>
                    <li>
                        <a href="#"
                           onclick="toastr.info('Estamos preparando la funcionalidad para ficheros desde web, en poco podras disfrutar más. Utiliza la opción de menu que encontrarás en LinceApp', 'En desarrollo');">
                            <i class="icon-share"></i> Exportar registros </a>
                    </li>
                </ul>
            </div>
        </div>
        <!-- END PAGE ACTIONS -->
        <div class="top-menu">
            <ul class="nav navbar-nav pull-right">
                <!-- BEGIN QUICK SIDEBAR TOGGLER -->
                <!-- DOC: Apply "dropdown-dark" class after below "dropdown-extended" to change the dropdown styte -->
                <li class="dropdown dropdown-extended quick-sidebar-toggler">
                    <span style="color: white!important;"><@spring.message "front.layout.label.changeObserver"/></span>
                    <i class="icon-logout"></i>
                </li>
                <!-- END QUICK SIDEBAR TOGGLER -->
            </ul>

        </div>
    </div>
    <!-- END HEADER INNER -->
</div>
<!-- END HEADER -->