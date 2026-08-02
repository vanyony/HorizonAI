USE horizonai;

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
