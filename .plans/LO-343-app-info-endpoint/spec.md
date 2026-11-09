# Spec: LO-343 — Application info and health endpoint

## Problem Statement
The dashboard cannot show which version of Lince PLUS it is talking to because neither the desktop
embedded server nor the cloud API exposes one. `/actuator/info` is reachable but empty on both. The
desktop app already has the pom version at runtime through `app.version`, but nothing publishes it over
HTTP. A shared contract is needed so both editions expose the same shape and the dashboard can use one
fetch hook.

## Requirements

### Functional Requirements
- FR-1: `GET /info` on the desktop embedded server returns HTTP 200 with JSON
  `{"name": string, "version": string, "target": "desktop"}` plus optional `build` and `buildTime`.
- FR-2: `version` equals the Maven project version of the running build (e.g. `4.1.1-SNAPSHOT`).
- FR-3: `GET /info/health` returns HTTP 200 with `{"status":"UP","target":"desktop"}`.
- FR-4: Both endpoints need no authentication and are served under the same root as the existing REST
  controllers (`/register`, `/profile`, ...), i.e. same-origin `/info` on desktop.
- FR-5: The contract (`InfoController`, `ApplicationInfoService`, DTOs) lives in `lince-data` so
  `lince-server` can implement it with its own service and reach
  `https://my.lince-plus.com/api/lince-observer/info`.
- FR-6: `target` is serialised as lowercase `desktop` or `cloud`.
- FR-7: When the version cannot be determined the field is omitted (not `"@version@"`, not `""`).
- FR-8: On any internal failure the controller returns HTTP 500 with an empty body and logs the error,
  matching the other `*ControllerImpl` classes.

### Non-Functional Requirements
- NFR-1: No Spring Security or new dependencies added to the desktop module.
- NFR-2: The endpoints must not require the JavaFX stage or any session state; they must answer as soon as
  the embedded web server is up.
- NFR-3: `target/classes/application.properties` must contain the filtered version after `mvn compile`,
  so IDE runs and tests match the packaged jar.
- NFR-4: The `build-info` goal must not alter the repackaged jar layout or Install4J inputs.
- NFR-5: Tests run without a Spring context (JUnit 5 + Mockito), consistent with
  `AnalysisControllerImplTest`.

## Acceptance Criteria
- [ ] AC-1: `curl http://localhost:<port>/info` on a running desktop build returns 200 and a `version`
  matching `<version>` in `src/pom.xml`.
- [ ] AC-2: `curl http://localhost:<port>/info/health` returns 200 and `{"status":"UP","target":"desktop"}`.
- [ ] AC-3: `lince-data` compiles the contract types and exposes them to downstream modules
  (`InfoController`, `ApplicationInfoService`, `ApplicationInfo`, `ApplicationHealth`, `ApplicationTarget`).
- [ ] AC-4: Unit tests cover version resolution (property, placeholder fallback to `BuildProperties`,
  unknown → null), health status, and the controller's 200/500 paths. `mvn test` passes for
  `lince-data` and `lince-desktop`.
- [ ] AC-5: After `mvn compile -Plocal`, `grep app.version lince-desktop/target/classes/application.properties`
  shows the real version, not `@version@`.
- [ ] AC-6: `mvn package -Plocal -DskipTests` still produces the desktop jar; `META-INF/build-info.properties`
  is present inside it.
- [ ] AC-7: Plan documents the exact cloud steps (service, controller, whitelist) so the lince-server MR
  can be opened without re-analysis.

## Edge Cases
- `app.version` is the literal `@version@` (unfiltered classpath): treat as unknown, fall back to
  `BuildProperties`, then omit.
- `BuildProperties` bean absent (IDE run without `build-info`, or a downstream module that does not run
  the goal): `ObjectProvider` yields null; `buildTime` omitted, version from property only.
- Property present but blank: treated as unknown.
- Service throws: controller logs and returns 500 with empty body; dashboard hides the row.
- Request with credentials/CORS preflight from the dashboard dev server: `@CrossOrigin` on the controller
  answers the preflight like the other controllers.

## Out of Scope
- lince-server implementation, `ServerEndpointDefinition` and `AUTH_WHITELIST` changes.
- Dashboard hook and `LinceBrand` wiring.
- Website version string.
- Git commit sha on desktop builds.
- Changing or removing `/actuator/*`.
