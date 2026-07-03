# Temporal / Nexus 融合路线

本项目推荐先用 Temporal Workflow 承载“巡检任务生命周期”，再在服务边界稳定后引入 Nexus。

## 现阶段接入点

当前架构是：

```text
Frontend -> Node.js BFF -> Spring Boot -> MySQL / Redis / RabbitMQ / MinIO
```

接入 Temporal 后建议变成：

```text
Frontend -> Node.js BFF -> Spring Boot -> Temporal Workflow
                                      ├── MySQL: 业务查询和审计
                                      ├── RabbitMQ: AI 识别结果事件
                                      └── MinIO: 证据截图和视频
```

Spring Boot 仍然是业务入口。Temporal 不直接暴露给前端，也不替代数据库；它负责可靠编排、重试、超时、等待外部事件和恢复执行。

## Compose 服务

`deploy/docker-compose.yml` 已加入：

- `temporal`: Temporal Server，gRPC 端口 `7233`。
- `temporal-ui`: Temporal UI，默认宿主机端口 `8088`。

启动后访问：

```bash
docker compose --env-file deploy/.env -f deploy/docker-compose.yml up -d --build
```

Temporal UI：

```text
http://localhost:8088
```

Java Docker 环境会获得这些变量：

```text
TEMPORAL_TARGET_ENDPOINT=temporal:7233
TEMPORAL_NAMESPACE=default
TEMPORAL_TASK_QUEUE=uav-inspection-task-queue
```

本地开发默认连接：

```text
localhost:7233
```

## 推荐 Workflow

第一条工作流建议命名为 `InspectionWorkflow`，围绕巡检任务状态推进：

```text
startInspection(taskCode)
  -> validateTask
  -> assignDrone
  -> dispatchRoute
  -> waitForSignals
       - alarmDetected
       - evidenceUploaded
       - taskCompleted
       - taskCancelled
  -> closeTask
```

Activity 建议保持短小、幂等：

- `TaskActivities`: 校验任务、更新任务状态、写审计日志。
- `DroneActivities`: 分配无人机、下发航线。
- `EvidenceActivities`: 保存证据元数据、关联 MinIO 对象 key。
- `NotificationActivities`: 触发 Node.js 或消息队列推送。

## RabbitMQ 与 Temporal 的关系

AI 服务仍可以把识别结果发送到 RabbitMQ。Java 消费消息后不要在消费者里完成整段业务流程，而是把事件转成 Workflow Signal：

```text
RabbitMQ alarm message -> Java consumer -> InspectionWorkflow.signalAlarmDetected(...)
```

这样即使 Java 服务重启，Workflow 的等待状态和历史仍由 Temporal 维护。

## 什么时候用 Nexus

Temporal Nexus 适合跨 Temporal 应用的可靠服务调用。官方文档说明，Nexus 通过 Endpoint 暴露服务，Endpoint 会把请求路由到目标 Namespace 和 Task Queue；操作可以同步或异步执行，底层具备重试、限流、负载均衡和 Worker 恢复后的继续处理能力。

在本项目中，建议先不要把 Nexus 作为第一步。等这些能力变成独立服务后再抽象 Nexus：

- `flight-control.dispatchRoute`: 飞控/无人机调度服务。
- `ai-detection.startAnalysis`: AI 视频分析服务。
- `evidence.archiveCase`: 证据归档服务。
- `notification.broadcastAlarm`: 告警通知服务。

到那时，每个团队或服务可以拥有自己的 Namespace、Task Queue 和 Worker，Spring Boot 的巡检 Workflow 通过 Nexus 调用它们。

## Java 代码落地顺序

1. 添加 Temporal Java SDK 依赖。
2. 添加 `TemporalProperties` 读取 `app.temporal.*` 配置。
3. 注册 `WorkflowClient`、`WorkerFactory` 和 `Worker`。
4. 新建 `InspectionWorkflow` 接口和实现。
5. 新建 Activity 接口和实现，把现有任务、告警、证据逻辑逐步迁入。
6. 在 `InspectionTaskController` 增加启动工作流、查询工作流状态、取消工作流接口。
7. RabbitMQ 消费 AI 告警后给 Workflow 发 Signal。

第一版只需要一个 Namespace 和一个 Task Queue：

```text
namespace: default
taskQueue: uav-inspection-task-queue
```

生产环境再拆分 Namespace、mTLS/API Key、权限、归档和可观测性。
