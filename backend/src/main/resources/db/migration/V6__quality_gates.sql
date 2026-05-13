-- Quality gates schema (docs/quality_gates_and_simulation_design.md)
-- Adds tables for test results, compliance results, gate evaluations,
-- and a simulation column on audit_logs.

CREATE TABLE IF NOT EXISTS test_results (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL,
    agent_run_id BIGINT NOT NULL,
    story_id VARCHAR(255),
    tests_run INTEGER NOT NULL,
    tests_passed INTEGER NOT NULL,
    tests_failed INTEGER NOT NULL,
    tests_skipped INTEGER DEFAULT 0,
    coverage_percent DOUBLE PRECISION NOT NULL,
    failures_json TEXT,
    status VARCHAR(20) NOT NULL,
    correlation_id VARCHAR(255) NOT NULL,
    branch VARCHAR(128),
    simulation BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    evaluated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS compliance_results (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL,
    agent_run_id BIGINT NOT NULL,
    checks_passed_json TEXT,
    checks_failed_json TEXT,
    overall_status VARCHAR(30) NOT NULL,
    severity VARCHAR(20) NOT NULL,
    report_url VARCHAR(512),
    correlation_id VARCHAR(255) NOT NULL,
    branch VARCHAR(128),
    simulation BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    evaluated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS gate_evaluations (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL,
    gate_type VARCHAR(20) NOT NULL,
    passed BOOLEAN NOT NULL,
    test_result_id BIGINT,
    compliance_result_id BIGINT,
    correlation_id VARCHAR(255) NOT NULL,
    details TEXT,
    branch VARCHAR(128),
    simulation BOOLEAN DEFAULT FALSE,
    evaluated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Add simulation column to audit_logs if not exists
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'audit_logs' AND column_name = 'simulation'
    ) THEN
        ALTER TABLE audit_logs ADD COLUMN simulation BOOLEAN DEFAULT FALSE;
    END IF;
END $$;
