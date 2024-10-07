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

# Run Maven deploy command
mvn deploy \
    -DaltDeploymentRepository=github::default::https://maven.pkg.github.com/$GITHUB_USERNAME/$GITHUB_REPO \
    -Dtoken=$GITHUB_LICENSE_TOKEN

# Check if the deployment was successful
if [ $? -eq 0 ]; then
    echo "Deployment successful!"
else
    echo "Deployment failed. Please check the logs for more information."
    exit 1
fi
