package com.kectech.android.kectechapp.listitem;

import android.graphics.Bitmap;

/**
 * Created by Paul on 16/06/2015.
 * define a class for custom listView item
 */
public class VideoListItem {
    private Bitmap bitmap = null;
    private String icon;
    private String title;
    private String description;
    private int position;
    private String url;
    private int id;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
    public Bitmap getImage() {
        return bitmap;
    }

    public void setImage(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public String getDesc() {
        return description;
    }

    public void setDesc(String desc) {
        this.description = desc;
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
        return icon;
    }

    public void setThumbURL(String thumbURL) {
        this.icon = thumbURL;
    }

    public String getVideoUrl() {
        //return "http://192.168.9.40/demo/test.html";
        return url;
    }

    public void setVideoUrl(String videoUrl) {
        this.url = videoUrl;
    }
    @Override
    public String toString() {
        return title + "\n" + description;
    }
}
