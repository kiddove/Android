package com.kectech.android.wyslink.listitem;

import com.kectech.android.wyslink.activity.MainActivity;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Created by Paul on 16/06/2015.
 * define a class for custom listView item
 */
public class Tab_Main_Hall_ListItem {
    private String title = null;
    private String desc = null;
    private int type;
    private String thumb = null;
    private int id;
    private String keyid = null;
    private String follow = null;

    public Tab_Main_Hall_ListItem(Tab_Main_Hall_ListItem item) {
        this.title = item.title;
        this.desc = item.desc;
        this.type = item.type;
        this.thumb = item.thumb;
        this.id = item.id;
        this.keyid = item.keyid;
        this.follow = item.follow;
    }

    public String getDesc() {
        return desc.replace("<br/>", "\n");
    }

    public String getTitle() {
        return title.replace("<br/>", "\n");
    }

    public int getType() {
        return type;
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

    public String getMemo() {

        // todo follow@follow.com
        int p = follow.indexOf("@");
        switch (type) {
            case 1:
                // public
                return "Publicshow " + "@" + follow.substring(0, p);
            case 2:
                // show room
                return "Showroom " + "@" + follow.substring(0, p);
            case 3:
                // event hall
                return "EventHall " + "@" + follow.substring(0, p);
            default:
                return null;
        }
    }
}
