package com.kectech.android.kectechapp.listitem;

import android.graphics.Bitmap;

/**
 * Created by Paul on 03/07/2015.
 */
public class PhotoListItemDetail {

    private Bitmap thumbNail = null;
    private String image = null;
    private String thumb = null;

    public String getImageURL() {
        return image.replaceAll(" ", "%20");
    }

    public void setImageURL(String imageUrl) {
        this.image = imageUrl;
    }

    public String getThumbURL() {
        return thumb.replaceAll(" ", "%20");
    }

    public void setThumbURL(String thumbURL) {
        this.thumb = thumbURL;
    }

    public Bitmap getThumbNail() {
        return thumbNail;
    }

    public void setThumbNail(Bitmap thumbNail) {
        this.thumbNail = thumbNail;
    }

}