package cn.aezo.demo.betwixt.template;

/**
 * Created by smalle on 2017/3/23.
 */
public class ExampleImpl implements IExample {

    private int id;
    private String name;

    public ExampleImpl() {}

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}