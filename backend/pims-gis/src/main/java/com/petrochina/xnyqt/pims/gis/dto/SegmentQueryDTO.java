package com.petrochina.xnyqt.pims.gis.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Size;

/**
 * @Name: SegmentQueryDTO
 * @Description: 管段查询条件
 * @Version: V1.0.0
 * @Author: PIMS项目组
 * @Create Date: 2026-10-28
 */
@Data
@ApiModel("管段查询条件")
public class SegmentQueryDTO {

    @ApiModelProperty("气矿")
    @Size(max = 64, message = "气矿名称过长")
    private String mine;

    @ApiModelProperty("管线")
    @Size(max = 128, message = "管线名称过长")
    private String pipelineName;

    @ApiModelProperty("管段名称关键字")
    @Size(max = 64, message = "关键字过长")
    private String keyword;
}
