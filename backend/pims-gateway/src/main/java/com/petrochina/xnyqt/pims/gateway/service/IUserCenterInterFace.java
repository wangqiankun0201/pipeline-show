package com.petrochina.xnyqt.pims.gateway.service;

import com.petrochina.xnyqt.pims.common.context.CurrentUser;
import reactor.core.publisher.Mono;

/**
 * @Name: IUserCenterInterFace
 * @Description: 梦想云用户中心访问接口
 * @Version: V1.0.0
 * @Author: PIMS项目组
 * @Create Date: 2026-10-28
 */
public interface IUserCenterInterFace {

    /**
     * @Name: getCurrentUser
     * @Description: 使用令牌查询当前用户，令牌无效时返回空
     * @Author: PIMS项目组
     * @Create Date: 2026-10-28
     * @Parameters: String token 梦想云令牌
     * @Return: Mono CurrentUser 当前用户
     */
    Mono<CurrentUser> getCurrentUser(String token);

    /**
     * @Name: getMockUser
     * @Description: 关闭鉴权时返回本地开发用户
     * @Author: PIMS项目组
     * @Create Date: 2026-10-28
     * @Parameters: 无
     * @Return: CurrentUser 本地用户
     */
    CurrentUser getMockUser();
}
