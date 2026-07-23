# HorizonAI（观澜）

> 观澜，察技术之波澜。

HorizonAI 是一个 **AI 驱动的技术情报平台**。它主动监测 GitHub 热门仓库、技术新闻和行业动态，利用大语言模型对内容进行分析、评分和摘要，帮助开发者从信息噪音中筛选真正值得关注的技术趋势。

与传统问答式 AI 不同，观澜强调的是 **AI 的主动认知能力** —— 不只是回答问题，而是作为技术信息代理，主动推送每日摘要、生成周报、挖掘趋势。

## 功能

### 用户端
- **今日速览** — AI 评分排序的当日 Top 5 技术趋势
- **每日摘要** — AI 生成的每日技术简报，含头条文章和关联趋势
- **趋势浏览** — 按来源（NEWS / GITHUB / TREND）筛选浏览
- **趋势详情** — 附带 AI 分析（重要性评分、行业影响、学习建议）
- **AI 对话** — 上下文感知的技术问答助手
- **周报** — AI 生成的技术趋势周报
- **用户中心** — 注册（选兴趣标签）/ 登录 / 浏览历史 / 兴趣管理

### 管理端
- 文章管理（CRUD）
- 同步中心（手动触发 GitHub 趋势拉取）
- AI 控制台（手动触发分析 & 摘要生成）
- 标签管理

## 技术栈

| 层级 | 技术 |
|---|---|
| 后端框架 | Spring Boot 2.7 + Java 17 |
| 安全 | Spring Security + JWT + BCrypt |
| ORM | MyBatis Plus 3.5 |
| 数据库 | MySQL 8.0 |
| 前端 | Vue 3（Composition API）+ Element Plus（暗色主题）|
| 状态管理 | Pinia |
| 构建 | Maven（后端）/ Vite（前端）|
| AI | DeepSeek API（预留 Qwen / OpenAI 扩展口）|

## 设计模式

AI 引擎层实现了三种经典设计模式：

- **工厂模式** — `AiModelFactory` 根据配置动态创建对应模型客户端（DeepSeek / Qwen / OpenAI），业务代码不感知具体模型
- **策略模式** — 不同来源（新闻 / GitHub / 趋势）使用不同的分析策略，`AnalysisContext` 在运行时按文章类型路由
- **观察者模式** — 文章分析完成后通过 Spring 事件机制异步触发摘要更新和推荐记录，解耦分析过程与后续行为

## 项目结构

```
horizonai-backend/src/main/java/com/horizonai/
  ai/
    factory/          # 工厂模式（模型客户端）
    strategy/         # 策略模式（内容分析策略）
    observer/         # 观察者模式（事件驱动后处理）
  common/            # 统一响应封装、业务异常
  config/            # Spring 配置（安全、跨域、数据初始化）
  controller/        # REST 控制器（9 个）
  dto/ / entity/     # 数据传输对象 / 数据库实体
  mapper/            # MyBatis Plus 数据访问层
  security/          # JWT 认证过滤器 & 令牌提供者
  service/           # 业务逻辑（8 个服务接口 + 实现）
  vo/                # 视图对象

horizonai-frontend/src/
  api/               # Axios API 模块
  layouts/           # 布局组件（默认 / 管理）
  router/            # 懒加载路由 + 鉴权守卫
  views/             # 13 个页面视图
```

## 快速开始

### 前置条件
- Java 17+ / Maven
- Node.js
- MySQL 8.0
- DeepSeek API Key

### 配置

复制 `.env.example` 为 `.env`，填入配置：

```bash
MYSQL_USERNAME=root
MYSQL_PASSWORD=your_password
JWT_SECRET=your_jwt_secret
DEEPSEEK_API_KEY=sk-your_key
GITHUB_API_TOKEN=your_github_token   # 可选
HORIZONAI_ADMIN_PASSWORD=your_admin_password  # 可选，不设则随机生成
```

### 启动后端

```bash
cd horizonai-backend
# 先在 MySQL 中创建数据库 horizonai，然后执行 src/main/resources/sql/schema.sql
mvn spring-boot:run
```

后端运行在 `http://localhost:8085`。

### 启动前端

```bash
cd horizonai-frontend
npm install
npm run dev
```

前端开发服务器运行在 `http://localhost:5173`，自动代理 `/api` 到后端。

## 架构决策

- **无 Lombok** — 所有 getter/setter/构造器手写，保持代码透明
- **MyBatis Plus 而非 JPA** — 更简洁的 CRUD，内置分页，Lambda 类型安全查询
- **优雅降级** — GitHub API 拉取失败时使用 mock 数据，确保演示流程完整
- **无状态 JWT 认证** — 24h 过期，BCrypt 密码哈希
- **AutoSyncTask** — 每日凌晨 2:00 自动执行趋势拉取→分析→摘要流水线

## License

MIT
