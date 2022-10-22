package cn.aezo.utils.ext.spring;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class HttpContextU {

	public static HttpServletRequest getHttpServletRequest() {
		return ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();
	}

	public static HttpServletResponse getHttpServletResponse() {
		return ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getResponse();
	}

	public static String getDomain(){
		HttpServletRequest request = getHttpServletRequest();
		StringBuffer url = request.getRequestURL();
		return url.delete(url.length() - request.getRequestURI().length(), url.length()).toString();
	}

	public static String getOrigin(){
		HttpServletRequest request = getHttpServletRequest();
		return request.getHeader("Origin");
	}

	/**
	 * 返回的key会全部变成小写
	 * @author smalle
	 * @since 2022/10/11
	 * @return java.util.Map<java.lang.String, java.lang.String>
	 */
	public static Map<String, String> getHeaders() {
		HttpServletRequest request = getHttpServletRequest();
		Map<String, String> map = new HashMap<String, String>();
		Enumeration headerNames = request.getHeaderNames();
		while (headerNames.hasMoreElements()) {
			String key = (String) headerNames.nextElement();
			String value = request.getHeader(key);
			map.put(key, value);
		}
		return map;
	}

	/**
	 * 通过自定义返回的key获取header
	 * @author smalle
	 * @since 2022/10/11
	 * @return java.util.Map<java.lang.String, java.lang.String>
	 */
	public static Map<String, String> getHeaders(String... keys) {
		Map<String, String> map = new HashMap<String, String>();
		HttpServletRequest request = getHttpServletRequest();
		for (String key : keys) {
			map.put(key, request.getHeader(key));
		}
		return map;
	}
}
