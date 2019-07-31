package cn.aezo.core.auth;

/**
 * Created by smalle on 2019-03-20 12:42.
 */
public interface UserAuth {
    UserAuthInfo checkAndGetUserAuthInfo(String username, String password);

    UserAuthInfo getUserAuthInfo(String username);
}
