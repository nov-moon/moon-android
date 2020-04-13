package com.meili.component.uploadimg.converter;

import com.meili.component.uploadimg.MLUploadFileConverter;
import com.meili.moon.sdk.util.IOUtil;

import java.io.File;
import java.io.FileInputStream;

/**
 * bmp图片格式转换器
 * Created by imuto on 18/1/17.
 */
public class BMPConverter implements MLUploadFileConverter {
    // bmp 头文件格式，MB+4字节文件大小+4字节保留位(必须全部为0)
    // 0                   1
    // 0 1 2 3 4 5 6 7 8 0 1 2 3 4 5 6 7 8
    // +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
    // |      'M'      |    'B'          |
    // +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+—+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
    // |       4字节 File Size                                                |
    // +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+—+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
    // |    2字节保留位必须为0             |       2字节保留位必须为0             |
    // +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
    //BM标示
    private static final int[] BM_FILE_HEADER = new int[]{0x42, 0x4D};
    //保留位
    private static final int[] KEEP_FILE_HEADER = new int[]{0X00, 0x00, 0x00, 0x00};
    //fileSize部分长度
    private static final int FILE_SIZE_LENGTH = 4;

    @Override
    public boolean match(File file) {
        FileInputStream stream = null;
        try {
            stream = new FileInputStream(file);
            byte[] bytes = IOUtil.readBytes(stream, 0, BM_FILE_HEADER.length);
            if (!ConverterManager.byteArrayEquals(bytes, BM_FILE_HEADER)) {
                return false;
            }
            bytes = IOUtil.readBytes(stream, FILE_SIZE_LENGTH, KEEP_FILE_HEADER.length);
            if (!ConverterManager.byteArrayEquals(bytes, KEEP_FILE_HEADER)) {
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
