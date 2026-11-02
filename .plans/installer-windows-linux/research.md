# Installer issues: Windows 11 Smart App Control and Linux launcher

Branch `fix/installer-windows-linux`. Research date 2026-11-02. User reports in `resources/`.

## 1. Linux (fixed in this branch)

Reproduced with the released `lince-plus_unix_4_1_0_linux-x64.sh` inside `debian:bookworm-slim`
(`/bin/sh` is dash, as on Ubuntu/Mint). Two defects in `src/installer/install4j/lince-plus-installer-v3.install4j`:

| Symptom reported | Root cause | Fix |
|---|---|---|
| `./lince-plus: 419: Bad substitution`, desktop/menu icons do nothing | Launcher `workingDirectory=".${launcher:sys.launcherDirectory}"`. install4j does not expand launcher variables in the Unix shell launcher, so line 419 of the generated script is literally `cd "$prg_dir"/.${launcher:sys.launcherDirectory}`. dash rejects it; bash silently expands it to empty, which is why `bash lince-plus` worked for the tester. | `workingDirectory="."` on both launchers. |
| `lince-os-launcher.sh: Permission denied`, needed `chmod +x` | The post-install chmod action (id 900) never ran: its condition read `context.getVariable("sys.platform")`, a compile-time variable that is `null` at install time, so it threw an NPE on every install. Its `executable` property was also serialized as a string instead of a `java.io.File`, which install4j logged as a bean serialization error at build time. The `mode="755"` attribute on the file entry is not a real install4j attribute. | Condition `!Util.isWindows()`, executable as `java.io.File`, arguments as a typed array. |
| `java: no se ha encontrado la orden`, `Unable to access jarfile lince-desktop.jar` | Both come from the legacy fallback `lince-os-launcher.sh`, which used the system `java` and a cwd-relative jar path. Its `--add-opens` flags were also placed after `-jar`, so they were never applied to the JVM. | Script deleted and removed from the Linux media. The real launcher `lince-plus` bundles Liberica 17 and already handles cwd and JVM flags. |

Verified locally with install4j 10.0.9 (`install4jc -b 337`) and the 4.1.0 application jar: after install in Debian the
launcher starts the JVM from any cwd under dash, `lince-os-launcher.sh` is gone, the chmod action runs without errors,
and `uninstall -q` removes the folder. The remaining log errors in the container are only `xdg-desktop-menu` and
`~/Desktop` missing, which do not exist in a container.

Not addressed from the reports (out of scope, documented for later):
- Duplicate menu entries in `~/.gnome/apps` and `~/.local/share/applications` come from install4j's
  "Create program group" plus "Create start menu entry" actions. Removing one of them is a UX decision.
- A user-scope uninstaller cannot see a previous `sudo` install in `/usr/local/lince-plus`. install4j has no
  built-in cross-scope detection; it would need a custom "previous installation" check.
- The `.desktop` entry has `Categories=Application;`, so desktops file it under "Other". `Education;Science;` would fit.

Re-build loop used, reusable for any future installer change:

```sh
# needs INSTALL4J_LICENSE in the shell and /Applications/install4j.app (10.0.9)
cp <some lince-desktop.jar> src/installer/install4j/lince-desktop.jar     # gitignored
/Applications/install4j.app/Contents/Resources/app/bin/install4jc -b 337 -r 4.1.1-SNAPSHOT \
  -D version=4.1.1-SNAPSHOT -d /tmp/i4j-out src/installer/install4j/lince-plus-installer-v3.install4j
docker run --rm --platform linux/amd64 -v /tmp/i4j-out:/dl:ro debian:bookworm-slim sh -c \
  'sh /dl/lince-plus_unix_*_linux-x64.sh -q -dir /opt/lince-plus; cd /tmp; /opt/lince-plus/lince-plus'
```

## 2. Windows 11: "Control Inteligente de Aplicaciones bloqueó una aplicación"

### What the dialog is

Windows 11 **Smart App Control (SAC)** blocked `C:\Program Files\lince-plus\lince-plus.exe`. SAC is on by default
on clean Windows 11 installs. It allows a binary only if (a) Microsoft's cloud reputation service already knows it as
safe, or (b) it is signed with a certificate chaining to a CA in the Microsoft Trusted Root Program. It checks every
PE file, so signing only the installer `.exe` is not enough: the launcher `lince-plus.exe` that install4j generates
must be signed too. There is no per-app allow-list for end users and a self-signed certificate does not count.
Every new unsigned build starts with zero reputation.

### Options, cheapest first

| Route | Cost | Fits our pipeline? | Fixes the dialog for... |
|---|---|---|---|
| **A. SignPath Foundation** (free OSS code signing, GitHub Actions is a supported trusted build system) | Free. Needs application approval, MFA for all team members, a "Code signing policy" section on the project page, manual approval per release. | Yes, once install4j can call an external signer (see prerequisite). | Everyone, including GitHub Releases and website downloads. |
| **B. Microsoft Store, MSIX** | Free (individual and company registration fees were dropped in 2025/2026). The Store re-signs MSIX with a Microsoft certificate, so no certificate is needed. | Needs an MSIX package. install4j 13.0 builds MSIX natively; install4j 10 cannot. First submission is manual in Partner Center, updates can run from CI with `microsoft/setup-msstore-cli` + `msstore publish`. | Only users who install from the Store. Website/GitHub downloads stay blocked unless also signed. |
| C. Azure Artifact Signing (ex Trusted Signing) | USD 9.99/month. Individuals only in US/CA. EU organizations are eligible after identity validation (a university or company tax id). | Yes via `jsign` or SignTool as install4j external signer (same prerequisite as A). | Everyone. |
| D. OV certificate from a CA (Certum OSS ~EUR 69/yr, others EUR 150-300/yr) | Paid, and since 2023 the key must live on a hardware token or HSM, so CI signing needs a cloud HSM anyway. | Awkward in GitHub-hosted CI. | Everyone. |

Store submission of the current `.exe` installer is **not** a shortcut: for EXE/MSI submissions the Store does not
re-sign and requires the installer and all PE files inside it to be Authenticode-signed already.

### The common prerequisite: install4j 11 or newer

- We build with install4j **10.0.9** (`ci.yml` downloads the .deb, `install4j.version` 10.0.6 in `src/pom.xml`).
- The "external executable for Windows code signing" option (`$EXECUTABLE`/`$OUTFILE` hook, runs for the launcher
  and the installer) arrived in **install4j 11.0** (2024-09). "Additional binaries to be signed" arrived in 12.0.
  MSIX media arrived in **13.0** (2026-06). Latest is 13.1.1 (2026-09-18).
- ej-technologies grants **free open source licenses** for install4j to non-profit projects with a website and a
  released product, in exchange for a "multi-platform installer builder" link on the project page. Lince PLUS is
  GPL-3.0 and public (`observesport/lince-plus`), so it qualifies. Request a 13.x license.

### Recommendation

1. **Now:** request an install4j 13 open source license from ej-technologies and apply to SignPath Foundation
   (https://signpath.org/apply). Both are free and both take days, not hours. Register the (free) Microsoft Store
   developer account in parallel so the name "Lince PLUS" is reserved.
2. **When the install4j license arrives:** open the project in install4j 13 (it migrates the project file), bump
   `install4j.version` and the .deb URL in `ci.yml`, verify the four media still build. Nothing else changes.
3. **When SignPath approves:** add signing to the `build` job (design below). This removes the dialog for every
   download channel. This is the primary fix.
4. **Optional, after 2:** add an MSIX media set and a Store listing for discoverability and automatic updates.
   Store updates from CI: `msstore reconfigure` with tenant/seller/client secrets, then `msstore publish <msix>`.

### CI design for SignPath on GitHub Actions (to implement after approval)

SignPath verifies that the artifact was produced by a GitHub-hosted workflow and uploaded as a workflow artifact,
so signing has to happen as workflow steps, not inside the install4j process. Two signing requests per release:

1. `build` job (Linux, as today) runs install4j with the Windows media **unsigned**, with
   `--disable-signing`-equivalent config, then uploads the Windows launcher input and the installer separately.
   Simplest workable shape with install4j 11+: configure the external signer as a tiny script that copies
   `$EXECUTABLE` to a staging folder and returns; after `install4jc` finishes, upload the staging folder,
   call `signpath/github-action-submit-signing-request@v3` with `wait-for-completion: true`, then run
   `install4jc` a second time with the external signer pointing at the signed copies (copy-back script).
   Alternative: run the whole Windows media build on a `windows-latest` runner and use SignPath's PowerShell
   `Submit-SigningRequest -WaitForCompletion` as the install4j external signer, which is one pass. Pick after
   trying the first in a draft PR, because it depends on install4j behaviour we cannot test on 10.0.9.
2. Secrets needed in the `cicd` environment: `SIGNPATH_API_TOKEN`; organization id, project slug and policy slug
   can be repository variables.
3. The `Verify installers` step should additionally run `osslsigncode verify` (available on ubuntu-24.04) on the
   Windows installer so a release can never ship unsigned by accident.
4. Website: add the SignPath "Code signing policy" section and the install4j link (both are license conditions).

### Interim message for users (until signing lands)

SAC cannot be bypassed per app. A user can switch SAC off in Windows Security > App & browser control
(since the April 2026 update it can be turned back on later). Worth a short FAQ entry on the website.

## 3. macOS: same class of problem, different fix

Checked on the locally built `mac-arm` media (id 268) installed with `-q` on this Apple Silicon Mac. The installed
`Lince Plus.app` starts the JVM from any cwd with the new `workingDirectory="."`, so the Linux fix does not break
macOS. Its signature state is bad, though:

```
codesign -dv "Lince Plus.app"  ->  TeamIdentifier=989KYA3D43   (ej-technologies' stub signature, not ours)
spctl --assess --type execute   ->  code has no resources but signature indicates they must be present
```

The project has no macOS signing or notarization configured, so the stub keeps the vendor's signature and
Gatekeeper rejects the bundle. On macOS 15 users see "Apple could not verify..." or "is damaged and can't be opened"
and must use System Settings > Privacy & Security > Open Anyway (the right-click Open shortcut no longer works on
macOS 15). Neither SignPath nor the Microsoft Store help here.

What macOS needs:
- Apple Developer Program membership, USD 99/year. There is no free route; Apple has no OSS program.
- A "Developer ID Application" certificate plus notarization through App Store Connect (API key).
- install4j already supports both in 10.x (General Settings > Code Signing > macOS, and the notarization step),
  keystore as `.p12` plus the App Store Connect key, both storable as GitHub secrets. Notarization used to require a
  macOS build machine; newer install4j versions advertise cross-platform notarization (version not verified).
  Alternative that definitely runs on `ubuntu-24.04`: `rcodesign` (apple-codesign) signs and notarizes `.app`/`.dmg`
  from Linux with the same certificate and API key, as a post-build step in the `build` job.
- Until then, add the "Open Anyway" steps to the download page next to the Windows SAC note.

## Sources

- Smart App Control overview (Microsoft): https://learn.microsoft.com/en-us/windows/apps/develop/smart-app-control/overview
- Code signing options for Windows app developers: https://learn.microsoft.com/en-us/windows/apps/package-and-deploy/code-signing-options
- Store MSIX package requirements (re-signing statement): https://learn.microsoft.com/en-us/windows/apps/publish/publish-your-app/msix/app-package-requirements
- Store EXE/MSI requirements (must be signed): https://learn.microsoft.com/en-us/windows/apps/publish/publish-your-app/msi/app-package-requirements
- Store publishing from GitHub Actions: https://learn.microsoft.com/en-us/windows/apps/publish/msstore-dev-cli/github-actions
- Free Store registration, individuals (2025-09): https://blogs.windows.com/windowsdeveloper/2025/09/10/free-developer-registration-for-individual-developers-on-microsoft-store/
- Free Store registration, companies (2026-05): https://blogs.windows.com/windowsdeveloper/2026/05/07/publish-to-microsoft-store-as-a-company-now-with-free-registration-and-faster-onboarding/
- SignPath Foundation terms: https://signpath.org/terms and GitHub Actions integration: https://docs.signpath.io/trusted-build-systems/github
- Azure Trusted/Artifact Signing eligibility and pricing: https://textslashplain.com/2025/03/12/authenticode-in-2025-azure-trusted-signing/
- install4j code signing concepts (external executable, `$EXECUTABLE`): https://www.ej-technologies.com/resources/install4j/help/doc/concepts/codeSigning.html
- install4j changelog (11.0 external signing, 13.0 MSIX): https://www.ej-technologies.com/install4j/changelog
- install4j open source licenses: https://www.ej-technologies.com/buy/install4j/licensing.html
- jsign (cross-platform Authenticode, supports Azure Artifact Signing, suited to install4j output): https://ebourg.github.io/jsign/
- Smart App Control blocked-app threads with the same dialog: https://github.com/NousResearch/hermes-agent/issues/94438 , https://textslashplain.com/2026/04/28/smart-app-control/
