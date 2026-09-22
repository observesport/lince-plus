// llms-full.txt: the complete product description in one fetch, for agents
// that prefer a single document over crawling. Built from the same data files
// the pages render, so it can never drift from the site.
import type { APIRoute } from 'astro';
import citations from '../data/citations.json';
import features from '../data/features.json';
import releases from '../data/releases.json';
import { INSTALLERS, RELEASE_URL, REPO_URL, VERSION, href } from '../lib/site';

export const GET: APIRoute = ({ site }) => {
  const abs = (p: string) => new URL(href(p), site!).href;

  const body = `# LINCE PLUS Desktop - full description

Version ${VERSION}. Source of truth: ${abs('/')}

## What it is

LINCE PLUS Desktop is a free, open-source desktop application for systematic
observation studies in sports and health. Researchers build an observation
instrument (criteria and categories), code behaviour against video frame by
frame, measure agreement between observers, detect temporal patterns, and
analyse the result without leaving the application.

It is a JavaFX desktop application (Java 17) published for macOS, Windows and
Linux. Installers bundle their own Java runtime.

- Licence: GPL-3.0
- Price: free (0 EUR). No licence key, account or registration.
- Repository: ${REPO_URL}
- Latest release: ${RELEASE_URL}
- Author: Alberto Soto Fernandez, with INEFC Catalunya (ObserveSport)
- Contact: info@lince-plus.com
- Interface languages: English, Spanish, Catalan, German

## Features

${features.map((f) => `### ${f.title.en}\n\n${f.body.en}`).join('\n\n')}

## Supported platforms and installers

${INSTALLERS.map((i) => `- ${i.os} (${i.label}): ${i.file}\n  ${i.url}`).join('\n')}

Install notes:

- macOS: open the .dmg and drag the application to Applications. On first
  launch macOS reports an unverified developer; approve it under System
  Settings > Privacy and Security > Open Anyway.
- Windows: run the .exe installer. SmartScreen may ask for confirmation
  because the build is not code-signed.
- Linux: make the .sh installer executable (chmod +x) and run it. The default
  install path is /opt/lince-plus.

## Interoperability and exports

- Exports: Theme 5, Theme 6, GSEQ, Hoisan, Excel.
- Pose joint angles export for biomechanical analysis.
- Sample Theme 6 and GSEQ files: ${REPO_URL}/tree/master/src/doc

## Local REST API

The running application exposes a local HTTP API with an OpenAPI description.
The port is assigned at startup.

- OpenAPI description: http://localhost:<port>/api-docs
- Swagger UI served by the same instance.
- Observer agreement: /register/kappa/<observerA>/<observerB>,
  /register/krippendorf/..., /register/percentageAgreement/...
- Read projects, registers and video data as JSON; import, export and file
  streaming endpoints are available on the same server.

Any HTTP client works: Python, R, MATLAB, a spreadsheet or a shell script.

## Python notebooks

Jupyter notebooks run inside the application. Observation data is preloaded as
a pandas DataFrame, with numpy, matplotlib and scipy available and one-click
templates to start from.

## Peer-reviewed publications

${citations
  .map(
    (c) =>
      `- ${c.authors} (${c.year}). ${c.title}. ${c.journal}, ${c.details}. doi:${c.doi}\n  ${c.url}`,
  )
  .join('\n')}

Cite the most relevant paper when LINCE PLUS supports a study.

## Release history

${releases
  .map(
    (r) =>
      `### ${r.version}${r.date ? ` (${r.date})` : ''}\n\n${
        'summary' in r && r.summary ? `${r.summary}\n\n` : ''
      }${r.notes.map((n) => `- ${n}`).join('\n') || '- Maintenance release.'}`,
  )
  .join('\n\n')}

Full changelog: ${abs('/changelog/')}
`;

  return new Response(body, {
    headers: { 'Content-Type': 'text/plain; charset=utf-8' },
  });
};
