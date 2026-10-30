package com.petrochina.xnyqt.pims.gateway.filter;

import com.petrochina.xnyqt.pims.common.ApiResult;
import com.petrochina.xnyqt.pims.common.context.CurrentUser;
import com.petrochina.xnyqt.pims.common.type.HeaderType;
import com.petrochina.xnyqt.pims.common.type.ResultCodeType;
import com.petrochina.xnyqt.pims.gateway.config.AuthProperties;
import com.petrochina.xnyqt.pims.gateway.service.IUserCenterInterFace;
import com.petrochina.xnyqt.pims.gateway.util.GatewayResponseUtil;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * @Name: AuthGlobalFilter
 * @Description: 统一鉴权：校验梦想云令牌，并将用户信息透传给下游服务
 * @Version: V1.0.0
 * @Author: PIMS项目组
 * @Create Date: 2026-10-28
 */
@Component
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    private final AuthProperties authProperties;

    private final IUserCenterInterFace userCenterService;

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public AuthGlobalFilter(AuthProperties authProperties, IUserCenterInterFace userCenterService) {
        this.authProperties = authProperties;
        this.userCenterService = userCenterService;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        //先清除外部伪造的用户透传头
        ServerHttpRequest cleaned = request.mutate().headers(headers -> {
            headers.remove(HeaderType.USER_ID);
            headers.remove(HeaderType.LOGIN_NAME);
            headers.remove(HeaderType.DISPLAY_NAME);
            headers.remove(HeaderType.ORG_ID);
        }).build();
        ServerWebExchange cleanedExchange = exchange.mutate().request(cleaned).build();
        String path = request.getURI().getPath();
        //白名单路径不校验
        if (isWhitelisted(path)) {
            return chain.filter(cleanedExchange);
        }
        //关闭鉴权时使用本地用户
        if (!authProperties.isEnabled()) {
            return chain.filter(withUser(cleanedExchange, userCenterService.getMockUser()));
        }
        String token = GatewayResponseUtil.resolveToken(request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION));
        if (token == null) {
            return unauthorized(exchange);
        }
        return userCenterService.getCurrentUser(token)
                .flatMap(user -> chain.filter(withUser(cleanedExchange, user)))
                .switchIfEmpty(Mono.defer(() -> unauthorized(exchange)));
    }

    @Override
    public int getOrder() {
        return -200;
    }

    private boolean isWhitelisted(String path) {
        return authProperties.getWhitelist().stream().anyMatch(pattern -> pathMatcher.match(pattern, path));
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange) {
        return GatewayResponseUtil.writeJson(exchange.getResponse(), HttpStatus.UNAUTHORIZED,
                ApiResult.of(ResultCodeType.UNAUTHORIZED, null));
    }

    private ServerWebExchange withUser(ServerWebExchange exchange, CurrentUser user) {
        ServerHttpRequest request = exchange.getRequest().mutate().headers(headers -> {
            headers.set(HeaderType.USER_ID, user.getUserId());
            if (user.getLoginName() != null) {
                headers.set(HeaderType.LOGIN_NAME, user.getLoginName());
            }
            if (user.getDisplayName() != null) {
                headers.set(HeaderType.DISPLAY_NAME, encode(user.getDisplayName()));
            }
            if (user.getOrgId() != null) {
                headers.set(HeaderType.ORG_ID, user.getOrgId());
            }
        }).build();
        return exchange.mutate().request(request).build();
    }

    private String encode(String value) {
        try {
            return URLEncoder.encode(value, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return value;
        }
    }
}
