# LINCE PLUS website

Public site for LINCE PLUS, published to https://observesport.github.io/lince-plus/ by the
`update-docs` stage of `.github/workflows/maven.yml`.

Built with [Astro](https://astro.build). Static output, no client-side framework.

## Local development

```bash
cd site
npm ci
npm run dev        # http://localhost:4321/lince-plus/
npm run build      # writes dist/
npm run preview    # serves dist/ at http://localhost:4321/lince-plus/
```

Node 22.12 or later (see `.nvmrc`).

`npm run smoke -- http://localhost:4321/lince-plus` checks a running copy of the site:
every page answers, the 404 page works, home-page assets resolve, and the advertised
version matches `lince-version.json`. CI runs the same script against the surge.sh preview.

To build for a different host (the CI preview does this): `SITE_URL=https://example.test
SITE_BASE=/ npm run build`.

## Where the content lives

| What | Where |
| --- | --- |
| Current version and release link | `../lince-version.json` (root). Also polled by the desktop app. |
| Release notes shown on `/changelog` and in the release panel | `src/data/releases.json` |
| Feature cards | `src/data/features.json` |
| Citations | `src/data/citations.json` |
| Learning links | `src/data/learn.json` |
| iOS privacy policy (`/privacy-mobile/`) | `src/pages/privacy-mobile.md` |
| Colours, type, spacing tokens | `src/styles/global.css` |
| Installer file names and shared URLs | `src/lib/site.ts` |

Installer download links are derived from the version in `lince-version.json` using the same
file names the release pipeline verifies before publishing, so the buttons only ever point at
assets that exist.

## Releasing

1. Update `lince-version.json` and add the new entry at the top of `src/data/releases.json`
   as part of the release commit.
2. The pipeline builds the site on every push (`build-site`). Pull requests can also get a
   temporary surge.sh preview (`preview-site`, needs the `SURGE_TOKEN` secret; see
   `docs/CI.md`). On a release build from `master` the `update-docs` job deploys the site
   after manual approval and installer verification.

## Design

Tokens follow the Lince Plus design system: charcoal canvas (`#14181B`), one saturated teal
(`#3FCDC2`, panels in `#5CD7CE`), Montserrat at heavy weights, pill buttons, no gradients or
backdrop blur. The lynx-head mark in `public/lince-logo.svg` uses `currentColor`.
