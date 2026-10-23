---
name: run-lince
description: Start the Lince PLUS JavaFX desktop app (LinceAppFx) from a Claude session on a fixed port with a debugger attached, open the dashboard or the launch page in the browser to test, hit its REST API, read its log, and stop it. Use whenever asked to run, start, launch, debug or open the app, to check a change in the real app, or to call /api/import, /api/export or /info against it.
---

# run-lince — start the desktop app with a fixed port and debug support

One script does everything: `.claude/skills/run-lince/scripts/lince.sh` (call it with the path from the checkout or
worktree root; state lives in `.claude/run/`, git-ignored).

## Flow

1. **Start** (builds first when `src/lince-desktop/target/classes` is missing; pass `--build` after code changes):
   ```bash
   .claude/skills/run-lince/scripts/lince.sh start                         # reuse compiled classes
   .claude/skills/run-lince/scripts/lince.sh start --build                 # mvn -Plocal install -DskipTests, then start
   .claude/skills/run-lince/scripts/lince.sh start --port 8350 --debug-port 5006   # when 8349 or 5005 are busy
   ```
   It launches `com.lince.observer.desktop.LinceAppFx` directly with the Liberica JavaFX JDK
   (`~/.sdkman/candidates/java/17.0.13.fx-librca`, override with `LINCE_FX_JDK`), the JVM flags of the IDEA run
   configuration, the module classpath resolved once by Maven (`.claude/run/classpath.txt`, refreshed by `--build`),
   `-Dserver.port=8349` (override with `--port` or `LINCE_PORT`) and JDWP on `localhost:5005` (`suspend=n`, override
   with `--debug-port` or `LINCE_DEBUG_PORT`). The JVM is detached, so the call returns as soon as
   `Tomcat started on port <N>` appears in `.claude/run/lince.log` (about 10 s) and prints the URLs. The JavaFX
   window opens on the user's screen. Without the skill the app picks a random port (`server.port=0`); with it the
   app is always at **http://localhost:8349/**.
2. **Port / URL**: `http://localhost:8349/` by default; `lince.sh port` and `lince.sh url` read the port actually
   bound from `.claude/run/lince.port` in case it was overridden.
3. **Open in the browser**: prefer the Chrome tools (`mcp__claude-in-chrome__navigate` to `http://localhost:8349/`
   for the React dashboard, `http://localhost:8349/desktop/index.html` for the launch page the JavaFX window embeds);
   `lince.sh open [path]` uses the system browser instead. API checks with curl: `curl -s localhost:8349/info`,
   `curl -s localhost:8349/api/export/types | jq`, `curl -F file=@project.xml localhost:8349/api/import`.
4. **Debug**: attach IDEA "Remote JVM Debug" to `localhost:5005`; the app keeps running (`suspend=n`).
5. **Logs**: `lince.sh logs [N]` (default 50 lines) or read `.claude/run/lince.log` (Spring console log; the app's own
   `LinceDesktopV3.log` is written in `.claude/run/` too); `lince.sh status`.
6. **Stop when done**: `lince.sh stop` (kills the JVM and anything it spawned). Always stop before a reactor build,
   since the running JVM reads `target/classes`.

## Notes

- Only one instance per checkout: `start` refuses when the pid file is alive or when the HTTP port is already bound.
- Never launch the JVM any other way from a tool call that stays attached to it: the harness then waits for the JVM
  and the call hangs. The script detaches it (`nohup`, stdin from `/dev/null`, output to the log) for that reason.
- The classpath points at the sibling modules' jars in `~/.m2`, so after changing lince-data, lince-legacy,
  lince-data-fx or lince-math use `start --build` (or run `mvn -Plocal install -DskipTests` from `src/` yourself);
  lince-desktop itself runs from `target/classes`, so a desktop-only change needs `mvn -Plocal compile -pl lince-desktop`.
- A startup failure prints the last 30 log lines; the usual causes are a missing JavaFX JDK, port 8349 or 5005
  already taken (`--port 8350 --debug-port 5006`), or stale jars in `~/.m2`.
