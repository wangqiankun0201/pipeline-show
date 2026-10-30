package com.petrochina.xnyqt.pims.corrosion;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * @Name: PimsCorrosionApplication
 * @Description: 内腐蚀趋势预测服务启动类
 * @Version: V1.0.0
 * @Author: PIMS项目组
 * @Create Date: 2026-10-28
 */
@SpringBootApplication
@ConfigurationPropertiesScan
@MapperScan("com.petrochina.xnyqt.pims.corrosion.dao")
public class PimsCorrosionApplication {

    public static void main(String[] args) {
        SpringApplication.run(PimsCorrosionApplication.class, args);
    }
}
