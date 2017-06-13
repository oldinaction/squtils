package cn.aezo.utils.base.star;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by smalle on 2017/6/7.
 */
public class ObjectType {
    public static final String LANG_PACKAGE = "java.lang.";
    public static final String SQL_PACKAGE = "java.sql.";

    /**
     * 引用数据类型
     */
    private static final Map<String, String> classAlias = new HashMap<String, String>();
    /**
     * 基础数据类型
     */
    private static final Map<String, Class<?>> primitives = new HashMap<String, Class<?>>();

    static {
        classAlias.put("Object", "java.lang.Object");
        classAlias.put("String", "java.lang.String");
        classAlias.put("Boolean", "java.lang.Boolean");
        classAlias.put("BigDecimal", "java.math.BigDecimal");
        classAlias.put("Double", "java.lang.Double");
        classAlias.put("Float", "java.lang.Float");
        classAlias.put("Long", "java.lang.Long");
        classAlias.put("Integer", "java.lang.Integer");
        classAlias.put("Short", "java.lang.Short");
        classAlias.put("Byte", "java.lang.Byte");
        classAlias.put("Character", "java.lang.Character");
        classAlias.put("Timestamp", "java.sql.Timestamp");
        classAlias.put("Time", "java.sql.Time");
        classAlias.put("Date", "java.sql.Date");
        classAlias.put("Locale", "java.util.Locale");
        classAlias.put("Collection", "java.util.Collection");
        classAlias.put("List", "java.util.List");
        classAlias.put("Set", "java.util.Set");
        classAlias.put("Map", "java.util.Map");
        classAlias.put("HashMap", "java.util.HashMap");
        classAlias.put("TimeZone", "java.util.TimeZone");
        primitives.put("boolean", Boolean.TYPE);
        primitives.put("short", Short.TYPE);
        primitives.put("int", Integer.TYPE);
        primitives.put("long", Long.TYPE);
        primitives.put("float", Float.TYPE);
        primitives.put("double", Double.TYPE);
        primitives.put("byte", Byte.TYPE);
        primitives.put("char", Character.TYPE);
    }
}
