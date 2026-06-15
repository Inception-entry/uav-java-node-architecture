# UAV Java + Node.js 架构脚手架

面向“无人机巡检、人员持械识别、实时告警与证据链”场景的前后端架构脚手架。项目采用 Vue 3 + Cesium 构建三维可视化界面，NestJS 承担 BFF 与实时推送，Spring Boot 负责核心业务和数据持久化。

## 技术架构

```text
Vue 3 / Cesium 前端
        │
        ├── HTTP ────────> NestJS BFF ── REST ──> Spring Boot 核心服务
        │                       │                         │
        └── Socket.IO <─────────┘                         ├── MySQL
                                                        ├── Redis
                                                        ├── RabbitMQ
                                                        └── MinIO
```

- **Frontend**：三维地图、无人机展示、主题与语言切换。
- **Node.js**：面向前端的接口聚合、Java 服务代理、Socket.IO 告警推送。
- **Java**：告警、设备、巡检任务等核心业务及数据持久化。
- **基础设施**：MySQL、Redis、RabbitMQ、MinIO。

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
│   └── vite.config.ts                   # Vite 与 Cesium 配置
├── deploy/
│   ├── mysql/init/001_init.sql          # 数据库与业务表初始化脚本
│   ├── .env.example                     # Compose 环境变量模板
│   └── docker-compose.yml               # 后端及基础设施编排
├── docs/
│   └── architecture.md                  # 服务职责与通信方式
├── scripts/
│   └── start.sh                         # 从仓库根目录启动 Compose
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

## Docker 启动

1. 准备环境变量：

```bash
cp deploy/.env.example deploy/.env
```

2. 检查 `deploy/.env` 中的 `MYSQL_DATABASE` 与
   `deploy/mysql/init/001_init.sql` 中创建并使用的数据库名称一致。

3. 在仓库根目录启动：

```bash
bash scripts/start.sh
```

也可以直接使用 Compose：

```bash
docker compose --env-file deploy/.env -f deploy/docker-compose.yml up -d --build
```

查看容器状态和日志：

```bash
docker compose --env-file deploy/.env -f deploy/docker-compose.yml ps
docker compose --env-file deploy/.env -f deploy/docker-compose.yml logs -f
```

停止服务：

```bash
docker compose --env-file deploy/.env -f deploy/docker-compose.yml down
```

> `scripts/start.sh` 当前执行 `docker compose up -d`，不会强制重新构建镜像。代码更新后可使用上面的 `--build` 命令。

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

### Vue/Cesium 前端

```bash
cd frontend
npm install
npm run dev
```

访问：`http://localhost:8888`

当前 Compose 只编排后端和基础设施，前端需要单独启动或自行构建部署。

## 服务与端口

| 服务 | 默认地址 | 说明 |
| --- | --- | --- |
| Frontend | `http://localhost:8888` | Vite 开发服务 |
| Java API | `http://localhost:8080/api` | Docker 环境核心业务服务 |
| Java API（本地） | `http://localhost:8081/api` | local profile + H2 |
| Node API | `http://localhost:3000/api` | BFF 接口 |
| Socket.IO | `http://localhost:3001` | 实时告警推送 |
| MySQL | `localhost:3307` | 宿主机映射端口 |
| Redis | `localhost:6379` | 缓存与在线状态预留 |
| RabbitMQ | `http://localhost:15672` | 管理控制台 |
| MinIO API | `http://localhost:9001` | 对象存储 API |
| MinIO Console | `http://localhost:9002` | 对象存储控制台 |

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
| `connected` | 服务端 -> 客户端 | 建立连接后返回客户端 ID |
| `ping` | 客户端 -> 服务端 | 连通性测试 |
| `pong` | 服务端 -> 客户端 | 返回 `ping` 携带的数据 |
| `alarm.created` | 服务端 -> 客户端 | 通过 Node 创建告警后广播 |

## 当前实现边界

已实现：

- Java 告警持久化、最近告警查询、参数校验和统一响应。
- 设备、巡检任务示例查询接口。
- Node 告警代理与 Socket.IO 广播。
- Vue 3 + Cesium 基础页面、主题、布局和国际化能力。
- Docker Compose 基础设施及后端服务编排。

预留或待完善：

- Redis 缓存与在线状态的实际业务接入。
- RabbitMQ 告警消息生产与消费。
- MinIO 截图、视频等证据文件上传。
- Python AI 推理服务及识别结果接入。
- 前端与 Node API、Socket.IO 的完整业务联调。
- 用户权限、审计日志和完整设备/任务持久化。
