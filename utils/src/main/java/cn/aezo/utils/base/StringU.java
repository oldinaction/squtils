package cn.aezo.utils.base;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import sun.misc.BASE64Encoder;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Character.toUpperCase;


public class StringU {
	private static final char[] letter = new char[] {'q', 'w', 'e', 'r', 't', 'y', 'u', 'i', 'o', 'p', 'a', 's', 'd', 'f', 'g', 'h', 'j', 'k', 'l', 'z', 'x', 'c', 'v', 'b', 'n', 'm'};
	private static final char[] number = new char[] {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};
	private static final char[] letterNumber = new char[] {'q', 'w', 'e', 'r', 't', 'y', 'u', 'i', 'p', 'a', 's', 'd', 'f', 'g', 'h', 'j', 'k', 'z', 'x', 'c', 'v', 'b', 'n', 'm', '2', '3', '4', '5', '6', '7', '8', '9'};
	
	/**
	 * 生成随机字符串
	 * @param length 字符串长度
	 * @param type 生成字符串类型(null: 字母数字混合, true: 纯字母, false: 纯数字)
	 * @return
	 * @author smalle
	 * @date 2016年12月8日 下午12:20:24
	 */
	public static String random(int length, Boolean type) {
		char[] randomChar;
		if(type == null) {
			randomChar = letterNumber;
		} else if(type == true) {
			randomChar = letter;
		} else {
			randomChar = number;
		}
		int charLen = randomChar.length;

		StringBuffer randomStr = new StringBuffer(length);
		Random random = new Random();
		for(int i = 0; i < length; i++) {
			char c = randomChar[random.nextInt(charLen)];
			randomStr.append(c);
		}

		return randomStr.toString();
	}
	
	/**
	 * 日期格式：yyyy-MM-dd HH:mm:ss.SSS
	 * @param fromat
	 * @return
	 * @author smalle
	 * @date 2016年12月25日 上午11:05:40
	 */
	public static String nowTime(String fromat) {
		Date currentTime = new Date();
    	SimpleDateFormat formatter = new SimpleDateFormat(fromat);
    	String dateString = formatter.format(currentTime);
		return dateString;
	}

	/**
	 * 下划线转驼峰法
	 * @param line 源字符串
	 * @param smallCamel 大小驼峰,是否为小驼峰
	 * @return 转换后的字符串
	 */
	public static String underline2Camel(String line,boolean smallCamel){
		if(line==null||"".equals(line)){
			return "";
		}
		StringBuffer sb=new StringBuffer();
		Pattern pattern= Pattern.compile("([A-Za-z\\d]+)(_)?");
		Matcher matcher=pattern.matcher(line);
		while(matcher.find()){
			String word=matcher.group();
			sb.append(smallCamel&&matcher.start()==0?Character.toLowerCase(word.charAt(0)): toUpperCase(word.charAt(0)));
			int index=word.lastIndexOf('_');
			if(index>0){
				sb.append(word.substring(1, index).toLowerCase());
			}else{
				sb.append(word.substring(1).toLowerCase());
			}
		}
		return sb.toString();
	}

	/**
	 * 驼峰法转下划线
	 * @param line 源字符串
	 * @return 转换后的字符串
	 */
	public static String camel2Underline(String line){
		if(line==null||"".equals(line)){
			return "";
		}
		line=String.valueOf(line.charAt(0)).toUpperCase().concat(line.substring(1));
		StringBuffer sb = new StringBuffer();
		Pattern pattern=Pattern.compile("[A-Z]([a-z\\d]+)?");
		Matcher matcher=pattern.matcher(line);
		while(matcher.find()){
			String word=matcher.group();
			sb.append(word.toUpperCase());
			sb.append(matcher.end()==line.length()?"":"_");
		}
		return sb.toString();
	}

	/**
	 * 首字母大写
	 */
	public static String toUpperCaseFirst(String string) {
		char[] methodName = string.toCharArray();
		methodName[0] = Character.toUpperCase(methodName[0]);
		return String.valueOf(methodName);
	}

	/**
	 * 将josn字符串转成Map（此json字符串可解析为Map, 如果子项有List会自动解析为List）
	 * @param jsonStr
	 * @return
	 */
	public static Map<String, Object> parseJsonStr2Map(String jsonStr) throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();
		//最外层解析
		JSONObject json = JSONObject.fromObject(jsonStr);
		for(Object k : json.keySet()){
			Object v = json.get(k);
			//如果内层还是数组的话，继续解析
			if(v instanceof JSONArray){
				List list = parseJsonStr2List(v.toString());
				map.put(k.toString(), list);
			} else {
				map.put(k.toString(), v);
			}
		}
		return map;
	}

	/**
	 * 将json字符串转成List（此json字符串可解析为List）
	 * @param jsonStr
	 * @return
	 */
	public static List parseJsonStr2List(String jsonStr) throws Exception {
		List list = new ArrayList();
		JSONArray jsonArr = JSONArray.fromObject(jsonStr);
		Iterator<JSONObject> it = jsonArr.iterator();
		while(it.hasNext()) {
			Object obj = it.next();
			if(obj instanceof JSONObject) {
				list.add(parseJsonStr2Map(obj.toString()));
			} else {
				list.add(obj.toString());
			}
		}

		return list;
	}

	/**
	 * 将Map转成json字符串
	 * @param map
	 * @return
	 */
	public static String parseMap2JsonStr(Map<String, Object> map) {
		return JSONObject.fromObject(map).toString();
	}

	/**
	 * 通过HTTP获取JSON数据（List）
	 * @param url
	 * @return
	 */
    public static List<Map<String, Object>> getListByUrl(String url) {
        try {
            InputStream in = new URL(url).openStream();  
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));  
            StringBuilder sb = new StringBuilder();  
            String line;  
            while((line=reader.readLine())!=null){  
                sb.append(line);  
            }  
            return parseJsonStr2List(sb.toString());  
        } catch (Exception e) {  
            e.printStackTrace();  
        }  
        return null;  
    }

	/**
	 * 通过HTTP获取JSON数据（Map）
	 * 北京的天气接口: "http://apistore.baidu.com/microservice/cityinfo?cityname=%E5%8C%97%E4%BA%AC"
	 * @param url
	 * @return
	 */
	public static Map<String, Object> getMapByUrl(String url){
        try {
            InputStream in = new URL(url).openStream();  
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));  
            StringBuilder sb = new StringBuilder();  
            String line;  
            while((line=reader.readLine())!=null){  
                sb.append(line);  
            }  
            return parseJsonStr2Map(sb.toString());  
        } catch (Exception e) {  
            e.printStackTrace();  
        }  
        return null;  
    }

	/**
	 * 解析字节型((2^8)-1 = 0 ~ 225)字符串为字符串.如：104,101,108,108,111 转换成 hello
	 * @param byteStr
	 * @param split
	 * @return
	 */
	public static String parseByteStr(String byteStr, String split) {
		String retStr;
		if(byteStr == null) {
			return null;
		}

		byteStr = byteStr.trim();
		if("".equals(byteStr)) {
			return null;
		}
		if(split == null || "".equals(split)) {
			split = ",";
		}

		String sarr[] = byteStr.split(split);
		byte[] barr = new byte[sarr.length];
		for (int i = 0; i < sarr.length; i++) {
			barr[i] = new Byte(sarr[i].trim());
		}

		try {
			retStr = new String(barr, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		}

		return retStr;
	}

	/**
	 * 数组转成字符串
	 * @param arr
	 * @param separator 加入的分割符, 如："\n"
	 * @return
	 */
	public static String arrToStr(Object[] arr, String separator) {
		StringBuffer sb = new StringBuffer();
		for (Object obj : arr) {
			sb.append(obj.toString());
			if(separator != null) {
				sb.append(separator);
			}
		}

		if(separator != null) {
			return sb.toString().substring(0, sb.toString().length() - separator.length());
		} else {
			return sb.toString();
		}
	}

	public static class Security {

		/**
		 * md5和base64加密
		 * @param s
		 * @return
		 */
		public static String md5AndBase64(String s) {
			if (s == null) return null;

			String encodeStr;
			byte[] utfBytes = s.getBytes();
			MessageDigest md;
			try {
				md = MessageDigest.getInstance("MD5");
				md.update(utfBytes);
				byte[] bytes = md.digest();

				BASE64Encoder b64Encoder = new BASE64Encoder();
				encodeStr = b64Encoder.encode(bytes);
			} catch (NoSuchAlgorithmException e) {
				return null;
			}

			return encodeStr;
		}

		/**
		 * base64加密
		 * @param str
		 * @return
		 */
		public static String base64(String str) {
			if (str == null) return null;
			BASE64Encoder b64Encoder = new BASE64Encoder();
			return b64Encoder.encode(str.getBytes());
		}

		/**
		 * md5加密
		 * @param str
		 * @return
		 */
		public static String md5(String str) {
			if (str == null) return null;

			MessageDigest md;
			try {
				md = MessageDigest.getInstance("MD5"); // 生成一个MD5加密计算摘要
				md.update(str.getBytes()); // 计算md5函数
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
				return null;
			}

			return new BigInteger(1, md.digest()).toString(16);
		}

		public static String makeToken() {
			// 7346734837483  834u938493493849384  43434384
			String token = (System.currentTimeMillis() + new Random().nextInt(999999999)) + "";
			// 数据指纹   128位长   16个字节  md5
			try {
				MessageDigest md = MessageDigest.getInstance("md5");
				byte md5[] = md.digest(token.getBytes());
				//base64编码--任意二进制编码明文字符
				BASE64Encoder encoder = new BASE64Encoder();
				return encoder.encode(md5);
			} catch (NoSuchAlgorithmException e) {
				throw new RuntimeException(e);
			}
		}
	}

	/**
	 * 生成Oracle的分页sql语句
	 * @param sql <b>sql语句的第一个from需要小写</b>
	 * @param pageIndex 页下标
	 * @param pagingLength 页长
	 * @param returnTotals 是否返回记录总数(返回的字段名为paging_total__) 注：数据量大的时候最好不要返回
	 * @return
	 */
	public static String pagingSqlOracle(String sql, Integer pageIndex, Integer pagingLength, Boolean returnTotals) {
		int _pageIndex = 1;
		int _pagingLength = 10;

		if(null != pageIndex && pageIndex.compareTo(0) != 0) {
			_pageIndex = pageIndex;
		}
		if(null != pagingLength && pagingLength.compareTo(0) != 0) {
			_pagingLength = pagingLength;
		}

		int start = (_pageIndex - 1) * _pagingLength + 1;

		if(returnTotals)
			sql = sql.replaceFirst(" from ", ", count(*) over () paging_total__ from ");
		sql = (new StringBuilder("select * from (select rownum as rn__, paging_t1.* from (")).append(sql).append(") paging_t1 where rownum < ").append(start + _pagingLength).append(") paging_t2 where paging_t2.rn__ >= ").append(start).toString();

		return sql;
	}

    //test
    public static void main(String[] args) throws Exception {

	}

	
}
