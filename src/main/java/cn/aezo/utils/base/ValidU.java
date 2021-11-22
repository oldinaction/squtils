package cn.aezo.utils.base;

import cn.aezo.utils.mix.PasswordChecker;
import cn.hutool.core.exceptions.ValidateException;
import cn.hutool.core.lang.Validator;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 校验
 *
 * @author smalle
 * @since 2017/2/9 21:04
 */
public class ValidU extends Validator {
    public static PasswordChecker PasswordChecker = new PasswordChecker();

    /**
     * 验证是否为非空，为空时抛出异常<br>
     * 对于String类型判定是否为empty(null 或 "")<br>
     *
     * @param <T>      值类型
     * @param value    值
     * @param errorMsg 验证错误的信息
     * @return 验证后的值，验证通过返回此值，非空值
     * @throws ValidateException 验证异常
     */
    public static <T> T validateNotEmpty(T value, String errorMsg) throws ValidateException {
        if (isEmpty(value)) {
            throw new ValidateException(errorMsg);
        }
        return value;
    }

    /** 为空或者只有空白字符(" \t\n\r"等)则返回true */
    public static boolean isBlank(Object value) {
        if (value == null) {
            return true;
        }
        if (value instanceof CharSequence) {
            return isBlank((CharSequence) value);
        }
        return isEmpty(value);
    }
    private static boolean isBlank(final CharSequence cs) {
        int strLen;
        if (cs == null || (strLen = cs.length()) == 0) {
            return true;
        }
        for (int i = 0; i < strLen; i++) {
            if (Character.isWhitespace(cs.charAt(i)) == false) {
                return false;
            }
        }
        return true;
    }

    /**
     * 不为空且不为空白字符串(" \t\n\r"等)
     * @param value
     * @return
     */
    public static boolean isNotBlank(Object value) {
        return !isBlank(value);
    }

    /** 是否为空(包含String的""、集合的大小) */
    public static boolean isEmpty(Object value) {
        if (value == null) {
            return true;
        }

        // cn.hutool.json.JSONNull 对NULL做了处理
        if (value.equals(null)) {
            return true;
        }

        if (value instanceof String) {
            return ((String) value).length() == 0;
        }
        if (value instanceof Collection) {
            return ((Collection<? extends Object>) value).size() == 0;
        }
        if (value instanceof Map) {
            return ((Map<? extends Object, ? extends Object>) value).size() == 0;
        }
        if (value instanceof CharSequence) {
            return ((CharSequence) value).length() == 0;
        }

        // Number covers: BigDecimal, BigInteger, Byte, Double, Float, Integer, Long, Short
        if (value instanceof Boolean) {
            return false;
        }
        if (value instanceof Number) {
            return false;
        }
        if (value instanceof Character) {
            return false;
        }
        if (value instanceof java.util.Date) {
            return false;
        }

        return false;
    }

    /** 是否不为空(包含String的""、集合的大小) */
    public static boolean isNotEmpty(Object value) {
        return !isEmpty(value);
    }

    /** 全部为空(true) */
    public static boolean isAllEmpty(Collection<Object> collection) {
        if(isEmpty(collection)) {
            return true;
        }
        return isAllEmpty(collection.toArray());
    }

    /** 判断一组对象是否全部有效 */
    public static boolean isAllEmpty(Object... arr) {
        if(arr == null) {
            return true;
        }
        for (Object o : arr) {
            if (!isEmpty(o)) {
                return false;
            }
        }
        return true;
    }

    /** 全部不为空(true) */
    public static boolean isAllNotEmpty(Collection<Object> collection) {
        if(isEmpty(collection)) {
            return false;
        }
        return isAllNotEmpty(collection.toArray());
    }

    /** 判断一组对象是否全部无效 */
    public static boolean isAllNotEmpty(Object... arr) {
        boolean flag = true;
        for (Object o : arr) {
            if(isEmpty(o)) {
                flag = false;
                break;
            }
        }
        return flag;
    }

    /** 至少有一个为空(true) */
    public static Boolean hasEmpty(Collection<Object> collection) {
        return !isAllNotEmpty(collection);
    }

    public static Boolean hasEmpty(Object... arr) {
        return !isAllNotEmpty(arr);
    }

    /** 至少有一个不为空(true) */
    public static Boolean hasNotEmpty(Collection<Object> collection) {
        return !isAllEmpty(collection);
    }

    public static Boolean hasNotEmpty(Object... arr) {
        return !isAllEmpty(arr);
    }

    public static boolean isTrue(Object value) {
        if(value == null) {
            return false;
        }
        if(value instanceof Boolean) {
            return (boolean) value;
        }
        return false;
    }

    public static boolean isFalse(Object value) {
        return !(isTrue(value));
    }

    /** 相互equals(包含null == null) */
    public static boolean equals(Object obj, Object obj2) {
        if (obj == null) {
            return obj2 == null;
        } else {
            return obj.equals(obj2);
        }
    }

    /** 相互equals且不为null */
    public static boolean equalsAndNotNull(Object obj, Object obj2) {
        if (obj == null) {
            return false;
        } else {
            return obj.equals(obj2);
        }
    }

    /**
     * 是否是日期
     * @param date
     * @param format 自定义格式，如：yyyy-MM-dd HH:mm:ss.SSS
     * @return
     */
    public static boolean isDate(String date, String format){
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        try {
            sdf.parse(date);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }

    public static boolean checkPassword(String pass) {
        return PasswordChecker.check(pass);
    }

    /**
     * 正则验证
     */
    public static class Regex {
        /** 整数  */
        private static final String V_INTEGER="^-?[1-9]\\d*$";

        /**  正整数 */
        private static final String V_POSITIVE_INTEGER="^[1-9]\\d*$";

        /**负整数 */
        private static final String V_NEGATINE_INTEGER="^-[1-9]\\d*$";

        /** 数字 */
        private static final String V_NUMBER="^([+-]?)\\d*\\.?\\d+$";

        /**正数 */
        private static final String V_POSITIVE_NUMBER="^[1-9]\\d*|0$";

        /** 负数 */
        private static final String V_NEGATINE_NUMBER="^-[1-9]\\d*|0$";

        /** 浮点数 */
        private static final String V_FLOAT="^([+-]?)\\d*\\.\\d+$";

        /** 正浮点数 */
        private static final String V_POSTTIVE_FLOAT="^[1-9]\\d*.\\d*|0.\\d*[1-9]\\d*$";

        /** 负浮点数 */
        private static final String V_NEGATIVE_FLOAT="^-([1-9]\\d*.\\d*|0.\\d*[1-9]\\d*)$";

        /** 非负浮点数（正浮点数 + 0） */
        private static final String V_UNPOSITIVE_FLOAT="^[1-9]\\d*.\\d*|0.\\d*[1-9]\\d*|0?.0+|0$";

        /** 非正浮点数（负浮点数 + 0） */
        private static final String V_UN_NEGATIVE_FLOAT="^(-([1-9]\\d*.\\d*|0.\\d*[1-9]\\d*))|0?.0+|0$";

        /** 邮件 */
        private static final String V_EMAIL="^\\w+((-\\w+)|(\\.\\w+))*\\@[A-Za-z0-9]+((\\.|-)[A-Za-z0-9]+)*\\.[A-Za-z0-9]+$";

        /** 颜色 */
        private static final String V_COLOR="^[a-fA-F0-9]{6}$";

        /** url */
        private static final String V_URL="^http[s]?:\\/\\/([\\w-]+\\.)+[\\w-]+([\\w-./?%&=]*)?$";

        /** 仅中文 */
        private static final String V_CHINESE="^[\\u4E00-\\u9FA5\\uF900-\\uFA2D]+$";

        /** 仅ACSII字符 */
        private static final String V_ASCII="^[\\x00-\\xFF]+$";

        /** 邮编 */
        private static final String V_ZIPCODE="^\\d{6}$";

        /** 手机 */
        private static final String V_MOBILE="^(1)[0-9]{10}$";

        /** 电话号码的函数(包括验证国内区号,国际区号,分机号) */
        private static final String V_TEL="^(([0\\+]\\d{2,3}-)?(0\\d{2,3})-)?(\\d{7,8})(-(\\d{3,}))?$";

        /** ip地址 */
        private static final String V_IP4="^(25[0-5]|2[0-4]\\d|[0-1]\\d{2}|[1-9]?\\d)\\.(25[0-5]|2[0-4]\\d|[0-1]\\d{2}|[1-9]?\\d)\\.(25[0-5]|2[0-4]\\d|[0-1]\\d{2}|[1-9]?\\d)\\.(25[0-5]|2[0-4]\\d|[0-1]\\d{2}|[1-9]?\\d)$";

        /** 非空 */
        private static final String V_NOTEMPTY="^\\S+$";

        /** 图片  */
        private static final String V_PICTURE="(.*)\\.(jpg|bmp|gif|ico|pcx|jpeg|tif|png|raw|tga)$";

        /**  压缩文件  */
        private static final String V_RAR="(.*)\\.(rar|zip|7zip|tgz)$";

        /** 日期 */
        private static final String V_DATE="^((((1[6-9]|[2-9]\\d)\\d{2})-(0?[13578]|1[02])-(0?[1-9]|[12]\\d|3[01]))|(((1[6-9]|[2-9]\\d)\\d{2})-(0?[13456789]|1[012])-(0?[1-9]|[12]\\d|30))|(((1[6-9]|[2-9]\\d)\\d{2})-0?2-(0?[1-9]|1\\d|2[0-8]))|(((1[6-9]|[2-9]\\d)(0[48]|[2468][048]|[13579][26])|((16|[2468][048]|[3579][26])00))-0?2-29-)) (20|21|22|23|[0-1]?\\d):[0-5]?\\d:[0-5]?\\d$";

        /** QQ号码  */
        private static final String V_QQ_NUMBER="^[1-9]*[1-9][0-9]*$";

        /** 用来用户注册。匹配由数字、26个英文字母或者下划线组成的字符串 */
        private static final String V_USERNAME="^\\w+$";

        /** 字母 */
        private static final String V_LETTER="^[A-Za-z]+$";

        /** 大写字母  */
        private static final String V_LETTER_U="^[A-Z]+$";

        /** 小写字母 */
        private static final String V_LETTER_I="^[a-z]+$";

        /** 身份证  */
        private static final String V_IDCARD ="^(\\d{15}$|^\\d{18}$|^\\d{17}(\\d|X|x))$";

        /**验证密码(数字和英文同时存在)*/
        private static final String V_PASSWORD_REG="[A-Za-z]+[0-9]";

        /**验证密码长度(6-18位)*/
        private static final String V_PASSWORD_LENGTH="^\\d{6,18}$";

        /**验证两位数*/
        private static final String V_TWO＿POINT="^[0-9]+(.[0-9]{2})?$";

        /**验证一个月的31天*/
        private static final String V_31DAYS="^((0?[1-9])|((1|2)[0-9])|30|31)$";


        private Regex(){}


        /**
         * 验证是不是整数
         * @param value 要验证的字符串 要验证的字符串
         * @return  如果是符合格式的字符串,返回 <b>true </b>,否则为 <b>false </b>
         */
        public static boolean integer(String value){
            return match(V_INTEGER,value);
        }

        /**
         * 验证是不是正整数
         * @param value 要验证的字符串
         * @return  如果是符合格式的字符串,返回 <b>true </b>,否则为 <b>false </b>
         */
        public static boolean positiveInteger(String value){
            return match(V_POSITIVE_INTEGER,value);
        }

        /**
         * 验证是不是负整数
         * @param value 要验证的字符串
         * @return  如果是符合格式的字符串,返回 <b>true </b>,否则为 <b>false </b>
         */
        public static boolean negatineInteger(String value){
            return match(V_NEGATINE_INTEGER,value);
        }

        /**
         * 验证是不是数字
         * @param value 要验证的字符串
         * @return 如果是符合格式的字符串,返回 <b>true </b>,否则为 <b>false </b>
         */
        public static boolean number(String value){
            return match(V_NUMBER,value);
        }

        /**
         * 验证是不是正数
         * @param value 要验证的字符串
         * @return 如果是符合格式的字符串,返回 <b>true </b>,否则为 <b>false </b>
         */
        public static boolean positiveNumber(String value){
            return match(V_POSITIVE_NUMBER,value);
        }

        /**
         * 验证是不是负数
         * @param value 要验证的字符串
         * @return 如果是符合格式的字符串,返回 <b>true </b>,否则为 <b>false </b>
         */
        public static boolean negatineNumber(String value){
            return match(V_NEGATINE_NUMBER,value);
        }

        /**
         * 验证是不是ASCII
         * @param value 要验证的字符串
         * @return 如果是符合格式的字符串,返回 <b>true </b>,否则为 <b>false </b>
         */
        public static boolean ASCII(String value){
            return match(V_ASCII,value);
        }

        /**
         * 验证是不是中文
         * @param value 要验证的字符串
         * @return 如果是符合格式的字符串,返回 <b>true </b>,否则为 <b>false </b>
         */
        public static boolean chinese(String value){
            return match(V_CHINESE,value);
        }

        /**
         * 验证是不是颜色
         * @param value 要验证的字符串
         * @return 如果是符合格式的字符串,返回 <b>true </b>,否则为 <b>false </b>
         */
        public static boolean color(String value){
            return match(V_COLOR,value);
        }

        /**
         * 验证是不是日期
         * @param value 要验证的字符串
         * @return 如果是符合格式的字符串,返回 <b>true </b>,否则为 <b>false </b>
         */
        public static boolean date(String value){
            return match(V_DATE,value);
        }

        /**
         * 验证一个月的31天
         * @param value 要验证的字符串
         * @return 如果是符合格式的字符串,返回 <b>true </b>,否则为 <b>false </b>
         */
        public static boolean is31Days(String value){
            return match(V_31DAYS,value);
        }

        /**
         * 验证是不是邮箱地址
         * @param value 要验证的字符串
         * @return 如果是符合格式的字符串,返回 <b>true </b>,否则为 <b>false </b>
         */
        public static boolean email(String value){
            return match(V_EMAIL,value);
        }

        /**
         * 验证是不是浮点数
         * @param value 要验证的字符串
         * @return  如果是符合格式的字符串,返回 <b>true </b>,否则为 <b>false </b>
         */
        public static boolean isFloat(String value){
            return match(V_FLOAT,value);
        }

        /**
         * 验证正浮点数
         * @param value 要验证的字符串
         * @return 如果是符合格式的字符串,返回 <b>true </b>,否则为 <b>false </b>
         */
        public static boolean positiveFloat(String value){
            return match(V_POSTTIVE_FLOAT,value);
        }

        /**
         * 验证是不是负浮点数
         * @param value 要验证的字符串
         * @return 如果是符合格式的字符串,返回 <b>true </b>,否则为 <b>false </b>
         */
        public static boolean negativeFloat(String value){
            return match(V_NEGATIVE_FLOAT,value);
        }

        /**
         * 验证是不是字母
         * @param value 要验证的字符串
         * @return 如果是符合格式的字符串,返回 <b>true </b>,否则为 <b>false </b>
         */
        public static boolean letter(String value){
            return match(V_LETTER,value);
        }

        /**
         * 验证是不是小写字母
         * @param value 要验证的字符串
         * @return 如果是符合格式的字符串,返回 <b>true </b>,否则为 <b>false </b>
         */
        public static boolean letterLower(String value){
            return match(V_LETTER_I,value);
        }

        /**
         * 验证是不是大写字母
         * @param value 要验证的字符串
         * @return 如果是符合格式的字符串,返回 <b>true </b>,否则为 <b>false </b>
         */
        public static boolean letterUpper(String value){
            return match(V_LETTER_U,value);
        }

        /**
         * 验证非空
         * @param value 要验证的字符串
         * @return 如果是符合格式的字符串,返回 <b>true </b>,否则为 <b>false </b>
         */
        public static boolean notEmpty(String value){
            return match(V_NOTEMPTY,value);
        }

        /**
         * 验证是不是正确的身份证号码
         * @param value 要验证的字符串
         * @return 如果是符合格式的字符串,返回 <b>true </b>,否则为 <b>false </b>
         */
        public static boolean IDcard(String value){
            return match(V_IDCARD,value);
        }

        /**
         * 验证是不是正确的IP地址
         * @param value 要验证的字符串
         * @return 如果是符合格式的字符串,返回 <b>true </b>,否则为 <b>false </b>
         */
        public static boolean IP4(String value){
            return match(V_IP4,value);
        }

        /**
         * 验证是不是手机号码
         * @param value 要验证的字符串
         * @return 如果是符合格式的字符串,返回 <b>true </b>,否则为 <b>false </b>
         */
        public static boolean phoneNumber(String value){
            return match(V_MOBILE,value);
        }

        /**
         * 验证座机号码
         * @param value 要验证的字符串
         * @return 如果是符合格式的字符串,返回 <b>true </b>,否则为 <b>false </b>
         */
        public static boolean telNumber(String value){
            return match(V_TEL,value);
        }

        /**
         * 验证URL
         * @param value 要验证的字符串
         * @return 如果是符合格式的字符串,返回 <b>true </b>,否则为 <b>false </b>
         */
        public static boolean url(String value){
            return match(V_URL,value);
        }

        /**
         * 验证邮编
         * @param value 要验证的字符串
         * @return 如果是符合格式的字符串,返回 <b>true </b>,否则为 <b>false </b>
         */
        public static boolean zipCode(String value){
            return match(V_ZIPCODE,value);
        }

        /**
         * 验证用户注册。匹配由数字、26个英文字母或者下划线组成的字符串
         * @param value 要验证的字符串
         * @return 如果是符合格式的字符串,返回 <b>true </b>,否则为 <b>false </b>
         */
        public static boolean userName(String value){
            return match(V_USERNAME,value);
        }

        /**
         * 验证密码数字和英文同时存在, 且长度(6~18位)
         * @param value 要验证的字符串
         * @return 如果是符合格式的字符串,返回 <b>true </b>,否则为 <b>false </b>
         */
        public static boolean password(String value){
            return match(V_PASSWORD_REG,value) && match(V_PASSWORD_LENGTH,value);
        }

        /**
         * 验证图片文件名
         * @param value 要验证的字符串
         * @return 如果是符合格式的字符串,返回 <b>true </b>,否则为 <b>false </b>
         */
        public static boolean pictureName(String value){
            return match(V_PICTURE,value);
        }

        /**
         * 验证QQ号码
         * @param value 要验证的字符串
         * @return 如果是符合格式的字符串,返回 <b>true </b>,否则为 <b>false </b>
         */
        public static boolean QQnumber(String value){
            return match(V_QQ_NUMBER,value);
        }

        /**
         * 验证压缩文件名
         * @param value 要验证的字符串
         * @return 如果是符合格式的字符串,返回 <b>true </b>,否则为 <b>false </b>
         */
        public static boolean rarName(String value){
            return match(V_RAR,value);
        }

        /**
         * 验证两位小数
         * @param value 要验证的字符串
         * @return 如果是符合格式的字符串,返回 <b>true </b>,否则为 <b>false </b>
         */
        public static boolean twoPoint(String value){
            return match(V_TWO＿POINT,value);
        }

        /**
         * 验证非正浮点数
         * @param value 要验证的字符串
         * @return 如果是符合格式的字符串,返回 <b>true </b>,否则为 <b>false </b>
         */
        public static boolean unNegativeFloat(String value){
            return match(V_UN_NEGATIVE_FLOAT,value);
        }

        /**
         * 验证非负浮点数
         * @param value 要验证的字符串
         * @return 如果是符合格式的字符串,返回 <b>true </b>,否则为 <b>false </b>
         */
        public static boolean unPositiveFloat(String value){
            return match(V_UNPOSITIVE_FLOAT,value);
        }

        /**
         * @param regex 正则表达式字符串
         * @param str 要匹配的字符串
         * @return 如果str 符合 regex的正则表达式格式,返回true, 否则返回 false;
         */
        private static boolean match(String regex, String str) {
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(str);
            return matcher.matches();
        }
    }
}
