package com.petrochina.xnyqt.pims.risk.vo;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import java.time.LocalDate;

@Data
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE,
        isGetterVisibility = JsonAutoDetect.Visibility.NONE)
public class GammaSegmentVO {
    private Long segmentId;
    private String segmentName;
    private String pipelineName;
    private LocalDate commissionDate;
    private double wallThicknessMm;
    private LocalDate inspectionDate;
    private double inspectionDepthMm;
    private double kValue;
    private double n;
    private String status;
}
