USE horizonai;

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

-- 升级前已存在的未完成任务补齐首次投递事件。
INSERT IGNORE INTO pipeline_outbox(task_id, event_type, status, publish_attempt, next_attempt_at)
SELECT id, 'PIPELINE_TASK_READY', 'PENDING', 0, NOW()
FROM pipeline_tasks
WHERE status IN ('PENDING', 'RETRY_WAIT');
