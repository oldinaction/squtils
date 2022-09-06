package cn.aezo.utils.base;

import cn.hutool.core.util.CharUtil;
import cn.hutool.core.util.StrUtil;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class StrU extends StrUtil {
	private static final char[] letter = new char[] {'q', 'w', 'e', 'r', 't', 'y', 'u', 'i', 'o', 'p', 'a', 's', 'd', 'f', 'g', 'h', 'j', 'k', 'l', 'z', 'x', 'c', 'v', 'b', 'n', 'm'};
	private static final char[] number = new char[] {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};
	/**
	 * 字母、数字（去掉0, o, l, 1）
	 */
	private static final char[] letterNumberIncomplete = new char[] {'q', 'w', 'e', 'r', 't', 'y', 'u', 'i', 'p', 'a', 's', 'd', 'f', 'g', 'h', 'j', 'k', 'z', 'x', 'c', 'v', 'b', 'n', 'm', '2', '3', '4', '5', '6', '7', '8', '9'};
	/**
	 * 生成唯一订单编号
	 */
	private static OrderNo orderNo = new OrderNo(0, 0);

	public static String toCamelCase(CharSequence name) {
		if(name != null && !name.toString().contains("-")) {
			// StrUtil.toCamelCase没有下划线则不会转换
			return name.toString().toLowerCase();
		}
		return StrUtil.toCamelCase(name);
	}

	/**
	 * 将驼峰式命名的字符串转换为中划线方式。例如：<br/>
	 *
	 * <pre>
	 * HelloWorld=》hello-world
	 * Hello_World=》hello-world
	 * HelloWorld_test=》hello-world-test
	 * </pre>
	 *
	 * @param str
	 * @return java.lang.String
	 */
	public static String toDashedCase(CharSequence str) {
		return toSymbolCase(str, CharUtil.DASHED);
	}

	/**
	 * 连接字符串
	 * @param objects
	 * @return
	 */
	public static String buffer(Object... objects) {
		StringBuffer stringBuffer = new StringBuffer();
		for (Object o : objects) {
			stringBuffer.append(o);
		}
		return stringBuffer.toString();
	}

	/**
	 * 连接字符串
	 * @param objects
	 * @return
	 */
	public static String bufferJoin(String joinStr, Object... objects) {
		StringBuffer stringBuffer = new StringBuffer();

		if(objects != null) {
			int size = objects.length;
			if(size > 0) {
				for (int i = 0; i < size - 1; i++) {
					stringBuffer.append(objects[i]);
					if(joinStr != null) {
						stringBuffer.append(joinStr);
					}
				}
				stringBuffer.append(objects[size - 1]);
			}
		}

		return stringBuffer.toString();
	}

	/**
	 * 生成随机字符串
	 * @param length 字符串长度
	 * @param letterOrNum 生成字符串类型(null: 字母数字混合, true: 纯字母, false: 纯数字)
	 * @return
	 * @author smalle
	 * @date 2016年12月8日 下午12:20:24
	 */
	public static String random(int length, Boolean letterOrNum) {
		char[] randomChar;
		if(letterOrNum == null) {
			randomChar = letterNumberIncomplete;
		} else if(letterOrNum == true) {
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
	 * 获取驼峰/下划线简称. 如：helloWorld 或 hello_world => hw
	 * @author smalle
	 * @since 2021/1/12
	 * @param str
	 * @return java.lang.String
	 */
	public static String getUnderlineShortName(String str) {
		if(ValidU.isEmpty(str)) {
			return "";
		}
		String[] split = StrU.toUnderlineCase(str).toLowerCase().split("_");
		String ret = "";
		for (String s : split) {
			ret += s.charAt(0);
		}
		return ret;
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

	/**
	 * 将一个字符串转化为输入流
	 *
	 * @param strContent
	 * @return
	 */
	public static InputStream getStrToStream(String strContent) {
		if (strContent != null && !strContent.trim().equals("")) {
			try {
				ByteArrayInputStream tInputStringStream = new ByteArrayInputStream(strContent.getBytes());
				return tInputStringStream;
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		return null;
	}

	/**
	 * 将一个输入流转化为字符串
	 *
	 * @param tInputStream
	 * @return
	 */
	public static String getStreamToStr(InputStream tInputStream) {
		if (tInputStream != null) {
			try {
				BufferedReader tBufferedReader = new BufferedReader(new InputStreamReader(tInputStream));
				StringBuilder sb = new StringBuilder();
				String sTempOneLine;
				while ((sTempOneLine = tBufferedReader.readLine()) != null) {
					sb.append(sTempOneLine);
				}
				return sb.toString();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		return null;
	}

	/**
	 * 获取下一个雪花ID编号(如：381785090499280897)
	 *
	 * 381785090499280897
	 * 792800768355729408
	 *
	 * @return String
	 */
	public static String getNextNo() {
		long id = orderNo.nextId();
		return "" + id;
	}

	public static List<String> getNextNo(int count) {
		List<String> ret = new ArrayList<>();
		for (int i = 0; i < count; i++) {
			ret.add(orderNo.nextId() + "");
		}
		return ret;
	}

	/**
	 * 获取下一个编号(以时间开头，如：20171119381785090499280898)
	 * @param startDate
	 * @return String
	 */
	public static String getNextNo(Boolean startDate) {
		long id = orderNo.nextId();
		return startDate ? (new SimpleDateFormat("yyyyMMdd").format(new Date()) + id) : ("" + id);
	}

	/**
	 * 生成订单编号(18位)
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
		private final long epoch = 1420041600000L;

		/** 机器id所占的位数 */
		private final long workerIdBits = 5L;

		/** 数据标识id所占的位数 */
		private final long dataCenterIdBits = 5L;

		/** 支持的最大机器id，结果是31 (这个移位算法可以很快的计算出几位二进制数所能表示的最大十进制数) */
		private final long maxWorkerId = -1L ^ (-1L << workerIdBits);

		/** 支持的最大数据标识id，结果是31 */
		private final long maxDataCenterId = -1L ^ (-1L << dataCenterIdBits);

		/** 序列在id中占的位数 */
		private final long sequenceBits = 12L;

		/** 机器ID向左移12位 */
		private final long workerIdShift = sequenceBits;

		/** 数据标识id向左移17位(12+5) */
		private final long dataCenterIdShift = sequenceBits + workerIdBits;

		/** 时间截向左移22位(5+5+12) */
		private final long timestampLeftShift = sequenceBits + workerIdBits + dataCenterIdBits;

		/** 生成序列的掩码，这里为4095 (0b111111111111=0xfff=4095) */
		private final long sequenceMask = -1L ^ (-1L << sequenceBits);

		/** 工作机器ID(0~31) */
		private long workerId;

		/** 数据中心ID(0~31) */
		private long dataCenterId;

		/** 毫秒内序列(0~4095) */
		private long sequence = 0L;

		/** 上次生成ID的时间截 */
		private long lastTimestamp = -1L;

		public OrderNo() {}

		/**
		 * 构造函数
		 * @param workerId 工作机器ID (0~31)
		 * @param dataCenterId 数据中心ID (0~31)
		 */
		public OrderNo(long workerId, long dataCenterId) {
			if (workerId > maxWorkerId || workerId < 0) {
				throw new IllegalArgumentException(String.format("worker Id can't be greater than %d or less than 0", maxWorkerId));
			}
			if (dataCenterId > maxDataCenterId || dataCenterId < 0) {
				throw new IllegalArgumentException(String.format("dataCenter Id can't be greater than %d or less than 0", maxDataCenterId));
			}
			this.workerId = workerId;
			this.dataCenterId = dataCenterId;
		}

		/**
		 * 获得下一个ID (该方法是线程安全的)
		 * @return SnowflakeId
		 */
		public synchronized long nextId() {
			long timestamp = timeGen();

			// 如果当前时间小于上一次ID生成的时间戳，说明系统时钟回退过这个时候应当抛出异常
			if (timestamp < lastTimestamp) {
				throw new RuntimeException(
						String.format("Clock moved backwards.  Refusing to generate id for %d milliseconds", lastTimestamp - timestamp));
			}

			// 如果是同一时间生成的，则进行毫秒内序列
			if (lastTimestamp == timestamp) {
				sequence = (sequence + 1) & sequenceMask;
				//毫秒内序列溢出
				if (sequence == 0) {
					//阻塞到下一个毫秒,获得新的时间戳
					timestamp = tilNextMillis(lastTimestamp);
				}
			}
			// 时间戳改变，毫秒内序列重置
			else {
				sequence = 0L;
			}

			// 上次生成ID的时间截
			lastTimestamp = timestamp;

			// 移位并通过或运算拼到一起组成64位的ID
			return ((timestamp - epoch) << timestampLeftShift)
					| (dataCenterId << dataCenterIdShift)
					| (workerId << workerIdShift)
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
}
