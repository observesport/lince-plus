# Plan: LO-350 — Renjin removal, through to the CI/CD pipelines

Jira: https://lince-observer.atlassian.net/browse/LO-350
Companion: `handover.md` (state of the lince-plus-public cut), PR #180.

## Overview

Cuts 1 (dashboard, MR !48) and 2 (lince-plus-public, PR #180) remove the Renjin R engine
from the code. This plan covers what is left so the removal holds everywhere the artifacts
still flow: the lince-server repo and the GitLab pipeline and Docker image that build it.
The 35 Renjin/CRAN mirror packages that the deleted `upload_renjin_to_github_own.sh` pushed
to GitHub Packages stay as an archive; from now on nothing declares or resolves them.

## Goals

- No Renjin dependency, controller, repository or mirror remains in lince-plus-public,
  lince-server, their Maven settings, Dockerfile or pipeline files.
- lince-server builds against a Renjin-free `lince-math`.
- No pipeline, pom or Maven settings file resolves Renjin any more: the `bedatadriven`
  repository and the Renjin dependencies disappear from lince-server too.
- The dashboard bundle served by lince-server no longer ships the `RStudio-*.js` chunk.

## Non-Goals

- Deleting the 35 `org.renjin*` / `org.renjin.cran.*` packages from GitHub Packages
  (observesport). Owner decision 2026-11-09: they stay as an archive so an old tag can still
  be rebuilt; nothing from now on declares them.
- A CI guard that fails when `renjin` reappears. Nothing brings it back by accident; if
  wanted later it is one `rg -i renjin` line in `ci.yml` (listed as optional in task.md).
- Removing the GitHub Packages repository from the poms. It still serves the 8
  `com.lince.observer.*` modules that lince-server consumes and that CI deploys.
- Re-auditing the Java 17 dependency ceiling now that Renjin is gone (separate ticket).
- Changing jupyterlite or anything else in lince-server's static folder beyond the
  dashboard bundle refresh.

## Technical Approach

Order matters because lince-server today pins `linceDesktopVersion=4.1.1-SNAPSHOT`, whose
`lince-math` pom still declares the Renjin dependencies (resolved from the GitHub Packages
archive; the bedatadriven Nexus is effectively dead). Its bump needs a Renjin-free
`lince-math` published first. So:

1. **Merge PR #180** into lince-plus-public develop.
2. **Publish a Renjin-free lince-math**: manual snapshot run on develop
   (`gh workflow run ci.yml --ref develop -f snapshot=true`) deploys `4.2.1-SNAPSHOT`
   library jars to GitHub Packages. A release (`4.2.1`) works too; use whichever comes first.
3. **lince-server cut 3** (GitLab repo `lince-saas/lince-server`, branch from develop):
   - delete `lince-api/src/main/java/com/lince/api/controller/rest/observer/RenjinControllerImpl.java`
   - `pom.xml`: drop the `bedatadriven` repository (lines ~28-33) and the two Renjin
     `dependencyManagement` entries (lines ~273-284); set `linceDesktopVersion` to the
     version published in step 2
   - `.m2/settings.xml`: drop the `bedatadriven` mirror repository (lines ~30-32); keep
     `github` (own modules)
   - `Dockerfile`: drop the stale "Custom settings to package renjin properly" comment
   - static bundle: rebuild from dashboard develop `c22b7451` with the dashboard's server
     build script, pointed at `lince-api/src/main/resources/static`; the `RStudio-*.js`
     chunk disappears, Integrations stays desktop-only so the server sidebar just loses R Studio
   - `mvn -B package -DskipTests` locally, then the GitLab `build` -> `test` -> `deploy`
     pipeline must pass with the GitHub token-substituted settings (`.gitlab-ci.yml:37-38`)
4. Close the work state and memory notes (`lo350-remove-renjin`, dashboard repo note,
   `lince-lo-350-remove-renjin.md` here).

lince-plus-public itself needs nothing further: `ci.yml`, `docs/CI.md`, the install4j
project files, launcher and Dependabot config carry no Renjin reference, and the
`settings.xml` wiring in `ci.yml` stays for the own-module snapshots.

## Dependencies

- PR #180 merged (lince-plus-public).
- A published Renjin-free `lince-math` on GitHub Packages (snapshot run or release).
- Dashboard repo `lince-plus-dashboard-hub` at develop `c22b7451` for the server bundle.
- GitLab access to `lince-saas/lince-server` with a `GITHUB_TOKEN` CI variable that can
  read GitHub Packages.

## Risks & Mitigations

| Risk | Impact | Mitigation |
|------|--------|------------|
| lince-server bumped to a Renjin-free lince-math before one is published | High: server pipeline cannot resolve `lince-math` | Step 2 before step 3; check the version on the Packages page |
| Snapshot of lince-math never published, server bump has nothing to point at | Med | Run the manual `snapshot` workflow right after #180 merges, or cut `4.2.1` |
| Server bundle rebuilt with the desktop script/target by mistake | Med: desktop-only flags leak into the server UI | Use the server build script and check `VITE_ENABLE_INTEGRATIONS` is absent/false in the main chunk |
| Install4J build without the renjin jars untested until CI runs #180 | Low | Watch the `build` job on PR #180; nothing in the install4j project references them |

## Open Questions

- Snapshot (`4.2.1-SNAPSHOT`) or release (`4.2.1`) for the lince-server bump? Snapshot is
  faster; release matches how production should pin. Owner's call at step 2.
- Does lince-server still expose any `/renjin` route in its API docs or the dashboard's
  server menu that needs a 410 instead of a 404? Checked the code: only
  `RenjinControllerImpl`, so plain deletion is proposed.
