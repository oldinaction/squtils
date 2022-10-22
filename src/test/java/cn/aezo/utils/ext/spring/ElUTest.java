package cn.aezo.utils.ext.spring;

import cn.aezo.utils.base.MiscU;

import java.util.HashMap;
import java.util.Map;

public class ElUTest {
    // @Test
    public void test() {
        Map<String, Object> map = new HashMap<>(16);
        map.put("exp", 4);
        Map<String, Object> map2 = new HashMap<>(16);
        map2.put("name", "test");
        map.put("map2", map2);

        Object result = ElU.parse("${exp>2}", map);
        // true
        System.out.println("result:" + result);

        result = ElU.parse("${map2.name}", map);
        // test
        System.out.println("result:" + result);

        result = ElU.parse("exp", map);
        // exp
        System.out.println("result:" + result);

        result = ElU.parseByFormat("A${map2.name} == ${map2.name}A", map);
        // Atest == testA
        System.out.println("result:" + result);

        // 测试括号
        // {S2_1=[-], S5_2=（2018[-]2019）, S6_3=《工业和信息化部关于印发电话用户真实身份信息登记实施规范的通知（2018[-]2019）》, S8_4=〔2018〕, S8_5=〔2019〕, S5_6=（工信部网安〔2018〕〔2019〕105号）, S8_7=〔2018〕}
        String title = "根据《工业和信息化部关于印发电话用户真实身份信息登记实施规范的通知（2018[-]2019）》（工信部网安〔2018〕〔2019〕105号）〔2018〕";
        Map<String, String> symbolTwinMap = MiscU.toMapAll(
                "S1", "()", "S2", "[]", "S3", "{}", "S4", "【】", "S5", "（）", "S6", "《》", "S7", "<>", "S8", "〔〕");
        System.out.println(ElU.getSymbolStr(symbolTwinMap, title));
    }
}
