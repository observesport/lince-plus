#!/bin/bash

echo "This script will install and set Java 17.0.13.fx-librca as the default Java version."

read -p "Do you want to install Java 17.0.13.fx-librca? (y/n): " install_choice
if [[ $install_choice == "y" || $install_choice == "Y" ]]; then
    sdk install java 17.0.13.fx-librca
    echo "Java 17.0.13.fx-librca has been installed."
else
    echo "Skipping Java installation."
fi

read -p "Do you want to set Java 17.0.13.fx-librca as the default Java version? (y/n): " default_choice
if [[ $default_choice == "y" || $default_choice == "Y" ]]; then
    sdk default java 17.0.13.fx-librca
    echo "Java 17.0.13.fx-librca has been set as the default Java version."
else
    echo "Skipping setting default Java version."
fi

#read -p "Do you want to start the project with the specified JVM parameters? (y/n): " start_choice
#if [[ $start_choice == "y" || $start_choice == "Y" ]]; then
#    default_jar_path="src/lince-desktop/target/lince-desktop.jar"
#    read -p "Enter the path to your project's JAR file (default: $default_jar_path): " jar_path
#    jar_path=${jar_path:-$default_jar_path}
#
#    java -jar "$jar_path" \
#    --add-opens java.base/sun.nio.ch=ALL-UNNAMED \
#    --add-opens java.base/java.io=ALL-UNNAMED \
#    --add-opens javafx.base/com.sun.javafx.collections=ALL-UNNAMED \
#    --add-opens javafx.base/com.sun.javafx.collections=javafx.web \
#    --add-opens javafx.controls/javafx.scene.control=ALL-UNNAMED \
#    --add-opens javafx.controls/javafx.scene.control.skin=ALL-UNNAMED \
#    --add-opens javafx.graphics/javafx.scene.input=ALL-UNNAMED \
#    --add-opens javafx.web/javafx.scene.web=ALL-UNNAMED \
#    --add-opens javafx.web/com.sun.webkit=ALL-UNNAMED \
#    --add-opens javafx.controls/com.sun.javafx.scene.control.skin=ALL-UNNAMED \
#    --add-exports javafx.graphics/com.sun.javafx.application=ALL-UNNAMED \
#    --add-exports javafx.graphics/com.sun.glass.ui=ALL-UNNAMED \
#    --add-opens javafx.graphics/com.sun.glass.ui=ALL-UNNAMED \
#    --add-modules ALL-MODULE-PATH \
#    --add-modules javafx.controls,javafx.fxml,javafx.graphics,javafx.media,javafx.swing,javafx.web \
#    --add-exports javafx.graphics/com.sun.javafx.application=ALL-UNNAMED
#    echo "Project started with specified JVM parameters."
#else
#    echo "Skipping project start."
#fi

echo "Script execution completed."