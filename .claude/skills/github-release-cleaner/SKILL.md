---
name: github-release-cleaner
description: Clean up GitHub draft releases and associated package versions for the observesport/lince-plus repository. Use this skill whenever the user wants to delete draft releases, remove stale GitHub Packages versions, clean up release artifacts, fix CI/CD failures caused by leftover packages, or manage GitHub releases. Triggers on phrases like "clean releases", "delete draft", "remove package versions", "clean artifacts", "release cleanup", "github packages cleanup", "cicd failing master", or any request to tidy up GitHub release state.
---

# GitHub Release Cleaner

This skill helps you clean up draft GitHub releases and their associated GitHub Packages artifacts for the `observesport/lince-plus` repository. The typical scenario: a failed or unwanted CI/CD run creates a draft release AND publishes package versions — deleting the release alone won't fix CI/CD failures if the package versions persist.

## Why this matters

When GitHub Actions pushes to `master`, Maven publishes packages to GitHub Packages. If a draft release exists with those package versions, re-running CI/CD on master will fail with a conflict. You must delete the package versions **before** or alongside deleting the draft release.

## Workflow

### Step 1: Verify gh CLI authentication

```bash
gh auth status
```

If not authenticated:
```bash
gh auth login
```

Ensure the token has `read:packages`, `delete:packages`, `repo` scopes. If using a personal access token (PAT), it must have `delete:packages` — GitHub's fine-grained tokens do NOT support package deletion as of 2025; use a classic PAT.

### Step 2: Discover draft releases

List all draft releases:
```bash
gh release list --repo observesport/lince-plus --limit 50 | grep -i draft
```

Or using the API to get full details including tag names:
```bash
gh api repos/observesport/lince-plus/releases --jq '.[] | select(.draft==true) | {id: .id, tag_name: .tag_name, name: .name, created_at: .created_at}'
```

**Present these to the user** with tag names and creation dates. Ask which one(s) they want to clean. If there's only one, suggest it as the default.

### Step 3: Identify the package version to delete

Ask the user for the version string (e.g., `4.0.8`, `4.0.9-SNAPSHOT`), or infer it from the draft release tag name.

**Important:** `observesport` is a GitHub **user** account, not an org. Use user-scoped GraphQL queries — the `/orgs/` REST endpoints will return 404.

List all lince packages and their versions via GraphQL:
```bash
gh api graphql -f query='
{
  user(login: "observesport") {
    packages(first: 100, packageType: MAVEN, names: ["com.lince.observer.lince-data", "com.lince.observer.lince-data-fx", "com.lince.observer.lince-desktop", "com.lince.observer.lince-ai", "com.lince.observer.lince-math", "com.lince.observer.lince-transcoding"]) {
      nodes {
        id
        name
        versions(first: 20) {
          nodes { id version }
        }
      }
    }
  }
}'
```

The lince-plus project publishes 6 Maven modules: `lince-data`, `lince-data-fx`, `lince-desktop`, `lince-ai`, `lince-math`, `lince-transcoding`. Find all version node IDs matching the target version string.

### Step 4: Confirm with user before deleting

Show a summary:
```
Draft release to delete: untagged-XXXXX (release ID: NNNN)
Package versions to delete:
  - com.lince.observer.data / version 4.0.8 (version ID: XXXXXX)
  - com.lince.observer.desktop / version 4.0.8 (version ID: XXXXXX)
  - ... (N total)

Proceed? (yes/no)
```

Never delete without explicit confirmation.

### Step 5: Delete package versions first

Delete each package version using the GraphQL mutation (REST endpoints don't work for user-owned packages):
```bash
gh api graphql -f query="mutation { deletePackageVersion(input: {packageVersionId: \"VERSION_NODE_ID\"}) { success } }"
```

Loop pattern for bulk deletion using the node IDs from Step 3:
```bash
for id in PV_xxx PV_yyy PV_zzz; do
  result=$(gh api graphql -f query="mutation { deletePackageVersion(input: {packageVersionId: \"$id\"}) { success } }" 2>&1)
  echo "$id: $result"
done
```

**Important:** The token must belong to the `observesport` account (the package owner), not a collaborator account. A collaborator token will return `FORBIDDEN` even with `delete:packages` scope.

### Step 6: Delete the draft release

```bash
gh release delete TAG_NAME --repo observesport/lince-plus --yes
```

If the tag is untagged (e.g., `untagged-2931a5d63b30f2a8c42c`), use the release ID directly via the API:
```bash
gh api --method DELETE /repos/observesport/lince-plus/releases/RELEASE_ID
```

To also delete the git tag (if it exists):
```bash
gh api --method DELETE /repos/observesport/lince-plus/git/refs/tags/TAG_NAME
```

### Step 7: Verify cleanup

```bash
# Confirm no draft releases remain
gh api repos/observesport/lince-plus/releases --jq '.[] | select(.draft==true) | .tag_name'

# Confirm package versions are gone
gh api /orgs/observesport/packages/maven/PACKAGE_NAME/versions \
  --jq '.[] | select(.name=="TARGET_VERSION")'
```

If both return empty, the cleanup is complete and CI/CD on master should succeed.

## Common issues

**403 on package deletion**: The token lacks `delete:packages`. Use a classic PAT (not fine-grained) with `delete:packages` scope. Set it: `export GITHUB_TOKEN=ghp_...` then re-run.

**Release not found by tag**: Use the release ID from the API (`gh api repos/observesport/lince-plus/releases`) instead of the tag name.

**Package still shows after deletion**: GitHub Packages has eventual consistency — wait 30–60 seconds and recheck.

**CI still fails after cleanup**: Check if Maven's `pom.xml` version in the commit matches a version that still exists in packages. Run `mvn versions:display-dependency-updates` from `src/` to diagnose.
