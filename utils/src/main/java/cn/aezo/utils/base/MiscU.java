package cn.aezo.utils.base;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static cn.aezo.utils.base.StringU.toUpperCaseFirst;

public final class MiscU {

	// ==============
	// 操作List
	// ==============

	/**
	 * 将 List(存放的Map) 按照其中map的某两个字段(keyName:valueName)提取成一个map
	 * @param list
	 * @param keyName
	 * @param valueName
	 * @return
	 */
	public static Map<String, Object> extractMap(List list, Object keyName, Object valueName) {
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
	 * @return
	 * @throws Exception
	 */
	public static Map<String, Object> extractMap(List list, List keyNames, List valueNames) {
		Map retMap = new HashMap();

		for(int i=0, n=list.size(); i<n; i++){
			Object bean = list.get(i);
			Map map = BeanU.transBean2Map(bean);

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
		Map<String, Object> retMap = new HashMap();
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
			if(("get" + toUpperCaseFirst(filedName)).equals(m.getName())) {
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
	 * 将 List(存放的Map) 按照map的某个字段(key)的值分组
	 * @param dataList
	 * @param key 分组字段
	 * @return
	 * @author smalle
	 * @date 2016年11月26日 下午8:27:37
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static <K> Map<K, List> listMapGroupBy(List dataList, K key) {
		Map<K, List> resultMap = new HashMap<K, List>();
		for (Map map : (List<Map>) dataList) {
			if(resultMap.containsKey(map.get(key))){
				resultMap.get(map.get(key)).add(map);
			} else {
				List<Map> list = new ArrayList<Map>();
				list.add(map);
				if(ValidU.isNotEmpty(map.get(key))) { // 业务上要求不为空
					resultMap.put((K) map.get(key), list);
				}
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
	public static Map<Object, List> listBeanGroupBy(List<? extends Object> beanList, String filedName, Class classes) {
		HashMap<Object, List> resultMap = new HashMap();

		Method method = null;
		Method[] methods = classes.getMethods();
		for (Method m : methods) {
			if(("get" + toUpperCaseFirst(filedName)).equals(m.getName())) {
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

				if(resultMap.containsKey(retObj)) {
					(resultMap.get(retObj)).add(object);
				} else {
					ArrayList list = new ArrayList();
					list.add(object);
					if(null != retObj) {
						resultMap.put(retObj, list);
					}
				}
			}
		}

		return resultMap;
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
	public static <K, V, E> Map filterMap(Map<K, V> map, Set<E> keySet) {
		Map result = new HashMap(keySet.size());
		for (K k : map.keySet()) {
			if(keySet.contains(k)) {
				result.put(k, (V) map.get(k));
			}
		}
		return result;
	}

	// ==============
	// 快速组装实例
	// ==============
	public static class Instance {
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
		public static <K, V> Map<String, V> toMap(Object... data) {
			if (data.length == 1 && data[0] instanceof Map) {
				// return UtilGenerics.<String, V>checkMap(data[0]); // TODO 校验
			}
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

		@SuppressWarnings("unchecked")
		private static <K, V> Map<String, V> populateMap(Map<String, V> map, Object... data) {
			for (int i = 0; i < data.length;) {
				map.put((String) data[i++], (V) data[i++]);
			}
			return map;
		}

		/**
		 * Create a Set from passed objX parameters
		 * @return The resulting Set
		 */
		public static <T> Set<T> toSet(T obj1) {
			Set<T> theSet = new LinkedHashSet<T>();
			theSet.add(obj1);
			return theSet;
		}

		/**
		 * Create a Set from passed objX parameters
		 * @return The resulting Set
		 */
		public static <T> Set<T> toSet(T obj1, T obj2) {
			Set<T> theSet = new LinkedHashSet<T>();
			theSet.add(obj1);
			theSet.add(obj2);
			return theSet;
		}

		/**
		 * Create a Set from passed objX parameters
		 * @return The resulting Set
		 */
		public static <T> Set<T> toSet(T obj1, T obj2, T obj3) {
			Set<T> theSet = new LinkedHashSet<T>();
			theSet.add(obj1);
			theSet.add(obj2);
			theSet.add(obj3);
			return theSet;
		}

		/**
		 * Create a Set from passed objX parameters
		 * @return The resulting Set
		 */
		public static <T> Set<T> toSet(T obj1, T obj2, T obj3, T obj4) {
			Set<T> theSet = new LinkedHashSet<T>();
			theSet.add(obj1);
			theSet.add(obj2);
			theSet.add(obj3);
			theSet.add(obj4);
			return theSet;
		}

		/**
		 * Create a Set from passed objX parameters
		 * @return The resulting Set
		 */
		public static <T> Set<T> toSet(T obj1, T obj2, T obj3, T obj4, T obj5) {
			Set<T> theSet = new LinkedHashSet<T>();
			theSet.add(obj1);
			theSet.add(obj2);
			theSet.add(obj3);
			theSet.add(obj4);
			theSet.add(obj5);
			return theSet;
		}

		/**
		 * Create a Set from passed objX parameters
		 * @return The resulting Set
		 */
		public static <T> Set<T> toSet(T obj1, T obj2, T obj3, T obj4, T obj5, T obj6) {
			Set<T> theSet = new LinkedHashSet<T>();
			theSet.add(obj1);
			theSet.add(obj2);
			theSet.add(obj3);
			theSet.add(obj4);
			theSet.add(obj5);
			theSet.add(obj6);
			return theSet;
		}

		public static <T> Set<T> toSet(T obj1, T obj2, T obj3, T obj4, T obj5, T obj6, T obj7, T obj8) {
			Set<T> theSet = new LinkedHashSet<T>();
			theSet.add(obj1);
			theSet.add(obj2);
			theSet.add(obj3);
			theSet.add(obj4);
			theSet.add(obj5);
			theSet.add(obj6);
			theSet.add(obj7);
			theSet.add(obj8);
			return theSet;
		}

		public static <T> Set<T> toSet(Collection<T> collection) {
			if (collection == null) return null;
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
		public static <T> List<T> toList(T obj1) {
			List<T> list = new LinkedList<T>();

			list.add(obj1);
			return list;
		}

		/**
		 * Create a list from passed objX parameters
		 * @return The resulting List
		 */
		public static <T> List<T> toList(T obj1, T obj2) {
			List<T> list = new LinkedList<T>();

			list.add(obj1);
			list.add(obj2);
			return list;
		}

		/**
		 * Create a list from passed objX parameters
		 * @return The resulting List
		 */
		public static <T> List<T> toList(T obj1, T obj2, T obj3) {
			List<T> list = new LinkedList<T>();

			list.add(obj1);
			list.add(obj2);
			list.add(obj3);
			return list;
		}

		/**
		 * Create a list from passed objX parameters
		 * @return The resulting List
		 */
		public static <T> List<T> toList(T obj1, T obj2, T obj3, T obj4) {
			List<T> list = new LinkedList<T>();

			list.add(obj1);
			list.add(obj2);
			list.add(obj3);
			list.add(obj4);
			return list;
		}

		/**
		 * Create a list from passed objX parameters
		 * @return The resulting List
		 */
		public static <T> List<T> toList(T obj1, T obj2, T obj3, T obj4, T obj5) {
			List<T> list = new LinkedList<T>();

			list.add(obj1);
			list.add(obj2);
			list.add(obj3);
			list.add(obj4);
			list.add(obj5);
			return list;
		}

		/**
		 * Create a list from passed objX parameters
		 * @return The resulting List
		 */
		public static <T> List<T> toList(T obj1, T obj2, T obj3, T obj4, T obj5, T obj6) {
			List<T> list = new LinkedList<T>();

			list.add(obj1);
			list.add(obj2);
			list.add(obj3);
			list.add(obj4);
			list.add(obj5);
			list.add(obj6);
			return list;
		}

		public static <T> List<T> toList(T obj1, T obj2, T obj3, T obj4, T obj5, T obj6, T obj7, T obj8, T obj9) {
			List<T> list = new LinkedList<T>();

			list.add(obj1);
			list.add(obj2);
			list.add(obj3);
			list.add(obj4);
			list.add(obj5);
			list.add(obj6);
			list.add(obj7);
			list.add(obj8);
			list.add(obj9);
			return list;
		}

		public static <T> List<T> toList(Collection<T> collection) {
			if (collection == null) return null;
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


	
	/*
	public static void main(String[] args) {
		Map<String, Object> map = new HashMap<>();
		map.put("a", true);
		map.put("b", "2");
		map.put("c", 1);
		
		Set<String> set = new HashSet<>();
		set.add("a");
		set.add("c");
		
		System.out.println(MapU.filter(map, set));
	}
	*/

}
