package com.petrochina.xnyqt.pims.gis.vo;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @Name: GeoImportResultVO
 * @Description: GIS 数据导入结果
 * @Version: V1.0.0
 * @Author: PIMS项目组
 * @Create Date: 2026-10-28
 */
@Data
@ApiModel("GIS 数据导入结果")
public class GeoImportResultVO {

    private int total;

    private int updated;

    private int skipped;

    private List<String> messages = new ArrayList<>();
}
