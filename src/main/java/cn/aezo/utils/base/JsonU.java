package cn.aezo.utils.base;

import cn.aezo.utils.func.AdjustJsonItemValueFunc;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
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
     * 递归处理JSON字符串的每一项，将其值进行trim
     * @param jsonStr
     * @return
     */
    public static Object adjustJsonItemValueWithTrim(String jsonStr) {
        Object obj = jsonStr;
        try {
            obj = JSONUtil.parseObj(jsonStr);
        } catch (Exception e) {
            try {
                obj = JSONUtil.parseArray(jsonStr);
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
        if (json instanceof JSONArray) {
            List list = new LinkedList();
            Iterator<Object> it = ((JSONArray) json).iterator();
            while (it.hasNext()) {
                Object obj = it.next();
                if(obj instanceof JSONArray || obj instanceof JSONObject) {
                    list.add(adjustJsonItemValue(obj, adjustFunc));
                } else {
                    // 普通对象
                    list.add(adjustFunc.adjustValue(obj, null));
                }
            }
            return list;
        } else if (json instanceof JSONObject) {
            Map<String, Object> map = new HashMap<>();
            JSONObject jsonObject = (JSONObject) json;
            for (String k : jsonObject.keySet()) {
                Object v = jsonObject.get(k);
                map.put(k, adjustJsonItemValue(v, adjustFunc));
            }
            return map;
        } else {
            // 普通对象
            return adjustFunc.adjustValue(json, null);
        }
    }
    // public static Map<String, Object> adjustJsonItemValue(String jsonStr, AdjustJsonItemValueFunc adjustFunc) {
    //     Map<String, Object> map = new HashMap<>();
    //     JSONObject jsonObject = JSONUtil.toBean(jsonStr, JSONObject.class, true);
    //     for (String k : jsonObject.keySet()) {
    //         Object v = jsonObject.get(k);
    //         if (v instanceof JSONArray) {
    //             List list = new ArrayList<>();
    //             Iterator<Object> it = ((JSONArray) v).iterator();
    //             while (it.hasNext()) {
    //                 Object obj = it.next();
    //                 if(obj instanceof JSONArray || obj instanceof JSONObject) {
    //                     list.add(adjustJsonItemValue(obj.toString(), adjustFunc));
    //                 } else {
    //                     list.add(adjustFunc.adjustValue(k, obj));
    //                 }
    //                 map.put(k, list);
    //             }
    //             map.put(k, list);
    //         } else if (v instanceof JSONObject) {
    //             map.put(k, adjustJsonItemValue(v.toString(), adjustFunc));
    //         } else {
    //             Object adjustValue = adjustFunc.adjustValue(k, v);
    //             map.put(k, adjustValue);
    //         }
    //     }
    //     return map;
    // }

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
            return JSONUtil.toBean(sb.toString(), Map.class);
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
    public static List<Map> getListByUrlGet(String url) {
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
