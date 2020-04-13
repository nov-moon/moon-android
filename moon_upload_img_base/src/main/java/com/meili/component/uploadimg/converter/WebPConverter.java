package com.meili.component.uploadimg.converter;

import com.meili.component.uploadimg.MLUploadFileConverter;
import com.meili.moon.sdk.util.IOUtil;

import java.io.File;
import java.io.FileInputStream;

/**
 * 默认支持的转换器，支持png和jpg
 * Created by imuto on 18/1/16.
 */
public class WebPConverter implements MLUploadFileConverter {
    // webp 头文件格式，RIFF+4字节文件大小+WEBP
    // 0                   1                   2                   3
    // 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
    // +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
    // |      'R'      |      'I'      |      'F'      |      'F'      |
    // +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
    // |                           File Size                           |
    // +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
    // |      'W'      |      'E'      |      'B'      |      'P'      |
    // +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
    //riff标示
    private static final int[] RIFF_FILE_HEADER = new int[]{0x52, 0x49, 0X46, 0x46};
    //webp标示
    private static final int[] WEBP_FILE_HEADER = new int[]{0X57, 0x45, 0x42, 0x50};
    //fileSize部分长度
    private static final int FILE_SIZE_LENGTH = 4;

    @Override
    public boolean match(File file) {
        FileInputStream stream = null;
        try {
            stream = new FileInputStream(file);
            byte[] bytes = IOUtil.readBytes(stream, 0, RIFF_FILE_HEADER.length);
            if (!ConverterManager.byteArrayEquals(bytes, RIFF_FILE_HEADER)) {
                return false;
            }
            bytes = IOUtil.readBytes(stream, FILE_SIZE_LENGTH, WEBP_FILE_HEADER.length);
            if (!ConverterManager.byteArrayEquals(bytes, WEBP_FILE_HEADER)) {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            IOUtil.closeQuietly(stream);
        }
        return true;
    }

    @Override
    public File convert(File file) {
        return ConverterManager.convertFile2Jpg(file);
    }

}
