package com.petrochina.xnyqt.pims.corrosion.algorithm;

import com.petrochina.xnyqt.pims.common.exception.BusinessException;
import com.petrochina.xnyqt.pims.common.util.DateUtil;
import java.time.LocalDate;

/** 用户指定参数，按段名识别；不得从内检重新拟合或由配置覆盖。 */
public enum FixedPowerLawModel {
    FENG_RU_A2("峰汝线A2段", 0.22517, 1.07087),
    WAN_WO_B("万卧线B段", 0.01697, 1.72263),
    WAN_WO_C("万卧线C段", 0.05313, 1.29786);

    private final String segmentName;
    private final double k;
    private final double n;

    FixedPowerLawModel(String segmentName, double k, double n) {
        this.segmentName = segmentName;
        this.k = k;
        this.n = n;
    }

    public double getK() { return k; }
    public double getN() { return n; }

    public static FixedPowerLawModel find(String name) {
        for (FixedPowerLawModel model : values()) {
            if (model.segmentName.equals(name)) { return model; }
        }
        return null;
    }

    public static FixedPowerLawModel require(String name) {
        FixedPowerLawModel model = find(name);
        if (model == null) { throw new BusinessException(name + " 未纳入固定参数预测范围"); }
        return model;
    }

    public void checkN(Double requestedN) {
        if (requestedN != null && Double.compare(requestedN, n) != 0) {
            throw new BusinessException("仅允许使用该管段固定指数 n=" + n + "，不支持自定义 n");
        }
    }

    public double depth(double years, double wall) {
        return Math.min(wall, PowerLawCorrosionAlgorithm.depthAt(k, years, n));
    }

    public double cutoffYears(double wall) {
        return PowerLawCorrosionAlgorithm.timeToReach(k, wall, n);
    }

    /** 小数年换算成天，按最近自然日输出，精确模型时间保留在曲线终止点 tYears 中。 */
    public LocalDate cutoffDate(LocalDate commission, double wall) {
        return DateUtil.plusYears(commission, cutoffYears(wall));
    }
}
