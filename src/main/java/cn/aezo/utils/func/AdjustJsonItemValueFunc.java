package cn.aezo.utils.func;

/**
 * @author smalle
 * @since 2021-06-19
 */
@FunctionalInterface
public interface AdjustJsonItemValueFunc {
    Object adjustValue(Object value, String key);
}
