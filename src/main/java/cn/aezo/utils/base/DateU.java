package cn.aezo.utils.base;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by smalle on 2017/1/12.
 */
public class DateU {

    /**
     * 日期格式化为字符串
     * @param date
     * @param dateFormat
     * @return
     */
    public static String format(Date date, SimpleDateFormat dateFormat) {
        return dateFormat.format(date);
    }

    /**
     * 获取当前时间字符串：yyyyMMddHHmmssSSS
     * @return
     */
    public static String now() {
        Date now = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        return format(now, dateFormat);
    }

    /**
     * 返回系统当前时间
     * @return Timestamp for right now
     */
    public static Timestamp nowTimestamp() {
        return getTimestamp(System.currentTimeMillis());
    }

    /**
     * Convert a millisecond value to a Timestamp.
     * @param time millsecond value
     * @return Timestamp
     */
    public static Timestamp getTimestamp(long time) {
        return new Timestamp(time);
    }

    /**
     * Convert a millisecond value to a Timestamp.
     * @param milliSecs millsecond value
     * @return Timestamp
     */
    public static Timestamp getTimestamp(String milliSecs) throws NumberFormatException {
        return new Timestamp(Long.parseLong(milliSecs));
    }


}
