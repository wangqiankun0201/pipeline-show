package com.petrochina.xnyqt.pims.gateway.controller;

import com.petrochina.xnyqt.pims.common.ApiResult;
import com.petrochina.xnyqt.pims.common.context.CurrentUser;
import com.petrochina.xnyqt.pims.common.type.ResultCodeType;
import com.petrochina.xnyqt.pims.gateway.config.AuthProperties;
import com.petrochina.xnyqt.pims.gateway.service.IUserCenterInterFace;
import com.petrochina.xnyqt.pims.gateway.util.GatewayResponseUtil;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * @Name: AuthController
 * @Description: 当前登录用户查询（前端顶栏显示用户名）
 * @Version: V1.0.0
 * @Author: PIMS项目组
 * @Create Date: 2026-10-28
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthProperties authProperties;

    private final IUserCenterInterFace userCenterService;

    public AuthController(AuthProperties authProperties, IUserCenterInterFace userCenterService) {
        this.authProperties = authProperties;
        this.userCenterService = userCenterService;
    }

    /**
     * @Name: getCurrentUser
     * @Description: 根据令牌返回当前用户
     * @Author: PIMS项目组
     * @Create Date: 2026-10-28
     * @Parameters: String authorization 请求头
     * @Return: Mono ResponseEntity 当前用户
     */
    @GetMapping("/currentuser")
    public Mono<ResponseEntity<ApiResult<CurrentUser>>> getCurrentUser(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization) {
        if (!authProperties.isEnabled()) {
            return Mono.just(ResponseEntity.ok(ApiResult.success(userCenterService.getMockUser())));
        }
        String token = GatewayResponseUtil.resolveToken(authorization);
        ResponseEntity<ApiResult<CurrentUser>> unauthorized = ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResult.of(ResultCodeType.UNAUTHORIZED, null));
        if (token == null) {
            return Mono.just(unauthorized);
        }
        return userCenterService.getCurrentUser(token)
                .map(user -> ResponseEntity.ok(ApiResult.success(user)))
                .defaultIfEmpty(unauthorized);
    }
}
