package cn.aezo.core.config.rawbean;

import cn.com.unilog.uyida.common.config.DsConstant;
import org.springframework.core.convert.converter.Converter;
import org.springframework.util.StringUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class StringToDateConverter implements Converter<String, Date> {
    @Override
    public Date convert(String value) {
        return parseDate(value);
    }


    public static Date parseDate(Object dateString) {
        if(StringUtils.isEmpty(dateString)) {
            return null;
        }

        try {
            String value = (String) dateString;
            value = value.trim();

            if(value.contains("-") || value.contains("/")) {
                SimpleDateFormat formatter;
                if(value.contains(":")) {
                    if(value.contains("T") && value.contains("Z")) {
                        // 前端js插件qs默认格式化时间为1970-01-01T00:00:00.007Z
                        formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
                    } else {
                        formatter = new SimpleDateFormat(DsConstant.dateFormat);
                    }
                } else {
                    formatter = new SimpleDateFormat(DsConstant.dateFormatShort);
                }

                Date dtDate = formatter.parse(value);
                return dtDate;
            } else if(value.matches("^\\d+$")) {
                Long lDate = new Long(value);
                return new Date(lDate);
            }
        } catch (Exception e) {
            throw new RuntimeException(String.format("parser %s to Date fail", dateString));
        }
        throw new RuntimeException(String.format("parser %s to Date fail", dateString));
    }

}
