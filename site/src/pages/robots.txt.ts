// robots.txt is generated so the Sitemap line follows Astro.site + base.
// A static file in public/ would hard-code the production origin and break
// the surge.sh previews, which are served from the root of their own host.
import type { APIRoute } from 'astro';

export const GET: APIRoute = ({ site }) => {
  const base = import.meta.env.BASE_URL.replace(/\/$/, '');
  const abs = (path: string) => new URL(`${base}${path}`, site).href;

  const body = `# LINCE PLUS Desktop - https://github.com/observesport/lince-plus
# Free, open-source software for systematic observation studies in sports and health.

User-agent: *
Allow: /

# AI and answer-engine crawlers are welcome: this site documents free,
# open-source research software and we want it quoted correctly.
User-agent: GPTBot
Allow: /

User-agent: OAI-SearchBot
Allow: /

User-agent: ChatGPT-User
Allow: /

User-agent: ClaudeBot
Allow: /

User-agent: Claude-Web
Allow: /

User-agent: Claude-SearchBot
Allow: /

User-agent: anthropic-ai
Allow: /

User-agent: PerplexityBot
Allow: /

User-agent: Perplexity-User
Allow: /

User-agent: Google-Extended
Allow: /

User-agent: CCBot
Allow: /

User-agent: Applebot-Extended
Allow: /

User-agent: Bytespider
Allow: /

User-agent: meta-externalagent
Allow: /

# Machine-readable summaries for agents (llmstxt.org)
# ${abs('/llms.txt')}
# ${abs('/llms-full.txt')}

Sitemap: ${abs('/sitemap-index.xml')}
`;

  return new Response(body, {
    headers: { 'Content-Type': 'text/plain; charset=utf-8' },
  });
};
