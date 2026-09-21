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
| UI strings in EN, ES, DE, CA | `src/i18n/ui.ts` (same keys in every locale; English is the fallback) |
| Hero screenshot (AI Studio player) | `public/img/ai-studio-player.png` |
| Colours, type, spacing tokens | `src/styles/global.css` |
| Installer file names and shared URLs | `src/lib/site.ts` |

Installer download links are derived from the version in `lince-version.json` using the same
file names the release pipeline verifies before publishing, so the buttons only ever point at
assets that exist.

## Languages

English is served at the site root, Spanish, German and Catalan under `/es/`, `/de/` and
`/ca/` (Astro i18n routing, `astro.config.mjs`). The page files in `src/pages` and
`src/pages/[lang]` are thin wrappers around the shared views in `src/views`; components read
the locale with `getLocale(Astro)` and translate with `useTranslations(locale)`. Feature and
learning cards carry one string per locale in `src/data/*.json`. Release notes stay in English;
the Spanish home page shows the Spanish highlights from `lince-version.json` instead. The
iOS privacy policy is English only.

## Analytics

Google Analytics 4 uses the same property as lince-plus.com (`GA_MEASUREMENT_ID` in
`src/lib/site.ts`). Every hit carries `app_mode: lince-plus-desktop-site` as a config
parameter and user property so the desktop site can be segmented from the main site. Link
clicks are sent as `click` events (or `file_download` for installers) with `link_url`,
`link_text`, `link_id` (from `data-ga`) and `section`; see `src/components/Analytics.astro`.

## Releasing

1. Update `lince-version.json` and add the new entry at the top of `src/data/releases.json`
   as part of the release commit.
2. The pipeline builds the site on every push (`build-site`). Pull requests can also get a
   temporary surge.sh preview (`preview-site`, needs the `SURGE_TOKEN` secret; see
   `docs/CI.md`). On a release build from `master` the `update-docs` job deploys the site
   after manual approval and installer verification.

## Design

Colours and shapes follow the Lince Plus design system: charcoal canvas (`#14181B`), one
saturated teal (`#3FCDC2`, panels in `#5CD7CE`), pill buttons, no gradients or backdrop blur.
Typography and the icon match lince-plus.com: Inter for text, Red Hat Display for headings and
the wordmark, and the same lynx mark (`linceIconV2.svg`, cropped to the artwork in
`public/lince-logo.svg`, coloured through `currentColor`). The brand reads "LINCE PLUS" with a
"Desktop" tag to tell this site apart from lince-plus.com.
