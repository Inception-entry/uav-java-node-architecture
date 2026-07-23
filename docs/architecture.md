# 架构说明

## 推荐职责边界

Nginx 负责：

- 域名入口和 HTTP/HTTPS 终止
- Vue/Cesium 静态资源托管与缓存
- 将 `/api/` 和 `/socket.io/` 转发给 Spring Cloud Gateway
- 上传大小、代理超时和基础安全响应头

Spring Cloud Gateway 负责：

- JWT 鉴权、issuer/audience 校验与 Keycloak 角色解析（默认开启）
- Node BFF API 与 WebSocket 服务路由
- 基于 Redis 的请求限流
- 统一请求 ID、访问日志和网关指标
- 将可信用户 ID、用户名和角色传给 BFF

Java 负责：

- 用户权限
- 巡检任务
- Temporal Workflow 编排巡检生命周期
- 设备管理
- 告警事件
- 证据链
- 数据一致性
- 审计日志

Node.js 负责：

- 前端 BFF 聚合
- 页面接口的数据转换与前端业务编排
- Cesium 地图接口聚合
- WebSocket 实时推送
- Socket.IO 握手 JWT 验签、角色校验和 Token 到期断连
- 调用 Java 服务
- 透传 Java 返回的 SSE AI 实时输出

Python AI 服务负责：

- LangChain + Ollama 本地对话和任务分析
- 使用 LangChain `astream` 输出 SSE Token
- Qdrant 文档向量化、语义检索和来源追踪
- YOLO 推理
- 视频抽帧
- 截图保存
- 识别结果发送 RabbitMQ

## 推荐通信方式

```text
浏览器 -> Nginx: HTTPS + 静态资源
Nginx -> Spring Cloud Gateway: HTTP + WebSocket
Spring Cloud Gateway -> Node.js BFF: HTTP + WebSocket
Node.js -> Java: REST
Python -> Java: RabbitMQ / REST
Java -> Python AI: REST（聊天、知识文档管理和检索）
Python AI -> Java -> Node -> Gateway -> Nginx -> Vue: SSE 流式 Token
Python AI -> Qdrant: 文档向量和元数据
Java -> MinIO: 保存证据截图、视频片段
Java -> MySQL: 保存业务数据
Java / Node -> Redis: 缓存、在线状态
Java -> Temporal: 启动、查询、推进巡检工作流
```

交互式 `/chat` 使用独立 SSE 链路，以避免把每个模型 Token 写入
Temporal Workflow 历史。原有同步分析接口继续由 Temporal 编排并返回完整
结果，负责可靠重试和可追踪执行。

两条分析通道都会在模型完整返回后由 Java 服务写入
`ai_analysis_result` 表。Temporal 通道使用 Workflow ID 作为唯一
`analysis_id`，SSE 通道使用独立 UUID；因此 Activity 重试不会产生
重复记录，连接中断或模型失败也不会保存为成功结果。

## 网关预置配置

默认访问链路：

```text
http://localhost:8888/api/**
  -> Nginx
  -> Spring Cloud Gateway
  -> Node BFF
  -> Java
```

Gateway 默认在宿主机 `8082` 端口开放调试入口，并提供：

- `GET /actuator/health`：健康检查。
- `/api/**`：转发到 Node BFF 的 `3000` 端口。
- `/socket.io/**`：转发到 Node 实时服务的 `3001` 端口。
- 默认每个用户或客户端 IP 每秒补充 20 个令牌，突发容量 40。

开启 JWT 鉴权前，需要先准备兼容 JWT/JWK 的身份中心，然后设置：

```dotenv
GATEWAY_SECURITY_ENABLED=true
GATEWAY_JWT_JWK_SET_URI=https://auth.example.com/realms/uav/protocol/openid-connect/certs
GATEWAY_JWT_ISSUER_URI=https://auth.example.com/realms/uav
GATEWAY_JWT_AUDIENCE=uav-web
```

无论 JWT 是否启用，Gateway 都会删除客户端传入的
`X-Authenticated-User`、`X-Authenticated-Username` 和
`X-Authenticated-Roles`。完整的本地 Keycloak 启用步骤见
[`docs/gateway.md`](gateway.md)。

Nginx 默认使用 HTTP 配置。启用 HTTPS 时：

1. 将 `frontend/nginx.https.conf.example` 复制为 `frontend/nginx.conf`。
2. 将证书分别放到 `deploy/nginx/certs/fullchain.pem` 和
   `deploy/nginx/certs/privkey.pem`。
3. 在 `deploy/.env` 配置真实 `NGINX_SERVER_NAME`、
   `GATEWAY_ALLOWED_ORIGIN` 和 `HTTPS_PORT=443`。
4. 执行 `./scripts/uav.sh rebuild frontend`。

## Temporal 融合建议

Temporal 适合承载“无人机巡检任务”的长期状态和可恢复编排，不建议替代现有的 MySQL、RabbitMQ、MinIO 或 Socket.IO。

推荐分层：

- Spring Boot 保持核心业务入口，负责创建任务、查询任务、落库、鉴权和审计。
- Temporal Worker 放在 Spring Boot 进程内或独立 Java 进程中，注册巡检工作流和活动。
- Node.js 继续做 BFF 和实时推送，不直接操作 Temporal；前端请求仍先进入 Node，再由 Node 调 Java。
- RabbitMQ 继续接收 AI 识别结果；Java 消费后可以给 Temporal Workflow 发 Signal，推进任务状态。
- MinIO 继续保存截图、视频和证据文件；Temporal 只保存证据对象 key、任务状态和重试历史。

优先落地的巡检 Workflow：

```text
创建巡检任务
  -> 分配无人机
  -> 下发航线
  -> 等待 AI 告警/完成/人工取消 Signal
  -> 保存证据链
  -> 更新任务状态
  -> 推送前端实时事件
```

Nexus 的使用时机：

- 第一阶段先使用普通 Temporal Workflow/Activity，解决任务可恢复、重试、超时和状态追踪。
- 当后续出现独立 AI 服务、任务调度服务、证据服务，并且它们也各自运行 Temporal Worker 时，再把跨服务能力抽象成 Nexus Service。
- Nexus 更适合“跨 Temporal 应用的可靠调用”，例如 `ai-detection.startAnalysis`、`evidence.archiveCase`、`flight-control.dispatchRoute`。
