package com.petrochina.xnyqt.pims.risk.dto;

import lombok.Data;
import javax.validation.constraints.*;

@Data
public class GammaPredictRequestDTO {
    @NotNull(message = "请选择管段")
    private Long segmentId;
    @DecimalMin(value = "0.001", message = "theta须在0.001至5之间")
    @DecimalMax(value = "5", message = "theta须在0.001至5之间")
    private Double theta;
    @Min(value = 1000, message = "sampleCount须在1000至200000之间")
    @Max(value = 200000, message = "sampleCount须在1000至200000之间")
    private Integer sampleCount;
    private Long seed;
}
