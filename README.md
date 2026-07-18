# UAV Java + Node.js 架构脚手架

面向“无人机巡检、人员持械识别、实时告警与证据链”场景的前后端架构脚手架。项目采用 Vue 3 + Cesium 构建三维可视化界面，Nginx 提供统一入口，Spring Cloud Gateway 承担鉴权、路由、限流和日志，NestJS 承担 BFF 与实时推送，Spring Boot 负责核心业务和数据持久化。

## 技术架构

```text
浏览器
  -> Nginx（域名、HTTPS、静态资源、网络代理）
  -> Spring Cloud Gateway（鉴权、路由、限流、日志）
  -> NestJS BFF（聚合、转换、前端业务编排）
  -> Spring Boot（核心业务、Temporal、MySQL）
  -> Python AI（LangChain、Ollama、RAG）
```

- **Nginx**：域名与 HTTPS 入口、静态资源托管、API/WebSocket 反向代理。
- **Gateway**：JWT/Keycloak 鉴权、服务路由、Redis 限流、可信身份传递、请求 ID、访问日志与指标。
- **Frontend**：三维地图、无人机展示、主题与语言切换。
- **Node.js**：面向前端的接口聚合、Java 服务代理、Socket.IO 告警推送。
- **Java**：告警、设备、巡检任务等核心业务、数据持久化及 Temporal 工作流编排入口。
- **Python AI**：LangChain 调用本地 Ollama，结合 MySQL 任务上下文和 Qdrant 知识库生成分析结果。
- **基础设施**：MySQL、Redis、RabbitMQ、MinIO、Temporal、Qdrant。

更详细的职责边界见 [`docs/architecture.md`](docs/architecture.md)。

## 目录结构

```text
uav-java-node-architecture/
├── backend-java/                       # Spring Boot 核心业务服务
│   ├── src/main/java/com/uav/backend/
│   │   ├── alarm/                      # 告警领域：接口、服务、实体、仓储和 DTO
│   │   ├── common/                     # 统一响应结构和全局异常处理
│   │   ├── device/                     # 设备查询示例接口
│   │   ├── health/                     # Java 服务健康检查
│   │   ├── task/                       # 巡检任务查询示例接口
│   │   └── BackendJavaApplication.java # Java 应用入口
│   ├── src/main/resources/
│   │   ├── application.yml             # 公共配置及默认 local profile
│   │   ├── application-local.yml       # 本地 H2 配置，默认端口 8081
│   │   └── application-docker.yml      # Docker MySQL 配置
│   ├── src/test/                        # Java 测试
│   ├── Dockerfile                       # Java 多阶段构建镜像
│   └── pom.xml                          # Maven 依赖与构建配置
├── backend-node/                        # NestJS BFF 与实时通信服务
│   ├── src/
│   │   ├── alarm/                       # 告警查询、创建及请求 DTO
│   │   ├── health/                      # Node 服务健康检查
│   │   ├── realtime/                    # Socket.IO 告警网关
│   │   ├── shared/                      # Java HTTP 客户端
│   │   ├── app.module.ts                # NestJS 根模块
│   │   └── main.ts                      # Node 应用入口
│   ├── Dockerfile                       # Node 多阶段构建镜像
│   ├── package.json                     # npm 脚本与依赖
│   └── tsconfig.json                    # TypeScript 配置
├── backend-ai/                          # FastAPI + LangChain + Ollama RAG 服务
├── gateway-java/                        # Spring Cloud Gateway 独立服务
│   ├── src/main/                        # 路由、安全、限流和日志配置
│   ├── Dockerfile                       # Gateway 多阶段构建镜像
│   └── pom.xml                          # Spring Cloud 依赖管理
├── frontend/                            # Vue 3 + Cesium 三维可视化前端
│   ├── src/
│   │   ├── assets/                      # 无人机模型等静态资源
│   │   ├── components/                  # Cesium、工具栏、菜单和覆盖层组件
│   │   ├── libs/cesium/                 # Cesium 初始化与 Vue 插件
│   │   ├── locales/                     # 中英文语言资源
│   │   ├── router/                      # 页面路由
│   │   ├── store/                       # Pinia 状态管理
│   │   ├── style/                       # 全局样式与变量
│   │   ├── theme/                       # 主题配置
│   │   ├── utils/                       # 语言、布局和主题工具
│   │   ├── views/                       # 首页与无人机页面
│   │   ├── App.vue                      # Vue 根组件
│   │   └── main.ts                      # 前端入口
│   ├── package.json                     # 前端脚本与依赖
│   ├── Dockerfile                       # Vite 构建与 Nginx 运行镜像
│   ├── nginx.conf                       # SPA、API 和 Socket.IO 反向代理
│   └── vite.config.ts                   # Vite 与 Cesium 配置
├── deploy/
│   ├── mysql/init/001_init.sql          # 数据库与业务表初始化脚本
│   ├── .env.example                     # Compose 环境变量模板
│   └── docker-compose.yml               # 后端及基础设施编排
├── docs/
│   ├── architecture.md                  # 服务职责与通信方式
│   ├── knowledge-base.md                # 知识库使用与 RAG 调用链
│   └── temporal-integration.md          # Temporal / Nexus 融合路线
├── scripts/
│   └── uav.sh                           # 统一管理 Compose 服务
└── README.md                            # 项目总览与运行说明
```

`target/`、`node_modules/` 等目录是本地构建产物或依赖目录，不属于源码结构。

## 环境要求

完整 Docker 部署：

- Docker
- Docker Compose

本地开发：

- JDK 17、Maven 3.9+
- Node.js 20+、npm
- 前端推荐 Node.js 22+
- Ollama，以及 `my-drone-expert` 和 `nomic-embed-text` 模型

## Docker 启动

1. 准备环境变量：

```bash
cp deploy/.env.example deploy/.env
```

2. 检查 `deploy/.env` 中的 `MYSQL_DATABASE` 与
   `deploy/mysql/init/001_init.sql` 中创建并使用的数据库名称一致。

3. 首次运行或代码发生变化时，构建并启动全部服务：

```bash
./scripts/uav.sh rebuild
```

日常启动、查看状态和日志：

```bash
./scripts/uav.sh start
./scripts/uav.sh status
./scripts/uav.sh logs
```

只操作单个服务：

```bash
./scripts/uav.sh rebuild backend-java
./scripts/uav.sh rebuild gateway
./scripts/uav.sh restart temporal-ui
./scripts/uav.sh logs backend-node
```

重启或停止全部服务：

```bash
./scripts/uav.sh restart
./scripts/uav.sh stop
```

Compose 会同时启动前端、Gateway、Node BFF、Java 服务和基础设施。前端由 Nginx 托管，并将 `/api/` 与 `/socket.io/` 统一代理到 Gateway，再由 Gateway 路由到 Node BFF。

首次使用知识库前需要准备本地嵌入模型：

```bash
ollama pull nomic-embed-text
```

启动后访问 `http://localhost:8888/knowledge` 上传和检索文档。详细说明见 [`docs/knowledge-base.md`](docs/knowledge-base.md)。

> Gateway、Node、Java 与基础设施的宿主机端口只绑定到
> `127.0.0.1`。局域网/公网业务入口只有 Nginx。Vue 使用 Keycloak PKCE
> 登录，具体配置见 [`docs/gateway.md`](docs/gateway.md)。

> `scripts/uav.sh` 可以从任意目录调用，并始终使用项目中的 `deploy/.env` 和 Compose 文件。运行 `./scripts/uav.sh help` 可以查看全部子命令。

## 本地开发

### Java 核心服务

默认启用 `local` profile，使用内存 H2 数据库，不依赖 MySQL。

```bash
cd backend-java
mvn spring-boot:run
```

访问：`http://localhost:8081/api/health`

H2 控制台：`http://localhost:8081/api/h2-console`

### Node.js BFF

Node 服务会通过 `JAVA_BASE_URL` 调用 Java 服务。本地 Java 默认端口是 `8081`，因此建议显式配置：

```bash
cd backend-node
npm install
JAVA_BASE_URL=http://localhost:8081 npm run start:dev
```

HTTP 健康检查：`http://localhost:3000/api/health`

Socket.IO 服务：`http://localhost:3001`

### Spring Cloud Gateway

本地开发时先启动 Redis、Java 和 Node BFF，然后运行：

```bash
cd gateway-java
mvn spring-boot:run
```

Gateway API：`http://localhost:8082/api`

健康检查：`http://localhost:8082/actuator/health`

### Vue/Cesium 前端

```bash
cd frontend
npm install
npm run dev
```

访问：`http://localhost:8888`

本地开发时使用 Vite 的 `8888` 端口；Docker 部署时由 Nginx 提供前端页面，宿主机端口由 `FRONTEND_PORT` 配置。

## 服务与端口

| 服务 | 默认地址 | 说明 |
| --- | --- | --- |
| Frontend / Nginx | `http://localhost:8888` | 统一业务访问入口 |
| HTTPS（预留） | `https://localhost:8443` | 使用 HTTPS 模板和证书后启用 |
| Spring Cloud Gateway | `http://localhost:8082` | 鉴权、路由、限流和访问日志 |
| Java API | `http://localhost:8081/api` | Docker 环境核心业务服务 |
| Java API（本地） | `http://localhost:8081/api` | local profile + H2 |
| Node API | `http://localhost:3000/api` | BFF 接口 |
| Socket.IO | `http://localhost:3001` | 实时告警推送 |
| MySQL | `localhost:3307` | 宿主机映射端口 |
| Redis | `localhost:6380` | 缓存、聊天记忆和 Gateway 限流 |
| Qdrant | `localhost:6333` | RAG 文档向量和元数据 |
| RabbitMQ | `http://localhost:15672` | 管理控制台 |
| MinIO API | `http://localhost:9011` | 对象存储 API |
| MinIO Console | `http://localhost:9012` | 对象存储控制台 |
| Temporal gRPC | `localhost:7233` | Temporal Server |
| Temporal UI | `http://localhost:8088` | 工作流可视化控制台 |

账号、密码和端口均以 `deploy/.env` 为准，请勿在生产环境继续使用示例密码。

## API 速查

### Java 服务

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| `GET` | `/api/health` | 健康检查 |
| `GET` | `/api/devices` | 设备列表示例 |
| `GET` | `/api/inspection-tasks` | 巡检任务列表示例 |
| `GET` | `/api/alarms/latest` | 最近 20 条告警 |
| `POST` | `/api/alarms` | 创建告警并写入数据库 |
| `GET` | `/api/knowledge/documents` | 查询知识文档 |
| `POST` | `/api/knowledge/documents` | 上传知识文档（ADMIN） |
| `POST` | `/api/knowledge/search` | 语义检索知识片段 |
| `DELETE` | `/api/knowledge/documents/{id}` | 删除知识文档（ADMIN） |

### Node BFF

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| `GET` | `/api/health` | 健康检查 |
| `GET` | `/api/alarms/latest` | 转发 Java 最近告警接口 |
| `POST` | `/api/alarms` | 创建告警，并广播 `alarm.created` 事件 |

创建告警示例：

```bash
curl -X POST http://localhost:3000/api/alarms \
  -H 'Content-Type: application/json' \
  -d '{
    "deviceCode": "UAV-001",
    "taskCode": "TASK-001",
    "eventType": "WEAPON_DETECTED",
    "weaponType": "KNIFE",
    "confidence": 0.96,
    "latitude": 31.2304,
    "longitude": 121.4737
  }'
```

Node BFF 会自动补充缺失的 `eventTime`。直接请求 Java 服务时，必须传入 ISO 格式的 `eventTime`。

## Socket.IO 事件

| 事件 | 方向 | 说明 |
| --- | --- | --- |
| `connected` | 服务端 -> 客户端 | 鉴权成功后返回客户端 ID、用户名、角色和 Token 到期时间 |
| `ping` | 客户端 -> 服务端 | 连通性测试 |
| `pong` | 服务端 -> 客户端 | 返回 `ping` 携带的数据 |
| `alarm.created` | 服务端 -> 客户端 | 通过 Node 创建告警后广播 |

## 当前实现边界

已实现：

- Java 告警持久化、最近告警查询、参数校验和统一响应。
- 设备、巡检任务示例查询接口。
- Node 告警代理、Socket.IO JWT 握手鉴权与实时广播。
- Vue 3 + Cesium 基础页面、主题、布局和国际化能力。
- Docker Compose 前端、后端及基础设施编排。
- LangChain + Ollama + Qdrant 知识库，支持 PDF、Markdown、TXT 入库和可追溯 RAG 检索。

预留或待完善：

- Redis 缓存与在线状态的实际业务接入。
- RabbitMQ 告警消息生产与消费。
- MinIO 截图、视频等证据文件上传。
- Temporal 巡检工作流、Worker 和 Nexus 服务化。
- Python 视觉推理服务及识别结果接入。
- 用户权限、审计日志和完整设备/任务持久化。
