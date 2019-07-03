<#import "macro/helper.ftl" as helper>
<#import "/spring.ftl" as spring/><#-- <@spring.message "front.home.label.legal.1.title"/>-->
<#assign txt_aboutLince><@spring.message "front.home.label.advice"/></#assign>
<#assign txt_main><@spring.message "front.label.main"/></#assign>
<#assign varContent>
<div class="pull-right">
    <span class="font-green-sharp"><@spring.message "front.consoleR.r_vars"/>&#160;</span>
    <#list rAttributes as item>
        <span class="label label-info" style="text-transform: none !important;">
        ${item!}</span>
    </#list>
</div>
</#assign>
<@layout>

<!-- BEGIN PAGE HEADER-->
    <@helper.breadcrumb currentPage="${txt_main}"/>
<!-- END PAGE HEADER-->
<div class="row" data-auto-height="true">
    <div class="col-lg-6 col-md-6 col-sm-12  col-xs-12">
        <div class="row">
            <div class="col-md-12 col-sm-12">
                <@helper.portlet captionTitle="R Console" portletId="console-r-portlet"
                captionButtons=varContent>
                    <a href="#" class="pull-left" id="execute-code">
                        <span class="label label-warning">
                            <i class="fa fa-floppy-o"
                               aria-hidden="true"></i>&#160;<@spring.message "front.label.execute"/>
                        </span>
                    </a>
                    <div id="r-code">
                        <form>
                            <!--
                            print(sample(1:3))
                            print(sample(1:3, size=3, replace=FALSE))  # same as previous line
                            print(sample(c(2,5,3), size=4, replace=TRUE))
                            print(sample(1:2, size=10, prob=c(1,3), replace=TRUE))
                            --
                            df <- data.frame(x=1:10, y=(1:10)+rnorm(n=10));
                            print(df);
                            print(lm(y ~ x, df));
                            -->
                            <textarea id="code" name="code">print('<@spring.message "front.consoleR.r_example"/>');
print(linceData);
print(linceDataByCategory);
#This is a comment!
df <- data.frame(x = seq(5), y = runif(5));
print(df);
print(attributes(df));
print(is.list(df));
</textarea>
                            <br/>
                        </form>
                    </div>
                </@helper.portlet>
            </div>
        </div>
    </div>
    <div class="col-md-6 col-sm-6 col-sm-12  col-xs-12">
        <div class="row">
            <div class="col-md-12 col-sm-12">
                <@helper.portlet captionTitle="R Result">
                    <div id="r-result">
                        <form>
                            <textarea id="code-result" name="code-result">
                                No result
                            </textarea>
                        </form>
                    </div>
                </@helper.portlet>
            </div>
        </div>
    </div>
    <div class="col-md-12 col-sm-12 alert alert-block fade in">
        <div class="portlet mt-element-ribbon light portlet-fit ">
            <div class="ribbon ribbon-vertical-right ribbon-shadow ribbon-color-primary uppercase">
                <div class="ribbon-sub ribbon-bookmark"></div>
                <i class="fa fa-star"></i>
            </div>
            <div class="portlet-title">
                <div class="caption">
                    <i class=" icon-layers font-green"></i>
                    <span class="caption-subject font-green bold uppercase"><@spring.message "front.consoleR.know"/></span>
                </div>
            </div>
            <div class="portlet-body">
                <div class="row">
                    <div class="col-md-2 col-sm-2">
                        <div class="btn-group btn-group-circle">
                            <button type="button" class="btn btn-default" data-dismiss="alert"><i
                                    class="fa fa-times"></i>
                            </button>
                            <button type="button" class="btn btn-default btn-next-advice">
                                <i class="fa fa-arrow-right" aria-hidden="true"></i></button>
                        </div>
                        <span id="advice-counter">1/8</span>
                    </div>
                    <div class="col-md-10 col-sm-10">
                        <div class="advice show" id="advice-1" data-count="1">
                            <p><@spring.message "front.consoleR.know.1.header"/></p>
                            <p><@spring.message "front.consoleR.know.1.body"/></p>
                            <pre><code>Square <- function(x) {
  return(x^2);
}
print(Square(16));
print(match(5, c(2,7,5,3))); #  5 is in 3rd place
print(seq(from=1,to=3,by=.5) %in% 1:3);</code></pre>
                        </div>
                        <div class="advice hidden" id="advice-2" data-count="2">
                            <p><@spring.message "front.consoleR.know.2"/>
                                <a href="https://cran.r-project.org/doc/manuals/r-release/R-lang.html#Indexing-matrices-and-arrays"
                                   target="_blank">aquí</a>
                            </p><br/>
                            <pre><code>#Para ver los datos con los criterios concatenados con "," utiliza:
print(lince);
#Para ver los datos del análisis y los criterios agrupados por categorías utiliza:
print(linceByCategory);
#Para ver los datos o recorrerlos puedes utilizar lo siguiente,fijate que el nombre de las columnas es case sensitive:
#x[i]
#x[i, j]
#x[[i]]
#x[[i, j]]
#x$a
#x$"a"
print(lince[1,1]);
print(lince[8,'SceneName']);
print(lince[1,'Categories']);</code></pre>
                        </div>
                        <div class="advice hidden" id="advice-3" data-count="3">
                            <p><@spring.message "front.consoleR.know.3"/>
                            </p>
                        </div>
                        <div class="advice hidden" id="advice-4" data-count="4">
                            <p><@spring.message "front.consoleR.know.4"/>
                                <a href="https://cran.r-project.org/web/packages/jsonlite/vignettes/json-apis.html"
                                   target="_blank">jsonlite </a>, que es el que necesitas.<br/>
                                Ejecuta este código en la <b>consola</b> de tu aplicación R (no en la de Lince!):
                            </p>
                            <pre><code>install.packages("devtools")</code></pre>
                            <p>
                                Una vez finalices la instalación, prueba este codigo en un <b>script</b> de R:
                            </p>
                            <pre><code>library(jsonlite)
hadley_orgs <- fromJSON("https://api.github.com/users/hadley/orgs")
print(hadley_orgs)</code></pre>
                        </div>
                        <div class="advice hidden" id="advice-5" data-count="5">
                            <p>
                                Si quieres atacar dede R directamente al programa puedes hacerlo sin problema via REST.
                                Para ello tienes que instalar la libreria jsonlite explicada anteriormente y ejecutar el
                                siguiente codigo en la cabecera de tu código para que apunte al programa de Lince.
                                Recuerda que cada vez que se ejecuta Lince, cambia la dirección, así que tienes que
                                sustituir la dirección del servidor de Lince. La actual se muestra a continuación:
                            </p>
                            <pre><code>library(jsonlite)
lince_data <- fromJSON("http://localhost:${port!"52649"}/register/get")
print(lince_data)</code></pre>
                        </div>
                        <div class="advice hidden" id="advice-6" data-count="6">
                            <p>
                                Puede que tu código contenga errores. Hemos adaptado Lince para que puedas ver tus
                                errores. Seguramente verás algo similar a "Parse Exception".<br/>
                                La descripción del error te puede ayudar a resolver que sucede, aunque nosotros
                                incorporamos
                                todo tu código en una única línea y por eso ves que el número de línea y carácter
                                mostrado
                                no coincide con el tuyo. Puedes probar este código incorrecto para ver que sucede:<br/>
                            </p>
                            <pre><code>df <- data.frame(x=1:10, y=(1:10)+rnorm(n=10))
print(df)</code></pre>
                            <p>
                                Para corregirlo, tan sólo tienes que añadir los ";" al final de cada instrucción.</p>
                        </div>
                        <div class="advice hidden" id="advice-7" data-count="7">
                            <p>Si necesitas más espacio y tiempo para ejecutar tu código, puedes ampliar la ventana con
                                las flechas de la consola en la esquina superior derecha y ejecutar el codigo. El
                                programa te avisará cuando tu código esté listo, pero recuerda que según lo que quieras
                                hacer puede tardar.</p>
                        </div>
                        <div class="advice hidden" id="advice-8" data-count="8">
                            <p>Hemos incorporado un bloqueo en la ventana de consola para cuando evalues código. <br/>
                                Dale tiempo ya que los procesos matemáticos pueden ser muy complejos.</p>
                        </div>
                        <div class="advice hidden" id="advice-9" data-count="9">
                            <p>Puedes generar grafos y añadir valores a los vectores generados,
                                para calcular los caminos entre nodos</p>
                            <pre><code>## build a graph with 5 nodes
x <- matrix(NA, 5, 5);
diag(x) <- 0;
x[1,2] <- 30; x[1,3] <- 10;
x[2,4] <- 70; x[2,5] <- 40;
x[3,4] <- 50; x[3,5] <- 20;
x[4,5] <- 60;
x[5,4] <- 10;
print(x);
## compute all path lengths
z <- allShortestPaths(x);
print(z);
## the following should give 1 -> 3 -> 5 -> 4
print(extractPath(z, 1, 4));
                            </code></pre>
                        </div>
                        <div class="advice hidden" id="advice-9" data-count="9">
                            <p>Ejemplo de matriz</p>
                            <pre><code>A = matrix(c(2, 4, 3, 1, 5, 7),nrow=2,ncol=3,byrow = TRUE);
print(A);</code></pre>
                            <p>Krippendorff</p>
                            <pre><code>#Krippendorff
# the "C" data from Krippendorff
nmm<-matrix(c(1,1,NA,1,2,2,3,2,3,3,3,3,3,3,3,3,2,2,2,2,1,2,3,4,4,4,4,4,
+  1,1,2,1,2,2,2,2,NA,5,5,5,NA,NA,1,1,NA,NA,3,NA),nrow=4);
# first assume the default nominal classification
print("kripp.alpha(nmm)");
# now use the same data with the other three methods
print("kripp.alpha(nmm,'ordinal')");
print("kripp.alpha(nmm,'interval')");
print("kripp.alpha(nmm,'ratio')");
                            </code></pre>
                        </div>

                    </div>
                </div>
            </div>
        </div>
    </div>
</div>
</@layout>
<script type="text/javascript">
    var editorIn = CodeMirror.fromTextArea(document.getElementById("code"), {
        lineNumbers: true, lineSeparator: ""
    });
    var editorOut = CodeMirror.fromTextArea(document.getElementById("code-result"), {
        lineNumbers: true
    });
    jQuery(document).ready(function () {
        var consolePortletID = "#console-r-portlet";
        /**
         * Dummy code for searching next advice
         */
        $(".btn-next-advice").click(function (e) {
            var elem = $("div.advice.show");
            var current = elem.data("count");
            var max = $(".advice").size();
            var next = (current + 1) % max;
            if (next == 0) {
                next = max;
            }
            elem.removeClass("show");
            $("div.advice:not([class*='hidden'])").addClass("hidden");
            $("div#advice-" + next).addClass("show");
            $("div#advice-" + next).removeClass("hidden");
            $("#advice-counter").html(next + " / " + max);
        });
        /**
         * Blocks user actions and executes the commands in R via ajax
         */
        $("#execute-code").click(function (el) {
            App.blockUI({
                target: consolePortletID,
                animate: true
            });
            //console.log($('#code').html());
            var data = editorIn.getValue();
            console.log(data);
            editorIn.save();
            console.log($('#code').html());
            $.when($.linceApp.global.doAjax('/renjin/execute/', 'POST', data, function (e) {
                console.log(e);
                editorOut.getDoc().setValue(e.responseText);
                toastr.success('<@spring.message "front.consoleR.r_vars"/>Ejecución finalizada');
                App.unblockUI(consolePortletID);
            }, function (e) {
                toastr.error('<@spring.message "front.consoleR.r_vars"/>Tu script no puede ejecutarse', 'Error');
                App.unblockUI(consolePortletID);
            })).done(function (data) {
                //$('#code-result').html(data.toString());
                return true;
            });
        });
    });
</script>