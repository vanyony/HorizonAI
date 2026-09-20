# Roadmap

## 已完成

- 多源采集、双层去重与持久化任务处理
- AI 分析版本幂等与 Redis 缓存降级
- 受控研究工具、Evidence 引用与调用回放
- Trace 传播、健康检查与管理端任务看板
- Transactional Outbox、RocketMQ 异步分发与幂等消费

## 下一步

- [ ] 为认证、任务重试、研究引用补充集成测试
- [ ] 接入 Flyway 或 Liquibase 管理数据库升级
- [ ] 提供应用容器镜像和完整部署示例
- [ ] 为多实例 Outbox relay 与任务领取增加数据库集成并发验证
- [ ] 增加 Outbox 积压、最老未发布事件和消息消费延迟指标
- [ ] 添加 API 文档（OpenAPI）与限流策略
- [ ] 优化前端产物体积与依赖拆分
