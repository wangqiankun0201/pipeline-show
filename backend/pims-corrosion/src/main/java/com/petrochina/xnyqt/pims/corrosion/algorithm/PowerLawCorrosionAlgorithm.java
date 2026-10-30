package com.petrochina.xnyqt.pims.corrosion.algorithm;

/**
 * @Name: PowerLawCorrosionAlgorithm
 * @Description: 管道内腐蚀幂律预测模型（V1.0，最近内检锚定形式）
 *               标准式 d(t) = k·tⁿ；系统实现 d̂(T+Δt) = d₀·(1+Δt/T)ⁿ，k = d₀/Tⁿ，d₀ = 壁厚×D₀/100
 *               依据《西南油气田管道内腐蚀幂律预测模型与参数初步标定技术说明》V1.0
 * @Version: V1.0.0
 * @Author: PIMS项目组
 * @Create Date: 2026-10-28
 */
public final class PowerLawCorrosionAlgorithm {

    private PowerLawCorrosionAlgorithm() {
    }

    /**
     * @Name: initialDepth
     * @Description: 由壁厚百分数换算最近内检最大腐蚀深度 d₀ = t_wall × D₀ / 100
     * @Author: PIMS项目组
     * @Create Date: 2026-10-28
     * @Parameters: double wallThicknessMm 壁厚(mm), double maxDepthPct 最大腐蚀深度(%)
     * @Return: double d₀(mm)
     */
    public static double initialDepth(double wallThicknessMm, double maxDepthPct) {
        return wallThicknessMm * maxDepthPct / 100.0;
    }

    /**
     * @Name: kValue
     * @Description: 反算腐蚀发展系数 k = d₀ / Tⁿ（单位 mm/年ⁿ，与 n 成组使用）
     * @Author: PIMS项目组
     * @Create Date: 2026-10-28
     * @Parameters: double d0 最近内检深度(mm), double tYears 投运至内检年数, double n 增长指数
     * @Return: double k
     */
    public static double kValue(double d0, double tYears, double n) {
        return d0 / Math.pow(tYears, n);
    }

    /**
     * @Name: predictDepth
     * @Description: 锚定形式预测 d̂(T+Δt) = d₀·(1+Δt/T)ⁿ，Δt 为目标日期距最近内检的年数
     * @Author: PIMS项目组
     * @Create Date: 2026-10-28
     * @Parameters: double d0 最近内检深度(mm), double tYears T, double deltaYears Δt, double n 增长指数
     * @Return: double 预测深度(mm)
     */
    public static double predictDepth(double d0, double tYears, double deltaYears, double n) {
        return d0 * Math.pow(1.0 + deltaYears / tYears, n);
    }

    /**
     * @Name: depthAt
     * @Description: 标准式 d(t) = k·tⁿ，t 为自暂定腐蚀起点（投运日）起算的年数
     * @Author: PIMS项目组
     * @Create Date: 2026-10-28
     * @Parameters: double k 发展系数, double tYears 年数, double n 增长指数
     * @Return: double 深度(mm)
     */
    public static double depthAt(double k, double tYears, double n) {
        return tYears <= 0 ? 0.0 : k * Math.pow(tYears, n);
    }

    /**
     * @Name: yearsToReach
     * @Description: 求深度达到指定值（如名义壁厚）所需的内检后年数 Δt = T·((d/d₀)^(1/n) − 1)
     * @Author: PIMS项目组
     * @Create Date: 2026-10-28
     * @Parameters: double d0 最近内检深度, double tYears T, double targetDepth 目标深度, double n 增长指数
     * @Return: double 内检后年数（d₀ 已不小于目标深度时返回 0）
     */
    public static double yearsToReach(double d0, double tYears, double targetDepth, double n) {
        if (d0 >= targetDepth) {
            return 0.0;
        }
        return tYears * (Math.pow(targetDepth / d0, 1.0 / n) - 1.0);
    }

    /**
     * @Name: timeToReach
     * @Description: 标准式下深度达到目标值的作用年数 t = (d/k)^(1/n)
     * @Author: PIMS项目组
     * @Create Date: 2026-10-28
     * @Parameters: double k 发展系数, double targetDepth 目标深度, double n 增长指数
     * @Return: double 自投运起年数
     */
    public static double timeToReach(double k, double targetDepth, double n) {
        return Math.pow(targetDepth / k, 1.0 / n);
    }

    /**
     * @Name: apparentExponent
     * @Description: 双期记录表观指数 n_app = ln(d₂/d₁) / ln(T₂/T₁)（最大值未证明为同一缺陷，仅为表观值）
     * @Author: PIMS项目组
     * @Create Date: 2026-10-28
     * @Parameters: double d1 第一期深度, double d2 第二期深度, double t1 第一期年数, double t2 第二期年数
     * @Return: double 表观指数
     */
    public static double apparentExponent(double d1, double d2, double t1, double t2) {
        return Math.log(d2 / d1) / Math.log(t2 / t1);
    }
}
