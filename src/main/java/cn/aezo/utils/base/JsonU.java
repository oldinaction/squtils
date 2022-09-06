package cn.aezo.utils.base;

import cn.aezo.utils.func.AdjustJsonItemValueFunc;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * 注意:<br/>
 * Hutool解析json字符串时，如果不忽略NULL，则会将NULL值设值成JSONNull对象<br/>
 * 而Jackson在序列话输出时会出现报错"No serializer found for class cn.hutool.json.JSONNull"<br/>
 * 因此需要增加下列转换器<br/>
 * <pre>
 * simpleModule.addSerializer(JSONNull.class, new JsonSerializer<JSONNull>(){
 *     @Override
 *     public void serialize(JSONNull jsonNull, JsonGenerator jsonGenerator
 *             , SerializerProvider serializerProvider) throws IOException {
 *         jsonGenerator.writeNull();
 *     }
 * });
 * simpleModule.addDeserializer(JSONNull.class, new JsonDeserializer<JSONNull>() {
 *     @Override
 *     public JSONNull deserialize(JsonParser jsonParser
 *             , DeserializationContext deserializationContext) {
 *         return null;
 *     }
 * });
 * </pre>
 *
 * @author smalle
 * @date 2017/12/30
 */
@Slf4j
public class JsonU {
    private static Method toMapMethod = null;
    private static Method toBeanMethod = null;
    private static Method toListMethod = null;

    static {
        try {
            Class jacksonUClass = Class.forName("cn.aezo.utils.ext.JacksonU");
            toMapMethod = jacksonUClass.getMethod("toMap", String.class);
            toBeanMethod = jacksonUClass.getMethod("toBean", String.class, Class.class);
            toListMethod = jacksonUClass.getMethod("toList", String.class);
        } catch (ClassNotFoundException | NoSuchMethodException e) {
        }
    }

    public static String toJsonStr(Object obj) {
        return JSONUtil.toJsonStr(obj);
    }

    public static Map<String, Object> toMap(String str) {
        if(toMapMethod != null) {
            return ReflectU.invokeStatic(toMapMethod, str);
        } else {
            return JSONUtil.toBean(str, Map.class, true);
        }
    }

    public static Map<String, Object> toMapSafe(String str) {
        Map<String, Object> ret = toMap(str);
        if(ret == null) {
            ret = new HashMap<>();
        }
        return ret;
    }

    public static <T> T toBean(String str, Class<T> beanClass) {
        if(toBeanMethod != null) {
            return ReflectU.invokeStatic(toBeanMethod, str, beanClass);
        } else {
            return JSONUtil.toBean(str, beanClass);
        }
    }

    public static List<Map<String, Object>> toList(String str) {
        if(toListMethod != null) {
            return ReflectU.invokeStatic(toListMethod, str);
        } else {
            return JSONUtil.toBean(str, List.class, true);
        }
    }

    public static List toListSafe(String str) {
        List ret = toList(str);
        if(ret == null) {
            ret = new ArrayList();
        }
        return ret;
    }

    /**
     * 递归处理JSON字符串的每一项，将其值进行trim
     * @param jsonStr
     * @return
     */
    public static Object adjustJsonItemValueWithTrim(String jsonStr) {
        Object obj = jsonStr;
        try {
            obj = toMap(jsonStr);
        } catch (Exception e) {
            try {
                obj = toList(jsonStr);
            } catch (Exception e2) {
                // do nothing
            }
        }
        return adjustJsonItemValue(obj, (v, k) -> {
            if (v instanceof String) {
                return v.toString().trim();
            }
            return v;
        });
    }

    /**
     * 递归处理JSON的每一项
     * @param json
     * @param adjustFunc 处理函数
     * @return
     */
    public static Object adjustJsonItemValue(Object json, AdjustJsonItemValueFunc adjustFunc) {
        if (json instanceof List) {
            List list = new LinkedList();
            Iterator<Object> it = ((List) json).iterator();
            while (it.hasNext()) {
                Object obj = it.next();
                if(obj instanceof List || obj instanceof Map) {
                    list.add(adjustJsonItemValue(obj, adjustFunc));
                } else {
                    // 普通对象
                    list.add(adjustFunc.adjustValue(obj, null));
                }
            }
            return list;
        } else if (json instanceof Map) {
            Map map = new HashMap<>();
            Map jsonObject = (Map) json;
            for (Object k : jsonObject.keySet()) {
                Object v = jsonObject.get(k);
                map.put(k, adjustJsonItemValue(v, adjustFunc));
            }
            return map;
        } else {
            // 普通对象
            return adjustFunc.adjustValue(json, null);
        }
    }

    /**
     * 根据URL地址，获取其中的参数
     * @param url
     * @return
     */
    public static Map<String, String> getMapByUrl(String url) {
        Map<String, String> map = new HashMap<>();
        if (!url.contains("?")) {
            return new HashMap<>();
        }
        String[] parts = url.split("\\?", 2);
        if (parts.length < 2) {
            return new HashMap<>();
        }
        String parsedStr = parts[1];
        if (parsedStr.contains("&")) {
            String[] multiParamObj = parsedStr.split("&");
            for (String obj : multiParamObj) {
                parseBasicParam(map, obj);
            }
            return map;
        }
        parseBasicParam(map, parsedStr);
        return map;

    }
    private static void parseBasicParam(Map<String, String> map, String str) {
        String[] paramObj = str.split("=");
        if (paramObj.length < 2) {
            return;
        }
        map.put(paramObj[0], paramObj[1]);
    }

    /**
     * 通过HTTP请求获取JSON数据（Map）
     * 北京的天气接口: "http://apistore.baidu.com/microservice/cityinfo?cityname=%E5%8C%97%E4%BA%AC"
     * @param url
     * @return
     */
    public static Map<String, Object> getMapByUrlGet(String url) {
        try {
            InputStream in = new URL(url).openStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            StringBuilder sb = new StringBuilder();
            String line;
            while((line=reader.readLine())!=null){
                sb.append(line);
            }
            return toMap(sb.toString());
        } catch (Exception e) {
            log.error("", e);
        }
        return null;
    }

    /**
     * 通过HTTP请求获取JSON数据（List）
     * @param url
     * @return
     */
    public static List<Map<String, Object>> getListByUrlGet(String url) {
        try {
            InputStream in = new URL(url).openStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            StringBuilder sb = new StringBuilder();
            String line;
            while((line=reader.readLine())!=null){
                sb.append(line);
            }
            return toList(sb.toString());
        } catch (Exception e) {
            log.error("", e);
        }
        return null;
    }
}
