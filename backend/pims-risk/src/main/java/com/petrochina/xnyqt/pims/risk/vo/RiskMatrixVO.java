package com.petrochina.xnyqt.pims.risk.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

/**
 * @Name: RiskMatrixVO
 * @Description: 风险矩阵配置
 * @Version: V1.0.0
 * @Author: PIMS项目组
 * @Create Date: 2026-10-28
 */
@Data
@ApiModel("风险矩阵配置")
public class RiskMatrixVO {

    @ApiModelProperty("失效可能性等级")
    private List<MatrixLevelVO> pofLevels = new ArrayList<>();

    @ApiModelProperty("失效后果等级")
    private List<MatrixLevelVO> cofLevels = new ArrayList<>();

    @ApiModelProperty("风险等级")
    private List<RiskLevelVO> riskLevels = new ArrayList<>();

    @ApiModelProperty("矩阵单元 cells[pof-1][cof-1]")
    private List<List<String>> cells = new ArrayList<>();
}
