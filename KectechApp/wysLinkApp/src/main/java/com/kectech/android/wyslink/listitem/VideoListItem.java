package com.kectech.android.wyslink.listitem;

/**
 * Created by Paul on 16/06/2015.
 * define a class for custom listView item
 */
public class VideoListItem {
    //private Bitmap bitmap = null;
    private String icon = null;
    private String title = null;
    private String description = null;
    private String url = null;
    private String id = null;
    private String datetime = null;
    // vod(duration  1:10 ) or live
    private String streamType = "LIVE";

    public String getDatetime() {
        return datetime;
    }

    public String getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getThumbURL() {
        return icon.replaceAll(" ", "%20");
    }

    public String getVideoUrl() {
        //return "http://192.168.9.40/demo/test.html";
        //return "http://192.168.9.40/demo/webview.html";
        return url;
    }

    public String getStreamType() {
        return streamType.toUpperCase();
    }

    @Override
    public String toString() {
        return title + "\n" + description;
    }
}
