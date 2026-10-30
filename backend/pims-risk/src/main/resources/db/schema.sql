-- 失效风险服务表结构（PostgreSQL，schema: pims_risk）

CREATE TABLE IF NOT EXISTS t_risk_factor (
    factor_code   VARCHAR(64)   NOT NULL PRIMARY KEY,
    group_code    VARCHAR(8)    NOT NULL,
    factor_name   VARCHAR(64)   NOT NULL,
    unit          VARCHAR(16),
    input_type    VARCHAR(16)   NOT NULL,
    source        VARCHAR(16)   NOT NULL,
    options_json  TEXT,
    required      BOOLEAN       DEFAULT FALSE,
    min_value     NUMERIC(14, 4),
    max_value     NUMERIC(14, 4),
    sort_order    INTEGER       DEFAULT 0,
    enabled       BOOLEAN       DEFAULT TRUE
);

COMMENT ON TABLE t_risk_factor IS '失效风险因素定义（可配置，计算公式据此取值）';
COMMENT ON COLUMN t_risk_factor.group_code IS '分组 POF 失效可能性/COF 失效后果';
COMMENT ON COLUMN t_risk_factor.input_type IS '输入类型 number/select';
COMMENT ON COLUMN t_risk_factor.source IS '数据来源 CORROSION/GIS/MANUAL';
COMMENT ON COLUMN t_risk_factor.options_json IS '选项 JSON 数组 [{label,value}]';

CREATE TABLE IF NOT EXISTS t_risk_assessment (
    assessment_id   VARCHAR(36)   NOT NULL PRIMARY KEY,
    segment_id      BIGINT        NOT NULL,
    segment_name    VARCHAR(128),
    evaluate_years  NUMERIC(8, 2),
    model_code      VARCHAR(32)   NOT NULL,
    status          VARCHAR(32)   NOT NULL,
    pof_level       INTEGER,
    cof_level       INTEGER,
    pof_value       NUMERIC(18, 8),
    cof_value       NUMERIC(18, 8),
    risk_level      VARCHAR(16),
    message         VARCHAR(512),
    inputs_json     TEXT,
    operator        VARCHAR(64),
    assess_time     TIMESTAMP     DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_risk_assessment_segment ON t_risk_assessment (segment_id, assess_time);

COMMENT ON TABLE t_risk_assessment IS '失效风险评价记录';
COMMENT ON COLUMN t_risk_assessment.status IS 'MODEL_PENDING 模型待接入/COMPLETED 已完成/FAILED 计算失败';
COMMENT ON COLUMN t_risk_assessment.risk_level IS 'LOW/MEDIUM/HIGH/VERY_HIGH';
COMMENT ON COLUMN t_risk_assessment.inputs_json IS '因素输入快照';
