package com.halove.xyp.retrofitdemo;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by xyp on 2018/3/14.
 */

public class Title implements Serializable {
    private String name;

    @SerializedName("ptype")
    private int type;

    private String icon;
    private String img;

    @SerializedName("sortid")
    private int sortId;

    @SerializedName("ispreview")
    private int isPreview;

    @SerializedName("previewuthority")
    private int previewLevel;

    public int getIsPreview() {
        return isPreview;
    }

    public void setIsPreview(int isPreview) {
        this.isPreview = isPreview;
    }

    public int getPreviewLevel() {
        return previewLevel;
    }

    public void setPreviewLevel(int previewLevel) {
        this.previewLevel = previewLevel;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public int getSortId() {
        return sortId;
    }

    public void setSortId(int sortId) {
        this.sortId = sortId;
    }
}
