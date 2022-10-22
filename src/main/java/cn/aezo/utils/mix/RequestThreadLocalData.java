package cn.aezo.utils.mix;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author smalle
 * @since 2021-06-19
 */
public class RequestThreadLocalData {
    private static final ThreadLocal<Map<String, Object>> CONTEXT_THREAD_LOCAL = new ThreadLocal<>();

    public static void init() {
        CONTEXT_THREAD_LOCAL.set(new LinkedHashMap<>());
    }

    public static void put(String key, Object val) {
        Map<String, Object> context = CONTEXT_THREAD_LOCAL.get();
        if(context == null) {
            return;
        }
        context.put(key, val);
    }

    public static Object get(String key) {
        Map<String, Object> context = CONTEXT_THREAD_LOCAL.get();
        if(context == null) {
            return null;
        }
        return context.get(key);
    }

    public static Map<String, Object> get() {
        return CONTEXT_THREAD_LOCAL.get();
    }

    public static void remove() {
        CONTEXT_THREAD_LOCAL.remove();
    }

}
