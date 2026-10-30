package com.petrochina.xnyqt.pims.gis;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * @Name: PimsGisApplication
 * @Description: 管道位置服务启动类
 * @Version: V1.0.0
 * @Author: PIMS项目组
 * @Create Date: 2026-10-28
 */
@SpringBootApplication
@ConfigurationPropertiesScan
@MapperScan("com.petrochina.xnyqt.pims.gis.dao")
public class PimsGisApplication {

    public static void main(String[] args) {
        SpringApplication.run(PimsGisApplication.class, args);
    }
}
