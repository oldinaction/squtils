package cn.aezo.utils.func;

import java.util.Map;

/**
 * 一般用于提取http请求返回中的主要数据，如果没提取到可返回null, 以此判断是否调用成功
 * @author smalle
 * @since 2021/12/27
 */
@FunctionalInterface
public interface RespExtractDataFunc {
    Object extract(Map<String, ? extends Object> resp);
}
