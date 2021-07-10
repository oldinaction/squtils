package cn.aezo.utils.base;

import cn.hutool.core.exceptions.UtilException;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * @author smalle
 * @since 2020-12-10 21:04
 */
public class ReflectU extends ReflectUtil {
    private static final int MAX_CAUSE = 10;

    /**
     * 解决反射获取不到最终异常
     * @author smalle
     * @since 2021/3/31
     * @param object
     * @param method
     * @param args
     * @throws ExceptionU
     * @return T
     */
    public static <T> T invoke(Object object, Method method, Object... args) throws ExceptionU {
        try {
            return ReflectUtil.invoke(object, method, args);
        } catch (Throwable e) {
            for (int i = 0; i < MAX_CAUSE; i++) {
                if(e.getCause() != null) {
                    e = e.getCause();
                } else {
                    break;
                }
            }
            if(e instanceof ExceptionU) {
                throw (ExceptionU) e;
            } else {
                throw new ExceptionU(e.getMessage(), e);
            }
        }
    }

    public static <T> T invoke(Object object, String methodName, Object... args) throws ExceptionU {
        final Method method = getMethodOfObj(object, methodName, args);
        if (null == method) {
            throw new UtilException(StrUtil.format("No such method: [{}]", methodName));
        }
        return invoke(object, method, args);
    }

    /**
     * 调用类的静态方法<br/>
     * 注意使用 ReflectU.getMethodWithArgs 不能非常准确能获取Method
     * @author smalle
     * @since 2021/7/9
     * @param clazz
     * @param methodName
     * @param args
     * @throws
     * @return T
     */
    public static  <T> T invokeStaticMethod(Class<?> clazz, String methodName, Object... args) {
        Method publicMethod = ReflectU.getMethodWithArgs(clazz, methodName, args);
        return ReflectU.invokeStatic(publicMethod, args);
    }

    public static Method getMethodWithArgs(Class<?> clazz, String methodName, Object... args) {
        return getMethodWithArgs(clazz, methodName, null, null, null, args);
    }

    /**
     * 基于参数值，反射获取获取类方法<br/>
     * 1.不能非常准确能获取Method，基于类型可准确获取方法(如：ReflectUtil.getPublicMethod)<br/>
     * 2.ClassUtil.getClasses(args)可根据参数值获取类型，但是如参数值为NULL，则返回的类型为Object，如果对应参数类型为Map等，则无法准确获取<br/>
     * @author smalle
     * @since 2021/7/10
     * @param clazz
     * @param methodName
     * @param yesPublic
     * @param yesStatic
     * @param returnClazz
     * @param args
     * @throws
     * @return java.lang.reflect.Method
     */
    public static Method getMethodWithArgs(Class<?> clazz, String methodName, Boolean yesPublic, Boolean yesStatic,
                                           Class<?> returnClazz, Object... args) {
        Method[] methods = ReflectU.getMethods(clazz, item -> {
            boolean matchName = item.getName().equals(methodName);
            if (!matchName) {
                return false;
            }

            Class<?> returnType = item.getReturnType();
            if(returnClazz != null && !returnClazz.isAssignableFrom(returnType)) {
                return false;
            }

            int modifiers = item.getModifiers();
            if(yesPublic != null && yesPublic && !Modifier.isPublic(modifiers)) {
                return false;
            }
            if(yesStatic != null && yesStatic && !Modifier.isStatic(modifiers)) {
                return false;
            }

            Class<?>[] parameterTypes = item.getParameterTypes();
            if (args == null) {
                return parameterTypes.length != 0;
            }
            if (parameterTypes.length != args.length) {
                return false;
            }
            for (int i = 0; i < args.length; i++) {
                Class<?> parameterType = parameterTypes[i];
                Object arg = args[i];
                if (arg != null) {
                    Class<?> aClass = arg.getClass();
                    if (!parameterType.isAssignableFrom(aClass)) {
                        return false;
                    }
                } else {
                    boolean flag = MiscU.toList(long.class, double.class, float.class, boolean.class,
                            char.class, byte.class, short.class, int.class).contains(parameterType);
                    if (flag) {
                        return false;
                    }
                }
            }

            return true;
        });

        if(methods.length == 0) {
            return null;
        } else if(methods.length == 1) {
            return methods[0];
        }
        throw new ExceptionU("存在多个相关服务方法");
    }

    /**
     * 仅获取当前类和直接继承父类的方法
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
