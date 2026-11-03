"""Gauss-Kruger (3-degree zone) inverse projection -> WGS84 lon/lat.

Coordinates in the survey tables are (northing x, zone+easting y), e.g.
x=3361778.844, y=36500006.505 -> zone 36, easting 500006.505, CM 108E.
"""
import math

A = 6378137.0                 # CGCS2000 semi-major axis
F = 1 / 298.257222101
E2 = F * (2 - F)
EP2 = E2 / (1 - E2)


def _meridian_arc(phi):
    e2 = E2
    return A * ((1 - e2 / 4 - 3 * e2 ** 2 / 64 - 5 * e2 ** 3 / 256) * phi
                - (3 * e2 / 8 + 3 * e2 ** 2 / 32 + 45 * e2 ** 3 / 1024) * math.sin(2 * phi)
                + (15 * e2 ** 2 / 256 + 45 * e2 ** 3 / 1024) * math.sin(4 * phi)
                - (35 * e2 ** 3 / 3072) * math.sin(6 * phi))


def _footpoint(northing):
    mu = northing / (A * (1 - E2 / 4 - 3 * E2 ** 2 / 64 - 5 * E2 ** 3 / 256))
    e1 = (1 - math.sqrt(1 - E2)) / (1 + math.sqrt(1 - E2))
    phi = (mu
           + (3 * e1 / 2 - 27 * e1 ** 3 / 32) * math.sin(2 * mu)
           + (21 * e1 ** 2 / 16 - 55 * e1 ** 4 / 32) * math.sin(4 * mu)
           + (151 * e1 ** 3 / 96) * math.sin(6 * mu)
           + (1097 * e1 ** 4 / 512) * math.sin(8 * mu))
    return phi


def inverse(x, y):
    """x: northing (m), y: zone-prefixed easting (m) -> (lon, lat) in degrees."""
    zone = int(y // 1_000_000)
    easting = y - zone * 1_000_000
    lon0 = math.radians(3 * zone)
    phi1 = _footpoint(x)
    sin1, cos1, tan1 = math.sin(phi1), math.cos(phi1), math.tan(phi1)
    n1 = A / math.sqrt(1 - E2 * sin1 ** 2)
    r1 = A * (1 - E2) / (1 - E2 * sin1 ** 2) ** 1.5
    t1 = tan1 ** 2
    c1 = EP2 * cos1 ** 2
    d = (easting - 500000.0) / n1
    lat = phi1 - (n1 * tan1 / r1) * (
        d ** 2 / 2
        - (5 + 3 * t1 + 10 * c1 - 4 * c1 ** 2 - 9 * EP2) * d ** 4 / 24
        + (61 + 90 * t1 + 298 * c1 + 45 * t1 ** 2 - 252 * EP2 - 3 * c1 ** 2) * d ** 6 / 720)
    lon = lon0 + (
        d
        - (1 + 2 * t1 + c1) * d ** 3 / 6
        + (5 - 2 * c1 + 28 * t1 - 3 * c1 ** 2 + 8 * EP2 + 24 * t1 ** 2) * d ** 5 / 120) / cos1
    return math.degrees(lon), math.degrees(lat)


def forward(lon, lat, zone=36):
    """Inverse check: (lon, lat) -> (northing, easting)."""
    lon0 = math.radians(3 * zone)
    phi, lam = math.radians(lat), math.radians(lon) - lon0
    sin1, cos1, tan1 = math.sin(phi), math.cos(phi), math.tan(phi)
    n1 = A / math.sqrt(1 - E2 * sin1 ** 2)
    t1 = tan1 ** 2
    c1 = EP2 * cos1 ** 2
    aa = lam * cos1
    m = _meridian_arc(phi)
    easting = 500000 + n1 * (aa + (1 - t1 + c1) * aa ** 3 / 6
                             + (5 - 18 * t1 + t1 ** 2 + 72 * c1 - 58 * EP2) * aa ** 5 / 120)
    northing = m + n1 * tan1 * (aa ** 2 / 2 + (5 - t1 + 9 * c1 + 4 * c1 ** 2) * aa ** 4 / 24
                                + (61 - 58 * t1 + t1 ** 2 + 600 * c1 - 330 * EP2) * aa ** 6 / 720)
    return northing, easting
