#!/usr/bin/env bash
# Start, inspect and stop the Lince PLUS desktop app (LinceAppFx) from a Claude session.
# Usage: lince.sh start [--build] [--port N] [--debug-port N] [--timeout S] | port | url | open [path] | status | logs [N] | stop
set -euo pipefail

ROOT="$(git rev-parse --show-toplevel 2>/dev/null || pwd)"
RUN_DIR="$ROOT/.claude/run"; LOG="$RUN_DIR/lince.log"; PID_FILE="$RUN_DIR/lince.pid"; PORT_FILE="$RUN_DIR/lince.port"; CP_FILE="$RUN_DIR/classpath.txt"
FX_JDK="${LINCE_FX_JDK:-$HOME/.sdkman/candidates/java/17.0.13.fx-librca}"
DEBUG_PORT="${LINCE_DEBUG_PORT:-5005}"; HTTP_PORT="${LINCE_PORT:-8349}"; TIMEOUT=240; BUILD=0

die() { echo "lince.sh: $*" >&2; exit 1; }
running() { [ -f "$PID_FILE" ] && kill -0 "$(cat "$PID_FILE")" 2>/dev/null; }
port() { [ -f "$PORT_FILE" ] && cat "$PORT_FILE"; }

cmd="${1:-status}"; shift || true
while [ $# -gt 0 ]; do case "$1" in
  --build) BUILD=1;; --port) HTTP_PORT="$2"; shift;; --debug-port) DEBUG_PORT="$2"; shift;; --timeout) TIMEOUT="$2"; shift;; *) break;; esac; shift; done

case "$cmd" in
  start)
    running && { echo "already running (pid $(cat "$PID_FILE"), port $(port))"; exit 0; }
    [ -x "$FX_JDK/bin/java" ] || die "JavaFX JDK not found at $FX_JDK (set LINCE_FX_JDK)"
    mkdir -p "$RUN_DIR"; : > "$LOG"; rm -f "$PORT_FILE"
    if [ "$BUILD" = 1 ] || [ ! -d "$ROOT/src/lince-desktop/target/classes" ]; then
      echo "building (mvn -Plocal install -DskipTests)…"; (cd "$ROOT/src" && JAVA_HOME="$FX_JDK" mvn -Plocal install -DskipTests -q); rm -f "$CP_FILE"
    fi
    if [ ! -s "$CP_FILE" ]; then
      echo "resolving the lince-desktop classpath…"
      (cd "$ROOT/src" && JAVA_HOME="$FX_JDK" mvn -Plocal -q -pl lince-desktop dependency:build-classpath -Dmdep.outputFile="$CP_FILE" >/dev/null)
      [ -s "$CP_FILE" ] || die "could not resolve the classpath"
    fi
    if lsof -nP -iTCP:"$HTTP_PORT" -sTCP:LISTEN >/dev/null 2>&1; then die "port $HTTP_PORT is already in use (another Lince? use --port)"; fi
    echo "starting LinceAppFx on http://localhost:$HTTP_PORT with JDWP on port $DEBUG_PORT, log $LOG"
    (cd "$RUN_DIR"; nohup "$FX_JDK/bin/java" \
      -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address="*:$DEBUG_PORT" \
      --add-opens java.base/sun.nio.ch=ALL-UNNAMED --add-opens java.base/java.io=ALL-UNNAMED \
      --add-opens javafx.base/com.sun.javafx.collections=ALL-UNNAMED --add-opens javafx.base/com.sun.javafx.collections=javafx.web \
      --add-opens javafx.controls/javafx.scene.control=ALL-UNNAMED --add-opens javafx.controls/javafx.scene.control.skin=ALL-UNNAMED \
      --add-opens javafx.graphics/javafx.scene.input=ALL-UNNAMED --add-opens javafx.web/javafx.scene.web=ALL-UNNAMED \
      --add-opens javafx.web/com.sun.webkit=ALL-UNNAMED --add-opens javafx.controls/com.sun.javafx.scene.control.skin=ALL-UNNAMED \
      --add-exports javafx.graphics/com.sun.javafx.application=ALL-UNNAMED --add-exports javafx.graphics/com.sun.glass.ui=ALL-UNNAMED \
      --add-opens javafx.graphics/com.sun.glass.ui=ALL-UNNAMED \
      --add-modules ALL-MODULE-PATH --add-modules javafx.controls,javafx.fxml,javafx.graphics,javafx.media,javafx.swing,javafx.web \
      -Dserver.port="$HTTP_PORT" -Dfile.encoding=UTF-8 -cp "$ROOT/src/lince-desktop/target/classes:$(cat "$CP_FILE")" \
      com.lince.observer.desktop.LinceAppFx </dev/null >"$LOG" 2>&1 & echo $! > "$PID_FILE"; disown 2>/dev/null || true)
    for i in $(seq 1 "$TIMEOUT"); do
      p=$(grep -oE "Tomcat started on port [0-9]+" "$LOG" 2>/dev/null | tail -1 | grep -oE "[0-9]+$" || true)
      if [ -n "$p" ]; then echo "$p" > "$PORT_FILE"; echo "up: http://localhost:$p/  (launch page: /desktop/index.html, API: /api/export/types, debug: localhost:$DEBUG_PORT)"; exit 0; fi
      running || { tail -30 "$LOG"; die "process exited before the server started (see $LOG)"; }
      sleep 1
    done
    die "no 'Tomcat started on port' line after ${TIMEOUT}s (see $LOG)";;
  port) p=$(port) || true; [ -n "${p:-}" ] || die "not started"; echo "$p";;
  url) p=$(port) || true; [ -n "${p:-}" ] || die "not started"; echo "http://localhost:$p/";;
  open) p=$(port) || true; [ -n "${p:-}" ] || die "not started"; open "http://localhost:$p/${1:-}";;
  status) if running; then echo "running: pid $(cat "$PID_FILE"), port $(port), log $LOG"; else echo "not running"; fi;;
  logs) tail -n "${1:-50}" "$LOG";;
  stop)
    running || { echo "not running"; rm -f "$PID_FILE" "$PORT_FILE"; exit 0; }
    pid=$(cat "$PID_FILE"); pkill -P "$pid" 2>/dev/null || true; kill "$pid" 2>/dev/null || true
    for i in 1 2 3 4 5; do kill -0 "$pid" 2>/dev/null || break; sleep 1; done
    kill -9 "$pid" 2>/dev/null || true
    pkill -f "lince-desktop/target/classes.*LinceAppFx" 2>/dev/null || true
    rm -f "$PID_FILE" "$PORT_FILE"; echo "stopped";;
  *) die "unknown command $cmd";;
esac
