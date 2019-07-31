package cn.aezo.core.auth;

import java.util.Date;
import java.util.List;

/**
 * Created by smalle on 2019-03-20 12:47.
 */
public interface UserAuthInfo {
    String getUsername();
    List<String> getPermissions();
    List<String> getRoles();
    Date getLastPasswordChange();
    boolean getEnabled();
    boolean getNonLocked();
    boolean getNonExpired();
}
