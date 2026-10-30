package com.petrochina.xnyqt.pims.corrosion.vo;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE,
        isGetterVisibility = JsonAutoDetect.Visibility.NONE)
public class PowerLawReportVO {
    private Long segmentId;
    private String segmentName;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate commissionDate;
    private Double wallThicknessMm;
    private Double kValue;
    private Double n;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate cutoffDate;
    @ApiModelProperty("内检日期拟合比较，不是独立验证；errorMm=计算值-实测值")
    private List<Comparison> comparison = new ArrayList<>();
    private List<CurvePointVO> curve = new ArrayList<>();
    private List<Annual> annual = new ArrayList<>();

    @Data
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE,
            isGetterVisibility = JsonAutoDetect.Visibility.NONE)
    public static class Comparison {
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate date;
        private Double tYears;
        private Double measuredDepthMm;
        private Double calculatedDepthMm;
        private Double errorMm;
    }

    @Data
    public static class Annual {
        private Integer year;
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate date;
        private Double depthMm;
        private boolean stopped;
    }
}
