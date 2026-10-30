package com.petrochina.xnyqt.pims.corrosion.algorithm;

import com.petrochina.xnyqt.pims.common.util.DateUtil;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @Name: PowerLawCorrosionAlgorithmTest
 * @Description: 以《幂律预测模型与参数初步标定技术说明》中的算例校核算法
 * @Version: V1.0.0
 * @Author: PIMS项目组
 * @Create Date: 2026-10-28
 */
class PowerLawCorrosionAlgorithmTest {

    @Test
    void shouldMatchFengruA2Example() {
        //技术说明 4.2：峰汝线A2段，投运 1990-10-01，内检 2023-03-14，壁厚 11mm，最大深度 85%
        double t = DateUtil.yearsBetween(LocalDate.of(1990, 10, 1), LocalDate.of(2023, 3, 14));
        double d0 = PowerLawCorrosionAlgorithm.initialDepth(11, 85);
        double k = PowerLawCorrosionAlgorithm.kValue(d0, t, 1.0);
        double fiveYears = PowerLawCorrosionAlgorithm.predictDepth(d0, t, 5, 1.0);
        assertEquals(32.4497, t, 1e-4);
        assertEquals(9.350, d0, 1e-9);
        assertEquals(0.2881, k, 1e-4);
        assertEquals(10.791, fiveYears, 1e-3);
        assertEquals(0.981, fiveYears / 11, 1e-3);
    }

    @Test
    void shouldMatchAppendixParameters() {
        //附录A：万卧线B段 投运 1989-06-01，内检 2015-10-13，壁厚 14mm，34%
        double t = DateUtil.yearsBetween(LocalDate.of(1989, 6, 1), LocalDate.of(2015, 10, 13));
        double d0 = PowerLawCorrosionAlgorithm.initialDepth(14, 34);
        assertEquals(26.37, t, 1e-2);
        assertEquals(0.1805, PowerLawCorrosionAlgorithm.kValue(d0, t, 1.0), 1e-4);
        assertEquals(5.663, PowerLawCorrosionAlgorithm.predictDepth(d0, t, 5, 1.0), 1e-3);
    }

    @Test
    void anchoredFormShouldEqualStandardForm() {
        //锚定式与标准式在任意 n 下等价：d₀(1+Δt/T)ⁿ = k(T+Δt)ⁿ
        double d0 = 1.74;
        double t = 21.1531;
        for (double n : new double[]{0.7, 1.0, 1.3}) {
            double k = PowerLawCorrosionAlgorithm.kValue(d0, t, n);
            assertEquals(PowerLawCorrosionAlgorithm.depthAt(k, t + 7, n),
                    PowerLawCorrosionAlgorithm.predictDepth(d0, t, 7, n), 1e-9);
            assertEquals(d0, PowerLawCorrosionAlgorithm.depthAt(k, t, n), 1e-9);
        }
    }

    @Test
    void shouldMatchApparentExponentTable() {
        //附录B：源ID 5，T1=21.243，T2=32.450，54%→85%，n_app=+1.0709
        assertEquals(1.0709, PowerLawCorrosionAlgorithm.apparentExponent(54, 85, 21.243, 32.450), 1e-3);
        //源ID 14：27%→26%，n_app=-0.1092
        assertEquals(-0.1092, PowerLawCorrosionAlgorithm.apparentExponent(27, 26, 22.612, 31.951), 1e-3);
    }

    @Test
    void yearsToReachShouldInvertPrediction() {
        double d0 = 9.35;
        double t = 32.4497;
        double dt = PowerLawCorrosionAlgorithm.yearsToReach(d0, t, 11, 1.0);
        assertEquals(11, PowerLawCorrosionAlgorithm.predictDepth(d0, t, dt, 1.0), 1e-9);
        assertEquals(0.0, PowerLawCorrosionAlgorithm.yearsToReach(12, t, 11, 1.0), 0.0);
    }
}
