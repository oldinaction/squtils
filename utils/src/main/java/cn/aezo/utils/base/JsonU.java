package cn.aezo.utils.base;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by smalle on 2017/12/30.
 * XmlMapper(解析xml)需要引入jackson-dataformat-xml
 */
public class JsonU {
    private static ObjectMapper objectMapper = new ObjectMapper();

    /**
     * javaBean,list,array convert to json string(map中包含list亦可转换)
     */
    public static String obj2json(Object obj) throws JsonProcessingException {
        return objectMapper.writeValueAsString(obj);
    }

    public static Map obj2map(Object obj) {
        return objectMapper.convertValue(obj, Map.class);
    }

    public static <T> T convertValue(Object obj,  Class<T> c) {
        return objectMapper.convertValue(obj, c);
    }

    /**
     * json string convert to map
     */
    public static Map<String, Object> json2map(String jsonStr) throws IOException {
        return objectMapper.readValue(jsonStr, Map.class);
    }

    /**
     * json string convert to map with javaBean(map中存放的为某个javaBean)
     */
    public static <T> Map<String, T> json2map(String jsonStr, Class<T> clazz) throws IOException {
        Map<String, Map<String,Object>> map =  objectMapper.readValue(jsonStr, new TypeReference<Map<String,T>>() {});
        Map<String, T> result = new HashMap<String, T>();
        for (Map.Entry<String, Map<String,Object>> entry : map.entrySet()) {
            result.put(entry.getKey(), map2pojo(entry.getValue(), clazz));
        }
        return result;
    }

    /**
     * json string convert to javaBean
     */
    public static <T> T json2pojo(String jsonStr, Class<T> clazz) throws IOException {
        return objectMapper.readValue(jsonStr, clazz);
    }

    /**
     * json array string convert to list with string
     */
    public static List<String> json2list(String jsonArrayStr) throws IOException {
        return json2list(jsonArrayStr, String.class);
    }

    /**
     * json array string convert to list with javaBean
     */
    public static <T> List<T> json2list(String jsonArrayStr, Class<T> clazz)throws IOException {
        List<Map<String,Object>> list = objectMapper.readValue(jsonArrayStr, new TypeReference<List<T>>() {
        });
        List<T> result = new ArrayList<T>();
        for (Map<String, Object> map : list) {
            result.add(map2pojo(map, clazz));
        }
        return result;
    }

    /**
     * map convert to javaBean
     */
    public static <T> T map2pojo(Map map, Class<T> clazz){
        return objectMapper.convertValue(map, clazz);
    }

    /**
     * 通过HTTP获取JSON数据（Map）
     * 北京的天气接口: "http://apistore.baidu.com/microservice/cityinfo?cityname=%E5%8C%97%E4%BA%AC"
     * @param url
     * @return
     */
    public static Map<String, Object> getMapByUrl(String url) {
        try {
            InputStream in = new URL(url).openStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            StringBuilder sb = new StringBuilder();
            String line;
            while((line=reader.readLine())!=null){
                sb.append(line);
            }
            return JsonU.json2map(sb.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 通过HTTP获取JSON数据（List）
     * @param url
     * @return
     */
    public static List<Map> getListByUrl(String url) {
        try {
            InputStream in = new URL(url).openStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            StringBuilder sb = new StringBuilder();
            String line;
            while((line=reader.readLine())!=null){
                sb.append(line);
            }
            return json2list(sb.toString(), Map.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
