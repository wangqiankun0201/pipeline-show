"""Build surveyed pipeline GeoJSON + SQL from the parsed 管线成果表 CSVs.

The five surveyed chains map onto the six display segments as:
  峰汝线A段 -> 峰汝线A1段
  峰汝线B段 -> 峰汝线A2段 + 峰汝线B段 (split at the same proportion as the
              previous schematic geometry: 13.910 / 30.878 km)
  万卧线A/B/C段 -> 万卧线A/B/C段
"""
import csv
import json
import math
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
from gk import inverse

R = 6371008.8
SPLIT_RATIO = 0.4504853014623579  # from the replaced schematic A2/(A2+B) length
SOURCE = "真实管线成果表（万州采输气作业区）"


def haversine(a, b):
    lon1, lat1 = map(math.radians, a)
    lon2, lat2 = map(math.radians, b)
    dlon, dlat = lon2 - lon1, lat2 - lat1
    h = math.sin(dlat / 2) ** 2 + math.cos(lat1) * math.cos(lat2) * math.sin(dlon / 2) ** 2
    return 2 * R * math.asin(math.sqrt(h))


def chain_length(pts):
    return sum(haversine(a[:2], b[:2]) for a, b in zip(pts, pts[1:]))


def load(path):
    """Parse a survey CSV -> [(lon, lat, mileage_text)], dropping duplicate vertices."""
    pts = []
    for row in csv.DictReader(open(path, encoding="utf-8-sig")):
        lon, lat = inverse(float(row["x"]), float(row["y"]))
        if pts and haversine(pts[-1][:2], (lon, lat)) < 0.05:
            continue
        pts.append((round(lon, 6), round(lat, 6), row["mileage"]))
    return pts


def split_at(pts, ratio):
    """Split a chain at a cumulative-distance ratio; the cut point is shared."""
    target = chain_length(pts) * ratio
    acc = 0.0
    for i, (a, b) in enumerate(zip(pts, pts[1:])):
        step = haversine(a[:2], b[:2])
        if acc + step >= target:
            t = (target - acc) / step if step else 0.0
            cut = (round(a[0] + (b[0] - a[0]) * t, 6), round(a[1] + (b[1] - a[1]) * t, 6))
            return pts[:i + 1] + [(cut[0], cut[1], "分界")], [(cut[0], cut[1], "分界")] + pts[i + 1:]
        acc += step
    return pts, pts[-1:]


def km_text(mileage):
    return mileage.replace("+", "+")


def build(work):
    surveys = {name: load(work / f"{name}.csv") for name in
               ["峰汝线A段", "峰汝线B段", "万卧线A段", "万卧线B段", "万卧线C段"]}

    a2, b = split_at(surveys["峰汝线B段"], SPLIT_RATIO)

    specs = [
        (4, "峰汝线", "峰汝线A1段", surveys["峰汝线A段"], "峰汝线A段"),
        (5, "峰汝线", "峰汝线A2段", a2, "峰汝线B段"),
        (6, "峰汝线", "峰汝线B段", b, "峰汝线B段"),
        (14, "万卧线", "万卧线A段", surveys["万卧线A段"], "万卧线A段"),
        (15, "万卧线", "万卧线B段", surveys["万卧线B段"], "万卧线B段"),
        (16, "万卧线", "万卧线C段", surveys["万卧线C段"], "万卧线C段"),
    ]

    features = []
    for segment_id, pipeline, name, pts, survey_name in specs:
        coords = [[p[0], p[1]] for p in pts]
        length_km = round(chain_length([tuple(c) for c in coords]) / 1000, 3)
        def endpoint(pt):
            return "A2/B分界" if pt[2] == "分界" else f"{survey_name} K{km_text(pt[2])}"
        start_name, end_name = endpoint(pts[0]), endpoint(pts[-1])
        features.append({
            "type": "Feature",
            "properties": {
                "segmentId": segment_id,
                "segmentName": name,
                "pipelineName": pipeline,
                "startName": start_name,
                "endName": end_name,
                "lengthKm": length_km,
                "coordStatus": "SURVEYED",
                "geoSource": SOURCE,
            },
            "geometry": {"type": "LineString", "coordinates": coords},
        })
        print(f"{segment_id:>2} {name}: {len(coords)} pts, {length_km:.3f} km, "
              f"{coords[0]} -> {coords[-1]}")
    return features


def sql_block(features):
    rows = []
    for f in features:
        p = f["properties"]
        coords = f["geometry"]["coordinates"]
        geojson = json.dumps({"type": "LineString", "coordinates": coords},
                             ensure_ascii=False, separators=(",", ":"))
        rows.append(
            "({id}, 'LineString', '{geojson}', 4326, '{start}', '{end}', {slon}, {slat}, "
            "{elon}, {elat}, {length}, 'SURVEYED', '{source}')".format(
                id=p["segmentId"], geojson=geojson.replace("'", "''"),
                start=p["startName"], end=p["endName"],
                slon=coords[0][0], slat=coords[0][1],
                elon=coords[-1][0], elat=coords[-1][1],
                length=p["lengthKm"], source=SOURCE))
    return ("-- 管段空间数据：来源《真实管线成果表》（万州采输气作业区），CGCS2000 3° 带高斯-克吕格投影反算 WGS84\n"
            "-- 峰汝线A2段/B段按原示意几何的里程比例（45.05%）切分峰汝线B段成果表\n"
            "INSERT INTO t_pipe_segment_geometry (segment_id, geom_type, geojson, srid, start_name, end_name, "
            "start_lon, start_lat, end_lon, end_lat, length_km, coord_status, geo_source) VALUES\n"
            + ",\n".join(rows) + ";\n")


def main(work):
    work = Path(work)
    features = build(work)
    collection = {"type": "FeatureCollection", "features": features}
    (work / "surveyed-pipelines.geojson").write_text(
        json.dumps(collection, ensure_ascii=False, separators=(",", ":")), encoding="utf-8")
    (work / "surveyed-pipelines.sql").write_text(sql_block(features), encoding="utf-8")
    print(f"wrote {work/'surveyed-pipelines.geojson'} and surveyed-pipelines.sql")


if __name__ == "__main__":
    main(sys.argv[1] if len(sys.argv) > 1 else Path(__file__).parent / "work")
