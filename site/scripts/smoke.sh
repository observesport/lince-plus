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

# Localized pages: same content, correct <html lang>, translated navigation.
for lang in es de ca; do
  check "$BASE/$lang/" 200
  expect_text "<html lang=\"$lang\"" "/$lang/ has lang=$lang"
  expect_text "releases/download/v$VERSION/" "/$lang/ links the v$VERSION installers"
  check "$BASE/$lang/changelog/" 200
  expect_text "<html lang=\"$lang\"" "/$lang/changelog/ has lang=$lang"
done
expect_text 'hreflang="x-default"' "changelog carries hreflang alternates"

check "$BASE/" 200
expect_text 'app_mode' "GA4 app_mode tagging present"

check "$BASE/privacy-mobile/" 200
expect_text "Privacy Policy" "privacy policy renders"

check "$BASE/favicon.svg" 200

# --- SEO and GEO artifacts -------------------------------------------------

check "$BASE/robots.txt" 200
expect_text "Sitemap:" "robots.txt declares a sitemap"
expect_text "GPTBot" "robots.txt addresses AI crawlers"
# The Sitemap line names the production origin, which is unreachable when a
# production build is smoke-tested from a local server (Site workflow, check job).
# Only check that it points at sitemap-index.xml; the file itself is fetched
# by path below.
if grep -qE '^Sitemap: .*/sitemap-index\.xml[[:space:]]*$' "$BODY"; then
  echo "ok    text  robots.txt Sitemap line points at sitemap-index.xml"
else
  echo "FAIL  text  robots.txt Sitemap line missing or not sitemap-index.xml"
  FAILED=$((FAILED + 1))
fi

check "$BASE/sitemap-index.xml" 200
check "$BASE/sitemap-0.xml" 200
expect_text 'hreflang=' "sitemap carries hreflang alternates"
for lang in es de ca; do
  expect_text "/$lang/" "sitemap lists the /$lang/ pages"
done
if grep -q '/404' "$BODY"; then
  echo "FAIL  text  sitemap must not list the 404 page"
  FAILED=$((FAILED + 1))
else
  echo "ok    text  sitemap excludes the 404 page"
fi

check "$BASE/llms.txt" 200
expect_text "# LINCE PLUS Desktop" "llms.txt has the product heading"
expect_text "GPL-3.0" "llms.txt states the licence"
expect_text "Current version: $VERSION" "llms.txt advertises $VERSION"
check "$BASE/llms-full.txt" 200
expect_text "api-docs" "llms-full.txt documents the local API"

check "$BASE/" 200
expect_text 'application/ld+json' "home page carries JSON-LD"
expect_text '"SoftwareApplication"' "JSON-LD declares SoftwareApplication"
expect_text '"ScholarlyArticle"' "JSON-LD declares the citations"
expect_text '"price":"0"' "JSON-LD states the product is free"
check "$BASE/changelog/" 200
expect_text '"BreadcrumbList"' "changelog carries a BreadcrumbList"

check "$BASE/this-page-does-not-exist/" 404
# The 404 body is served for unknown paths; it must ask not to be indexed.
check "$BASE/404.html" 200
expect_text 'noindex' "404 page is noindex"

if [ "$FAILED" -gt 0 ]; then
  echo "$FAILED check(s) failed"
  exit 1
fi
echo "All checks passed"
