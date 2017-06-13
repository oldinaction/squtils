package cn.aezo.utils.io;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.PoolableObjectFactory;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * 使用说明： TODO 目录设置
 * (1) 随应用程序初始化FtpU,初始化连接池等(也可需要的时候初始化)
 * (2) 获取FtpU实例：FtpU ftpU = FtpU.getInstance();
 * (3) 上传/下载 ftpU.upload / ftpU.download
 * (4) 返回FtpU实例：ftpU.returnClient();
 */
public class FtpU {
	public static Map<String, FTPClientPool> pool = new HashMap<String, FTPClientPool>();
	public FTPClient ftpClient;

	/**
	 * 随应用程序初始化FtpU, 初始化连接池等（默认初始化一个_default_的连接池）
	 * @param host
	 * @param port
	 * @param username
	 * @param password
	 * @throws Exception
	 */
	public static void init(String host, Integer port, String username, String password) throws Exception {
		init("_default_", host, port, username, password);
	}

	/**
	 * 随应用程序初始化多个FtpU连接(clientPoolName, host(int), port, username, password)
	 * @param configList
	 * @throws Exception
	 */
	public static void multiInit(List<List<String>> configList) throws Exception {
		for (List<String> list : configList) {
			String clientPoolName = list.get(0);
			String host = list.get(1);
			Integer port = null;
			if(list.get(2) != null) {
				port = Integer.valueOf(list.get(2));
			}
			String username = list.get(3);
			String password = list.get(4);

			init(clientPoolName, host, port, username, password);
		}
	}

	public static void init(String clientPoolName, String host, Integer port, String username, String password) throws Exception {
		FTPClientConfigure config = new FTPClientConfigure();
		config.setHost(host);
		config.setPort(port);
		config.setUsername(username);
		config.setPassword(password);
		config.setFileType(FTPClient.BINARY_FILE_TYPE); // 图片上传是一定要设置
		config.setPassiveMode("false");
		config.setClientTimeout(30 * 1000);

		FtpClientFactory factory = new FtpClientFactory(config);
		FtpU.pool.put(clientPoolName, new FTPClientPool(factory));
	}

	/**
	 * 获取FtpU实例（默认调用_default_的连接池）
	 * @return
	 * @throws Exception
	 */
	public static FtpU getInstance() throws Exception {
		if(pool.get("_default_") == null) return null;

		FtpU ftpU = new FtpU();
		ftpU.ftpClient = pool.get("_default_").borrowObject();
		ftpU.ftpClient.enterLocalPassiveMode(); // 防止Linux系统调用listFile()时返回空
		return ftpU;
	}

	/**
	 * 获取FtpU实例
	 * @param clientPoolName Ftp客户端连接池名
	 * @return
	 * @throws Exception
	 */
	public static FtpU getInstance(String clientPoolName) throws Exception {
		if(pool.get(clientPoolName) == null) return null;

		FtpU ftpU = new FtpU();
		ftpU.ftpClient = pool.get(clientPoolName).borrowObject();
		ftpU.ftpClient.enterLocalPassiveMode();
		return ftpU;
	}

	/**
	 * 返回FtpU实例（默认返回_default_的连接池）
	 * @return
	 * @throws Exception
	 */
	public FtpU returnClient() throws Exception {
		pool.get("_default_").returnObject(ftpClient);
		return this;
	}

	/**
	 * 返回FtpU实例
	 * @return
	 * @throws Exception
	 */
	public FtpU returnClient(String clientPool) throws Exception {
		pool.get(clientPool).returnObject(ftpClient);
		return this;
	}

	/**
	 * (A) ftp上传文件（可根据文件路径创建目录）
	 * @param filePath 上传到ftp对应文件路径(基于ftp根目录), 如：/test/me.png 会在根目录下创建一个文件夹
	 * @param inputStream
	 * @throws Exception
	 */
	public FtpU upload(String filePath, InputStream inputStream) throws IOException {
		if(filePath == null) {
			return this;
		}

		String[] paths = filePath.split("/");
		if(paths.length == 1) {
			ftpClient.storeFile(paths[0], inputStream); // a.txt
		} else {
			// /a.txt a/b.txt /a/b/c.txt
			String path = "";
			for (int i=0; i<paths.length-1; i++) {
				path += paths[i] + "/";
				ftpClient.makeDirectory(path);
				ftpClient.changeWorkingDirectory(path);
			}

			ftpClient.storeFile(paths[paths.length-1], inputStream);
			ftpClient.changeToParentDirectory(); // 切换到顶级目录
		}

		inputStream.close();

		return this;
	}
	
	/**
	 * ftp上传文件（无法创建目录）
	 * @param localFile
	 * @param remoteUploadPath
	 * @throws Exception
	 */
	public boolean upload(File localFile, String remoteUploadPath) {
        BufferedInputStream bis = null;    
        boolean success = false;    
        try {  
            boolean flag = ftpClient.changeWorkingDirectory(remoteUploadPath); // 改变工作路径
            System.out.println(flag);
            bis = new BufferedInputStream(new FileInputStream(localFile));    
            success = ftpClient.storeFile(localFile.getName(), bis);
        } catch (FileNotFoundException e) {    
            e.printStackTrace();    
        } catch (IOException e) {    
            e.printStackTrace();    
        } finally {    
            if (bis != null) {    
                try {    
                    bis.close();    
                } catch (IOException e) {    
                    e.printStackTrace();    
                }    
            }    
        }    
        return success;    
    }   
	
	public void upload(File file) {
		try {
			if (file.isDirectory()) {
				ftpClient.makeDirectory(file.getName());
				ftpClient.changeWorkingDirectory(file.getName());
				String[] files = file.list();
				for (String str : files) {
					File file1 = new File(file.getPath() + "/" + str);
					if (file1.isDirectory()) {
						upload(file1);
						ftpClient.changeToParentDirectory();
					} else {
						File file2 = new File(file.getPath() + "/" + str);
						FileInputStream input = new FileInputStream(file2);
						ftpClient.storeFile(file2.getName(), input);
						input.close();
					}
				}
			} else {
				File file2 = new File(file.getPath());
				FileInputStream input = new FileInputStream(file2);
				ftpClient.storeFile(file2.getName(), input);
				input.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 下载文件
	 * @param localBaseDir 本地目录
	 * @param ftpFilePath Ftp文件路径
	 * @throws Exception
	 */
	public Boolean download(String localBaseDir, String ftpFilePath) throws Exception {
		if(null == ftpFilePath) {
			return false;
		}

		String fileName = ftpFilePath;
		if(ftpFilePath.indexOf("/") != -1) {
			String dir = ftpFilePath.substring(0, ftpFilePath.lastIndexOf("/") + 1);
			fileName = ftpFilePath.substring(ftpFilePath.lastIndexOf("/") + 1, ftpFilePath.length());
			boolean changedir = ftpClient.changeWorkingDirectory(dir);
			if(!changedir) {
				return false;
			}
		}

		// 下载文件
		ftpClient.setControlEncoding("UTF-8");
		FTPFile[] files = ftpClient.listFiles(); // 获取全部文件信息
		for (int i = 0; i < files.length; i++) {
			if(fileName.equals(files[i].getName())) {
				File locaFile = new File(localBaseDir + fileName);
				if (!locaFile.exists()) {
					OutputStream outputStream = new FileOutputStream(locaFile);
					ftpClient.retrieveFile(fileName, outputStream); // 下载ftp文件
					ftpClient.changeToParentDirectory(); // 切换到顶级目录
					outputStream.flush();
					outputStream.close();
				} else {
					return null;
				}
				break;
			}
		}

		return true;
	}

	/**
	 * 下载文件
	 * @param outputStream
	 * @param ftpFilePath Ftp文件路径
	 * @throws IOException
	 * @author smalle
	 * @date 2016年12月26日 下午5:01:51
	 */
	public Boolean download(OutputStream outputStream, String ftpFilePath) throws Exception {
		if(null == ftpFilePath) {
			return false;
		}

		String fileName = ftpFilePath;
		if(ftpFilePath.indexOf("/") != -1) {
			String dir = ftpFilePath.substring(0, ftpFilePath.indexOf("/") + 1);
			fileName = ftpFilePath.substring(ftpFilePath.indexOf("/"), ftpFilePath.length());
			boolean changedir = ftpClient.changeWorkingDirectory(dir);
			if(!changedir) {
				return false;
			}
		}

		// 下载文件
		ftpClient.setControlEncoding("UTF-8");
		FTPFile[] files = ftpClient.listFiles(); // 获取全部文件信息
		for (int i = 0; i < files.length; i++) {
			if(fileName.equals(files[i].getName())) {
				ftpClient.retrieveFile(fileName, outputStream);
				ftpClient.changeToParentDirectory(); // 切换到顶级目录
				outputStream.flush();
				outputStream.close();
				break;
			}
		}

		return true;
	}
	
	/**
	 * 实现了一个FTPClient连接池
	 */
	public static class FTPClientPool implements ObjectPool {
		private static final int DEFAULT_POOL_SIZE = 10;
		private final BlockingQueue<FTPClient> pool;
		private final FtpClientFactory factory;

		/**
		 * 初始化连接池，需要注入一个工厂来提供FTPClient实例
		 * 
		 * @param factory
		 * @throws Exception
		 */
		public FTPClientPool(FtpClientFactory factory) throws Exception {
			this(DEFAULT_POOL_SIZE, factory);
		}

		/**
		 * @param poolSize
		 * @param factory
		 * @throws Exception
		 */
		public FTPClientPool(int poolSize, FtpClientFactory factory)
				throws Exception {
			this.factory = factory;
			pool = new ArrayBlockingQueue<FTPClient>(poolSize * 2);
			initPool(poolSize);
		}

		/**
		 * 初始化连接池，需要注入一个工厂来提供FTPClient实例
		 * 
		 * @param maxPoolSize
		 * @throws Exception
		 */
		private void initPool(int maxPoolSize) throws Exception {
			for (int i = 0; i < maxPoolSize; i++) {
				// 往池中添加对象
				addObject();
			}

		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.apache.commons.pool.ObjectPool#borrowObject()
		 */
		public FTPClient borrowObject() throws Exception {
			FTPClient client = pool.take();
			if (client == null) {
				client = factory.makeObject();
				addObject();
			} else if (!factory.validateObject(client)) {// 验证不通过
				// 使对象在池中失效
				invalidateObject(client);
				// 制造并添加新对象到池中
				client = factory.makeObject();
				addObject();
			}
			return client;

		}

		public void returnObject(Object o) throws Exception {

		}

		public void invalidateObject(Object o) throws Exception {

		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.apache.commons.pool.ObjectPool#returnObject(java.lang.Object)
		 */
		public void returnObject(FTPClient client) throws Exception {
			if ((client != null) && !pool.offer(client, 3, TimeUnit.SECONDS)) {
				try {
					factory.destroyObject(client);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		public void invalidateObject(FTPClient client) throws Exception {
			// 移除无效的客户端
			pool.remove(client);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.apache.commons.pool.ObjectPool#addObject()
		 */
		public void addObject() throws Exception, IllegalStateException, UnsupportedOperationException {
			// 插入对象到队列
			pool.offer(factory.makeObject(), 3, TimeUnit.SECONDS);
		}

		public int getNumIdle() throws UnsupportedOperationException {
			return 0;
		}

		public int getNumActive() throws UnsupportedOperationException {
			return 0;
		}

		public void clear() throws Exception, UnsupportedOperationException {

		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.apache.commons.pool.ObjectPool#close()
		 */
		public void close() throws Exception {
			while (pool.iterator().hasNext()) {
				FTPClient client = pool.take();
				factory.destroyObject(client);
			}
		}

		public void setFactory(PoolableObjectFactory factory)
				throws IllegalStateException, UnsupportedOperationException {

		}

	}

	/**
	 * FTPClient工厂类，通过FTPClient工厂提供FTPClient实例的创建和销毁
	 */
	public static class FtpClientFactory implements PoolableObjectFactory {
		private FTPClientConfigure config;

		// 给工厂传入一个参数对象，方便配置FTPClient的相关参数
		public FtpClientFactory(FTPClientConfigure config) {
			this.config = config;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.apache.commons.pool.PoolableObjectFactory#makeObject()
		 */
		public FTPClient makeObject() throws Exception {
			FTPClient ftpClient = new FTPClient();
			ftpClient.setConnectTimeout(config.getClientTimeout());
			try {
				ftpClient.connect(config.getHost(), config.getPort());
				int reply = ftpClient.getReplyCode();
				if (!FTPReply.isPositiveCompletion(reply)) {
					ftpClient.disconnect();
					return null;
				}
				boolean result = ftpClient.login(config.getUsername(), config.getPassword());
				if (!result) {
					throw new FTPClientException("ftpClient login fail! userName:"
							+ config.getUsername() + " ; password:"
							+ config.getPassword());
				}
				ftpClient.setFileType(config.getFileType());
				ftpClient.setBufferSize(1024);
				ftpClient.setControlEncoding(config.getEncoding());
				if (config.getPassiveMode().equals("true")) {
					ftpClient.enterLocalPassiveMode();
				}
			} catch (IOException e) {
				e.printStackTrace();
			} catch (FTPClientException e) {
				e.printStackTrace();
			}
			return ftpClient;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.apache.commons.pool.PoolableObjectFactory#destroyObject(java.lang.Object)
		 */
		public void destroyObject(FTPClient ftpClient) throws Exception {
			try {
				if (ftpClient != null && ftpClient.isConnected()) {
					ftpClient.logout();
				}
			} catch (IOException io) {
				io.printStackTrace();
			} finally {
				// 注意,一定要在finally代码中断开连接，否则会导致占用ftp连接情况
				try {
					ftpClient.disconnect();
				} catch (IOException io) {
					io.printStackTrace();
				}
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.apache.commons.pool.PoolableObjectFactory#validateObject(java.lang.Object)
		 */
		public boolean validateObject(FTPClient ftpClient) {
			try {
				return ftpClient.sendNoOp();
			} catch(SocketException e) {
				return false; // TODO 否则程序启动过一段时间将无法上传
			} catch (IOException e) {
				throw new RuntimeException("Failed to validate client: " + e, e);
			}
		}

		public void activateObject(FTPClient ftpClient) throws Exception {
		}

		public void passivateObject(FTPClient ftpClient) throws Exception {

		}

		public void activateObject(Object obj) throws Exception {
			
		}

		public void destroyObject(Object obj) throws Exception {
		}

		public void passivateObject(Object obj) throws Exception {
		}

		public boolean validateObject(Object obj) {
			return false;
		}
	}
	
	/**
	 * FTPClient配置类，封装了FTPClient的相关配置
	 */
	public static class FTPClientConfigure {
		private String host;
		private int port;
		private String username;
		private String password;
		private String passiveMode;
		private String encoding;
		private int clientTimeout;
		private int threadNum;
		private int fileType;
		private boolean renameUploaded;
		private int retryTimes;

		public String getHost() {
			return host;
		}

		public void setHost(String host) {
			this.host = host;
		}

		public int getPort() {
			return port;
		}

		public void setPort(int port) {
			this.port = port;
		}

		public String getUsername() {
			return username;
		}

		public void setUsername(String username) {
			this.username = username;
		}

		public String getPassword() {
			return password;
		}

		public void setPassword(String password) {
			this.password = password;
		}

		public String getPassiveMode() {
			return passiveMode;
		}

		public void setPassiveMode(String passiveMode) {
			this.passiveMode = passiveMode;
		}

		public String getEncoding() {
			return encoding;
		}

		public void setEncoding(String encoding) {
			this.encoding = encoding;
		}

		public int getClientTimeout() {
			return clientTimeout;
		}

		public void setClientTimeout(int clientTimeout) {
			this.clientTimeout = clientTimeout;
		}

		public int getThreadNum() {
			return threadNum;
		}

		public void setThreadNum(int threadNum) {
			this.threadNum = threadNum;
		}

		public int getFileType() {
			return fileType;
		}

		public void setFileType(int fileType) {
			this.fileType = fileType;
		}

		public boolean isRenameUploaded() {
			return renameUploaded;
		}

		public void setRenameUploaded(boolean renameUploaded) {
			this.renameUploaded = renameUploaded;
		}

		public int getRetryTimes() {
			return retryTimes;
		}

		public void setRetryTimes(int retryTimes) {
			this.retryTimes = retryTimes;
		}

		@Override
		public String toString() {
			return "FTPClientConfig [host=" + host + "\n port=" + port
					+ "\n username=" + username + "\n password=" + password
					+ "\n passiveMode=" + passiveMode + "\n encoding="
					+ encoding + "\n clientTimeout=" + clientTimeout
					+ "\n threadNum=" + threadNum + "\n fileType="
					+ fileType + "\n renameUploaded=" + renameUploaded
					+ "\n retryTimes=" + retryTimes + "]";
		}

	}
	
	public static class FTPClientException extends RuntimeException {
		private static final long serialVersionUID = 6639689724672349724L;
		
		public FTPClientException() {
	        super();
	    }

	    public FTPClientException(String message) {
	        super(message);
	    }
	}

	/*
	public static void main(String[] args) throws Exception {
		FTPClientConfigure config = new FTPClientConfigure();  
        config.setHost("127.0.0.1");
        config.setPort(21);
        config.setUsername("admin");
        config.setPassword("smalle");
        config.setFileType(FTPClient.BINARY_FILE_TYPE); // 图片上传是一定要设置
        config.setPassiveMode("false");
        config.setClientTimeout(30 * 1000);
        FtpClientFactory factory = new FtpClientFactory(config);
        FtpU.pool = new FTPClientPool(factory);
		
		for (int i=0; i<5; i++) {
	        FtpU ftpU = FtpU.getInstance();
	        File file = new File("d:/temp/1.png");
	        // FtpU.upload(file);
	        
	        // FtpU.upload(file, "img");
	        ftpU.upload(i + file.getGradeName(), new FileInputStream(file));
		
	        // FtpU.download("d:/temp/download/", "/test.txt");
	        System.out.println(i);
	        ftpU.returnClient();
		}
		
		System.out.println("ok");
		
	}
	*/
}
