package com.petrochina.xnyqt.pims.common.web;

import com.petrochina.xnyqt.pims.common.context.CurrentUser;
import com.petrochina.xnyqt.pims.common.context.UserContext;
import com.petrochina.xnyqt.pims.common.type.HeaderType;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

/**
 * @Name: UserContextInterceptor
 * @Description: 读取网关透传的用户信息写入用户上下文
 * @Version: V1.0.0
 * @Author: PIMS项目组
 * @Create Date: 2026-10-28
 */
public class UserContextInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String userId = request.getHeader(HeaderType.USER_ID);
        //网关未透传用户信息时不设置上下文
        if (userId != null) {
            UserContext.set(new CurrentUser(userId, request.getHeader(HeaderType.LOGIN_NAME),
                    decode(request.getHeader(HeaderType.DISPLAY_NAME)), request.getHeader(HeaderType.ORG_ID)));
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContext.clear();
    }

    private String decode(String value) {
        if (value == null) {
            return null;
        }
        try {
            return URLDecoder.decode(value, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return value;
        }
    }
}
