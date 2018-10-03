#!/bin/sh

DEPLOYMENT_REPO_DIR=$1

sbt dist
TMP_DIR=$(mktemp -d -t run)
unzip -d $TMP_DIR target/universal/reciplay-*.zip 
mv $TMP_DIR/reciplay-* run
rmdir $TMP_DIR
rm -f run/bin/*.bat 
rm -rf "${DEPLOYMENT_REPO_DIR}/run"
mv run "${DEPLOYMENT_REPO_DIR}/"


