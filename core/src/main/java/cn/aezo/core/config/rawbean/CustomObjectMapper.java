package cn.aezo.core.config.rawbean;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class CustomObjectMapper extends ObjectMapper {
    public CustomObjectMapper() {
        this.configure(SerializationFeature.INDENT_OUTPUT, true);
    }

    public CustomObjectMapper setNotContainNull() {
        this.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        return this;
    }

    public CustomObjectMapper setCamelCaseToLowerCaseWithUnderscores() {
        this.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
        return this;
    }

    public CustomObjectMapper setDateFormatPattern(String dateFormatPattern) {
        if (StringUtils.isNotEmpty(dateFormatPattern)) {
            DateFormat dateFormat = new SimpleDateFormat(dateFormatPattern);
            this.setDateFormat(dateFormat);
        }
        return this;
    }

    public CustomObjectMapper setDisableWriteDatesAsTimestamps(boolean disable) {
        if (disable) {
            // 防止LocalDate等序列化成一个Map TODO JDK8 SpringBoot2才需要
            // this.registerModule(new JavaTimeModule());
            // this.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        }
        return this;
    }

    public CustomObjectMapper setNotContainNullKey() {
        this.getSerializerProvider().setNullKeySerializer(new MyNullKeySerializer());
        return this;
    }

    private class MyNullKeySerializer extends JsonSerializer<Object> {
        @Override
        public void serialize(Object nullKey, JsonGenerator jsonGenerator, SerializerProvider unused) throws IOException {
            jsonGenerator.writeFieldName("");
        }
    }
}
