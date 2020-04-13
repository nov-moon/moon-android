package com.meili.component.uploadimg;

import java.io.Serializable;

/**
 * 上传图片的标准model
 * Created by imuto on 17/11/10.
 */
public interface MLUploadModel extends Serializable {
    /** 获取上传文件的文件路径 */
    String getUploadFilePath();

    /**
     * 设置上传结果
     *
     * @param uploadTargetId  上传文件的id
     * @param uploadTargetUrl 上传文件的url
     */
    void setUploadResult(String uploadTargetId, String uploadTargetUrl);

    /**获取id*/
    String getUploadTargetId();

    /**获取url*/
    String getUploadTargetUrl();

    /** 压缩后大小，如果没有压缩，则此值=原始大小，compressSize：具体大小 */
    void setCompressSize(long compressSize);

    /** 获取压缩后大小 */
    long getCompressSize();

    /** 默认的MLUploadModel的实现类 */
    public class DefUploadModelImpl implements MLUploadModel, MLCompressInfoModel {
        private String filePath;
        private String uploadTargetId;
        private String uploadTargetUrl;
        private int isCompress;
        private long compressSize;
        private String compressHashCode;
        private long originSize;
        private String originHashCode;

        public DefUploadModelImpl(String filePath) {
            this.filePath = filePath;
        }

        @Override
        public String getUploadFilePath() {
            return filePath;
        }

        @Override
        public void setUploadResult(String uploadTargetId, String uploadTargetUrl) {
            this.uploadTargetId = uploadTargetId;
            this.uploadTargetUrl = uploadTargetUrl;
        }

        public String getFilePath() {
            return filePath;
        }

        public void setFilePath(String filePath) {
            this.filePath = filePath;
        }

        @Override
        public String getUploadTargetId() {
            return uploadTargetId;
        }

        public void setUploadTargetId(String uploadTargetId) {
            this.uploadTargetId = uploadTargetId;
        }

        @Override
        public String getUploadTargetUrl() {
            return uploadTargetUrl;
        }

        public void setUploadTargetUrl(String uploadTargetUrl) {
            this.uploadTargetUrl = uploadTargetUrl;
        }

        @Override
        public String toString() {
            return "DefUploadModelImpl{" +
                    "filePath='" + filePath + '\'' +
                    ", imgId='" + uploadTargetId + '\'' +
                    ", imgUrl='" + uploadTargetUrl + '\'' +
                    '}';
        }

        public int getIsCompress() {
            return isCompress;
        }

        @Override
        public void setIsCompress(int isCompress) {
            this.isCompress = isCompress;
        }

        public long getCompressSize() {
            return compressSize;
        }

        @Override
        public void setCompressSize(long compressSize) {
            this.compressSize = compressSize;
        }

        public String getCompressHashCode() {
            return compressHashCode;
        }

        @Override
        public void setCompressHashCode(String compressHashCode) {
            this.compressHashCode = compressHashCode;
        }

        public long getOriginSize() {
            return originSize;
        }

        @Override
        public void setOriginSize(long originSize) {
            this.originSize = originSize;
        }

        public String getOriginHashCode() {
            return originHashCode;
        }

        @Override
        public void setOriginHashCode(String originHashCode) {
            this.originHashCode = originHashCode;
        }
    }
}
