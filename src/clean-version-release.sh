#!/bin/bash
# This script will help to clean the artifacts for the current version

# Function to check if gh CLI is installed and install it if not
check_and_install_gh() {
    if ! command -v gh &> /dev/null; then
        echo "GitHub CLI (gh) is not installed. Installing now..."
        if [[ "$OSTYPE" == "darwin"* ]]; then
            # macOS
            brew install gh
        elif [[ "$OSTYPE" == "linux-gnu"* ]]; then
            # Linux
            type -p curl >/dev/null || (sudo apt update && sudo apt install curl -y)
            curl -fsSL https://cli.github.com/packages/githubcli-archive-keyring.gpg | sudo dd of=/usr/share/keyrings/githubcli-archive-keyring.gpg \
            && sudo chmod go+r /usr/share/keyrings/githubcli-archive-keyring.gpg \
            && echo "deb [arch=$(dpkg --print-architecture) signed-by=/usr/share/keyrings/githubcli-archive-keyring.gpg] https://cli.github.com/packages stable main" | sudo tee /etc/apt/sources.list.d/github-cli.list > /dev/null \
            && sudo apt update \
            && sudo apt install gh -y
        else
            echo "Unsupported operating system. Please install GitHub CLI manually."
            exit 1
        fi
    fi

    # Check gh version
    gh_version=$(gh --version | head -n 1 | awk '{print $3}')
    echo "GitHub CLI version: $gh_version"
}

# Function to get the current version from pom.xml
get_current_version() {
    mvn help:evaluate -Dexpression=project.version -q -DforceStdout
}

# Function to clean up artifacts for a specific version
clean_artifacts() {
    local version=$1
    echo "Cleaning up artifacts for version $version"

    # List all artifacts and filter by the version
    artifacts=$(gh api repos/:owner/:repo/actions/artifacts | jq -r ".artifacts[] | select(.name | contains(\"$version\")) | .id")

    if [ -z "$artifacts" ]; then
        echo "No artifacts found for version $version"
        return
    fi

    # Delete each artifact
    for artifact_id in $artifacts; do
        echo "Deleting artifact $artifact_id"
        gh api -X DELETE repos/:owner/:repo/actions/artifacts/$artifact_id
    done

    echo "Cleanup completed for version $version"
}

# Main script execution
check_and_install_gh

current_version=$(get_current_version)
release_version=${current_version%-SNAPSHOT}

echo "Current version: $current_version"
echo "Release version: $release_version"

read -p "Do you want to clean up artifacts for version $release_version? (y/n) " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]
then
    clean_artifacts $release_version
else
    echo "Cleanup aborted."
    exit 0
fi

#
## Function to check if we're on the develop branch
#check_develop_branch() {
#    current_branch=$(git rev-parse --abbrev-ref HEAD)
#    if [ "$current_branch" != "develop" ]; then
#        echo "Error: You must be on the develop branch to run this script."
#        exit 1
#    fi
#}
#
## Function to check if there are uncommitted changes
#check_uncommitted_changes() {
#    if ! git diff-index --quiet HEAD --; then
#        echo "Error: You have uncommitted changes. Please commit or stash them before proceeding."
#        exit 1
#    fi
#}
#
## Function to get the current version from pom.xml
#get_current_version() {
#    mvn help:evaluate -Dexpression=project.version -q -DforceStdout
#}
#
## Function to get the release version (without -SNAPSHOT)
#get_release_version() {
#    current_version=$(get_current_version)
#    echo ${current_version%-SNAPSHOT}
#}
#
## Function to get the next snapshot version
#get_next_snapshot_version() {
#    current_version=$(get_current_version)
#    major=$(echo "$current_version" | cut -d. -f1)
#    minor=$(echo "$current_version" | cut -d. -f2)
#
#    next_minor=$((minor + 1))
#    echo "$major.$next_minor-SNAPSHOT"
#}
#
## Main script execution
#check_develop_branch
#check_uncommitted_changes
#
#current_version=$(get_current_version)
#release_version=$(get_release_version)
#next_snapshot_version=$(get_next_snapshot_version)
#
#echo "Current version: $current_version"
#echo "Release version: $release_version"
#echo "Next snapshot version: $next_snapshot_version"
#
#read -p "Do you want to proceed with the release? (y/n) " -n 1 -r
#echo
#if [[ ! $REPLY =~ ^[Yy]$ ]]
#then
#    exit 1
#fi
#
## Perform release
#mvn versions:set -DnewVersion=$release_version
#mvn clean install
#git add .
#git commit -m "Release version $release_version"
#git push origin develop
#
## Create a pull request
#git checkout -b release-$release_version
#git push origin release-$release_version
#gh pr create --base main --head release-$release_version --title "Release $release_version" --body "Merging release $release_version into main"
#
#echo "Pull request created. Please review and merge it on GitHub."
#read -p "Press enter when the pull request has been merged..."
#
## Update to next snapshot version
#git checkout develop
#git pull origin develop
#mvn versions:set -DnewVersion=$next_snapshot_version
#git add .
#git commit -m "Prepare for next development iteration"
#git push origin develop
#
#echo "Release process completed successfully!"
