#!/bin/bash
set -e

NEW_VERSION=$1

if [ -z "$NEW_VERSION" ]; then
  echo "Usage: $0 <new-version>"
  exit 1
fi

echo "Updating pom.xml version to $NEW_VERSION"

# Update version in pom.xml using mvn versions plugin
mvn -B versions:set -DnewVersion="$NEW_VERSION" -DgenerateBackupPoms=false

echo "Version updated successfully to $NEW_VERSION"
