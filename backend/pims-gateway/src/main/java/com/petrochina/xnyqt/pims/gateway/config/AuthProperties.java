package com.petrochina.xnyqt.pims.gateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * @Name: AuthProperties
 * @Description: 梦想云用户中心鉴权配置
 * @Version: V1.0.0
 * @Author: PIMS项目组
 * @Create Date: 2026-10-28
 */
@Data
@ConfigurationProperties(prefix = "pims.auth")
public class AuthProperties {

    //是否启用用户中心令牌校验（本地开发可关闭）
    private boolean enabled = true;

    //用户中心地址，如 http://user.api.pcep.cloud
    private String userCenterUrl = "http://user.api.dev.pcep.cloud";

    //当前用户接口路径
    private String currentUserPath = "/api/v2/sys/user/currentuser";

    //令牌校验结果缓存分钟数
    private long cacheMinutes = 5;

    //用户中心调用超时毫秒数
    private long timeoutMillis = 5000;

    //免鉴权路径
    private List<String> whitelist = new ArrayList<>();

    //关闭鉴权时使用的本地用户
    private String mockUserId = "local-dev";

    private String mockDisplayName = "本地开发用户";
}
