CREATE TABLE IF NOT EXISTS audit_log (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  request_id VARCHAR(128) NOT NULL,
  actor_id VARCHAR(128) NOT NULL,
  username VARCHAR(128) NOT NULL,
  roles VARCHAR(256) NOT NULL,
  action_code VARCHAR(64) NOT NULL,
  resource_type VARCHAR(64) NOT NULL,
  resource_id VARCHAR(128),
  http_method VARCHAR(16) NOT NULL,
  request_path VARCHAR(512) NOT NULL,
  status_code INT NOT NULL,
  outcome VARCHAR(16) NOT NULL,
  client_ip VARCHAR(64) NOT NULL,
  duration_ms BIGINT NOT NULL,
  error_type VARCHAR(128),
  created_at DATETIME(6) NOT NULL
);

CREATE INDEX idx_audit_log_created
  ON audit_log (created_at);

CREATE INDEX idx_audit_log_actor_created
  ON audit_log (actor_id, created_at);

CREATE INDEX idx_audit_log_action_created
  ON audit_log (action_code, created_at);

CREATE INDEX idx_audit_log_outcome_created
  ON audit_log (outcome, created_at);
