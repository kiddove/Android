package com.kectech.android.kectechapp.listitem;

import android.graphics.Bitmap;
import android.support.annotation.Nullable;

/**
 * Created by Paul on 25/06/2015.
 * define the class use for custom ListView items for photo tab
 * title, desc, image(s), actually thumbs, image url(s)
 */
public class PhotoListItem {

    private Bitmap thumbNail = null;
    private String title = null;
    private String desc = null;
    private String image = null;
    private String thumb = null;
    private int position = 0;

    public PhotoListItem(@Nullable String imageURL, @Nullable String thumbURL, @Nullable String title, @Nullable String description, @Nullable Bitmap thumbNail, @Nullable int position) {
        this.thumbNail = thumbNail;
        this.title = title;
        this.desc = description;
        this.image = imageURL;
        this.thumb = thumbURL;
        this.position = position;
    }
//    public PhotoListItem(PhotoListItem item) {
//        this.thumbNail = item.thumbNail;
//        this.title = item.title;
//        this.description = item.description;
//        this.imageURL = item.imageURL;
//        this.thumbURL = item.thumbURL;
//        this.position = item.position;
//    }

    public Bitmap getThumbNail() {
        return thumbNail;
    }

    public void setThumbNail(Bitmap thumbNail) {
        this.thumbNail = thumbNail;
    }

    public String getDescription() {
        return desc;
    }

    public void setDescription(String description) {
        this.desc = description;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImageURL() {
        return image;
    }

    public void setImageURL(String imageUrl) {
        this.image = imageUrl;
    }

    public String getThumbURL() {
        return thumb;
    }

    public void setThumbURL(String thumbURL) {
        this.thumb = thumbURL;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }
}
