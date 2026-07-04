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
  uav.sh help                 显示帮助

示例：
  uav.sh start
  uav.sh rebuild backend-ai
  uav.sh rebuild backend-java
  uav.sh restart temporal-ui
  uav.sh logs backend-ai
  uav.sh logs backend-java
EOF
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
