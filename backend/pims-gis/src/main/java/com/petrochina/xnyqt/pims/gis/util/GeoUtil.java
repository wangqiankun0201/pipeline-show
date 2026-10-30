package com.petrochina.xnyqt.pims.gis.util;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;

/**
 * @Name: GeoUtil
 * @Description: GeoJSON 线几何工具（WGS84），支持 LineString / MultiLineString
 * @Version: V1.0.0
 * @Author: PIMS项目组
 * @Create Date: 2026-10-28
 */
public final class GeoUtil {

    //地球平均半径（km）
    private static final double EARTH_RADIUS_KM = 6371.0088;

    public static final String LINE_STRING = "LineString";

    public static final String MULTI_LINE_STRING = "MultiLineString";

    private GeoUtil() {
    }

    /**
     * @Name: listLines
     * @Description: 将线几何拆分为坐标串列表，每个坐标为 [经度, 纬度]
     * @Author: PIMS项目组
     * @Create Date: 2026-10-28
     * @Parameters: JsonNode geometry GeoJSON 几何
     * @Return: List 坐标串列表
     */
    public static List<List<double[]>> listLines(JsonNode geometry) {
        List<List<double[]>> lines = new ArrayList<>();
        String type = geometry.path("type").asText();
        JsonNode coordinates = geometry.path("coordinates");
        if (LINE_STRING.equals(type)) {
            lines.add(toPoints(coordinates));
        } else if (MULTI_LINE_STRING.equals(type)) {
            for (JsonNode line : coordinates) {
                lines.add(toPoints(line));
            }
        }
        return lines;
    }

    /**
     * @Name: validateLine
     * @Description: 校验线几何，返回错误描述，合法返回空
     * @Author: PIMS项目组
     * @Create Date: 2026-10-28
     * @Parameters: JsonNode geometry GeoJSON 几何
     * @Return: String 错误描述
     */
    public static String validateLine(JsonNode geometry) {
        String type = geometry == null ? "" : geometry.path("type").asText();
        if (!LINE_STRING.equals(type) && !MULTI_LINE_STRING.equals(type)) {
            return "几何类型须为 LineString 或 MultiLineString";
        }
        List<List<double[]>> lines = listLines(geometry);
        String error = null;
        for (List<double[]> line : lines) {
            //每条线至少两个点
            if (line.size() < 2) {
                error = "线几何至少包含两个坐标点";
            }
            for (double[] point : line) {
                //经纬度范围校验（WGS84）
                if (Math.abs(point[0]) > 180 || Math.abs(point[1]) > 90) {
                    error = "坐标超出 WGS84 经纬度范围";
                }
            }
        }
        return lines.isEmpty() ? "几何坐标为空" : error;
    }

    /**
     * @Name: lengthKm
     * @Description: 按球面距离（Haversine）计算线长度
     * @Author: PIMS项目组
     * @Create Date: 2026-10-28
     * @Parameters: JsonNode geometry GeoJSON 几何
     * @Return: double 长度（km）
     */
    public static double lengthKm(JsonNode geometry) {
        double total = 0;
        for (List<double[]> line : listLines(geometry)) {
            for (int i = 1; i < line.size(); i++) {
                total += haversineKm(line.get(i - 1), line.get(i));
            }
        }
        return total;
    }

    public static double[] firstPoint(JsonNode geometry) {
        List<List<double[]>> lines = listLines(geometry);
        return lines.isEmpty() || lines.get(0).isEmpty() ? null : lines.get(0).get(0);
    }

    public static double[] lastPoint(JsonNode geometry) {
        List<List<double[]>> lines = listLines(geometry);
        if (lines.isEmpty()) {
            return null;
        }
        List<double[]> last = lines.get(lines.size() - 1);
        return last.isEmpty() ? null : last.get(last.size() - 1);
    }

    private static double haversineKm(double[] a, double[] b) {
        double lat1 = Math.toRadians(a[1]);
        double lat2 = Math.toRadians(b[1]);
        double dLat = lat2 - lat1;
        double dLon = Math.toRadians(b[0] - a[0]);
        double h = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(lat1) * Math.cos(lat2) * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return 2 * EARTH_RADIUS_KM * Math.asin(Math.sqrt(h));
    }

    private static List<double[]> toPoints(JsonNode line) {
        List<double[]> points = new ArrayList<>();
        for (JsonNode coordinate : line) {
            points.add(new double[]{coordinate.path(0).asDouble(), coordinate.path(1).asDouble()});
        }
        return points;
    }
}
