CREATE TABLE IF NOT EXISTS ai_analysis_result (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  analysis_id VARCHAR(128) NOT NULL,
  task_code VARCHAR(64) NOT NULL,
  session_id VARCHAR(64) NOT NULL,
  channel VARCHAR(32) NOT NULL,
  question TEXT NOT NULL,
  answer LONGTEXT NOT NULL,
  model VARCHAR(128),
  sources_json LONGTEXT,
  created_at DATETIME(6) NOT NULL,
  CONSTRAINT uk_ai_analysis_result_analysis_id UNIQUE (analysis_id)
);

CREATE INDEX idx_ai_analysis_result_task_created
  ON ai_analysis_result (task_code, created_at);

CREATE INDEX idx_ai_analysis_result_session_created
  ON ai_analysis_result (session_id, created_at);
