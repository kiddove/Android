package com.kectech.android.kectechapp.listitem;

/**
 * Created by Paul on 16/06/2015.
 * define a class for custom listView item
 */
public class VideoListItem {
    private int imgId;
    private String strTitle;
    private String strDesc;

    public VideoListItem(int imgid, String title, String desc) {
        this.imgId = imgid;
        this.strTitle = title;
        this.strDesc = desc;
    }

    public int getImageId() {
        return imgId;
    }

    public void setImageId(int imageId) {
        this.imgId = imageId;
    }

    public String getDesc() {
        return strDesc;
    }

    public void setDesc(String desc) {
        this.strDesc = desc;
    }

    public String getTitle() {
        return strTitle;
    }

    public void setTitle(String title) {
        this.strTitle = title;
    }

    @Override
    public String toString() {
        return strTitle + "\n" + strDesc;
    }
}
