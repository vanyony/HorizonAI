# HorizonAI（观澜）

> AI 驱动的技术情报处理与研究助手平台

HorizonAI 面向开发者的技术信息获取场景，将 GitHub、RSS 等来源的内容统一接入持久化处理流水线，完成标准化、指纹去重、AI 结构化分析、热点聚合与个性化推荐。研究助手通过受控工具调用检索站内情报、GitHub 趋势和用户兴趣，并基于检索证据生成带来源引用的技术解读。

## 核心能力

### 内容处理流水线

- GitHub、RSS 与可重复离线数据源采用统一 `ContentCollector` 协议。
- 使用来源外部 ID 与规范化内容 SHA-256 进行双层去重。
- 采集和 AI 分析拆分为持久化异步任务，支持幂等提交、失败重试和服务重启恢复。
- 任务状态、执行次数、下次重试时间和失败原因均保存到 MySQL。
- 模型分析结果按照内容指纹、模型和 Prompt 版本进行幂等落库。

### Redis 与个性化推荐

- 使用 Redis ZSet 保存每日热点趋势。
- 缓存用户兴趣画像与个性化推荐结果，并设置独立 TTL。
- 兴趣变更后主动失效画像和推荐缓存。
- Redis 不可用时自动回退 MySQL，不影响核心查询。

### 可观测性

- HTTP 请求生成或透传 `X-Trace-Id`。
- MDC Trace 通过 `TaskDecorator` 传播到异步线程。
- 键值结构日志包含 Trace、任务 ID、线程、外部服务和调用耗时。
- 管理端支持任务筛选、Trace 时间线和失败任务人工重试。
- Spring Boot Actuator 提供 MySQL、Redis、GitHub 与模型配置健康状态。

### 技术研究助手

- 白名单工具：站内情报检索、GitHub 趋势查询、用户兴趣匹配。
- 单次请求最多选择三个工具，限制每个工具的结果数量。
- 工具失败被独立记录，不会生成伪造数据。
- 模型只能根据收集到的 Evidence 生成事实性解读。
- 最终回答包含 `[1]`、`[2]` 形式的引用及可点击来源。
- 会话、工具输入输出、执行状态、耗时、引用和 Trace 均支持回溯。

## 技术栈

| 层级 | 技术 |
|---|---|
| 后端 | Java 17、Spring Boot 2.7、Spring Security |
| 数据访问 | MyBatis-Plus、MySQL 8 |
| 缓存 | Redis、Lettuce |
| 异步任务 | MySQL 持久化任务、ThreadPoolTaskExecutor |
| 可观测性 | MDC、Actuator、Micrometer、结构化日志 |
| AI | DeepSeek OpenAI-compatible API |
| 前端 | Vue 3、Vite、Element Plus、Axios |
| 测试 | JUnit 5、Mockito |

## 核心链路

```text
GitHub / RSS / Offline Demo
          │
          ▼
   ContentCollector
          │
          ▼
 标准化与 SHA-256 去重
          │
          ▼
   MySQL 文章数据
          │ AFTER_COMMIT
          ▼
 PipelineTask: ANALYZE_ARTICLE
          │
          ▼
 AI 结构化分析与版本幂等
          │
     ┌────┴────┐
     ▼         ▼
 Redis 热点   MySQL 分析结果
     │
     ▼
 Research Assistant
     │
     ▼
 带 Evidence 引用的技术解读
```

## 项目结构

```text
horizonai-backend/src/main/java/com/horizonai/
├── collector/       # 多源采集协议与实现
├── task/            # 持久化任务、执行器、调度与重试
├── cache/           # Redis 热点、兴趣与推荐缓存
├── research/        # 研究助手白名单工具与证据模型
├── observability/   # Trace 与健康检查
├── ai/              # 模型客户端、分析策略与领域事件
├── controller/      # REST API
├── service/         # 业务服务
├── mapper/          # MyBatis-Plus Mapper
└── entity/          # 数据库实体

horizonai-frontend/src/
├── views/chat/ResearchView.vue       # 研究助手与引用展示
├── views/admin/PipelineTaskView.vue  # 流水线任务看板
├── api/                              # 前端 API 封装
└── views/                            # 资讯、用户和管理页面
```

## 本地运行

### 1. 环境要求

- JDK 17+
- Maven 3.8+
- Node.js 18+
- MySQL 8
- Redis 6+，可选；未安装时可以关闭 Redis 功能

### 2. 环境变量

参考 [.env.example](.env.example) 配置：

```dotenv
MYSQL_USERNAME=root
MYSQL_PASSWORD=your_password
REDIS_ENABLED=true
REDIS_HOST=localhost
REDIS_PORT=6379
JWT_SECRET=replace_with_a_secure_secret
DEEPSEEK_API_KEY=sk-your-key
GITHUB_API_TOKEN=github-token
RSS_FEED_URLS=https://example.com/feed.xml
```

Redis 未启动时可设置：

```dotenv
REDIS_ENABLED=false
```

### 3. 数据库

新数据库直接执行：

```bash
mysql -u root -p < horizonai-backend/src/main/resources/sql/schema.sql
```

### 4. 启动后端

```bash
cd horizonai-backend
mvn spring-boot:run
```

后端地址：`http://localhost:8085`

健康状态：`http://localhost:8085/actuator/health`

### 5. 启动前端

```bash
cd horizonai-frontend
npm install
npm run dev
```

前端地址：`http://localhost:5173`

## 关键接口

| 方法 | 路径 | 说明 |
|---|---|---|
| POST | `/api/admin/pipeline/sync/{source}` | 提交 GITHUB、RSS 或 DEMO 采集任务 |
| GET | `/api/admin/pipeline/tasks` | 分页查询流水线任务 |
| GET | `/api/admin/pipeline/traces/{traceId}` | 查询完整任务链路 |
| POST | `/api/admin/pipeline/tasks/{id}/retry` | 人工重试最终失败任务 |
| GET | `/api/user/recommendations` | 获取个性化推荐 |
| POST | `/api/research/ask` | 执行带工具调用的技术研究 |
| GET | `/api/research/sessions/{id}/tools` | 回放工具调用过程 |
| GET | `/actuator/health` | 查看关键依赖健康状态 |

## 构建与测试

后端：

```bash
cd horizonai-backend
mvn test
```

前端：

```bash
cd horizonai-frontend
npm run build
```

当前测试覆盖内容指纹稳定性、任务幂等初始化和研究助手工具白名单选择。
