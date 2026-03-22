---
name: gen-mr
description: Generate merge/pull requests using CLI tools (gh/glab). Use this skill whenever the user wants to create a PR, MR, merge request, or push changes for review. Handles branch→develop and develop→master workflows with conventional commits, squash options, draft PRs, branch cleanup, and version bumping. Triggers on "create pr", "create mr", "gen mr", "merge request", "push for review", "open pr", "open mr", or any variation of requesting a pull/merge request.
---

# gen-mr — Merge/Pull Request Generator

Generate merge requests using the appropriate CLI tool (`gh` for GitHub, `glab` for GitLab), with conventional commit titles, auto-generated descriptions, and workflow-aware defaults.

## Workflow Overview

There are two flows:

1. **Feature branch → develop**: The standard flow for shipping work. After merge, the source branch is deleted.
2. **develop → master**: A release promotion. The branch is kept. May require a version bump.

## Step-by-Step Procedure

### 1. Detect Platform

Check the git remote URL to determine the platform:
- `github.com` in remote → use `gh`
- `gitlab.com` or other GitLab instance in remote → use `glab`

Then verify the CLI is authenticated:

```bash
# GitHub
gh auth status

# GitLab
glab auth status
```

If not authenticated, run web login:

```bash
gh auth login --web
# or
glab auth login
```

### 2. Determine Flow Type

Check the current branch:
- If on `develop` → this is a **develop → master** promotion
- If on any other branch → this is a **branch → develop** merge

### 3. Pre-flight Checks

Before creating the MR:

1. Ensure working tree is clean (no uncommitted changes). If dirty, stop and tell the user.
2. Push the current branch to remote if not already pushed:
   ```bash
   git push -u origin <branch-name>
   ```
3. Check if a PR/MR already exists for this branch. If so, show the URL and ask if the user wants to update it or create a new one.

### 4. Generate the Title (Conventional Commit Format)

Derive the commit type from the branch prefix:

| Branch prefix | Commit type |
|---------------|-------------|
| `feat/`, `feature/` | `feat` |
| `fix/`, `bug/`, `bugfix/`, `hotfix/` | `fix` |
| `docs/` | `docs` |
| `refactor/` | `refactor` |
| `test/` | `test` |
| `chore/` | `chore` |
| `ci/` | `ci` |
| `perf/` | `perf` |
| `release/` | `release` |

For **branch → develop**: Generate a conventional commit title from the branch name.
Example: `fix/export-theme-gseq` → `fix: export theme gseq`

For **develop → master**: Use `release: <version>` or `chore: merge develop into master` depending on whether a version bump is happening.

Remove ticket-like prefixes (e.g., `LO-330-`) from the title since this is an open source project without ticket tracking.

Present the generated title to the user and let them edit it before proceeding.

### 5. Generate the Description

Analyze the changes that will be included in the MR:

```bash
# For branch → develop
git log develop..HEAD --oneline
git diff develop...HEAD --stat

# For develop → master
git log master..develop --oneline
git diff master...develop --stat
```

Generate a description with this structure:

```markdown
## Summary
<2-4 bullet points describing WHAT changed and WHY, derived from reading the actual code diff — not just commit messages>

## Changes
<grouped list of files changed, organized by module/area>

## Test plan
- [ ] <relevant testing steps>

---
Generated with [Claude Code](https://claude.ai/code)
```

The summary should go beyond commit messages — read the actual diff to understand the intent and describe it clearly. This is the main value-add over just listing commits.

### 6. Ask About Squash

Ask the user: "Squash commits into a single commit? (y/n)"

- If yes, add the `--squash` flag (GitHub) or note that squash will happen on merge (GitLab)
- Default suggestion: squash for feature branches, no squash for develop → master

### 7. Create the MR/PR

Always create as **draft**.

```bash
# GitHub
gh pr create \
  --base develop \
  --title "feat: the title" \
  --body "$(cat <<'EOF'
<generated body>
EOF
)" \
  --draft

# GitLab
glab mr create \
  --target-branch develop \
  --title "feat: the title" \
  --description "$(cat <<'EOF'
<generated body>
EOF
)" \
  --draft
```

### 8. Post-Creation

After successful creation:

1. Display the MR/PR URL to the user
2. For **branch → develop**: Remind that the branch will be deleted after merge (set auto-delete if supported):
   ```bash
   # GitHub: repository setting, or mention manual deletion
   # GitLab: --remove-source-branch flag
   ```
3. For **develop → master**: Ask about version bumping:
   - "Do you want to bump the version before merging? Options: (1) Run version.sh, (2) Skip, (3) I'll handle it manually"
   - If option 1: look for `version.sh` in the repo root or `src/` directory and tell the user to run it (since it's interactive and needs user input for the version number)

### 9. Edge Cases

- **No commits ahead of target**: Warn the user and abort — nothing to merge.
- **Conflicts detected**: Run `git merge --no-commit --no-ff <target>` to check, then abort. If conflicts exist, warn the user and list conflicting files.
- **Multiple remotes**: If more than one remote exists, ask which one to use. Default to `origin`.

## Platform-Specific Notes

### GitHub (`gh`)
- PRs use `--draft` flag
- Squash is a merge option, not a PR creation option — mention it in the description
- Branch deletion after merge: `gh pr merge --delete-branch` when the time comes

### GitLab (`glab`)
- MRs use `--draft` flag
- Squash: `--squash` flag on MR creation
- Branch deletion: `--remove-source-branch` flag on MR creation
