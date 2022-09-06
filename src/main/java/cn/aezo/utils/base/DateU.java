package cn.aezo.utils.base;

import cn.hutool.core.date.DateUtil;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;

/**
 * Created by smalle on 2017/1/12.
 */
public class DateU extends DateUtil {

    /**
     * 日期格式：yyyy-MM-dd HH:mm:ss.SSS
     * @param format
     * @return
     * @author smalle
     * @date 2016年12月25日 上午8:05:40
     */
    public static String nowTimeStr(String format) {
        Date currentTime = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat(format);
        return formatter.format(currentTime);
    }

    /**
     * 当前时间 yyyy-MM-dd
     */
    public static String nowDateDefault() {
        return nowTimeStr("yyyy-MM-dd");
    }

    /**
     * 当前时间 yyyy/MM/dd
     */
    public static String nowDateDefault2() {
        return nowTimeStr("yyyy/MM/dd");
    }

    /**
     * 当前时间 yyyy-MM-dd HH:mm:ss
     */
    public static String nowTimeDefault() {
        return nowTimeStr("yyyy-MM-dd HH:mm:ss");
    }

    /**
     * 当前时间 yyyy/MM/dd HH:mm:ss
     */
    public static String nowTimeDefault2() {
        return nowTimeStr("yyyy/MM/dd HH:mm:ss");
    }

    /**
     * 返回系统当前时间
     * @return Timestamp for right now
     */
    public static Timestamp nowTimestamp() {
        return getTimestamp(System.currentTimeMillis());
    }


    /**
     * 日期打印(yyyy-MM-dd HH:mm:ss)
     * @param date
     */
    public void printDate(Date date) {
        System.out.println(formatToTime(date));
    }

    /**
     * 日期格式化为字符串(yyyy/MM/dd HH:mm:ss)
     * @param date
     * @return
     */
    public static String formatToTime(Date date) {
        return format(date, "yyyy/MM/dd HH:mm:ss");
    }

    /**
     * 日期格式化为字符串(yyyy/MM/dd)
     * @param date
     * @return
     */
    public static String formatToDate(Date date) {
        return format(date, "yyyy/MM/dd");
    }

    /**
     * 日期格式化为字符串
     * @param date
     * @param format 如：yyyy/MM/dd HH:mm:ss
     * @return
     */
    public static String format(Date date, String format) {
        return format(date, new SimpleDateFormat(format));
    }

    /**
     * 日期格式化为字符串
     * @param date
     * @param dateFormat yyyy/MM/dd HH:mm:ss.SSS
     * @return
     */
    public static String format(Date date, SimpleDateFormat dateFormat) {
        if(date == null) {
            return "";
        }
        return dateFormat.format(date);
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

    /**
     * 两个时间相差距离多少天多少小时多少分多少秒
     * @param str1
     * @param str2
     * @return
     * @throws ParseException
     */
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

    /**
     * 加减时间
     * @param date 为空则为当前时间。如当前时间的前一天：null, Calendar.DAY_OF_MONTH, -1
     * @param field
     * @param amount
     * @return
     */
    public static Date add(Date date, int field, int amount) {
        Calendar calendar = new GregorianCalendar();
        if(date != null) {
            calendar.setTime(date);
        }
        calendar.add(field, amount);
        return calendar.getTime();
    }

    /**
     * 获取一天的开始时间 yyyy/MM/dd 00:00:00
     * @param date 为空则为当前时间
     * @return
     */
    public static Date getDayStart(Date date) {
        Calendar calendar = new GregorianCalendar();
        if(date != null) {
            calendar.setTime(date);
        }

        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        return calendar.getTime();
    }

    /**
     * 获取一天的结束时间 yyyy/MM/dd 23:59:59
     * @param date 为空则为当前时间
     * @return
     */
    public static Date getDayEnd(Date date) {
        Calendar calendar = new GregorianCalendar();
        if(date != null) {
            calendar.setTime(date);
        }

        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);

        return calendar.getTime();
    }

    /**
     * 获取某月的开始时间 yyyy/MM/01 00:00:00
     * @param date 为空则为当前时间
     * @return
     */
    public static Date getMonthStart(Date date) {
        Calendar calendar = new GregorianCalendar();
        if(date != null) {
            calendar.setTime(date);
        }

        // 获取某月最小天数
        int firstDay = calendar.getActualMinimum(Calendar.DAY_OF_MONTH);
        // 设置日历中月份的最小天数
        calendar.set(Calendar.DAY_OF_MONTH, firstDay);

        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        return calendar.getTime();
    }

    /**
     * 获取某月的结束时间 yyyy/MM/31 23:59:59
     * @param date 为空则为当前时间
     * @return
     */
    public static Date getMonthEnd(Date date) {
        Calendar calendar = new GregorianCalendar();
        if(date != null) {
            calendar.setTime(date);
        }

        //获取某月最大天数
        int lastDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        //设置日历中月份的最大天数
        calendar.set(Calendar.DAY_OF_MONTH, lastDay);

        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);

        return calendar.getTime();
    }

    /**
     * 填充日期字符串后面的时间并转换为日期
     * @author smalle
     * @since 2021/2/8
     * @param map
     * @param keys 需要改变时间的字段名，字段值支持String/String[]/List<String>
     * @return void
     */
    public static void fillTimeStrAndParse(Map<String, Object> map, String... keys) {
        fillTimeStr(map, keys);
        parseContextDateStr(map, null, keys);
    }

    /**
     * 填充日期字符串后面的时间
     * @author smalle
     * @since 2021/2/8
     * @param map
     * @param keys 需要填充时间的字段名，字段值支持String/String[]/List<String>
     * @return void
     */
    public static void fillTimeStr(Map<String, Object> map, String... keys) {
        for (String key : keys) {
            if(ValidU.isEmpty(map.get(key))) {
                continue;
            }
            Object val = map.get(key);
            if(val instanceof String[]) {
                String[] arr = (String[]) map.get(key);
                if(arr.length > 0) {
                    arr[0] = arr[0] + " 00:00:00";
                }
                if(arr.length > 1) {
                    arr[1] = arr[1] + " 23:59:59";
                }
            }
            if(val instanceof List) {
                List arr = (List) map.get(key);
                if(arr.size() > 0 && arr.get(0) instanceof String) {
                    arr.set(0, arr.get(0) + " 00:00:00");
                }
                if(arr.size() > 1 && arr.get(1) instanceof String) {
                    arr.set(1, arr.get(1) + " 23:59:59");
                }
            }
        }
    }

    /**
     * 将Map中的字符串时间转成时间格式
     * @author smalle
     * @since 2021/2/8
     * @param map
     * @param pattern 时间格式，默认为 yyyy-MM-dd HH:mm:ss
     * @param keys 需要转换的字段名，字段值支持String/String[]/List<String>
     * @return void
     */
    public static void parseContextDateStr(Map<String, Object> map, String pattern, String... keys) {
        if(ValidU.isEmpty(pattern)) {
            pattern = "yyyy-MM-dd HH:mm:ss";
        }
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        for (String key : keys) {
            try {
                if(map.get(key) != null) {
                    if(map.get(key) instanceof String) {
                        map.put(key, sdf.parse((String) map.get(key)));
                    } else if(map.get(key) instanceof String[]) {
                        String[] arr = (String[]) map.get(key);
                        Date[] tm = new Date[arr.length];
                        for (int i = 0; i < arr.length; i++) {
                            if(ValidU.isNotEmpty(arr[i])) {
                                tm[i] = sdf.parse(arr[i]);
                            } else {
                                tm[i] = null;
                            }
                        }
                        map.put(key, tm);
                    } else if (map.get(key) instanceof List) {
                        List arr = (List) map.get(key);
                        if(arr.size() != 0 && arr.get(0) instanceof String) {
                            List<Date> tm = new ArrayList<>();
                            for (int i = 0; i < arr.size(); i++) {
                                if(arr.get(i) instanceof String) {
                                    if(ValidU.isNotEmpty(arr.get(i))) {
                                        tm.add(sdf.parse((String) arr.get(i)));
                                    } else {
                                        tm.add(null);
                                    }
                                }
                            }
                            map.put(key, tm);
                        }
                    }
                }
            } catch (ParseException e) {
                throw new ExceptionU("解析时间出错");
            }
        }
    }

}
