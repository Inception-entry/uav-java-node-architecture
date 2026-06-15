# 架构说明

## 推荐职责边界

Java 负责：

- 用户权限
- 巡检任务
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
```
