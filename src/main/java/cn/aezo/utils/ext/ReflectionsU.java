package cn.aezo.utils.ext;

import org.reflections.Reflections;

import java.lang.reflect.Modifier;
import java.util.Set;

public class ReflectionsU {
    /**
     * 获取某包下接口的实现类(不含抽象类)
     * @author smalle
     * @since 2022/10/11
     * @param packageName
     * @param clazz
     * @return java.util.Set<java.lang.Class < ? extends T>>
     */
    public static <T> Set<Class<? extends T>> getSubTypesIgnoreAbstract(String packageName, Class<T> clazz) {
        Set<Class<? extends T>> impList = getSubTypesOf(packageName, clazz);
        impList.removeIf(next -> Modifier.isAbstract(next.getModifiers()));
        return impList;
    }

    /**
     * 获取某包下接口的实现类(含抽象类)
     * @author smalle
     * @since 2022/10/11
     * @param packageName
     * @param clazz
     * @return java.util.Set<java.lang.Class < ? extends T>>
     */
    public static <T> Set<Class<? extends T>> getSubTypesOf(String packageName, Class<T> clazz) {
        Reflections reflections = new Reflections(packageName);
        // 获取在指定包扫描的目录所有的实现类
        Set<Class<? extends T>> impList = reflections.getSubTypesOf(clazz);
        return impList;
    }
}
