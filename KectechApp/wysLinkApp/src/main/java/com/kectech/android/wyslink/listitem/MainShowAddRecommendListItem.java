package com.kectech.android.wyslink.listitem;

import com.kectech.android.wyslink.activity.MainActivity;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Created by Paul on 16/06/2015.
 * define a class for custom listView item
 */
public class MainShowAddRecommendListItem {
    private String title = null;
    private String description = null;
    private String thumb = null;
    private int id;
    private String showname = null;    // name
    private String owner = null;   // owner
    private boolean follow = false; // if followed this showroom

    public MainShowAddRecommendListItem(MainShowAddRecommendListItem item) {
        this.title = item.title;
        this.description = item.description;
        this.thumb = item.thumb;
        this.id = item.id;
        this.showname = item.showname;
        this.owner = item.owner;
        this.follow = item.follow;
    }

    public String getDescription() {
        if (description != null)
            return description.replace("<br/>", "\n");
        else
            return null;
    }

    public String getTitle() {
        if (title != null)
        return title.replace("<br/>", "\n");
        else
            return null;
    }

    public String getThumbURL() {
        try {
//            //return thumb;
//            switch (type) {
//                case 1:
//                    // public
//                    return null;
//                case 2:
//                    // show room
//                    return null;
//                case 3:
//                    // event hall
//                    return "http://www.kdlinx.com/EHLogo.ashx?type=0&owner=" + this.owner + "&eh=" + URLEncoder.encode(this.showname, MainActivity.ENCODING);
//                default:
//                    return null;
//            }
            // TODO: 15/04/2016 showroom icon http://206.190.141.88/simg.ashx?name=%E5%8A%A0%E6%8B%BF%E5%A4%A7%E5%9B%BD%E5%AE%B6%E7%94%B5%E8%A7%86%E5%8F%B0CNTV (name = showroom name...)
            return String.format("http://206.190.141.88/simg.ashx?name=%s", URLEncoder.encode(this.showname, MainActivity.ENCODING));
        } catch (UnsupportedEncodingException uee) {
            uee.printStackTrace();
            return null;
        } catch (NullPointerException npe) {
            npe.printStackTrace();
            return null;
        }
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return showname;
    }

    public boolean isFollow() {
        return follow;
    }
    @Override
    public String toString() {
        return title + "\n" + description;
    }

    public String getMemo() {

        // todo owner@owner.com
        int p = owner.indexOf("@");
        return "Showroom " + "@" + owner.substring(0, p);
//        switch (type) {
//            case 1:
//                // public
//                return "Publicshow " + "@" + owner.substring(0, p);
//            case 2:
//                // show room
//                return "Showroom " + "@" + owner.substring(0, p);
//            case 3:
//                // event hall
//                return "EventHall " + "@" + owner.substring(0, p);
//            default:
//                return null;
//        }
    }
}
