package cn.aezo.utils.base;

import org.apache.commons.codec.binary.Base64;
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
import java.util.Random;

/**
 * @author smalle
 * @date 2019-11-08 17:28
 */
public class PassU {
    // ====================== 简单加密
    /**
     * md5和base64加密
     * @param s
     * @return
     */
    public static String md5AndBase64(String s) {
        if (s == null) return null;

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
            return null;
        }

        return encodeStr;
    }

    /**
     * base64加密
     * @param str
     * @return
     */
    public static String base64(String str) {
        if (str == null) return null;
        BASE64Encoder b64Encoder = new BASE64Encoder();
        return b64Encoder.encode(str.getBytes());
    }

    /**
     * md5加密(32位小写)
     * @param bytes eg: str.getBytes()
     * @return
     */
    public static String md5(byte[] bytes) {
        if (bytes == null) return null;
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
                if (i < 0)
                    i += 256;
                if (i < 16)
                    buf.append("0");
                buf.append(Integer.toHexString(i));
            }
            md5 = buf.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }

        return md5;
    }

    /**
     * md5加密(16位)
     * @param str
     * @return
     */
    public static String md516(String str) {
        return md5(str.getBytes()).substring(8, 24);
    }

    /**
     * 生成一串token：WSLqf5fVUJxGVkUnDOTpig==
     * @return
     */
    public static String getToken() {
        // 7346734837483  834u938493493849384  43434384
        String token = (System.currentTimeMillis() + new Random().nextInt(999999999)) + "";
        // 数据指纹   128位长   16个字节  md5
        try {
            MessageDigest md = MessageDigest.getInstance("md5");
            byte md5[] = md.digest(token.getBytes());
            //base64编码--任意二进制编码明文字符
            BASE64Encoder encoder = new BASE64Encoder();
            return encoder.encode(md5);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    // ====================== 对称密钥算法AES(公钥私钥一致，使用秘钥多次加密后的数据是一样的)
    /**
     *
     * 使用的是 AES/CBC/PKCS5Padding (算法/模式/填充)
     */
    private static String AES_KEY_STRING = "128bitslength@#*"; // 16个字节大小
    private static final String AES_IV_STRING = "A-16-Byte-String"; // 16个字节大小
    private static final String AES_CHARSET = "UTF-8";

    /**
     * 加密(数据越大，密文越长)。KRXiw6C1Py7ELBaxg1OlbQ==
     * @param content
     * @return
     */
    public static String aesEncrypt(String content) {
        try {
            byte[] contentBytes = content.getBytes(AES_CHARSET);
            byte[] keyBytes = AES_KEY_STRING.getBytes(AES_CHARSET);
            byte[] encryptedBytes = aesEncryptBytes(contentBytes, keyBytes);
            return Base64.encodeBase64String(encryptedBytes);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String aesDecrypt(String content) {
        try {
            byte[] encryptedBytes = Base64.decodeBase64(content);
            byte[] keyBytes = AES_KEY_STRING.getBytes(AES_CHARSET);
            byte[] decryptedBytes = aesDecryptBytes(encryptedBytes, keyBytes);
            if(decryptedBytes == null) return null;
            return new String(decryptedBytes, AES_CHARSET);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] aesEncryptBytes(byte[] contentBytes, byte[] keyBytes) {
        try {
            return cipherOperation(contentBytes, keyBytes, Cipher.ENCRYPT_MODE);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] aesDecryptBytes(byte[] contentBytes, byte[] keyBytes) {
        try {
            return cipherOperation(contentBytes, keyBytes, Cipher.DECRYPT_MODE);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static byte[] cipherOperation(byte[] contentBytes, byte[] keyBytes, int mode) {
        try {
            SecretKeySpec secretKey = new SecretKeySpec(keyBytes, "AES");
            byte[] initParam = AES_IV_STRING.getBytes(AES_CHARSET);
            IvParameterSpec ivParameterSpec = new IvParameterSpec(initParam);
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(mode, secretKey, ivParameterSpec);
            return cipher.doFinal(contentBytes);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static void aesTest() {
        String e = PassU.aesEncrypt(MiscU.toMap("username", "smalle", "role", "admin",
                "description", "O(∩_∩)O哈哈~O(∩_∩)O哈哈~", "createTime", System.currentTimeMillis()).toString());
        System.out.println("加密后 = " + e);
        String d = PassU.aesDecrypt(e);
        System.out.println("解密后 = " + d);
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
     * @return byte[] 加密数据. 可使用Base64.encodeBase64String得到字符串
     */
    public static byte[] rsaEncryptByPrivateKey(byte[] data, String key) throws Exception {

        //取得私钥
        PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(Base64.decodeBase64(key));
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
     * @return byte[] 加密数据. 可使用Base64.encodeBase64String得到字符串
     */
    public static byte[] rsaEncryptByPublicKey(byte[] data, String key) throws Exception {

        //实例化密钥工厂
        KeyFactory keyFactory = KeyFactory.getInstance(RSA_KEY);
        //初始化公钥
        //密钥材料转换
        X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(Base64.decodeBase64(key));
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
     * @return byte[] 解密数据. 可使用Base64.encodeBase64String得到字符串
     */
    public static byte[] rsaDecryptByPrivateKey(byte[] data, String key) throws Exception {
        //取得私钥
        PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(Base64.decodeBase64(key));
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
     * @return byte[] 解密数据. 可使用Base64.encodeBase64String得到字符串
     */
    public static byte[] rsaDecryptByPublicKey(byte[] data, String key) throws Exception {

        //实例化密钥工厂
        KeyFactory keyFactory = KeyFactory.getInstance(RSA_KEY);
        //初始化公钥
        //密钥材料转换
        X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(Base64.decodeBase64(key));
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
     * @return byte[] 私钥. 可使用Base64.encodeBase64String得到字符串
     */
    public static byte[] rsaGetPrivateKey(Map<String, Object> keyMap) {
        Key key = (Key) keyMap.get(RSA_PRIVATE_KEY);
        return key.getEncoded();
    }

    /**
     * 取得公钥
     *
     * @param keyMap 密钥map
     * @return byte[] 公钥. 可使用Base64.encodeBase64String得到字符串
     */
    public static byte[] rsaGetPublicKey(Map<String, Object> keyMap) {
        Key key = (Key) keyMap.get(RSA_PUBLIC_KEY);
        return key.getEncoded();
    }

    /**
     * 测试程序
     * @throws Exception
     */
    private static void rasTest() throws Exception {
        // 1.甲方初始化密钥，生成密钥对
        Map<String, Object> keyMap = PassU.rsaInitKey();
        // 公钥(由甲方给乙方)
        byte[] publicKey = PassU.rsaGetPublicKey(keyMap);
        // 私钥(甲方自己保管)
        byte[] privateKey = PassU.rsaGetPrivateKey(keyMap);
        // Base64.encodeBase64String(publicKey);
        String publicKeyStr = "MFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBAJtfJsyNOdOtOsVxhelevk92JrpvvsZryyZZF8+K4ttdHK+Nmuqn+1/nWO8q7HlSTKUgiLVUrITf/0Pl/IrRO4UCAwEAAQ==";
        // Base64.encodeBase64String(privateKey);
        String privateKeyStr = "MIIBVAIBADANBgkqhkiG9w0BAQEFAASCAT4wggE6AgEAAkEAm18mzI050606xXGF6V6+T3Ymum++xmvLJlkXz4ri210cr42a6qf7X+dY7yrseVJMpSCItVSshN//Q+X8itE7hQIDAQABAkBk1cMTfBBsjX+YBo43H/L5FSAbXUx98BVfJPEsE+IZJPMtspdoNjCEHh9P440s8B1NRBQpfVTA9SDfi7JXvvadAiEA9qayuu6H1Bb8x7/qVjtUCs6SQIYcR09f+OH9yCWpO9MCIQChQsByDTi5XFE1ycZzrc9L73pCu+VjSYgURRjtkAVMRwIhAIi16tjrv0OSHjgFKrjzF2kKCExVSTVJTJOhh025eizbAiA4MgDKPd1Eiu0p51Smvyz780oRs1SuktM+7Q1Qy91sxQIgFZ9Zy/OM4oqEFVo+QaMzedkUcaNT7yCHA4V8l/PzZzs=";
        System.out.println("公钥：" + publicKeyStr);
        System.out.println("私钥：" + privateKeyStr);
        System.out.println("================密钥对构造完毕,甲方将公钥公布给乙方，开始进行加密数据的传输=============" + "\n");

        // 2.甲加密->乙解密
        System.out.println("===========甲方向乙方发送加密数据==============");
        String str = "------甲方向乙方发送数据RSA算法";
        System.out.println("原文:" + str);
        // 甲方使用私钥进行数据的加密
        byte[] code1 = PassU.rsaEncryptByPrivateKey(str.getBytes(), privateKeyStr);
        System.out.println("加密后的数据：" + Base64.encodeBase64String(code1)); // HJ04XIANAmoGoqByx4MoG9b+61C6rruSx3Uczws/CmmIkywM9mTOXtClY/ZNG1e+q8XI510Ig6E1WZEKnIqrAA==
        System.out.println("===========乙方使用甲方提供的公钥对数据进行解密==============");
        // 乙方进行数据的解密
        byte[] decode1 = PassU.rsaDecryptByPublicKey(code1, publicKeyStr);
        System.out.println("乙方解密后的数据：" + new String(decode1) + "\n");

        // 3.乙加密->甲解密
        System.out.println("===========反向进行操作，乙方向甲方发送数据==============");
        str = "------乙方向甲方发送数据RSA算法";
        System.out.println("原文:" + str);
        // 乙方使用公钥对数据进行加密
        byte[] code2 = PassU.rsaEncryptByPublicKey(str.getBytes(), publicKeyStr);
        System.out.println("加密后的数据：" + Base64.encodeBase64String(code2)); // Oz0Y4cLyMbRQ8DZ8shWnY2F2tpeLmy8l7UqzA8NecSOpft1CEvVSK3Toq8ga0Fdryw+jk3dqytSr30wzCKL29Q==
        System.out.println("===========甲方使用私钥对数据进行解密==============");
        // 甲方使用私钥对数据进行解密
        byte[] decode2 = PassU.rsaDecryptByPrivateKey(code2, privateKeyStr);
        System.out.println("甲方解密后的数据：" + new String(decode2));
    }

    /**
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        aesTest();

        // rasTest();
    }

}
