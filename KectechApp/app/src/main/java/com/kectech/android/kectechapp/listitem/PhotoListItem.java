package com.kectech.android.kectechapp.listitem;

import java.util.ArrayList;

/**
 * Created by Paul on 25/06/2015.
 * define the class use for custom ListView items for photo tab
 * title, desc, image(s), actually thumbs, image url(s)
 * multiple images and thumbnails
 */
public class PhotoListItem {

    private String title = null;
    private String desc = null;
    public ArrayList<PhotoListItemDetail> items = null;

    public PhotoListItem() {

    }

    public String getDescription() {
        return desc;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

}
