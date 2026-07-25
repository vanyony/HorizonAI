USE horizonai;

ALTER TABLE ai_analysis_results
  ADD COLUMN prompt_version VARCHAR(50) DEFAULT NULL COMMENT 'Prompt 版本' AFTER model_used,
  ADD COLUMN content_hash CHAR(64) DEFAULT NULL COMMENT '分析时的内容指纹' AFTER prompt_version,
  ADD UNIQUE KEY uk_analysis_version (article_id, content_hash, model_used, prompt_version);
