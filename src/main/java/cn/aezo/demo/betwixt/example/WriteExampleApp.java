package cn.aezo.demo.betwixt.example;

import org.apache.commons.betwixt.io.BeanWriter;

import java.io.StringWriter;

/**
 * Created by smalle on 2017/3/23.
 */
public class WriteExampleApp {

    /**
     * 转换javaBean为xml数据
     */
    public static final void main(String [] args) throws Exception {

        StringWriter outputWriter = new StringWriter();

        outputWriter.write("<?xml version='1.0' encoding='UTF-8' ?>\n");

        BeanWriter beanWriter = new BeanWriter(outputWriter);

        // Configure betwixt
        // 是否将字段的值以属性的形式展现：false表示以子节点的形式
        beanWriter.getXMLIntrospector().getConfiguration().setAttributesForPrimitives(false);
        beanWriter.getBindingConfiguration().setMapIDs(false);
        beanWriter.enablePrettyPrint();

        // 命名映射：可实现 NameMapper 接口。
        // 提供 DefaultNameMapper/DecapitalizeNameMapper 默认字段名、HyphenatedNameMapper 连字符、CapitalizeNameMapper 首字母大写、BadCharacterReplacingNMapper 错误字符替换
        // writer.getXMLIntrospector().getConfiguration().setElementNameMapper(new HyphenatedNameMapper(true, "_"));

        // 主要需要PersonBean中的getter方法，如果属性类型为集合，则会渲染成子标签
        beanWriter.write("person", new PersonBean("John Smith", 21));
        beanWriter.write("person", new PersonBean("Smalle", 18));

        System.out.println(outputWriter.toString());

        // Betwixt writes fragments not documents so does not automatically close
        // writers or streams.
        // This example will do no more writing so close the writer now.
        outputWriter.close();
    }

}
