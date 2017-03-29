package com.myimageselectcontainer.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 单个文件夹下的图片信息
 */

public class ImageBean implements Parcelable {
    private String path;
    private boolean isSelect = false;


    public static final Creator<ImageBean> CREATOR = new Creator<ImageBean>() {
        @Override
        public ImageBean createFromParcel(Parcel in) {
            ImageBean bean = new ImageBean();
            bean.path = in.readString();
            //1: true  0:false
            bean.isSelect = in.readByte() != 0;
            return bean;
        }

        @Override
        public ImageBean[] newArray(int size) {
            return new ImageBean[size];
        }
    };

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public boolean isSelect() {
        return isSelect;
    }

    public void setSelect(boolean select) {
        isSelect = select;
    }

    @Override
    public String toString() {
        return "ImageBean{" +
                "path='" + path + '\'' +
                ", isSelect=" + isSelect +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(path);
        //1: true  0:false
        dest.writeByte((byte) (isSelect ? 1 : 0));
    }
}
