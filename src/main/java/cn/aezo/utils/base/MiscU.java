package cn.aezo.utils.base;

import cn.hutool.core.collection.CollUtil;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * 集合操作工具类
 * @author smalle
 * @since 2017/2/3
 */
public class MiscU {

	public static void sort(List<? extends Comparable> list) {
		Collections.sort(list);
	}

	public static void sortToDesc(List<? extends Comparable> list) {
		Collections.sort(list);
		Collections.reverse(list);
	}

	// ==============
	// 操作List
	// ==============

	/**
	 * 将 List(存放的Map) 按照其中map的某两个字段(keyName: valueName)提取成一个map
	 * @param list
	 * @param keyName
	 * @param valueName
	 * @return
	 */
	public static Map<String, Object> extractMap(List<Object> list, Object keyName, Object valueName) {
		Map retMap = new HashMap();

		for(int i=0, n=list.size(); i<n; i++){
			Object bean = list.get(i);
			Map map = BeanU.transBean2Map(bean);
			retMap.put(map.get(keyName), map.get(valueName));
		}

		return retMap;
	}

	/**
	 * 将 List(存放的Map) 按照其中map的所有key字段和所有的value字段(keyNames : valueNames)提取成一个map
	 * @param list
	 * @param keyNames
	 * @param valueNames
	 * @return Map<String, String>
	 * @throws Exception
	 */
	public static Map<String, Object> extractMap(List<Object> list, List keyNames, List valueNames) {
		Map<String, Object> retMap = new HashMap<String, Object>();

		for(int i=0, n=list.size(); i<n; i++) {
			Object bean = list.get(i);
			Map<String, Object> map = BeanU.transBean2Map(bean);

			String keyStr = "";
			for (Object keyName : keyNames) {
				Object o = map.get(keyName);
				keyStr += String.valueOf(o);
			}

			String valueStr = "";
			for (Object valueName : valueNames) {
				Object o = map.get(valueName);
				valueStr += String.valueOf(o);
			}

			retMap.put(keyStr, valueStr);
		}

		return retMap;
	}

	/**
	 * 将 List(存放的Map) 按照其中map的某两个字段(keyName:valueName)提取成一个map (无依赖)
	 * @param list
	 * @param keyName
	 * @param valueName
	 * @return
	 */
	@Deprecated
	public static Map<String, Object> extractMap2(List<? extends Map<String, Object>> list, String keyName, String valueName) {
		Map<String, Object> retMap = new HashMap<>();
		for (Map map : list) {
			if(map.get(keyName) != null) {
				String key = String.valueOf(map.get(keyName));
				retMap.put(key, map.get(valueName));
			}
		}

		return retMap;
	}

	/**
	 * 从List(Bean)中将某个字段的值放到一个新list中
	 * @param beanList
	 * @param filedName
	 * @param classes
	 * @param containNull 默认不包含空/""
	 * @return
	 */
	public static List extractList(List<? extends Object> beanList, String filedName, Class classes, Boolean containNull) {
		List results = new ArrayList();

		Method method = null;
		Method[] methods = classes.getMethods();
		for (Method m : methods) {
			if(("get" + StrU.upperFirst(filedName)).equals(m.getName())) {
				method = m;
				break;
			}
		}

		if(method != null) {
			for (Object object : beanList) {
				Object retObj = null;
				try {
					retObj = method.invoke(object);
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				}

				if(containNull != null && containNull) {
					results.add(retObj);
				} else {
					if(retObj != null && !(retObj instanceof String)) {
						results.add(retObj);
					} else if(retObj != null && !"".equals(retObj)) {
						results.add(retObj);
					}
				}
			}
		}

		return results;
	}

	/**
	 * 将List按元素的某个Key转换成Map, Key一般是唯一值
	 * @author smalle
	 * @since 2021/7/6
	 * @param iterable 支持元素为Map或Bean
	 * @param fieldName
	 * @throws
	 * @return java.util.Map<java.lang.String, V> 会自动将元素的Key转成字符串
	 */
	public static <V> Map<String, V> fieldValueMapSimple(Iterable<V> iterable, String fieldName) {
		Map<String, V> retMap = new HashMap<>();
		boolean yesMap = false;
		Iterator<V> iterator = iterable.iterator();
		while (iterator.hasNext()) {
			V next = iterator.next();
			if(next instanceof Map) {
				yesMap = true;
				Object key = ((Map) next).get(fieldName);
				retMap.put(key == null ? "" : key.toString(), next);
			}
		}
		if(yesMap) {
			return retMap;
		} else {
			Map<Object, V> objectVMap = CollUtil.fieldValueMap(iterable, fieldName);
			for (Map.Entry<Object, V> entry : objectVMap.entrySet()) {
				retMap.put(entry.getKey().toString(), entry.getValue());
			}
			return retMap;
		}
	}

	/**
	 * 将List按元素的某个Key转换成Map, Key一般是唯一值
	 * @author smalle
	 * @since 2021/7/6
	 * @param iterable 支持元素为Map或Bean
	 * @param fieldName
	 * @throws
	 * @return java.util.Map<java.lang.String, V> 会将元素的Key的元素类型值作为Map的Key
	 */
	public static <K, V> Map<K, V> fieldValueMap(Iterable<V> iterable, String fieldName) {
		Map<K, V> retMap = new HashMap<>();
		boolean yesMap = false;
		Iterator<V> iterator = iterable.iterator();
		while (iterator.hasNext()) {
			V next = iterator.next();
			if(next instanceof Map) {
				yesMap = true;
				retMap.put((K) ((Map) next).get(fieldName), next);
			}
		}
		if(yesMap) {
			return retMap;
		} else {
			return CollUtil.fieldValueMap(iterable, fieldName);
		}
	}

	/**
	 * 将 List(存放的Map) 按照map的某个字段(key)的值分组
	 * @param dataList
	 * @param key 分组字段。可以传入Long型等，但是返回的Map.key为字符串
	 * @return
	 * @author smalle
	 * @date 2016年11月26日 下午8:27:37
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static Map<String, List> groupByMapKey(List dataList, Object key) {
		Map<String, List> resultMap = new HashMap<>();
		for (Map map : (List<Map>) dataList) {
			String keyStr = String.valueOf(map.get(key));
			if(resultMap.containsKey(keyStr)) {
				resultMap.get(keyStr).add(map);
			} else {
				resultMap.put(keyStr, toList(map));
			}
		}

		return resultMap;
	}

	/**
	 * 将 List(存放的Map) 按照map的某几个字段(key)的值分组
	 * @param dataList
	 * @param joinStr 关联字符，默认为$
	 * @param keys 分组字段。可以传入Long型等，但是返回的Map.key为字符串
	 * @return
	 * @author smalle
	 * @date 2016年11月26日 下午8:27:37
	 */
	public static Map<String, List> groupByMapKeys(List dataList, String joinStr, Object... keys) {
		Map<String, List> resultMap = new HashMap<String, List>();
		if(joinStr == null) {
			joinStr = "$";
		}
		for (Map map : (List<Map>) dataList) {
			String keyStr = "";
			for (Object key : keys) {
				keyStr += joinStr + String.valueOf(map.get(key));
			}
			if(ValidU.isNotEmpty(keyStr)) {
				keyStr = keyStr.substring(1);
			}
			if(resultMap.containsKey(keyStr)){
				resultMap.get(keyStr).add(map);
			} else {
				resultMap.put(keyStr, toList(map));
			}
		}

		return resultMap;
	}

	/**
	 * 将 List(存放的Bean) 按照bean的某个字段(filed)的值分组. (使用反射)
	 * @param beanList
	 * @param filedName
	 * @param classes
	 * @return
	 */
	public static Map<Object, List> groupByBeanKey(List<? extends Object> beanList, String filedName, Class classes) {
		HashMap<Object, List> resultMap = new HashMap();

		Method method = null;
		Method[] methods = classes.getMethods();
		for (Method m : methods) {
			if(("get" + StrU.upperFirst(filedName)).equals(m.getName())) {
				method = m;
				break;
			}
		}

		if(method != null) {
			for (Object object : beanList) {
				Object retObj = null;
				try {
					retObj = method.invoke(object);
				} catch (IllegalAccessException | InvocationTargetException e) {
					e.printStackTrace();
				}

				if(resultMap.containsKey(retObj)) {
					(resultMap.get(retObj)).add(object);
				} else {
					ArrayList list = new ArrayList();
					list.add(object);
					resultMap.put(retObj, list);
				}
			}
		}

		return resultMap;
	}

	/**
	 * 计算集合的单差集，即只返回【集合1】中有，但是【集合2】中没有的元素，例如：
	 *
	 * <pre>
	 *     subtract([1,2,3,4],[2,3,4,5]) -》 [1]
	 * </pre>
	 *
	 * @param coll1 集合1，支持NULL
	 * @param coll2 集合2，支持NULL
	 * @param <T>   元素类型
	 * @return 单差集
	 */
    public static <T> Collection<T> subtract(Collection<T> coll1, Collection<T> coll2) {
        if(ValidU.isEmpty(coll1)) {
            return new ArrayList<>();
        }
        if(ValidU.isEmpty(coll2)) {
            return coll1;
        }
        // CollUtil.subtractToList 如果参数2位空集合，则返回空集合
        return CollUtil.subtract(coll1, coll2);
    }

	// ==============
	// 操作Map
	// ==============

	/**
	 * 通过有效的key过滤map
	 * @param map
	 * @param keySet
	 * @return
	 * @author smalle
	 * @date 2016年11月15日 下午12:24:36
	 */
	public static <K, V, E> Map mapFilter(Map<K, V> map, Set<E> keySet) {
		Map result = new HashMap(keySet.size());
		for (K k : map.keySet()) {
			if(keySet.contains(k)) {
				result.put(k, (V) map.get(k));
			}
		}
		return result;
	}

	/**
	 * 获取map中所有的值
	 * @param map
	 * @return
	 */
	public static List<Object> mapValueList(Map<String, Object> map) {
		List<Object> valueList = new ArrayList<>();
		for (Map.Entry<String, Object> entry : map.entrySet()) {
			valueList.add(entry.getValue());
		}
		return valueList;
	}

	/**
	 * 移除map中的null(含"")值
	 * @param map
	 * @param <K>
	 * @param <V>
	 * @return
	 */
	public static <K, V> Map<K, V> mapRemoveNullValue(Map<K, V> map) {
		Map<K, V> retMap = new HashMap<K, V>();
		for (Map.Entry<K, V> entry : map.entrySet()) {
			if(entry.getValue() != null && !"".equals(entry.getValue())) {
				retMap.putAll(map);
			}
		}
		return retMap;
	}

	/**
	 * 求两个map的并集(后面的会覆盖前面的)
	 * @param maps
	 * @return
	 */
	public static <K, V> Map<K, V> mapUnicon(Map<K, V>... maps) {
		Map<K, V> retMap = new HashMap<>();
		for (Map<K, V> map : maps) {
			retMap.putAll(map);
		}
		return retMap;
	}

	/**
	 * 根据字段名求Map集合的交集(需要 V 实现了equals方法)
	 * @param map1
	 * @param map2
	 * @return
	 */
	public final static <K, V> Map<K, V> mapIntersection(Map<K, V> map1, Map<K, V> map2) {
		Map<K, V> map = new HashMap<>();
		if (ValidU.isAllNotEmpty(map1, map2)) {
			Set<K> key1 = new HashSet<>(map1.keySet());
			Set<K> key2 = new HashSet<>(map2.keySet());
			key1.retainAll(key2);
			for (K k : key1) {
				if(map2.containsValue(map1.get(k))) {
					map.put(k, map1.get(k));
				}
			}
		}
		return map;
	}

	/**
	 * 根据字段名求两个map的差集 map1-map2 = map1`
	 * @param map1
	 * @param map2
	 * @return
	 */
	public final static <K, V> Map<K, V> mapSubtractByKey(Map<K, V> map1, Map<K, V> map2) {
		Map<K, V> map = new HashMap<>();
		if (map1 != null && map2 != null) {
			Set<K> key1 = new HashSet<>(map1.keySet());
			Set<K> key2 = new HashSet<>(map2.keySet());
			for (K k : key2) {
				key1.remove(k);
			}
			for (K k : key1) {
				map.put(k, map1.get(k));
			}
		} else if (map1 != null) {
			map.putAll(map1);
		}
		return map;
	}

	/**
	 * 根据字段值求差集 map1-map2 = map1`
	 * @param map1
	 * @param map2
	 * @return
	 */
	public final static <K, V> Map<K, V> mapSubtractByValue(Map<K, V> map1, Map<K, V> map2) {
		Map<K, V> map = new HashMap<>();
		if (map1 != null && map2 != null) {
			Set<K> setkey1 = new HashSet<>(map1.keySet());
			Set<K> setkey2 = new HashSet<>(map2.keySet());
			for (K k : setkey2) {
				if(("" + map1.get(k)).equals("" + map2.get(k))) {
					setkey1.remove(k);
				}
			}
			for (K k : setkey1) {
				map.put(k, map1.get(k));
			}
		} else if (map1 != null) {
			map.putAll(map1);
		}
		return map;
	}

	/**
	 * 根据map中values获取keys
	 * @param map
	 * @param values
	 * @return
	 */
	public static <T> List<T> mapGetKeysByValues(Map<T, Object> map, Object... values) {
		if(map == null) {
			return null;
		}

		List<T> retList = new ArrayList<>();
		for (Map.Entry<T, Object> entry : map.entrySet()) {
			T key = entry.getKey();
			Object valueItem = entry.getValue();
			for (Object o : values) {
				if(o != null && o.equals(valueItem)) {
					retList.add(key);
				}
			}
		}

		return retList;
	}

	/**
	 * 对map中的字符串去空格并转大写
	 * @param map
	 * @param keySet 需要去重的key(null表示全部执行)
	 * @return
	 */
	public final static <K, V> Map mapValueTrimAndUpper(Map<K, V> map, Set<String> keySet) {
		for (K k : map.keySet()) {
			if(keySet == null || keySet.size() == 0 || keySet.contains(k)) {
				V value = map.get(k);
				if(value != null && value instanceof String) {
					map.put(k, (V) ((String) value).trim().toUpperCase());
				}
			}
		}

		return map;
	}

	/**
	 * 对map中的字符串去空格并转大写(忽略某些key)
	 * @param map
	 * @param ignoreKeySet 忽略的key(为空则都不忽略)
	 * @return
	 */
	public final static <K, V> Map mapValueTrimAndUpperWithIgnoreKeys(Map<K, V> map, Set<String> ignoreKeySet) {
		for (K k : map.keySet()) {
			if(ignoreKeySet != null && ignoreKeySet.contains(k)) {
				continue;
			} else {
				V value = map.get(k);
				if(value != null && value instanceof String) {
					map.put(k, (V) ((String) value).trim().toUpperCase());
				}
			}
		}

		return map;
	}


	// ==============
	// 快速组装实例
	// ==============
	/**
	 * Create a map from passed nameX, valueX parameters
	 * @return The resulting Map
	 */
	public static <V, V1 extends V> Map<String, V> toMap(String name1, V1 value1) {
		return populateMap(new HashMap<String, V>(), name1, value1);
	}

	/**
	 * Create a map from passed nameX, valueX parameters
	 * @return The resulting Map
	 */
	public static <V, V1 extends V, V2 extends V> Map<String, V> toMap(String name1, V1 value1, String name2, V2 value2) {
		return populateMap(new HashMap<String, V>(), name1, value1, name2, value2);
	}

	/**
	 * Create a map from passed nameX, valueX parameters
	 * @return The resulting Map
	 */
	public static <V, V1 extends V, V2 extends V, V3 extends V> Map<String, V> toMap(String name1, V1 value1, String name2, V2 value2, String name3, V3 value3) {
		return populateMap(new HashMap<String, V>(), name1, value1, name2, value2, name3, value3);
	}

	/**
	 * Create a map from passed nameX, valueX parameters
	 * @return The resulting Map
	 */
	public static <V, V1 extends V, V2 extends V, V3 extends V, V4 extends V> Map<String, V> toMap(String name1, V1 value1, String name2, V2 value2, String name3, V3 value3, String name4, V4 value4) {
		return populateMap(new HashMap<String, V>(), name1, value1, name2, value2, name3, value3, name4, value4);
	}

	/**
	 * Create a map from passed nameX, valueX parameters
	 * @return The resulting Map
	 */
	public static <V, V1 extends V, V2 extends V, V3 extends V, V4 extends V, V5 extends V> Map<String, V> toMap(String name1, V1 value1, String name2, V2 value2, String name3, V3 value3, String name4, V4 value4, String name5, V5 value5) {
		return populateMap(new HashMap<String, V>(), name1, value1, name2, value2, name3, value3, name4, value4, name5, value5);
	}

	/**
	 * Create a map from passed nameX, valueX parameters
	 * @return The resulting Map
	 */
	public static <V, V1 extends V, V2 extends V, V3 extends V, V4 extends V, V5 extends V, V6 extends V> Map<String, V> toMap(String name1, V1 value1, String name2, V2 value2, String name3, V3 value3, String name4, V4 value4, String name5, V5 value5, String name6, V6 value6) {
		return populateMap(new HashMap<String, V>(), name1, value1, name2, value2, name3, value3, name4, value4, name5, value5, name6, value6);
	}

	/**
	 * Create a map from passed nameX, valueX parameters
	 * @return The resulting Map
	 */
	@SuppressWarnings("unchecked")
	public static <K, V> Map<String, V> toMapAll(Object... data) {
		if (data.length % 2 == 1) {
			IllegalArgumentException e = new IllegalArgumentException("You must pass an even sized array to the toMap method (size = " + data.length + ")");
			throw e;
		}
		Map<String, V> map = new HashMap<String, V>();
		for (int i = 0; i < data.length;) {
			map.put((String) data[i++], (V) data[i++]);
		}
		return map;
	}

	/**
	 * 往一个map中按照MiscU.toMap的方式加入数据
	 */
	@SuppressWarnings("unchecked")
	public static <K, V> Map<String, V> populateMap(Map<String, V> map, Object... data) {
		for (int i = 0; i < data.length;) {
			map.put((String) data[i++], (V) data[i++]);
		}
		return map;
	}

	/**
	 * Create a Set from passed objX parameters
	 * @return The resulting Set
	 */
	public static <T> Set<T> toSet(T obj1, T... s) {
		Set<T> theSet = new LinkedHashSet<T>();
		theSet.add(obj1);
		if(s != null) {
			theSet.addAll(toSetArray(s));
		}
		return theSet;
	}

	public static <T> Set<T> toSet(Collection<T> collection) {
		if (collection == null) {
			return null;
		}
		if (collection instanceof Set<?>) {
			return (Set<T>) collection;
		} else {
			Set<T> theSet = new LinkedHashSet<T>();
			theSet.addAll(collection);
			return theSet;
		}
	}

	public static <T> Set<T> toSetArray(T[] data) {
		if (data == null) {
			return null;
		}
		Set<T> set = new LinkedHashSet<T>();
		for (T value: data) {
			set.add(value);
		}
		return set;
	}

	/**
	 * Create a list from passed objX parameters
	 * @return The resulting List
	 */
	public static <T> List<T> toList(T obj1, T... t) {
		List<T> list = new LinkedList<T>();
		list.add(obj1);
		if (t != null) {
			list.addAll(toListArray(t));
		}
		return list;
	}

	public static <T> List<T> toList(Collection<T> collection) {
		if (collection == null) {
			return null;
		}
		if (collection instanceof List<?>) {
			return (List<T>) collection;
		} else {
			List<T> list = new LinkedList<T>();
			list.addAll(collection);
			return list;
		}
	}

	public static <T> List<T> toListArray(T[] data) {
		if (data == null) {
			return null;
		}
		List<T> list = new LinkedList<T>();
		for (T value: data) {
			list.add(value);
		}
		return list;
	}

}
