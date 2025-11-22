<#import "/spring.ftl" as spring/>
<!-- BEGIN SIDEBAR -->
<div class="page-sidebar-wrapper">
    <!-- END SIDEBAR -->
    <!-- DOC: Set data-auto-scroll="false" to disable the sidebar from auto scrolling/focusing -->
    <!-- DOC: Change data-auto-speed="200" to adjust the sub menu slide up/down speed -->
    <div class="page-sidebar navbar-collapse collapse">
        <!--
        https://github.com/thesabbir/simple-line-icons
        https://simplelineicons.github.io/
        -->
        <!-- BEGIN SIDEBAR MENU -->
        <!-- DOC: Apply "page-sidebar-menu-light" class right after "page-sidebar-menu" to enable light sidebar menu style(without borders) -->
        <!-- DOC: Apply "page-sidebar-menu-hover-submenu" class right after "page-sidebar-menu" to enable hoverable(hover vs accordion) sub menu mode -->
        <!-- DOC: Apply "page-sidebar-menu-closed" class right after "page-sidebar-menu" to collapse("page-sidebar-closed" class must be applied to the body element) the sidebar sub menu mode -->
        <!-- DOC: Set data-auto-scroll="false" to disable the sidebar from auto scrolling/focusing -->
        <!-- DOC: Set data-keep-expand="true" to keep the submenues expanded -->
        <!-- DOC: Set data-auto-speed="200" to adjust the sub menu slide up/down speed -->
        <ul class="page-sidebar-menu  page-header-fixed page-sidebar-menu-hover-submenu " data-keep-expanded="false"
            data-auto-scroll="true" data-slide-speed="200">
            <li class="nav-item start active open "><#--active -->
                <a href="/" class="nav-link nav-toggle">
                    <i class="icon-home"></i>
                    <span class="title"><@spring.message "front.layout.label.home"/></span>
                    <span class="arrow"></span>
                </a>
            </li>
            <li class="nav-item start active open ">
                <a href="/categoryConfig" class="nav-link nav-toggle">
                    <i class="icon-docs"></i>
                    <span class="title"><@spring.message "front.layout.label.configure"/></span>
                    <span class="arrow"></span>
                </a>
            </li><#--
            <li class="nav-item start active open ">
                <a href="/categoryConfig?react=true" class="nav-link nav-toggle">
                    <i class="icon-docs"></i>
                    <span class="title">Configurar</span>
                    <span class="arrow"></span>
                </a>
            </li>-->
            <li class="nav-item start active open ">
                <a href="/sceneScanner" class="nav-link nav-toggle">
                    <i class="icon-social-youtube"></i>
                    <span class="title"><@spring.message "front.layout.label.scenes"/></span>
                    <span class="arrow"></span>
                </a>
            </li>
        <#--  <li class="nav-item start active open ">
              <a href="/videoPlayer?react=false" class="nav-link nav-toggle">
                  <i class="icon-social-dribbble"></i>
                  <span class="title">Analizar</span>
                  <span class="arrow"></span>
              </a>
          </li>-->

            <li class="nav-item start active open ">
                <a href="/videoPlayer?react=true" class="nav-link nav-toggle">
                    <i class="icon-social-dribbble"></i>
                    <span class="title"><@spring.message "front.layout.label.analyze"/></span>
                    <span class="arrow"></span>
                </a>
            </li>
            <!--export-->
        <#--li class="nav-item start active open ">
            <a href="/dummy" class="nav-link nav-toggle">
                <i class="icon-settings"></i>
                <span class="title">Exportar</span>
                <span class="arrow"></span>
            </a>
        </li-->
            <li class="nav-item start active open ">
                <a href="/stats" class="nav-link nav-toggle">
                    <i class="icon-bar-chart"></i>
                    <span class="title"><@spring.message "front.layout.label.stats"/></span>
                    <span class="arrow"></span>
                </a>
            </li>
            <li class="nav-item start active open ">
                <a href="/r-console" class="nav-link nav-toggle">
                    <i class="icon-target"></i>
                    <span class="title"><@spring.message "front.layout.label.r-console"/></span>
                    <span class="arrow"></span>
                </a>
            </li>
            <li class="nav-item start active open ">
                <a href="/results" class="nav-link nav-toggle">
                    <i class="icon-rocket"></i>
                    <span class="title"><@spring.message "front.layout.label.results"/></span>
                    <span class="arrow"></span>
                </a>
            </li>
        </ul>
        <!-- END SIDEBAR MENU -->
    </div>
    <!-- END SIDEBAR -->
</div>
<!-- END SIDEBAR -->