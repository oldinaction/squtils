package cn.aezo.utils.ext.tpl;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;

/**
 * @author smalle
 * @date 2019-09-05 22:52
 */
public class FtlU {
    /**
     * 根据模板文件输出内容到指定的输出流中(文件中)
     * @param name 模板文件的名称
     * @param path 模板文件的目录: 如ftl与此java文件同目录, 则此处为 ""
     * @param rootMap 模板的数据模型
     * @param outputStream 输出流
     */
    public static void rendToStream(String name, String path, Map<String, Object> rootMap, OutputStream outputStream) throws TemplateException, IOException {
        Writer out = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
        // 将模板文件内容以UTF-8编码输出到相应的流中
        getTemplate(name, path).process(rootMap, out);
        if (null != out) {
            out.close();
        }
    }

    public static void rendToStream(String sourceCode, Map<String, Object> rootMap, OutputStream outputStream) throws
            TemplateException, IOException {
        Configuration cfg = new Configuration(Configuration.VERSION_2_3_23);
        cfg.setDefaultEncoding("UTF-8");

        Template template = new Template("", sourceCode, cfg);

        Writer out = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
        template.process(rootMap, out);
        if (null != out) {
            out.close();
        }
    }

    /**
     * 根据模板文件输出内容到控制台
     * @param name       模板文件的名称
     * @param pathPrefix 模板文件的目录
     * @param rootMap    模板的数据模型
     */
    public static void rendToConsole(String name, String pathPrefix, Map<String, Object> rootMap) throws
            TemplateException, IOException {
        getTemplate(name, pathPrefix).process(rootMap, new PrintWriter(System.out));
    }

    public static void rendToConsole(String sourceCode, Map<String, Object> rootMap) throws
            TemplateException, IOException {
        Configuration cfg = new Configuration(Configuration.VERSION_2_3_23);
        cfg.setDefaultEncoding("UTF-8");
        Template template = new Template("", sourceCode, cfg);
        template.process(rootMap, new PrintWriter(System.out));
    }

    public static String rendToString(String sourceCode, Map<String, Object> rootMap) throws
            TemplateException, IOException {
        Configuration cfg = new Configuration(Configuration.VERSION_2_3_23);
        cfg.setDefaultEncoding("UTF-8");

        Template template = new Template("", sourceCode, cfg);
        StringWriter sw = new StringWriter();
        template.process(rootMap, sw);
        return sw.getBuffer().toString();
    }

    /**
     * 获取指定目录下的Ftl模板文件
     * @param name 模板文件的名称
     * @param path 模板文件的目录
     */
    public static Template getTemplate(String name, String path) throws IOException {
        // 通过FreeMarker的Configuration对象可以读取ftl文件
        Configuration cfg = new Configuration(Configuration.VERSION_2_3_23);
        // 设置模板文件的目录
        cfg.setClassForTemplateLoading(FtlU.class, path);
        cfg.setDefaultEncoding("UTF-8");
        // 在模板文件目录中寻找名为"name"的模板文件
        return cfg.getTemplate(name);
    }
}
