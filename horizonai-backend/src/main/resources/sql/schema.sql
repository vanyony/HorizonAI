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
  publish_date      DATE          DEFAULT NULL COMMENT '发布日期',
  importance_rating INT           DEFAULT 0 COMMENT 'AI 重要度评分 (1-10)',
  created_at        DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at        DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  INDEX idx_source_type (source_type),
  INDEX idx_publish_date (publish_date)
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
  created_at          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '分析时间',
  INDEX idx_article_id (article_id)
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
