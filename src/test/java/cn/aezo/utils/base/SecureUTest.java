package cn.aezo.utils.base;

import org.junit.Test;

import java.util.Base64;
import java.util.Map;

/**
 * @author smalle
 * @date 2020-11-26 23:15
 */
public class SecureUTest {

    @Test
    public void aesTest() {
        System.out.println("Base64.encode(\")|][d&%^+=-··~!o`\") = " + Base64.getEncoder().encodeToString(")|][d&%^+=-··~!o`".getBytes()));

        String e = SecureU.aesEncrypt(MiscU.toMap("username", "smalle", "roleCode", "admin",
                "description", "O(∩_∩)O哈哈~O(∩_∩)O哈哈~", "createTime", System.currentTimeMillis()).toString());
        System.out.println("加密后 = " + e); // 3/dzpNqIfJvzLymZfbCP29D2S4JGyv2JSGfOoK9KaXznO4N47eJxPVEwyLwolC+wdfz5u49RV1ARHkl+wsblwb1DAOU1dKnLyDSflzyhvwuALruxLSdLY9ioiywDktBmly8bXj3KQml1nMJQkSEe2A==
        String d = SecureU.aesDecrypt(e);
        System.out.println("解密后 = " + d); // {roleCode=admin, createTime=1606926017920, description=O(∩_∩)O哈哈~O(∩_∩)O哈哈~, username=smalle}
    }

    /**
     * 非对称加密算法(公钥/私钥)
     */
    @Test
    public void rasTest() throws Exception {
        // 1.甲方初始化密钥，生成密钥对
        Map<String, Object> keyMap = SecureU.rsaInitKey();
        // 公钥(由甲方给乙方)
        byte[] publicKey = SecureU.rsaGetPublicKey(keyMap);
        String publicKeyNew = Base64.getEncoder().encodeToString(publicKey);
        System.out.println("publicKeyNew = " + publicKeyNew);
        // 私钥(甲方自己保管)
        byte[] privateKey = SecureU.rsaGetPrivateKey(keyMap);
        String privateKeyNew = Base64.getEncoder().encodeToString(privateKey);
        System.out.println("privateKeyNew = " + privateKeyNew);

        String publicKeyStr = "MFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBAJtfJsyNOdOtOsVxhelevk92JrpvvsZryyZZF8+K4ttdHK+Nmuqn+1/nWO8q7HlSTKUgiLVUrITf/0Pl/IrRO4UCAwEAAQ==";
        String privateKeyStr = "MIIBVAIBADANBgkqhkiG9w0BAQEFAASCAT4wggE6AgEAAkEAm18mzI050606xXGF6V6+T3Ymum++xmvLJlkXz4ri210cr42a6qf7X+dY7yrseVJMpSCItVSshN//Q+X8itE7hQIDAQABAkBk1cMTfBBsjX+YBo43H/L5FSAbXUx98BVfJPEsE+IZJPMtspdoNjCEHh9P440s8B1NRBQpfVTA9SDfi7JXvvadAiEA9qayuu6H1Bb8x7/qVjtUCs6SQIYcR09f+OH9yCWpO9MCIQChQsByDTi5XFE1ycZzrc9L73pCu+VjSYgURRjtkAVMRwIhAIi16tjrv0OSHjgFKrjzF2kKCExVSTVJTJOhh025eizbAiA4MgDKPd1Eiu0p51Smvyz780oRs1SuktM+7Q1Qy91sxQIgFZ9Zy/OM4oqEFVo+QaMzedkUcaNT7yCHA4V8l/PzZzs=";
        System.out.println("公钥：" + publicKeyStr);
        System.out.println("私钥：" + privateKeyStr);
        System.out.println("================密钥对构造完毕,甲方将公钥公布给乙方，开始进行加密数据的传输=============" + "\n");

        // 2.甲加密->乙解密
        System.out.println("===========甲方向乙方发送加密数据==============");
        String str = "------甲方向乙方发送数据RSA算法";
        System.out.println("原文:" + str);
        // 甲方使用私钥进行数据的加密
        byte[] code10 = SecureU.rsaEncryptByPrivateKey(str.getBytes(), privateKeyStr); // 加密直接取字符串的字节数组
        String transCode1 = Base64.getEncoder().encodeToString(code10); // 传输时使用Base64再加密
        System.out.println("加密后的数据：" + transCode1); // HJ04XIANAmoGoqByx4MoG9b+61C6rruSx3Uczws/CmmIkywM9mTOXtClY/ZNG1e+q8XI510Ig6E1WZEKnIqrAA==
        System.out.println("===========乙方使用甲方提供的公钥对数据进行解密==============");
        // 乙方进行数据的解密
        byte[] code11 = Base64.getDecoder().decode(transCode1); // 使用Base64解密传输字符串
        byte[] decode1 = SecureU.rsaDecryptByPublicKey(code11, publicKeyStr);
        System.out.println("乙方解密后的数据：" + new String(decode1) + "\n"); // 将字节数组直接转成字符串

        // 3.乙加密->甲解密
        System.out.println("===========反向进行操作，乙方向甲方发送数据==============");
        str = "------乙方向甲方发送数据RSA算法";
        System.out.println("原文:" + str);
        // 乙方使用公钥对数据进行加密
        byte[] code20 = SecureU.rsaEncryptByPublicKey(str.getBytes(), publicKeyStr);
        String transCode2 = Base64.getEncoder().encodeToString(code20);
        System.out.println("加密后的数据：" + transCode2); // Oz0Y4cLyMbRQ8DZ8shWnY2F2tpeLmy8l7UqzA8NecSOpft1CEvVSK3Toq8ga0Fdryw+jk3dqytSr30wzCKL29Q==
        System.out.println("===========甲方使用私钥对数据进行解密==============");
        // 甲方使用私钥对数据进行解密
        byte[] code21 = Base64.getDecoder().decode(transCode2);
        byte[] decode2 = SecureU.rsaDecryptByPrivateKey(code21, privateKeyStr);
        System.out.println("甲方解密后的数据：" + new String(decode2));
    }
}
