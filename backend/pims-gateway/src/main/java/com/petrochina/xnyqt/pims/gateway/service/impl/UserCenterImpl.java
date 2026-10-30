package com.petrochina.xnyqt.pims.gateway.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.petrochina.xnyqt.pims.common.context.CurrentUser;
import com.petrochina.xnyqt.pims.common.type.ResultCodeType;
import com.petrochina.xnyqt.pims.gateway.config.AuthProperties;
import com.petrochina.xnyqt.pims.gateway.service.IUserCenterInterFace;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * @Name: UserCenterImpl
 * @Description: 调用用户中心 /api/v2/sys/user/currentuser 校验令牌，结果按令牌短期缓存
 * @Version: V1.0.0
 * @Author: PIMS项目组
 * @Create Date: 2026-10-28
 */
@Slf4j
@Service
public class UserCenterImpl implements IUserCenterInterFace {

    private final AuthProperties authProperties;

    private final WebClient webClient;

    private final Cache<String, CurrentUser> userCache;

    public UserCenterImpl(AuthProperties authProperties, WebClient.Builder webClientBuilder) {
        this.authProperties = authProperties;
        this.webClient = webClientBuilder.baseUrl(authProperties.getUserCenterUrl()).build();
        this.userCache = Caffeine.newBuilder()
                .expireAfterWrite(authProperties.getCacheMinutes(), TimeUnit.MINUTES)
                .maximumSize(10000)
                .build();
    }

    @Override
    public Mono<CurrentUser> getCurrentUser(String token) {
        CurrentUser cached = userCache.getIfPresent(token);
        if (cached != null) {
            return Mono.just(cached);
        }
        return webClient.get()
                .uri(authProperties.getCurrentUserPath())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .timeout(Duration.ofMillis(authProperties.getTimeoutMillis()))
                .flatMap(this::parseUser)
                .doOnNext(user -> userCache.put(token, user))
                .onErrorResume(e -> {
                    log.warn("用户中心令牌校验失败：{}", e.getMessage());
                    return Mono.empty();
                });
    }

    @Override
    public CurrentUser getMockUser() {
        return new CurrentUser(authProperties.getMockUserId(), authProperties.getMockUserId(),
                authProperties.getMockDisplayName(), null);
    }

    /**
     * @Name: parseUser
     * @Description: 解析用户中心返回结构 {code, msg, data}
     * @Author: PIMS项目组
     * @Create Date: 2026-10-28
     * @Parameters: JsonNode body 返回体
     * @Return: Mono CurrentUser 用户
     */
    private Mono<CurrentUser> parseUser(JsonNode body) {
        JsonNode data = body.path("data");
        //code 非 success 或无用户ID均视为令牌无效
        if (!ResultCodeType.SUCCESS.getCode().equalsIgnoreCase(body.path("code").asText())
                || data.path("userId").isMissingNode()) {
            return Mono.empty();
        }
        return Mono.just(new CurrentUser(data.path("userId").asText(), data.path("loginName").asText(null),
                data.path("displayName").asText(null), data.path("belongOrg").asText(null)));
    }
}
