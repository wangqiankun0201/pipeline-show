package com.petrochina.xnyqt.pims.corrosion.util;

import com.petrochina.xnyqt.pims.common.exception.BusinessException;
import com.petrochina.xnyqt.pims.common.util.NumberUtil;
import com.petrochina.xnyqt.pims.corrosion.model.CorrosionInspectionModel;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Name: InspectionExcelReader
 * @Description: 读取《内腐蚀数据.xlsx》模板（按表头名称定位列，兼容列顺序调整与不规范单元格）
 * @Version: V1.0.0
 * @Author: PIMS项目组
 * @Create Date: 2026-10-28
 */
public final class InspectionExcelReader {

    public static final String COL_SEGMENT_ID = "管段ID";
    public static final String COL_MINE = "气矿";
    public static final String COL_WORK_AREA = "作业区";
    public static final String COL_PIPELINE = "管线";
    public static final String COL_SEGMENT = "管段";
    public static final String COL_COMMISSION = "投运时间";
    public static final String COL_OUTER_DIAMETER = "外径(mm)";
    public static final String COL_WALL = "壁厚(mm)";
    public static final String COL_MATERIAL = "材质等级";
    public static final String COL_DESIGN_PRESSURE = "设计压力(MPa)";
    public static final String COL_OPERATING_PRESSURE = "运行压力(MPa)";
    public static final String COL_PRESSURE_FACTOR = "压力系数";
    public static final String COL_TEMPERATURE = "运行温度(℃)";
    public static final String COL_MEDIUM = "介质类型";
    public static final String COL_LIQUID = "是否积液";
    public static final String COL_CO2 = "CO2(mol%)";
    public static final String COL_H2S = "H2S(mol%)";
    public static final String COL_O2 = "O2(mol%)";
    public static final String COL_WATER_CUT = "含水率(%)";
    public static final String COL_CHLORIDE = "Cl-(mg/L)";
    public static final String COL_PH = "pH";
    public static final String COL_INHIBITOR = "缓蚀剂投加";
    public static final String COL_INHIBITOR_CONC = "缓蚀剂浓度";
    public static final String COL_PIGGING = "清管周期(天)";
    public static final String COL_INSPECTION_DATE = "内检时间";
    public static final String COL_DEFECT_COUNT = "内腐蚀缺陷数量";
    public static final String COL_MAX_DEPTH = "最大腐蚀深度(%)";
    public static final String COL_AVG_ERF = "平均ERF";
    public static final String COL_MAX_ERF = "最大ERF";
    public static final String COL_REMARK = "备注";

    //必需表头
    private static final List<String> REQUIRED = Arrays.asList(COL_SEGMENT_ID, COL_MINE, COL_PIPELINE, COL_SEGMENT,
            COL_COMMISSION, COL_WALL, COL_INSPECTION_DATE, COL_MAX_DEPTH);

    private static final List<DateTimeFormatter> DATE_PATTERNS = Arrays.asList(
            DateTimeFormatter.ofPattern("yyyy-MM-dd"), DateTimeFormatter.ofPattern("yyyy/M/d"),
            DateTimeFormatter.ofPattern("yyyy.M.d"), DateTimeFormatter.ofPattern("yyyy-M-d"));

    private final DataFormatter formatter = new DataFormatter();

    private Map<String, Integer> columns;

    /**
     * @Name: read
     * @Description: 读取第一个工作表的检测记录，无效行写入提示信息后跳过
     * @Author: PIMS项目组
     * @Create Date: 2026-10-28
     * @Parameters: InputStream input 文件流, List messages 提示信息（输出）
     * @Return: List CorrosionInspectionModel 检测记录（未设置物理管段ID）
     */
    public List<CorrosionInspectionModel> read(InputStream input, List<String> messages) {
        List<CorrosionInspectionModel> rows = new ArrayList<>();
        try (Workbook workbook = WorkbookFactory.create(input)) {
            Sheet sheet = workbook.getSheetAt(0);
            columns = readHeader(sheet.getRow(sheet.getFirstRowNum()));
            for (int i = sheet.getFirstRowNum() + 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                //跳过空行
                if (row == null || isBlankRow(row)) {
                    continue;
                }
                CorrosionInspectionModel model = readRow(row);
                if (model.getSourceSegmentId() == null || model.getPipelineName() == null
                        || model.getSegmentName() == null || model.getMine() == null) {
                    messages.add("第" + (i + 1) + "行缺少管段ID/气矿/管线/管段，已跳过");
                    continue;
                }
                rows.add(model);
            }
        } catch (IOException | RuntimeException e) {
            if (e instanceof BusinessException) {
                throw (BusinessException) e;
            }
            throw new BusinessException("Excel 文件读取失败，请使用《内腐蚀数据.xlsx》模板");
        }
        return rows;
    }

    private Map<String, Integer> readHeader(Row header) {
        if (header == null) {
            throw new BusinessException("Excel 缺少表头行");
        }
        Map<String, Integer> map = new HashMap<>();
        for (Cell cell : header) {
            map.put(normalize(formatter.formatCellValue(cell)), cell.getColumnIndex());
        }
        List<String> missing = new ArrayList<>();
        for (String name : REQUIRED) {
            if (!map.containsKey(normalize(name))) {
                missing.add(name);
            }
        }
        if (!missing.isEmpty()) {
            throw new BusinessException("Excel 缺少必需列：" + String.join("、", missing));
        }
        return map;
    }

    private CorrosionInspectionModel readRow(Row row) {
        CorrosionInspectionModel m = new CorrosionInspectionModel();
        m.setRowNo(row.getRowNum() + 1);
        Double segmentId = number(row, COL_SEGMENT_ID);
        m.setSourceSegmentId(segmentId == null ? null : segmentId.longValue());
        m.setMine(text(row, COL_MINE));
        m.setWorkArea(text(row, COL_WORK_AREA));
        m.setPipelineName(text(row, COL_PIPELINE));
        m.setSegmentName(text(row, COL_SEGMENT));
        m.setCommissionDate(date(row, COL_COMMISSION));
        m.setOuterDiameterMm(number(row, COL_OUTER_DIAMETER));
        m.setWallThicknessMm(number(row, COL_WALL));
        m.setMaterialGrade(text(row, COL_MATERIAL));
        m.setDesignPressureMpa(number(row, COL_DESIGN_PRESSURE));
        m.setOperatingPressureText(text(row, COL_OPERATING_PRESSURE));
        m.setOperatingPressureMpa(number(row, COL_OPERATING_PRESSURE));
        m.setPressureFactor(number(row, COL_PRESSURE_FACTOR));
        m.setOperatingTemperature(text(row, COL_TEMPERATURE));
        m.setMediumType(text(row, COL_MEDIUM));
        m.setLiquidAccumulation(text(row, COL_LIQUID));
        m.setCo2MolPct(number(row, COL_CO2));
        m.setH2sMolPct(number(row, COL_H2S));
        m.setO2MolPct(number(row, COL_O2));
        m.setWaterCutPct(number(row, COL_WATER_CUT));
        m.setChlorideMgL(number(row, COL_CHLORIDE));
        m.setPh(number(row, COL_PH));
        m.setInhibitorDosing(text(row, COL_INHIBITOR));
        m.setInhibitorConcentration(text(row, COL_INHIBITOR_CONC));
        m.setPiggingCycleDays(integer(row, COL_PIGGING));
        m.setInspectionDate(date(row, COL_INSPECTION_DATE));
        m.setDefectCount(integer(row, COL_DEFECT_COUNT));
        m.setMaxDepthPct(number(row, COL_MAX_DEPTH));
        m.setAvgErf(number(row, COL_AVG_ERF));
        m.setMaxErf(number(row, COL_MAX_ERF));
        m.setRemark(text(row, COL_REMARK));
        return m;
    }

    private Cell cell(Row row, String column) {
        Integer index = columns.get(normalize(column));
        return index == null ? null : row.getCell(index);
    }

    private String text(Row row, String column) {
        Cell cell = cell(row, column);
        String value = cell == null ? "" : formatter.formatCellValue(cell).trim();
        return value.isEmpty() ? null : value;
    }

    private Double number(Row row, String column) {
        Cell cell = cell(row, column);
        if (cell == null) {
            return null;
        }
        CellType type = cell.getCellType() == CellType.FORMULA ? cell.getCachedFormulaResultType() : cell.getCellType();
        //数值单元格直接取值，文本单元格宽松解析
        if (type == CellType.NUMERIC) {
            return cell.getNumericCellValue();
        }
        return type == CellType.STRING ? NumberUtil.parseDouble(cell.getStringCellValue()) : null;
    }

    private Integer integer(Row row, String column) {
        Double value = number(row, column);
        return value == null ? null : (int) Math.round(value);
    }

    private LocalDate date(Row row, String column) {
        Cell cell = cell(row, column);
        if (cell == null) {
            return null;
        }
        CellType type = cell.getCellType() == CellType.FORMULA ? cell.getCachedFormulaResultType() : cell.getCellType();
        if (type == CellType.NUMERIC && org.apache.poi.ss.usermodel.DateUtil.isCellDateFormatted(cell)) {
            return cell.getDateCellValue().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        }
        return type == CellType.STRING ? parseDate(cell.getStringCellValue().trim()) : null;
    }

    private LocalDate parseDate(String text) {
        for (DateTimeFormatter pattern : DATE_PATTERNS) {
            try {
                return LocalDate.parse(text, pattern);
            } catch (DateTimeParseException ignored) {
                //尝试下一种格式
            }
        }
        return null;
    }

    private boolean isBlankRow(Row row) {
        for (Cell cell : row) {
            if (!formatter.formatCellValue(cell).trim().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private static String normalize(String header) {
        return header == null ? "" : header.trim().replace("（", "(").replace("）", ")").replace(" ", "");
    }
}
