#!/usr/bin/env bash

set -euo pipefail

URL="${1:?用法：wait-for-http.sh <url> [timeout-seconds] [label]}"
TIMEOUT_SECONDS="${2:-180}"
LABEL="${3:-$URL}"
DEADLINE=$((SECONDS + TIMEOUT_SECONDS))
LAST_STATUS="000"

while ((SECONDS < DEADLINE)); do
  LAST_STATUS="$(
    curl --silent \
      --output /dev/null \
      --write-out "%{http_code}" \
      --max-time 5 \
      "$URL" || true
  )"

  if [[ "$LAST_STATUS" =~ ^[23][0-9][0-9]$ ]]; then
    printf '服务已就绪：%-20s HTTP %s\n' "$LABEL" "$LAST_STATUS"
    exit 0
  fi
  sleep 2
done

printf '等待服务超时：%s，最后状态 HTTP %s\n' "$LABEL" "$LAST_STATUS"
exit 1
