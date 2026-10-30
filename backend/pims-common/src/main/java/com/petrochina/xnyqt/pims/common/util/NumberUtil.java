package com.petrochina.xnyqt.pims.common.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * @Name: NumberUtil
 * @Description: 数值工具类
 * @Version: V1.0.0
 * @Author: PIMS项目组
 * @Create Date: 2026-10-28
 */
public final class NumberUtil {

    private NumberUtil() {
    }

    /**
     * @Name: round
     * @Description: 四舍五入保留指定小数位，空值返回空
     * @Author: PIMS项目组
     * @Create Date: 2026-10-28
     * @Parameters: Double value 原值, int scale 小数位
     * @Return: Double 结果
     */
    public static Double round(Double value, int scale) {
        if (value == null || value.isNaN() || value.isInfinite()) {
            return null;
        }
        return BigDecimal.valueOf(value).setScale(scale, RoundingMode.HALF_UP).doubleValue();
    }

    /**
     * @Name: parseDouble
     * @Description: 宽松解析数值文本，无法解析（如区间值、横杠）返回空
     * @Author: PIMS项目组
     * @Create Date: 2026-10-28
     * @Parameters: String text 文本
     * @Return: Double 数值
     */
    public static Double parseDouble(String text) {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }
        try {
            return Double.valueOf(text.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
