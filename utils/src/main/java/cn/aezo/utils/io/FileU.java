package cn.aezo.utils.io;

import cn.aezo.utils.base.ValidU;

import java.io.*;
import java.net.URL;

/**
 * Created by smalle on 2017/5/10.
 */
public class FileU {
    /**
     * 根据classpath获取文件
     * @param relativePath 相对classpath的路径, 开头不需要/ (如：cn/aezo/utils/data.json)
     * @return
     */
    public static File getFileByClasspath(String relativePath) {
        URL url = FileU.class.getClassLoader().getResource(relativePath);
        if(url == null) return null;
        return new File(url.getFile());
    }

    public static File newFile(String fileFullName) throws IOException {
        File file = new File(fileFullName);
        boolean flag;
        if (!file.getParentFile().exists()) {
            flag = file.getParentFile().mkdirs();
            if(!flag) throw new IOException("创建父目录失败");
            flag = file.createNewFile();
            if(!flag) throw new IOException("创建文件失败");
        }
        return file;
    }

    /**
     * 创建目录(包含目录是否存在判断)
     * @param dirPath 绝对路径
     */
    public static void mkdir(String dirPath) {
        File root = new File(dirPath);
        if(root.exists()) {
            if (!root.isDirectory()) {
                throw new RuntimeException("目录被占用");
            }
        } else {
            root.mkdir();
        }
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
     * 读取文件内容(以行为单位读取)
     */
    public static String read(Object fileOrName) throws IOException {
        File file = fileOrName instanceof String ? new File((String) fileOrName) : (File) fileOrName;
        StringBuffer buffer = new StringBuffer();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            String tempString = null;
            // 一次读入一行，直到读入null为文件结束
            while ((tempString = reader.readLine()) != null) {
                buffer.append(tempString);
            }
        } finally {
            close(reader);
        }
        return buffer.toString();
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
                writer = new BufferedWriter (new OutputStreamWriter(new FileOutputStream((File) fileOrPath, append),"UTF-8"));
            } else {
                writer = new BufferedWriter (new OutputStreamWriter(new FileOutputStream((String) fileOrPath, append),"UTF-8"));
            }
            writer.write(content);
        }  finally {
            close(writer);
        }
    }

    /**
     * 复制文件
     * @param fromFile
     * @param toFile 复制到的文件完整路径。建议使用FileU.newFile传入文件(会自动创建目录)
     * @throws IOException
     */
    public static void copyFile(File fromFile, File toFile) throws IOException {
        FileInputStream ins = null;
        FileOutputStream out = null;
        try {
            ins = new FileInputStream(fromFile);
            out = new FileOutputStream(toFile);
            byte[] b = new byte[1024];
            int n;
            while((n=ins.read(b))!=-1){
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
        if(closeables == null) return;
        for (Closeable closeable: closeables) {
            try {
                if(closeable != null)
                    closeable.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
