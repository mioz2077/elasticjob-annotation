package com.mioz.elasticjob.annotation.utils;

import com.alibaba.druid.filter.config.ConfigTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 使用druid中工具进行加密/解密
 *
 * 明文 + 私钥(defaultPrivateKey)加密 = 密文
 * 密文 + 公钥(defaultPublicKey)解密 = 明文
 *
 * @author ALI
 */
public class SecurityUtil {

    private static final Logger logger = LoggerFactory.getLogger(SecurityUtil.class);

    /**
     * 私钥
     */
    static String defaultPrivateKey;
    /**
     * 公钥
     */
    static String defaultPublicKey;

    static {
        try {
            String [] keyPair = ConfigTools.genKeyPair(512);
            defaultPrivateKey = keyPair[0];
            defaultPublicKey = keyPair[1];
        } catch (Exception e) {
            logger.error("加密工具类获取密钥对异常", e);
            // 这都会异常？ 表示内心十分崩溃，甚至还想退出、

            // 算了，还是直接退出吧，不想玩下去 ╥﹏╥...
            System.exit(-1);
        }
    }

    public static String encrypt(String plainText){
        return encrypt(defaultPrivateKey, plainText);
    }
    public static String encrypt(String privateKey, String plainText){
        String cipherText = "";
        try {
            cipherText = ConfigTools.encrypt(privateKey, plainText);
        } catch (Exception e) {
            logger.error("加密工具类加密获取密文异常", e);
        }
        return cipherText;
    }

    public static String decrypt(String cipherText){
        return decrypt(defaultPublicKey, cipherText);
    }
    public static String decrypt(String publicKey, String cipherText){
        String plainText = "";
        try {
            plainText = ConfigTools.decrypt(publicKey, cipherText);
        } catch (Exception e) {
            logger.error("加密工具类加密获取密文异常", e);
        }
        return plainText;
    }

    public static void main(String[] args) throws Exception {
        String publickey = "MFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBAKxwCf78UbuhfMeR7xwGh9FfVz5Hgh4q2pqi3PRA3CfZvneWu2jBQlshXpoTMoBkIGgM22lA3ntjbS7MNRCTaCsCAwEAAQ==";
        String passwd = "DwVZar+2abz3OOciiFklYAd1P79LoSzYyZ9Rqb2PRXn6lmae2VEYwiW28xemJqeHjhycxJyecvCbStSvOjzg/g==";
        String sss = decrypt(publickey, passwd);
        System.out.println(sss);

        String passwdss = "root";
        String cipherText = encrypt(passwdss);
        System.out.println(cipherText);
        String plainText = decrypt(cipherText);
        System.out.println(plainText);
    }
}
