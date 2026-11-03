# 真实管线成果表 → GeoJSON

把 `真实gis数据/*.doc`（管线成果表）里的逐桩坐标转换成平台使用的 WGS84 GeoJSON / SQL。

## 步骤

```bash
# 1. .doc 表格导出为文本（每行一条记录）
antiword -w 0 -m UTF-8.txt "../../../真实gis数据/峰汝线A段.doc" > work/峰汝线A段.txt

# 2. 解析为 CSV
python parse_doc.py work/峰汝线A段.txt work/峰汝线A段.csv

# 3. 高斯-克吕格反算 + 生成 GeoJSON/SQL
python build_geojson.py
```

产出在 `work/`：

| 文件 | 用途 |
|---|---|
| `surveyed-pipelines.geojson` | 页面“导入GIS数据”直接上传，或 `POST /gis/segments/geojson/import` |
| `surveyed-pipelines.sql` | `pims-gis` 启动种子（合并进 `db/data.sql` / `deploy/sql/01_pims_gis.sql`） |

## 坐标与分段说明

- 成果表坐标为 CGCS2000 3° 带高斯-克吕格投影，`y` 带 36 带号（中央子午线 108°E），`gk.py` 用标准反算公式转 WGS84；反算后用正算回代和成果表里程校核，相对误差 < 0.1%。
- 五份成果表与平台五个管段一一对应：峰汝线A段、峰汝线B段、万卧线A/B/C段，不做切分。
- 万卧线A段成果表首行的埋深单元格为空，解析器按无埋深处理，仍保留该桩号坐标。
