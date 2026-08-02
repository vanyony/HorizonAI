# HorizonAI（观澜）

> 面向开发者的 AI 技术情报处理与研究助手。

HorizonAI 将 GitHub、RSS 与本地演示数据接入统一内容流水线，完成去重、异步 AI 分析、热点聚合与个性化推荐。研究助手仅能使用受控工具检索已收集的证据，并在回答中给出来源引用。

## 功能概览

- **多源采集与去重**：统一 `ContentCollector` 协议；以来源外部 ID 和规范化内容 SHA-256 双重去重。
- **可恢复处理流水线**：采集与分析拆为 MySQL 持久化任务，支持幂等提交、失败重试和重启恢复。
- **AI 分析与推荐**：按内容指纹、模型和 Prompt 版本幂等保存分析结果；Redis 不可用时回退 MySQL。
- **可追踪研究助手**：白名单工具、调用上限、Evidence 约束和可点击引用；工具调用和 Trace 可回放。
- **运行可观测性**：`X-Trace-Id` 贯穿 HTTP 与异步任务；提供结构化日志和 Actuator 健康检查。

## 架构

```text
GitHub / RSS / Demo
        │
        ▼
 ContentCollector → 标准化与双层去重 → MySQL articles
                                          │ AFTER_COMMIT
                                          ▼
                              PipelineTask（持久化任务）
                                          │
                                          ▼
                              AI 结构化分析与幂等落库
                                   │                    │
                                   ▼                    ▼
                            Redis 热点/缓存        MySQL 分析结果
                                   │                    │
                                   └──── Research Assistant ────► 带证据引用的回答
```

更多设计说明见 [架构文档](docs/architecture.md)。

## 技术栈

| 层级 | 技术 |
| --- | --- |
| 后端 | Java 17、Spring Boot 2.7、Spring Security、MyBatis-Plus |
| 存储与缓存 | MySQL 8、Redis 6+、Lettuce |
| AI | DeepSeek OpenAI-compatible API |
| 前端 | Vue 3、Vite、Element Plus、Axios |
| 可观测性 | Actuator、Micrometer、MDC、结构化日志 |
| 测试 | JUnit 5、Mockito |

## 快速开始

### 1. 准备依赖

需要 JDK 17、Maven 3.8+、Node.js 18+、MySQL 8 和 Redis 6+。可用 Docker 仅启动 MySQL 与 Redis：

```bash
cp .env.example .env
docker compose --env-file .env up -d
```

Windows PowerShell 可使用：

```powershell
Copy-Item .env.example .env
docker compose --env-file .env up -d
```

填写 `.env` 中的 `JWT_SECRET` 和 `HORIZONAI_ADMIN_PASSWORD`。如需调用 AI、GitHub 或 RSS，再填写相应凭证；这些值不应提交到版本库。完整配置见[开发文档](docs/development.md)。

### 2. 初始化数据库

对于全新数据库，执行最新完整结构：

```bash
mysql -u root -p < horizonai-backend/src/main/resources/sql/schema.sql
mysql -u root -p horizonai < horizonai-backend/src/main/resources/sql/data.sql
```

历史迁移仅用于从旧版本升级；不要在已执行最新 `schema.sql` 的数据库上重复执行。详见[数据库说明](docs/development.md#数据库)。

### 3. 启动服务

```bash
cd horizonai-backend
mvn spring-boot:run
```

```bash
cd horizonai-frontend
npm install
npm run dev
```

- 前端：`http://localhost:5173`
- 后端：`http://localhost:8085`
- 健康检查：`http://localhost:8085/actuator/health`

首次启动会创建 `admin` 管理员账号；密码必须由 `HORIZONAI_ADMIN_PASSWORD` 提供，且不会写入日志。

## 演示路径

1. 使用管理员账号登录，进入管理端的“内容流水线”。
2. 提交 `DEMO` 来源采集任务，观察任务状态、执行记录与 Trace。
3. 在研究助手中提问，查看站内检索、GitHub 趋势或兴趣匹配工具产生的证据与引用。
4. 修改兴趣标签，回到推荐页观察缓存失效后的结果。

`DEMO` 来源的标题、摘要和链接均为本地演示数据，不代表真实新闻或外部事实。

## 常用接口

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| POST | `/api/admin/pipeline/sync/{source}` | 提交 `GITHUB`、`RSS` 或 `DEMO` 采集任务 |
| GET | `/api/admin/pipeline/tasks` | 查询流水线任务 |
| GET | `/api/admin/pipeline/traces/{traceId}` | 查询任务链路 |
| POST | `/api/admin/pipeline/tasks/{id}/retry` | 重试最终失败任务 |
| POST | `/api/research/ask` | 执行带工具调用的研究请求 |
| GET | `/api/research/sessions/{id}/tools` | 回放工具调用过程 |
| GET | `/actuator/health` | 查看依赖健康状态 |

## 构建与测试

```bash
cd horizonai-backend && mvn test
cd horizonai-frontend && npm run build
```

现有测试覆盖内容指纹稳定性、任务幂等初始化与研究工具白名单规划。后续测试计划见 [Roadmap](docs/roadmap.md)。

## 文档与参与

- [架构与设计取舍](docs/architecture.md)
- [本地开发与安全配置](docs/development.md)
- [贡献指南](CONTRIBUTING.md)
- [安全策略](SECURITY.md)
- [项目路线图](docs/roadmap.md)
- [MIT License](LICENSE)

提交 issue 或 pull request 前请先阅读贡献指南。本项目以 MIT License 发布。
