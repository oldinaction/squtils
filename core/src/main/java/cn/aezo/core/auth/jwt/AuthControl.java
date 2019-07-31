package cn.aezo.core.auth.jwt;

import cn.aezo.core.auth.UserAuth;
import cn.aezo.core.auth.UserAuthInfo;
import cn.aezo.core.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;
import java.util.Map;

/**
 * Created by smalle on 2019-03-20 12:42.
 * 1.注册AuthFilter拦截器
 * 2.创建用户信息包装类(持有原始User对象). UserAuthInfoWrapper extends User implements UserAuthInfo
 * 3.实现UserAuth, 并加入到bean管理池
 */
@RestController
@RequestMapping("/api/auth")
public class AuthControl {
    @Autowired
    private UserAuth userAuth;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private JwtU jwtU;

    /**
     * 登录
     */
    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public Result login(@RequestBody Map<String, Object> param) {
        String username = (String) param.get("username");
        String password = (String) param.get("password");
        UserAuthInfo userAuthInfo = userAuth.checkAndGetUserAuthInfo(username, password);
        Map<String, Object> tokenInfo = jwtU.createTokenInfo(userAuthInfo);
        return Result.success(tokenInfo);
    }

    /**
     * 利用refresh_token重新获取access_token
     */
    @RequestMapping(value = "/refreshToken", method = RequestMethod.POST)
    public Result refreshToken() {
        String refreshToken = "";
        Enumeration headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String key = (String) headerNames.nextElement();
            if("refresh_token".equals(key)) {
                refreshToken = request.getHeader(key);
                break;
            }
        }
        Map<String, Object> tokenInfo = jwtU.refreshToken(refreshToken);
        return Result.success(tokenInfo);
    }

    /**
     * 验证token是否有效
     */
    @RequestMapping(value = "/verify", method = RequestMethod.POST)
    public Result verify() {
        String accessToken = "";
        Enumeration headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String key = (String) headerNames.nextElement();
            if("access_token".equals(key)) {
                accessToken = request.getHeader(key);
                break;
            }
        }

        return jwtU.validateToken(accessToken) ? Result.success() : Result.failure();
    }
}
