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
 */
public class SSLU implements X509TrustManager {
    private SSLSocketFactory sslFactory = null;

    private SSLU(){
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

    private static SSLU _instance = null;

    /** 获取SSL管理助手类实例 */
    synchronized public static SSLU getInstance() throws NoSuchAlgorithmException, KeyManagementException {
        if (_instance == null){
            _instance = new SSLU();
            SSLContext sc = SSLContext.getInstance("SSLv3");
            sc.init(null, new TrustManager[]{new SSLU()}, null);
            _instance.sslFactory = sc.getSocketFactory();
        }
        return _instance;
    }
}
