package com.petrochina.xnyqt.pims.risk.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @Name: RiskFactorModel
 * @Description: 风险因素定义，对应表 t_risk_factor
 * @Version: V1.0.0
 * @Author: PIMS项目组
 * @Create Date: 2026-10-28
 */
@Data
@TableName("t_risk_factor")
public class RiskFactorModel {

    //因素编码
    @TableId(type = IdType.INPUT)
    private String factorCode;

    //分组 POF/COF
    private String groupCode;

    //因素名称
    private String factorName;

    //单位
    private String unit;

    //输入类型 number/select
    private String inputType;

    //数据来源 CORROSION/GIS/MANUAL
    private String source;

    //选项 JSON [{label,value}]
    private String optionsJson;

    //是否必填
    private Boolean required;

    //最小值
    private Double minValue;

    //最大值
    private Double maxValue;

    //排序
    private Integer sortOrder;

    //是否启用
    private Boolean enabled;
}
