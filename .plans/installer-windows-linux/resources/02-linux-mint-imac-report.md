Informe-resumen: Instalación de Lince Plus en Linux Mint (iMac mid-2011)
1. Contexto del sistema
Equipo: iMac mid-2011 (Intel 2ª generación Core, arquitectura x64, gráfica AMD Radeon HD 6xxx)
Sistema operativo: Linux Mint (entorno Cinnamon)
Software: Lince Plus 4.1.0 (instalador .sh basado en install4j), sustituyendo versiones previas (4.0.10 y 4.0.12-SNAPSHOT)
2. Incidencias detectadas y resolución
2.1. Instalaciones previas dispersas y sin desinstalador aparente
Antes de instalar la versión 4.1.0 se detectaron múltiples restos de instalaciones anteriores en distintas ubicaciones del sistema: carpeta de usuario (~/lince-plus), lanzadores duplicados en tres rutas distintas (~/.gnome/apps, ~/.local/share/applications, /usr/share/applications), preferencias de Java, cachés y una copia obsoleta en ~/Baixades. El instalador install4j sí incorpora un script uninstall oficial (localizado en la propia carpeta de instalación), pero no es evidente a simple vista y su nombre no contiene "lince", lo que dificulta su localización.

Resolución: se ejecutó ~/lince-plus/uninstall -q, complementado con limpieza manual de residuos (lanzadores, preferencias, cachés).

2.2. Instalación de sistema huérfana en /usr/local
Tras la desinstalación "oficial" y el nuevo despliegue de 4.1.0, seguía apareciendo un icono duplicado en la categoría "Altres" del menú de aplicaciones. La investigación reveló una segunda instalación completa, independiente de la de usuario, ubicada en /usr/local/lince-plus (instalada en algún momento anterior con privilegios de administrador). Esta instalación de sistema no fue tocada por el desinstalador de usuario, ya que este solo gestiona su propio ámbito (~/lince-plus).

Resolución: eliminación manual con sudo rm -rf /usr/local/lince-plus y actualización de la base de datos de escritorio (update-desktop-database).

2.3. Fallo de arranque: "Bad substitution"
Una vez limpio el sistema y reinstalada la versión 4.1.0, ni el icono del escritorio ni el del menú abrían la aplicación (sin mensaje de error visible). Al ejecutar el script de lanzamiento directamente desde terminal, apareció el error:

./lince-plus: 419: Bad substitution
Causa raíz: el script lince-plus generado por install4j utiliza sintaxis específica de Bash (sustituciones de parámetros no soportadas por POSIX sh). En Linux Mint —como en la mayoría de distribuciones basadas en Debian/Ubuntu— /bin/sh está enlazado a dash, un intérprete POSIX estricto que no reconoce esa sintaxis. El fichero .desktop generado por el instalador invoca el script sin especificar el intérprete (Exec="/ruta/lince-plus" %U), por lo que el sistema recurre al shebang interno del script o al intérprete por defecto, provocando el fallo silencioso.

Resolución: modificación manual de los tres ficheros .desktop (carpeta de instalación, escritorio y menú de aplicaciones) para forzar la invocación explícita con bash:

Exec=bash "/home/marquitusmt/lince-plus/lince-plus" %U
2.4. Permisos de ejecución insuficientes
El script auxiliar lince-os-launcher.sh no tenía permiso de ejecución tras la instalación (Permission denied), y hubo que aplicarlo manualmente con chmod +x.

3. Propuestas de mejora
Corregir el script de lanzamiento generado por install4j para que sea compatible con sh POSIX (evitando sustituciones específicas de Bash), o bien fijar explícitamente el intérprete en el shebang (#!/bin/bash) y en el Exec= del .desktop, evitando depender del intérprete por defecto del sistema.
Incluir un desinstalador de ámbito de sistema. El desinstalador actual solo cubre instalaciones de usuario; si en algún momento se instaló con sudo, no queda rastro de cómo revertirlo limpiamente. Sería recomendable que el propio instalador detectara y avisara de instalaciones previas en otras rutas (usuario vs. sistema) antes de proceder.
Asegurar permisos de ejecución correctos post-instalación en todos los scripts auxiliares (lince-os-launcher.sh incluido), evitando que el usuario tenga que corregirlo manualmente.
Documentar explícitamente el requisito de Bash en la documentación oficial de instalación en Linux, junto con un apartado de resolución de problemas específico para distribuciones Debian/Ubuntu-based (Mint, Pop!_OS, etc.), dado que es un fallo silencioso difícil de diagnosticar sin recurrir a terminal.
Unificar la ubicación de los lanzadores de escritorio o, como mínimo, documentar todas las rutas donde el instalador puede depositar copias (~/.gnome/apps, ~/.local/share/applications, /usr/share/applications), para facilitar la limpieza manual en caso de reinstalación o migración de versión.
Considerar un diagnóstico de compatibilidad de hardware/Java en el propio instalador, dado que en equipos con más de una década de antigüedad (como el iMac mid-2011) conviene verificar de antemano la versión de Java disponible y la memoria RAM, evitando arranques fallidos por incompatibilidades no comunicadas al usuario.
