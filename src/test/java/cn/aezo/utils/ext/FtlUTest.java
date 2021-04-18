package cn.aezo.utils.ext;

import cn.aezo.utils.base.MiscU;
import cn.aezo.utils.ext.tpl.FtlU;
import lombok.SneakyThrows;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;

/**
 * @author smalle
 * @since 2021-04-18
 */
public class FtlUTest {

    @SneakyThrows
    @Test
    public void getNextNo() {
        FtlU.rendToConsole("Hello ${name}", MiscU.toMap("name", "smalle1"));
        FtlU.rendToStream("Hello ${name}", MiscU.toMap("name", "smalle2"),
                new FileOutputStream(new File("D://temp/target0.ftl")));
        // mytpl为classpath根目录下文件夹
        FtlU.rendToConsole("test.ftl", "/mytpl", MiscU.toMap("name", "smalle3"));
        FtlU.rendToStream("test.ftl", "/mytpl", MiscU.toMap("name", "smalle4"),
                new FileOutputStream(new File("D://temp/target.ftl")));
    }
}
