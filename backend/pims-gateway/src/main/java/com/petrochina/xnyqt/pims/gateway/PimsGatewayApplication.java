package com.petrochina.xnyqt.pims.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * @Name: PimsGatewayApplication
 * @Description: 管道完整性展示平台网关启动类
 * @Version: V1.0.0
 * @Author: PIMS项目组
 * @Create Date: 2026-10-28
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class PimsGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(PimsGatewayApplication.class, args);
    }
}
