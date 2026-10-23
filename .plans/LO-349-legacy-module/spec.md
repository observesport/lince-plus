# Spec: LO-349 (phase 2) — JavaFX-free legacy transfer module and import/export follow-ups

## Problem Statement

`lince-server` and any other non-JavaFX consumer can only use four of the thirteen export types and one of the four
import types, because the legacy model and the nine legacy transfer types live in `lince-data-fx`, which depends on
JavaFX. Phase 1 also left three product gaps: two independent modern-to-legacy converters that have drifted, CSV
output that breaks on separators and is formula-injectable in Excel, and a full import that silently accepts an
empty project file and wipes the loaded one.

## Requirements

### Functional Requirements

- FR-1: A Maven module `lince-legacy` contains the legacy model (`com.lince.observer.legacy.**`), the Lince 1 and
  HOISAN loaders, `LegacyBridge`, and the nine legacy importers/exporters, with package names unchanged.
- FR-2: `lince-legacy` declares no dependency on `org.openjfx`; its classes import no `javafx.*`, no `java.awt.*`
  and no `javax.swing.*` other than `javax.swing.tree`.
- FR-3: `lince-data-fx` depends on `lince-legacy`; `lince-desktop` behaviour (REST and JavaFX menu) is unchanged
  for all 13 export and 4 import types.
- FR-4: The Spring beans of the nine types are discovered by the existing `com.lince.observer` component scan in
  lince-desktop without configuration changes; a lince-server depending on lince-data + lince-legacy sees all 13
  export and 4 import types.
- FR-5: `StatsServiceImpl` kappa uses `LegacyBridge` for the modern-to-legacy conversion; `LegacyConverterService`
  no longer contains a converter.
- FR-6: Kappa results for the regression fixture are identical before and after FR-5, or any difference is
  explained and accepted in the PR.
- FR-7: CSV export quotes fields per RFC 4180 (separator, double quote, CR/LF inside a field).
- FR-8: `csv` export accepts option `excelSafe` (boolean, default `false`); when true, cells starting with
  `=`, `+`, `-`, `@`, tab or CR are prefixed with a single quote. The JavaFX "Excel" entry exports with
  `excelSafe=true`.
- FR-9: `POST /api/import` with `scope=full` and a project file holding no criteria and no observations answers
  400 `{reason: "empty_project"}` and leaves the loaded project untouched; the JavaFX "Import project" entry shows
  the same message.
- FR-10: `GET /api/export/types/csv` lists `excelSafe` among its options; `outcome.md` documents `excelSafe` and
  `empty_project`.
- FR-11: The `lince-legacy` test-jar ships the Lince 1 fixtures and fixture builders the desktop and math tests use.

### Non-Functional Requirements

- NFR-1: `mvn -Plocal install` from `src/` is green for all seven modules; CI compile/test/build unchanged apart
  from the new module.
- NFR-2: The manual snapshot deploy publishes `lince-legacy` with the other libraries (no workflow change beyond
  the module list, verified by a dry run).
- NFR-3: Text exports stay byte-identical with phase 1 for every selection that contains no field needing
  quoting (golden files unchanged).
- NFR-4: No regression in the existing 140 + 185 + 24 + 270 tests; moved tests keep passing in their new module.
- NFR-5: The desktop installer still starts and the launch page, import and export menus work (manual check).

## Acceptance Criteria

- [ ] AC-1: `src/lince-legacy` exists, builds without `org.openjfx` on the classpath, and a `NoUiImportsTest`
  over the whole module passes (FR-1, FR-2).
- [ ] AC-2: Dependency graph is `lince-data` ← `lince-legacy` ← `lince-data-fx`; `mvn dependency:tree -pl lince-legacy`
  shows no `org.openjfx` artifact (FR-2).
- [ ] AC-3: `GET /api/export/types` returns 13 types and `GET /api/import/types` returns 4, the desktop abuse suite
  (270 tests) passes unchanged except the empty-project case, and a test context with only lince-data + lince-legacy
  beans lists the same 13 and 4 (FR-3, FR-4).
- [ ] AC-4: `LegacyConverterService` has no `migrateDataToLegacy*`/`migrateRegisterToLegacy`; lince-math imports no
  `javax.swing` type; the kappa regression test passes (FR-5, FR-6).
- [ ] AC-5: CSV export of a project with a category code containing the separator, a quote and a line break
  produces a file that `univocity`/R `read.csv` parses back to the same values (FR-7).
- [ ] AC-6: With `excelSafe=true` a code `=1+1` is written as `'=1+1` (quoted as needed); with the default it is
  written raw; the JavaFX "Excel" entry output matches the `excelSafe=true` form (FR-8).
- [ ] AC-7: Uploading an empty `<linceProject/>` with `scope=full` returns 400 `empty_project`, the project is
  untouched, no spool file remains; the JavaFX menu shows the message (FR-9).
- [ ] AC-8: `outcome.md` option and error tables updated; the handover doc updated to match (FR-10).
- [ ] AC-9: `mvn -Plocal install` green; CI green on the PR; snapshot dry run lists `lince-legacy` (NFR-1, NFR-2).
- [ ] AC-10: Golden files for Theme 5, SAS, SDIS-GSEQ, Lince 1 and the CSV characterisation without special
  characters are byte-identical to phase 1 (NFR-3).

## Edge Cases

- A legacy class that logs through `JavaFXLogHelper`: replace with SLF4J before moving.
- A test in lince-data or lince-desktop reading fixtures by relative path into `lince-data-fx`: re-point to
  `lince-legacy`.
- Kappa on two registers of the same project: `LegacyBridge` installs the instrument singleton once per call;
  the second call must not invalidate the first register's references (verify by the regression test).
- `excelSafe` with a code that is only `-` or `+1`: prefixed, which is the documented trade-off; R users keep the
  default.
- Empty file with `scope=observations`: unchanged (zero counts, no error).
- A project file with criteria but no observations on `scope=full`: accepted (an instrument-only import is a
  legitimate use).

## Out of Scope

- Replacing `XMLDecoder`; removing Swing tree classes from the legacy model.
- lince-server implementation, dashboard pages, desktop API authentication/CORS, label i18n.
- Theme 6 selected-observer behaviour and the REST CSV default separator (product review, unchanged here).
