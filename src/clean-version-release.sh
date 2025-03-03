#!/bin/bash
# This script will help to clean the artifacts, draft release and tag for a specific version

# Function to get repository information
get_repo_info() {
    # Get the remote URL
    remote_url=$(git config --get remote.origin.url)
    # Extract owner and repo from the URL
    if [[ $remote_url =~ github\.com[:/]([^/]+)/([^/]+)(\.git)?$ ]]; then
        REPO_OWNER="${BASH_REMATCH[1]}"
        REPO_NAME="${BASH_REMATCH[2]%.git}"
        export REPO_OWNER
        export REPO_NAME
    else
        echo "Error: Could not determine repository information from remote URL"
        exit 1
    fi
}

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

    # Check if authenticated and has correct token scopes
    if ! gh auth status &>/dev/null; then
        echo "Not authenticated with GitHub. Please login:"
        if ! gh auth login --scopes "repo,delete_repo"; then
            echo "Failed to authenticate with GitHub"
            exit 1
        fi
    else
        # Verify token has required scopes
        if ! gh auth status 2>&1 | grep -q "repo"; then
            echo "GitHub token missing required scopes. Please re-authenticate:"
            if ! gh auth login --scopes "repo,delete_repo"; then
                echo "Failed to authenticate with GitHub"
                exit 1
            fi
        fi
    fi
    echo "Successfully authenticated with GitHub"
}

# Function to check if a release is in draft state
check_draft_release() {
    local version=$1
    local api_response
    echo "Checking draft status for version $version in $REPO_OWNER/$REPO_NAME"

    api_response=$(gh api --header "Accept: application/vnd.github.v3+json" "repos/$REPO_OWNER/$REPO_NAME/releases" 2>&1) || {
        error_msg=$api_response
        if [[ $error_msg == *"Not Found"* ]]; then
            echo "Error: Repository not found. Please check if $REPO_OWNER/$REPO_NAME exists and you have access to it."
        elif [[ $error_msg == *"Bad credentials"* ]]; then
            echo "Error: Authentication failed. Please check your GitHub token."
        else
            echo "Error: Failed to fetch releases from GitHub API - $error_msg"
        fi
        return 2
    }

    # Debug: Print the response
    echo "Successfully fetched releases from GitHub API"

    # First check if the release exists
    local release_exists=$(echo "$api_response" | jq -r ".[] | select(.tag_name==\"$version\") | .id")
    if [ -z "$release_exists" ]; then
        echo "No release found with version $version"
        return 1
    fi

    local draft_status=$(echo "$api_response" | jq -r ".[] | select(.tag_name==\"$version\") | .draft")
    echo "Draft status: $draft_status"

    if [ "$draft_status" = "true" ]; then
        echo "Found draft release for version $version"
        return 0
    else
        echo "Release exists but is not in draft state"
        return 1
    fi
}

# Function to delete draft release
delete_draft_release() {
    local version=$1
    local api_response
    api_response=$(gh api --header "Accept: application/vnd.github.v3+json" "repos/$REPO_OWNER/$REPO_NAME/releases" 2>&1) || {
        error_msg=$api_response
        if [[ $error_msg == *"Not Found"* ]]; then
            echo "Error: Repository not found. Please check if $REPO_OWNER/$REPO_NAME exists and you have access to it."
        elif [[ $error_msg == *"Bad credentials"* ]]; then
            echo "Error: Authentication failed. Please check your GitHub token."
        else
            echo "Error: Failed to fetch releases from GitHub API - $error_msg"
        fi
        return 1
    }
    local release_id=$(echo "$api_response" | jq -r ".[] | select(.tag_name==\"$version\") | .id")
    if [ -z "$release_id" ]; then
        echo "No release found with tag $version"
        return 1
    fi
    echo "Deleting draft release with ID $release_id"
    delete_response=$(gh api --header "Accept: application/vnd.github.v3+json" -X DELETE "repos/$REPO_OWNER/$REPO_NAME/releases/$release_id" 2>&1) || {
        error_msg=$delete_response
        if [[ $error_msg == *"Not Found"* ]]; then
            echo "Error: Release not found. It might have been already deleted."
        elif [[ $error_msg == *"Bad credentials"* ]]; then
            echo "Error: Authentication failed. Please check your GitHub token."
        else
            echo "Error: Failed to delete release - $error_msg"
        fi
        return 1
    }
    echo "Successfully deleted release"
    return 0
}

# Function to delete tag
delete_tag() {
    local version=$1
    echo "Attempting to delete tag $version"
    delete_response=$(gh api --header "Accept: application/vnd.github.v3+json" -X DELETE "repos/$REPO_OWNER/$REPO_NAME/git/refs/tags/$version" 2>&1) || {
        error_msg=$delete_response
        if [[ $error_msg == *"Not Found"* ]]; then
            echo "Error: Tag $version not found. It might have been already deleted."
        elif [[ $error_msg == *"Bad credentials"* ]]; then
            echo "Error: Authentication failed. Please check your GitHub token."
        else
            echo "Error: Failed to delete tag - $error_msg"
        fi
        return 1
    }
    echo "Successfully deleted tag $version"
    return 0
}

# Function to get the current version from pom.xml
get_current_version() {
    mvn help:evaluate -Dexpression=project.version -q -DforceStdout
}

# Function to delete packages for a specific version
delete_packages() {
    local version=$1
    echo "Checking for packages with version $version"

    local api_response
    echo "Fetching package versions..."
    api_response=$(gh api --header "Accept: application/vnd.github.v3+json" "repos/$REPO_OWNER/$REPO_NAME/packages/maven/lince-plus-root/versions" 2>&1) || {
        error_msg=$api_response
        if [[ $error_msg == *"Not Found"* ]]; then
            echo "Error: No packages found or no access to packages."
        elif [[ $error_msg == *"Bad credentials"* ]]; then
            echo "Error: Authentication failed. Please check your GitHub token."
        else
            echo "Error: Failed to fetch packages - $error_msg"
        fi
        return 1
    }

    echo "API Response for packages: $api_response"
    # Try to match exact version or version with classifier
    local package_ids=$(echo "$api_response" | jq -r ".[] | select(.metadata.package_version==\"$version\") | .id")

    if [ -z "$package_ids" ]; then
        # If no exact match, try to find packages containing the version
        package_ids=$(echo "$api_response" | jq -r ".[] | select(.metadata.package_version | contains(\"$version\")) | .id")
    fi

    echo "Found package IDs: $package_ids"

    if [ -z "$package_ids" ]; then
        echo "No packages found for version $version"
        return 0
    fi

    local failed=0
    for package_id in $package_ids; do
        echo "Deleting package version with ID $package_id"
        delete_response=$(gh api --header "Accept: application/vnd.github.v3+json" -X DELETE "repos/$REPO_OWNER/$REPO_NAME/packages/maven/lince-plus-root/versions/$package_id" 2>&1) || {
            error_msg=$delete_response
            if [[ $error_msg == *"Not Found"* ]]; then
                echo "Warning: Package version $package_id not found. It might have been already deleted."
            elif [[ $error_msg == *"Bad credentials"* ]]; then
                echo "Warning: Authentication failed while deleting package version $package_id"
            else
                echo "Warning: Failed to delete package version - $error_msg"
            fi
            failed=1
        }
    done

    if [ $failed -eq 1 ]; then
        echo "Some package versions could not be deleted"
        return 1
    fi

    echo "Package versions cleanup completed successfully"
    return 0
}

# Function to clean up artifacts for a specific version
clean_artifacts() {
    local version=$1
    echo "Cleaning up artifacts for version $version"

    # List all artifacts and filter by the version
    local api_response
    api_response=$(gh api --header "Accept: application/vnd.github.v3+json" "repos/$REPO_OWNER/$REPO_NAME/actions/artifacts" 2>&1) || {
        error_msg=$api_response
        if [[ $error_msg == *"Not Found"* ]]; then
            echo "Error: Repository not found or no access to artifacts."
        elif [[ $error_msg == *"Bad credentials"* ]]; then
            echo "Error: Authentication failed. Please check your GitHub token."
        else
            echo "Error: Failed to fetch artifacts - $error_msg"
        fi
        return 1
    }

    local artifacts=$(echo "$api_response" | jq -r ".artifacts[] | select(.name | contains(\"$version\")) | .id")

    if [ -z "$artifacts" ]; then
        echo "No artifacts found for version $version"
        return 0
    fi

    local failed=0
    # Delete each artifact
    for artifact_id in $artifacts; do
        echo "Deleting artifact $artifact_id"
        delete_response=$(gh api --header "Accept: application/vnd.github.v3+json" -X DELETE "repos/$REPO_OWNER/$REPO_NAME/actions/artifacts/$artifact_id" 2>&1) || {
            error_msg=$delete_response
            if [[ $error_msg == *"Not Found"* ]]; then
                echo "Warning: Artifact $artifact_id not found. It might have been already deleted."
            elif [[ $error_msg == *"Bad credentials"* ]]; then
                echo "Warning: Authentication failed while deleting artifact $artifact_id"
            else
                echo "Warning: Failed to delete artifact $artifact_id - $error_msg"
            fi
            failed=1
        }
    done

    if [ $failed -eq 1 ]; then
        echo "Some artifacts could not be deleted"
        return 1
    fi

    echo "Artifacts cleanup completed successfully"
    return 0
}

# Main script execution
check_and_install_gh
get_repo_info

# Request version to clean
while true; do
    read -p "Enter the version to clean: " version_to_clean
    if [ -z "$version_to_clean" ]; then
        echo "Error: Version cannot be empty. Please provide a valid version."
        continue
    fi
    break
done

echo "Version to clean: $version_to_clean"

# Check if release exists and is in draft
echo "Checking release status..."
check_draft_release "$version_to_clean"
check_status=$?

if [ $check_status -eq 2 ]; then
    echo "Failed to check release status. Please verify your GitHub token and permissions."
    exit 1
elif [ $check_status -eq 0 ]; then
    echo "Proceeding with draft release cleanup..."
    read -p "Do you want to clean up artifacts, packages, draft release and tag for version $version_to_clean? (y/n) " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]
    then
        if ! clean_artifacts "$version_to_clean"; then
            echo "Warning: Failed to clean up some artifacts"
        fi
        if ! delete_packages "$version_to_clean"; then
            echo "Warning: Failed to delete some packages"
        fi
        if ! delete_draft_release "$version_to_clean"; then
            echo "Warning: Failed to delete draft release"
        fi
        if ! delete_tag "$version_to_clean"; then
            echo "Warning: Failed to delete tag"
        fi
        echo "Cleanup process completed"
    else
        echo "Cleanup aborted."
        exit 0
    fi
else
    echo "No draft release found for version $version_to_clean"
    read -p "Do you want to clean up artifacts and packages for version $version_to_clean? (y/n) " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]
    then
        if ! clean_artifacts "$version_to_clean"; then
            echo "Warning: Failed to clean up some artifacts"
        fi
        if ! delete_packages "$version_to_clean"; then
            echo "Warning: Failed to delete some packages"
        fi
        echo "Cleanup process completed"
    else
        echo "Cleanup aborted."
        exit 0
    fi
fi
