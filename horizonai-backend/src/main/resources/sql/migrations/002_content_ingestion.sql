USE horizonai;

ALTER TABLE articles
  ADD COLUMN source_name VARCHAR(50) DEFAULT NULL COMMENT 'GITHUB / RSS / DEMO' AFTER source_type,
  ADD COLUMN external_id VARCHAR(255) DEFAULT NULL COMMENT '来源侧稳定标识' AFTER source_name,
  ADD COLUMN content_hash CHAR(64) DEFAULT NULL COMMENT '规范化内容 SHA-256' AFTER external_id,
  ADD COLUMN collected_at DATETIME DEFAULT NULL COMMENT '采集时间' AFTER content_hash,
  ADD COLUMN analysis_status VARCHAR(20) DEFAULT NULL COMMENT 'PENDING / RUNNING / SUCCEEDED / FAILED' AFTER collected_at,
  ADD UNIQUE KEY uk_article_source_identity (source_name, external_id),
  ADD UNIQUE KEY uk_article_content_hash (content_hash),
  ADD INDEX idx_article_analysis_status (analysis_status);
