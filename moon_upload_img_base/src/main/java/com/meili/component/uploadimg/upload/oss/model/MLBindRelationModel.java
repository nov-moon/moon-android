package com.meili.component.uploadimg.upload.oss.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by imuto on 2018/4/2.
 */
public class MLBindRelationModel implements Parcelable {
    public String objectKey;
    public int index;
    public Long fileSize;
    public MLBindRelationResultModel resultModel;

    public MLBindRelationModel() {
    }

    protected MLBindRelationModel(Parcel in) {
        objectKey = in.readString();
        index = in.readInt();
        if (in.readByte() == 0) {
            fileSize = null;
        } else {
            fileSize = in.readLong();
        }
        if (in.readByte() == 0) {
            resultModel = null;
        } else {
            resultModel = (MLBindRelationResultModel) in.readSerializable();
        }
    }

    public static final Creator<MLBindRelationModel> CREATOR = new Creator<MLBindRelationModel>() {
        @Override
        public MLBindRelationModel createFromParcel(Parcel in) {
            return new MLBindRelationModel(in);
        }

        @Override
        public MLBindRelationModel[] newArray(int size) {
            return new MLBindRelationModel[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(objectKey);
        dest.writeInt(index);
        if (fileSize == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(fileSize);
        }
        if (resultModel == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeSerializable(resultModel);
        }
    }
}
