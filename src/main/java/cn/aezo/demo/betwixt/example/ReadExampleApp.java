package cn.aezo.demo.betwixt.example;

import org.apache.commons.betwixt.io.BeanReader;

import java.io.StringReader;

/**
 * Created by smalle on 2017/3/23.
 */
public class ReadExampleApp {

    public static final void main(String args[]) throws Exception{
        StringReader xmlReader = new StringReader("<?xml version='1.0' ?><person><age>25</age><name>James Smith</name></person>");

        BeanReader beanReader  = new BeanReader();

        // Configure the reader
        beanReader.getXMLIntrospector().getConfiguration().setAttributesForPrimitives(false);
        beanReader.getBindingConfiguration().setMapIDs(false);

        beanReader.registerBeanClass("person", PersonBean.class);

        PersonBean person = (PersonBean) beanReader.parse(xmlReader);

        System.out.println(person); // PersonBean[name='James Smith',age='25']
    }
}
