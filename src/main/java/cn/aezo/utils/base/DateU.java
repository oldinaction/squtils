package cn.aezo.utils.base;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by smalle on 2017/1/12.
 */
public class DateU {

    /**
     * 日期格式化为字符串
     * @param date
     * @param dateFormat yyyy/MM/dd HH:mm:ss.SSS
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

    public static long[] getDistanceTimes(String str1, String str2) throws ParseException {
        return getDistanceTimes(str1, str2, "yyyy-MM-dd HH:mm:ss");
    }

    /**
     * @param date1 需要比较的时间 不能为空(null), 需要正确的日期格式
     * @param date2 被比较的时间 为空(null) 则为当前时间
     * @param type 返回值类型 0为多少天，1为多少个月，2为多少年
     * @return
     */
    public static int getDistanceDate(String date1, String date2, int type) throws ParseException {
        int n = 0;

        if(date2 == null) {
            Calendar c = Calendar.getInstance();
            Date date = c.getTime();
            SimpleDateFormat simple = new SimpleDateFormat("yyyy-MM-dd");
            date2 = simple.format(date);
        }

        String formatStyle = type == 1 ? "yyyy-MM" : "yyyy-MM-dd";
        DateFormat df = new SimpleDateFormat(formatStyle);
        Calendar c1 = Calendar.getInstance();
        Calendar c2 = Calendar.getInstance();

        c1.setTime(df.parse(date1));
        c2.setTime(df.parse(date2));

        boolean flag = true; // 第一个时间比第二个时间大
        if(c1.after(c2)) {
            // c1比c2大
            Calendar cTemp = c2;
            c2 = c1;
            c1 = cTemp;
        } else {
            flag = false;
        }

        while (!c1.after(c2)) {
            // 循环对比，直到相等，n 就是所要的结果
            n++;

            if(type == 0){
                c1.add(Calendar.DATE, 1);          // 比较天数，日期+1
            } else if(type == 1){
                c1.add(Calendar.MONTH, 1);         // 比较月份，月份+1
            } else if(type == 2) {
                c1.add(Calendar.YEAR, 1);          // 比较年份，年份+1
            } else {
                throw new ParseException("type must be [0, 1, 2]", 0);
            }
        }

        return flag ? n : -n;
    }

    /**
     * 两个时间相差距离多少天多少小时多少分多少秒
     * @param str1 时间参数 1 格式：1990-01-01 12:00:00
     * @param str2 时间参数 2 格式：2009-01-01 12:00:00
     * @return long[] 返回值为：{天, 时, 分, 秒}
     */
    public static long[] getDistanceTimes(String str1, String str2, String dateFormat) throws ParseException {
        DateFormat df = new SimpleDateFormat(dateFormat);
        Date one;
        Date two;
        long day = 0;
        long hour = 0;
        long min = 0;
        long sec = 0;

        one = df.parse(str1);
        two = df.parse(str2);
        long time1 = one.getTime();
        long time2 = two.getTime();
        long diff ;
        if(time1<time2) {
            diff = time2 - time1;
        } else {
            diff = time1 - time2;
        }

        day = diff / (24 * 60 * 60 * 1000);
        hour = (diff / (60 * 60 * 1000) - day * 24);
        min = ((diff / (60 * 1000)) - day * 24 * 60 - hour * 60);
        sec = (diff/1000-day*24*60*60-hour*60*60-min*60);

        long[] times = {day, hour, min, sec};
        return times;
    }

}
