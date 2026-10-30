package com.petrochina.xnyqt.pims.gateway.filter;

import com.petrochina.xnyqt.pims.common.ApiResult;
import com.petrochina.xnyqt.pims.gateway.util.GatewayResponseUtil;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.EnumSet;
import java.util.Set;

/**
 * @Name: HttpMethodGlobalFilter
 * @Description: 安全规范：仅开放 GET、HEAD、POST 方法，其余方法直接拒绝
 * @Version: V1.0.0
 * @Author: PIMS项目组
 * @Create Date: 2026-10-28
 */
@Component
public class HttpMethodGlobalFilter implements GlobalFilter, Ordered {

    private static final Set<HttpMethod> ALLOWED = EnumSet.of(HttpMethod.GET, HttpMethod.HEAD, HttpMethod.POST);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        HttpMethod method = exchange.getRequest().getMethod();
        if (method == null || !ALLOWED.contains(method)) {
            return GatewayResponseUtil.writeJson(exchange.getResponse(), HttpStatus.METHOD_NOT_ALLOWED,
                    ApiResult.fail("不支持的请求方法"));
        }
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return -300;
    }
}
