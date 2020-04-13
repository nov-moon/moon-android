package com.meili.component.uploadimg;

import java.io.File;

/**
 * 支持文件格式转换器
 * Created by imuto on 18/1/16.
 */
public interface MLUploadFileConverter {
    /** 指定文件是否匹配当前转换器 */
    boolean match(File file);

    /** 转换指定文件，并返回转换后的文件。如果转换失败，则返回null */
    File convert(File file);
}
