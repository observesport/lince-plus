# Outcome: LO-349 — import/export over REST (frontend handover)

As of 2026-10-22. Branch `feat/lo-349-importexport`, draft PR https://github.com/observesport/lince-plus/pull/153.
The two endpoints exist in **lince-desktop** (the JavaFX app's embedded server). The JavaFX menu entries
(Data > Import / Export) go through the same services, so what the menu produces is what the API produces.
Verified by hand in the running app on 2026-10-22; 98 + 183 + 90 unit tests green (lince-data, lince-data-fx, lince-desktop).
Not yet in **lince-server** (cloud): the dashboard gets a 404 there until the follow-up ticket lands.

## Base URL and conventions

- The dashboard is served by the same Spring server, so use relative URLs (`/api/import`, `/api/export`).
  The server port is random; the launch page and the QR code show `http://<ip>:<port>`. In the Vite dev
  server, proxy `/api` to the backend port like the other endpoints.
- CORS is open (`@CrossOrigin`); `Content-Disposition` is exposed for the export download.
- All ids are the project's UUIDs as strings. Type ids are lower-case kebab-case strings (table below).
- Type labels are English for the three lince-data types and follow the server locale for the legacy ones.
- Text exports use the server's platform charset (byte-identical with the files the JavaFX menu writes);
  the `Content-Type` of text files carries `; charset=<name>`.
- Upload limits: 50 MB per file, 55 MB per request.

## Import: `POST /api/import`

Multipart form fields:

| Field | Required | Meaning |
|---|---|---|
| `file` | yes | the uploaded file; the original file name's extension drives type detection |
| `type` | no | force a type id; if it contradicts the extension → 415 `type_mismatch` |
| `scope` | no | `full` (default) or `observations` |
| `options.<key>` | no | per-type options (none defined today) |

Type detection by extension: `.xml` → `lince-plus`, `.rlince` → `lince1-register`, `.ilince` → `lince1-tool`,
`.mdb` → `hoisan`. Unknown extension and no `type` → 400 `unknown_type`.

Scope (only `lince-plus` honours it, the legacy types always import as a full replacement and echo the scope):

- `full` = "Import Lince PLUS project": replaces the instrument and the registers with the file's, rebuilds the
  user profiles from the registers. The research profile (fps, names) is kept; videos are never imported.
- `observations` = "Import observer": keeps the instrument, appends the file's registers with fresh ids and
  registers each observer. Categories whose code is not in the loaded instrument are dropped and reported as
  warnings. Requires a loaded instrument → 400 `instrument_required` otherwise.

Legacy types: `lince1-register` replaces the register (and the instrument it carries), `lince1-tool` replaces the
instrument (sub-categories are dropped with a warning), `hoisan` creates one observer per video found in the database.

Response `200` `ImportResult`:

```json
{ "type": "lince-plus", "scope": "full", "criteriaCount": 4, "registerCount": 1,
  "observationCount": 20, "profileCount": 1, "warnings": [] }
```

`GET /api/import/types` → `ImportTypeDescriptor[]`; `GET /api/import/types/{id}` → one descriptor or 404.

```json
{ "id": "lince-plus", "label": "Lince PLUS project", "extensions": ["xml"], "options": [] }
```

## Export: `POST /api/export`

JSON body `ExportRequest`:

| Field | Required | Meaning |
|---|---|---|
| `type` | yes | export type id |
| `researchId` | no | observer (register) UUID; omitted → the currently selected observer, or the first one |
| `criteriaIds` | no | criteria UUIDs to include; empty/omitted = all criteria (types with `supportsCriteriaSelection`) |
| `options` | no | `{ key: value }` strings, keys from the descriptor's `options` |

Response `200` is the file itself: `Content-Type` from the descriptor, `Content-Disposition: attachment; filename=...`.
Types with `multiFile: true` answer a zip named `<type>-export.zip` (`application/zip`) with every file inside.

`GET /api/export/types` → `ExportTypeDescriptor[]`; `GET /api/export/types/{id}` → one descriptor or 404.

```json
{ "id": "csv", "label": "CSV", "extension": "csv", "mimeType": "text/csv", "supportsCriteriaSelection": true,
  "multiFile": false, "options": [
    { "key": "separator", "label": "Separator", "type": "enum", "defaultValue": "semicolon", "values": ["comma", "semicolon"], "nullable": false },
    { "key": "timeColumns", "label": "Time columns", "type": "string", "defaultValue": "<comma-separated column ids>", "nullable": false } ] }
```

`TransferOption.type` is one of `boolean`, `enum`, `string`, `integer`; `values` is present for `enum`;
`defaultValue` is always a string (or absent when `nullable`). Build the options form from the descriptor,
never hard-code it.

### Export types

| id | label | file | criteria selection | multiFile | options |
|---|---|---|---|---|---|
| `lince-plus` | Lince PLUS project | `.xml` | no | no | |
| `csv` | CSV (Excel) | `.csv` | yes | no | `separator` = `comma` or `semicolon` (default `semicolon`); `timeColumns` = comma-separated time column ids to include (default: all; empty string = none, but at least one criterion or one time column is required) |
| `theme6` | Theme 6 | `.txt` + `vvt.vvt` | yes | yes (zip) | |
| `theme5` | Theme 5 | `.csv` + `.vvt` | yes | yes (zip) | |
| `sas` | SAS | `.sds` | yes | no | |
| `sdis-gseq-event` | SDIS-GSEQ event | `.sds` | yes | no | |
| `sdis-gseq-state` | SDIS-GSEQ state | `.sds` | yes | no | |
| `sdis-gseq-interval` | SDIS-GSEQ interval | `.sds` | yes | no | `intervalSeconds` (integer, nullable) |
| `sdis-gseq-multievent` | SDIS-GSEQ multi-event | `.sds` | yes | no | |
| `sdis-gseq-timed` | SDIS-GSEQ timed event | `.sds` | yes | no | |
| `hoisan` | HOISAN | `.mdb` | no | no | |
| `lince1-register` | Lince 1 register | `.rlince` | no | no | |
| `lince1-tool` | Lince 1 tool | `.ilince` | no | no | |

Always read the list from `GET /api/export/types`; the table is a snapshot of the current build.
Theme 6 exports the selected observer only (the old menu exported every observer).

## Errors

4xx answers carry `{ "reason": "<code>", "message": "<text>", "ids": [..] }` (`ids` only when the error points at
specific ids: unknown criteria, invalid option key). 500 answers have an empty body.

| HTTP | reason | when |
|---|---|---|
| 400 | `unknown_type` | type id not known, or extension not recognised on import |
| 400 | `unreadable` | the file could not be parsed, or is empty |
| 400 | `instrument_required` | `scope=observations` into a project without an instrument |
| 400 | `empty_selection` | nothing to export (no observations, or CSV with no column at all) |
| 400 | `unknown_criteria` | a `criteriaIds` entry is not in the project; `ids` lists them |
| 400 | `unknown_research` | `researchId` has no register |
| 400 | `invalid_option` | option value rejected; `ids` names the key |
| 400 | `invalid_scope` | `scope` is not `full` or `observations` |
| 400 | `invalid_request` | binding failure: missing `file` part, malformed JSON, non-UUID `researchId` |
| 404 | (no body) | `GET .../types/{id}` with an unknown id |
| 415 | `type_mismatch` | `type` contradicts the file extension |
| 500 | `io` (empty body) | server-side I/O failure |

## Examples

```bash
curl -F file=@project.xml http://localhost:PORT/api/import
curl -F file=@observer.xml -F scope=observations http://localhost:PORT/api/import
curl -F file=@tool.ilince -F type=lince1-tool http://localhost:PORT/api/import
curl -OJ -X POST http://localhost:PORT/api/export -H 'Content-Type: application/json' \
     -d '{"type":"csv","options":{"separator":"comma"}}'
curl -OJ -X POST http://localhost:PORT/api/export -H 'Content-Type: application/json' \
     -d '{"type":"theme6","researchId":"<uuid>","criteriaIds":["<uuid>","<uuid>"]}'   # → theme6-export.zip
```

```ts
export type TransferOption = { key: string; label: string; type: 'boolean' | 'enum' | 'string' | 'integer';
  defaultValue?: string; values?: string[]; nullable: boolean };
export type ImportTypeDescriptor = { id: string; label: string; extensions: string[]; options: TransferOption[] };
export type ExportTypeDescriptor = { id: string; label: string; extension: string; mimeType: string;
  supportsCriteriaSelection: boolean; multiFile: boolean; options: TransferOption[] };
export type ImportResult = { type: string; scope: 'full' | 'observations'; criteriaCount: number;
  registerCount: number; observationCount: number; profileCount: number; warnings: string[] };
export type ExportRequest = { type: string; researchId?: string; criteriaIds?: string[]; options?: Record<string, string> };
export type TransferError = { reason: string; message: string; ids?: string[] };

export async function importFile(file: File, scope?: 'full' | 'observations', type?: string): Promise<ImportResult> {
  const form = new FormData();
  form.append('file', file);
  if (scope) form.append('scope', scope);
  if (type) form.append('type', type);
  const res = await fetch('/api/import', { method: 'POST', body: form });
  if (!res.ok) throw (await res.json().catch(() => ({ reason: 'http_' + res.status, message: res.statusText }))) as TransferError;
  return res.json();
}

export async function exportFile(request: ExportRequest): Promise<{ blob: Blob; fileName: string }> {
  const res = await fetch('/api/export', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(request) });
  if (!res.ok) throw (await res.json().catch(() => ({ reason: 'http_' + res.status, message: res.statusText }))) as TransferError;
  const disposition = res.headers.get('Content-Disposition') ?? '';
  const match = /filename\*=UTF-8''([^;]+)|filename="?([^";]+)"?/.exec(disposition);
  const fileName = decodeURIComponent(match?.[1] ?? match?.[2] ?? `${request.type}-export`);
  return { blob: await res.blob(), fileName };
}
```

Save the blob with an `<a download={fileName}>` and an object URL, as the quantitative export already does.

## Suggested frontend work

- Import page: drop zone, detected type shown from the extension (override with a select fed by `/api/import/types`),
  scope toggle "Replace project / Add observer" (lince-plus only), result card with the counts and the warnings.
- Export page: type select from `/api/export/types`, observer select (`researchId`), criteria multi-select when
  `supportsCriteriaSelection`, options form built from `options`, download button; show `TransferError.message`.
- Reload the project views after a successful import (instrument, registers and observers change).

## Follow-ups outside this ticket

- lince-server: implement `ImportService`/`ExportService`, register both controllers, whitelist `/api/import/**`
  and `/api/export/**`; later move the legacy model into a JavaFX-free module.
- Theme 6 selected-observer behaviour and the CSV default separator (semicolon in the API, comma in the old
  LinceCsvExporter) are open for product review.
- Code: unify the modern-to-legacy conversion that still exists in both `LegacyBridge` and the kappa path of
  `LegacyConverterService`.
