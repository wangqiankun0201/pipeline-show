package com.petrochina.xnyqt.pims.risk.vo;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.Data;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE,
        isGetterVisibility = JsonAutoDetect.Visibility.NONE)
public class GammaPredictionVO {
    private Long segmentId;
    private String segmentName;
    private String modelVersion;
    private LocalDate commissionDate;
    private LocalDate inspectionDate;
    private double wallThicknessMm;
    private double inspectionDepthMm;
    private double kValue;
    private double n;
    private double theta;
    private int sampleCount;
    private long seed;
    private String status;
    private List<String> warnings = new ArrayList<>();
    private List<Annual> annual = new ArrayList<>();
    private List<Convergence> convergence = new ArrayList<>();
    private List<History> history = new ArrayList<>();

    @Data
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE,
            isGetterVisibility = JsonAutoDetect.Visibility.NONE)
    public static class Annual {
        private int year;
        private LocalDate date;
        private double tYears;
        private double shape;
        private double meanDepthMm;
        private double cappedMeanDepthMm;
        //物理展示剩余壁厚，最小为0；概率仍使用未截断增长。
        private double remainingWallMm;
        private double probability;
        private double monteCarloProbability;
        private int failedSamples;
        private double standardError;
        private double confidenceLow;
        private double confidenceHigh;
        //仅未观测到失败时有值，单侧95%精确上界。
        private Double zeroFailureUpper95;
    }

    @Data
    public static class Convergence {
        private int sampleCount;
        private int year;
        //共用路径前缀的Monte Carlo概率。
        private double probability;
        private double standardError;
    }

    @Data
    public static class History {
        private LocalDate date;
        private double measuredDepthMm;
        //未截断固定幂律k*t^n，用于对照历史测量，不作条件反向预测。
        private double calculatedDepthMm;
    }
}
