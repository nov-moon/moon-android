package com.meili.component.uploadimg;

import java.io.File;
import java.io.Serializable;

/**
 * 压缩结果信息Model接口
 * Created by imuto on 17/12/22.
 */
public interface MLCompressInfoModel extends Serializable {

    /** 是否被压缩，isCompress：1.压缩，0.没有压缩 */
    void setIsCompress(int isCompress);

    /** 压缩后大小，如果没有压缩，则此值=原始大小，compressSize：具体大小 */
    void setCompressSize(long compressSize);

    /** 获取压缩后大小 */
    long getCompressSize();

    /** 压缩后文件Md5值，如果没有压缩，则为原文件MD5 */
    void setCompressHashCode(String compressHashCode);

    /** 原始大小 */
    void setOriginSize(long originSize);

    /** 原始文件md5 */
    void setOriginHashCode(String originHashCode);


    /** 可获取压缩后图片路径，如果设置了自动删除，则本file.exits = false */
    interface MLCompressInfoWithFileModel extends MLCompressInfoModel {
        void setCompressResultFile(File compressResultFile);
    }
}
