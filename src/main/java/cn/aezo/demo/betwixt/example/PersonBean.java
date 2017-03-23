package cn.aezo.demo.betwixt.example;

/**
 * Created by smalle on 2017/3/23.
 */
public class PersonBean {

    private String name;
    private int age;

    /** Need to allow bean to be created via reflection */
    public PersonBean() {}

    public PersonBean(String name, int age) {
        this.name = name;
        this.age = age;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    // 可省略
    public String toString() {
        return "PersonBean[name='" + name + "',age='" + age + "']";
    }
}
