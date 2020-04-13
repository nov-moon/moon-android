package com.meili.component.uploadimg.converter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.meili.component.uploadimg.MLConfig;
import com.meili.component.uploadimg.MLUploadFileConverter;
import com.meili.moon.sdk.CommonSdk;
import com.meili.moon.sdk.log.LogUtil;
import com.meili.moon.sdk.util.IOUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

/**
 * convert的管理类
 * Created by imuto on 18/1/16.
 */
public class ConverterManager {

    /** 使用转化器列表，转化一个文件到支持格式 */
    public static File convert(File file, MLConfig config) {
        for (MLUploadFileConverter item : config.getUploadFileConverter()) {
            if (item.match(file)) {
                return item.convert(file);
            }
        }
        LogUtil.e("当前为不支持的文件类型！！file = " + file.getAbsolutePath());
        return file;
//        throw new ConvertFileException("不支持的文件类型");
    }

    /** 转换系统支持的图片为jpg格式 */
    /*package*/
    static File convertFile2Jpg(File file) {
        Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
        if (bitmap == null) {
            return null;
        }
        StringBuilder fileName = new StringBuilder();
        if (file.getName().contains(".")) {
            fileName.append(file.getName().substring(0, file.getName().lastIndexOf(".")));
        } else {
            fileName.append(file.getName());
        }
        fileName.append("_")
                .append(System.currentTimeMillis())
                .append(".jpg");

        File outFile = new File(CommonSdk.app().getCacheDir(), fileName.toString());
        if (outFile.exists()) {
            outFile.delete();
        }
        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(outFile);
            LogUtil.e("原图大小：" + file.length());
            bitmap.compress(Bitmap.CompressFormat.JPEG, 95, outputStream);
            LogUtil.e("转换大小：" + outFile.length());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        } finally {
            IOUtil.closeQuietly(outputStream);
        }
        bitmap.recycle();
        return outFile;
    }

    /** 验证两个数组是否相等，对byte数组做了无符号处理 */
    /*package*/
    static boolean byteArrayEquals(byte[] bytes, int[] other) {
        for (int i = 0; i < bytes.length; i++) {
            //转换byte为无符号值
            if ((bytes[i] & 0xff) != other[i]) {
                return false;
            }
        }
        return true;
    }


}
