package com.petrochina.xnyqt.pims.common.context;

/**
 * @Name: UserContext
 * @Description: 当前请求用户上下文（线程级）
 * @Version: V1.0.0
 * @Author: PIMS项目组
 * @Create Date: 2026-10-28
 */
public final class UserContext {

    private static final ThreadLocal<CurrentUser> HOLDER = new ThreadLocal<>();

    private UserContext() {
    }

    public static void set(CurrentUser user) {
        HOLDER.set(user);
    }

    public static CurrentUser get() {
        return HOLDER.get();
    }

    /**
     * @Name: getOperatorName
     * @Description: 获取操作人名称，用于计算记录留痕
     * @Author: PIMS项目组
     * @Create Date: 2026-10-28
     * @Parameters: 无
     * @Return: String 操作人
     */
    public static String getOperatorName() {
        CurrentUser user = HOLDER.get();
        //无用户信息时（如内部调用）记为系统
        if (user == null) {
            return "system";
        }
        return user.getDisplayName() != null ? user.getDisplayName() : user.getLoginName();
    }

    public static void clear() {
        HOLDER.remove();
    }
}
