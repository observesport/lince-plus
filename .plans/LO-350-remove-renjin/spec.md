# Spec: LO-350 — Renjin removal, through to the CI/CD pipelines

## Problem Statement

The Renjin engine is gone from the dashboard and (pending PR #180) from the desktop code,
but it still lives in three places the pipelines touch: lince-server's pom, settings,
controller, Dockerfile and static bundle; the lince-server GitLab pipeline that resolves
it transitively through `lince-math 4.1.1-SNAPSHOT`; and 35 mirror packages on GitHub
Packages. Left alone, the next `lince-math` bump breaks lince-server's build. The archived
packages on GitHub Packages are kept on purpose; the support around them goes.

## Requirements

### Functional Requirements

- FR-1: lince-server has no `org.renjin*` dependency, no `bedatadriven` repository and no
  `RenjinControllerImpl`.
- FR-2: lince-server depends on a `lince-math` version whose pom declares no Renjin
  dependency (`4.2.1-SNAPSHOT` or later).
- FR-3: lince-server's static bundle contains no `RStudio-*.js` chunk and no
  `renjin|rstudio|r-studio` string outside third-party folders (`jupyterlite`).
- FR-4: No pom, `settings.xml`, Dockerfile or pipeline file in either repo names a Renjin
  repository (`bedatadriven`) or artifact. The 35 `org.renjin*` packages on GitHub Packages
  stay untouched as an archive.
- FR-5: lince-plus-public `develop` after #180: `rg -i "renjin|rstudio|r-studio"` hits only
  `site/src/data/releases.json` (historical changelog) and `.plans/`.

### Non-Functional Requirements

- NFR-1: Both pipelines (lince-plus-public `ci.yml`, lince-server `.gitlab-ci.yml`) pass
  without new secrets or repositories.
- NFR-2: lince-server is bumped only after a Renjin-free `lince-math` is published (ordering, not tooling).
- NFR-3: No new abstraction, feature flag or stub endpoint is introduced.

## Acceptance Criteria

- [ ] AC-1: PR #180 merged; lince-plus-public CI green on develop including the `build`
      (Install4J) job.
- [ ] AC-2: A Renjin-free `lince-math` is on GitHub Packages (snapshot run or release) and
      lince-server `pom.xml` `linceDesktopVersion` points at it.
- [ ] AC-3: `rg -i renjin` over lince-server (excluding `.git`, `target`, `node_modules`,
      `static/jupyterlite`) returns nothing.
- [ ] AC-4: lince-server GitLab pipeline `build` -> `test` -> `deploy` green on the MR.
- [ ] AC-5: Served server dashboard shows no R Studio entry; `find static -iname "*RStudio*"`
      is empty.
- [ ] AC-6: `rg -i "renjin|bedatadriven"` over both repos' `pom.xml`, `.m2/settings.xml`,
      `Dockerfile`, `.gitlab-ci.yml` and `.github/workflows/*.yml` returns nothing.
- [ ] AC-7: Work state `lo350-remove-renjin` and the memory notes closed/updated.

## Edge Cases

- `4.2.1-SNAPSHOT` changes under lince-server: pipeline uses `mvn -U` or a fresh cache
  (`.gitlab-ci.yml` caches `.m2/repository`); if a stale lince-math snapshot is cached, clear
  the job cache once.
- An old lince-server tag rebuilt later still resolves Renjin from the GitHub Packages archive
  (that is why the packages stay); it needs a token with `read:packages`, as today.
- The `github` repository entry stays in lince-server `.m2/settings.xml` and pom: it is what
  resolves `com.lince.observer.*`, do not remove it along with `bedatadriven`.
- jupyterlite's vendored bundle mentions "renjin" in a kernel list string: third-party, ignore.

## Out of Scope

- CI guard against Renjin reappearing (optional one-liner, not required).
- Dependency ceiling review (Java 17, dkpro) now unblocked by Renjin's removal.
- Removing GitHub Packages or its credentials from either build.
- Deleting the archived Renjin packages from GitHub Packages (owner decision: keep).
- Any lince-server feature beyond deleting the Renjin route.
