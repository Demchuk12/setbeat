#!/usr/bin/env bash
set -euo pipefail

if [ $# -ne 1 ]; then
  echo "usage: $0 plan.json" >&2
  exit 1
fi

adb="${ADB:-$HOME/Android/Sdk/platform-tools/adb}"
python3 -c "import json, sys; json.load(open(sys.argv[1]))" "$1"
"$adb" push "$1" /sdcard/Android/data/com.vadem.setbeat/files/plan.json
"$adb" shell am force-stop com.vadem.setbeat
echo "plan uploaded, reopen SetBeat on the watch"
