package cn.aezo.demo.betwixt.template;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by smalle on 2017/3/23.
 */
public class ExampleBean {

    private String name;
    private List examples = new ArrayList();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List getExamples() {
        return examples;
    }

    public void addExample(IExample example) {
        examples.add(example);
    }
}
