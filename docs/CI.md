# Release pipeline

The CI/CD pipeline lives in [`.github/workflows/maven.yml`](../.github/workflows/maven.yml).

## Stages

```
build -> test -> bundle-installer -> deploy -> publish-installer -> update-docs
                                                                     (manual gate)
```

| Stage | What it does |
| --- | --- |
| `build` | Compiles the reactor, resolves the version, tiers the run |
| `test` | Runs the unit suite, publishes a JUnit report as a check |
| `bundle-installer` | Runs Install4j and verifies all four installers exist |
| `deploy` | `mvn deploy` to GitHub Packages |
| `publish-installer` | Attaches the four installers to the `v<version>` release and publishes it |
| `build-site` | Builds the Astro website under `site/` and uploads it as the Pages artifact (every branch, independent of Maven) |
| `preview-site` | Optional. Deploys the website to a temporary surge.sh URL and smoke-tests it there (PRs, or manual runs with `preview`) |
| `preview-teardown` | Removes the surge.sh preview once validation is done |
| `update-docs` | Deploys the website built by `build-site`; `lince-version.json` on `master` is what triggers the in-app update notice |

## How far a run goes

The pipeline is tiered by where it runs, so branches only pay for what they need:

| Trigger | Stages |
| --- | --- |
| Feature branch push, or any PR | `build` -> `test`, plus `build-site` |
| `develop` | ... + `bundle-installer` (built and verified, **not** uploaded or published) |
| `master`, non-SNAPSHOT | ... + `deploy` -> `publish-installer` -> `update-docs` |

A **release build** is a non-SNAPSHOT version on `master`. A SNAPSHOT on
`master` deliberately stops after `test` — that is the guard that keeps an
in-progress development version from publishing.

On a pull request `GITHUB_REF` is `refs/pull/N/merge`, so neither tier flag is
ever set for a PR. A PR only ever gets `build` + `test`, even when it targets
`master` with a release version.

`test` gates everything downstream: no installer is ever bundled from code with
failing tests. When tests fail, the surefire reports are uploaded as an
artifact for debugging.

On `develop` the installers are built and verified but deliberately not
uploaded — the run proves they can still be produced, and nothing downstream
consumes them.

## Why docs are last, and manual

`lince-version.json` is polled by the desktop app every 20 minutes
(`SCHEDULE_UPDATE_CHECK_WINDOW` in `ServerAppParams`) to advertise available
updates. If it announces a version whose installers do not exist yet, users are
told to download something that isn't there.

That is exactly what happened on the 4.1.0 release: GitHub's automatic Pages
build and the Maven workflow both fired on the same push at `07:55:30Z`. Pages
finished in ~1m14s, while the release was not published until `08:08:07Z` —
so the site advertised 4.1.0 about **twelve minutes before** the installers
existed.

`update-docs` therefore sits behind two independent gates:

1. **Manual approval** — the `docs-release` environment requires a reviewer.
2. **Automated verification** — after approval it re-checks, via the API, that
   the release exists, is not a draft, and carries all four installers. It also
   checks that `lince-version.json` matches the version being released. Any
   failure aborts before the docs are touched.

Gate 2 matters because approval alone cannot see whether the release actually
succeeded. If the installers are missing, the job fails and the site keeps
advertising the previous version.

## One-time repository setup

Two settings must be applied by a repo admin, or the gates do nothing.

### 1. Disable the automatic Pages build

**This is the setting that caused the 4.1.0 incident.** While it is on, GitHub
rebuilds and deploys the site on every push to `master`, completely bypassing
this workflow.

> Settings -> Pages -> Build and deployment -> Source: **GitHub Actions**

Switching the source from "Deploy from a branch" to "GitHub Actions" hands
control to `update-docs`. The site is the Astro project under `site/` (see
`site/README.md`); `docs/` no longer holds a Jekyll site.

### 2. Create the `docs-release` environment with required reviewers

> Settings -> Environments -> New environment -> `docs-release`
> -> Required reviewers -> add the maintainers who may approve a docs publish

Without required reviewers the environment exists but never pauses, so the
"manual" stage would run automatically.

## Website previews on surge.sh (optional)

`preview-site` publishes the website to `https://lince-plus-pr<N>.surge.sh`
(or `lince-plus-<branch>.surge.sh` on a manual run), runs
`site/scripts/smoke.sh` against the live URL, and `preview-teardown` removes it
again. The URL shows up as the `site-preview` deployment on the PR.

It is opt-in and does nothing until the secret exists:

> Settings -> Secrets and variables -> Actions -> New repository secret
> -> `SURGE_TOKEN` = output of `npx surge token` for the surge.sh account that
> should own the preview domains

Runs:

| Trigger | Behaviour |
| --- | --- |
| Pull request | Deploy, smoke-test, tear down. The whole cycle is automatic. |
| Manual run, `preview` ticked | Same as above for the selected branch. |
| Manual run, `preview` + `keep_preview` ticked | Deploy and smoke-test, keep the site up. Remove it later with `npx surge teardown <domain>`. |

Teardown always runs when a deploy happened, even if the smoke test failed, so
nothing is left behind. To add a human sign-off, create the
`site-preview-teardown` environment with required reviewers: the teardown then
waits until someone approves it, and the preview stays reachable meanwhile.

## Releasing

1. Merge the release branch into `master` with a non-SNAPSHOT version.
2. Watch `build` -> `test` -> `bundle-installer` -> `deploy` -> `publish-installer`.
3. Verify the published release and download an installer.
4. Approve the `update-docs` job. The site and `lince-version.json` go live.

If something looks wrong at step 3, simply do not approve. Nothing user-facing
has been announced yet, and the release can be deleted and rebuilt.

## Notes

- `concurrency` never cancels a run on `master` or `develop`, so a second push
  cannot cancel a release midway and leave assets half-published. On feature
  branches superseded runs are cancelled, which also collapses the duplicate
  run a PR branch would otherwise get from `push` and `pull_request` both
  firing.
- Installers are built **once** in `bundle-installer` and passed forward as an
  artifact, so the binaries that get verified are the ones that get published.
- `update-docs` fails if `lince-version.json` disagrees with the released
  version. Update it as part of the release commit (the `release-notes` skill
  does this), together with the new entry in `site/src/data/releases.json`.
- `build-site` runs on every push, so a site that fails to build blocks a PR
  the same way failing tests do. It never deploys; only `update-docs` does.
