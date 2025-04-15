#!/bin/bash

# GitHub repository details
GITHUB_USERNAME="observesport"
GITHUB_REPO="lince-plus"
REPOSITORY_ID="github"
MAVEN_REPO_URL="https://maven.pkg.github.com/${GITHUB_USERNAME}/${GITHUB_REPO}"

# Path to local Maven repository
LOCAL_MAVEN_REPO="${HOME}/.m2/repository"

# Path to local lib directory
LIB_DIR="$(pwd)/lib"

# Create lib directory if it doesn't exist
mkdir -p "$LIB_DIR"
echo "Using lib directory: ${LIB_DIR}"

# Function to upload a dependency to GitHub Packages
upload_dependency() {
    local group_id=$1
    local artifact_id=$2
    local version=$3

    # Convert group_id to path format (org.renjin -> org/renjin)
    local group_path=${group_id//./\/}

    # Construct path to the JAR file in local Maven repository
    local jar_path="${LOCAL_MAVEN_REPO}/${group_path}/${artifact_id}/${version}/${artifact_id}-${version}.jar"

    # Check if the JAR file exists
    if [ -f "$jar_path" ]; then
        echo "Uploading ${group_id}:${artifact_id}:${version}"

        # Create the same directory structure in lib directory
        local lib_dir_structure="${LIB_DIR}/${group_path}/${artifact_id}/${version}"
        mkdir -p "$lib_dir_structure"

        # Copy the JAR file preserving the folder structure
        local lib_jar="${lib_dir_structure}/${artifact_id}-${version}.jar"
        cp "$jar_path" "$lib_jar"
        echo "Copied to ${lib_jar}"

        # Deploy using the copied JAR file
        mvn deploy:deploy-file \
          -DgroupId="${group_id}" \
          -DartifactId="${artifact_id}" \
          -Dversion="${version}" \
          -Dpackaging=jar \
          -Dfile="${lib_jar}" \
          -DrepositoryId="${REPOSITORY_ID}" \
          -Durl="${MAVEN_REPO_URL}" \
          -DgeneratePom=true

        echo "-----------------------------------"
    else
        echo "WARNING: JAR file not found: ${jar_path}"
    fi
}

# Main script
echo "Starting upload of Renjin dependencies to GitHub Packages..."
echo "Repository: ${MAVEN_REPO_URL}"
echo "-----------------------------------"

# Core Renjin packages
upload_dependency "org.renjin" "renjin-script-engine" "0.9.2726"
upload_dependency "org.renjin" "renjin-core" "0.9.2726"
upload_dependency "org.renjin" "renjin-appl" "0.9.2726"
upload_dependency "org.renjin" "renjin-blas" "0.9.2726"
upload_dependency "org.renjin" "renjin-nmath" "0.9.2726"
upload_dependency "org.renjin" "renjin-math-common" "0.9.2726"
upload_dependency "org.renjin" "renjin-lapack" "0.9.2726"
upload_dependency "org.renjin" "gcc-runtime" "0.9.2726"
upload_dependency "org.renjin" "renjin-asm" "5.0.4b"
upload_dependency "org.renjin" "renjin-guava" "17.0b"
upload_dependency "org.renjin" "renjin-gnur-runtime" "0.9.2726"
upload_dependency "org.renjin" "stats" "0.9.2726"
upload_dependency "org.renjin" "methods" "0.9.2726"
upload_dependency "org.renjin" "utils" "0.9.2726"
upload_dependency "org.renjin" "grDevices" "0.9.2726"
upload_dependency "org.renjin" "graphics" "0.9.2726"
upload_dependency "org.renjin" "compiler" "0.9.2726"
upload_dependency "org.renjin" "libstdcxx" "4.7.4-b18"

# Renjin packages with older versions
upload_dependency "org.renjin" "parallel" "0.8.2397"
upload_dependency "org.renjin" "datasets" "0.8.2397"
upload_dependency "org.renjin" "splines" "0.8.2397"
upload_dependency "org.renjin" "tcltk" "0.8.2397"
upload_dependency "org.renjin" "stats4" "0.8.2397"
upload_dependency "org.renjin" "tools" "0.8.2397"
upload_dependency "org.renjin" "grid" "0.8.2397"

# CRAN packages for Renjin
upload_dependency "org.renjin.cran" "jsonlite" "1.5-b2"
upload_dependency "org.renjin.cran" "e1071" "1.6-8-b34"
upload_dependency "org.renjin.cran" "class" "7.3-14-b86"
upload_dependency "org.renjin.cran" "MASS" "7.3-49-b4"
upload_dependency "org.renjin.cran" "KappaGUI" "2.0.2-b1"
upload_dependency "org.renjin.cran" "shiny" "1.0.5-b85"
upload_dependency "org.renjin.cran" "xtable" "1.8-2-b80"
upload_dependency "org.renjin.cran" "htmltools" "0.3.6-b29"
upload_dependency "org.renjin.cran" "Rcpp" "0.12.13-renjin-15"
upload_dependency "org.renjin.cran" "sourcetools" "0.1.6-b17"
upload_dependency "org.renjin.cran" "digest" "0.6.14-b4"
upload_dependency "org.renjin.cran" "R6" "2.2.2-b38"
upload_dependency "org.renjin.cran" "mime" "0.5-b45"
upload_dependency "org.renjin.cran" "httpuv" "1.3.5-b15"
upload_dependency "org.renjin.cran" "irr" "0.84-b243"
upload_dependency "org.renjin.cran" "lpSolve" "5.6.13-b76"

echo "Upload process completed."
echo "All JAR files have been copied to ${LIB_DIR} with their original folder structure"
