# Spec: LO-349 — Import/export as a REST sequence

## Problem Statement
Import and export in Lince PLUS are bound to the JavaFX root menu, to native file dialogs and to legacy
Swing `Command` classes that mix conversion logic with `JOptionPane` messages. The React dashboard, which
already imports quantitative data through the API, cannot import a project, a Lince 1 file or a HOISAN
database, nor produce any analysis format (CSV, Theme, SAS, SDIS-GSEQ, HOISAN, Lince 1). The conversion
logic cannot be reused by the cloud edition either. Two REST actions are needed, `/api/import` and
`/api/export`, backed by pure-Java importer/exporter classes that the JavaFX menu uses as well.

## Requirements

### Functional Requirements
- FR-1: `POST /api/import` accepts a multipart `file` and imports it into the loaded project. The type is
  detected from the extension (`.xml` Lince PLUS project, `.rlince` Lince 1 register, `.ilince` Lince 1
  tool, `.mdb` HOISAN) and may be forced with a `type` parameter; when neither resolves, 400.
- FR-2: For `lince-plus` files a `scope` parameter selects `full` (instrument, observations and profiles,
  no videos; today's "Import Lince PLUS project") or `observations` (today's "Import observer").
  Default `full`.
- FR-3: Lince 1 register and tool files and HOISAN databases import through the same action and produce
  the same project state as the JavaFX menu entries, including the legacy→modern conversion that
  `ensureImportCompatibility()` performs today.
- FR-4: `POST /api/import` returns an `ImportResult` with the type, scope, resulting counts (criteria,
  registers, observations, profiles) and warnings (unmatched criteria, skipped rows).
- FR-5: `POST /api/export` accepts `{ type, researchId?, criteriaIds[], options{} }` and returns the
  exported file. Empty `criteriaIds` means all criteria. `researchId` selects the observer/research
  profile as `getResearchSelection()` does today; omitted means current selection.
- FR-6: Export types available: `lince-plus` (.xml), `csv` (option `separator` comma/semicolon),
  `theme6` (.txt + vvt.vvt), `theme5` (.csv + .vvt), `sas` (.sds), `sdis-gseq-event`,
  `sdis-gseq-state`, `sdis-gseq-interval` (option `intervalSeconds`), `sdis-gseq-multievent`,
  `sdis-gseq-timed` (.sds), `hoisan` (.mdb), `lince1-register` (.rlince), `lince1-tool` (.ilince).
  Output is byte-identical to what the JavaFX actions write for the same selection.
- FR-7: Types producing several files return one `application/zip`; single-file types return the file
  with its MIME type. Both carry `Content-Disposition: attachment; filename=...`.
- FR-8: `GET /api/import/types` and `GET /api/export/types` describe each type (id, label, extensions,
  MIME type, criteria selection support, multi-file flag, options with key/label/type/default/values).
- FR-9: Adding an import or export type requires only a new class implementing the strategy interface,
  registered as a bean: no controller, service or FE change.
- FR-10: All conversion logic lives in pure-Java importer/exporter classes with no JavaFX, Swing,
  `Platform.runLater`, `Alert` or `JOptionPane` usage; errors are exceptions with a reason code.
- FR-11: The JavaFX menu import/export entries keep their `FileChooser` and criteria panel but delegate
  to the same services/classes, producing identical results.
- FR-12: Errors: unknown or undetectable type → 400; `type` contradicting the extension → 415; unknown
  `researchId` or criteria ids → 400 with the offending ids; empty selection where the type requires at
  least one criterion → 400 (today's "select at least one criteria" alert); internal failure → 500 with
  empty body and logged error, matching the other `*ControllerImpl` classes.
- FR-13: Contract (controller interfaces, DTOs, service and strategy interfaces) lives in `lince-data`
  so `lince-server` can implement it.
- FR-14: New / Open / Save / Save as / Download are untouched and stay JavaFX-only.

### Non-Functional Requirements
- NFR-1: No Spring Security or new third-party dependencies; zip via `java.util.zip`.
- NFR-2: Endpoints answer as soon as the embedded server is up and do not need the JavaFX stage.
- NFR-3: Typical projects (hundreds of observations) export within a normal HTTP request; multipart
  limits allow files of at least 50 MB.
- NFR-4: HOISAN temp files created for `.mdb` handling are removed after the request.
- NFR-5: Unit tests run without a Spring context (JUnit 5 + Mockito), consistent with
  `AnalysisControllerImplTest`, `LinceCsvExporterTest`, `Lince2ThemeExportTest`, `GseqExporterTest`.
- NFR-6: Labels for types and options come from the existing resource bundles so the dashboard shows
  the same names as the FX menu.

## Acceptance Criteria
- [ ] AC-1: `curl -F file=@project.xml :<port>/api/import` merges instrument, observations and profiles;
  `-F scope=observations` merges observations only; state matches the JavaFX menu entries (compared via
  `GET /analysis/get`, `GET /observationTool/getAll`, `GET /profile/get`).
- [ ] AC-2: `.rlince`, `.ilince` and `.mdb` uploads import without `type`; a `.dat` upload returns 400;
  `type=hoisan` with a `.xml` file returns 415.
- [ ] AC-3: `GET /api/export/types` lists every type in FR-6 with correct flags and options.
- [ ] AC-4: `POST /api/export` with `type=csv` and a criteria subset matches `CsvExportComponent` output
  for the same selection; `separator` switches comma/semicolon.
- [ ] AC-5: `type=theme6` returns a zip with `<name>.txt` and `vvt.vvt` equal to `Theme6ExportComponent`
  output; `type=theme5`, `sas`, each `sdis-gseq-*`, `hoisan`, `lince1-*` match their JavaFX counterparts
  on the test fixtures.
- [ ] AC-6: Unknown type → 404 on `/types/{id}` and 400 on `POST`; unknown criteria id → 400 listing it;
  empty selection on a criteria-requiring type → 400.
- [ ] AC-7: JavaFX menu: every import and export entry still works and writes the same files.
- [ ] AC-8: A test asserts the new importer/exporter classes import nothing from `javafx.`, `javax.swing`
  or `java.awt`.
- [ ] AC-9: Unit tests cover each importer/exporter, `ExportSelection`, `LegacyBridge`, both services and
  both controllers; `mvn test` passes for `lince-data`, `lince-data-fx`, `lince-math`, `lince-desktop`.
- [ ] AC-10: `plan.md` documents the request/response contract well enough for the dashboard ticket and
  the lince-server ticket to be implemented without re-analysis.

## Edge Cases
- Lince PLUS `.xml` whose instrument differs from the loaded one with `scope=observations`: import what
  maps, list mismatches in `warnings` (today silently logged).
- Lince 1 register imported into a project with no instrument: 400 with reason `instrument_required`
  (today `ensureImportCompatibility()` migrates the instrument first; the importer does the same when
  an instrument exists).
- Upload with no extension and no `type`: 400 asking for `type`.
- `criteriaIds` containing ids unknown to the current project: 400 listing them.
- Project with no observations: export succeeds with headers only.
- HOISAN `.mdb` export when the Access driver is unavailable: 500 with a logged cause, same as today's
  silent failure, but visible to the client.
- Concurrent import while the JavaFX UI edits: last writer wins, as today.

## Out of Scope
- New / Open / Save / Save as / Download (JavaFX-only, untouched).
- Dashboard pages and hooks (separate repo and ticket).
- Deleting the Swing `Command` classes/panels (they become thin wrappers; removal is a follow-up).
- Video playlist import/export.
- Background jobs, progress, server-side file browsing.
- Changing any file format.
- `lince-server` implementation and whitelist.
