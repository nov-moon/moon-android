package com.meili.component.uploadimg;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.meili.component.uploadimg.common.IRetryHandler;
import com.mljr.moon.imgcompress.compressrule.ICompressRule;

/**
 * 自定义当前上传参数
 * Created by imuto on 17/11/24.
 */
public class MLUploadOption implements Parcelable {

    /**
     * 初始化对象
     *
     * @param idPrefix 上传图片id的前缀，最终id格式为：channelId + / + 前缀 + 文件md5 + 当前时间戳 + . + 文件后缀
     */
    public MLUploadOption(String idPrefix) {
        this.idPrefix = idPrefix;
    }

    /**
     * 初始化对象
     */
    public MLUploadOption() {
        this("");
    }

    /**
     * 上传图片id的前缀，最大不能超过700字节
     * <p>
     * 最终id格式为：channelId + / + 前缀 + 文件md5 + 当前时间戳 + 上传平台标示(android端为:0) + . + 文件后缀
     */
    public String idPrefix;

    /**
     * 是否开启压缩， 如果不开启压缩，则{@link #compressOptions}参数设置无效，默认开启。
     * <p>
     * 如果开启了压缩，但是{@link #compressOptions}=null，则使用默认配置的option进行压缩，
     * <p>
     * 如果默认配置为null，则不进行压缩
     */
    public boolean isCompress = true;

    /** 压缩算法的配置选项，依赖{@link #isCompress}参数开启 */
    public ICompressRule compressOptions;

    /** 是否删除上传成功后的压缩图片，默认使用全局配置，全局配置默认为true */
    public boolean isAutoDelCompressCache = true;

    /**
     * 默认的最大上传重试次数，如果此参数<0，则参数设置失效，如果=0，则不做重试
     * <p>
     * 如果设置了{@link #uploadRetryHandler}，则此参数设置无效，使用handler配置
     */
    public int maxRetryTimes = -1;

    /** 上传的重试配置 */
    public IRetryHandler uploadRetryHandler;

    /** 验证参数有效性 */
    public String validate() {
        if (!TextUtils.isEmpty(idPrefix)) {
            if (idPrefix.getBytes().length > 700) {
                return "idPrefix最大长度不能超过700字节";
            }
        }
        return null;
    }

    protected MLUploadOption(Parcel in) {
        idPrefix = in.readString();
        isCompress = in.readByte() != 0;
        maxRetryTimes = in.readInt();
        uploadRetryHandler = (IRetryHandler) in.readSerializable();
        compressOptions = (ICompressRule) in.readSerializable();
    }

    public static final Creator<MLUploadOption> CREATOR = new Creator<MLUploadOption>() {
        @Override
        public MLUploadOption createFromParcel(Parcel in) {
            return new MLUploadOption(in);
        }

        @Override
        public MLUploadOption[] newArray(int size) {
            return new MLUploadOption[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(idPrefix);
        dest.writeByte((byte) (isCompress ? 1 : 0));
        dest.writeInt(maxRetryTimes);
        dest.writeSerializable(uploadRetryHandler);
        dest.writeSerializable(compressOptions);
    }

}
