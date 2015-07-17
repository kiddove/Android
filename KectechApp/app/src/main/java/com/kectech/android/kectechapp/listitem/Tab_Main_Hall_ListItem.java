package com.kectech.android.kectechapp.listitem;

import android.graphics.Bitmap;

import com.kectech.android.kectechapp.activity.MainActivity;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;

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
    private String keyid;
    private String follow;

    public Tab_Main_Hall_ListItem(Tab_Main_Hall_ListItem item) {
        this.title = item.title;
        this.desc = item.desc;
        this.type = item.type;
        this.position = item.position;
        this.thumb = item.thumb;
        this.id = item.id;
        this.keyid = item.keyid;
        this.follow = item.follow;
    }

    public Bitmap getImage() {
        return bitmap;
    }

    public void setImage(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public String getDesc() {
        return desc.replace("<br/>", "\n");
    }

    public String getTitle() {
        return title.replace("<br/>", "\n");
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getType() {
        return type;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public String getThumbURL() {
        try {
            //return thumb;
            switch (type) {
                case 1:
                    // public
                    return null;
                case 2:
                    // show room
                    return null;
                case 3:
                    // event hall
                    return "http://www.kdlinx.com/EHLogo.ashx?type=0&owner=" + this.follow + "&eh=" + URLEncoder.encode(this.keyid, MainActivity.ENCODING);
                default:
                    return null;
            }
        } catch (UnsupportedEncodingException uee) {
            uee.printStackTrace();
            return null;
        }
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return keyid;
    }

    public String getFollow() {
        return follow;
    }
    @Override
    public String toString() {
        return title + "\n" + desc;
    }
}
