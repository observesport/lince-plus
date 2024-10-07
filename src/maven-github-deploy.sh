#!/bin/bash

set -e

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

# Ensure we're on the correct branch and up to date
git checkout $BRANCH_NAME
git pull origin $BRANCH_NAME

# Read the current version from pom.xml
CURRENT_VERSION=$(mvn help:evaluate -Dexpression=project.version -q -DforceStdout)

# Function to increment version
increment_version() {
    local version=$1
    local delimiter=.
    local array=($(echo "$version" | tr $delimiter '\n'))
    array[-1]=$((array[-1] + 1))
    echo $(local IFS=$delimiter ; echo "${array[*]}")
}

# Generate a suggested version number based on the current version
if [[ $CURRENT_VERSION == *-SNAPSHOT ]]; then
    # If it's a SNAPSHOT version, just use the current version
    SUGGESTED_VERSION=$CURRENT_VERSION
else
    # If it's a release version, increment the last number and add -SNAPSHOT
    SUGGESTED_VERSION="$(increment_version $CURRENT_VERSION)-SNAPSHOT"
fi

# Ask the user for confirmation or a new version
echo "Current version: $CURRENT_VERSION"
echo "Suggested version: $SUGGESTED_VERSION"
read -p "Do you want to use the suggested version? (y/n): " CONFIRM

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
if mvn clean deploy \
    -DaltDeploymentRepository=github::default::https://maven.pkg.github.com/$GITHUB_USERNAME/$GITHUB_REPO \
    -Dtoken=$GITHUB_LICENSE_TOKEN
then
    echo "Deployment successful! Version: $VERSION"

    # Commit the version change
    git add pom.xml
    git commit -m "Update version to $VERSION"

    # Attempt to push changes
    if git push origin $BRANCH_NAME; then
        echo "Changes pushed successfully."
    else
        echo "Failed to push changes. Please push manually."
    fi

    read -p "Do you want to commit the version change and remove the back up files? (y/n): " CONFIRM
    if [ "$CONFIRM" != "y" ]; then
      # Remove backup files
      mvn versions:commit
    fi
else
    echo "Deployment failed. Please check the logs for more information."
    exit 1
fi

