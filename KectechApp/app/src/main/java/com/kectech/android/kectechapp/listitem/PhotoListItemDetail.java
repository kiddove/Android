package com.kectech.android.kectechapp.listitem;

/**
 * Created by Paul on 03/07/2015.
 * no bitmap here anymore
 */
public class PhotoListItemDetail {

    private String image = null;
    private String thumb = null;

    public String getImageURL() {
        return image.replaceAll(" ", "%20");
    }

    public String getThumbURL() {
        return thumb.replaceAll(" ", "%20");
    }

}