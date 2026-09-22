# 本地开发

## 环境变量

复制 `.env.example` 为 `.env`，其中以下变量是启动后端的必要配置：

| 变量 | 说明 |
| --- | --- |
| `MYSQL_USERNAME` / `MYSQL_PASSWORD` | MySQL 账号与密码 |
| `JWT_SECRET` | 至少 32 个字符的随机签名密钥 |
| `HORIZONAI_ADMIN_PASSWORD` | 首次创建 `admin` 账号时使用的密码 |

`DEEPSEEK_API_KEY`、`GITHUB_API_TOKEN` 与 `RSS_FEED_URLS` 为可选能力配置。未配置 AI 凭证时，不要触发依赖模型的分析或周报生成任务。`.env` 已被 Git 忽略，禁止提交任何真实凭证。

流水线默认使用 RocketMQ：应用默认激活 `rocketmq` profile，并使用 `PIPELINE_DISPATCH_MODE=rocketmq`。需配置 `ROCKETMQ_NAME_SERVER`、`PIPELINE_TOPIC` 和 `PIPELINE_CONSUMER_GROUP`。只做轻量本地调试时，同时设置 `SPRING_PROFILES_ACTIVE=local` 与 `PIPELINE_DISPATCH_MODE=local`，此时不要求 Broker 在线。数据库迁移 `006_pipeline_outbox.sql` 必须先执行，否则任务提交会因 Outbox 不存在而整体回滚。

`CORS_ALLOWED_ORIGIN_PATTERNS` 默认仅允许本地前端 `http://localhost:5173`。生产环境必须显式填入实际前端域名，多个域名使用逗号分隔。

## 数据库

### 新数据库

`schema.sql` 是当前版本的完整建表脚本，`data.sql` 只包含基础标签数据：

```bash
mysql -u root -p < horizonai-backend/src/main/resources/sql/schema.sql
mysql -u root -p horizonai < horizonai-backend/src/main/resources/sql/data.sql
```

应用启动时还会按需写入本地演示内容和首个管理员账号。

### 从旧版本升级

`horizonai-backend/src/main/resources/sql/migrations/` 中的文件按编号顺序记录历史增量。只对已有旧数据库执行尚未应用的迁移；不要将它们与最新完整 `schema.sql` 混用。生产环境建议接入 Flyway 或 Liquibase，将迁移执行记录纳入部署流程。

## Docker 依赖服务

根目录的 `docker-compose.yml` 默认启动 MySQL、Redis、RocketMQ NameServer 与 Broker，应用仍在本机运行。先准备 `.env`，再执行：

```bash
docker compose --env-file .env up -d
docker compose --env-file .env ps
```

默认启动就是 RocketMQ 链路。Maven 不会自动读取根目录 `.env`，启动后端前仍需在运行环境中提供数据库与密钥变量。轻量本地调试可显式切换：

```bash
cd horizonai-backend
mvn spring-boot:run -Dspring-boot.run.profiles=local -Dspring-boot.run.arguments=--pipeline.dispatch-mode=local
```

停止容器：

```bash
docker compose --env-file .env down
```

加上 `-v` 会删除容器数据卷，仅在确认不再需要本地数据时使用。

## 常用命令

```bash
# 后端测试
cd horizonai-backend && mvn test

# 前端生产构建
cd horizonai-frontend && npm run build
```

## 安全说明

- 不要使用示例值作为真实密钥或密码。
- 初始管理员密码只从环境变量读取，应用不会输出该密码。
- 本地默认 CORS 仅服务 Vite 开发地址；部署前必须收紧来源。
- Actuator 仅公开健康检查，其他端点需要管理员权限。
