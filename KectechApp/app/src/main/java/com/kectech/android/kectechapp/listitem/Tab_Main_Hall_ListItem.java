package com.kectech.android.kectechapp.listitem;

import android.graphics.Bitmap;

/**
 * Created by Paul on 16/06/2015.
 * define a class for custom listView item
 */
public class Tab_Main_Hall_ListItem {
    private Bitmap bitmap = null;
    private String title;
    private String desc;
    private int type;
    private int position;
    private String thumb = null;
    private int id;

    public Tab_Main_Hall_ListItem(Tab_Main_Hall_ListItem item) {
        this.title = item.title;
        this.desc = item.desc;
        this.type = item.type;
        this.position = item.position;
        this.thumb = item.thumb;
        this.id = item.id;
    }

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

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
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
    @Override
    public String toString() {
        return title + "\n" + desc;
    }
}
