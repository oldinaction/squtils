package cn.aezo.utils.base;

import org.junit.Test;

/**
 * @author smalle
 * @since 2021-01-06
 */
public class ValidUTest {

    @Test
    public void getNextNo() {
        System.out.println("isAllNotEmpty = " + ValidU.isAllNotEmpty("a", "b"));
        System.out.println("isAllNotEmpty2 = " + ValidU.isAllNotEmpty(new Object(), new Object(), null));
        System.out.println("isAllNotEmpty3 = " + ValidU.isAllNotEmpty(MiscU.toList("a"), MiscU.toList("b")));
    }

}
