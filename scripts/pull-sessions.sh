#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")/.."
adb="${ADB:-$HOME/Android/Sdk/platform-tools/adb}"

mkdir -p spike-data
"$adb" pull /sdcard/Android/data/com.vadem.setbeat/files/sessions spike-data/
ls -1 spike-data/sessions
