#!/usr/bin/env bash

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
ENV_FILE="$ROOT_DIR/deploy/.env"
COMPOSE_FILE="$ROOT_DIR/deploy/docker-compose.yml"

compose() {
  docker compose \
    --env-file "$ENV_FILE" \
    -f "$COMPOSE_FILE" \
    "$@"
}

require_env() {
  if [[ ! -f "$ENV_FILE" ]]; then
    echo "缺少环境变量文件：$ENV_FILE"
    echo "请先执行：cp deploy/.env.example deploy/.env"
    exit 1
  fi
}

show_help() {
  cat <<'EOF'
用法：
  uav.sh start [服务...]      启动服务，不重新构建镜像
  uav.sh rebuild [服务...]    重新构建镜像并启动服务
  uav.sh stop [服务...]       停止服务；未指定服务时关闭整套环境
  uav.sh restart [服务...]    重启现有服务
  uav.sh status               查看服务状态
  uav.sh logs [服务...]       持续查看日志
  uav.sh auth-start           单独启动本地 Keycloak
  uav.sh auth-stop            停止本地 Keycloak
  uav.sh auth-logs            查看 Keycloak 日志
  uav.sh auth-token           获取 uav-service 的 Bearer Token
  uav.sh help                 显示帮助

示例：
  uav.sh start
  uav.sh rebuild backend-ai
  uav.sh rebuild backend-java
  uav.sh rebuild gateway
  uav.sh restart temporal-ui
  uav.sh logs gateway
  uav.sh auth-start
  uav.sh auth-token
  uav.sh logs backend-ai
  uav.sh logs backend-java
EOF
}

env_value() {
  local key="$1"
  awk -F= -v key="$key" '
    $1 == key {
      sub(/^[^=]*=/, "")
      print
      exit
    }
  ' "$ENV_FILE"
}

require_service_client_secret() {
  local client_secret

  client_secret="$(env_value KEYCLOAK_UAV_SERVICE_CLIENT_SECRET)"
  if [[ -z "$client_secret" ]]; then
    echo "请先在 deploy/.env 设置 KEYCLOAK_UAV_SERVICE_CLIENT_SECRET"
    exit 1
  fi
}

require_local_keycloak_secrets() {
  local admin_password
  local dev_user_password

  admin_password="$(env_value KEYCLOAK_ADMIN_PASSWORD)"
  dev_user_password="$(env_value KEYCLOAK_DEV_USER_PASSWORD)"

  if [[ -z "$admin_password" \
    || "$admin_password" == "change-me-before-start" ]]; then
    echo "请先在 deploy/.env 设置非默认的 KEYCLOAK_ADMIN_PASSWORD"
    exit 1
  fi
  if [[ -z "$dev_user_password" ]]; then
    echo "请先在 deploy/.env 设置 KEYCLOAK_DEV_USER_PASSWORD"
    exit 1
  fi
  require_service_client_secret
}

require_env

ACTION="${1:-help}"
shift || true

case "$ACTION" in
  start)
    compose up -d "$@"
    ;;
  rebuild)
    compose up -d --build "$@"
    ;;
  stop)
    if (($# > 0)); then
      compose stop "$@"
    else
      compose down
    fi
    ;;
  restart)
    compose restart "$@"
    ;;
  status | ps)
    compose ps "$@"
    ;;
  logs)
    compose logs -f "$@"
    ;;
  auth-start)
    require_local_keycloak_secrets
    compose --profile auth up -d keycloak
    ;;
  auth-stop)
    compose --profile auth stop keycloak
    ;;
  auth-logs)
    compose --profile auth logs -f keycloak
    ;;
  auth-token)
    require_service_client_secret
    keycloak_url="$(env_value KEYCLOAK_PUBLIC_URL)"
    keycloak_url="${keycloak_url:-http://localhost:8180}"
    client_secret="$(env_value KEYCLOAK_UAV_SERVICE_CLIENT_SECRET)"
    response="$(curl --fail --silent --show-error \
      -X POST \
      "$keycloak_url/realms/uav/protocol/openid-connect/token" \
      -H "Content-Type: application/x-www-form-urlencoded" \
      --data-urlencode "grant_type=client_credentials" \
      --data-urlencode "client_id=uav-service" \
      --data-urlencode "client_secret=$client_secret")"
    token="$(python3 -c '
import json
import sys

payload = json.load(sys.stdin)
token = payload.get("access_token")
if not token:
    raise SystemExit("Keycloak 响应中没有 access_token")
print(token)
' <<<"$response")"
    printf 'Bearer %s\n' "$token"
    ;;
  help | -h | --help)
    show_help
    ;;
  *)
    echo "未知操作：$ACTION"
    echo
    show_help
    exit 2
    ;;
esac
