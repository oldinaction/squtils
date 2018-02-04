package cn.aezo.utils.jar;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.compression.CompressionCodecs;
import org.apache.commons.codec.binary.Base64;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

/**
 * Created by smalle on 2017/10/27.
 *
 * 依赖：jjwt
 */
public class JwtU {
	// public static final String Role_Refresh_Token = "Role_Refresh_Token";
    //
	// private static final String Claim_Key_User_Id = "user_id";
	// private static final String Claim_Key_Authorities = "scope";
	// private static final String Claim_Key_Account_Enabled = "enabled";
	// private static final String Claim_Key_Account_Non_Locked = "non_locked";
	// private static final String Claim_Key_Account_Non_Expired = "non_expired";

	//@Value("${jwt.secretKey}")
	private String secretKey = "7786df7fc3a34e26a61c034d5e888526";

	///@Value("${jwt.accessTokenExpiration}")
	private Long access_token_expiration = 1000L*60*60*2; // access_token 默认2小时(2*60*60*1000L millisecond)

	//@Value("${jwt.refreshTokenExpiration}")
	private Long refresh_token_expiration = 1000L*60*60*24*7;  // refresh_token 默认7天

	private final SignatureAlgorithm Signature_Algorithm = SignatureAlgorithm.HS256;

	// public JwtUserDetails getUserFromToken(String token) {
	// 	JwtUserDetails user;
	// 	try {
	// 		final Claims claims = getClaimsFromToken(token);
	// 		long userId = getUserIdFromToken(token);
	// 		String username = claims.getSubject();
	// 		List roles = (List) claims.get(Claim_Key_Authorities);
	// 		Collection<? extends GrantedAuthority> authorities = parseArrayToAuthorities(roles);
	// 		boolean account_enabled = (Boolean) claims.get(Claim_Key_Account_Enabled);
	// 		boolean account_non_locked = (Boolean) claims.get(Claim_Key_Account_Non_Locked);
	// 		boolean account_non_expired = (Boolean) claims.get(Claim_Key_Account_Non_Expired);
    //
	// 		user = new JwtUserDetails(userId, username, "password", account_enabled, account_non_expired, true, account_non_locked, authorities);
	// 	} catch (Exception e) {
	// 		e.printStackTrace();
	// 		user = null;
	// 	}
	// 	return user;
	// }

	// public long getUserIdFromToken(String token) {
	// 	long userId;
	// 	try {
	// 		final Claims claims = getClaimsFromToken(token);
	// 		if(claims.get(Claim_Key_User_Id) == null)
	// 			return 0;
	// 		userId = Long.valueOf(String.valueOf(claims.get(Claim_Key_User_Id)));
	// 	} catch (Exception e) {
	// 		e.printStackTrace();
	// 		userId = 0;
	// 	}
	// 	return userId;
	// }

	public String getUsernameFromToken(String token) {
		String username;
		try {
			final Claims claims = getClaimsFromToken(token);
			username = claims.getSubject();
		} catch (Exception e) {
			e.printStackTrace();
			username = null;
		}
		return username;
	}

	public Date getCreatedDateFromToken(String token) {
		Date created;
		try {
			final Claims claims = getClaimsFromToken(token);
			created = claims.getIssuedAt();
		} catch (Exception e) {
			e.printStackTrace();
			created = null;
		}
		return created;
	}

	public Date getExpirationDateFromToken(String token) {
		Date expiration;
		try {
			final Claims claims = getClaimsFromToken(token);
			expiration = claims.getExpiration();
		} catch (Exception e) {
			e.printStackTrace();
			expiration = null;
		}
		return expiration;
	}

	// public String generateAccessToken(UserDetails userDetails) {
	// 	JwtUserDetails user = (JwtUserDetails) userDetails;
	// 	Map<String, Object> claims = generateClaims(user);
	// 	try {
	// 		claims.put(Claim_Key_Authorities, authoritiesToArray(user.getAuthorities()));
	// 	} catch (Exception e) {
	// 		e.printStackTrace();
	// 		return "";
	// 	}
	// 	return generateAccessToken(user.getUsername(), claims);
	// }

	public String refreshToken(String token) {
		String refreshedToken;
		try {
			final Claims claims = getClaimsFromToken(token);
			refreshedToken = generateAccessToken(claims.getSubject(), claims);
		} catch (Exception e) {
			e.printStackTrace();
			refreshedToken = null;
		}
		return refreshedToken;
	}

	// public String generateRefreshToken(UserDetails userDetails) {
	// 	JwtUserDetails user = (JwtUserDetails) userDetails;
	// 	Map<String, Object> claims = generateClaims(user);
	// 	// 只授于更新 token 的权限
	// 	String roles[] = new String[]{JwtUtils.Role_Refresh_Token};
	// 	try {
	// 		claims.put(Claim_Key_Authorities, roles);
	// 	} catch (Exception e) {
	// 		e.printStackTrace();
	// 		return "";
	// 	}
	// 	return generateRefreshToken(user.getUsername(), claims);
	// }

	public Boolean canTokenBeRefreshed(String token, Date lastPasswordReset) {
		final Date created = getCreatedDateFromToken(token);
		return !isCreatedBeforeLastPasswordReset(created, lastPasswordReset)
				&& (!isTokenExpired(token));
	}

	// public Boolean validateToken(String token, UserDetails userDetails) {
	// 	JwtUserDetails user = (JwtUserDetails) userDetails;
	// 	final long userId = getUserIdFromToken(token);
	// 	final String username = getUsernameFromToken(token);
	// 	// final Date created = getCreatedDateFromToken(token);
	// 	// final Date expiration = getExpirationDateFromToken(token);
	// 	return (userId == user.getUserId()
	// 			&& username.equals(user.getUsername())
	// 			&& !isTokenExpired(token)
     //            /* && !isCreatedBeforeLastPasswordReset(created, userDetails.getLastPasswordResetDate()) */
	// 	);
	// }

	private Claims getClaimsFromToken(String token) {
		Claims claims;
		try {
			claims = Jwts.parser()
					.setSigningKey(generalSecretKey())
					.parseClaimsJws(token)
					.getBody();
		} catch (Exception e) {
			e.printStackTrace();
			claims = null;
		}
		return claims;
	}

	private String generateAccessToken(String subject, Map<String, Object> claims) {
		return generateToken(subject, claims, access_token_expiration);
	}

	private String generateRefreshToken(String subject, Map<String, Object> claims) {
		return generateToken(subject, claims, refresh_token_expiration);
	}

	// private Map<String, Object> generateClaims(JwtUserDetails user) {
	// 	Map<String, Object> claims = new HashMap<>();
	// 	claims.put(Claim_Key_User_Id, user.getUserId());
	// 	claims.put(Claim_Key_Account_Enabled, user.isEnabled());
	// 	claims.put(Claim_Key_Account_Non_Locked, user.isAccountNonLocked());
	// 	claims.put(Claim_Key_Account_Non_Expired, user.isAccountNonExpired());
	// 	return claims;
	// }

	private Boolean isTokenExpired(String token) {
		final Date expiration = getExpirationDateFromToken(token);
		return expiration.before(new Date());
	}

	private Boolean isCreatedBeforeLastPasswordReset(Date created, Date lastPasswordReset) {
		return (lastPasswordReset != null && created.before(lastPasswordReset));
	}

	// private List authoritiesToArray(Collection<? extends GrantedAuthority> authorities) {
	// 	List<String> list = new ArrayList<>();
	// 	for (GrantedAuthority ga : authorities) {
	// 		list.add(ga.getAuthority());
	// 	}
	// 	return list;
	// }
    //
	// private Collection<? extends GrantedAuthority> parseArrayToAuthorities(List roles) {
	// 	Collection<GrantedAuthority> authorities = new ArrayList<>();
	// 	SimpleGrantedAuthority authority;
	// 	for (Object role : roles) {
	// 		authority = new SimpleGrantedAuthority(role.toString());
	// 		authorities.add(authority);
	// 	}
	// 	return authorities;
	// }

	private String generateToken(String subject, Map<String, Object> claims, long expiration) {
		return Jwts.builder()
				.setClaims(claims)
				.setSubject(subject)
				.setId(UUID.randomUUID().toString())
				.setIssuedAt(new Date())
				.setExpiration(generateExpirationDate(expiration))
				.compressWith(CompressionCodecs.DEFLATE)
				.signWith(Signature_Algorithm, generalSecretKey())
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
