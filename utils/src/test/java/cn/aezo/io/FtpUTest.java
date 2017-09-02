package cn.aezo.io;

import cn.aezo.utils.io.FtpU;

import java.io.File;
import java.io.FileInputStream;

public class FtpUTest extends Thread {
	private static int n = 0;
    
	public void run() {
        try {
            /********************业务代码调用样例*********************/  
            FtpU ftpU = FtpU.getInstance();
            System.out.println(ftpU.ftpClient);
            File file = new File("d:/temp/1.png");

            String dir = "";
            if(n==0) {
               dir = "/aa/bb/" + n + file.getName();
            } else if(n==1) {
                dir = "/aa/" + n + file.getName();
            } else if(n==2) {
                dir = "" + n + file.getName();
            } else if(n==3) {
                dir = "/" + n + file.getName();
            } else if(n==4) {
                dir = "/aa/" + n + file.getName();
            } else if(n==5) {
                dir = "bb/" + n + file.getName();
            }
            String t = "连接" + ++n;

            System.out.println(dir);
            sleep(3000);
            ftpU.upload(dir, new FileInputStream(file));

            ftpU.returnClient();
            System.out.println(t + "释放连接==>" + ftpU.ftpClient);  
            /***************************************************/  
        } catch (Exception e) {
            e.printStackTrace();  
        }
    }

    /*
	public static void main(String[] args) throws Exception {

        FtpU.FTPClientConfigure config = new FtpU.FTPClientConfigure();
        // 测试时下载的Slyar FTPserver性能太差，多并发时数据可能会中断
        config.setHost("192.168.17.50");
        config.setPort(23);
        config.setUsername("ftpservice");
        config.setPassword("xxx");
        config.setFileType(FTPClient.BINARY_FILE_TYPE); // 图片上传是一定要设置
        config.setPassiveMode("false");
        config.setClientTimeout(30 * 1000);
        FtpU.FtpClientFactory factory = new FtpU.FtpClientFactory(config);
        FtpU.pool.put("_default_", new FtpU.FTPClientPool(factory));

        // 并发测试
        for (int i = 1; i <= 6; i++) {
            FtpUTest test = new FtpUTest();
            test.start();
        }

        // 测试二
	    FtpU.multiInit(MiscU.Instance.toList(
	            MiscU.Instance.toList("tx", "xx.xx.xx.xx", "21", "ftp1", "aa"),
                MiscU.Instance.toList("ug", "192.168.17.50", "23", "ftpservice", "xxx")));
        File tx = new File("d:/temp/1.png");
        FtpU.getInstance("tx").upload("/a/1.png", new FileInputStream(tx));

        File ug = new File("d:/temp/test.txt");
        FtpU.getInstance("ug").upload("/b/test.txt", new FileInputStream(ug));
	}
	*/

	
}
