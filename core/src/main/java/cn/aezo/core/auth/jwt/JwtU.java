package cn.aezo.core.auth.jwt;

import cn.aezo.core.auth.UserAuth;
import cn.aezo.core.auth.UserAuthInfo;
import cn.aezo.utils.base.ExceptionU;
import cn.aezo.utils.base.MiscU;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.compression.CompressionCodecs;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Created by smalle on 2017/10/27.
 *
 * 依赖：jjwt
 */
@Component
public class JwtU {
	public static final String Role_Refresh_Token = "Role_Refresh_Token";
	private static final String Claim_Key_Permissions = "permissions";
	private static final String Claim_Key_Roles = "roles";
	private static final String Claim_Key_Account_Enabled = "enabled";
	private static final String Claim_Key_Account_Non_Locked = "non_locked";
	private static final String Claim_Key_Account_Non_Expired = "non_expired";
    private final SignatureAlgorithm signature_algorithm = SignatureAlgorithm.HS256;

    @Value("${jwt.secretKey:7786df7fc3a34e26a61c034d5e888526}")
    private String secretKey;

    @Value("${jwt.accessTokenExpiration:#{60*60*12}}") // access_token 默认12小时(60*60*12 second)
    private Long access_token_expiration;

    @Value("${jwt.refreshTokenExpiration:#{60*60*24}}")  // refresh_token 默认24小时
    private Long refresh_token_expiration;

    @Autowired
    UserAuth userAuth;

    /**
     * 获取token信息
     * @param userAuthInfo
     * @return access_token、refresh_token、expires_in(单位s)
     */
    public Map<String, Object> createTokenInfo(UserAuthInfo userAuthInfo) {
        String access_token = createAccessToken(userAuthInfo);
        String refresh_token = createRefreshToken(userAuthInfo);
        return MiscU.Instance.toMap(
                "access_token", access_token,
                "refresh_token", refresh_token,
                "expires_in", access_token_expiration); // expires_in单位s
    }

    public String createAccessToken(UserAuthInfo userAuthInfo) {
        if(userAuthInfo == null || userAuthInfo.getUsername() == null)
            throw new ExceptionU.AuthTokenInvalidException("无效的用户标识");
        Map<String, Object> claims = generateClaims(userAuthInfo);
        return generateAccessToken(userAuthInfo.getUsername(), claims);
    }

    public String createRefreshToken(UserAuthInfo userAuthInfo) {
        if(userAuthInfo == null || userAuthInfo.getUsername() == null)
            throw new ExceptionU.AuthTokenInvalidException("无效的用户标识");
        Map<String, Object> claims = generateClaims(userAuthInfo);
        claims.put(Claim_Key_Roles, MiscU.Instance.toList(Role_Refresh_Token)); // 只授于更新 token 的权限
        return generateRefreshToken(userAuthInfo.getUsername(), claims);
    }

    public Map<String, Object> refreshToken(String refreshToken) {
        if(!isRefreshToken(refreshToken)) throw new ExceptionU.AuthTokenInvalidException("refreshToken无效或已过期");

        String username = getUsernameFromToken(refreshToken);
        UserAuthInfo userAuthInfo = userAuth.getUserAuthInfo(username);
        if(userAuthInfo == null) throw new ExceptionU.AuthTokenInvalidException("未知的用户");

        if(userAuthInfo.getLastPasswordChange() != null) {
            if(!canTokenBeRefreshed(refreshToken, userAuthInfo.getLastPasswordChange())) {
                throw new IllegalStateException("密码已修改，请重新登录");
            }
        }

        return createTokenInfo(userAuthInfo);
    }

    /**
     * 校验token是否有效
     * @param token
     * @return true 有效
     */
    public Boolean validateToken(String token) {
        if(token == null || token.isEmpty()) return false;
        return !isTokenExpired(token);
    }

    /**
     * 是否为有效的refreshToken
     * @param token
     * @return
     */
    public Boolean isRefreshToken(String token) {
        if(!validateToken(token)) return false;
        List<String> roles = (List<String>) getClaimsFromToken(token).get(Claim_Key_Roles);
        if(roles == null || !roles.contains(Role_Refresh_Token)) return false;
        return true;
    }

    public String getUsernameFromToken(String token) {
        final Claims claims = getClaimsFromToken(token);
        return claims.getSubject();
    }

    public Claims getClaimsFromToken(String token) {
		return Jwts.parser()
                .setSigningKey(generalSecretKey())
                .parseClaimsJws(token)
                .getBody();
	}

    private Map<String, Object> generateClaims(UserAuthInfo userAuthInfo) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(Claim_Key_Permissions, userAuthInfo.getPermissions());
        claims.put(Claim_Key_Roles, userAuthInfo.getRoles());
        claims.put(Claim_Key_Account_Enabled, userAuthInfo.getEnabled());
        claims.put(Claim_Key_Account_Non_Locked, userAuthInfo.getNonLocked());
        claims.put(Claim_Key_Account_Non_Expired, userAuthInfo.getNonExpired());
        return claims;
    }

    private Boolean canTokenBeRefreshed(String refreshToken, Date lastPasswordReset) {
        final Date created = getCreatedDateFromToken(refreshToken);
        return !isCreatedBeforeLastPasswordReset(created, lastPasswordReset)
                && (!isTokenExpired(refreshToken));
    }

	private String generateAccessToken(String subject, Map<String, Object> claims) {
		return generateToken(subject, claims, access_token_expiration);
	}

	private String generateRefreshToken(String subject, Map<String, Object> claims) {
		return generateToken(subject, claims, refresh_token_expiration);
	}

    private Date getCreatedDateFromToken(String token) {
        final Claims claims = getClaimsFromToken(token);
        return claims.getIssuedAt();
    }

    private Date getExpirationDateFromToken(String token) {
        final Claims claims = getClaimsFromToken(token);
        return claims.getExpiration();
    }

    /**
     * 是否已过期
     * @param token
     * @return
     */
    private Boolean isTokenExpired(String token) {
        Date expiration;
        try {
            expiration = getExpirationDateFromToken(token);
        } catch (ExpiredJwtException e) {
            return true;
        }
        return expiration.before(new Date());
    }

	private Boolean isCreatedBeforeLastPasswordReset(Date created, Date lastPasswordReset) {
		return (lastPasswordReset != null && created.before(lastPasswordReset));
	}

	private String generateToken(String subject, Map<String, Object> claims, long expiration) {
		return Jwts.builder()
				.setClaims(claims)
				.setSubject(subject)
				.setId(UUID.randomUUID().toString())
				.setIssuedAt(new Date())
				.setExpiration(generateExpirationDate(expiration))
				.compressWith(CompressionCodecs.DEFLATE)
				.signWith(signature_algorithm, generalSecretKey())
				.compact();
	}

	private Date generateExpirationDate(long expiration) {
		return new Date(System.currentTimeMillis() + expiration * 1000);
	}

	private SecretKey generalSecretKey() {
		byte[] encodedKey = Base64.decodeBase64(secretKey);
		return new SecretKeySpec(encodedKey, 0, encodedKey.length, "AES");
	}
}
