package cn.aezo.core.auth.jwt;

import cn.aezo.utils.base.ExceptionU;
import cn.aezo.utils.base.ValidU;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by smalle on 2017/5/12.
 */
public class AuthFilter implements Filter {
    private JwtU jwtU;
    private static List<String> ignoreUrlPatternList = new ArrayList<>();

    public AuthFilter(JwtU jwtU) {
        this.jwtU = jwtU;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        ignoreUrlPatternList.add("/api/auth/*");
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;

        // 是否不进行access_token验证
        boolean authFlag = needAuth(request);

        // 验证access_token
        if (authFlag) {
            String access_token = "";
            Enumeration headerNames = request.getHeaderNames();
            while (headerNames.hasMoreElements()) {
                String key = (String) headerNames.nextElement();
                if ("access_token".equals(key)) {
                    access_token = request.getHeader(key);
                    break;
                }
            }

            if (ValidU.isNotEmpty(access_token)) {
                boolean flag;
                try {
                    flag = jwtU.validateToken(access_token) && !jwtU.isRefreshToken(access_token);
                } catch (Exception e) {
                    throw new ExceptionU.AuthTokenInvalidException("解析token出错");
                }
                if(!flag) throw new ExceptionU.AuthTokenInvalidException("无效的token");
            } else {
                throw new ExceptionU.AuthTokenInvalidException("缺少token信息");
            }
        }

        filterChain.doFilter(servletRequest, servletResponse);
    }

    private boolean needAuth(HttpServletRequest request) {
        if("OPTIONS".equals(request.getMethod())) {
            // axios的OPTIONS请求不带header参数
            return false;
        }

        Boolean authFlag = true;
        String url = request.getRequestURI();
        for (int i = 0; i < ignoreUrlPatternList.size(); i++) {
            String urlPattern = ignoreUrlPatternList.get(i).replaceAll("\\*", "(.*?)");
            if (Pattern.matches(urlPattern, url)) {
                authFlag = false;
                break;
            }
        }
        return authFlag;
    }

    @Override
    public void destroy() {

    }
}