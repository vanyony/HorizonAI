-- =============================================
-- HorizonAI 初始数据
-- 管理员账号由 DataInitializer.java 在启动时创建
-- =============================================

USE horizonai;

-- 预设技术标签
INSERT IGNORE INTO tags (name, description) VALUES
('人工智能', 'AI、机器学习、深度学习、大模型相关'),
('前端开发', 'Vue、React、Angular、CSS、Web 标准'),
('后端开发', 'Java、Spring、Go、Rust、API 设计'),
('云原生', 'Kubernetes、Docker、Serverless、DevOps'),
('安全技术', '网络安全、漏洞分析、密码学'),
('数据科学', '大数据、数据仓库、ETL、BI'),
('移动开发', 'iOS、Android、Flutter、React Native'),
('开源项目', '值得关注的开源工具和框架');
