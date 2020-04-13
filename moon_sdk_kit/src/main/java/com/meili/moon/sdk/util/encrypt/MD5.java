package com.meili.moon.sdk.util.encrypt;

import com.meili.moon.sdk.util.StringUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static com.meili.moon.sdk.util.ArrayUtil.isEmpty;
import static com.meili.moon.sdk.util.IOUtil.closeQuietly;


/**
 * Created by imuto on 15/8/13.
 */
public class MD5 {

    public static String getFileMD5String(File file) throws IOException {
        if (file == null || !file.exists()) return null;
        MessageDigest messagedigest = null;
        FileInputStream in = null;
        FileChannel ch = null;
        byte[] encodeBytes = null;
        try {
            messagedigest = MessageDigest.getInstance("MD5");
            in = new FileInputStream(file);
            ch = in.getChannel();
            MappedByteBuffer byteBuffer = ch.map(FileChannel.MapMode.READ_ONLY, 0, file.length());
            messagedigest.update(byteBuffer);
            encodeBytes = messagedigest.digest();
        } catch (NoSuchAlgorithmException neverHappened) {
            throw new RuntimeException("MD5FileUtil messagedigest初始化失败", neverHappened);
        } finally {
            closeQuietly(in);
            closeQuietly(ch);
        }

        return StringUtil.toHexString(encodeBytes);
    }

    public static String md5(String string) {
        if (isEmpty(string)) return string;
        byte[] encodeBytes = null;
        try {
            encodeBytes = MessageDigest.getInstance("MD5").digest(string.getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException neverHappened) {
            throw new RuntimeException("Huh, MD5 should be supported?", neverHappened);
        } catch (UnsupportedEncodingException neverHappened) {
            throw new RuntimeException("Huh, UTF-8 should be supported?", neverHappened);
        }

        return StringUtil.toHexString(encodeBytes);
    }
}
