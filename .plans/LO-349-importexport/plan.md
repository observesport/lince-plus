# Plan: LO-349 — Import/export as a REST sequence (JavaFX menu → `/api/import` + `/api/export`)

## Overview
The import and export actions of Lince PLUS live in the JavaFX root menu (`RootLayoutController`):
"Import Lince PLUS project" (instrument + observations + profiles), "Import observer" (observations
only), the Lince 1 register/tool import and export, the HOISAN import and export, and the Theme 5,
Theme 6, SAS, CSV/Excel and SDIS-GSEQ exports. Each one opens a native `FileChooser`; the exports also
open a JavaFX `GenericExportComponent` whose `SelectionPanelComponent` lets the user pick the criteria to
include. Most of the actual work is hidden inside legacy Swing `Command` classes (`LoadRegistro`,
`LoadInstrumentoObservacional`, `AbrirImportarHoisan`, `SaveRegistroAs`, `ExportarRegistro*`, …) that mix
file dialogs, `JOptionPane` messages and the conversion itself.

The React dashboard (separate repo, bundle shipped under `lince-desktop/src/main/resources/public`)
cannot reach any of it. The quantitative-data import already works the way we want: the browser owns the
file and the backend only deals with data over HTTP. This change gives import and export that shape.

Two endpoints, contracted in `lince-data` and implemented in `lince-desktop` (same `controller`
interface + `*ControllerImpl` pattern as `/info`, LO-343):

- `POST /api/import` — one action for every importable file. The type is detected from the extension
  (`.xml`, `.rlince`, `.ilince`, `.mdb`) and can be forced with an explicit `type`; a `scope` option
  covers the "whole project" vs "observations only" distinction.
- `POST /api/export` — one action that carries every parameter: `type`, `researchId`, `criteriaIds`
  and per-type `options`, and streams the file (or a zip when the type produces several files).

The core of the change is not the controllers but the **pure-Java importer/exporter classes**: the
logic now trapped in the `Command`s moves into reusable classes that take an `ILinceProject` (or a
stream) in and return content out, with no dialogs, no `Platform.runLater`, no `JOptionPane`. The REST
services and the JavaFX menu both call them, so the menu keeps working with the same results.

New / Open / Save / Save as / Download stay in `lince-desktop` as JavaFX actions. They are not part of
this ticket.

## Goals
- `POST /api/import` and `POST /api/export` callable from the dashboard without any desktop dialog.
- One reusable, dialog-free class per import type and per export type, living where its model lives,
  discoverable through two small registries (`ImportType`, `ExportType`).
- Export is type-agnostic: every type accepts the same request (criteria selection, research, options);
  adding a type touches no controller and no FE code.
- Import detects the type from the file extension and accepts an explicit `type` override.
- Contract (controller interfaces, DTOs, service interfaces, strategy interfaces) in `lince-data` so
  `lince-server` can implement it.
- JavaFX menu entries keep working by delegating to the same classes; their `FileChooser` stays.
- Unit tests at the importer/exporter and service level (JUnit 5 + Mockito, no Spring context).

## Non-Goals
- New / Open / Save / Save as / Download of the project file: they remain JavaFX-only (`LinceFileHelperFx`).
- The React screens (separate repo). This plan fixes the API they will call.
- Deleting the Swing `Command` classes or panels. They become thin wrappers over the new classes; removal
  is a follow-up once nothing else uses them.
- Video playlist import/export (`/video` already exists).
- Async jobs, progress, server-side file browsing. Exports are synchronous and return bytes.
- The cloud (`lince-server`) implementation and whitelist; this plan states what it must add.
- Changing any file format.

## Technical Approach

### Step 1 — Pure-Java importers and exporters (the reusable layer)
Move the logic out of each `Command` into a class with no UI. Where the model is modern the class goes
in `lince-data`; where it needs the legacy `Registro`/`InstrumentoObservacional` model it goes in
`lince-data-fx` (the module that owns the legacy packages) but still without Swing/JavaFX calls.

| Type id | Today (dialog-coupled) | New reusable class | Module |
|---------|------------------------|--------------------|--------|
| import `lince-plus` (.xml) | `LinceFileHelperFx.addBaseProjectInfo` / `addObservations` | `LinceProjectImporter` — `read(InputStream) → LinceFileProjectWrapper`, `merge(target, source, ImportScope)` | lince-data |
| import `lince1-register` (.rlince) | `LoadRegistro` → `Registro.loadInstance(File)` + `ensureImportCompatibility()` | `Lince1RegisterImporter` — loads the legacy register from a stream and converts to `ILinceProject` registers via `LegacyConverterService` | lince-data-fx |
| import `lince1-tool` (.ilince) | `LoadInstrumentoObservacional` | `Lince1ToolImporter` — loads the legacy instrument and converts to `List<Criteria>` | lince-data-fx |
| import `hoisan` (.mdb) | `AbrirImportarHoisan` → `HoisanTool.importFile(File)` | `HoisanImporter` — wraps `HoisanTool.importFile` + conversion | lince-data-fx |
| export `csv` | `CsvExportRegisterCommand` → `LinceCsvExporter` | `CsvExporter` (thin: already pure) | lince-data |
| export `theme6` | `Theme6ExportComponent` → `Lince2ThemeExport` | `Theme6Exporter` (.txt + vvt.vvt) | lince-data |
| export `theme5` | `ExportarRegistroTheme5` → `Registro.exportToTheme5(criterios)` | `Theme5Exporter` (.csv + .vvt via `ExportarInstrumentoObservacionalTheme`) | lince-data-fx |
| export `sas` | `ExportarRegistroSas` | `SasExporter` (.sds) | lince-data-fx |
| export `sdis-gseq-{event,state,interval,multievent,timed}` | `ExportarRegistroSdisGseq*` → `GseqExporterFactory` | `SdisGseqExporter` parametrised by `GseqFormatType` (.sds) | lince-data-fx |
| export `hoisan` | `AbrirExportarHoisan` → `HoisanTool.exportFile(File)` | `HoisanExporter` (.mdb) | lince-data-fx |
| export `lince1-register` / `lince1-tool` | `SaveRegistroAs` / `SaveInstrumentoObservacionalAs` | `Lince1RegisterExporter` (.rlince) / `Lince1ToolExporter` (.ilince) | lince-data-fx |

Shared pieces:
- `ImportType` / `ExportType` descriptors (id, label, extensions, mimeType, `supportsCriteriaSelection`,
  `multiFile`, `options`), returned by each class so the registries are built by injection
  (`List<LinceImporter>`, `List<LinceExporter>`).
- `ExportSelection` — resolves `researchId` + `criteriaIds` against an `ILinceProject` into the filtered
  criteria and register rows every exporter consumes (replaces what `SelectionPanelComponent` +
  `getResearchSelection()` do today).
- `LegacyBridge` — the headless version of `ensureExportCompatibility()` / `ensureImportCompatibility()`:
  `ILinceProject → Registro` and back through `LegacyConverterService` (`lince-math`), with the modern
  `Criteria` ↔ legacy `Criterio` id mapping. Only legacy exporters/importers use it.
- Errors are exceptions (`ImportException`, `ExportException` with a reason code); the services map them
  to HTTP, the JavaFX wrappers map them to the existing `JavaFXLogHelper.showMessage`.

### Step 2 — Contract (lince-data)
| Type | Package | Role |
|------|---------|------|
| `ImportController` | `com.lince.observer.data.controller` | interface, `RQ_MAPPING_NAME = "/api/import"`, `POST ""` multipart, `GET /types` |
| `ExportController` | `com.lince.observer.data.controller` | interface, `RQ_MAPPING_NAME = "/api/export"`, `POST ""` JSON, `GET /types` |
| `ImportScope` | `com.lince.observer.data.bean.transfer` | enum `FULL` (instrument + observations + profiles), `OBSERVATIONS`; lowercase JSON |
| `ImportTypeDescriptor` / `ExportTypeDescriptor` | `com.lince.observer.data.bean.transfer` | records as above |
| `ImportResult` | `com.lince.observer.data.bean.transfer` | record: `type`, `scope`, `criteriaCount`, `registerCount`, `observationCount`, `profileCount`, `warnings` |
| `ExportRequest` | `com.lince.observer.data.bean.transfer` | record: `type`, `researchId?`, `criteriaIds` (empty = all), `options: Map<String,String>` |
| `ExportedFile` | `com.lince.observer.data.bean.transfer` | record: `name`, `mimeType`, `content: byte[]` |
| `LinceImporter` / `LinceExporter` | `com.lince.observer.data.transfer` | strategy interfaces: `descriptor()`, `supports(fileName)`, `importInto(ILinceProject, InputStream, ImportScope, options)` / `export(ExportSelection, options): List<ExportedFile>` |
| `ImportService` / `ExportService` | `com.lince.observer.data.service` | `types()`, `importFile(fileName, InputStream, type?, scope, options)`, `export(ExportRequest)` |

Note: existing controllers mount at `/analysis`, `/profile`, … without a prefix. `/api/...` is what the
ticket asks for; on cloud the dashboard already prefixes `/api/lince-observer`, so the same relative
path works there.

### Step 3 — REST sequences
Import:

| Verb | Path | Body / params | Response |
|------|------|---------------|----------|
| GET | `/api/import/types` | — | `List<ImportTypeDescriptor>` (id, label, extensions, options) |
| POST | `/api/import` | multipart `file`; optional `type` (`lince-plus`, `lince1-register`, `lince1-tool`, `hoisan`); optional `scope` (`full` default, `observations`); optional `options.*` | `ImportResult`; 400 when the type cannot be detected or the file is unreadable; 415 when the extension and the given `type` disagree |

Detection order: explicit `type` → extension (`.xml`→`lince-plus`, `.rlince`→`lince1-register`,
`.ilince`→`lince1-tool`, `.mdb`→`hoisan`) → 400 asking for `type`. `scope` only applies to `lince-plus`.

Export:

| Verb | Path | Body / params | Response |
|------|------|---------------|----------|
| GET | `/api/export/types` | — | `List<ExportTypeDescriptor>` |
| POST | `/api/export` | `ExportRequest` JSON: `type`, `researchId?`, `criteriaIds[]`, `options{}` | the file with its MIME type and `Content-Disposition: attachment`; `multiFile` types return `application/zip` |

Dashboard sequence for an export: `GET /api/export/types` → form built from the descriptor's options →
criteria from the existing `GET /observationTool/getAll`, research from `GET /profile/get` →
`POST /api/export` → browser saves the blob. For an import: `<input type=file>` → `POST /api/import` →
show `ImportResult` and refresh the existing data hooks. The two `GET /types` calls are the only
discovery the FE needs; drop them if the dashboard prefers a hardcoded list.

### Step 4 — Desktop implementation (lince-desktop)
- `spring/service/DesktopImportService` — `@Service @DesktopQualifier`. Detects the type, picks the
  `LinceImporter`, applies the result to the live project through the same calls
  `LinceFileHelperFx.addBaseProjectInfo` / `addObservations` make (`DataHubService`,
  `AnalysisService`, `CategoryService`, `ProfileService`), then `reloadResearchProfileDataFromRegister()`.
- `spring/service/DesktopExportService` — resolves the current `ILinceProject` (the
  `getCurrentProjectDetails()` logic moves out of `RootLayoutController` into a `CurrentProjectProvider`),
  builds `ExportSelection`, dispatches to the `LinceExporter`, zips when `multiFile`.
- `ImportControllerImpl`, `ExportControllerImpl` — `@RestController @CrossOrigin`; 200/400/404/415/500
  mapping and logging like `InfoControllerImpl`.
- `RootLayoutController` — `handleImportLincePlusProject`, `handleImportLincePlusObserver`,
  `handleImportRegisterLince1`, `handleImportToolLince1`, `handleImportHoisan` keep their `FileChooser`
  and call `ImportService.importFile(...)`; `handleExport*` keep `GenericExportComponent` for the
  criteria panel but its `executeExport` calls `ExportService.export(...)` and writes the returned bytes.
  `doImport`/`doExport`/`ensure*Compatibility` go away.
- `application.properties`: multipart limits for large `.xml`/`.mdb` files.

### Cloud (lince-server, documented only)
Implement `ImportService`/`ExportService` with `@CloudQualifier` over the stored project, add both
`*ControllerImpl`, whitelist `/api/import/**` and `/api/export/**` behind the normal auth.

## Dependencies
- LO-343 pattern (`InfoController`, `InfoControllerImplTest`).
- `LinceFileHelper`, `LinceFileImporter`, `LinceCsvExporter`, `Lince2ThemeExport` (lince-data).
- `LegacyConverterService` (lince-math); `Registro`, `InstrumentoObservacional`, `HoisanTool`,
  `GseqExporterFactory`, `ExportarInstrumentoObservacionalTheme` (lince-data-fx).
- Spring multipart (already in `spring-boot-starter-web`).
- Dashboard repo: Import/Export pages (follow-up), reusing the quantitative-import upload component.

## Risks & Mitigations
| Risk | Impact | Mitigation |
|------|--------|------------|
| Legacy code reads the `Registro` / `InstrumentoObservacional` singletons and pops `JOptionPane`s | High | Step 1 extracts the conversion into classes that receive their inputs; singleton access is confined to `LegacyBridge`; dialogs are replaced by exceptions |
| HOISAN import/export goes through `.mdb` files on disk (`HoisanTool` takes `File`) | Med | Importer/exporter spool the stream to a temp file and delete it; documented in the descriptor |
| Modern `Criteria` ids vs legacy `Criterio` for criteria selection | Med | `LegacyBridge` keeps a name/code map built during conversion; unknown ids → 400 |
| `lince-data-fx` importers/exporters still compile against JavaFX | Low | They only use the legacy model packages; a test asserts no `javafx.`/`javax.swing` imports in the new classes |
| Two code paths (menu, REST) drifting | Med | Menu handlers delegate to the same services; tests live at the importer/exporter level |
| Large uploads | Low | multipart limits; stream to the importer, no double buffering |

## Open Questions
- `GET /api/import/types` and `GET /api/export/types`: keep as discovery for the FE, or hardcode the lists
  in the dashboard and drop them?
- Legacy types (Lince 1, HOISAN, Theme 5, SAS, SDIS-GSEQ) in the first MR, or ship `lince-plus` import +
  `csv`/`theme6` export first and the legacy extraction in a second MR on the same branch?
- Multi-file exports (Theme 5, Theme 6): zip (proposed) or JSON manifest with base64 content?
- Which dashboard page/hook does the quantitative import use, so the FE ticket reuses its upload component?

## Implementation notes (2026-10-21)
Deviations from the plan above, as built:

- **`LegacyBridge` lives in `lince-data-fx`** (`com.lince.observer.data.transfer.legacy`), not `lince-math`: `lince-math` cannot see the legacy `Registro`/`InstrumentoObservacional` model. It ports the `LegacyConverterService` rules but works on the project passed in, and serialises access to the legacy singletons with one class-wide lock.
- **Legacy fixes made while extracting** (stage 1B): `FilaRegistro.getDatosMixtos` fixed; HOISAN reader fixes in `HoisanTool`/`HoisanVars`; the HOISAN `.mdb` template is read from the classpath instead of a working-directory path.
- **JavaFX behaviour changes**:
  - every export panel calls `ExportService` (the same path as `POST /api/export`) instead of the legacy singletons, so the panels list the project's modern criteria and need no `ensureExportCompatibility()`;
  - Theme 6 honours the panel selection and the chosen observer;
  - CSV needs at least one criterion; the selected time columns travel as the `timeColumns` option;
  - an unknown observer is an error (`UNKNOWN_RESEARCH`) instead of a silent fallback;
  - Theme 5 writes the `.csv` and `.vvt` together from one button;
  - SAS offers criteria only (a converted project has no Lince 1 fixed/mixed nodes);
  - Lince 1 and HOISAN exports use a JavaFX save `FileChooser`, with no Swing dialog.
  - HOISAN import creates one observer per video, named after the video; the menu used to migrate only the last video as "Lince v1 observer";
  - a SAS export of an empty register yields an empty file, where the panel refused;
  - Lince 1 sub-categories are dropped with a warning (pre-existing converter limitation);
  - `.rlince` bytes can differ between two conversions of the same project because `Criterio` defines `equals` without `hashCode` (pre-existing, the menu behaves the same).
- **Import scope** only applies to `lince-plus`; every other type is imported with `full` whatever the request says.
- **Removed legacy commands**: once the menu went through the new classes, `SaveRegistroAs`, `SaveInstrumentoObservacionalAs`, `LoadInstrumentoObservacional`, `AbrirImportarHoisan`, `OldAbrirImportarHoisan` and `AbrirExportarHoisan` had no callers and were deleted, as were `CsvExportRegisterCommand` and the five `RegistroSdisGseq*Export` panels. The dead Swing export layer went too: the `legacy/exportar` package (`AbrirExportarCsv` and the eight `Exportar*Panel`), the `legacy/commands/exportar` package (`ExportarInstrumentoObservacionalTheme`, `ExportarRegistroCsv`, `ExportarRegistroSas`, the five `ExportarRegistroSdisGseq*`, `ExportarRegistroTheme5`), `NewRegistro`, `SaveRegistro` and the whole `legacy/commands/instrumento` package (`AddCategoria`, `AddCriterio`, `MoveRuleAction`, `NewInstrumentoObservacional`, `RemoveCategoria`, `RemoveCriterio`), together with the 26 i18n keys only they used. `LoadRegistro` stays: `CalcularKappa` (Kappa agreement panel) still calls it. The JavaFX panels under `data/javafx/` remain the menu UI.
- **File names per type** (REST attachment names; the menu writes the first file to the chosen name and siblings next to it): `lince-project.xml`, `lince-export.csv`, `theme6.txt` + fixed `vvt.vvt` (overwrite prompt in the menu), `theme5.csv` + `theme5.vvt` (sibling follows the chosen base name), `sas.sds`, `gseq-<format>.sds`, `hoisan.mdb`, `register.rlince`, `tool.ilince`. Multi-file results are zipped by the controller as `<type>-export.zip`.
- **`INVALID_OPTION`**: new `TransferException.Reason`, thrown for unknown CSV `separator`/`timeColumns` values and a non-positive or non-numeric SDIS-GSEQ `intervalSeconds`; the controllers answer 400 with `reason: invalid_option` and the option key in `ids`.
- **Charset**: text exports keep the platform default charset (`String.getBytes()`), so they stay byte-identical to what the menu wrote before; Lince 1 files (`.rlince`, `.ilince`) are XML and always UTF-8.
- **Cloud**: in this MR `lince-server` depends on `lince-data-fx` excluding `org.openjfx`, with a headless test (`HeadlessTransferTest`, surefire `java.awt.headless=true`) proving the transfer classes load without JavaFX. Follow-up: move the legacy model and the transfer packages into a JavaFX-free `lince-legacy` module.

