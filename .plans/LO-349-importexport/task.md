# Tasks: LO-349 — Import/export as a REST sequence

## Phase 1 — Setup
- [x] Create worktree `.claude/worktrees/feat-lo-349-importexport` on branch `feat/lo-349-importexport` from `develop`
- [x] Write `.plans/LO-349-importexport/{plan,spec,task}.md`
- [x] Resolve the open questions in `plan.md` (discovery endpoints, legacy types in first MR, zip vs manifest): `GET /types` kept, legacy types in this MR, zip for multi-file

## Phase 2 — Implementation

### lince-data (contract + modern importers/exporters)
- [x] `ImportScope` enum, lowercase JSON (file: `src/lince-data/src/main/java/com/lince/observer/data/bean/transfer/ImportScope.java`)
- [x] `ImportTypeDescriptor`, `ExportTypeDescriptor`, `TransferOption`, `ImportResult`, `ExportRequest`, `ExportedFile` records, `@JsonInclude(NON_NULL)` (dir: `src/lince-data/src/main/java/com/lince/observer/data/bean/transfer/`)
- [x] `LinceImporter` / `LinceExporter` strategy interfaces + `ImportException` / `ExportException` with reason codes (dir: `src/lince-data/src/main/java/com/lince/observer/data/transfer/`)
- [x] `ExportSelection`: `ILinceProject` + `researchId` + `criteriaIds` → filtered criteria and register rows (file: `src/lince-data/src/main/java/com/lince/observer/data/transfer/ExportSelection.java`)
- [x] `LinceProjectImporter` (read + merge with `ImportScope`) reusing `LinceFileHelper`/`LinceFileImporter` (file: `src/lince-data/src/main/java/com/lince/observer/data/transfer/importers/LinceProjectImporter.java`)
- [x] `LinceProjectExporter` (`lince-plus` .xml via `LinceFileHelper.writeProjectFile`), `CsvExporter` (`LinceCsvExporter`, `separator`), `Theme6Exporter` (`Lince2ThemeExport`, multiFile) (dir: `src/lince-data/src/main/java/com/lince/observer/data/transfer/exporters/`)
- [x] `ImportService` / `ExportService` interfaces (dir: `src/lince-data/src/main/java/com/lince/observer/data/service/`)
- [x] `ImportController` interface, `RQ_MAPPING_NAME = "/api/import"`, `POST ""` multipart + `GET /types` (file: `src/lince-data/src/main/java/com/lince/observer/data/controller/ImportController.java`)
- [x] `ExportController` interface, `RQ_MAPPING_NAME = "/api/export"`, `POST ""` + `GET /types` (file: `src/lince-data/src/main/java/com/lince/observer/data/controller/ExportController.java`)

### lince-data-fx / lince-math (legacy importers/exporters, headless)
- [x] `LegacyBridge`: headless `ensureExportCompatibility()` / `ensureImportCompatibility()` over `LegacyConverterService`, with `Criteria` ↔ `Criterio` id map (done in `lince-data-fx`: `src/lince-data-fx/src/main/java/com/lince/observer/data/transfer/legacy/LegacyBridge.java`)
- [x] `Lince1RegisterImporter` (.rlince) from `LoadRegistro` / `Registro.loadInstance`; `Lince1ToolImporter` (.ilince) from `LoadInstrumentoObservacional`; `HoisanImporter` (.mdb, temp file) from `AbrirImportarHoisan` / `HoisanTool.importFile` (dir: `src/lince-data-fx/src/main/java/com/lince/observer/data/transfer/importers/`)
- [x] `Theme5Exporter` (.csv + .vvt) from `ExportarRegistroTheme5` + `ExportarInstrumentoObservacionalTheme`; `SasExporter` from `ExportarRegistroSas`; `SdisGseqExporter` × `GseqFormatType` from `ExportarRegistroSdisGseq*` / `GseqExporterFactory`; `HoisanExporter` from `AbrirExportarHoisan` / `HoisanTool.exportFile`; `Lince1RegisterExporter` / `Lince1ToolExporter` from `SaveRegistroAs` / `SaveInstrumentoObservacionalAs` (dir: `src/lince-data-fx/src/main/java/com/lince/observer/data/transfer/exporters/`)
- [x] Turn each legacy `Command` into a thin wrapper: file dialog → new class → `JavaFXLogHelper.showMessage` on exception (files: `src/lince-data-fx/src/main/java/com/lince/observer/data/legacy/commands/{registro,instrumento,importar,exportar}/*.java`)
- [x] Register every importer/exporter as a Spring bean (check how `lince-data-fx` beans are picked up by `lince-desktop`'s component scan)

### lince-desktop (desktop edition)
- [x] `CurrentProjectProvider`: move `getCurrentProjectDetails()` and `getResearchSelection()` out of `RootLayoutController` (file: `src/lince-desktop/src/main/java/com/lince/observer/desktop/spring/service/CurrentProjectProvider.java`)
- [x] `DesktopImportService` `@Service @DesktopQualifier`: type detection (explicit → extension), importer dispatch, apply to live project as `LinceFileHelperFx.addBaseProjectInfo`/`addObservations` do, `reloadResearchProfileDataFromRegister()` (file: `src/lince-desktop/src/main/java/com/lince/observer/desktop/spring/service/DesktopImportService.java`)
- [x] `DesktopExportService`: `ExportSelection` build, exporter dispatch, zip for multiFile (file: `src/lince-desktop/src/main/java/com/lince/observer/desktop/spring/service/DesktopExportService.java`)
- [x] `ImportControllerImpl`, `ExportControllerImpl` `@RestController @CrossOrigin`, 200/400/404/415/500, `Content-Disposition` (dir: `src/lince-desktop/src/main/java/com/lince/observer/desktop/spring/controller/rest/`)
- [x] Multipart limits (file: `src/lince-desktop/src/main/resources/application.properties`)
- [x] `RootLayoutController`: `handleImport*` keep the `FileChooser`, call `ImportService`; `handleExport*` keep `GenericExportComponent` but `executeExport`/`executePairedExport` call `ExportService` and write the bytes; delete `doImport`, `doExport`, `ensureImportCompatibility`, `ensureExportCompatibility` (files: `src/lince-desktop/src/main/java/com/lince/observer/desktop/javafx/view/RootLayoutController.java`, `src/lince-data-fx/src/main/java/com/lince/observer/data/javafx/GenericExportComponent.java`)

### Integration stage (added 2026-10-21)
- [x] Full reactor `mvn -Plocal install` green with every module's changes together (first run needed no fixes)
- [x] JavaFX export panels go through `ExportService`: `GenericExportComponent` builds the `ExportRequest` from the selection panel and writes the returned files (`ExportTargets`); `Theme5`/`Theme6`/`Sas`/`Csv` panels plus one `SdisGseqExportComponent` replacing the five `RegistroSdisGseq*Export` (dir: `src/lince-data-fx/src/main/java/com/lince/observer/data/javafx/`)
- [x] `RootLayoutController`: Lince 1 register/tool and HOISAN exports use a save `FileChooser` + `ExportService`; delete `doExport`, `doDataIntegration`, `ensureExportCompatibility`, `ensureImportCompatibility`, the `Custom*ExportComponent` inner classes and `JavaFXLinceBaseController.openEmbedded` (no Swing left reachable from the import/export menu)
- [x] Delete `CsvExportRegisterCommand` (logic folded into `CsvExportComponent`)
- [x] Delete the legacy commands the menu no longer reaches: `SaveRegistroAs`, `SaveInstrumentoObservacionalAs`, `LoadInstrumentoObservacional`, `AbrirImportarHoisan`, `OldAbrirImportarHoisan`, `AbrirExportarHoisan`
- [x] Delete the dead Swing export layer in lince-data-fx: `legacy/exportar/`, `legacy/commands/exportar/`, `legacy/commands/instrumento/`, `NewRegistro`, `SaveRegistro`, plus the 26 i18n keys only they used (`LoadRegistro` kept for `CalcularKappa`)
- [x] `DesktopImportService`: scope only applies to `lince-plus`, other types always import `full` (test in `DesktopImportServiceTest`)
- [x] `TransferException.Reason.INVALID_OPTION` thrown by `CsvExporter` (`separator`, `timeColumns`) and `SdisGseqExporter` (`intervalSeconds`), mapped to 400 `invalid_option` with the option key in `ids`
- [x] `ExportTargetsTest` (sibling naming, fixed-name overwrite check, extension, byte-exact write)
- [x] De-duplication: `LinceDataOperationsComponent` delegates to `CurrentProjectProvider`; temp-file deletion uses `FileUtils.deleteQuietly`; `Lince2ThemeExport` keeps only the string conversion
- [x] ripwire `--quality-delta` / `--test-gate` reviewed; findings on new code fixed (duplication, error masking, parameter count); remaining rows are pre-existing code or call-graph false negatives

## Phase 3 — Testing
- [x] JSON tests for `ImportScope`, descriptors, `ExportRequest` (dir: `src/lince-data/src/test/java/com/lince/observer/data/bean/transfer/`)
- [x] `ExportSelectionTest`: all / subset / unknown ids / research filter (file: `src/lince-data/src/test/java/com/lince/observer/data/transfer/ExportSelectionTest.java`)
- [x] `LinceProjectImporterTest`: `full` vs `observations` scope against `src/lince-desktop/src/test/resources/*.xml` fixtures — AC-1
- [x] `CsvExporterTest`, `Theme6ExporterTest`, `LinceProjectExporterTest` reusing `LinceCsvExporterTest` / `Lince2ThemeExportTest` expectations — AC-4, AC-5
- [x] Legacy: `Lince1RegisterImporterTest`, `Lince1ToolImporterTest`, `HoisanImporterTest`, `Theme5ExporterTest`, `SasExporterTest`, `SdisGseqExporterTest` (reuse `GseqExporterTest`, `RegistroExportTest`, `ExportFormatComplianceTest` fixtures), `HoisanExporterTest`, `Lince1*ExporterTest` (dir: `src/lince-data-fx/src/test/java/com/lince/observer/data/transfer/`) — AC-5
- [x] `NoUiImportsTest`: new classes import nothing from `javafx.`, `javax.swing`, `java.awt` — AC-8 (one per module: lince-data, lince-data-fx)
- [x] `DesktopImportServiceTest`: detection order, 400/415 cases, scope handling (file: `src/lince-desktop/src/test/java/com/lince/observer/desktop/spring/service/DesktopImportServiceTest.java`) — AC-2
- [x] `DesktopExportServiceTest`: unknown type, empty selection rule, zip for multiFile, research resolution — AC-6
- [x] `ImportControllerImplTest`, `ExportControllerImplTest` in the style of `InfoControllerImplTest` (dir: `src/lince-desktop/src/test/java/com/lince/observer/desktop/spring/controller/rest/`) — AC-6, AC-9
- [x] `mvn -Plocal -pl lince-data,lince-data-fx,lince-math install -DskipTests` then `mvn -Plocal -pl lince-data,lince-data-fx,lince-math,lince-desktop test` (from `src/`) — AC-9
- [ ] Manual: run the app; `curl` the import and export sequences from `spec.md` AC-1..AC-6; exercise every JavaFX import/export menu entry — AC-7

## Phase 4 — Cleanup & Review
- [ ] Write the API contract section for the dashboard ticket (copy the two tables from `plan.md` into the Jira sub-task) — AC-10
- [ ] Self-review the diff against `plan.md` and `spec.md`
- [ ] Commit with conventional message and trailers
- [ ] Create MR with `/lince:gen-mr` targeting `develop`
- [ ] Open follow-ups: dashboard Import/Export pages (FE repo); `lince-server` services + controllers + whitelist
