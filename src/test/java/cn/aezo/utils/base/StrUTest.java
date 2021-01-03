package cn.aezo.utils.base;

import org.junit.Test;

import java.util.List;

/**
 * @author smalle
 * @since 2020-12-27
 */
public class StrUTest {

    @Test
    public void getNextNo() {
        List<String> nextNo = StrU.getNextNo(5);
        System.out.println("nextNo = " + nextNo);
    }
}
