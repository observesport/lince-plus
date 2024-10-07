#!/bin/bash

# Set variables
PROJECT_DIR="/Users/berto/src/lince/lince-plus-public/src"
GITHUB_USERNAME="observesport"
GITHUB_REPO="lince-plus"

# Check if the GITHUB_LICENSE_TOKEN environment variable is set
if [ -z "$GITHUB_LICENSE_TOKEN" ]; then
    echo "Error: GITHUB_LICENSE_TOKEN environment variable is not set"
    exit 1
fi

# Navigate to the project directory
cd $PROJECT_DIR || exit 1

# Ensure we're on the main branch and up to date
git checkout main
git pull origin main

# Generate a suggested version number based on the current date and time
SUGGESTED_VERSION=$(date +"%Y.%m.%d-%H%M%S")

# Ask the user for confirmation or a new version
echo "Suggested version: $SUGGESTED_VERSION"
read -p "Do you want to use this version? (y/n): " CONFIRM

if [ "$CONFIRM" != "y" ]; then
    read -p "Enter the desired version number: " USER_VERSION
    VERSION=$USER_VERSION
else
    VERSION=$SUGGESTED_VERSION
fi

echo "Using version: $VERSION"

# Update the project version in pom.xml
mvn versions:set -DnewVersion=$VERSION

# Run Maven deploy command with the new version
mvn clean deploy \
    -DaltDeploymentRepository=github::default::https://maven.pkg.github.com/$GITHUB_USERNAME/$GITHUB_REPO \
    -Dtoken=$GITHUB_LICENSE_TOKEN

# Check if the deployment was successful
if [ $? -eq 0 ]; then
    echo "Deployment successful! Version: $VERSION"

    # Commit the version change
    git add pom.xml
    git commit -m "Update version to $VERSION"
    git push origin main
else./m
    echo "Deployment failed. Please check the logs for more information."
    exit 1
fi
