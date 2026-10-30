package com.petrochina.xnyqt.pims.risk;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * @Name: PimsRiskApplication
 * @Description: 失效风险计算服务启动类
 * @Version: V1.0.0
 * @Author: PIMS项目组
 * @Create Date: 2026-10-28
 */
@SpringBootApplication
@ConfigurationPropertiesScan
@EnableFeignClients
@MapperScan("com.petrochina.xnyqt.pims.risk.dao")
public class PimsRiskApplication {

    public static void main(String[] args) {
        SpringApplication.run(PimsRiskApplication.class, args);
    }
}
