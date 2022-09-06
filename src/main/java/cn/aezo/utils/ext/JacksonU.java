package cn.aezo.utils.ext;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import java.util.List;
import java.util.Map;

public class JacksonU {

    public static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        // 否则在序列化返回前端时报错: out of START_OBJECT token
        objectMapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
    }

    @SneakyThrows
    public static Map<String, Object> toMap(String str) {
        return objectMapper.readValue(str, Map.class);
    }

    @SneakyThrows
    public static <T> Class<T> toBean(String str, Class<T> beanClass) {
        return (Class<T>) objectMapper.readValue(str, beanClass);
    }

    @SneakyThrows
    public static List<Map<String, Object>> toList(String str) {
        return objectMapper.readValue(str, List.class);
    }

}
