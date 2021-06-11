package cn.aezo.utils.base;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by smalle on 2017/12/30.
 */
@Slf4j
public class JsonU {

    public static String toJsonStr(Object obj) {
        return JSONUtil.toJsonStr(obj);
    }

    public static <T> T toBean(String jsonString, Class<T> beanClass) {
        return JSONUtil.toBean(jsonString, beanClass);
    }

    public static Map<String, Object> toMapSafe(String str) {
        Map<String, Object> ret = JSONUtil.toBean(str, Map.class, true);
        if(ret == null) {
            ret = new HashMap<>();
        }
        return ret;
    }

    public static JSONArray toListSafe(String str) {
        JSONArray ret = JSONUtil.toBean(str, JSONArray.class, true);
        if(ret == null) {
            ret = new JSONArray();
        }
        return ret;
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
            return JSONUtil.toBean(sb.toString(), Map.class);
        } catch (Exception e) {
            log.error("", e);
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
            return JSONUtil.toList((JSONArray) JSONUtil.toBean(sb.toString(), JSONArray.class, true), Map.class);
        } catch (Exception e) {
            log.error("", e);
        }
        return null;
    }
}
