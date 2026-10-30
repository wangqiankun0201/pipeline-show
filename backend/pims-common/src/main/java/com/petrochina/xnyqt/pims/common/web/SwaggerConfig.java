package com.petrochina.xnyqt.pims.common.web;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.servlet.mvc.method.RequestMappingInfoHandlerMapping;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.spring.web.plugins.WebMvcRequestHandlerProvider;

import java.lang.reflect.Field;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Name: SwaggerConfig
 * @Description: Swagger API 文档配置（规范要求微服务输出 Swagger 文档用于联调）
 * @Version: V1.0.0
 * @Author: PIMS项目组
 * @Create Date: 2026-10-28
 */
@Configuration
@ConditionalOnClass(Docket.class)
@ConditionalOnProperty(prefix = "pims.swagger", name = "enabled", havingValue = "true", matchIfMissing = true)
public class SwaggerConfig {

    @Value("${pims.swagger.title:PIMS 接口文档}")
    private String title;

    @Value("${pims.swagger.base-package:com.petrochina.xnyqt.pims}")
    private String basePackage;

    @Bean
    public Docket pimsDocket() {
        return new Docket(DocumentationType.OAS_30)
                .apiInfo(new ApiInfoBuilder().title(title).description("管道完整性展示平台").version("V1.0.0").build())
                .select()
                .apis(RequestHandlerSelectors.basePackage(basePackage))
                .paths(PathSelectors.any())
                .build();
    }

    /**
     * @Name: springfoxHandlerProviderBeanPostProcessor
     * @Description: 兼容 Spring Boot 2.6 + Actuator：过滤使用 PathPatternParser 的映射，避免 springfox 启动报错
     * @Author: PIMS项目组
     * @Create Date: 2026-10-28
     * @Parameters: 无
     * @Return: BeanPostProcessor 后置处理器
     */
    @Bean
    public static BeanPostProcessor springfoxHandlerProviderBeanPostProcessor() {
        return new BeanPostProcessor() {
            @Override
            public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
                if (bean instanceof WebMvcRequestHandlerProvider) {
                    List<RequestMappingInfoHandlerMapping> mappings = getHandlerMappings(bean);
                    List<RequestMappingInfoHandlerMapping> kept = mappings.stream()
                            .filter(mapping -> mapping.getPatternParser() == null)
                            .collect(Collectors.toList());
                    mappings.clear();
                    mappings.addAll(kept);
                }
                return bean;
            }

            @SuppressWarnings("unchecked")
            private List<RequestMappingInfoHandlerMapping> getHandlerMappings(Object bean) {
                Field field = ReflectionUtils.findField(bean.getClass(), "handlerMappings");
                ReflectionUtils.makeAccessible(field);
                return (List<RequestMappingInfoHandlerMapping>) ReflectionUtils.getField(field, bean);
            }
        };
    }
}
