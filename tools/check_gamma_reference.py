"""Compare the running Gamma API against independent SciPy reference values.

Usage: python -I tools/check_gamma_reference.py http://localhost:9303
Requires scipy in the verification environment only.
"""
import json
import math
import sys
from datetime import date
from urllib.request import Request, urlopen

from scipy.special import gammaincc

BASE = sys.argv[1] if len(sys.argv) > 1 else "http://localhost:9303"
CASES = [
    (5, "峰汝线A2段", .22517, 1.07087, date(1990, 10, 1), date(2023, 3, 14), 9.35, 11),
    (15, "万卧线B段", .01697, 1.72263, date(1989, 6, 1), date(2015, 10, 13), 4.76, 14),
    (16, "万卧线C段", .05313, 1.29786, date(1989, 6, 1), date(2017, 8, 31), 4.06, 14),
]


def predict(segment_id):
    payload = json.dumps(dict(segmentId=segment_id, theta=.2, sampleCount=100000, seed=20261029)).encode()
    request = Request(BASE + "/risk/gamma/predict", payload, {"Content-Type": "application/json"})
    with urlopen(request, timeout=45) as response:
        result = json.load(response)
    assert result["code"] == "success", result
    return result["data"]


for segment_id, name, k, n, commission, inspection, depth, wall in CASES:
    report = predict(segment_id)
    assert report == predict(segment_id), "Fixed-seed response must reproduce exactly"
    assert report["segmentName"] == name
    assert report["inspectionDate"] == inspection.isoformat()
    assert math.isclose(report["inspectionDepthMm"], depth)
    assert report["kValue"] == k and report["n"] == n
    assert [row["year"] for row in report["annual"]] == list(range(2026, 2032))
    ti = (inspection - commission).days / 365.2422
    previous_p = previous_events = 0
    for row in report["annual"]:
        t = (date(row["year"], 12, 31) - commission).days / 365.2422
        increment = k * (t**n - ti**n)
        expected_p = float(gammaincc(increment / .2, (wall - depth) / .2))
        assert math.isclose(row["probability"], expected_p, rel_tol=1e-9, abs_tol=1e-25), row
        assert math.isclose(row["meanDepthMm"], depth + increment, rel_tol=1e-12), row
        assert row["probability"] >= previous_p and row["failedSamples"] >= previous_events
        assert row["failedSamples"] / report["sampleCount"] == row["monteCarloProbability"]
        assert 0 <= row["confidenceLow"] <= row["monteCarloProbability"] <= row["confidenceHigh"] <= 1
        tolerance = 6 * math.sqrt(expected_p * (1 - expected_p) / report["sampleCount"]) + 3 / report["sampleCount"]
        assert abs(row["monteCarloProbability"] - expected_p) <= tolerance, row
        if row["failedSamples"] == 0:
            assert math.isclose(row["zeroFailureUpper95"], 1 - .05 ** (1 / report["sampleCount"]), rel_tol=1e-9)
        previous_p, previous_events = row["probability"], row["failedSamples"]
    print(name, "PASS", "2026 Pf:", report["annual"][0]["probability"], "2031 Pf:", report["annual"][-1]["probability"])
print("All independent references, reproducibility and path monotonicity checks passed.")
