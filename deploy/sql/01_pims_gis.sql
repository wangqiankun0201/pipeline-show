-- 管道位置服务（pims-gis）PostgreSQL 初始化脚本，由 tools/build_sql.py 生成，请勿手工修改
-- 执行用户须对 schema pims_gis 具有建表权限
CREATE SCHEMA IF NOT EXISTS pims_gis;
SET search_path TO pims_gis;

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

-- 管段基础信息：来源《内腐蚀数据.xlsx》（同一管段多条记录取最近一次的属性）
INSERT INTO t_pipe_segment (segment_id, mine, work_area, pipeline_name, segment_name, commission_date, outer_diameter_mm, wall_thickness_mm, material_grade, design_pressure_mpa, medium_type, data_source) VALUES
(4,  '重庆气矿', '万州采输气作业区', '峰汝线', '峰汝线A1段', DATE '1990-10-01', 325, 11, 'ST45', 8,    '原料气',   '内腐蚀数据.xlsx'),
(5,  '重庆气矿', '万州采输气作业区', '峰汝线', '峰汝线A2段', DATE '1990-10-01', 325, 11, 'ST45', 8,    '含硫湿气', '内腐蚀数据.xlsx'),
(6,  '重庆气矿', '万州采输气作业区', '峰汝线', '峰汝线B段',  DATE '1990-10-01', 325, 11, 'ST45', 8,    '含硫湿气', '内腐蚀数据.xlsx'),
(14, '重庆气矿', '万州采输气作业区', '万卧线', '万卧线A段',  DATE '1989-06-01', 325, 10, '20#',  7.85, '含硫干气', '内腐蚀数据.xlsx'),
(15, '重庆气矿', '万州采输气作业区', '万卧线', '万卧线B段',  DATE '1989-06-01', 426, 14, 'X52',  7.85, '含硫干气', '内腐蚀数据.xlsx'),
(16, '重庆气矿', '万州采输气作业区', '万卧线', '万卧线C段',  DATE '1989-06-01', 426, 14, 'ST45', 7.85, '含硫干气', '内腐蚀数据.xlsx');

-- 管段空间数据：当前为【示意坐标】，仅用于界面联调；正式 GIS 数据通过 /gis/segments/geojson/import 导入后替换
INSERT INTO t_pipe_segment_geometry (segment_id, geom_type, geojson, srid, start_name, end_name, start_lon, start_lat, end_lon, end_lat, length_km, coord_status, geo_source) VALUES
(4,  'LineString', '{"type":"LineString","coordinates":[[108.402,30.792],[108.386,30.781],[108.371,30.771],[108.358,30.760],[108.345,30.752],[108.331,30.745],[108.318,30.738]]}', 4326, '峰汝线首站（示意）', 'A1/A2分界（示意）', 108.402, 30.792, 108.318, 30.738, NULL, 'SCHEMATIC', '示意数据'),
(5,  'LineString', '{"type":"LineString","coordinates":[[108.318,30.738],[108.304,30.727],[108.290,30.716],[108.276,30.709],[108.262,30.701],[108.247,30.692],[108.231,30.682],[108.218,30.671],[108.205,30.660]]}', 4326, 'A1/A2分界（示意）', 'A2/B分界（示意）', 108.318, 30.738, 108.205, 30.660, NULL, 'SCHEMATIC', '示意数据'),
(6,  'LineString', '{"type":"LineString","coordinates":[[108.205,30.660],[108.189,30.646],[108.172,30.633],[108.156,30.622],[108.140,30.611],[108.121,30.600],[108.101,30.590],[108.084,30.578],[108.066,30.566]]}', 4326, 'A2/B分界（示意）', '峰汝线末站（示意）', 108.205, 30.660, 108.066, 30.566, NULL, 'SCHEMATIC', '示意数据'),
(14, 'LineString', '{"type":"LineString","coordinates":[[108.385,30.812],[108.358,30.806],[108.330,30.800],[108.301,30.792],[108.272,30.785],[108.243,30.781],[108.214,30.776],[108.187,30.768],[108.160,30.758]]}', 4326, '万卧线首站（示意）', 'A/B分界（示意）', 108.385, 30.812, 108.160, 30.758, NULL, 'SCHEMATIC', '示意数据'),
(15, 'LineString', '{"type":"LineString","coordinates":[[108.160,30.758],[108.129,30.747],[108.098,30.735],[108.066,30.720],[108.035,30.705],[108.003,30.691],[107.972,30.676],[107.941,30.659],[107.910,30.640]]}', 4326, 'A/B分界（示意）', 'B/C分界（示意）', 108.160, 30.758, 107.910, 30.640, NULL, 'SCHEMATIC', '示意数据'),
(16, 'LineString', '{"type":"LineString","coordinates":[[107.910,30.640],[107.876,30.619],[107.842,30.598],[107.806,30.574],[107.770,30.550],[107.735,30.523],[107.700,30.497],[107.666,30.474],[107.632,30.452]]}', 4326, 'B/C分界（示意）', '万卧线末站（示意）', 107.910, 30.640, 107.632, 30.452, NULL, 'SCHEMATIC', '示意数据');
