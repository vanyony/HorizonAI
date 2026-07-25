USE horizonai;

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
