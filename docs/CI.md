# Release pipeline

The CI/CD pipeline lives in [`.github/workflows/maven.yml`](../.github/workflows/maven.yml).

## Stages

```
build -> bundle-installer -> deploy -> publish-installer -> update-docs
                                                             (manual gate)
```

| Stage | Runs on | What it does |
| --- | --- | --- |
| `build` | every push to `master` / `develop` | Compiles the reactor, resolves the version, decides whether this is a release build |
| `bundle-installer` | every push | Runs Install4j, verifies all four installers exist, uploads them as a run artifact |
| `deploy` | release only | `mvn deploy` to GitHub Packages |
| `publish-installer` | release only | Attaches the four installers to the `v<version>` release and publishes it |
| `update-docs` | release only, **after manual approval** | Publishes the website and the `lince-version.json` that triggers the in-app update notice |

A **release build** is a non-SNAPSHOT version on `master`. Everything else stops
after `bundle-installer`, which is what gives `develop` its downloadable
installer artifacts.

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
control to `update-docs`. The site content still comes from `docs/`; only the
trigger changes.

### 2. Create the `docs-release` environment with required reviewers

> Settings -> Environments -> New environment -> `docs-release`
> -> Required reviewers -> add the maintainers who may approve a docs publish

Without required reviewers the environment exists but never pauses, so the
"manual" stage would run automatically.

## Releasing

1. Merge the release branch into `master` with a non-SNAPSHOT version.
2. Watch `build` -> `bundle-installer` -> `deploy` -> `publish-installer`.
3. Verify the published release and download an installer.
4. Approve the `update-docs` job. The site and `lince-version.json` go live.

If something looks wrong at step 3, simply do not approve. Nothing user-facing
has been announced yet, and the release can be deleted and rebuilt.

## Notes

- `concurrency` is keyed per branch with `cancel-in-progress: false`, so a
  second push cannot cancel a release midway and leave assets half-published.
- Installers are built **once** in `bundle-installer` and passed forward as an
  artifact, so the binaries that get verified are the ones that get published.
- `update-docs` fails if `lince-version.json` disagrees with the released
  version. Update it as part of the release commit (the `release-notes` skill
  does this).
