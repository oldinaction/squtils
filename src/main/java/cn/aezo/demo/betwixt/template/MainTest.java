package cn.aezo.demo.betwixt.template;

import org.apache.commons.betwixt.io.BeanReader;

import java.io.File;
import java.io.FileReader;

/**
 * Created by smalle on 2017/3/23.
 */
public class MainTest {

    public static void main(String[] args) throws Exception {
        String folder = System.getProperty("user.dir") + "/src/main/java/cn/aezo/demo/betwixt/template/";
        FileReader fileReader = new FileReader(new File(folder + "readTarget.xml"));

        BeanReader beanReader  = new BeanReader();

        // Configure the reader
        beanReader.getXMLIntrospector().getConfiguration().setAttributesForPrimitives(false);
        beanReader.getBindingConfiguration().setMapIDs(false);

        beanReader.registerBeanClass("me", ExampleBean.class);

        ExampleBean example = (ExampleBean) beanReader.parse(fileReader);

        System.out.println(example.getName());
        System.out.println(example.getExamples());
    }
}
