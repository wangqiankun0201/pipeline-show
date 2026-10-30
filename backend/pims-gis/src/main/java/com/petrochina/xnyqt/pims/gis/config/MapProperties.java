package com.petrochina.xnyqt.pims.gis.config;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * @Name: MapProperties
 * @Description: 地图底图配置（内网上线时在 Nacos 中替换为天地图/区域湖瓦片服务）
 * @Version: V1.0.0
 * @Author: PIMS项目组
 * @Create Date: 2026-10-28
 */
@Data
@ApiModel("地图配置")
@ConfigurationProperties(prefix = "pims.map")
public class MapProperties {

    //初始中心点 [经度, 纬度]
    private List<Double> center = new ArrayList<>();

    //初始缩放级别
    private Integer zoom = 9;

    private List<Basemap> basemaps = new ArrayList<>();

    /**
     * @Name: Basemap
     * @Description: 底图图层
     * @Version: V1.0.0
     * @Author: PIMS项目组
     * @Create Date: 2026-10-28
     */
    @Data
    public static class Basemap {

        private String key;

        private String name;

        //XYZ 瓦片地址模板
        private String url;

        private String attribution;

        //注记图层地址（天地图影像/矢量需叠加注记），可为空
        private String annotationUrl;
    }
}
