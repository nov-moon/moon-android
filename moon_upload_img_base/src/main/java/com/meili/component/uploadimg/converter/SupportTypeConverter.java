package com.meili.component.uploadimg.converter;

import com.meili.component.uploadimg.MLUploadFileConverter;
import com.meili.moon.sdk.util.IOUtil;

import java.io.File;
import java.io.FileInputStream;

/**
 * 默认支持的转换器，支持png和jpg
 * Created by imuto on 18/1/16.
 */
public class SupportTypeConverter implements MLUploadFileConverter {

    //png头文件格式
    private static final int[] PNG_FILE_HEADER = new int[]{0x89, 0x50, 0X4E, 0x47, 0X0D, 0X0A, 0X1A, 0X0A};
    //jpg头文件
    private static final int[] JPEG_FILE_HEADER = new int[]{0XFF, 0xD8};
//    //jpg文件尾
//    private static final int[] JPEG_FILE_FOOTER = new int[]{0XFF, 0xD9};

    @Override
    public boolean match(File file) {
        return matchFile(file, PNG_FILE_HEADER) || matchFile(file, JPEG_FILE_HEADER);
    }

    /** 检查文件头是否符合png格式 */
    private boolean matchFile(File file, int[] headers) {
        FileInputStream stream = null;
        try {
            stream = new FileInputStream(file);
            byte[] bytes = IOUtil.readBytes(stream, 0, headers.length);
            if (!ConverterManager.byteArrayEquals(bytes, headers)) {
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
        return file;
    }

}
