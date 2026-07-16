#!/usr/bin/env bash
set -euo pipefail

VERSION="${1:-1.0.0}"
ARTIFACT_ID="rdap-java"
GROUP_PATH="com/slxca"
BUILD_DIR="build"
BUNDLE_DIR="$BUILD_DIR/central-bundle/$GROUP_PATH/$ARTIFACT_ID/$VERSION"
BUNDLE_ZIP="$BUILD_DIR/central-bundle.zip"

USERNAME="${SONATYPE_USERNAME:?SONATYPE_USERNAME not set}"
PASSWORD="${SONATYPE_PASSWORD:?SONATYPE_PASSWORD not set}"

mkdir -p "$BUNDLE_DIR"

# Copy and rename files into Maven layout
cp "$BUILD_DIR/libs/$ARTIFACT_ID-$VERSION.jar"            "$BUNDLE_DIR/"
cp "$BUILD_DIR/libs/$ARTIFACT_ID-$VERSION.jar.asc"        "$BUNDLE_DIR/"
cp "$BUILD_DIR/libs/$ARTIFACT_ID-$VERSION-sources.jar"    "$BUNDLE_DIR/"
cp "$BUILD_DIR/libs/$ARTIFACT_ID-$VERSION-sources.jar.asc" "$BUNDLE_DIR/"
cp "$BUILD_DIR/libs/$ARTIFACT_ID-$VERSION-javadoc.jar"    "$BUNDLE_DIR/"
cp "$BUILD_DIR/libs/$ARTIFACT_ID-$VERSION-javadoc.jar.asc" "$BUNDLE_DIR/"
cp "$BUILD_DIR/publications/mavenJava/pom-default.xml"     "$BUNDLE_DIR/$ARTIFACT_ID-$VERSION.pom"
cp "$BUILD_DIR/publications/mavenJava/pom-default.xml.asc"  "$BUNDLE_DIR/$ARTIFACT_ID-$VERSION.pom.asc"
cp "$BUILD_DIR/publications/mavenJava/module.json"          "$BUNDLE_DIR/$ARTIFACT_ID-$VERSION.module"
cp "$BUILD_DIR/publications/mavenJava/module.json.asc"      "$BUNDLE_DIR/$ARTIFACT_ID-$VERSION.module.asc"

# Generate .md5 and .sha1 checksums for every file
for f in "$BUNDLE_DIR"/*; do
  [ -f "$f" ] || continue
  base="${f##*/}"
  # skip existing checksum files
  case "$base" in *.md5|*.sha1) continue ;; esac
  if command -v md5sum &>/dev/null; then
    md5sum "$f" | cut -d' ' -f1 > "$f.md5"
    sha1sum "$f" | cut -d' ' -f1 > "$f.sha1"
  else
    md5 -q "$f" > "$f.md5"
    shasum -a 1 "$f" | cut -d' ' -f1 > "$f.sha1"
  fi
done

# Create ZIP from bundle directory (cd into build/central-bundle to get clean paths)
cd "$BUILD_DIR/central-bundle"
zip -r "../central-bundle.zip" .
cd ../..

echo "Bundle created: $BUNDLE_ZIP"
echo "Contents:"
unzip -l "$BUNDLE_ZIP"

# Build auth header: Bearer <base64(username:password)>
AUTH="Bearer $(printf "%s" "$USERNAME:$PASSWORD" | base64)"

echo "Uploading bundle to Central Portal ..."
HTTP_CODE=$(curl -s --request POST \
  -H "Authorization: $AUTH" \
  --form bundle=@"$BUNDLE_ZIP" \
  -o /tmp/central-response.json \
  -w "%{http_code}" \
  "https://central.sonatype.com/api/v1/publisher/upload?publishingType=AUTOMATIC")

RESPONSE=$(cat /tmp/central-response.json)

if [ "$HTTP_CODE" = "200" ] || [ "$HTTP_CODE" = "201" ] || [ "$HTTP_CODE" = "202" ] || [ "$HTTP_CODE" = "204" ]; then
  echo "Upload successful: HTTP $HTTP_CODE"
  echo "Deployment ID: $RESPONSE"
else
  echo "Upload failed: HTTP $HTTP_CODE"
  echo "Response: $RESPONSE"
  exit 1
fi
