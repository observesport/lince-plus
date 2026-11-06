Dificultades encontradas en la instalación de Lince Plus en Ubuntu MATE
Durante la instalación de Lince Plus 4.0.10 para Linux x64 en un equipo Toshiba Satellite C50-A-1GZ con Ubuntu MATE 24.04 LTS de 64 bits, se produjeron varias dificultades que hicieron que la aplicación no pudiera utilizarse inicialmente desde el instalador ni desde el menú de aplicaciones.

El equipo dispone de un procesador Intel Core i3-3110M, 16 GB de RAM y SSD, por lo que los problemas encontrados no estaban relacionados con una falta de recursos del sistema.

Principales incidencias
Dependencia de Java no detectada previamente

El instalador permitió completar la instalación, pero al ejecutar el programa apareció:

java: no se ha encontrado la orden

Esto indica que la instalación no comprueba de forma suficientemente clara si Java está disponible o si la versión instalada es compatible.

Problema de ejecución del lanzador

Al ejecutar inicialmente lince-plus apareció:

Bad substitution

Posteriormente se comprobó que el lanzador adecuado era lince-os-launcher.sh.

Permisos de ejecución

El lanzador no disponía inicialmente de los permisos necesarios para ejecutarse directamente. Fue necesario aplicar:

chmod +x lince-os-launcher.sh

Problema con el directorio de trabajo

Aunque lince-desktop.jar estaba correctamente instalado en:

/home/marquitusmt/lince-plus/lince-desktop.jar

el programa mostraba:

Error: Unable to access jarfile lince-desktop.jar

El problema se solucionó ejecutando el lanzador desde el directorio de instalación:

/home/marquitusmt/lince-plus

Por tanto, el lanzador depende del directorio de trabajo actual para localizar el archivo .jar.

Integración en el menú de aplicaciones

La instalación generó inicialmente entradas .desktop, pero la aplicación no quedó correctamente accesible desde el menú de Ubuntu MATE.

Fue necesario crear y configurar manualmente una entrada:

~/.local/share/applications/lince-plus.desktop

incluyendo, entre otros elementos, el directorio de trabajo:

Path=/home/marquitusmt/lince-plus

Finalmente, se consiguió disponer de Lince Plus tanto en el escritorio como en el menú Aplicaciones → Educación.

Propuestas de mejora para el instalador
A partir de estas incidencias, sería recomendable introducir las siguientes mejoras en futuras versiones:

1. Comprobación automática de Java
El instalador debería comprobar antes de finalizar:

si Java está instalado;
qué versión está disponible;
si es compatible con la versión de Lince Plus;
si java está disponible en el PATH.
Si falta Java, debería mostrar un mensaje claro indicando qué paquete debe instalarse, por ejemplo:

«Lince Plus requiere Java. Instale OpenJDK 17 o una versión compatible antes de ejecutar la aplicación.»

Idealmente, el instalador debería ofrecer la posibilidad de instalar la dependencia cuando el sistema lo permita.

2. Lanzador independiente del directorio actual
El problema más significativo fue la dependencia del directorio de trabajo.

El script lince-os-launcher.sh debería determinar automáticamente su propio directorio de instalación y utilizar una ruta absoluta para localizar:

lince-desktop.jar

De esta manera, debería funcionar independientemente de si el usuario lo ejecuta desde:

el escritorio;
el menú de aplicaciones;
una terminal;
el explorador de archivos.
Esto evitaría el error:

Unable to access jarfile lince-desktop.jar

3. Evitar incompatibilidades con sh/bash
El error:

Bad substitution

sugiere que alguna parte del lanzamiento puede depender de características específicas de Bash.

El script debería:

declarar explícitamente el intérprete necesario mediante un shebang adecuado;
evitar sintaxis que no sea compatible con el intérprete declarado;
o utilizar una sintaxis POSIX cuando sea posible.
4. Configurar automáticamente los permisos
Los scripts y ejecutables instalados deberían disponer automáticamente de los permisos necesarios para su ejecución.

El usuario no debería tener que utilizar manualmente:

chmod +x
para iniciar la aplicación.

5. Crear correctamente la entrada del menú
El instalador debería generar automáticamente una entrada .desktop válida para los principales escritorios Linux.

En el caso de Ubuntu MATE sería recomendable incluir correctamente:

Exec=
Icon=
Path=
Categories=
StartupNotify=
y actualizar la base de datos de aplicaciones después de la instalación.

6. Una única entrada de aplicación
Durante la instalación se generaron varias entradas relacionadas con Lince Plus, por ejemplo en:

~/.local/share/applications/
~/.gnome/apps/
Esto puede generar duplicidades o entradas que no funcionan.

Sería preferible crear una única entrada .desktop estándar, compatible con los escritorios Linux actuales.

7. Comprobación final automática
El instalador debería realizar una prueba al finalizar:

Instalación → dependencias → permisos → Java → lanzamiento → menú → icono

y mostrar un mensaje del tipo:

Lince Plus se ha instalado correctamente y está listo para ejecutarse.

Si alguna comprobación falla, debería indicar claramente cuál es el problema y cómo solucionarlo.

Conclusión
La instalación finalmente se pudo completar y Lince Plus funciona correctamente en Ubuntu MATE 24.04 LTS, tanto desde el escritorio como desde el menú de aplicaciones.

Sin embargo, para un usuario de Linux sin conocimientos técnicos, las incidencias encontradas —Java, permisos de ejecución, directorio de trabajo, lanzador y entrada del menú— hacen que la instalación resulte innecesariamente compleja.

Las mejoras más importantes serían gestionar automáticamente Java, hacer que el lanzador utilice rutas absolutas independientes del directorio actual y generar correctamente una única entrada .desktop. Esto permitiría una instalación mucho más sencilla y predecible en Ubuntu MATE y otras distribuciones Linux basadas en Ubuntu.
