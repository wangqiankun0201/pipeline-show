package com.petrochina.xnyqt.pims.gateway.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.petrochina.xnyqt.pims.common.ApiResult;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

/**
 * @Name: GatewayResponseUtil
 * @Description: 网关直接输出统一 JSON 返回
 * @Version: V1.0.0
 * @Author: PIMS项目组
 * @Create Date: 2026-10-28
 */
public final class GatewayResponseUtil {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private GatewayResponseUtil() {
    }

    /**
     * @Name: writeJson
     * @Description: 输出 JSON 返回并结束请求
     * @Author: PIMS项目组
     * @Create Date: 2026-10-28
     * @Parameters: ServerHttpResponse response 响应, HttpStatus status 状态码, ApiResult result 返回体
     * @Return: Mono Void
     */
    public static Mono<Void> writeJson(ServerHttpResponse response, HttpStatus status, ApiResult<?> result) {
        byte[] bytes;
        try {
            bytes = MAPPER.writeValueAsBytes(result);
        } catch (JsonProcessingException e) {
            bytes = "{\"code\":\"fail\",\"msg\":\"系统繁忙\"}".getBytes(StandardCharsets.UTF_8);
        }
        response.setStatusCode(status);
        response.getHeaders().set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        DataBuffer buffer = response.bufferFactory().wrap(bytes);
        return response.writeWith(Mono.just(buffer));
    }

    /**
     * @Name: resolveToken
     * @Description: 从 Authorization 请求头解析 Bearer 令牌
     * @Author: PIMS项目组
     * @Create Date: 2026-10-28
     * @Parameters: String authorization 请求头值
     * @Return: String 令牌，不存在返回空
     */
    public static String resolveToken(String authorization) {
        String prefix = "Bearer ";
        if (authorization == null || !authorization.regionMatches(true, 0, prefix, 0, prefix.length())) {
            return null;
        }
        String token = authorization.substring(prefix.length()).trim();
        return token.isEmpty() ? null : token;
    }
}
