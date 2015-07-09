package com.kectech.android.kectechapp.listitem;

import android.graphics.Bitmap;

/**
 * Created by Paul on 16/06/2015.
 * define a class for custom listView item
 */
public class VideoListItem {
    private Bitmap bitmap = null;
    private String thumb;
    private String title;
    private String desc;
    private int position;
    private String url;

    public Bitmap getImage() {
        return bitmap;
    }

    public void setImage(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public String getThumbURL() {
        return thumb;
    }

    public void setThumbURL(String thumbURL) {
        this.thumb = thumbURL;
    }

    public String getVideoUrl() {
        return url;
    }

    public void setVideoUrl(String videoUrl) {
        this.url = videoUrl;
    }
    @Override
    public String toString() {
        return title + "\n" + desc;
    }
}
