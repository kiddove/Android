package com.kectech.android.kectechapp.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.kectech.android.kectechapp.R;
import com.kectech.android.kectechapp.listitem.VideoListItem;
import com.kectech.android.kectechapp.thirdparty.CacheBitmap.ImageFetcher;
import com.kectech.android.kectechapp.thirdparty.CacheBitmap.RecyclingImageView;

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
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        VideoListItem item = getItem(position);

        LayoutInflater layoutInflater = (LayoutInflater)context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.video_list_item, parent, false);
            holder = new ViewHolder();
            holder.imageView = (RecyclingImageView)convertView.findViewById(R.id.hall_video_list_item_img);
            //holder.imageView = null;
            holder.txtTitle = (TextView)convertView.findViewById(R.id.hall_video_list_item_title);
            holder.txtDesc = (TextView)convertView.findViewById(R.id.hall_video_list_item_desc);
            convertView.setTag(holder);
        }
        else
            holder = (ViewHolder)convertView.getTag();

        holder.txtDesc.setText(item.getDesc());
        holder.txtTitle.setText(item.getTitle());
        //holder.imageView.setImageBitmap(item.getImage());
        mImageFetcher.loadImage(item.getThumbURL(), holder.imageView);
        //holder.imageView = null;

        return convertView;
    }
}
