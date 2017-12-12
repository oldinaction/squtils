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
	 * 生成唯一订单编号
	 */
	private static OrderNo orderNo = new OrderNo(0, 0);

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

		/**
		 * 生成一串token：WSLqf5fVUJxGVkUnDOTpig==
		 * @return
		 */
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
	 * 获取下一个编号(如：381785090499280897)
	 * @return
	 */
	public static String getNextNo() {
		long id = orderNo.nextId();
		return "" + id;
	}

	/**
	 * 获取下一个编号(以时间开头，如：171119381785090499280898)
	 * @param startDate
	 * @return
	 */
	public static String getNextNo(Boolean startDate) {
		long id = orderNo.nextId();
		return startDate ? (DateU.format(new Date(), "yyMMdd") + id) : ("" + id);
	}

	/**
	 * 生成订单编号
	 * Twitter Snowflake<br>
	 * SnowFlake的结构如下(每部分用-分开):<br>
	 * 0 - 0000000000 0000000000 0000000000 0000000000 0 - 00000 - 00000 - 000000000000 <br>
	 * 1位标识，由于long基本类型在Java中是带符号的，最高位是符号位，正数是0，负数是1，所以id一般是正数，最高位是0<br>
	 * 41位时间截(毫秒级)，注意，41位时间截不是存储当前时间的时间截，而是存储时间截的差值（当前时间截 - 开始时间截)
	 * 得到的值），这里的的开始时间截，一般是我们的id生成器开始使用的时间，由我们程序来指定的（如下下面程序IdWorker类的startTime属性）。41位的时间截，可以使用69年，年T = (1L << 41) / (1000L * 60 * 60 * 24 * 365) = 69<br>
	 * 10位的数据机器位，可以部署在1024个节点，包括5位datacenterId和5位workerId<br>
	 * 12位序列，毫秒内的计数，12位的计数顺序号支持每个节点每毫秒(同一机器，同一时间截)产生4096个ID序号<br>
	 * 加起来刚好64位，为一个Long型。<br>
	 * SnowFlake的优点是，整体上按照时间自增排序，并且整个分布式系统内不会产生ID碰撞(由数据中心ID和机器ID作区分)，并且效率较高，经测试，SnowFlake每秒能够产生26万ID左右。
	 */
	public static class OrderNo {
		/** 开始时间截 (2015-01-01) */
		private final long twepoch = 1420041600000L;

		/** 机器id所占的位数 */
		private final long workerIdBits = 5L;

		/** 数据标识id所占的位数 */
		private final long datacenterIdBits = 5L;

		/** 支持的最大机器id，结果是31 (这个移位算法可以很快的计算出几位二进制数所能表示的最大十进制数) */
		private final long maxWorkerId = -1L ^ (-1L << workerIdBits);

		/** 支持的最大数据标识id，结果是31 */
		private final long maxDatacenterId = -1L ^ (-1L << datacenterIdBits);

		/** 序列在id中占的位数 */
		private final long sequenceBits = 12L;

		/** 机器ID向左移12位 */
		private final long workerIdShift = sequenceBits;

		/** 数据标识id向左移17位(12+5) */
		private final long datacenterIdShift = sequenceBits + workerIdBits;

		/** 时间截向左移22位(5+5+12) */
		private final long timestampLeftShift = sequenceBits + workerIdBits + datacenterIdBits;

		/** 生成序列的掩码，这里为4095 (0b111111111111=0xfff=4095) */
		private final long sequenceMask = -1L ^ (-1L << sequenceBits);

		/** 工作机器ID(0~31) */
		private long workerId;

		/** 数据中心ID(0~31) */
		private long datacenterId;

		/** 毫秒内序列(0~4095) */
		private long sequence = 0L;

		/** 上次生成ID的时间截 */
		private long lastTimestamp = -1L;

		public OrderNo() {}

		/**
		 * 构造函数
		 * @param workerId 工作ID (0~31)
		 * @param datacenterId 数据中心ID (0~31)
		 */
		public OrderNo(long workerId, long datacenterId) {
			if (workerId > maxWorkerId || workerId < 0) {
				throw new IllegalArgumentException(String.format("worker Id can't be greater than %d or less than 0", maxWorkerId));
			}
			if (datacenterId > maxDatacenterId || datacenterId < 0) {
				throw new IllegalArgumentException(String.format("datacenter Id can't be greater than %d or less than 0", maxDatacenterId));
			}
			this.workerId = workerId;
			this.datacenterId = datacenterId;
		}

		/**
		 * 获得下一个ID (该方法是线程安全的)
		 * @return SnowflakeId
		 */
		public synchronized long nextId() {
			long timestamp = timeGen();

			//如果当前时间小于上一次ID生成的时间戳，说明系统时钟回退过这个时候应当抛出异常
			if (timestamp < lastTimestamp) {
				throw new RuntimeException(
						String.format("Clock moved backwards.  Refusing to generate id for %d milliseconds", lastTimestamp - timestamp));
			}

			//如果是同一时间生成的，则进行毫秒内序列
			if (lastTimestamp == timestamp) {
				sequence = (sequence + 1) & sequenceMask;
				//毫秒内序列溢出
				if (sequence == 0) {
					//阻塞到下一个毫秒,获得新的时间戳
					timestamp = tilNextMillis(lastTimestamp);
				}
			}
			//时间戳改变，毫秒内序列重置
			else {
				sequence = 0L;
			}

			//上次生成ID的时间截
			lastTimestamp = timestamp;

			//移位并通过或运算拼到一起组成64位的ID
			return ((timestamp - twepoch) << timestampLeftShift) //
					| (datacenterId << datacenterIdShift) //
					| (workerId << workerIdShift) //
					| sequence;
		}

		/**
		 * 阻塞到下一个毫秒，直到获得新的时间戳
		 * @param lastTimestamp 上次生成ID的时间截
		 * @return 当前时间戳
		 */
		protected long tilNextMillis(long lastTimestamp) {
			long timestamp = timeGen();
			while (timestamp <= lastTimestamp) {
				timestamp = timeGen();
			}
			return timestamp;
		}

		/**
		 * 返回以毫秒为单位的当前时间
		 * @return 当前时间(毫秒)
		 */
		protected long timeGen() {
			return System.currentTimeMillis();
		}
	}


    // test
    public static void main(String[] args) throws Exception {

	}
}
