package com.petrochina.xnyqt.pims.corrosion.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @Name: CorrosionModelProperties
 * @Description: 内腐蚀幂律模型配置（模型版本、兼容字段、阈值）
 * @Version: V1.0.0
 * @Author: PIMS项目组
 * @Create Date: 2026-10-28
 */
@Data
@ConfigurationProperties(prefix = "pims.corrosion")
public class CorrosionModelProperties {

    private String modelName = "投运日起固定参数幂律模型";

    private String modelVersion = "V2.0-fixed";

    //兼容旧配置字段，不参与计算
    private double baseN = 1.0;

    //兼容旧配置字段，不再执行情景计算
    private List<Double> scenarioNs = new ArrayList<>();

    //关注阈值：预测深度占名义壁厚比例
    private double attentionRatio = 0.8;

    //默认预测内检后年数
    private double defaultYearsAfterIli = 5.0;

    //趋势曲线步长（年）
    private double curveStepYears = 0.5;

    //最近内检距今超过该年数时提示数据时效性
    private double staleInspectionYears = 3.0;

    //名义作用时间低于该年数时提示核实时间原点
    private double shortServiceYears = 5.0;

    //本地/首次部署是否导入内置种子数据
    private boolean seedEnabled = false;

    private String seedLocation = "classpath:seed/corrosion-inspection-seed.xlsx";
}
