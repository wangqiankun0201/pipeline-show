package com.petrochina.xnyqt.pims.common.type;

/**
 * @Name: HeaderType
 * @Description: 网关向下游服务透传的请求头名称
 * @Version: V1.0.0
 * @Author: PIMS项目组
 * @Create Date: 2026-10-28
 */
public final class HeaderType {

    //用户ID
    public static final String USER_ID = "X-Pims-User-Id";

    //用户登录名
    public static final String LOGIN_NAME = "X-Pims-Login-Name";

    //用户姓名（URL 编码）
    public static final String DISPLAY_NAME = "X-Pims-Display-Name";

    //机构ID
    public static final String ORG_ID = "X-Pims-Org-Id";

    private HeaderType() {
    }
}
