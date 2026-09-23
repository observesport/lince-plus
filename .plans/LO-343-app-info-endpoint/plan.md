# Plan: LO-343 — Application info and health endpoint (desktop first, cloud-ready)

## Overview
The dashboard sidebar shows the edition and has an optional version row that stays hidden until a backend
returns a value. Neither backend exposes a version today. The desktop app already knows its version at
runtime (`app.version=@version@`, Maven-filtered, read by `PropertyLoader.getVersionNumber()`), but only
the splash screen and the log use it.

This change adds a small, unauthenticated REST surface that both editions can implement:

- `GET /info` → `{"name","version","build?","buildTime?","target":"desktop|cloud"}`
- `GET /info/health` → `{"status":"UP","target":"desktop"}`

The contract (DTOs, service interface, controller interface) lives in `lince-data`, the module that
`lince-server` already consumes for the other controllers. The desktop implementation lives in
`lince-desktop`. `lince-server` will provide its own `@CloudQualifier` service (reading `BuildProperties`
from the `build-info` goal) and its own `InfoControllerImpl`, then whitelist the path in
`ServerEndpointDefinition` / `AUTH_WHITELIST`.

## Goals
- A single contract in `lince-data` that both editions implement, following the existing
  `controller` interface + `*ControllerImpl` pattern (`RQ_MAPPING_NAME` constant, mapping annotations on
  the interface).
- A desktop implementation that returns the pom version, mounted under the same root the dashboard
  already uses for `/register/get/` (same-origin `/info`).
- A basic health check at `/info/health`.
- Unit tests for the service and the controller, in the style of `AnalysisControllerImplTest`.
- Fix the latent defect where the unfiltered resources block leaves a literal `@version@` in
  `target/classes`, so tests and IDE runs see the real version.

## Non-Goals
- The `lince-server` (cloud) implementation and its security whitelist. That is a separate repo and MR;
  this plan documents exactly what it must add.
- The dashboard fetch hook on `feat/brand-wordmark`. Separate repo.
- The website version string (Astro output hardcodes v4.0.11). Separate ticket.
- Replacing Spring Actuator. `/actuator/*` stays as it is.
- Adding a git-commit plugin to the desktop build. `build` (git sha) is optional in the DTO and stays
  null on desktop until a plugin is introduced.

## Technical Approach

### Contract (lince-data)
| Type | Package | Role |
|------|---------|------|
| `ApplicationTarget` | `com.lince.observer.data.bean.info` | enum `DESKTOP`/`CLOUD`, serialised lowercase |
| `ApplicationInfo` | `com.lince.observer.data.bean.info` | DTO: `name`, `version`, `build`, `buildTime`, `target`; nulls omitted |
| `ApplicationHealth` | `com.lince.observer.data.bean.info` | DTO: `status`, `target`; `ApplicationHealth.up(target)` factory |
| `ApplicationInfoService` | `com.lince.observer.data.service` | interface: `getApplicationInfo()`, `getHealth()` |
| `InfoController` | `com.lince.observer.data.controller` | interface, `RQ_MAPPING_NAME = "/info"`, `GET ""` and `GET /health` |

The service interface mirrors `IHelloWorldService` (one interface, one implementation per edition) and the
edition marker is the existing `LinceQualifier.DesktopQualifier` / `CloudQualifier`.

### Desktop implementation (lince-desktop)
- `spring/service/DesktopApplicationInfoService` — `@Service @DesktopQualifier`, constructor-injected
  `Environment` and `ObjectProvider<BuildProperties>`. Version resolution order:
  1. `app.version` from the environment, if set and not the unfiltered placeholder `@version@`;
  2. `BuildProperties.getVersion()` when the `build-info` goal produced `META-INF/build-info.properties`;
  3. `null` (the dashboard hides the row).
  `buildTime` comes from `BuildProperties` when present. `target` is always `DESKTOP`.
- `spring/controller/rest/InfoControllerImpl` — `@CrossOrigin @RestController
  @RequestMapping(InfoController.RQ_MAPPING_NAME)`, delegates to `ApplicationInfoService`, same
  try/catch + `ResponseEntity` style as `ProfileControllerImpl`.
- `lince-desktop/pom.xml`:
  - add the `build-info` goal to `spring-boot-maven-plugin` so `BuildProperties` is available at runtime
    (same mechanism the cloud side will use);
  - exclude `**/*.properties` from the second, unfiltered `src/main/resources` block so it stops
    overwriting the filtered `application.properties` in `target/classes`.

### Cloud implementation (lince-server, follow-up MR)
- Enable `build-info` in `lince-api/pom.xml` (spring-boot-maven-plugin at ~line 303).
- `CloudApplicationInfoService implements ApplicationInfoService`, `@Service @CloudQualifier`, reads
  `BuildProperties` (version, time) and optionally `GitProperties` for `build`.
- `InfoControllerImpl implements InfoController`, same mapping.
- Declare `/info/**` in `ServerEndpointDefinition` and append it to `AUTH_WHITELIST` (~line 35), next to
  `/actuator/info`.
- Result: `https://my.lince-plus.com/api/lince-observer/info`.

### Data flow
Dashboard `fetch(<api-root>/info)` → `InfoControllerImpl.getInfo()` → `ApplicationInfoService` →
`ApplicationInfo` JSON → `LinceBrand` `version` prop. Any non-2xx or network error keeps the row hidden.

## Dependencies
- Spring Boot 3.5.5 (`spring-boot-starter-actuator` already in lince-data and lince-desktop, which brings
  `ProjectInfoAutoConfiguration` for `BuildProperties`).
- Jackson (already transitively present in lince-data via `jackson-module-jsonSchema`).
- Follow-up tickets: lince-server endpoint + whitelist; dashboard hook on `feat/brand-wordmark`.

## Risks & Mitigations
| Risk | Impact | Mitigation |
|------|--------|------------|
| `@version@` left unfiltered in `target/classes` makes tests/IDE report a bogus version | Med | Fix the duplicate resources block; service also treats the placeholder as "unknown" and falls back to `BuildProperties`. |
| `build-info` goal changes packaging or Install4J input | Low | The goal only writes `META-INF/build-info.properties` into `target/classes`; repackage config is untouched. Verified with `mvn -Plocal package`. |
| Bean ambiguity if lince-server ever loads both implementations | Low | Each impl is qualified (`@DesktopQualifier` / `@CloudQualifier`); lince-server does not depend on lince-desktop. |
| CORS blocks the dashboard in dev | Low | `@CrossOrigin(maxAge = 3600)` as on the other REST controllers; `WebConfig` already sets global CORS. |
| Desktop JavaFX tests are heavy | Low | New tests are plain JUnit 5 + Mockito, no Spring context, like `AnalysisControllerImplTest`. |

## Open Questions
- Path name: the ticket proposal says `/version`; this plan uses `/info` + `/info/health` because the
  request was extended to "a basic health check and a basic info endpoint". If the dashboard hook is
  already written against `/version`, add a one-line alias mapping in `InfoController`.
- Should `name` come from `spring.application.name` (`LinceDesktopV3`) or `app.ui.title`? Plan uses
  `spring.application.name` with `app.ui.title` as fallback.
- Git sha for desktop builds: requires `git-commit-id-maven-plugin`; deferred.
