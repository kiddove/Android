package com.kectech.android.wyslink.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.kectech.android.wyslink.R;
import com.kectech.android.wyslink.listitem.VideoListItem;
import com.kectech.android.wyslink.thirdparty.CacheBitmap.ImageFetcher;
import com.kectech.android.wyslink.thirdparty.CacheBitmap.RecyclingImageView;

import java.util.ArrayList;

/**
 * Created by Paul on 16/06/2015.
 * custom adapter for video tab listView
 */
public class VideoListViewAdapter extends ArrayAdapter<VideoListItem> {
    private  Context context;
    private ImageFetcher mImageFetcher;

    public VideoListViewAdapter(Context context, int resourceId, ArrayList<VideoListItem> items, ImageFetcher imageFetcher) {
        super(context, resourceId, items);
        this.context = context;
        this.mImageFetcher = imageFetcher;
    }

    // private view holder class
    private class ViewHolder {
        ImageView imageView;
        TextView txtTitle;
        TextView txtDesc;
        TextView txtLabel;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        VideoListItem item = getItem(position);

        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.video_list_item, parent, false);
            holder = new ViewHolder();
            holder.imageView = (RecyclingImageView) convertView.findViewById(R.id.hall_video_list_item_img);
            //holder.imageView = null;
            holder.txtTitle = (TextView) convertView.findViewById(R.id.hall_video_list_item_title);
            holder.txtDesc = (TextView) convertView.findViewById(R.id.hall_video_list_item_desc);
            holder.txtLabel = (TextView) convertView.findViewById(R.id.hall_video_list_item_label);
            convertView.setTag(holder);
        } else
            holder = (ViewHolder) convertView.getTag();

        holder.txtDesc.setText(item.getDesc());
        holder.txtTitle.setText(item.getTitle());
        holder.txtLabel.setText(item.getStreamType());
        mImageFetcher.loadImage(item.getThumbURL(), holder.imageView);
        //holder.imageView = null;
        if (item.getStreamType().compareToIgnoreCase("live") == 0) {
            holder.txtLabel.setBackgroundColor(context.getResources().getColor(R.color.ColorPrimaryDark));
        } else {// vod {
            holder.txtLabel.setBackgroundColor(context.getResources().getColor(R.color.video_list_item_label_background));
        }

        return convertView;
    }
}
