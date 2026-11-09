# Tasks: LO-349 (phase 2) — JavaFX-free legacy transfer module and import/export follow-ups

Branch `feat/LO-349-legacy-module` from develop, worktree `.claude/worktrees/feat-lo-349-legacy-module`
(seed `.idea/misc.xml` with the Maven link so IDEA runs LinceAppFx). Suggested split for parallel agents:
A = module move (lince-legacy + lince-data-fx + poms), B = CSV + empty import (lince-data + lince-desktop),
C = kappa unification (lince-math) after A lands. Commit by explicit pathspec once all agents are idle.

## Phase 1 — Setup
- [ ] Create `src/lince-legacy/pom.xml` (parent `lince-plus-root`, deps: lince-data, jackcess, commons-lang3,
      commons-collections4, commons-io; surefire `java.awt.headless=true`; maven-jar-plugin `test-jar` goal)
      (file: src/lince-legacy/pom.xml)
- [ ] Add `<module>lince-legacy</module>` before `lince-data-fx`; add `lince-legacy` dependency to lince-data-fx
      and its test-jar to lince-desktop/lince-math tests (file: src/pom.xml, src/lince-data-fx/pom.xml,
      src/lince-desktop/pom.xml, src/lince-math/pom.xml)
- [ ] Capture kappa regression values with the current converter on a fixture pair before any move
      (file: src/lince-math/src/test/java/com/lince/observer/math/service/KappaRegressionTest.java +
      src/lince-math/src/test/resources/kappa/expected.json)

## Phase 2 — Implementation
- [ ] `git mv` the legacy model, loaders, helpers, bundles and HOISAN template to lince-legacy (see plan table)
      (files: src/lince-legacy/src/main/java/com/lince/observer/legacy/**, .../data/legacy/{datos,plugins,utiles}/**,
      .../data/LegacyStatsHelper.java, src/lince-legacy/src/main/resources/{i18n,template}/**)
- [ ] `git mv` `transfer/legacy/**`, the three legacy importers + `LegacyImportSupport`, the six legacy exporter
      classes + `SdisGseqExporterConfiguration` + `LegacyExportSupport` (file: src/lince-legacy/src/main/java/com/lince/observer/data/transfer/**)
- [ ] Replace any `JavaFXLogHelper`/FX helper use in moved classes with SLF4J; delete `ModeloDeTablaLince` if
      unreferenced (file: moved classes)
- [ ] Move tests and fixtures; widen `NoUiImportsTest` to the whole module (javafx, java.awt, javax.swing except
      javax.swing.tree); keep `HeadlessTransferTest` (file: src/lince-legacy/src/test/**)
- [ ] Re-point fixture paths in lince-data (`LegacyXmlGuardTest`) and lince-desktop (`TransferApiHarness`,
      `FileLoaderTest`) to lince-legacy or its test-jar (file: those tests)
- [ ] Kappa: `StatsServiceImpl.calculateLegacyKappaLocked` builds both registers via
      `legacyBridge.toLegacy(new ExportSelection(project, id, List.of())).register()`; delete the converter half of
      `LegacyConverterService` (or the class) and its tests; drop `javax.swing.tree` import
      (file: src/lince-math/src/main/java/com/lince/observer/math/service/{StatsServiceImpl,LegacyConverterService}.java)
- [ ] CSV: RFC 4180 field writer in `LinceCsvExporter`; `excelSafe` option in `CsvExporter`; JavaFX
      `CsvExportComponent` passes `excelSafe=true` (file: src/lince-data/src/main/java/com/lince/observer/data/component/LinceCsvExporter.java,
      .../transfer/exporters/CsvExporter.java, src/lince-data-fx/src/main/java/com/lince/observer/data/javafx/CsvExportComponent.java)
- [ ] Empty full import: `Reason.EMPTY_PROJECT`, check in `LinceProjectImporter` FULL path, 400 mapping in
      `TransferResponses` (file: src/lince-data/src/main/java/com/lince/observer/data/transfer/{TransferException,importers/LinceProjectImporter}.java,
      src/lince-desktop/src/main/java/com/lince/observer/desktop/spring/controller/rest/TransferResponses.java)

## Phase 3 — Testing
- [ ] `NoUiImportsTest` + `mvn dependency:tree -pl lince-legacy` shows no org.openjfx; a Spring test context over
      lince-data + lince-legacy beans lists 13 export / 4 import types (AC-1, AC-2, AC-3)
- [ ] Desktop abuse suite green; flip `emptyLincePlusRootIsAcceptedAndEmptiesTheProject` to the refusal test
      (AC-3, AC-7) (file: src/lince-desktop/src/test/java/com/lince/observer/desktop/spring/controller/rest/ImportApiAbuseTest.java)
- [ ] Kappa regression test passes after the switch (AC-4)
- [ ] CSV tests: quoting round trip with separator/quote/newline inside codes; `excelSafe` on/off; golden CSV
      unchanged for plain codes (AC-5, AC-6, AC-10) (file: src/lince-data/src/test/java/com/lince/observer/data/transfer/exporters/CsvExporterTest.java)
- [ ] Full reactor `mvn -Plocal install`; CI green; snapshot dry run `mvn -Plocal deploy -DskipTests -pl '!lince-desktop' -Dmaven.deploy.skip=true` lists lince-legacy (AC-9)
- [ ] Manual: run LinceAppFx, every import/export menu entry, Excel entry output opens in Excel without formula
      evaluation (NFR-5)

## Phase 4 — Cleanup & Review
- [ ] Update `outcome.md` (option `excelSafe`, error `empty_project`, module note for lince-server) and the Claude
      Doc copy; update `CLAUDE.md` and `docs/CI.md` module lists (file: .plans/LO-349-importexport/outcome.md, CLAUDE.md, docs/CI.md)
- [ ] Record the lince-server checklist (depend on lince-legacy, services, controllers, whitelist, lock note) in
      `outcome.md` for the cloud repo
- [ ] Independent review pass (dead code, duplication, security of moved loaders) with opus subagents
- [ ] Create MR with `/gen-mr`; declare the remaining follow-ups (auth token + CORS, XMLDecoder replacement, Swing
      tree removal, label i18n, Theme 6 / separator product review)
