#!/usr/bin/env bash
# Smoke-test a deployed copy of the site.
#
#   scripts/smoke.sh <base-url>
#
#   scripts/smoke.sh http://localhost:4321/lince-plus     # after `npm run preview`
#   scripts/smoke.sh https://lince-plus-pr12.surge.sh     # CI preview
#
# Checks that every page answers, the 404 page is wired, the assets the home
# page references resolve, and the advertised version matches lince-version.json.
set -euo pipefail

BASE="${1:?usage: smoke.sh <base-url>}"
BASE="${BASE%/}"
ORIGIN="$(printf '%s' "$BASE" | sed -E 's#^(https?://[^/]+).*#\1#')"
HERE="$(cd "$(dirname "$0")" && pwd)"
VERSION="$(node -p "require('$HERE/../../lince-version.json').version")"
BODY="$(mktemp)"
trap 'rm -f "$BODY"' EXIT
FAILED=0

check() {
  local url="$1" want="$2" code
  code="$(curl -sS -L -o "$BODY" -w '%{http_code}' "$url" || echo 000)"
  if [ "$code" = "$want" ]; then
    echo "ok    $code  $url"
  else
    echo "FAIL  $code  $url (expected $want)"
    FAILED=$((FAILED + 1))
  fi
}

expect_text() {
  local needle="$1" label="$2"
  if grep -q -- "$needle" "$BODY"; then
    echo "ok    text  $label"
  else
    echo "FAIL  text  $label: '$needle' not found"
    FAILED=$((FAILED + 1))
  fi
}

echo "Smoke-testing $BASE (expecting v$VERSION)"

check "$BASE/" 200
expect_text "Download v$VERSION" "home advertises v$VERSION"
expect_text 'id="lince-logo-mark"' "logo sprite present"

# Every same-origin stylesheet, script and image the home page references.
grep -oE '(href|src)="[^"]+"' "$BODY" \
  | sed -E 's/^(href|src)="([^"]+)"$/\2/' \
  | grep -E '^/' \
  | grep -E '\.(css|js|svg|png|gif|jpe?g|webp|ico)$' \
  | sort -u \
  | while read -r path; do
      code="$(curl -sS -o /dev/null -w '%{http_code}' "$ORIGIN$path" || echo 000)"
      if [ "$code" = "200" ]; then
        echo "ok    $code  $ORIGIN$path"
      else
        echo "FAIL  $code  $ORIGIN$path"
        echo fail >> "$BODY.assets"
      fi
    done
if [ -f "$BODY.assets" ]; then
  FAILED=$((FAILED + $(wc -l < "$BODY.assets")))
  rm -f "$BODY.assets"
fi

check "$BASE/changelog/" 200
expect_text "v$VERSION" "changelog lists v$VERSION"

check "$BASE/privacy-mobile/" 200
expect_text "Privacy Policy" "privacy policy renders"

check "$BASE/favicon.svg" 200
check "$BASE/robots.txt" 200
check "$BASE/this-page-does-not-exist/" 404

if [ "$FAILED" -gt 0 ]; then
  echo "$FAILED check(s) failed"
  exit 1
fi
echo "All checks passed"
