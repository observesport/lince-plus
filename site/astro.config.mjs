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
  build: {
    assets: 'assets',
  },
});
