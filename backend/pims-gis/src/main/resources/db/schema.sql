-- 管道位置服务表结构（PostgreSQL，schema: pims_gis）

CREATE TABLE IF NOT EXISTS t_pipe_segment (
    segment_id          BIGINT        NOT NULL PRIMARY KEY,
    mine                VARCHAR(64)   NOT NULL,
    work_area           VARCHAR(64),
    pipeline_name       VARCHAR(128)  NOT NULL,
    segment_name        VARCHAR(128)  NOT NULL,
    commission_date     DATE,
    outer_diameter_mm   NUMERIC(10, 2),
    wall_thickness_mm   NUMERIC(10, 2),
    material_grade      VARCHAR(32),
    design_pressure_mpa NUMERIC(10, 3),
    medium_type         VARCHAR(32),
    data_source         VARCHAR(128),
    create_time         TIMESTAMP     DEFAULT CURRENT_TIMESTAMP,
    update_time         TIMESTAMP     DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE t_pipe_segment IS '管段基础信息';
COMMENT ON COLUMN t_pipe_segment.segment_id IS '管段ID（与内腐蚀数据源管段ID一致）';
COMMENT ON COLUMN t_pipe_segment.mine IS '气矿';
COMMENT ON COLUMN t_pipe_segment.work_area IS '作业区';
COMMENT ON COLUMN t_pipe_segment.pipeline_name IS '管线名称';
COMMENT ON COLUMN t_pipe_segment.segment_name IS '管段名称';
COMMENT ON COLUMN t_pipe_segment.commission_date IS '投运日期';
COMMENT ON COLUMN t_pipe_segment.outer_diameter_mm IS '外径(mm)';
COMMENT ON COLUMN t_pipe_segment.wall_thickness_mm IS '名义壁厚(mm)';
COMMENT ON COLUMN t_pipe_segment.material_grade IS '材质等级';
COMMENT ON COLUMN t_pipe_segment.design_pressure_mpa IS '设计压力(MPa)';
COMMENT ON COLUMN t_pipe_segment.medium_type IS '介质类型';
COMMENT ON COLUMN t_pipe_segment.data_source IS '数据来源';

CREATE TABLE IF NOT EXISTS t_pipe_segment_geometry (
    segment_id   BIGINT        NOT NULL PRIMARY KEY,
    geom_type    VARCHAR(32)   NOT NULL,
    geojson      TEXT          NOT NULL,
    srid         INTEGER       DEFAULT 4326,
    start_name   VARCHAR(128),
    end_name     VARCHAR(128),
    start_lon    NUMERIC(12, 7),
    start_lat    NUMERIC(12, 7),
    end_lon      NUMERIC(12, 7),
    end_lat      NUMERIC(12, 7),
    length_km    NUMERIC(10, 3),
    coord_status VARCHAR(16)   NOT NULL,
    geo_source   VARCHAR(128),
    update_time  TIMESTAMP     DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE t_pipe_segment_geometry IS '管段空间数据（GeoJSON，WGS84）';
COMMENT ON COLUMN t_pipe_segment_geometry.geom_type IS '几何类型 LineString/MultiLineString';
COMMENT ON COLUMN t_pipe_segment_geometry.geojson IS 'GeoJSON 几何对象';
COMMENT ON COLUMN t_pipe_segment_geometry.srid IS '坐标系 EPSG 编码';
COMMENT ON COLUMN t_pipe_segment_geometry.length_km IS '管段长度(km)，按球面距离计算';
COMMENT ON COLUMN t_pipe_segment_geometry.coord_status IS '坐标状态 SCHEMATIC 示意/SURVEYED 实测';
COMMENT ON COLUMN t_pipe_segment_geometry.geo_source IS '空间数据来源';
