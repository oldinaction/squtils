package cn.aezo.utils.base;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PropU extends Properties {

    public static Properties loadProperties(String filePath) {
        Properties properties = null;
        InputStream inputStream = null;
        try {
            properties = new PropU();
            inputStream = new FileInputStream(filePath);
            properties.load(inputStream);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return properties;
    }

    @Override
    public String getProperty(String key) {
        String str = super.getProperty(key);
        if(str == null || "".equals(str)) {
            return str;
        }

        String pattern = "\\$\\{.*?}";
        // 创建 Pattern 对象
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(str);

        while (m.find()) {
            String findKey = m.group();
            String fixKey = findKey.replaceAll("[${}]", "");
            String findValue = super.getProperty(fixKey);
            str = str.replaceAll(escapeExprSpecialWord(findKey), findValue);
        }
        return str;
    }

    /**
     * 转义正则特殊字符 （$()*+.[]?\^{},|）
     */
    private String escapeExprSpecialWord(String keyword) {
        if (keyword != null && keyword.length() > 0) {
            String[] fbsArr = { "\\", "$", "(", ")", "*", "+", ".", "[", "]", "?", "^", "{", "}", "|" };
            for (String key : fbsArr) {
                if (keyword.contains(key)) {
                    keyword = keyword.replace(key, "\\" + key);
                }
            }
        }
        return keyword;
    }
}
