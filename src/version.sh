#!/bin/bash

# Ask for the new version
echo "Enter the new version number:"
read newVersion

# Execute mvn versions:set with the new version
mvn versions:set -DnewVersion="$newVersion"

# Ask for confirmation to commit
echo "Do you want to commit this version change? (y/n)"
read confirm

if [ "$confirm" = "y" ] || [ "$confirm" = "Y" ]; then
    mvn versions:commit
    echo "Version change committed."

    # Git commit
    git add .
    git commit -m "Release version $newVersion"
    echo "Changes committed to Git with message: Release version $newVersion"

    # Ask if user wants to push
    echo "Do you want to push this commit to the remote repository? (y/n)"
    read pushConfirm
    if [ "$pushConfirm" = "y" ] || [ "$pushConfirm" = "Y" ]; then
        git push
        echo "Changes pushed to remote repository."
    else
        echo "Changes not pushed. You can push manually later."
    fi
else
    mvn versions:revert
    echo "Version change reverted."
fi
