# UAV Java + Node.js 架构脚手架

这是一套用于“无人机巡检 + 人员持械识别 + 告警 + 证据链”的后端基础架构脚手架。

## 项目组成

```text
uav-java-node-architecture/
├── backend-java/          # Java Spring Boot 核心业务服务
├── backend-node/          # Node.js NestJS BFF + WebSocket 服务
├── deploy/                # Docker Compose 一键部署环境
└── docs/                  # 架构和接口说明
```

## 最快启动方式

### 1. 安装基础环境

服务器建议使用 Ubuntu 22.04 / 24.04。

```bash
sudo apt update
sudo apt install -y git curl vim
curl -fsSL https://get.docker.com | sudo bash
sudo usermod -aG docker $USER
newgrp docker
```

确认 Docker 可用：

```bash
docker version
docker compose version
```

### 2. 启动整套系统

```bash
cd deploy
cp .env.example .env
docker compose up -d --build
```

### 3. 访问服务

```text
Java 核心业务服务: http://服务器IP:8080/api/health
Node BFF 服务:     http://服务器IP:3000/api/health
WebSocket 服务:    ws://服务器IP:3001
RabbitMQ 控制台:   http://服务器IP:15672
MinIO 控制台:      http://服务器IP:9002
```

默认账号密码：

```text
RabbitMQ: admin / admin123
MinIO:    minioadmin / minioadmin123
MySQL:    root / root123456
```

## 本地开发启动

### Java

```bash
cd backend-java
mvn spring-boot:run
```

### Node.js

```bash
cd backend-node
npm install
npm run start:dev
```

## 业务调用链

```text
前端 Vue / Cesium
  ↓ HTTP / WebSocket
Node.js NestJS BFF / Realtime
  ↓ REST
Java Spring Boot Core Service
  ↓
MySQL / Redis / RabbitMQ / MinIO
```

## 已内置示例能力

Java 服务：

- 健康检查接口
- 告警事件接口
- 设备接口
- 巡检任务接口
- 统一返回结构
- 全局异常处理
- 分层结构：controller / service / repository / domain / dto
- Dockerfile

Node 服务：

- 健康检查接口
- 告警聚合接口
- 调用 Java 服务的 HttpClient
- WebSocket 网关
- 配置管理
- Dockerfile

部署环境：

- MySQL
- Redis
- RabbitMQ
- MinIO
- Java 服务
- Node.js 服务
