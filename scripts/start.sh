#!/usr/bin/env bash

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

echo "构建并启动前端、后端和基础服务..."
docker compose \
  --env-file "$ROOT_DIR/deploy/.env" \
  -f "$ROOT_DIR/deploy/docker-compose.yml" \
  up -d --build

echo "启动完成，前端默认访问地址：http://localhost:8888"
