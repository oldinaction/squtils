package cn.aezo.utils.base;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;

import java.lang.reflect.Method;

/**
 * @author smalle
 * @since 2020-12-10 21:04
 */
public class ReflectU {

    /**
     * 仅获取当前类的方法
     * @author smalle
     * @since 2020/12/10 21:07 
     * @param clazz
     * @param methodName
     * @param ignoreCase
     * @param paramTypes
     * @return java.lang.reflect.Method
     */
    public static Method getMethodDirectly(Class<?> clazz, String methodName, boolean ignoreCase, Class<?>... paramTypes) {
        Method[] methods = ReflectUtil.getMethodsDirectly(clazz, false);
        if (ArrayUtil.isNotEmpty(methods)) {
            for (Method method : methods) {
                if (StrUtil.equals(methodName, method.getName(), ignoreCase)) {
                    if (ClassUtil.isAllAssignableFrom(method.getParameterTypes(), paramTypes)) {
                        return method;
                    }
                }
            }
        }
        return null;
    }
}
