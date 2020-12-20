package cn.aezo.utils.base;

import cn.hutool.core.io.FileUtil;

import java.io.*;
import java.net.URL;

/**
 * Created by smalle on 2017/5/10.
 */
public class FileU extends FileUtil {
    /**
     * 换行符
     */
    public enum LINE_SEPARATOR {
        WINDOWS("\r\n"), LINUX("\n"), MAC("\r"), UNKNOWN("");

        private String value;

        LINE_SEPARATOR(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    /**
     * 根据classpath获取文件
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

    public static File newFileSafe(String fileFullName) throws IOException {
        File file = new File(fileFullName);
        boolean flag;
        if (!file.getParentFile().exists()) {
            flag = file.getParentFile().mkdirs();
            if(!flag) {
                throw new IOException("创建父目录失败");
            }
            flag = file.createNewFile();
            if(!flag) {
                throw new IOException("创建文件失败");
            }
        }
        return file;
    }

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
     * 复制文件
     * @param fromFileOrInputStream
     * @param toFile 复制到的文件完整路径。建议使用FileU.newFile传入文件(会自动创建目录)
     * @throws IOException
     */
    public static void copyFile(Object fromFileOrInputStream, File toFile) throws IOException {
        InputStream ins = null;
        FileOutputStream out = null;
        try {
            if(fromFileOrInputStream instanceof File) {
                ins = new FileInputStream((File) fromFileOrInputStream);
            } else {
                ins = (InputStream) fromFileOrInputStream;
            }
            out = new FileOutputStream(toFile);
            byte[] b = new byte[1024];
            int n;
            while((n=ins.read(b))!=-1) {
                out.write(b, 0, n);
            }
        } finally {
            close(ins, out);
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
}
