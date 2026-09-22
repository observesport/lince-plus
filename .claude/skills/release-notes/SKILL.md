---
name: release-notes
description: Generates and publishes release notes for Lince PLUS. Analyzes git commits between the last published version and the latest tag, drafts user-facing notes, and updates lince-version.json and the website release data in site/src/data/releases.json.
allowed-tools: Bash(git log:*), Bash(git tag:*), Bash(git diff:*), Bash(git show:*), Bash(date:*), Bash(node -e:*), Bash(npm ci:*), Bash(npm run build:*), Read, Edit, Write, AskUserQuestion
---

# Release Notes Generator for Lince PLUS

Generate user-facing release notes by analyzing git history, then update `lince-version.json` and the website's release data `site/src/data/releases.json`.

The public website (https://observesport.github.io/lince-plus/) is an Astro project under `site/`, served in English, Spanish, German and Catalan. It reads the current version and release link from `lince-version.json` and renders the changelog page and the "latest release" panel from `site/src/data/releases.json`. Release notes are English only on every locale; the Spanish home page shows the Spanish `message` strings from `lince-version.json` instead. There is no `docs/README.md` any more; do not recreate it.

Where things live:

| What | File |
| --- | --- |
| Current version, release link, Spanish highlights (polled by the desktop app) | `lince-version.json` |
| Release notes shown on `/changelog` and in the latest-release panel | `site/src/data/releases.json` |
| Feature cards on the home page (one string per locale) | `site/src/data/features.json` |
| UI strings, including the hero copy and the player capabilities | `site/src/i18n/ui.ts` |

The CI workflow refuses to publish a release unless `lince-version.json` matches it, and the Site workflow refuses to publish the site unless that version is a published release with all four installers (see `docs/CI.md`). The site can be republished on its own from `master` without a release.

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
5. Also read the first entry of `site/src/data/releases.json`: its `version` is the newest release the website already knows about. Normally it equals the `lince-version.json` version.

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
- **v4.0.5–4.0.7**: Reporting page (PDF study reports with video thumbnails); AI Studio pose estimation exports calculated joint angles for biomechanical analysis; pause-on-selection toggle.
- **v4.1.0**: T-Pattern Explorer (repeating temporal patterns with configurable minimum occurrences, significance and granularity; clicking an occurrence seeks the video); predictive assistant suggesting the next likely events with timing and confidence; quantitative view overlaying sensor streams (heart rate, accelerometer, gyroscope) on the video timeline; data import module; refreshed dashboard views.
- The built-in video player (AI Studio) offers synchronised multi-video playback, frame stepping and speed control, drawing tools, selectable pose models (MoveNet, TensorFlow), a joint-angle panel with confidence, a 3D pose view, angle charts, snapshots and video/drawing/pose modes. Mention new player capabilities as such.
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
- English for `site/src/data/releases.json`, Spanish for `lince-version.json` messages
- No Markdown inside `releases.json` strings (they are rendered as plain text); write file names such as .vvt or /opt/lince-plus without backticks

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

#### `site/src/data/releases.json` — one new entry at the top of the array

```json
{
  "version": "<NEW_VERSION without v prefix>",
  "date": "<YYYY-MM-DD, today: date +%Y-%m-%d>",
  "tag": "v<NEW_VERSION>",
  "summary": "<one or two sentences describing the release, shown in the latest-release panel and at the top of the changelog entry>",
  "notes": [
    "<English bullet 1>",
    "<English bullet 2>"
  ]
}
```

- Insert it as the **first** element; the array is newest-first and the website reads `releases[0]` for the latest release panel when it matches `lince-version.json`.
- `date` is ISO (`YYYY-MM-DD`); the site formats it.
- `summary` is optional but recommended for feature releases; omit it for pure maintenance releases.
- `notes` are the categorized bullets from Step 3 flattened into one list, new features first, then improvements, fixes and technical updates. Keep the category order but do not add category headings.
- Keep the existing 2-space indentation and trailing newline of the file.

#### Headline features (optional)

If the release adds a capability that deserves a card on the home page, propose an update to `site/src/data/features.json` (six cards; replace or reword one rather than adding a seventh). Every card carries `title` and `body` in `en`, `es`, `de` and `ca`; supply all four. Do the same for `site/src/i18n/ui.ts` if the hero copy or the player capability list (`hero.cap1`..`cap8`) should mention it. Show these in the preview as separate, optional changes.

### Step 5: Preview and confirm

Show the user a complete preview of ALL changes that will be made:

```
## Release Notes Preview: vOLD → vNEW

### Categorized Changes
[Show the full categorized release notes from Step 3]

### File Changes

#### lince-version.json
[Show the complete new JSON content]

#### site/src/data/releases.json — new first entry
[Show the JSON object being inserted]

#### Optional: site/src/data/features.json / site/src/i18n/ui.ts
[Show proposed card or copy changes in the four languages, or "none"]
```

Then ask the user for confirmation using AskUserQuestion:
- "Apply these release notes?" with options: "Yes, apply all changes" / "Edit first"
- If the user wants edits, incorporate their feedback and show the preview again.

**Do NOT proceed to Step 6 without explicit user approval.**

### Step 6: Apply changes

After user approval:

1. Write the updated `lince-version.json` using the Write tool
2. Insert the new entry at the top of `site/src/data/releases.json` using the Edit tool (match on the opening `[` and the first existing entry's `"version"` line)
3. Apply any approved optional changes to `features.json` or `ui.ts`
4. Validate the JSON parses: `node -e "JSON.parse(require('fs').readFileSync('site/src/data/releases.json','utf8'))"`
5. Build the site to be sure it still renders: `cd site && npm ci && npm run build` (the Site workflow's `build` job runs the same on push)
6. Confirm the changes were applied successfully

Then ask the user if they want to proceed with the next steps using AskUserQuestion:
- "Run next steps?" with options: "Commit changes" / "Commit + create GitHub release draft" / "I'll handle it manually"

**If committing:**
1. Stage the files: `git add lince-version.json site/src/data/releases.json` (plus `site/src/data/features.json` or `site/src/i18n/ui.ts` if changed)
2. Commit with message: `docs: update release notes and version to <NEW_VERSION>`
   - Include the `Co-Authored-By: Claude <model> <noreply@anthropic.com>` line for the model in use in the commit body
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