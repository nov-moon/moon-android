package com.meili.moon.sdk.util.encrypt;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.Key;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;


/**
 * <p>
 * AES加密解密工具包
 * </p>
 *
 * @author IceWee
 * @version 1.0
 * @date 2012-5-18
 */
public class AESUtils {

    private static final String ALGORITHM = "AES";
    private static final int KEY_SIZE = 128;
    private static final int CACHE_SIZE = 1024;

    /**
     * <p>
     * 生成随机密钥
     * </p>
     *
     * @return
     * @throws Exception
     */
    public static String getSecretKey() throws Exception {
        return getSecretKey(null);
    }

    /**
     * <p>
     * 生成密钥
     * </p>
     *
     * @param seed 密钥种子
     * @return
     * @throws Exception
     */
    public static String getSecretKey(String seed) throws Exception {
        KeyGenerator keyGenerator = KeyGenerator.getInstance(ALGORITHM);
        SecureRandom secureRandom;
        if (seed != null && !"".equals(seed)) {
            secureRandom = new SecureRandom(seed.getBytes());
        } else {
            secureRandom = new SecureRandom();
        }
        keyGenerator.init(KEY_SIZE, secureRandom);
        SecretKey secretKey = keyGenerator.generateKey();
        return bytesToHexString(secretKey.getEncoded());
    }

    /**
     * <p>
     * 加密
     * </p>
     *
     * @param data
     * @param key
     * @return
     * @throws Exception
     */
    public static byte[] encrypt(byte[] data, String key) throws Exception {
        Key k = toKey(key);
        byte[] raw = k.getEncoded();
        SecretKeySpec secretKeySpec = new SecretKeySpec(raw, ALGORITHM);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
        return cipher.doFinal(data);
    }

    /**
     * <p>
     * 文件加密
     * </p>
     *
     * @param key
     * @param sourceFilePath
     * @param destFilePath
     * @throws Exception
     */
    public static void encryptFile(String key, String sourceFilePath, String destFilePath) throws Exception {
        File sourceFile = new File(sourceFilePath);
        File destFile = new File(destFilePath);
        if (sourceFile.exists() && sourceFile.isFile()) {
            if (!destFile.getParentFile().exists()) {
                destFile.getParentFile().mkdirs();
            }
            destFile.createNewFile();
            InputStream in = new FileInputStream(sourceFile);
            OutputStream out = new FileOutputStream(destFile);
            Key k = toKey(key);
            byte[] raw = k.getEncoded();
            SecretKeySpec secretKeySpec = new SecretKeySpec(raw, ALGORITHM);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
            CipherInputStream cin = new CipherInputStream(in, cipher);
            byte[] cache = new byte[CACHE_SIZE];
            int nRead = 0;
            while ((nRead = cin.read(cache)) != -1) {
                out.write(cache, 0, nRead);
                out.flush();
            }
            out.close();
            cin.close();
            in.close();
        }
    }

    /**
     * <p>
     * 解密
     * </p>
     *
     * @param data
     * @param key
     * @return
     * @throws Exception
     */
    public static byte[] decrypt(byte[] data, String key) throws Exception {
        Key k = toKey(key);
        byte[] raw = k.getEncoded();
        SecretKeySpec secretKeySpec = new SecretKeySpec(raw, ALGORITHM);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
        return cipher.doFinal(data);
    }

    /**
     * <p>
     * 文件解密
     * </p>
     *
     * @param key
     * @param in
     * @param destFilePath
     * @throws Exception
     */
    public static void decryptFile(String key, InputStream in, String destFilePath) throws Exception {
        File destFile = new File(destFilePath);

        if (!destFile.getParentFile().exists()) {
            destFile.getParentFile().mkdirs();
        } else {
            if (destFile.exists()) {
                destFile.delete();
            }
        }
        destFile.createNewFile();
        FileOutputStream out = new FileOutputStream(destFile);
        Key k = toKey(key);
        byte[] raw = k.getEncoded();
        SecretKeySpec secretKeySpec = new SecretKeySpec(raw, ALGORITHM);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
        CipherOutputStream cout = new CipherOutputStream(out, cipher);
        byte[] cache = new byte[CACHE_SIZE];
        int nRead = 0;
        while ((nRead = in.read(cache)) != -1) {
            cout.write(cache, 0, nRead);
            cout.flush();
        }
        cout.close();
        out.close();
        in.close();

    }

    /**
     * <p>
     * 转换密钥
     * </p>
     *
     * @param key
     * @return
     * @throws Exception
     */
    private static Key toKey(String key) throws Exception {
        byte[] keyData = hexStringToBytes(key);
        SecretKey secretKey = new SecretKeySpec(keyData, ALGORITHM);
        return secretKey;
    }

    public static final String bytesToHexString(byte[] buf) {
        StringBuilder sb = new StringBuilder(buf.length * 2);
        String tmp = "";
        // 将字节数组中每个字节拆解成2位16进制整数
        for (int i = 0; i < buf.length; i++) {
            // 1.
            // sb.append(Integer.toHexString((buf[i] & 0xf0) >> 4));
            // sb.append(Integer.toHexString((buf[i] & 0x0f) >> 0));
            // //////////////////////////////////////////////////////////////////
            // 2.sodino更喜欢的方式，嘿嘿...
            tmp = Integer.toHexString(0xff & buf[i]);
            tmp = tmp.length() == 1 ? "0" + tmp : tmp;
            sb.append(tmp.toUpperCase());
        }
        return sb.toString();
    }

    public static byte[] hexStringToBytes(String hexString) {
        char[] hex = hexString.toCharArray();
        // 转rawData长度减半
        int length = hex.length / 2;
        byte[] rawData = new byte[length];
        for (int i = 0; i < length; i++) {
            // 先将hex转10进位数值
            int high = Character.digit(hex[i * 2], 16);
            int low = Character.digit(hex[i * 2 + 1], 16);
            // 將第一個值的二進位值左平移4位,ex: 00001000 => 10000000 (8=>128)
            // 然后与第二个值的二进位值作联集ex: 10000000 | 00001100 => 10001100 (137)
            int value = (high << 4) | low;
            // 与FFFFFFFF作补集
            if (value > 127) {
                value -= 256;
            }
            // 最后转回byte就OK
            rawData[i] = (byte) value;
        }
        return rawData;
    }

}