#!/bin/bash

echo "启动基础服务..."
docker compose -f deploy/docker-compose.yml up -d

echo "启动完成"

