package com.petrochina.xnyqt.pims.common.context;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Name: CurrentUser
 * @Description: 当前登录用户（由网关校验梦想云令牌后透传）
 * @Version: V1.0.0
 * @Author: PIMS项目组
 * @Create Date: 2026-10-28
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CurrentUser {

    private String userId;

    private String loginName;

    private String displayName;

    private String orgId;
}
