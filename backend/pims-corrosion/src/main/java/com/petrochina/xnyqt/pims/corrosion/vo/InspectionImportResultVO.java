package com.petrochina.xnyqt.pims.corrosion.vo;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

/**
 * @Name: InspectionImportResultVO
 * @Description: 内腐蚀数据导入结果
 * @Version: V1.0.0
 * @Author: PIMS项目组
 * @Create Date: 2026-10-28
 */
@Data
@ApiModel("内腐蚀数据导入结果")
//按字段名序列化，保证 kValue、tYears 等字段名与接口文档一致
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE,
        isGetterVisibility = JsonAutoDetect.Visibility.NONE)
public class InspectionImportResultVO {

    @ApiModelProperty("总行数")
    private Integer total;

    @ApiModelProperty("导入行数")
    private Integer imported;

    @ApiModelProperty("物理管段数")
    private Integer segmentCount;

    @ApiModelProperty("提示信息")
    private List<String> messages = new ArrayList<>();
}
