package com.petrochina.xnyqt.pims.risk.type;

/**
 * @Name: RiskMatrixType
 * @Description: 5×5 风险矩阵默认配置（行：失效可能性 1~5，列：失效后果 1~5），待业务确认后可调整
 * @Version: V1.0.0
 * @Author: PIMS项目组
 * @Create Date: 2026-10-28
 */
public final class RiskMatrixType {

    public static final String[] POF_NAMES = {"极低", "低", "中", "高", "极高"};

    public static final String[] COF_NAMES = {"轻微", "较小", "中等", "严重", "灾难性"};

    public static final RiskLevelType[][] CELLS = {
            {RiskLevelType.LOW, RiskLevelType.LOW, RiskLevelType.LOW, RiskLevelType.MEDIUM, RiskLevelType.MEDIUM},
            {RiskLevelType.LOW, RiskLevelType.LOW, RiskLevelType.MEDIUM, RiskLevelType.MEDIUM, RiskLevelType.HIGH},
            {RiskLevelType.LOW, RiskLevelType.MEDIUM, RiskLevelType.MEDIUM, RiskLevelType.HIGH, RiskLevelType.HIGH},
            {RiskLevelType.MEDIUM, RiskLevelType.MEDIUM, RiskLevelType.HIGH, RiskLevelType.HIGH, RiskLevelType.VERY_HIGH},
            {RiskLevelType.MEDIUM, RiskLevelType.HIGH, RiskLevelType.HIGH, RiskLevelType.VERY_HIGH, RiskLevelType.VERY_HIGH}
    };

    private RiskMatrixType() {
    }

    /**
     * @Name: levelOf
     * @Description: 按失效可能性、失效后果等级查矩阵得到风险等级
     * @Author: PIMS项目组
     * @Create Date: 2026-10-28
     * @Parameters: int pofLevel 失效可能性等级(1~5), int cofLevel 失效后果等级(1~5)
     * @Return: RiskLevelType 风险等级
     */
    public static RiskLevelType levelOf(int pofLevel, int cofLevel) {
        return CELLS[pofLevel - 1][cofLevel - 1];
    }
}
