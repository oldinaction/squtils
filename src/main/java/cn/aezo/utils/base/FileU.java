package cn.aezo.utils.base;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.io.resource.ClassPathResource;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import lombok.SneakyThrows;

import java.io.*;
import java.net.URL;
import java.util.Date;
import java.util.UUID;

/**
 * Created by smalle on 2017/5/10.
 */
public class FileU extends FileUtil {
    /**
     * 创建文件，会自动创建父目录
     * @param fileFullName 文件路径
     */
    @SneakyThrows
    public static File newFileSafe(String fileFullName) {
        File file = new File(fileFullName);
        boolean flag;
        if (!file.getParentFile().exists()) {
            flag = file.getParentFile().mkdirs();
            if(!flag) {
                throw new ExceptionU("创建父目录失败");
            }
            flag = file.createNewFile();
            if(!flag) {
                throw new ExceptionU("创建文件失败");
            }
        }
        return file;
    }

    /**
     * 复制文件
     * @param is
     * @param toFile 复制到的文件完整路径。建议使用 FileU.newFile 传入文件(会自动创建目录)
     * @throws IOException
     */
    @SneakyThrows
    public static void copyFile(InputStream is, Object toFile) {
        FileOutputStream out = null;
        try {
            if(toFile instanceof String) {
                out = new FileOutputStream((String) toFile);
            } else {
                out = new FileOutputStream((File) toFile);
            }

            byte[] b = new byte[1024];
            int n;
            while((n = is.read(b))!=-1) {
                out.write(b, 0, n);
            }
        } finally {
            close(is, out);
        }
    }

    /**
     * 文件上传保存到本地
     * @author smalle
     * @since 2021/1/13
     * @param is
     * @param originFileNameOrSuffix 原始文件名或文件后缀(用于自动分析文件后缀)
     * @param rootPath 保存文件根路径 /data
     * @param datePathFormat 保存文件的日期路径格式. eg: yyyy/MM/dd
     * @return filePath 如: /yyyy/MM/dd/uuid.xx
     */
    public static String saveFile(InputStream is, String originFileNameOrSuffix, String rootPath, String datePathFormat) {
        try {
            String filePath = "/" + DateU.format(new Date(), datePathFormat) + "/" + cn.hutool.core.lang.UUID.fastUUID();
            if(ValidU.isNotEmpty(originFileNameOrSuffix)) {
                String[] split = originFileNameOrSuffix.split("\\.");
                if(split.length > 1) {
                    filePath = filePath + "." + split[split.length - 1];
                } else if(originFileNameOrSuffix.startsWith(".")) {
                    filePath = filePath + originFileNameOrSuffix;
                }
            }
            if (rootPath.endsWith("/")) {
                rootPath = rootPath.substring(0, rootPath.length() - 1);
            }
            File file = FileU.newFileSafe(rootPath + filePath);
            FileU.copyFile(is, file);
            return filePath;
        } finally {
            IoUtil.close(is);
        }
    }

    public static void saveFileDirect(InputStream is, String originFileName, String rootPath) {
        try {
            File file = FileU.newFileSafe(rootPath + originFileName);
            FileU.copyFile(is, file);
        } finally {
            IoUtil.close(is);
        }
    }

    /**
     * 基于流从classpath获取文件内容(一般用于读取文本文件)
     * @author smalle
     * @since 2020/12/23
     * @param srcXpath 如：data.json，/spring/config.xml
     * @throws Exception 如找不到相关文件时会报错
     * @return java.lang.String
     */
    public static String getFileContentByClasspath(String srcXpath) {
        return getFileContentByClasspath(srcXpath, "utf-8");
    }

    @SneakyThrows
    public static String getFileContentByClasspath(String srcXpath, String charsetName) {
        if (!srcXpath.startsWith("/")) {
            srcXpath = "/" + srcXpath;
        }

        String content;
        InputStream inputStream = FileU.class.getResourceAsStream(srcXpath);
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(inputStream, charsetName));
            StringBuilder builder = new StringBuilder();
            char[] charArray = new char[200];
            int number;
            while ((number = reader.read(charArray)) != -1) {
                builder.append(charArray, 0, number);
            }
            content = builder.toString();
        } finally {
            if(reader != null) {
                reader.close();
            }
        }
        return content;
    }

    @SneakyThrows
    public static String getFileContent(String path) {
        return getFileContent(new File(path));
    }

    @SneakyThrows
    public static String getFileContent(File file) {
        String content;
        BufferedReader reader = null;
        try {
            StringBuilder builder = new StringBuilder();
            reader = new BufferedReader(new FileReader(file));
            char[] charArray = new char[200];
            int number;
            while ((number = reader.read(charArray)) != -1) {
                builder.append(charArray, 0, number);
            }
            content = builder.toString();
        } finally {
            if(reader != null) {
                reader.close();
            }
        }
        return content;
    }

    /**
     * 基于流读取classpath文件并生成临时文件返回(一般用于读取二进制文件)<br/>
     * 1.SpringBoot打包成jar后无法直接返回File，此方式是生成一个临时文件，使用完之后建议删除临时文件<br/>
     * 2.下列方式在IDEA中可获取，打包成(SpringBoot)JAR后无法获取<br/>
     *
     * ResourceUtils.getFile(ResourceUtils.CLASSPATH_URL_PREFIX + "data.json"); // spring<br/>
     * FileUtil.file(getClass().getClassLoader().getResource("data.json")); // hutool<br/>
     * FileUtil.file(ResourceUtil.getResource("data.json")); // hutool<br/>
     * FileU.getFileByClasspath("data.json"); // 同 getClass().getClassLoader().getResource("data.json")<br/>
     *
     * @author smalle
     * @since 2020/12/23
     * @param relativePath
     * @throws Exception 如找不到相关文件时会报错
     * @return java.io.File
     */
    @SneakyThrows
    public static File getFileTempByClasspath(String relativePath) {
        File tempFile = null;
        InputStream in = null;
        try {
            ClassPathResource classPathResource = new ClassPathResource(relativePath);
            in = classPathResource.getStream();
            tempFile = File.createTempFile(UUID.randomUUID().toString(), "");
            FileUtil.writeFromStream(in, tempFile);
        } finally {
            if(in != null) {
                IoUtil.close(in);
            }
        }
        return tempFile;
    }

    /**
     * 根据classpath获取文件(SpringBoot打包成jar后，此方法无效) {@link FileU#getFileTempByClasspath}
     * @param relativePath 相对classpath的路径, 开头不需要/ (如：cn/aezo/utils/data.json)
     * @return
     */
    public static File getFileByClasspath(String relativePath) {
        URL url = FileU.class.getClassLoader().getResource(relativePath);
        if(url == null) {
            return null;
        }
        return new File(url.getFile());
    }

    /**
     * 获取文件编码
     * @return
     */
    private String getFileCharset(File sourceFile) {
        byte[] first3Bytes = new byte[3];
        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(sourceFile))) {
            bis.mark(0);
            int read = bis.read(first3Bytes, 0, 3);
            if (read == -1) {
                // 文件编码为 ANSI
                return "GBK";
            }

            if (first3Bytes[0] == (byte) 0xFF && first3Bytes[1] == (byte) 0xFE) {
                // 文件编码为 Unicode
                return "UTF-16LE";
            }

            if (first3Bytes[0] == (byte) 0xFE && first3Bytes[1] == (byte) 0xFF) {
                // 文件编码为 Unicode big endian
                return "UTF-16BE";
            }

            if (first3Bytes[0] == (byte) 0xEF && first3Bytes[1] == (byte) 0xBB && first3Bytes[2] == (byte) 0xBF) {
                // 文件编码为 UTF-8
                return "UTF-8";
            }

            bis.reset();

            while ((read = bis.read()) != -1) {
                if (read >= 0xF0) {
                    break;
                }
                if (0x80 <= read && read <= 0xBF) {
                    break;
                }
                if (0xC0 <= read && read <= 0xDF) {
                    read = bis.read();
                    if (0x80 <= read && read <= 0xBF) {
                        // (0x80 - 0xBF),也可能在GB编码内
                        continue;
                    }

                    break;
                } else if (0xE0 <= read && read <= 0xEF) {
                    // 也有可能出错，但是几率较小
                    read = bis.read();
                    if (0x80 <= read && read <= 0xBF) {
                        read = bis.read();
                        if (0x80 <= read && read <= 0xBF) {
                            return "UTF-8";
                        }
                        break;
                    }
                    break;
                }
            }
        } catch (Exception e) {
            throw new ExceptionU("获取文件编码出错", e);
        }
        return "GBK";
    }

    public static String addFileSubName(String originFileName) {
        String sub = RandomUtil.randomString(6);
        return addFileSubName(originFileName, sub);
    }

    /**
     * 给文件增加后缀名称，如 test.txt => test_123.txt
     * @author smalle
     * @since 2022/7/21
     * @param originFileName
     * @param subName
     * @return java.lang.String
     */
    public static String addFileSubName(String originFileName, String subName) {
        if(ValidU.isEmpty(originFileName)) {
            return originFileName;
        }
        String[] split = originFileName.split("\\.");
        if(split.length == 1) {
            originFileName += "_" + subName;
        } else {
            String suffix = split[split.length - 1];
            String[] subArr = ArrayUtil.sub(split, 0, split.length - 1);
            String join = StrUtil.join(".", subArr);
            originFileName = join + "_" + subName + "." + suffix;
        }
        return originFileName;
    }

    // SQ ===============================================================

    /**
     * 智能创建目录(无父目录会自动创建，目录分割符需为"/")
     * @param rootDir 根目录(结尾不含"/")
     * @param dirPath 目录名称(开头不含"/")
     */
    public static void mkdirAuto(String rootDir, String dirPath) {
        if(ValidU.isEmpty(rootDir) || ValidU.isEmpty(dirPath)) {
            throw new RuntimeException("目录不能为空");
        }

        String[] dirArr = dirPath.split("/");
        String dirName = rootDir + "/";
        for (String dir : dirArr) {
            if(ValidU.isNotEmpty(dir)) {
                dirName += dir + "/";
                mkdir(dirName);
            }
        }
    }

    /**
     * 创建多个目录
     * @param rootDir 根目录(结尾不含"/")
     * @param dirName 目录名称(开头不含"/")
     */
    public static boolean mkdirs(String rootDir, String... dirName) {
        boolean flag = true;
        for (String dir : dirName) {
            File root = new File(rootDir + "/" + dir);
            if(root.exists()) {
                if (!root.isDirectory()) {
                    throw new RuntimeException("目录被占用");
                }
            } else {
                flag = root.mkdir();
            }
        }
        return flag;
    }

    /**
     * 读取文件内容(以行为单位读取，忽略换行符)
     */
    public static String read(Object fileOrName) throws IOException {
        return read(fileOrName, false);
    }

    /**
     * 读取文件内容(以行为单位读取，包含换行符)
     */
    public static String readWithSeparator(Object fileOrName) throws IOException {
        return read(fileOrName, true);
    }

    /**
     * 读取文件内容
     * @param fileOrName
     * @param withSeparator 是否包含换行符
     * @return
     * @throws IOException
     */
    public static String read(Object fileOrName, boolean withSeparator) throws IOException {
        File file = fileOrName instanceof String ? new File((String) fileOrName) : (File) fileOrName;
        LINE_SEPARATOR ls = LINE_SEPARATOR.UNKNOWN;
        if(withSeparator) {
            ls = getLineSeparator(file);
        }
        StringBuilder builder = new StringBuilder();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            String tempString;
            while ((tempString = reader.readLine()) != null) {
                builder.append(tempString);
                if(withSeparator) {
                    builder.append(ls.getValue());
                }
            }
        } finally {
            close(reader);
        }
        return builder.toString();
    }

    /**
     * 以字节为单位读取文件，常用于读二进制文件，如图片、声音、影像等文件。
     */
    public static void readFileByBytes(String fileName) {
        File file = new File(fileName);
        InputStream in = null;
        try {
            System.out.println("以字节为单位读取文件内容，一次读一个字节：");
            // 一次读一个字节
            in = new FileInputStream(file);
            int tempbyte;
            while ((tempbyte = in.read()) != -1) {
                System.out.write(tempbyte);
            }
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        try {
            System.out.println("以字节为单位读取文件内容，一次读多个字节：");
            // 一次读多个字节
            byte[] tempbytes = new byte[100];
            int byteread = 0;
            in = new FileInputStream(fileName);
            // 读入多个字节到字节数组中，byteread为一次读入的字节数
            while ((byteread = in.read(tempbytes)) != -1) {
                System.out.write(tempbytes, 0, byteread);
            }
        } catch (Exception e1) {
            e1.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e1) {
                }
            }
        }
    }

    /**
     * 以字符为单位读取文件，常用于读文本，数字等类型的文件
     */
    public static void readFileByChars(String fileName) {
        File file = new File(fileName);
        Reader reader = null;
        try {
            System.out.println("以字符为单位读取文件内容，一次读一个字节：");
            // 一次读一个字符
            reader = new InputStreamReader(new FileInputStream(file));
            int tempchar;
            while ((tempchar = reader.read()) != -1) {
                // 对于windows下，\r\n这两个字符在一起时，表示一个换行。
                // 但如果这两个字符分开显示时，会换两次行。
                // 因此，屏蔽掉\r，或者屏蔽\n。否则，将会多出很多空行。
                if (((char) tempchar) != '\r') {
                    System.out.print((char) tempchar);
                }
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            System.out.println("以字符为单位读取文件内容，一次读多个字节：");
            // 一次读多个字符
            char[] tempchars = new char[30];
            int charread = 0;
            reader = new InputStreamReader(new FileInputStream(fileName));
            // 读入多个字符到字符数组中，charread为一次读取字符数
            while ((charread = reader.read(tempchars)) != -1) {
                // 同样屏蔽掉\r不显示
                if ((charread == tempchars.length)
                        && (tempchars[tempchars.length - 1] != '\r')) {
                    System.out.print(tempchars);
                } else {
                    for (int i = 0; i < charread; i++) {
                        if (tempchars[i] == '\r') {
                            continue;
                        } else {
                            System.out.print(tempchars[i]);
                        }
                    }
                }
            }

        } catch (Exception e1) {
            e1.printStackTrace();
        } finally {
            close(reader);
        }
    }

    /**
     * 追加/覆盖文件内容(使用FileWriter)
     * @param fileOrPath 文件或文件路径
     * @param content
     * @param append true 追加; false 覆盖
     */
    public static void write(Object fileOrPath, String content, boolean append) throws IOException {
        BufferedWriter writer = null; // FileWriter容易产生乱码
        try {
            if(fileOrPath instanceof File) {
                writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream((File) fileOrPath, append),"UTF-8"));
            } else {
                writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream((String) fileOrPath, append),"UTF-8"));
            }
            writer.write(content);
        }  finally {
            close(writer);
        }
    }

    /**
     * 读入流转换为字符串
     * @param is
     * @return
     * @throws IOException
     */
    public static String inputStreamToString(InputStream is) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(is));
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
        } finally {
            close(br);
        }

        return sb.toString();
    }

    public static void close(Closeable... closeables) {
        if(closeables == null) {
            return;
        }
        for (Closeable closeable: closeables) {
            try {
                if(closeable != null) {
                    closeable.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 获取文件换行符
     * @param f
     * @return
     * @throws IllegalArgumentException
     */
    public static LINE_SEPARATOR getLineSeparator(File f) throws IllegalArgumentException {
        if (f == null || !f.isFile() || !f.exists()) {
            throw new IllegalArgumentException("file must exists!");
        }

        RandomAccessFile raf = null;
        try {
            raf = new RandomAccessFile(f, "r");
            String line = raf.readLine();
            if (line == null) {
                return LINE_SEPARATOR.UNKNOWN;
            }

            // 必须执行这一步，因为 RandomAccessFile 的 readLine() 会自动忽略并跳过换行符，所以需要先回退文件指针位置
            // "ISO-8859-1" 为 RandomAccessFile 使用的字符集，此处必须指定，否则中文 length 获取不对
            raf.seek(line.getBytes("ISO-8859-1").length);

            byte nextByte = raf.readByte();
            if (nextByte == 0x0A) {
                return LINE_SEPARATOR.LINUX;
            }

            if (nextByte != 0x0D) {
                return LINE_SEPARATOR.UNKNOWN;
            }

            try {
                nextByte = raf.readByte();
                if (nextByte == 0x0A) {
                    return LINE_SEPARATOR.WINDOWS;
                }
                return LINE_SEPARATOR.MAC;
            } catch (EOFException e) {
                return LINE_SEPARATOR.MAC;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (raf != null) {
                try {
                    raf.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return LINE_SEPARATOR.UNKNOWN;
    }

    /**
     * 转换文件换行符
     * @param f
     * @param targetSeparator 文件目标换行符
     * @param charset 文件编码，默认UTF-8
     * @return
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static boolean convertFileSeparator(File f, LINE_SEPARATOR targetSeparator, String charset) {
        if (targetSeparator == null || targetSeparator == LINE_SEPARATOR.UNKNOWN) {
            return false;
        }
        if (f == null || !f.isFile() || !f.exists()) {
            return false;
        }
        if (charset == null || charset.isEmpty()) {
            charset = "UTF-8";
        }
        LINE_SEPARATOR nowLs = getLineSeparator(f);
        if (nowLs == targetSeparator) {
            return true;
        }

        File temp = new File(f.getParent(), "temp.txt");
        if (temp.exists()) {
            temp.delete();
        }

        BufferedReader br = null;
        BufferedWriter bw = null;
        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(f), charset));
            bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(temp), charset));
            String line;
            int lineNumber = 0;
            while ((line = br.readLine()) != null) {
                if (lineNumber != 0) {
                    switch (targetSeparator) {
                        case WINDOWS:
                            bw.append('\r').append('\n');
                            break;
                        case LINUX:
                            bw.append('\n');
                            break;
                        case MAC:
                            bw.append('\r');
                            break;
                        default:
                    }
                }
                bw.write(line);
                ++lineNumber;
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
                if (bw != null) {
                    bw.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            f.delete();
            temp.renameTo(f);
        }

        return false;
    }

    /**
     * 换行符
     */
    public static enum LINE_SEPARATOR {
        /**
         * windows 系统
         */
        WINDOWS("\r\n"),
        LINUX("\n"),
        MAC("\r"),
        UNKNOWN("");

        private String value;

        LINE_SEPARATOR(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }
}
