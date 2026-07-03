# 架构说明

## 推荐职责边界

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
- Cesium 地图接口聚合
- WebSocket 实时推送
- 调用 Java 服务
- 后续对接 Python AI 服务

Python AI 服务后续负责：

- YOLO 推理
- 视频抽帧
- 截图保存
- 识别结果发送 RabbitMQ

## 推荐通信方式

```text
前端 -> Node.js: HTTP + WebSocket
Node.js -> Java: REST
Python -> Java: RabbitMQ / REST
Java -> MinIO: 保存证据截图、视频片段
Java -> MySQL: 保存业务数据
Java / Node -> Redis: 缓存、在线状态
Java -> Temporal: 启动、查询、推进巡检工作流
```

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
