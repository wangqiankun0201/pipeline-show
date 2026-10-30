-- 内腐蚀服务表结构（PostgreSQL，schema: pims_corrosion）

CREATE TABLE IF NOT EXISTS t_corrosion_segment (
    segment_id          BIGINT        NOT NULL PRIMARY KEY,
    mine                VARCHAR(64)   NOT NULL,
    work_area           VARCHAR(64),
    pipeline_name       VARCHAR(128)  NOT NULL,
    segment_name        VARCHAR(128)  NOT NULL,
    commission_date     DATE,
    outer_diameter_mm   NUMERIC(10, 2),
    wall_thickness_mm   NUMERIC(10, 2),
    source_segment_ids  VARCHAR(256),
    import_batch        VARCHAR(128),
    update_time         TIMESTAMP     DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE t_corrosion_segment IS '内腐蚀物理管段（同一气矿-管线-管段归并）';
COMMENT ON COLUMN t_corrosion_segment.segment_id IS '物理管段ID（归并到最小源管段ID）';
COMMENT ON COLUMN t_corrosion_segment.wall_thickness_mm IS '名义壁厚(mm)，取最近一条有壁厚记录';
COMMENT ON COLUMN t_corrosion_segment.source_segment_ids IS '归并的源管段ID，逗号分隔';
COMMENT ON COLUMN t_corrosion_segment.import_batch IS '导入批次（来源文件）';

CREATE TABLE IF NOT EXISTS t_corrosion_inspection (
    inspection_id           BIGSERIAL     NOT NULL PRIMARY KEY,
    segment_id              BIGINT        NOT NULL,
    source_segment_id       BIGINT        NOT NULL,
    row_no                  INTEGER,
    mine                    VARCHAR(64),
    work_area               VARCHAR(64),
    pipeline_name           VARCHAR(128),
    segment_name            VARCHAR(128),
    commission_date         DATE,
    inspection_date         DATE,
    outer_diameter_mm       NUMERIC(10, 2),
    wall_thickness_mm       NUMERIC(10, 2),
    material_grade          VARCHAR(32),
    design_pressure_mpa     NUMERIC(10, 3),
    operating_pressure_mpa  NUMERIC(10, 3),
    operating_pressure_text VARCHAR(32),
    pressure_factor         NUMERIC(10, 4),
    operating_temperature   VARCHAR(32),
    medium_type             VARCHAR(32),
    liquid_accumulation     VARCHAR(16),
    co2_mol_pct             NUMERIC(10, 4),
    h2s_mol_pct             NUMERIC(10, 4),
    o2_mol_pct              NUMERIC(10, 4),
    water_cut_pct           NUMERIC(10, 4),
    chloride_mg_l           NUMERIC(12, 2),
    ph                      NUMERIC(6, 2),
    inhibitor_dosing        VARCHAR(16),
    inhibitor_concentration VARCHAR(32),
    pigging_cycle_days      INTEGER,
    defect_count            INTEGER,
    max_depth_pct           NUMERIC(6, 2),
    avg_erf                 NUMERIC(8, 4),
    max_erf                 NUMERIC(8, 4),
    remark                  VARCHAR(256),
    import_batch            VARCHAR(128),
    create_time             TIMESTAMP     DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_corrosion_inspection_segment ON t_corrosion_inspection (segment_id, inspection_date);

COMMENT ON TABLE t_corrosion_inspection IS '内腐蚀检测记录（内腐蚀数据.xlsx 逐行）';
COMMENT ON COLUMN t_corrosion_inspection.segment_id IS '物理管段ID';
COMMENT ON COLUMN t_corrosion_inspection.source_segment_id IS '源数据管段ID';
COMMENT ON COLUMN t_corrosion_inspection.inspection_date IS '内检时间';
COMMENT ON COLUMN t_corrosion_inspection.operating_pressure_mpa IS '运行压力(MPa)，区间值时为空';
COMMENT ON COLUMN t_corrosion_inspection.operating_pressure_text IS '运行压力原始文本';
COMMENT ON COLUMN t_corrosion_inspection.max_depth_pct IS '最大腐蚀深度(%壁厚)';
COMMENT ON COLUMN t_corrosion_inspection.avg_erf IS '平均ERF';
COMMENT ON COLUMN t_corrosion_inspection.max_erf IS '最大ERF';

CREATE TABLE IF NOT EXISTS t_corrosion_model_param (
    param_id                 BIGSERIAL     NOT NULL PRIMARY KEY,
    segment_id               BIGINT        NOT NULL,
    model_name               VARCHAR(64)   NOT NULL,
    model_version            VARCHAR(16)   NOT NULL,
    exponent_n               NUMERIC(8, 4) NOT NULL,
    coefficient_k            NUMERIC(14, 8) NOT NULL,
    d0_mm                    NUMERIC(10, 4) NOT NULL,
    anchor_years             NUMERIC(10, 4) NOT NULL,
    anchor_date              DATE          NOT NULL,
    commission_date          DATE          NOT NULL,
    wall_thickness_mm        NUMERIC(10, 2) NOT NULL,
    max_depth_pct            NUMERIC(6, 2) NOT NULL,
    anchor_source_segment_id BIGINT,
    time_origin              VARCHAR(32)   NOT NULL,
    grade                    VARCHAR(4)    NOT NULL,
    parameter_source         VARCHAR(256),
    calc_time                TIMESTAMP     DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_corrosion_model_param ON t_corrosion_model_param (segment_id, model_version, exponent_n);

COMMENT ON TABLE t_corrosion_model_param IS '幂律模型参数库（k 与 n 成组存储）';
COMMENT ON COLUMN t_corrosion_model_param.exponent_n IS '增长指数 n';
COMMENT ON COLUMN t_corrosion_model_param.coefficient_k IS '腐蚀发展系数 k = d0/T^n（mm/年^n）';
COMMENT ON COLUMN t_corrosion_model_param.d0_mm IS '最近内检最大腐蚀深度(mm)';
COMMENT ON COLUMN t_corrosion_model_param.anchor_years IS 'T：时间原点至最近内检年数';
COMMENT ON COLUMN t_corrosion_model_param.time_origin IS '时间原点口径 COMMISSION_DATE/CORROSION_START';
COMMENT ON COLUMN t_corrosion_model_param.grade IS '数据可信等级 A/B/C/D';

CREATE TABLE IF NOT EXISTS t_corrosion_predict_log (
    calc_id         VARCHAR(36)   NOT NULL PRIMARY KEY,
    segment_id      BIGINT        NOT NULL,
    model_version   VARCHAR(16)   NOT NULL,
    exponent_n      NUMERIC(8, 4),
    coefficient_k   NUMERIC(14, 8),
    d0_mm           NUMERIC(10, 4),
    anchor_years    NUMERIC(10, 4),
    anchor_date     DATE,
    target_date     DATE,
    delta_years     NUMERIC(10, 4),
    pred_depth_mm   NUMERIC(10, 4),
    depth_ratio     NUMERIC(10, 6),
    limit_flag      VARCHAR(32),
    request_json    TEXT,
    operator        VARCHAR(64),
    calc_time       TIMESTAMP     DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE t_corrosion_predict_log IS '内腐蚀预测计算留痕';
COMMENT ON COLUMN t_corrosion_predict_log.limit_flag IS 'NORMAL/ATTENTION/MODEL_OUT_OF_RANGE';
