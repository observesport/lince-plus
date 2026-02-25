---
name: release-notes
description: Generates and publishes release notes for Lince PLUS. Analyzes git commits between the last published version and the latest tag, drafts user-facing notes, and updates lince-version.json and docs/README.md.
allowed-tools: Bash(git log:*), Bash(git tag:*), Bash(git diff:*), Bash(git show:*), Bash(date:*), Read, Edit, Write, AskUserQuestion
---

# Release Notes Generator for Lince PLUS

Generate user-facing release notes by analyzing git history, then update `lince-version.json` and `docs/README.md`.

## Workflow

Follow these 6 steps in order. Do NOT skip the preview/confirmation step.

### Step 1: Gather versions

1. Read `lince-version.json` from the project root. Extract the `version` field — this is the **last published version**.
2. Get the latest git tag:
   ```bash
   git tag --sort=-v:refname | head -5
   ```
3. Compare the two. The tag uses a `v` prefix (e.g., `v4.0.4`), the JSON does not (e.g., `4.0.2`).
4. If the JSON version already matches the latest tag, tell the user everything is up to date and **stop**.
5. Also read `docs/README.md` to note its current version references (the Download section and release notes may reference a different, older version).

### Step 2: Analyze changes

Run these commands to understand what changed between the last published version and the latest tag:

```bash
# Commit log (concise, no merges)
git log v<OLD>..v<NEW> --oneline --no-merges

# Full commit messages for context
git log v<OLD>..v<NEW> --no-merges --pretty=format:"%h %s%n%b"

# Diff summary
git diff v<OLD>..v<NEW> --stat
```

For significant changes, inspect specific files:

```bash
# Focused diffs on Java source (skip test files for the summary)
git diff v<OLD>..v<NEW> -- "*.java" --stat
```

**Filtering rules:**
- Ignore file renames that are just frontend asset hash changes (e.g., `main.abc123.js` → `main.def456.js`)
- Ignore changes that are purely version bumps in `pom.xml` `<version>` tags (but DO note dependency upgrades)
- Focus on behavioral changes: new features, bug fixes, configuration changes, dependency upgrades
- **Do NOT expose internal technical details** (framework names, file paths, Docker configs, build scripts) — only describe what users can see and do

**Frontend context:** The frontend lives in a separate private repository. Commits in this repo that say "update frontend" bundle features built there. To understand what user-facing frontend features were added, check the merge commit messages and PR titles (e.g., `git log v<OLD>..v<NEW> --merges --oneline`). Known frontend features by version:

- **v4.0.3–4.0.4**: New "Research" page with embedded JupyterLite Python notebook environment. Users can analyze observation data with Python (pandas, numpy, matplotlib, scipy) directly in the browser. Includes one-click code templates for data analysis, plotting, and timeline visualization. Pre-loads register data automatically into a `df` DataFrame. Supports fullscreen mode.
- *(Add future version notes here as releases are made)*

When drafting release notes, incorporate these frontend features as user-facing descriptions. Do not mention JupyterLite, Pyodide, iframe, Docker, or other implementation details — describe the capability from the researcher's perspective.

### Step 3: Categorize and draft release notes

Group changes into these categories (omit any category that has no entries):

- **New Features** — new user-facing capabilities
- **Improvements** — enhancements to existing functionality
- **Bug Fixes** — resolved issues
- **Technical Updates** — dependency upgrades, build system changes, internal refactoring

**Writing guidelines:**
- Write from the user's perspective, not the developer's
- Each bullet should be a complete, understandable sentence
- Reference GitHub issue numbers where relevant (e.g., `#90`)
- Keep it concise — aim for 3-10 bullets total
- English for `docs/README.md`, Spanish for `lince-version.json` messages

### Step 4: Prepare all payloads

Prepare the exact content for both files before showing the preview.

#### `lince-version.json`

```json
{
  "version": "<NEW_VERSION without v prefix>",
  "link": "https://github.com/observesport/lince-plus/releases/tag/v<NEW_VERSION>",
  "message": ["<1-3 Spanish language strings summarizing key changes>"]
}
```

- The `message` array contains 1-3 short strings in **Spanish** describing the most important changes
- No `v` prefix on the version string

#### `docs/README.md` — three separate edits

**Edit A: Download section** (around line 33-40)
- Update the download link URL to point to the new version tag
- Update the "Current version X.Y.Z" heading to the new version
- Replace the brief description below it with a short summary of this release

**Edit B: Previous versions list** (around line 113)
- Add the OLD current version from the Download section to the TOP of the Previous versions list
- Format: `- [X.Y.Z - Mac x64 & Windows x64 (w10)](https://github.com/observesport/lince-plus/releases/tag/vX.Y.Z)`
- The old version to add comes from what was previously in the Download section heading (read `docs/README.md` to find it — it may differ from `lince-version.json`)

**Edit C: Release notes section** (around line 162)
- Add a new entry at the TOP of the release notes list (right after the `## Release notes` heading)
- Use today's date in DD/MM/YYYY format: `date +%d/%m/%Y`
- Format:
  ```
  - Version X.Y.Z RELEASE, DD/MM/YYYY
  	- Bullet point 1
  	- Bullet point 2
  	- Bullet point 3

  ```
- Use tab indentation for sub-bullets (matching the existing style in the file)

### Step 5: Preview and confirm

Show the user a complete preview of ALL changes that will be made:

```
## Release Notes Preview: vOLD → vNEW

### Categorized Changes
[Show the full categorized release notes from Step 3]

### File Changes

#### lince-version.json
[Show the complete new JSON content]

#### docs/README.md — Download section
[Show the new download section content]

#### docs/README.md — Previous versions
[Show the line being added]

#### docs/README.md — Release notes
[Show the new release notes entry]
```

Then ask the user for confirmation using AskUserQuestion:
- "Apply these release notes?" with options: "Yes, apply all changes" / "Edit first"
- If the user wants edits, incorporate their feedback and show the preview again.

**Do NOT proceed to Step 6 without explicit user approval.**

### Step 6: Apply changes

After user approval:

1. Write the updated `lince-version.json` using the Write tool
2. Apply the three edits to `docs/README.md` using the Edit tool (one edit per section)
3. Confirm the changes were applied successfully

Then ask the user if they want to proceed with the next steps using AskUserQuestion:
- "Run next steps?" with options: "Commit changes" / "Commit + create GitHub release draft" / "I'll handle it manually"

**If committing:**
1. Stage both files: `git add lince-version.json docs/README.md`
2. Commit with message: `docs: update release notes and version to <NEW_VERSION>`
   - Include `Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>` in the commit body
3. Show the commit result

**If also creating a GitHub release draft:**
1. After committing, create a draft release using `gh`:
   ```bash
   gh release create v<NEW_VERSION> --draft --title "Lince PLUS v<NEW_VERSION>" --notes "<release notes in markdown>"
   ```
   - Use the English categorized release notes from Step 3 as the `--notes` body
   - Pass notes via a HEREDOC for proper formatting
2. Show the release URL so the user can review and publish it

**If manual:**
- Print the suggested commit message and GitHub release URL for reference