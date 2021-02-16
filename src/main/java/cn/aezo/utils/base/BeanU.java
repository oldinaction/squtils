package cn.aezo.utils.base;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.PropDesc;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ReflectUtil;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Created by smalle on 2017/1/15.
 */
public class BeanU extends BeanUtil {

    /**
     * 仅复制部分字段
     * @author smalle
     * @since 2020/12/22
     * @param source
     * @param target
     * @param copyProperties
     * @return void
     */
    public static void copyPropertiesOnly(Object source, Object target, String... copyProperties) {
        if(ValidU.isEmpty(copyProperties)) {
            return;
        }
        final HashSet<String> ignoreSet = new HashSet<>();
        List<String> copyKeyList = Convert.toList(String.class, copyProperties);
        if(source instanceof Map) {
            Set keys = ((Map) source).keySet();
            for (Object key : keys) {
                if(key instanceof String && !copyKeyList.contains(key)) {
                    ignoreSet.add((String) key);
                }
            }
        } else {
            BeanUtil.descForEach(source.getClass(), new Consumer<PropDesc>() {
                @Override
                public void accept(PropDesc prop) {
                    String fieldName = prop.getFieldName();
                    if (!CollUtil.contains(copyKeyList, fieldName)) {
                        ignoreSet.add(fieldName);
                    }
                }
            });
        }

        BeanUtil.copyProperties(source, target, Convert.toStrArray(ignoreSet));
    }

    /**
     * 忽略NULL类型字段
     * @author smalle
     * @since 2020/12/22
     * @param src
     * @param target
     * @param ignoreProperties
     */
    public static void copyPropertiesIgnoreNull(Object src, Object target, String... ignoreProperties) {
        BeanUtil.copyProperties(src, target, CopyOptions.create().ignoreNullValue().setIgnoreProperties(ignoreProperties));
    }

    /**
     * 忽略NULL类型字段
     * @author smalle
     * @since 2020/12/22
     * @param src
     * @param tClass
     * @param ignoreProperties
     */
    public static <T> T copyPropertiesIgnoreNull(Object src, Class<T> tClass, String... ignoreProperties) {
        T target = ReflectUtil.newInstanceIfPossible(tClass);
        copyProperties(src, target, CopyOptions.create().ignoreNullValue().setIgnoreProperties(ignoreProperties));
        return target;
    }

    /**
     * 获取对象中值为NULL的字段
     * @author smalle
     * @since 2021/1/9
     * @param source
     * @return java.lang.String[]
     */
    public static String[] getNullPropertyNames(Object source) {
        final BeanWrapper src = new BeanWrapperImpl(source);
        java.beans.PropertyDescriptor[] pds = src.getPropertyDescriptors();

        Set<String> emptyNames = new HashSet<>();
        for(java.beans.PropertyDescriptor pd : pds) {
            Object srcValue = src.getPropertyValue(pd.getName());
            if(srcValue == null) {
                emptyNames.add(pd.getName());
            }
        }
        String[] result = new String[emptyNames.size()];
        return emptyNames.toArray(result);
    }

    /**
     * 将一个map对象转化为bean
     * <br/>(1) 利用Introspector和PropertyDescriptor
     * <br/>(2) <i>类似于org.apache.commons.beanutils.BeanUtils.populate(bean, map)</i>
     * @param map
     * @param obj
     */
    public static void transMap2Bean(Map map, Object obj) {
        try {
            BeanInfo beanInfo = Introspector.getBeanInfo(obj.getClass());
            PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
            for (PropertyDescriptor property : propertyDescriptors) {
                Object key = property.getName();
                if (map.containsKey(key)) {
                    Object value = map.get(key);
                    // 得到property对应的setter方法
                    Method setter = property.getWriteMethod();
                    setter.invoke(obj, value);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 将Bean转成Map
     * <br/>(1) 利用Introspector和PropertyDescriptor
     * <br/>(2) <i>类似于org.apache.commons.beanutils.BeanUtils.describe(bean) 此方法的缺点为返回类型为Map<String, String>. 如果bean中还有另外一个bean则返回了该bean的引用</i>
     * @param obj
     * @return
     */
    public static Map<String, Object> transBean2Map(Object obj) {
        if (obj == null) {
            return null;
        }
        Map<String, Object> map = new HashMap<String, Object>();
        try {
            BeanInfo beanInfo = Introspector.getBeanInfo(obj.getClass());
            PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
            for (PropertyDescriptor property : propertyDescriptors) {
                String key = property.getName();
                // 过滤class属性
                if (!key.equals("class")) {
                    // 得到property对应的getter方法
                    Method getter = property.getReadMethod();
                    Object value = getter.invoke(obj);
                    map.put(key, value);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }

    /**
     * 将 List<Map>对象转化为List<JavaBean>
     * @param listMap
     * @param T
     * @param <T>
     * @return
     * @throws Exception
     */
    public static <T> List<T> transListMap2ListBean(List<Map<String, Object>> listMap, Class T) throws Exception {
        List<T> beanList = new ArrayList<T>();
        for(int i=0, n = listMap.size(); i<n; i++){
            Map<String, Object> map = listMap.get(i);
            T bean = (T) BeanUtil.toBean(map, T);
            beanList.add(bean);
        }
        return beanList;
    }

    /**
     * 将 List<JavaBean>对象转化为List<Map>
     * @param beanList
     * @return
     * @throws Exception
     */
    public static List<Map<String,Object>> transListBean2ListMap(List<Object> beanList) throws Exception {
        List<Map<String,Object>> mapList = new ArrayList<Map<String,Object>>();
        for(int i=0, n=beanList.size(); i<n; i++){
            Object bean = beanList.get(i);
            Map map = transBean2Map(bean);
            mapList.add(map);
        }
        return mapList;
    }

    /**
     * 通过反射, 获得定义Class时声明的父类的范型参数的类型. 如 public BookManager extends GenericManager<Book>
     *
     * @param clazz The class to introspect
     * @return the first generic declaration, or <code>Object.class</code> if cannot be determined
     */
    public static Class getSuperClassGenericType(Class clazz) {
        return getSuperClassGenericType(clazz, 0);
    }

    /**
     * 通过反射, 获得定义Class时声明的父类的范型参数的类型. 如 public BookManager extends GenericManager<Book>
     *
     * @param clazz clazz The class to introspect
     * @param index the Index of the generic declaration, start from 0.
     */
    public static Class getSuperClassGenericType(Class clazz, int index)
            throws IndexOutOfBoundsException {
        Type genType = clazz.getGenericSuperclass();
        if (!(genType instanceof ParameterizedType)) {
            return Object.class;
        }
        Type[] params = ((ParameterizedType) genType).getActualTypeArguments();
        if (index >= params.length || index < 0) {
            return Object.class;
        }
        if (!(params[index] instanceof Class)) {
            return Object.class;
        }
        return (Class) params[index];
    }
}
