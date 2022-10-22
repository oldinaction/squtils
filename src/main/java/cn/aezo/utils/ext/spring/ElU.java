package cn.aezo.utils.ext.spring;

import cn.aezo.utils.base.MiscU;
import cn.aezo.utils.base.ValidU;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.map.MapUtil;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.expression.MapAccessor;
import org.springframework.core.env.Environment;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.common.TemplateParserContext;
import org.springframework.expression.spel.standard.SpelExpression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

/**
 * 建议EL表达式解析<br/>
 * 可用于自定义注解+EL表达式取值的场景。使用如`@HasPermission("${report-table.roleKey.manager}")`<br/>
 * @author smalle
 * @since 2022/10/12
 */
public class ElU {

    private static final Map<String, String> SymbolTwinMap = MiscU.toMap("KEY", "{}");

    public static <T> String parseByFormat(String express, Map context) {
        return parseByFormat(express, context, null, SymbolTwinMap);
    }

    public static <T> String parseByFormat(String express, Map context, Class<T> clazzConfig) {
        return parseByFormat(express, context, clazzConfig, SymbolTwinMap);
    }

    public static <T> String parseByFormat(String express, Map context, Class<T> clazzConfig, Map<String, String> symbolTwinMap) {
        String retValue = express;
        LinkedHashMap<String, String> symbolStr = getSymbolStr(symbolTwinMap, express);
        for (Map.Entry<String, String> entry : symbolStr.entrySet()) {
            // {abc}
            String value = entry.getValue();
            String el = "$" + value;
            if(retValue.contains(el)) {
                Object parse = parse(el, context, clazzConfig);
                String parseVal = Convert.toStr(parse, "");
                retValue = express.replaceAll("\\$\\{" + value.substring(1, value.length() - 1) + "\\}", parseVal);
            }
        }
        return retValue;
    }

    public static Object parse(String express, Map context) {
        return parse(express, context, null);
    }

    /**
     * 简单EL表达式解析<br/>
     * 支持: ${exp>2}、${map.name}<br/>
     * 表达式不符合规则则返回表达式本身，如果解析值出错则返回null<br/>
     *
     * @param express el表达式
     * @param context el表达式动态参数
     * @param clazzConfig springboot java配置类，用于获取默认值
     * @return
     */
    public static <T> Object parse(String express, Map context, Class<T> clazzConfig) {
        if (ValidU.isBlank(express)) {
            return null;
        }
        if (context == null) {
            context = new HashMap();
        }
        //创建一个EL解析器
        ExpressionParser parser = new SpelExpressionParser();
        TemplateParserContext templateParserContext = new TemplateParserContext("${", "}");
        SpelExpression expression;
        try {
            expression = (SpelExpression) parser.parseExpression(express, templateParserContext);
        } catch (Exception e) {
            return express;
        }
        //设置动态参数
        StandardEvaluationContext standardEvaluationContext = new StandardEvaluationContext();
        MapAccessor propertyAccessor = new MapAccessor();
        standardEvaluationContext.setVariables(context);
        standardEvaluationContext.setPropertyAccessors(Arrays.asList(propertyAccessor));
        expression.setEvaluationContext(standardEvaluationContext);
        Object ret;
        try {
            ret = expression.getValue(context);
            return ret;
        } catch (Exception e) {
        }
        // yml获取 ${user.name} => user.name
        String expressionString = expression.getExpressionString();
        try {
            Environment env = SpringU.getBean(Environment.class);
            ret = env.getProperty(expressionString);
            if (ret == null && clazzConfig != null) {
                // 配置类默认值(java代码中直接定义的属性默认值)
                try {
                    T config = SpringU.getBean(clazzConfig);
                    ConfigurationProperties[] annotationsByType = clazzConfig.getAnnotationsByType(ConfigurationProperties.class);
                    if(ValidU.isNotEmpty(annotationsByType)) {
                        String prefix = annotationsByType[0].prefix();
                        expressionString = expressionString.replace(prefix + ".", "");
                        expression = (SpelExpression) parser.parseExpression(
                                templateParserContext.getExpressionPrefix() + expressionString + templateParserContext.getExpressionSuffix(),
                                templateParserContext);
                        ret = expression.getValue(config);
                        return ret;
                    }
                } catch (Exception e) {
                }
            }
            return ret;
        } catch (Exception e) {
        }
        return null;
    }

    /**
     * 提取出成对符号的内容
     * @param symbolTwinMap {"S1": "{}", "S2": "[]"}
     * @param text          "ABC{12[**]34}DEF"
     * @return 如 {"S2_1": "[**]", "S1_1": "{12[**]34}"}
     */
    public static LinkedHashMap<String, String> getSymbolStr(Map<String, String> symbolTwinMap, String text) {
        //定义左右括号关系
        Map<Character, Character> bracket = getSymbolBracket(symbolTwinMap);
        Map<String, String> reverseSymbolTwinMap = MapUtil.reverse(symbolTwinMap);
        //残缺的括号内容
        List<Object> bracketList = new LinkedList<>();
        //完整的括号内容
        LinkedHashMap<String, String> retMap = new LinkedHashMap<String, String>();
        int count = 1;
        for (int x = 0; x < text.length(); x++) {
            Character nowStr = text.charAt(x);
            if (bracket.containsValue(nowStr)) {
                //如果是左括号
                if (bracketList.size() > 0) {
                    //如果不是第一次左括号说明之前还有左括号如：（工信部网安〔2018〕105号） 有俩左括号
                    for (int i = 0; i < bracketList.size(); i++) {
                        StringBuilder sb = (StringBuilder) bracketList.get(i);
                        sb.append(nowStr);
                    }
                }
                StringBuilder sb = new StringBuilder();
                sb.append(nowStr);
                bracketList.add(sb);
            } else if (bracket.containsKey(nowStr)) {
                //是右括号
                for (int i = 0; i < bracketList.size(); i++) {
                    StringBuilder sb = (StringBuilder) bracketList.get(i);
                    //添加右括号
                    sb.append(nowStr);
                    String symbolTwin = isSymbolTwin(symbolTwinMap, sb.toString());
                    //判断当前文本是否符合成对符号
                    if (symbolTwin != null) {
                        //符合
                        retMap.put(reverseSymbolTwinMap.get(symbolTwin) + "_" + count++, sb.toString());
                        //删除已经成对的内容 确保不会出现多次
                        bracketList.remove(sb);
                    } else {

                    }
                }
            } else if (bracketList.size() > 0) {
                //已经有了左括号
                for (int i = 0; i < bracketList.size(); i++) {
                    StringBuilder sb = (StringBuilder) bracketList.get(i);
                    sb.append(nowStr);
                }
            }
        }
        return retMap;
    }

    /**
     * 括号是否成对出现
     * 利用栈的先进后出特性来判断字符串的符合是否成对出现
     * 出现一次左边的进栈，出现一次右边的出栈，栈最终为空即为成对出现
     *
     * @param symbolTwinMap {"S1": "{}"}
     * @param text          "ABC{123}DEF"
     * @return 如 {}
     */
    public static String isSymbolTwin(Map<String, String> symbolTwinMap, String text) {
        //定义左右括号关系
        Map<Character, Character> bracket = getSymbolBracket(symbolTwinMap);
        String symbol = null;
        Stack stack = new Stack<>();
        for (int i = 0; i < text.length(); i++) {
            //转换成字符串
            Character nowStr = text.charAt(i);
            //是否为左括号
            if (bracket.containsValue(nowStr)) {
                stack.push(nowStr);
            } else if (bracket.containsKey(nowStr)) {
                //是否为右括号
                if (stack.isEmpty()) {
                    return null;
                }
                // ] => [
                Character character = bracket.get(nowStr);
                if (stack.peek().equals(character)) {
                    //左右括号匹配
                    symbol = character.toString() + nowStr;
                    //退栈
                    stack.pop();
                } else {
                    return null;
                }
            }
        }
        return stack.isEmpty() ? symbol : null;
    }

    private static Map<Character, Character> getSymbolBracket(Map<String, String> symbolTwinMap) {
        Map<Character, Character> bracket = new HashMap<>();
        for (Map.Entry<String, String> item : symbolTwinMap.entrySet()) {
            String value = item.getValue();
            bracket.put(value.substring(1).charAt(0), value.substring(0, 1).charAt(0));
        }
        return bracket;
    }

}

