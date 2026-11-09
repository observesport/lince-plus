# LO-350 · lince-plus-public cut — handover

Written 2026-11-09 for a fresh session opened in this worktree
(`/Users/berto/src/lince/lince-plus-public/.claude/worktrees/LO-350-remove-renjin`,
branch `feat/LO-350-remove-renjin`, based on develop `9239e2d0`, no drift from `origin/develop`).

Jira: https://lince-observer.atlassian.net/browse/LO-350 (ticket text never read: scope comes from the owner's chat description).

## Where we are

LO-350 removes the Renjin R engine in three cuts.

1. **Dashboard — DONE.** MR !48 merged 2026-11-09 into `lince-plus-dashboard-hub` develop (`c22b7451`). The R Studio page is replaced by **Software integrations** (`/integrations`, desktop-only via `VITE_ENABLE_INTEGRATIONS && isDesktopMode`): R / Python / HTTP snippets to pull registers from the desktop REST API, chart them and optionally compare two observers against `/register/kappa`. Design, spec and task docs: dashboard repo `.plans/LO-350-remove-renjin/`.
2. **lince-plus-public — THIS CUT, applied but uncommitted.** Everything below sits in this worktree's index / working tree. Nothing is committed on the branch yet (`git log` shows only develop).
3. **lince-server — TODO** (separate repo): delete `lince-api/src/main/java/com/lince/api/controller/rest/observer/RenjinControllerImpl.java` (imports lince-math `RenjinService`, breaks on the next lince-math bump), the renjin deps around `pom.xml:273-284`, and refresh the static bundle (it still ships the old `RStudio-*.js` chunk).

## What is staged / changed here (all confirmed by the owner on 2026-11-09)

- **Code**: `src/lince-data/.../controller/RenjinController.java`, `src/lince-desktop/.../controller/rest/RenjinControllerImpl.java`, `src/lince-math/.../RenjinDataAttribute.java`, `src/lince-math/.../service/RenjinService.java` deleted.
- **R scripts**: `src/lince-desktop/src/main/resources/integration/r-lang/` (all `.r`/`.R`/`.pdf`) deleted.
- **Maven**: `src/lince-math/pom.xml` loses the three renjin repositories and every renjin dependency (125 lines); `src/pom.xml` loses the renjin repositories, the renjin `dependencyManagement` entries and the commons-compress pin that only existed for them (the remaining security-pin comment now reads "Transitive security pin: fujion-core brings commons-beanutils 1.9.4 (CVE-2025-48734)"). **Keep the GitHub Packages repository** (`maven.pkg.github.com/observesport/lince-plus`, id `github`): it serves other artifacts.
- **Renjin mirror**: `src/lib/` (README_RENJIN_UPLOAD.md, dependency.txt, `org/renjin/**` jars + poms) and `src/upload_renjin_to_github_own.sh` deleted.
- **Text, all four kept removed by owner decision**: `CLAUDE.md` (lince-math line now "Mathematical calculations (profiles, analysis, stats services)"; the "Statistics: Renjin", "Renjin for R integration" and "R package integration…" bullets gone), `site/src/pages/privacy-desktop.md:85` (bundled components sentence without Renjin), `src/lince-math/leeme.txt` (five Renjin-link lines), `src/lince-math/doc/links-r-packages.md` (deleted).
- **Desktop bundle**: `src/lince-desktop/src/main/resources/public/` rebuilt from the dashboard's merged develop (`c22b7451`) with `DESKTOP_TARGET=<this worktree>/src/lince-desktop/src/main/resources/public bash scripts/build-desktop.sh` from the dashboard repo. 559 old assets deleted, 45 new (incl. `assets/Integrations-6vU0tT1q.js`, main chunk `index-D20h2lUZ.js` with `VITE_ENABLE_INTEGRATIONS:"true"`, `isDesktopMode=!0`), no `renjin|rstudio|r-studio` string left. `deprecated`, `desktop`, `media` folders and keycloak files preserved by the script.

Verification already done on the removal state (before the bundle refresh): `mvn -o compile` and `mvn -o test` (270 tests) BUILD SUCCESS. `rg -i renjin src --glob '!**/resources/public/**'` is empty.

## Next steps

1. `git status` to re-read the above; `rg -i "renjin|rstudio|r-studio" . --glob '!.git' --glob '!**/target/**'` must only hit this handover.
2. Re-run `mvn -o -f src/pom.xml compile` and `mvn -o -f src/pom.xml test` (offline worked; drop `-o` if a plugin is missing). Expect 270 tests green.
3. Optional smoke: package the jar from this worktree (`mvn -f src/lince-desktop/pom.xml -DskipTests package`), run it, open the dashboard it serves: the sidebar must end with Import/Export then **Integrations**, and `/integrations` must list the current project's observers.
4. Commit in two steps (conventional commits with the trailers from the user CLAUDE.md), then push and open the PR against `develop` with `gh` (`/lince:gen-mr` auto-detects GitHub from the remote):
   - `refactor(LO-350): remove the Renjin R engine (service, controllers, R scripts, Maven deps and the local mirror)` — everything except `resources/public`.
   - `chore(LO-350): refresh the embedded dashboard bundle (Software integrations page replaces R Studio)` — `resources/public` only.
   Suggested trailers: `Constraint: GitHub Packages repo stays (other artifacts)`, `Rejected: keep the Renjin Nexus mirror in src/lib | nothing depends on it any more`, `Not-tested: installer build (Install4J) with the new bundle`.
5. After merge: start cut 3 in lince-server (see above), then close the `lo350-remove-renjin` work state and the dashboard-repo memory note `lo350-remove-renjin.md`.

## Traps

- **Never commit in the main lince-plus-public checkout** (`/Users/berto/src/lince/lince-plus-public`): it has an unmerged `.gitignore` (`UU`) and a staged deletion of `docs/Lince_1.4.exe`. Work only in this worktree.
- The dashboard build must come from `scripts/build-desktop.sh` with `DESKTOP_TARGET` set, never a bare `nx build`; the default target is the main checkout's `public` folder.
- `mvn` is run from `src/` (`src/pom.xml` is the aggregator, 8 modules, 4.2.1-SNAPSHOT).
- Ponytail mode was active in the previous session (lazy senior dev: smallest complete change, end replies with what was skipped and the risks).
