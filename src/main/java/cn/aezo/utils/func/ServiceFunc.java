package cn.aezo.utils.func;

import java.util.Map;

@FunctionalInterface
public interface ServiceFunc<T> {
    T service(Map<String, ? extends Object> cxt);
}
