#!/bin/bash

# Check if INSTALL4J_LICENSE is set
if [ -z "$INSTALL4J_LICENSE" ]; then
    echo "Error: INSTALL4J_LICENSE environment variable is not set."
    echo "Please set it using: export INSTALL4J_LICENSE=your_license_key"
    exit 1
fi

# Set INSTALL4J_HOME
export INSTALL4J_HOME="/Applications/install4j.app"

# Run Maven command with additional arguments
mvn clean install -U -DskipTests \
    -DINSTALL4J_LICENSE="$INSTALL4J_LICENSE" \
    -Dinstall4j.home="$INSTALL4J_HOME"
