package com.petrochina.xnyqt.pims.risk.algorithm;

import com.petrochina.xnyqt.pims.common.exception.BusinessException;
import com.petrochina.xnyqt.pims.risk.vo.GammaPredictionVO;
import org.apache.commons.math3.distribution.GammaDistribution;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.Well19937c;
import org.apache.commons.math3.special.Gamma;
import org.springframework.stereotype.Component;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

/** 条件Gamma过程：D(T)=dI+Gamma(k*(T^n-TI^n)/theta, theta)。 */
@Component
public class GammaRiskAlgorithm {
    public static double years(LocalDate start, LocalDate end) {
        return ChronoUnit.DAYS.between(start, end) / 365.2422;
    }

    public double shape(double t, double ti, double k, double n, double theta) {
        if (!Double.isFinite(t) || !Double.isFinite(ti) || t < ti || ti < 0
                || !Double.isFinite(k) || k <= 0 || !Double.isFinite(n) || n <= 0
                || !Double.isFinite(theta) || theta <= 0) {
            throw new BusinessException("Gamma模型时间或参数无效");
        }
        double value = k * (Math.pow(t, n) - Math.pow(ti, n)) / theta;
        finite(value);
        return value;
    }

    public double probability(double shape, double remaining, double theta) {
        if (!Double.isFinite(shape) || shape < 0 || !Double.isFinite(remaining)
                || !Double.isFinite(theta) || theta <= 0) {
            throw new BusinessException("Gamma概率输入无效");
        }
        if (remaining <= 0) { return 1; }
        if (shape == 0) { return 0; }
        //直接计算上尾，禁止用1-CDF，避免极小概率的消减误差。
        double result = Gamma.regularizedGammaQ(shape, remaining / theta, 1e-14, 100000);
        finite(result);
        if (result < 0 || result > 1) { throw new BusinessException("Gamma概率计算超出有效范围"); }
        return result;
    }

    public void simulate(GammaPredictionVO result) {
        List<GammaPredictionVO.Annual> annual = result.getAnnual();
        int size = annual.size();
        int[] failures = new int[size];
        int[] prefixFailures = new int[size];
        int count = result.getSampleCount();
        int half = count / 2;
        RandomGenerator random = new Well19937c(result.getSeed());
        GammaDistribution[] increments = new GammaDistribution[size];
        double previousShape = 0;
        for (int j = 0; j < size; j++) {
            double incrementShape = annual.get(j).getShape() - previousShape;
            if (incrementShape > 0) {
                increments[j] = new GammaDistribution(random, incrementShape, result.getTheta());
            }
            previousShape = annual.get(j).getShape();
        }
        double g0 = result.getWallThicknessMm() - result.getInspectionDepthMm();
        //按路径优先采样，使相同seed下任意N共享前N条完整路径。
        for (int i = 0; i < count; i++) {
            double growth = 0;
            for (int j = 0; j < size; j++) {
                if (increments[j] != null) { growth += increments[j].sample(); }
                finite(growth);
                if (g0 - growth <= 0) {
                    failures[j]++;
                    if (i < half) { prefixFailures[j]++; }
                }
            }
        }
        for (int j = 0; j < size; j++) {
            GammaPredictionVO.Annual row = annual.get(j);
            double p = failures[j] / (double) count;
            double se = Math.sqrt(p * (1 - p) / count);
            row.setFailedSamples(failures[j]);
            row.setMonteCarloProbability(p);
            row.setStandardError(se);
            //Wilson双侧95%区间，边界p=0/1时仍保持有效覆盖。
            double z = 1.959963984540054;
            double z2 = z * z;
            double denominator = 1 + z2 / count;
            double center = (p + z2 / (2 * count)) / denominator;
            double radius = z * Math.sqrt(p * (1 - p) / count + z2 / (4.0 * count * count)) / denominator;
            row.setConfidenceLow(failures[j] == 0 ? 0 : Math.max(0, center - radius));
            row.setConfidenceHigh(failures[j] == count ? 1 : Math.min(1, center + radius));
            if (failures[j] == 0) {
                row.setZeroFailureUpper95(-Math.expm1(Math.log(0.05) / count));
                String warning = "部分年份模拟未观测失败，模拟概率0不表示解析概率为0；请参考对应年份的单侧95%概率上界";
                if (!result.getWarnings().contains(warning)) { result.getWarnings().add(warning); }
            }
            convergence(result, row.getYear(), half, prefixFailures[j]);
            convergence(result, row.getYear(), count, failures[j]);
        }
    }

    private void convergence(GammaPredictionVO result, int year, int count, int failed) {
        GammaPredictionVO.Convergence point = new GammaPredictionVO.Convergence();
        point.setYear(year);
        point.setSampleCount(count);
        double p = failed / (double) count;
        point.setProbability(p);
        point.setStandardError(Math.sqrt(p * (1 - p) / count));
        result.getConvergence().add(point);
    }

    public static void finite(double value) {
        if (!Double.isFinite(value)) { throw new BusinessException("Gamma模型计算出现非有限数值，请检查输入数据"); }
    }
}
