package cn.aezo.utils.ext.servlet;

import cn.aezo.utils.base.ValidU;

import javax.servlet.http.HttpServletRequest;

/**
 * @author smalle
 * @since 2020-12-07 18:48
 */
public class RequestU {

    /**
     * 检查请求头是否接受 application/json 返回格式
     * @author smalle
     * @since 2021/2/6
     * @param request
     * @return java.lang.Boolean
     */
    public static Boolean checkAcceptJson(HttpServletRequest request) {
        String accept = request.getHeader("Accept");
        if(ValidU.isEmpty(accept)) {
            return false;
        }
        return accept.contains("application/json");
    }
}
