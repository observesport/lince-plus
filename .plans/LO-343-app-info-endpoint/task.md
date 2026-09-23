# Tasks: LO-343 — Application info and health endpoint

## Phase 1 — Setup
- [x] Create worktree `../lince-plus-LO-343` on branch `feat/LO-343-app-info-endpoint` from `develop`
- [x] Write `.plans/LO-343-app-info-endpoint/{plan,spec,task}.md`

## Phase 2 — Implementation
### lince-data (shared contract)
- [x] `ApplicationTarget` enum, lowercase JSON (file: `src/lince-data/src/main/java/com/lince/observer/data/bean/info/ApplicationTarget.java`)
- [x] `ApplicationInfo` DTO, `@JsonInclude(NON_NULL)` (file: `src/lince-data/src/main/java/com/lince/observer/data/bean/info/ApplicationInfo.java`)
- [x] `ApplicationHealth` DTO with `up(target)` factory (file: `src/lince-data/src/main/java/com/lince/observer/data/bean/info/ApplicationHealth.java`)
- [x] `ApplicationInfoService` interface (file: `src/lince-data/src/main/java/com/lince/observer/data/service/ApplicationInfoService.java`)
- [x] `InfoController` interface, `RQ_MAPPING_NAME = "/info"`, `GET ""` + `GET /health` (file: `src/lince-data/src/main/java/com/lince/observer/data/controller/InfoController.java`)

### lince-desktop (desktop edition)
- [x] `DesktopApplicationInfoService` `@Service @DesktopQualifier` (file: `src/lince-desktop/src/main/java/com/lince/observer/desktop/spring/service/DesktopApplicationInfoService.java`)
- [x] `InfoControllerImpl` `@RestController` (file: `src/lince-desktop/src/main/java/com/lince/observer/desktop/spring/controller/rest/InfoControllerImpl.java`)
- [x] Add `build-info` execution to `spring-boot-maven-plugin` (file: `src/lince-desktop/pom.xml`)
- [x] Exclude `**/*.properties` from the unfiltered `src/main/resources` block (file: `src/lince-desktop/pom.xml`, ~line 375)

## Phase 3 — Testing
- [x] `ApplicationInfoJsonTest`: `target` lowercase, nulls omitted (file: `src/lince-data/src/test/java/com/lince/observer/data/bean/info/ApplicationInfoJsonTest.java`)
- [x] `DesktopApplicationInfoServiceTest`: property version, `@version@` → BuildProperties fallback, unknown → null, health UP (file: `src/lince-desktop/src/test/java/com/lince/observer/desktop/spring/service/DesktopApplicationInfoServiceTest.java`)
- [x] `InfoControllerImplTest`: 200 with body, 500 on exception, for both endpoints (file: `src/lince-desktop/src/test/java/com/lince/observer/desktop/spring/controller/rest/InfoControllerImplTest.java`)
- [x] `mvn -Plocal -pl lince-data install -DskipTests` then `mvn -Plocal -pl lince-data,lince-desktop test` (from `src/`) — AC-4
- [x] `grep app.version src/lince-desktop/target/classes/application.properties` shows the real version — AC-5
- [x] `mvn -Plocal -pl lince-desktop package -DskipTests` and check `META-INF/build-info.properties` in the jar — AC-6
- [x] Manual: run the app, `curl /info` and `/info/health` — AC-1, AC-2

## Phase 4 — Cleanup & Review
- [x] Self-review the diff against `plan.md` and `spec.md`
- [ ] Commit with conventional message and trailers
- [ ] Create MR with `/lince:gen-mr` targeting `develop`
- [ ] Open the follow-up for lince-server (cloud service + controller + whitelist) referencing this plan
