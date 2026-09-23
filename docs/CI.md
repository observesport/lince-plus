# CI/CD

Two workflows under `.github/workflows/`:

| Workflow | File | What it covers |
| --- | --- | --- |
| **CI** | `ci.yml` | The Java desktop app: compile, test, installers, GitHub Packages (snapshots and releases), GitHub release |
| **Site** | `site.yml` | The website under `site/`: build, check, optional preview, GitHub Pages |

They are independent. A change that only touches the website never runs the
Maven build, and the website can be published without cutting a release.

## CI: `compile` -> `test` / `build` -> `snapshot` / `deploy`

```
compile -> test  -> deploy -> (dispatches Site: publish)
        -> build ->
        -> test  -> snapshot        manual: "Run workflow" on develop, `snapshot` ticked
```

| Job | What it does |
| --- | --- |
| `compile` | Compiles the reactor, resolves the version, tiers the run |
| `test` | Unit suite, JUnit report as a check. Gates `deploy` |
| `build` | Install4j installers, verified to exist. Runs alongside `test` |
| `snapshot` | Manual. `mvn deploy` of the SNAPSHOT libraries to GitHub Packages. Waits for `test`; `build` is skipped on that run |
| `deploy` | Checks `lince-version.json`, `mvn deploy` to GitHub Packages, publishes the `v<version>` release with the four installers, then dispatches the Site workflow |

How far a run goes:

| Trigger | Jobs |
| --- | --- |
| Feature branch push, or any PR | `compile` -> `test` |
| `develop` | ... + `build` (installers verified, **not** uploaded) |
| `develop`, "Run workflow" with `snapshot` ticked | `compile` -> `test` -> `snapshot` (SNAPSHOT jars to GitHub Packages, no installers) |
| `master`, non-SNAPSHOT | ... + `deploy` |

A **release build** is a non-SNAPSHOT version on `master`. A SNAPSHOT on
`master` stops after `test`/`build`; that is the guard that keeps an
in-progress development version from publishing. On a pull request
`GITHUB_REF` is `refs/pull/N/merge`, so a PR never gets past `test`, even when
it targets `master` with a release version.

### Snapshots from `develop`

Snapshots are deployed on demand, not on every push, to save runner minutes.
Open Actions -> CI -> "Run workflow", pick `develop`, tick `snapshot` and run,
or from a terminal:

```
gh workflow run ci.yml --ref develop -f snapshot=true
```

That run does `compile` -> `test` -> `snapshot` and skips the installer
`build`. It deploys the parent pom, `lince-data`, `lince-data-fx` (with its
test-jar), `lince-ai`, `lince-math` and `lince-transcoding` to GitHub Packages.
`-Plocal` keeps Install4j out of the `install` phase, and `lince-desktop` is
left out because its Spring Boot fat jar is about 400 MB and has no consumer;
drop `-pl '!lince-desktop'` from the job if that changes.

The job refuses to run from any branch other than `develop` or with a
non-SNAPSHOT version, so a release version can never be overwritten this way. Each deploy adds a new timestamped file set
under the same `-SNAPSHOT` version, and the version's `maven-metadata.xml`
points at the latest one.

To consume a snapshot (for example `lince-data` from lince-server), enable
snapshots on the `github` repository and refresh with `-U`:

```xml
<repository>
    <id>github</id>
    <url>https://maven.pkg.github.com/observesport/lince-plus</url>
    <snapshots>
        <enabled>true</enabled>
    </snapshots>
</repository>
```

```
mvn -U ...
```

CI ignores pushes that only touch `site/`, `docs/`, Markdown files or
`.claude/`, so website and documentation edits do not pay for a Maven build.
Pushing a release commit (which changes `pom.xml`) always runs it.

`deploy` refuses to publish if `lince-version.json` does not match the version
being released. The desktop app polls that file every 20 minutes
(`SCHEDULE_UPDATE_CHECK_WINDOW` in `ServerAppParams`) to advertise updates, so
it has to agree with the release. The `release-notes` skill updates it together
with `site/src/data/releases.json`.

## Site: `build` -> `check` -> `publish`

```
build -> check   -> publish        master only: push, or "Run workflow"
      -> preview -> teardown       PRs / manual run, only with SURGE_TOKEN
```

| Job | What it does |
| --- | --- |
| `build` | `npm ci && npm run build`, uploads `site/dist` as the Pages artifact |
| `check` | Serves that artifact on the runner under `/lince-plus` and runs `site/scripts/smoke.sh` against it. No extra tokens |
| `preview` | Optional. Deploys the site to a temporary surge.sh URL and smoke-tests it there |
| `teardown` | Removes the surge.sh preview once it was checked |
| `publish` | Verifies the advertised release exists, then deploys the checked artifact to GitHub Pages |

Runs on pushes and PRs that change `site/`, `lince-version.json` or the
workflow itself, and on manual runs.

### Publishing the site without a release

Any of these publishes the current `master`:

- Push a website change to `master` (for example merge a site PR).
- Actions -> **Site** -> **Run workflow** on `master` (`publish` is ticked by default).
- `gh workflow run site.yml --ref master`.

`publish` never needs the Java build. It does check that the version in
`lince-version.json` is a **published** release with all four installers
attached, and fails otherwise. That is what keeps the site from advertising a
download that does not exist.

### Publishing after a release

The release commit usually changes `lince-version.json`, which triggers the
Site workflow at the same time as CI. Its `publish` job will fail if it runs
before the release is out (the installers do not exist yet). That is fine:
when CI's `deploy` finishes publishing the release it dispatches the Site
workflow on `master` again, and that run publishes the site.

To avoid the early failed run altogether, give the `docs-release` environment
a required reviewer (see below): the first run then waits for approval, and
you approve it after the release is live.

### Why the site publish is guarded

On the 4.1.0 release, GitHub's automatic Pages build and the Maven workflow
both fired on the same push at `07:55:30Z`. Pages finished in about a minute,
while the release was not published until `08:08:07Z`, so the site advertised
4.1.0 about twelve minutes before the installers existed.

`publish` therefore re-checks, via the API, that the release exists, is not a
draft, and carries all four installers before deploying. Optionally it also
waits for a reviewer.

## One-time repository setup

### 1. Pages source = GitHub Actions

**This is the setting that caused the 4.1.0 incident.** While the automatic
Pages build is on, GitHub rebuilds and deploys the site on every push to
`master`, bypassing the workflow.

> Settings -> Pages -> Build and deployment -> Source: **GitHub Actions**

### 2. Optional: `docs-release` environment with required reviewers

> Settings -> Environments -> New environment -> `docs-release`
> -> Required reviewers -> the maintainers who may approve a site publish

Without reviewers the environment never pauses and `publish` runs on its own,
guarded only by the release check.

### 3. Optional: `SURGE_TOKEN` for live previews

`preview` publishes the website to `https://lince-plus-pr<N>.surge.sh` (or
`lince-plus-<branch>.surge.sh` on a manual run), runs the smoke test against
the live URL, and `teardown` removes it again. The URL shows up as the
`site-preview` deployment on the PR. It does nothing until the secret exists:

> Settings -> Secrets and variables -> Actions -> New repository secret
> -> `SURGE_TOKEN` = output of `npx surge token`

| Trigger | Behaviour |
| --- | --- |
| Pull request | Deploy, smoke-test, tear down. Automatic. |
| Manual run, `preview` ticked | Same, for the selected branch. |
| Manual run, `preview` + `keep_preview` ticked | Deploy and smoke-test, keep the site up. Remove it later with `npx surge teardown <domain>`. |

Teardown runs whenever a deploy happened, even if the smoke test failed. To
add a human sign-off, create the `site-preview-teardown` environment with
required reviewers: the teardown then waits, and the preview stays reachable
meanwhile.

### Branch protection

If you make `Compile`/`Test` or `Build`/`Check` required status checks, note
that both workflows use path filters. A PR that does not touch the matching
paths never reports those checks, and GitHub keeps waiting for them. Either
require only the checks of the workflow that always runs for that kind of
change, or drop the path filters.

## Checking a site build without any token

`check` downloads the `github-pages` artifact that `build` uploaded, unpacks it
under `/lince-plus`, serves it with `python3 -m http.server`, runs
`site/scripts/smoke.sh` and stops the server. It tests the exact bytes
`publish` would deploy, with only the default `GITHUB_TOKEN`.

To look at a PR's site yourself, download the `github-pages` artifact from the
run and serve it the same way (the job summary prints the two commands).

## Releasing

1. Run the `release-notes` skill so `lince-version.json` and
   `site/src/data/releases.json` are part of the release commit.
2. Merge into `master` with a non-SNAPSHOT version.
3. Watch CI: `compile` -> `test` / `build` -> `deploy`.
4. `deploy` publishes the release and dispatches Site. Approve the
   `docs-release` environment if it has reviewers. The site goes live.

If the release looks wrong, do not approve the site publish (or re-run it
later): nothing on the website has changed yet.

## Notes

- `concurrency` never cancels a CI run on `master` or `develop`, so a second
  push cannot cancel a release midway. On feature branches superseded runs are
  cancelled, which also collapses the duplicate `push` + `pull_request` run of
  a PR branch. Site runs on `master` are never cancelled either, and Pages
  deployments are serialised through the `site-pages` group.
- Installers are built once in `build` and passed to `deploy` as an artifact,
  so the binaries that get verified are the ones that get published.
- The Site workflow triggers `publish` only from `master`. A manual run on any
  other branch builds, checks and (with `preview`) previews.
