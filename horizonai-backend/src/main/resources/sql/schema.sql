-- =============================================
-- HorizonAI 数据库初始化脚本
-- =============================================

CREATE DATABASE IF NOT EXISTS horizonai
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

USE horizonai;

-- =============================================
-- 1. 用户表
-- =============================================
CREATE TABLE IF NOT EXISTS users (
  id          BIGINT        AUTO_INCREMENT PRIMARY KEY,
  username    VARCHAR(50)   NOT NULL UNIQUE COMMENT '用户名',
  password    VARCHAR(255)  NOT NULL COMMENT 'BCrypt 加密密码',
  email       VARCHAR(100)  DEFAULT NULL COMMENT '邮箱',
  role        VARCHAR(20)   NOT NULL DEFAULT 'USER' COMMENT 'USER / ADMIN',
  avatar      VARCHAR(255)  DEFAULT NULL COMMENT '头像 URL',
  created_at  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- =============================================
-- 2. 文章/趋势表
-- =============================================
CREATE TABLE IF NOT EXISTS articles (
  id                BIGINT        AUTO_INCREMENT PRIMARY KEY,
  title             VARCHAR(500)  NOT NULL COMMENT '标题',
  summary           TEXT          DEFAULT NULL COMMENT '摘要',
  content           LONGTEXT      DEFAULT NULL COMMENT '原始内容或描述',
  source_url        VARCHAR(1000) DEFAULT NULL COMMENT '来源链接',
  source_type       VARCHAR(20)   NOT NULL COMMENT 'NEWS / GITHUB / TREND',
  source_name       VARCHAR(50)   DEFAULT NULL COMMENT 'GITHUB / RSS / DEMO',
  external_id       VARCHAR(255)  DEFAULT NULL COMMENT '来源侧稳定标识',
  content_hash      CHAR(64)      DEFAULT NULL COMMENT '规范化内容 SHA-256',
  collected_at      DATETIME      DEFAULT NULL COMMENT '采集时间',
  analysis_status   VARCHAR(20)   DEFAULT NULL COMMENT 'PENDING / RUNNING / SUCCEEDED / FAILED',
  publish_date      DATE          DEFAULT NULL COMMENT '发布日期',
  importance_rating INT           DEFAULT 0 COMMENT 'AI 重要度评分 (1-10)',
  created_at        DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at        DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  INDEX idx_source_type (source_type),
  INDEX idx_publish_date (publish_date),
  UNIQUE KEY uk_article_source_identity (source_name, external_id),
  UNIQUE KEY uk_article_content_hash (content_hash),
  INDEX idx_article_analysis_status (analysis_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文章/趋势表';

-- =============================================
-- 3. 标签表
-- =============================================
CREATE TABLE IF NOT EXISTS tags (
  id          BIGINT        AUTO_INCREMENT PRIMARY KEY,
  name        VARCHAR(50)   NOT NULL UNIQUE COMMENT '标签名称',
  description VARCHAR(200)  DEFAULT NULL COMMENT '标签描述',
  created_at  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='标签表';

-- =============================================
-- 4. 文章-标签关联表
-- =============================================
CREATE TABLE IF NOT EXISTS article_tag (
  id         BIGINT AUTO_INCREMENT PRIMARY KEY,
  article_id BIGINT NOT NULL COMMENT '文章 ID',
  tag_id     BIGINT NOT NULL COMMENT '标签 ID',
  UNIQUE KEY uk_article_tag (article_id, tag_id),
  INDEX idx_tag_id (tag_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文章标签关联表';

-- =============================================
-- 5. 用户兴趣表
-- =============================================
CREATE TABLE IF NOT EXISTS user_interests (
  id             BIGINT   AUTO_INCREMENT PRIMARY KEY,
  user_id        BIGINT   NOT NULL COMMENT '用户 ID',
  tag_id         BIGINT   NOT NULL COMMENT '标签 ID',
  interest_level INT      NOT NULL DEFAULT 1 COMMENT '兴趣程度 (1-5)',
  created_at     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  UNIQUE KEY uk_user_tag (user_id, tag_id),
  INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户兴趣表';

-- =============================================
-- 6. AI 分析结果表
-- =============================================
CREATE TABLE IF NOT EXISTS ai_analysis_results (
  id                  BIGINT   AUTO_INCREMENT PRIMARY KEY,
  article_id          BIGINT   NOT NULL COMMENT '文章 ID',
  importance_rating   INT      DEFAULT NULL COMMENT '重要度评分 (1-10)',
  target_audience     VARCHAR(500) DEFAULT NULL COMMENT '目标受众',
  industry_impact     TEXT     DEFAULT NULL COMMENT '行业影响分析',
  learning_suggestions TEXT    DEFAULT NULL COMMENT '学习建议',
  summary             TEXT     DEFAULT NULL COMMENT 'AI 生成摘要',
  raw_response        LONGTEXT DEFAULT NULL COMMENT 'AI 原始返回',
  model_used          VARCHAR(50)  DEFAULT NULL COMMENT '使用的模型',
  prompt_version      VARCHAR(50)  DEFAULT NULL COMMENT 'Prompt 版本',
  content_hash        CHAR(64)     DEFAULT NULL COMMENT '分析时的内容指纹',
  created_at          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '分析时间',
  INDEX idx_article_id (article_id),
  UNIQUE KEY uk_analysis_version (article_id, content_hash, model_used, prompt_version)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI分析结果表';

-- =============================================
-- 7. 对话历史表
-- =============================================
CREATE TABLE IF NOT EXISTS chat_history (
  id         BIGINT   AUTO_INCREMENT PRIMARY KEY,
  user_id    BIGINT   NOT NULL COMMENT '用户 ID',
  role       VARCHAR(20) NOT NULL COMMENT 'user / assistant',
  content    TEXT     NOT NULL COMMENT '消息内容',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
  INDEX idx_user_id (user_id),
  INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='对话历史表';

-- =============================================
-- 8. 每日摘要表
-- =============================================
CREATE TABLE IF NOT EXISTS daily_digests (
  id          BIGINT   AUTO_INCREMENT PRIMARY KEY,
  digest_date DATE     NOT NULL UNIQUE COMMENT '摘要日期',
  title       VARCHAR(500) DEFAULT NULL COMMENT '摘要标题',
  summary     TEXT     DEFAULT NULL COMMENT 'AI 生成的每日总结',
  article_ids VARCHAR(1000) DEFAULT NULL COMMENT '关联文章 ID 列表 (JSON 数组)',
  created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '生成时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='每日摘要表';

-- =============================================
-- 9. 浏览历史表
-- =============================================
CREATE TABLE IF NOT EXISTS browse_history (
  id         BIGINT   AUTO_INCREMENT PRIMARY KEY,
  user_id    BIGINT   NOT NULL COMMENT '用户 ID',
  article_id BIGINT   NOT NULL COMMENT '文章 ID',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '浏览时间',
  UNIQUE KEY uk_user_article (user_id, article_id),
  INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='浏览历史表';

-- =============================================
-- 10. 持久化流水线任务表
-- =============================================
CREATE TABLE IF NOT EXISTS pipeline_tasks (
  id              BIGINT        AUTO_INCREMENT PRIMARY KEY,
  trace_id        VARCHAR(64)   NOT NULL COMMENT '一次业务链路的 Trace ID',
  task_type       VARCHAR(50)   NOT NULL COMMENT '任务类型',
  biz_key         VARCHAR(255)  DEFAULT NULL COMMENT '业务对象标识',
  idempotency_key VARCHAR(255)  NOT NULL COMMENT '任务幂等键',
  payload_json    LONGTEXT      DEFAULT NULL COMMENT '任务输入 JSON',
  status          VARCHAR(20)   NOT NULL COMMENT 'PENDING/RUNNING/RETRY_WAIT/SUCCEEDED/FAILED',
  attempt         INT           NOT NULL DEFAULT 0 COMMENT '已执行次数',
  max_attempts    INT           NOT NULL DEFAULT 3 COMMENT '最大执行次数',
  next_retry_at   DATETIME      DEFAULT NULL COMMENT '下次重试时间',
  error_message   VARCHAR(2000) DEFAULT NULL COMMENT '最后一次失败原因',
  started_at      DATETIME      DEFAULT NULL COMMENT '最近一次开始时间',
  finished_at     DATETIME      DEFAULT NULL COMMENT '最终完成时间',
  created_at      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_pipeline_idempotency (idempotency_key),
  INDEX idx_pipeline_dispatch (status, next_retry_at),
  INDEX idx_pipeline_trace (trace_id),
  INDEX idx_pipeline_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='持久化内容处理任务';

CREATE TABLE IF NOT EXISTS pipeline_outbox (
  id              BIGINT        AUTO_INCREMENT PRIMARY KEY,
  task_id         BIGINT        NOT NULL COMMENT 'PipelineTask ID，消息只携带该标识',
  event_type      VARCHAR(50)   NOT NULL COMMENT 'PIPELINE_TASK_READY',
  status          VARCHAR(20)   NOT NULL COMMENT 'PENDING/PUBLISHING/PUBLISHED',
  publish_attempt INT           NOT NULL DEFAULT 0 COMMENT '消息发布尝试次数',
  next_attempt_at DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '下次发布时间',
  published_at    DATETIME      DEFAULT NULL COMMENT '最近发布成功时间',
  last_error      VARCHAR(2000) DEFAULT NULL COMMENT '最近发布错误',
  created_at      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_pipeline_outbox_task (task_id),
  INDEX idx_pipeline_outbox_relay (status, next_attempt_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='流水线任务 Transactional Outbox';

-- =============================================
-- 11. 研究助手会话
-- =============================================
CREATE TABLE IF NOT EXISTS research_sessions (
  id         BIGINT       AUTO_INCREMENT PRIMARY KEY,
  user_id    BIGINT       NOT NULL,
  title      VARCHAR(200) NOT NULL,
  trace_id   VARCHAR(64)  NOT NULL,
  created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_research_session_user (user_id, updated_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='研究助手会话';

CREATE TABLE IF NOT EXISTS research_messages (
  id             BIGINT      AUTO_INCREMENT PRIMARY KEY,
  session_id     BIGINT      NOT NULL,
  role           VARCHAR(20) NOT NULL,
  content        LONGTEXT    NOT NULL,
  citations_json LONGTEXT    DEFAULT NULL,
  created_at     DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_research_message_session (session_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='研究助手消息';

CREATE TABLE IF NOT EXISTS tool_invocations (
  id            BIGINT       AUTO_INCREMENT PRIMARY KEY,
  session_id    BIGINT       NOT NULL,
  trace_id      VARCHAR(64)  NOT NULL,
  tool_name     VARCHAR(100) NOT NULL,
  input_json    LONGTEXT     DEFAULT NULL,
  output_json   LONGTEXT     DEFAULT NULL,
  status        VARCHAR(20)  NOT NULL,
  duration_ms   BIGINT       DEFAULT NULL,
  error_message VARCHAR(2000) DEFAULT NULL,
  created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_tool_session (session_id, created_at),
  INDEX idx_tool_trace (trace_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='研究助手工具调用记录';

-- =============================================
-- 12. 技术趋势周报
-- =============================================
CREATE TABLE IF NOT EXISTS weekly_reports (
  id           BIGINT       AUTO_INCREMENT PRIMARY KEY,
  year         INT          NOT NULL,
  week_of_year INT          NOT NULL,
  start_date   DATE         NOT NULL,
  end_date     DATE         NOT NULL,
  title        VARCHAR(500) NOT NULL,
  summary      LONGTEXT     DEFAULT NULL,
  article_ids  VARCHAR(2000) DEFAULT NULL,
  deleted      TINYINT      NOT NULL DEFAULT 0,
  created_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_weekly_report_period (year, week_of_year),
  INDEX idx_weekly_report_period (year, week_of_year)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='技术趋势周报';
