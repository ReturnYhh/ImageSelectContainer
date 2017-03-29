package com.myimageselectcontainer.bean;

/**
 * 图片文件夹信息.
 */

public class ImageDirBean {
    //文件夹路径
    private String dir;
    //图片路径
    private String  imagePath;
    //图片名称
    private String imageName;
    //文件夹下的图片数量
    private int imageCount;

    public String getDir() {
        return dir;
    }

    public void setDir(String dir) {
        this.dir = dir;
        int lastIndexOf = this.dir.lastIndexOf("/");
        this.imageName = this.dir.substring(lastIndexOf);
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public int getImageCount() {
        return imageCount;
    }

    public void setImageCount(int imageCount) {
        this.imageCount = imageCount;
    }

    @Override
    public String toString() {
        return "ImageDirBean{" +
                "dir='" + dir + '\'' +
                ", imagePath='" + imagePath + '\'' +
                ", imageName='" + imageName + '\'' +
                ", imageCount=" + imageCount +
                '}';
    }
}
