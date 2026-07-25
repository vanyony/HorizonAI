USE horizonai;

CREATE TABLE IF NOT EXISTS research_sessions (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  title VARCHAR(200) NOT NULL,
  trace_id VARCHAR(64) NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_research_session_user (user_id, updated_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='研究助手会话';

CREATE TABLE IF NOT EXISTS research_messages (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  session_id BIGINT NOT NULL,
  role VARCHAR(20) NOT NULL,
  content LONGTEXT NOT NULL,
  citations_json LONGTEXT DEFAULT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_research_message_session (session_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='研究助手消息';

CREATE TABLE IF NOT EXISTS tool_invocations (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  session_id BIGINT NOT NULL,
  trace_id VARCHAR(64) NOT NULL,
  tool_name VARCHAR(100) NOT NULL,
  input_json LONGTEXT DEFAULT NULL,
  output_json LONGTEXT DEFAULT NULL,
  status VARCHAR(20) NOT NULL,
  duration_ms BIGINT DEFAULT NULL,
  error_message VARCHAR(2000) DEFAULT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_tool_session (session_id, created_at),
  INDEX idx_tool_trace (trace_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='研究助手工具调用记录';
