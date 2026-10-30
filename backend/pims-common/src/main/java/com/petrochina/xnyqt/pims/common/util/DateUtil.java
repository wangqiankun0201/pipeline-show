package com.petrochina.xnyqt.pims.common.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * @Name: DateUtil
 * @Description: 日期工具类，年数统一按回归年 365.2422 天/年换算（与《参数初步标定_v1》口径一致）
 * @Version: V1.0.0
 * @Author: PIMS项目组
 * @Create Date: 2026-10-28
 */
public final class DateUtil {

    //每年天数（回归年，复核技术说明附录 T 值确定）
    public static final double DAYS_PER_YEAR = 365.2422;

    public static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private DateUtil() {
    }

    /**
     * @Name: yearsBetween
     * @Description: 计算两个日期之间的年数（小数）
     * @Author: PIMS项目组
     * @Create Date: 2026-10-28
     * @Parameters: LocalDate start 起始日期, LocalDate end 截止日期
     * @Return: double 年数
     */
    public static double yearsBetween(LocalDate start, LocalDate end) {
        return ChronoUnit.DAYS.between(start, end) / DAYS_PER_YEAR;
    }

    /**
     * @Name: plusYears
     * @Description: 日期加上小数年数，结果按天取整
     * @Author: PIMS项目组
     * @Create Date: 2026-10-28
     * @Parameters: LocalDate date 基准日期, double years 年数
     * @Return: LocalDate 结果日期
     */
    public static LocalDate plusYears(LocalDate date, double years) {
        return date.plusDays(Math.round(years * DAYS_PER_YEAR));
    }

    public static String format(LocalDate date) {
        return date == null ? null : date.format(DATE_FORMAT);
    }
}
