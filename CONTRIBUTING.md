# 贡献指南

欢迎提交 issue 和 pull request。

## 开发前检查

1. 不要提交 `.env`、凭证、数据库文件、构建产物或 IDE 配置。
2. 保持改动范围聚焦，并同步更新相关文档与测试。
3. 提交前运行：

```bash
cd horizonai-backend && mvn test
cd horizonai-frontend && npm run build
```

## Pull Request

请在说明中描述问题、解决方式、验证命令以及可能的配置或数据库影响。涉及数据库结构时，提供可重复执行或有明确升级顺序的迁移。
