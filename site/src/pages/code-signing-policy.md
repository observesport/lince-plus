---
layout: ../layouts/Prose.astro
title: Code signing policy
description: How LINCE PLUS release binaries are built and signed, who approves them, and what the signature guarantees.
updated: '2026-11-04'
---

This page describes how the **LINCE PLUS** desktop installers are built and code-signed, and who is responsible for each step. It exists so that users and the signing service can verify that a signed binary comes from the public source code of this project.

If you have questions, contact **alberto.soto@gmail.com**.

---

## Code signing policy

Free code signing provided by [SignPath.io](https://about.signpath.io), certificate by [SignPath Foundation](https://signpath.org).

- Windows installers and the launcher inside them are signed with a certificate issued to SignPath Foundation. The signature confirms that the binary is an automated build produced by GitHub Actions from the source code in the public repository [observesport/lince-plus](https://github.com/observesport/lince-plus), at the commit the release is tagged on.
- Builds run on GitHub-hosted runners only. The workflow that produces them, `.github/workflows/ci.yml`, is part of the repository and therefore part of the reviewed source.
- Every release is submitted for signing manually and approved by a team member before the signature is issued.
- Signed packages may contain unsigned upstream open-source binaries, such as the bundled Java runtime.

## Team roles

| Role | Member |
|---|---|
| Author | Alberto Soto Fernández ([albertoSoto](https://github.com/albertoSoto)) |
| Reviewer | Alberto Soto Fernández |
| Approver | Alberto Soto Fernández |

Authors are trusted to modify the source code. Changes proposed by anyone else arrive as pull requests and are reviewed by a team member before merging. Approvers confirm each signing request.

All team members use multi-factor authentication for GitHub and for SignPath.

## Privacy policy

LINCE PLUS desktop runs entirely on your computer. It does not collect, transmit or share personal data, does not include analytics, advertising or tracking, and does not check for updates or contact any server on its own. Project files, videos and exports stay on your disk unless you share them yourself.

The iOS companion app has its own [privacy policy](/privacy-mobile/).

## Verifying a download

On Windows, right-click the installer, open **Properties > Digital Signatures** and check that the signer is **SignPath Foundation**. Release checksums are published with every [GitHub release](https://github.com/observesport/lince-plus/releases).
