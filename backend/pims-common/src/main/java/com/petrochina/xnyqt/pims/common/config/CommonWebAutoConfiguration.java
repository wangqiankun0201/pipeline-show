package com.petrochina.xnyqt.pims.common.config;

import com.petrochina.xnyqt.pims.common.web.GlobalExceptionHandler;
import com.petrochina.xnyqt.pims.common.web.SwaggerConfig;
import com.petrochina.xnyqt.pims.common.web.WebMvcConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * @Name: CommonWebAutoConfiguration
 * @Description: Servlet 业务服务公共自动配置（网关为 WebFlux，不会加载）
 * @Version: V1.0.0
 * @Author: PIMS项目组
 * @Create Date: 2026-10-28
 */
@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@Import({GlobalExceptionHandler.class, WebMvcConfig.class, SwaggerConfig.class})
public class CommonWebAutoConfiguration {
}
