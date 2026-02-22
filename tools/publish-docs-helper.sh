#!/usr/bin/env bash
set -euo pipefail

TAG="$1"
OUTDIR="${2:-site-publish}"

if [ -z "${GITHUB_REPOSITORY:-}" ]; then
  echo "GITHUB_REPOSITORY not set in environment. Expected owner/repo." >&2
  exit 1
fi

REPO_HTTP="https://github.com/${GITHUB_REPOSITORY}.git"
if [ -n "${GITHUB_TOKEN:-}" ]; then
  REPO_CLONE="https://${GITHUB_TOKEN}@github.com/${GITHUB_REPOSITORY}.git"
else
  REPO_CLONE="$REPO_HTTP"
fi

TMPDIR=$(mktemp -d)
GH_PAGES_DIR="$TMPDIR/gh-pages"

echo "Preparing temporary workspace: $TMPDIR"

set +e
git clone --branch gh-pages --single-branch "$REPO_CLONE" "$GH_PAGES_DIR" 2>/dev/null
RC=$?
set -e

if [ $RC -ne 0 ]; then
  echo "gh-pages branch doesn't exist — initializing in $GH_PAGES_DIR"
  mkdir -p "$GH_PAGES_DIR"
  git init "$GH_PAGES_DIR"
  pushd "$GH_PAGES_DIR" >/dev/null
  git checkout --orphan gh-pages || git checkout -b gh-pages
  git commit --allow-empty -m "Initialize gh-pages branch"
  git branch --set-upstream-to=origin/gh-pages gh-pages 2>/dev/null || true
  popd >/dev/null
  git clone --branch gh-pages --single-branch "$REPO_CLONE" "$GH_PAGES_DIR"
fi

if [ "$3" = "apidocs" ]; then
  # publish only aggregated apidocs content
  if [ ! -d "target/site/apidocs" ]; then
    echo "target/site/apidocs not found — ensure you ran mvn javadoc:aggregate site" >&2
    exit 1
  fi
  echo "Copying apidocs into gh-pages tree as /$TAG/"
  rm -rf "$GH_PAGES_DIR/$TAG"
  mkdir -p "$GH_PAGES_DIR/$TAG"
  # copy contents of apidocs as the site root for this tag
  cp -a target/site/apidocs/. "$GH_PAGES_DIR/$TAG/"
else
  if [ ! -d "target/site" ]; then
    echo "target/site not found — ensure you ran mvn site/javadoc:aggregate first" >&2
    exit 1
  fi
  echo "Copying site into gh-pages tree as /$TAG/"
  rm -rf "$GH_PAGES_DIR/$TAG"
  mkdir -p "$GH_PAGES_DIR/$TAG"
  cp -a target/site/. "$GH_PAGES_DIR/$TAG/"
fi

cd "$GH_PAGES_DIR"

# assemble versions list from directories (exclude .git and latest)
dirs=()
for d in *; do
  [ -d "$d" ] || continue
  if [ "$d" = ".git" ] || [ "$d" = "latest" ]; then
    continue
  fi
  dirs+=("$d")
done

# ensure TAG present
found=false
for v in "${dirs[@]}"; do
  if [ "$v" = "$TAG" ]; then
    found=true
    break
  fi
done
if [ "$found" = false ]; then
  dirs+=("$TAG")
fi

# sort versions (reverse version sort if possible)
sorted=$(printf '%s\n' "${dirs[@]}" | sort -Vr || printf '%s\n' "${dirs[@]}")

today=$(date -u +%F)
echo "[" > versions.json.tmp
first=true
while IFS= read -r v; do
  d=$(date -u -r "$v" +%F 2>/dev/null || echo "$today")
  if [ "$first" = true ]; then
    first=false
  else
    echo "," >> versions.json.tmp
  fi
  printf '  { "tag": "%s", "date": "%s", "url": "/%s/" }' "$v" "$d" "$v" >> versions.json.tmp
done <<< "$sorted"
echo "\n]" >> versions.json.tmp
mv versions.json.tmp versions.json

# render index.html from template if available
if [ -f "../site-templates/index-template.html" ]; then
  VERSIONS_HTML=''
  while IFS= read -r v; do
    VERSIONS_HTML+="    <li><a href=\"/$v/\">$v</a> <small>($today)</small></li>\n"
  done <<< "$sorted"

  TEMPLATE="../site-templates/index-template.html"
  sed "/{{VERSIONS_LIST}}/r /dev/stdin" "$TEMPLATE" > index.html <<EOF
$VERSIONS_HTML
EOF
else
  echo "<html><head><meta charset=\"utf-8\"><title>Documentation versions</title></head><body><h1>Versions</h1><ul>" > index.html
  while IFS= read -r v; do
    echo "  <li><a href=\"/$v/\">$v</a></li>" >> index.html
  done <<< "$sorted"
  echo "</ul></body></html>" >> index.html
fi

# create latest redirect to newest (first in sorted)
latest=$(printf '%s\n' "$sorted" | head -n1)
mkdir -p latest
cat > latest/index.html <<EOF
<!doctype html>
<html>
  <head>
    <meta http-equiv="refresh" content="0; url=/${latest}/">
    <link rel="canonical" href="/${latest}/">
  </head>
  <body>
    <p>Redirecting to <a href="/${latest}/">${latest}</a></p>
  </body>
</html>
EOF

echo "Staging publish dir: copying prepared gh-pages tree to output: $OUTDIR"
rm -rf "$PWD/../$OUTDIR"
mkdir -p "$PWD/../$OUTDIR"
cp -a "$GH_PAGES_DIR/." "$PWD/../$OUTDIR/"

echo "Publish staging complete at: $PWD/../$OUTDIR"
echo "Cleaning up temporary workspace"
rm -rf "$TMPDIR"

echo "Helper finished"
