# Renjin Dependencies Upload Scripts

This repository contains scripts to upload Renjin dependencies from your local Maven repository to GitHub Packages. These scripts are useful when the original Renjin Nexus repository is down or unavailable.

## Background

The Lince Plus project depends on several Renjin artifacts, which are normally available from the Bedatadriven Nexus repository at `https://nexus.bedatadriven.com/content/groups/public/`. If this repository is down, you can use these scripts to upload the dependencies from your local Maven repository to GitHub Packages.

## Prerequisites

- Maven installed and available in your PATH
- A GitHub Personal Access Token with the `write:packages` scope
- The Renjin dependencies already downloaded in your local Maven repository

## Scripts

### 1. Upload All Renjin Dependencies

The `upload_renjin_to_github.sh` script uploads all Renjin dependencies found in your local Maven repository to GitHub Packages.

#### Usage

```bash
# Set your GitHub token
export GITHUB_TOKEN=your_github_token

# Make the script executable
chmod +x upload_renjin_to_github.sh

# Run the script
./upload_renjin_to_github.sh
```

### 2. Upload Specific Renjin Dependencies

The `upload_specific_renjin_deps.sh` script uploads only the specific Renjin dependencies used in the Lince Plus project, along with their transitive dependencies.

#### Usage

```bash
# Set your GitHub token
export GITHUB_TOKEN=your_github_token

# Make the script executable
chmod +x upload_specific_renjin_deps.sh

# Run the script
./upload_specific_renjin_deps.sh
```

## Configuring Your Project

After uploading the dependencies to GitHub Packages, you need to configure your project to use them. Add the following repository configuration to your `pom.xml`:

```xml
<repositories>
    <repository>
        <id>github</id>
        <name>GitHub observesport Apache Maven Packages</name>
        <url>https://maven.pkg.github.com/observesport/lince-plus</url>
    </repository>
</repositories>
```

And add the following server configuration to your `~/.m2/settings.xml`:

```xml
<servers>
    <server>
        <id>github</id>
        <username>your_github_username</username>
        <password>your_github_token</password>
    </server>
</servers>
```

## Troubleshooting

If you encounter any issues:

1. Make sure your GitHub token has the correct permissions
2. Check that the dependencies exist in your local Maven repository
3. Verify that the paths in the scripts match your local Maven repository structure

## Dependencies Uploaded

The specific dependencies uploaded by the `upload_specific_renjin_deps.sh` script are:

- org.renjin:renjin-script-engine:0.9.2726
- org.renjin.cran:jsonlite:1.5-b2
- org.renjin.cran:e1071:1.6-8-b34
- org.renjin.cran:KappaGUI:2.0.2-b1
- org.renjin.cran:irr:0.84-b243

Along with their transitive dependencies.
