package cn.aezo.base;

import cn.aezo.utils.base.MathU;
import org.junit.Test;

/**
 * Created by smalle on 2017/6/2.
 */
public class MathUTest {

    @Test
    public void bug() {
        System.out.println(0.06 + 0.01); // 0.06999999999999999
        System.out.println(1.0 - 0.42); // 0.5800000000000001
        System.out.println(4.015 * 100); // 401.49999999999994
        System.out.println(303.1 / 1000); // 0.30310000000000004
   }

    @Test
    public void bigDecimal() {
        // 加法
        System.out.println(MathU.add(0.06, 0.01)); // 0.07

        // 减法
        System.out.println(MathU.sub(1.0, 0.42)); // 0.58

        // 乘法
        System.out.println(MathU.mul(4.015, 100)); // 401.5

        // 除法
        System.out.println(MathU.div(10, 3, 2)); // 3.33
    }
}
