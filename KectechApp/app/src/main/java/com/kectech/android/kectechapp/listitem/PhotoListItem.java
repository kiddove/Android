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
    private int position = 0;
    public ArrayList<PhotoListItemDetail> items = null;


    public PhotoListItem() {

    }
//    public PhotoListItem(PhotoListItem item) {
//        this.thumbNail = item.thumbNail;
//        this.title = item.title;
//        this.description = item.description;
//        this.imageURL = item.imageURL;
//        this.thumbURL = item.thumbURL;
//        this.position = item.position;
//    }

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

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }
}
