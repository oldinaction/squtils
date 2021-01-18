package cn.aezo.utils.base;

import cn.hutool.core.codec.Base64;
import sun.misc.BASE64Encoder;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author smalle
 * @date 2019-11-08 17:28
 */
public class SecureU {
    // ====================== 简单加密
    /**
     * md5和base64加密
     * @param s
     * @return
     */
    public static String md5AndBase64(String s) {
        if (s == null) {
            return null;
        }

        String encodeStr;
        byte[] utfBytes = s.getBytes();
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("MD5");
            md.update(utfBytes);
            byte[] bytes = md.digest();

            BASE64Encoder b64Encoder = new BASE64Encoder();
            encodeStr = b64Encoder.encode(bytes);
        } catch (NoSuchAlgorithmException e) {
            throw new ExceptionU("加密出错", e);
        }

        return encodeStr;
    }

    /**
     * base64加密
     * @param str
     * @return
     */
    public static String base64(String str) {
        if (str == null) {
            return null;
        }
        BASE64Encoder b64Encoder = new BASE64Encoder();
        return b64Encoder.encode(str.getBytes());
    }

    /**
     * md5加密(32位小写，大写可自行转换)
     * @param bytes eg: str.getBytes()
     * @return
     */
    public static String md5(byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        String md5 = "";
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("MD5"); // 生成一个MD5加密计算摘要
            md.update(bytes); // 计算md5函数

            byte b[] = md.digest();
            int i;
            StringBuffer buf = new StringBuffer();
            for (int offset = 0; offset < b.length; offset++) {
                i = b[offset];
                if (i < 0) {
                    i += 256;
                }
                if (i < 16) {
                    buf.append("0");
                }
                buf.append(Integer.toHexString(i));
            }
            md5 = buf.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new ExceptionU("加密出错", e);
        }

        return md5;
    }

    /**
     * md5加密(16位小写)
     * @param str
     * @return
     */
    public static String md516(String str) {
        return md5(str.getBytes()).substring(8, 24);
    }

    /**
     * 生成一串token(24位)：moFC0gvBUe7AzHPTWRuDYQ==
     * @return
     */
    public static String getToken() {
        // 7346734837483  834u938493493849384  43434384
        String token = UUID.randomUUID().toString();
        // 数据指纹   128位长   16个字节  md5
        try {
            MessageDigest md = MessageDigest.getInstance("md5");
            byte md5[] = md.digest(token.getBytes());
            //base64编码--任意二进制编码明文字符
            BASE64Encoder encoder = new BASE64Encoder();
            return encoder.encode(md5);
        } catch (NoSuchAlgorithmException e) {
            throw new ExceptionU("生成Token出错", e);
        }
    }

    // ====================== 对称密钥算法AES(公钥私钥一致，使用秘钥多次加密后的数据是一样的)
    /**
     *
     * 使用的是 AES/CBC/PKCS5Padding (算法/模式/填充)。对应的有 AES/CBC/PKCS7Padding(jdk默认不支持，具体参考博客)、DES/CBC/PKCS5Padding
     */
    private static final String AES_INSTANCE_NAME = "AES/CBC/PKCS5Padding";
    /**
     * 秘钥：16个字节大小(128位)。如：0123456789abcdef
     */
    private static String AES_KEY_STRING = "0123456789abcdef";
    /**
     * 偏移量：16个字节大小(128位)。如：0123456789abcdef
     */
    private static final String AES_IV_STRING = "0123456789abcdef";

    /**
     * 加密(数据越大，密文越长)。KRXiw6C1Py7ELBaxg1OlbQ==
     * @param content
     * @return
     */
    public static String aesEncrypt(String content) {
        return aesEncrypt(content, AES_INSTANCE_NAME, AES_KEY_STRING, AES_IV_STRING);
    }

    /**
     * 解密
     * @param content
     * @return java.lang.String
     */
    public static String aesDecrypt(String content) {
        return aesDecrypt(content, AES_INSTANCE_NAME, AES_KEY_STRING, AES_IV_STRING);
    }

    public static String aesEncrypt(String content, String instanceName, String key, String iv) {
        try {
            byte[] contentBytes = content.getBytes();
            Cipher cipher = initCipher(instanceName, key, iv, Cipher.ENCRYPT_MODE);
            byte[] encryptedBytes = cipher.doFinal(contentBytes);
            return Base64.encode(encryptedBytes);
        } catch (Exception e) {
            throw new ExceptionU("加密出错", e);
        }
    }

    public static String aesDecrypt(String content, String instanceName, String key, String iv) {
        try {
            byte[] encryptedBytes = Base64.decode(content);
            Cipher cipher = initCipher(instanceName, key, iv, Cipher.DECRYPT_MODE);
            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
            if(decryptedBytes == null) {
                return null;
            }
            return new String(decryptedBytes);
        } catch (Exception e) {
            throw new ExceptionU("解密出错", e);
        }
    }

    private static Cipher initCipher(String instanceName, String key, String iv, int mode) throws Exception {
        SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), "AES");
        IvParameterSpec ivParameterSpec = new IvParameterSpec(iv.getBytes());
        Cipher cipher = Cipher.getInstance(instanceName);
        cipher.init(mode, secretKey, ivParameterSpec);
        return cipher;
    }

    // ====================== 非对称密钥算法(公钥私钥不一致)
    /**
     * 参考：https://www.cnblogs.com/frank-quan/p/7073457.html
     * RSA算法相对于DES/AES等对称加密算法，速度慢一些
     *
     * 总原则：公钥加密，私钥解密 / 私钥加密，公钥解密
     *
     * 甲乙双方发送数据模型
     * 1.甲方在本地构建密钥对（公钥+私钥），并将公钥公布给乙方
     * 2.甲方将数据用私钥进行加密，发送给乙方(使用私钥多次加密后的数据是一样的)
     * 3.乙方用甲方提供的公钥对数据进行解密
     * 4.乙方用公钥对数据进行加密，然后传送给甲方(使用公钥多次加密后的数据是不一样的)
     * 5.甲方用私钥对数据进行解密
     */

    // 非对称密钥算法
    public static final String RSA_KEY = "RSA";
    // 密钥长度，DH算法的默认密钥长度是1024。密钥长度必须是64的倍数，在512到65536位之间
    private static final int RSA_KEY_SIZE = 512;
    // 公钥Key
    private static final String RSA_PUBLIC_KEY = "RSAPublicKey";
    // 私钥Key
    private static final String RSA_PRIVATE_KEY = "RSAPrivateKey";

    /**
     * 初始化密钥对
     * @return Map 甲方密钥的Map
     */
    public static Map<String, Object> rsaInitKey() throws Exception {
        //实例化密钥生成器
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(RSA_KEY);
        //初始化密钥生成器
        keyPairGenerator.initialize(RSA_KEY_SIZE);
        //生成密钥对
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        //甲方公钥
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        //甲方私钥
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
        //将密钥存储在map中
        Map<String, Object> keyMap = new HashMap<String, Object>();
        keyMap.put(RSA_PUBLIC_KEY, publicKey);
        keyMap.put(RSA_PRIVATE_KEY, privateKey);
        return keyMap;
    }

    /**
     * 私钥加密
     *
     * @param data 待加密数据
     * @param key       密钥
     * @return byte[] 加密数据. 可使用Base64.encode得到字符串
     */
    public static byte[] rsaEncryptByPrivateKey(byte[] data, String key) throws Exception {

        //取得私钥
        PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(Base64.decode(key));
        KeyFactory keyFactory = KeyFactory.getInstance(RSA_KEY);
        //生成私钥
        PrivateKey privateKey = keyFactory.generatePrivate(pkcs8KeySpec);
        //数据加密
        Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
        cipher.init(Cipher.ENCRYPT_MODE, privateKey);
        return cipher.doFinal(data);
    }

    /**
     * 公钥加密
     *
     * @param data 待加密数据
     * @param key       密钥
     * @return byte[] 加密数据. 可使用Base64.encode得到字符串
     */
    public static byte[] rsaEncryptByPublicKey(byte[] data, String key) throws Exception {

        //实例化密钥工厂
        KeyFactory keyFactory = KeyFactory.getInstance(RSA_KEY);
        //初始化公钥
        //密钥材料转换
        X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(Base64.decode(key));
        //产生公钥
        PublicKey pubKey = keyFactory.generatePublic(x509KeySpec);

        //数据加密
        Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
        cipher.init(Cipher.ENCRYPT_MODE, pubKey);
        return cipher.doFinal(data);
    }

    /**
     * 私钥解密
     *
     * @param data 待解密数据
     * @param key  密钥
     * @return byte[] 解密数据. 可使用Base64.encode得到字符串
     */
    public static byte[] rsaDecryptByPrivateKey(byte[] data, String key) throws Exception {
        //取得私钥
        PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(Base64.decode(key));
        KeyFactory keyFactory = KeyFactory.getInstance(RSA_KEY);
        //生成私钥
        PrivateKey privateKey = keyFactory.generatePrivate(pkcs8KeySpec);
        //数据解密
        Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        return cipher.doFinal(data);
    }

    /**
     * 公钥解密
     *
     * @param data 待解密数据
     * @param key  密钥
     * @return byte[] 解密数据. 可使用Base64.encode得到字符串
     */
    public static byte[] rsaDecryptByPublicKey(byte[] data, String key) throws Exception {

        //实例化密钥工厂
        KeyFactory keyFactory = KeyFactory.getInstance(RSA_KEY);
        //初始化公钥
        //密钥材料转换
        X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(Base64.decode(key));
        //产生公钥
        PublicKey pubKey = keyFactory.generatePublic(x509KeySpec);
        //数据解密
        Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
        cipher.init(Cipher.DECRYPT_MODE, pubKey);
        return cipher.doFinal(data);
    }

    /**
     * 取得私钥
     *
     * @param keyMap 密钥map
     * @return byte[] 私钥. 可使用Base64.encode得到字符串
     */
    public static byte[] rsaGetPrivateKey(Map<String, Object> keyMap) {
        Key key = (Key) keyMap.get(RSA_PRIVATE_KEY);
        return key.getEncoded();
    }

    /**
     * 取得公钥
     *
     * @param keyMap 密钥map
     * @return byte[] 公钥. 可使用Base64.encode得到字符串
     */
    public static byte[] rsaGetPublicKey(Map<String, Object> keyMap) {
        Key key = (Key) keyMap.get(RSA_PUBLIC_KEY);
        return key.getEncoded();
    }
}
