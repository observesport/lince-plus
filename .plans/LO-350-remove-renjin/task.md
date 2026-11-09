# Tasks: LO-350 — Renjin removal, through to the CI/CD pipelines

## Phase 1 — Land cut 2 and publish a Renjin-free lince-math
- [ ] Merge PR #180 (lince-plus-public, `feat/LO-350-remove-renjin` -> develop); confirm the
      `build` job (Install4J) passes without the renjin jars (AC-1)
- [ ] Publish `lince-math` without Renjin: `gh workflow run ci.yml --ref develop -f snapshot=true`
      (or cut release `4.2.1`); check the version appears under
      https://github.com/observesport/lince-plus/packages (AC-2)

## Phase 2 — lince-server cut 3 (repo `/Users/berto/src/lince/lince-server`, GitLab)
- [ ] Branch from develop: `git checkout -b feat/LO-350-remove-renjin`
- [ ] Delete `lince-api/src/main/java/com/lince/api/controller/rest/observer/RenjinControllerImpl.java`
- [ ] `pom.xml`: remove the `bedatadriven` repository block (~L28-33) and the
      `org.renjin:renjin-script-engine` + `org.renjin.cran:jsonlite` managed deps (~L273-284);
      set `linceDesktopVersion` (L47) to the version from Phase 1
- [ ] `.m2/settings.xml`: remove the `bedatadriven` repository (~L30-32); keep `github`
- [ ] `Dockerfile`: drop the stale "Custom settings to package renjin properly" comment (L21)
- [ ] Rebuild the static bundle from dashboard develop `c22b7451` with the dashboard's server
      build script, target `lince-api/src/main/resources/static`; verify
      `find lince-api/src/main/resources/static -iname "*RStudio*"` is empty and the main chunk
      has no `VITE_ENABLE_INTEGRATIONS:"true"` (AC-5)
- [ ] `rg -i "renjin|bedatadriven" . --glob '!.git' --glob '!**/target/**' --glob '!**/node_modules/**' --glob '!**/static/jupyterlite/**'`
      returns nothing (AC-3)
- [ ] `mvn -B package -DskipTests` then `mvn -B test` locally with the GitHub token in settings

## Phase 3 — Pipelines
- [ ] Open the GitLab MR to develop (`/lince:gen-mr` detects GitLab -> `glab`); squash, delete
      source branch
- [ ] GitLab pipeline `build` -> `test` -> `deploy` green; if `lince-math` resolves a stale
      cached snapshot, clear the job cache once and rerun (AC-4)
- [ ] Optional, one line: add `rg -i renjin pom.xml lince-api/pom.xml && exit 1 || true` style
      guard to `.gitlab-ci.yml` `build` job. Skip unless the owner wants it.

## Phase 4 — Cleanup & Review
- [ ] Update `.plans/LO-350-remove-renjin/handover.md` "Where we are" to mark cuts 2 and 3 done
- [ ] Close work state `lo350-remove-renjin`; update memory `lince-lo-350-remove-renjin.md`
      and the dashboard-repo note (AC-7); note there that the 35 Renjin packages stay on
      GitHub Packages on purpose
- [ ] Self-review both MRs against spec.md acceptance criteria
