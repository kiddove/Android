package com.kectech.android.kectechapp.adapter;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.kectech.android.kectechapp.R;
import com.kectech.android.kectechapp.activity.MainActivity;
import com.kectech.android.kectechapp.thirdparty.CacheBitmap.ImageFetcher;
import com.kectech.android.kectechapp.thirdparty.CacheBitmap.RecyclingImageView;

import java.util.ArrayList;

/**
 * Created by Paul on 03/09/2015.
 * adapter for new post grid
 */
public class NewPostGridAdapter extends ArrayAdapter<String>{
    private ImageFetcher mImageFetcher;
    private Context mContext;

    public NewPostGridAdapter(Context context, int resourceId, ArrayList<String> items, ImageFetcher imageFetcher) {
        super(context, resourceId, items);
        this.mContext = context;
        this.mImageFetcher = imageFetcher;
    }

    // private view holder class
    private static class ViewHolder {
        RecyclingImageView imageView;
        ImageView removeButton;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        String strUrl = getItem(position);

        LayoutInflater layoutInflater = (LayoutInflater)mContext.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

        try {
            if (convertView == null) {
                convertView = layoutInflater.inflate(R.layout.new_post_grid_item, parent, false);
                holder = new ViewHolder();
                holder.imageView = (RecyclingImageView) convertView.findViewById(R.id.item_image);
                holder.removeButton = (ImageView)convertView.findViewById(R.id.item_remove);
                convertView.setTag(holder);
            } else
                holder = (ViewHolder) convertView.getTag();
        } catch (Exception e) {
            Log.e(MainActivity.LOG_TAG, "Exception caught(NewPostGridAdapter): " + e.getMessage());
            return convertView;
        }

        if (strUrl.compareToIgnoreCase(MainActivity.NEW_POST_DEFAULT_IMAGE) == 0) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                holder.imageView.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_post_default_img, mContext.getTheme()));
                holder.imageView.setBackground(mContext.getResources().getDrawable(R.drawable.new_post_image_background_frame, mContext.getTheme()));
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                holder.imageView.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_post_default_img));
                holder.imageView.setBackground(mContext.getResources().getDrawable(R.drawable.new_post_image_background_frame));
            } else {
                holder.imageView.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_post_default_img));
                holder.imageView.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.new_post_image_background_frame));
            }
            holder.removeButton.setVisibility(View.GONE);
        } else {
            mImageFetcher.loadImage(strUrl, holder.imageView);
            holder.removeButton.setVisibility(View.VISIBLE);
        }

        return convertView;
    }

}
