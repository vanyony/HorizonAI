# 架构与设计取舍

## 内容处理

采集端通过 `ContentCollector` 适配 GitHub、RSS 和 `DEMO` 数据源。内容先标准化，再用 `(source_name, external_id)` 与 `content_hash` 双重约束去重。文章、`PipelineTask` 与 `pipeline_outbox` 在同一数据库事务中写入，避免文章已提交但分析任务或消息丢失。

Outbox relay 先用条件更新将事件从 `PENDING` 领取为 `PUBLISHING`，再发布只包含 `taskId` 的消息；成功记录发布时间，失败则记录次数与错误并延迟重试。`local` 模式发布到本地线程池，`rocketmq` 模式发布到 RocketMQ，业务提交路径不依赖 Broker 可用性。

RocketMQ consumer 根据 `taskId` 条件领取任务，只有 `PENDING / RETRY_WAIT → RUNNING` 更新成功的实例才能执行，因此重复投递不会重复处理。消息系统只负责通知、削峰和分发；业务执行失败由 `pipeline_tasks` 记录并计算退避时间，consumer 正常 ACK，待业务重试到期后再生成一次 Outbox 投递。这样不会把 MQ 重试与业务状态机重试叠加。

异常行为如下：Broker 不可用或发布失败时事件留在 Outbox；发布成功后服务崩溃可能造成重复消息，但条件领取会过滤；超过一分钟仍为 `PENDING` 的任务会由 watchdog 重新发布，覆盖“消息已发但尚未执行”的崩溃窗口；消费中进程中断时，超时 `RUNNING` 任务恢复为 `RETRY_WAIT`；服务重启后 relay 和业务重试扫描都会从数据库继续。

## AI 分析与缓存

分析结果通过 `(article_id, content_hash, model_used, prompt_version)` 唯一约束保存，复用已有结果并保证同一版本最终只落一份记录；并发请求仍可能发生重复模型调用。热点、兴趣画像与推荐结果使用 Redis 缓存；Redis 不可用时查询退回 MySQL，核心浏览链路仍可工作。

## 研究助手

研究助手不是任意网络浏览器。它只能从下列白名单工具中选择，且单次请求最多使用三个工具：

- 站内情报检索
- GitHub 趋势查询
- 用户兴趣匹配

每次调用都会保存输入、输出、状态、耗时和 Trace。模型获得的事实材料来自 `Evidence`，最终回答通过编号引用关联来源；工具失败不会被伪造成成功结果。

## 可观测性

请求入口生成或复用 `X-Trace-Id`，并通过异步任务装饰器传播 MDC。管理端可按 Trace 查询流水线记录；Actuator 提供 MySQL、Redis、GitHub 与模型配置的健康信息。

## 边界

- Outbox 是至少一次发布，允许重复消息；正确性依赖任务条件领取和下游结果唯一约束，而不是“消息只到一次”。
- 当前 Outbox relay 使用数据库轮询；更大规模可增加分片领取、归档策略与发布延迟监控。
- AI 输出可能不准确，引用表示证据来源而非对生成内容的担保。
- 公开部署时应配置受限 CORS 来源、强随机 JWT 密钥、密钥管理和访问限流。
