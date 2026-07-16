#!/usr/bin/env bash
set -euo pipefail

VERSION="${1:-1.0.0}"
ARTIFACT_ID="rdap-java"
BUILD_DIR="build"

USERNAME="${SONATYPE_USERNAME:?SONATYPE_USERNAME not set}"
PASSWORD="${SONATYPE_PASSWORD:?SONATYPE_PASSWORD not set}"

URL="https://central.sonatype.com/api/v1/publisher/upload"

FILES=()
add_file() {
  local path="$1"
  if [ -f "$path" ]; then
    FILES+=("$path")
  else
    echo "Warning: $path not found, skipping"
  fi
}

# build/libs/ — correct Maven names
add_file "$BUILD_DIR/libs/$ARTIFACT_ID-$VERSION.jar"
add_file "$BUILD_DIR/libs/$ARTIFACT_ID-$VERSION.jar.asc"
add_file "$BUILD_DIR/libs/$ARTIFACT_ID-$VERSION-sources.jar"
add_file "$BUILD_DIR/libs/$ARTIFACT_ID-$VERSION-sources.jar.asc"
add_file "$BUILD_DIR/libs/$ARTIFACT_ID-$VERSION-javadoc.jar"
add_file "$BUILD_DIR/libs/$ARTIFACT_ID-$VERSION-javadoc.jar.asc"

# build/publications/ — rename to Maven names
add_file "$BUILD_DIR/publications/mavenJava/pom-default.xml"
add_file "$BUILD_DIR/publications/mavenJava/pom-default.xml.asc"
add_file "$BUILD_DIR/publications/mavenJava/module.json"
add_file "$BUILD_DIR/publications/mavenJava/module.json.asc"

if [ ${#FILES[@]} -eq 0 ]; then
  echo "No files to upload"
  exit 1
fi

echo "Uploading ${#FILES[@]} files to $URL ..."

CURL_ARGS=(-s -u "$USERNAME:$PASSWORD")
for f in "${FILES[@]}"; do
  filename=$(basename "$f")
  # POM and module files need correct Maven filenames
  case "$filename" in
    pom-default.xml) filename="$ARTIFACT_ID-$VERSION.pom" ;;
    pom-default.xml.asc) filename="$ARTIFACT_ID-$VERSION.pom.asc" ;;
    module.json) filename="$ARTIFACT_ID-$VERSION.module" ;;
    module.json.asc) filename="$ARTIFACT_ID-$VERSION.module.asc" ;;
  esac
  CURL_ARGS+=(-F "file=@$f;filename=$filename")
done
CURL_ARGS+=("$URL")

HTTP_CODE=$(curl "${CURL_ARGS[@]}" -o /tmp/central-response.json -w "%{http_code}")
RESPONSE=$(cat /tmp/central-response.json)

if [ "$HTTP_CODE" = "200" ] || [ "$HTTP_CODE" = "201" ] || [ "$HTTP_CODE" = "202" ] || [ "$HTTP_CODE" = "204" ]; then
  echo "Upload successful: HTTP $HTTP_CODE"
else
  echo "Upload failed: HTTP $HTTP_CODE"
  echo "Response: $RESPONSE"
  exit 1
fi
