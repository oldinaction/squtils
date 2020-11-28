package cn.aezo.utils.io;

import cn.aezo.utils.base.ValidU;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.PoolableObjectFactory;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
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
 * 使用说明： TODO 目录设置、FTPClient被误退出导致不会重新创建
 * (1) 随应用程序初始化FtpU(initDefault),初始化连接池等(也可需要的时候初始化)
 * (2) 获取FtpU实例：FtpU ftpU = FtpU.getInstance();
 * (3) 上传/下载 ftpU.upload / ftpU.download
 * (4) 返回FtpU实例：ftpU.returnClient();
 */
public class FtpU {
	private static final Map<String, FTPClientPool> pool = new HashMap<>();
	private FTPClient ftpClient;

	public FTPClient getFtpClient() {
		return ftpClient;
	}

	public void setFtpClient(FTPClient ftpClient) {
		this.ftpClient = ftpClient;
	}

	/**
	 * 随应用程序初始化FtpU, 初始化连接池等（默认初始化一个_default_的连接池）
	 * @param host
	 * @param port
	 * @param username
	 * @param password
	 * @throws Exception
	 */
	public static FtpU initDefault(String host, Integer port, String username, String password, Map<String, Object> extConfig) throws Exception {
		return getOrCreateInstance("_default_", host, port, username, password, extConfig);
	}

	/**
	 * 随应用程序初始化多个FtpU连接(clientPoolName, host, port, username, password)
	 * @param configList
	 * @throws Exception
	 */
	public static void multiInit(List<Map<String, Object>> configList) throws Exception {
		for (Map<String, Object> configItem : configList) {
			List<Object> configs = (List<Object>) configItem.get("configs");
			Map<String, Object> extConfig = (Map<String, Object>) configItem.get("extConfig");
			String clientPollName = (String) configs.get(0); // clientPoll的名称
			String host = (String) configs.get(1);
			Object port = configs.get(2);
			String username = (String) configs.get(3);
			String password = (String) configs.get(4);

			Integer portInt;
			if(port instanceof String) {
				portInt = Integer.valueOf((String) port);
			} else {
				portInt = (Integer) port;
			}

			getOrCreateInstance(clientPollName, host, portInt, username, password, extConfig);
		}
	}

	public static FtpU getOrCreateInstance(String clientPollName, String host, Integer port, String username, String password,
										   Map<String, Object> extConfig) throws Exception {
		if(extConfig == null) extConfig = new HashMap<>();
		FtpU ftpU = getInstance(clientPollName);
		if(ftpU != null) return ftpU;
		int timeout = 30 * 1000; // 30秒
		if(ValidU.isNotEmpty(extConfig.get("timeout"))) {
			timeout = Integer.valueOf("" + extConfig.get("timeout"));
		}

		// 创建
		FTPClientConfigure config = new FTPClientConfigure();
		config.setHost(host);
		config.setPort(port);
		config.setUsername(username);
		config.setPassword(password);
		config.setFileType(FTPClient.BINARY_FILE_TYPE); // 图片上传是一定要设置
		config.setEnterLocalPassiveMode(false); // false: 不使用被动模式，即使用主动模式。建议使用被动模式，如果FTP服务器不支持被动则选择主动模式
		config.setClientTimeout(timeout);

		FtpClientFactory factory = new FtpClientFactory(config);
		FtpU.pool.put(clientPollName, new FTPClientPool(factory, extConfig));

		return getInstance(clientPollName);
	}

	/**
	 * 获取FtpU实例（默认调用_default_的连接池）
	 * @return
	 * @throws Exception
	 */
	public static FtpU getInstanceDefault() throws Exception {
		if(pool.get("_default_") == null) return new FtpU();

		FtpU ftpU = new FtpU();
		ftpU.ftpClient = pool.get("_default_").borrowObject();
		// ftpU.ftpClient.enterLocalPassiveMode(); // 防止Linux系统调用listFile()时返回空
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
		// ftpU.ftpClient.enterLocalPassiveMode();
		return ftpU;
	}

	/**
	 * 返回FtpU实例（默认返回_default_的连接池）
	 * @return
	 * @throws Exception
	 */
	public FtpU returnDefaultClient() throws Exception {
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
	 * @param filePath 上传到ftp对应文件路径(基于ftp根目录), 如：/test/me.png
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
		boolean flag;
		try {
			flag = ftpClient.changeWorkingDirectory(remoteUploadPath); // 改变工作路径
			if(!flag) return false;
			bis = new BufferedInputStream(new FileInputStream(localFile));
			flag = ftpClient.storeFile(localFile.getName(), bis);
		} catch (IOException e) {
			e.printStackTrace();
			flag = false;
		} finally {
			FileU.close(bis);
		}
		return flag;
	}

	public boolean upload(File file) {
		boolean flag = false;
		FileInputStream input = null;
		try {
			if (file.isDirectory()) {
				ftpClient.makeDirectory(file.getName());
				ftpClient.changeWorkingDirectory(file.getName());
				String[] files = file.list();
				if(files == null) return false;
				for (String str : files) {
					File file1 = new File(file.getPath() + "/" + str);
					if (file1.isDirectory()) {
						flag = upload(file1);
						if(!flag) return false;
						ftpClient.changeToParentDirectory();
					} else {
						File file2 = new File(file.getPath() + "/" + str);
						input = new FileInputStream(file2);
						flag = ftpClient.storeFile(file2.getName(), input);
						input.close();
					}
				}
			} else {
				File file2 = new File(file.getPath());
				input = new FileInputStream(file2);
				flag = ftpClient.storeFile(file2.getName(), input);
			}
		} catch (IOException e) {
			e.printStackTrace();
			flag = false;
		} finally {
			FileU.close(input);
		}

		return flag;
	}

	/**
	 * 下载文件
	 * @param ftpFilePath Ftp文件路径. eg: 1.png(基于根目录)
	 * @param localBaseDir 保存到的本地目录. eg: d:/temp/
	 * @param override 是否可以覆盖本地文件
	 */
	public boolean download(String ftpFilePath, String localBaseDir, boolean override) {
		boolean flag;
		if(null == ftpFilePath) {
			return false;
		}

		OutputStream outputStream = null;
		try {
			String fileName = ftpFilePath;
			if(ftpFilePath.contains("/")) {
				String dir = ftpFilePath.substring(0, ftpFilePath.lastIndexOf("/") + 1);
				fileName = ftpFilePath.substring(ftpFilePath.lastIndexOf("/") + 1, ftpFilePath.length());
				flag = ftpClient.changeWorkingDirectory(dir);
				if(!flag) return false;
			}

			// 下载文件
			ftpClient.setControlEncoding("UTF-8");
			FTPFile[] files = ftpClient.listFiles(); // 获取全部文件信息
			if(files == null || files.length == 0) return false;
			for (int i = 0; i < files.length; i++) {
				if(fileName.equals(files[i].getName())) {
					File localFile = new File(localBaseDir + fileName);
					if (localFile.exists()) {
						if(override) {
							outputStream = new FileOutputStream(localFile);
							flag = ftpClient.retrieveFile(fileName, outputStream); // 下载ftp文件
							if(!flag) return false;
							flag = ftpClient.changeToParentDirectory(); // 切换到顶级目录
							if(!flag) return false;
							outputStream.flush();
						}

						return true;
					} else {
						return false;
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			FileU.close(outputStream);
		}

		return false; // 未找到文件，则认为下载失败
	}

	/**
	 * 下载文件
	 * @param ftpFilePath Ftp文件路径
	 * @param outputStream
	 * @throws IOException
	 * @author smalle
	 * @date 2016年12月26日 下午5:01:51
	 */
	public Boolean download(String ftpFilePath, OutputStream outputStream) throws Exception {
		if(null == ftpFilePath) {
			return false;
		}

		String fileName = ftpFilePath;
		if(ftpFilePath.contains("/")) {
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
		public FTPClientPool(FtpClientFactory factory, Map<String, Object> extContext) throws Exception {
			Integer poolSize = DEFAULT_POOL_SIZE;
			if(extContext != null && extContext.get("poolSize") != null) {
				poolSize = (Integer) extContext.get("poolSize");
			}
			if(poolSize == null) poolSize= DEFAULT_POOL_SIZE;

			this.factory = factory;
			pool = new ArrayBlockingQueue<>(poolSize * 2);

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
			FTPClient client = pool.take(); // TODO pool.take会阻塞，pool.poll();会一致产生多个对象
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
			ftpClient.setRemoteVerificationEnabled(false);
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
				if (config.getEnterLocalPassiveMode()) {
					// 使用被动模式. pure-ftpd使用被动模式容易出现"storeFile, Connection refused: connect"
					ftpClient.enterLocalPassiveMode();
				}
			} catch (IOException | FTPClientException e) {
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
					assert ftpClient != null;
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
		private Boolean enterLocalPassiveMode;
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

		public Boolean getEnterLocalPassiveMode() {
			return enterLocalPassiveMode;
		}

		public void setEnterLocalPassiveMode(Boolean enterLocalPassiveMode) {
			this.enterLocalPassiveMode = enterLocalPassiveMode;
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
					+ "\n enterLocalPassiveMode=" + enterLocalPassiveMode + "\n encoding="
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

	// public static void main(String[] args) throws Exception {
	// 	FtpU.initDefault("192.168.17.196", 21, "ds", "ds88", MiscU.toMap("poolSize", 1));
    //
	// 	for (int i=0; i<5; i++) {
	// 		FtpU ftpU = FtpU.getInstanceDefault();
	// 		File file = new File("d:/temp/1.png");
	// 		ftpU.upload(file);
	// 		ftpU.returnDefaultClient();
	// 	}
    //
	// 	System.out.println("ok");
	// }
}
