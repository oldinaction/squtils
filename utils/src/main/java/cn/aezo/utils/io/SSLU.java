package cn.aezo.utils.io;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

/**
 * Created by smalle on 2017/11/30.
 * 如模拟https请求
 */
public class SslU implements X509TrustManager {
    private SSLSocketFactory sslFactory = null;

    private SslU(){
    }

    public void checkClientTrusted(X509Certificate[] arg0, String arg1)
            throws CertificateException {
    }

    public void checkServerTrusted(X509Certificate[] arg0, String arg1)
            throws CertificateException {
    }

    public X509Certificate[] getAcceptedIssuers() {
        return null;
    }

    /** 获取SSL Socket工厂 */
    public SSLSocketFactory getSSLSocketFactory() {
        return sslFactory;
    }

    private static SslU _instance = null;

    /** 获取SSL管理助手类实例 */
    synchronized public static SslU getInstance() throws NoSuchAlgorithmException, KeyManagementException {
        if (_instance == null) {
            _instance = new SslU();
            SSLContext sc = SSLContext.getInstance("SSLv3");
            sc.init(null, new TrustManager[]{new SslU()}, null);
            _instance.sslFactory = sc.getSocketFactory();
        }
        return _instance;
    }
}
