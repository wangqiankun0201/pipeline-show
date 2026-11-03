"""Parse 管线成果表 (antiword -w 0 text output) into CSV point lists."""
import csv
import re
import sys
from pathlib import Path

STAKE = re.compile(r"^\d{13}$")
MILEAGE = re.compile(r"^\d+\+\d+(?:\.\d+)?$")
NUMBER = re.compile(r"^\d+(?:\.\d+)?$")


def parse(text):
    """Token based: rows are '|'-separated cells; a 13-digit stake marks a row."""
    rows = []
    cells = [c.strip() for c in text.replace("\n", " ").split("|")]
    for i, cell in enumerate(cells):
        if not STAKE.match(cell):
            continue
        # cells: [.., index, stake, mileage, angle, elevation, x, y, burial, ..]
        window = cells[i + 1:i + 7]
        if len(window) < 6 or not MILEAGE.match(window[0]) or not NUMBER.match(window[4]):
            continue
        mileage, angle, elev, x, y, depth = window
        if not NUMBER.match(depth):
            depth = ""  # 首行等少数记录埋深单元格为空
        if not elev:
            continue
        km, metre = mileage.split("+")
        rows.append({
            "index": int(cells[i - 1]) if i > 0 and cells[i - 1].isdigit() else len(rows) + 1,
            "stake": cell,
            "mileage": mileage,
            "mileage_m": round(int(km) * 1000 + float(metre), 1),
            "angle": " ".join(angle.split()),
            "elevation_m": float(elev),
            "x": float(x),
            "y": float(y),
            "burial_m": float(depth) if depth else None,
        })
    return rows


def main(src, out):
    text = Path(src).read_text(encoding="utf-8", errors="replace")
    rows = parse(text)
    if not rows:
        raise SystemExit(f"no rows parsed from {src}")
    with open(out, "w", newline="", encoding="utf-8-sig") as f:
        writer = csv.DictWriter(f, fieldnames=list(rows[0]))
        writer.writeheader()
        writer.writerows(rows)
    length = rows[-1]["mileage_m"] - rows[0]["mileage_m"]
    print(f"{Path(src).stem}: {len(rows)} points, mileage {rows[0]['mileage']}..{rows[-1]['mileage']} = {length/1000:.3f} km")


if __name__ == "__main__":
    main(sys.argv[1], sys.argv[2])
