package cn.aezo.utils.base;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * @author smalle
 * @since 2020-12-10 21:04
 */
public class ReflectU extends ReflectUtil {

    /**
     * 仅获取当前类的方法(包括继承的直接父类方法)
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

    /**
     * 获取对象地址
     * @author smalle
     * @since 2021/1/23
     * @param objects
     * @return java.lang.String
     */
    public static String getAddresses(Object... objects) {
        StringBuffer sb = new StringBuffer();
        sb.append("0x");
        // sun.arch.data.model=32 // 32 bit JVM
        // sun.arch.data.model=64 // 64 bit JVM
        boolean is64bit = Integer.parseInt(System.getProperty("sun.arch.data.model")) == 32 ? false : true;
        Unsafe unsafe = getUnsafe();
        long last = 0;
        int offset = unsafe.arrayBaseOffset(objects.getClass());
        int scale = unsafe.arrayIndexScale(objects.getClass());
        switch (scale) {
            case 4:
                long factor = is64bit ? 8 : 1;
                final long i1 = (unsafe.getInt(objects, offset) & 0xFFFFFFFFL) * factor;
                // 输出指针地址
                sb.append(Long.toHexString(i1));
                last = i1;
                for (int i = 1; i < objects.length; i++)
                {
                    final long i2 = (unsafe.getInt(objects, offset + i * 4) & 0xFFFFFFFFL) * factor;
                    if (i2 > last) {
                        sb.append(", +" + Long.toHexString(i2 - last));
                    } else {
                        sb.append(", -" + Long.toHexString(last - i2));
                    }
                    last = i2;
                }
                break;
            case 8:
                throw new AssertionError("Not supported");
            default:
                throw new AssertionError("Not supported...");
        }
        return sb.toString();
    }

    private static Unsafe getUnsafe() {
        try {
            Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
            theUnsafe.setAccessible(true);
            return (Unsafe) theUnsafe.get(null);
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }
}
