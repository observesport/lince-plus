# Plan: LO-349 (phase 2) — JavaFX-free legacy transfer module and import/export follow-ups

Second plan folder under LO-349. Phase 1 (`.plans/LO-349-importexport/`, PR #153, merged 2026-10-22) moved the
import/export actions onto `/api/import` and `/api/export`. This phase makes the whole transfer catalogue usable
without JavaFX, so `lince-server` can offer it, and closes the three follow-ups phase 1 recorded.

## Overview

Only four transfer types are JavaFX-free today: export `lince-plus`, `csv`, `theme6` and import `lince-plus`
(all in `lince-data`). The other nine (Theme 5, SAS, the five SDIS-GSEQ variants, HOISAN, Lince 1 register and
tool) live in `lince-data-fx`, whose pom pulls `org.openjfx`. The transfer classes themselves import no JavaFX
(`NoUiImportsTest` enforces it), but the legacy model they drive (`com.lince.observer.legacy.*`) and its helpers
sit in the same module, so any consumer inherits JavaFX. `lince-server` currently has to depend on
`lince-data-fx` with `org.openjfx` excluded, which is brittle.

This phase extracts the legacy model, its loaders and the nine transfer types into a new Maven module
`lince-legacy` that depends on `lince-data` only (plus Jackcess and commons), leaves `lince-data-fx` as the JavaFX
layer depending on `lince-legacy`, and uses the single modern-to-legacy converter (`LegacyBridge`) for the kappa
statistics too. It also fixes CSV quoting, adds an Excel-safe option, and refuses an empty project on a full import.

Decision (2026-10-23): all nine legacy types move, Lince 1 and HOISAN included, so the server offers the same
catalogue as the desktop (13 export, 4 import types). A split that kept Lince 1 and HOISAN desktop-only was
considered and dropped: it forced the Lince 1 load/save entry points out of the model classes for no gain, since the
`LegacyXmlGuard` already covers the `XMLDecoder` path wherever it runs.

## Goals

- `lince-legacy` module: JavaFX-free, hosts the legacy model, `LegacyBridge`, the nine importers/exporters, the
  HOISAN reader, the Lince 1 XML guard path, the i18n bundles the descriptors read, and the Lince 1 fixtures as a
  test-jar.
- `lince-server` can depend on `lince-data` + `lince-legacy` and register all 13 export and 4 import types with no
  JavaFX on the classpath (documented, implemented in the lince-server repo).
- One modern-to-legacy conversion: kappa (`StatsServiceImpl`) goes through `LegacyBridge`; the export half of
  `LegacyConverterService` is deleted.
- CSV: RFC 4180 quoting always; `excelSafe` option neutralising formula-leading cells, off by default on the API,
  on for the JavaFX "Excel" menu entry.
- `POST /api/import` with `scope=full` refuses a project file with no criteria and no observations (new reason
  `empty_project`, 400); the JavaFX "Import project" entry shows the same message.
- Reactor, CI snapshot deploy and the desktop installer keep working unchanged apart from the new module.

## Non-Goals

- Replacing `java.beans.XMLDecoder` for Lince 1 files (the `LegacyXmlGuard` allow-list stays the mitigation).
- Removing the Swing tree classes (`javax.swing.tree.DefaultMutableTreeNode` and friends) from the legacy model.
  They come from the JDK `java.desktop` module, not JavaFX, and work headless; replacing them is a later refactor.
- The lince-server services, controllers and whitelist themselves (separate repo); dashboard Import/Export pages.
- Authentication for the desktop REST API and the CORS policy (own ticket).
- i18n of type labels beyond keeping the existing bundles working.

## Technical Approach

### Step 1 — module `lince-legacy`

New module `src/lince-legacy` (artifact `lince-legacy`, same parent, Java 17), dependencies: `lince-data`,
`jackcess`, `commons-lang3`, `commons-collections4`, `commons-io`, `jakarta.xml.bind-api` only if a moved class
needs it; **no** `org.openjfx`. Surefire runs with `java.awt.headless=true`. Produces a test-jar.

Moves (git mv, package names unchanged so Spring's `com.lince.observer` scan and lince-server stay valid):

| From lince-data-fx | Content |
|---|---|
| `com/lince/observer/legacy/**` | `Registro`, `FilaRegistro`, `instrumentoObservacional/*`, `export/*` (GSEQ writers). `ModeloDeTablaLince` (Swing table model) is deleted if nothing outside the deleted Swing UI uses it. |
| `data/legacy/datos/ControladorArchivos` | Lince 1 file load/save (uses `PackageAwareXMLSerializer` from lince-data). |
| `data/legacy/plugins/{HoisanTool,HoisanVars}` + `resources/template/**` | HOISAN reader/writer and its template database. |
| `data/legacy/utiles/{MyObservable,ResourceBundleHelper,Tiempo}` + `resources/i18n/Bundle*.properties` | Helpers and the bundles the descriptor labels read. |
| `data/LegacyStatsHelper` | Kappa helper over legacy registers. |
| `data/transfer/legacy/**`, `data/transfer/importers/{Hoisan,Lince1Register,Lince1Tool}Importer`, `LegacyImportSupport`, `data/transfer/exporters/{Theme5,Sas,SdisGseq,Hoisan,Lince1Register,Lince1Tool}Exporter`, `SdisGseqExporterConfiguration`, `LegacyExportSupport` | The nine types and the bridge. |
| tests: `transfer/**`, `legacy/**`, `integration/*Export*`, fixtures `lince1/**`, `prj/template.*`, `transfer/golden/**`, `Lince1Fixtures`, `ExporterFixture`, `HeadlessTransferTest`, `NoUiImportsTest` | Moved with the code; `NoUiImportsTest` widened to the whole module: no `javafx.*`, no `javax.swing.*` except `javax.swing.tree`, no `java.awt`. |

`lince-data-fx` keeps: `data/javafx/**` (panels, `ExportSaveFlow`, `ExportTargets`), `data/base/**`
(`DataHubServiceBase`, `LinceFileHelperFx`, `ILinceApp`), `data/system/**`, `data/util/{JavaFXLogHelper,FxCollectionHelper}`,
`data/bean/fx/**`, `EmptyLinceApp` test support. It depends on `lince-legacy`. Everything that imports the moved
packages keeps compiling because the packages do not change.

Dependency graph after the move: `lince-data` ← `lince-legacy` ← `lince-data-fx` ← `lince-math` ← `lince-desktop`.
`lince-math` still needs `lince-data-fx` for `DataHubServiceBase` and `DesktopQualifier`; `lince-ai` is unchanged.

Reactor: add `<module>lince-legacy</module>` before `lince-data-fx` in `src/pom.xml`. CI: the manual snapshot deploy
runs `-pl '!lince-desktop'`, so the new jar deploys with the others; the desktop fat jar and Install4j pick it up
transitively. `docs/CI.md` and `CLAUDE.md` module lists updated.

### Step 2 — one modern-to-legacy converter

`StatsServiceImpl.calculateLegacyKappaLocked` builds both legacy registers through
`legacyBridge.toLegacy(new ExportSelection(project, registerId, List.of()))` (all criteria), under the lock it already
holds (`withLegacyModel` is re-entrant). `LegacyProject` exposes the register; the instrument singleton is the same for
both registers of one project. Then delete `LegacyConverterService.migrateDataToLegacy*`, `migrateRegisterToLegacy`,
`getLegacyCriteria`, `getLegacyCategory`, `findInstrumentDataByCategory`, `addCategoriesToLegacyCriteria` and the class
itself if nothing else remains; drop the `javax.swing.tree` import from lince-math. A kappa regression test records
the values the old converter produced on a fixture pair before the switch and asserts them after it (the two
converters had drifted: integer division in the millisecond conversion and random criterion codes in the lince-math
copy; the test tells whether that drift was visible in kappa).

### Step 3 — CSV quoting and Excel-safe option

`LinceCsvExporter` (lince-data): every field goes through an RFC 4180 writer: wrap in double quotes when it contains
the separator, a double quote, CR or LF; double the quotes inside. `CsvExporter` gains
`TransferOption.bool("excelSafe", "Excel-safe cells", false)`: when true, a cell starting with `=`, `+`, `-`, `@`,
tab or CR is prefixed with a single quote before quoting (OWASP CSV injection mitigation). The JavaFX
`CsvExportComponent` (menu entry "Excel") passes `excelSafe=true`; the REST default stays `false` so R consumers get
raw codes. Golden/characterisation tests pin both forms. `outcome.md` option table updated.

### Step 4 — refuse an empty full import

`LinceProjectImporter.importInto`, scope `FULL`: when the source has no criteria and no register rows, throw
`ImportException(Reason.EMPTY_PROJECT, "The file holds no instrument and no observations")`. New reason
`EMPTY_PROJECT` → 400 in `TransferResponses`, documented in `outcome.md`. The desktop abuse test
`emptyLincePlusRootIsAcceptedAndEmptiesTheProject` is replaced by the refusal test. `scope=observations` on an empty
file keeps its current result (nothing imported, zero counts). The JavaFX menu surfaces the message through the existing
`TransferException` path.

### Cloud (lince-server, documented only)

Depend on `lince-data` + `lince-legacy` (no openjfx exclusion needed), implement `ImportService`/`ExportService` over
the stored project (`@CloudQualifier`), register `ImportControllerImpl`/`ExportControllerImpl` equivalents, whitelist
`/api/import/**` and `/api/export/**`, and run the lince-legacy `HeadlessTransferTest` equivalent in its own CI. The
`LegacyBridge` lock is process-wide: on a multi-user server every legacy export/import serialises on it (acceptable
for the current load; note it in lince-server's plan).

## Dependencies

- Phase 1 merged (PR #153). The lince-server repo for the cloud part. Jackcess 1.2.13 (already a dependency).
- GitHub Packages snapshot deploy (manual) to publish `lince-legacy` for lince-server.

## Risks & Mitigations

| Risk | Impact | Mitigation |
|------|--------|------------|
| A moved class still needs something JavaFX-side (e.g. `JavaFXLogHelper`) | Med | `NoUiImportsTest` on the whole module plus the compile itself; move the helper or replace the call before the move |
| Kappa values change after switching converters | Med | Regression test with values captured from the old converter first; investigate any difference before deleting |
| Swing tree classes on a headless server | Low | `java.desktop` is in every JDK; `DefaultMutableTreeNode` needs no display; `HeadlessTransferTest` runs with `java.awt.headless=true` |
| CSV output changes for existing R pipelines | Med | Quoting only touches fields that were already broken (separator/quote/newline inside); `excelSafe` is opt-in on the API |
| Desktop installer / fat jar misses the new module | Low | Transitive dependency; CI `build` job verifies installers on develop |
| Test fixtures referenced across modules (`../lince-data-fx/...` paths in lince-data and lince-desktop tests) | Low | Point them at `../lince-legacy/...` or read them from the lince-legacy test-jar |

## Open Questions

- Should the JavaFX "Excel" entry be renamed "CSV" and expose the Excel-safe checkbox, instead of defaulting it on?
- Does lince-server need the HOISAN types at all (Jackcess, Access databases) or should its catalogue omit them?
  (Moving them costs nothing; registering them on the server is a lince-server decision.)
- Keep `ModeloDeTablaLince` (Swing table model) if anything still references it, or delete with the move?
