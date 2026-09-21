// @ts-check
import { defineConfig } from 'astro/config';

// Production lives at https://observesport.github.io/lince-plus/ and is
// published by the `update-docs` stage of .github/workflows/maven.yml.
//
// Temporary previews (the `preview-site` stage, surge.sh) are served from the
// root of their own host, so the pipeline overrides both values through the
// environment: SITE_URL=https://<name>.surge.sh SITE_BASE=/
export default defineConfig({
  site: process.env.SITE_URL ?? 'https://observesport.github.io',
  base: process.env.SITE_BASE ?? '/lince-plus',
  trailingSlash: 'ignore',
  i18n: {
    // English at the root, the other languages under /es, /de and /ca.
    // Page files live in src/pages (English) and src/pages/[lang] (others);
    // both render the same views from src/views.
    defaultLocale: 'en',
    locales: ['en', 'es', 'de', 'ca'],
    routing: {
      prefixDefaultLocale: false,
    },
  },
  build: {
    assets: 'assets',
  },
});
