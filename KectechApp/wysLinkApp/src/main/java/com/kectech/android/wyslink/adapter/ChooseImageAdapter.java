package com.kectech.android.wyslink.adapter;


import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.kectech.android.kectechapp.R;
import com.kectech.android.wyslink.activity.MainActivity;
import com.kectech.android.wyslink.listitem.ChooseImageGridItem;
import com.kectech.android.wyslink.thirdparty.CacheBitmap.ImageFetcher;
import com.kectech.android.wyslink.thirdparty.CacheBitmap.RecyclingImageView;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by Paul on 21/08/2015.
 * used in choose image activity gridView
 */
public class ChooseImageAdapter extends ArrayAdapter<ChooseImageGridItem> {

    private ImageFetcher mImageFetcher;
    private Context mContext;

    private LinkedHashMap<Integer, Boolean> selection = new LinkedHashMap<>();
    public ChooseImageAdapter(Context context, int resourceId, ArrayList<ChooseImageGridItem> items, ImageFetcher imageFetcher) {
        super(context, resourceId, items);
        this.mContext = context;
        this.mImageFetcher = imageFetcher;
    }

    // private view holder class
    private static class ViewHolder {
        ImageView imageView;
        ImageView imageCheck;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        ChooseImageGridItem item = getItem(position);

        LayoutInflater layoutInflater = (LayoutInflater)mContext.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

        try {
            if (convertView == null) {
                convertView = layoutInflater.inflate(R.layout.choose_image_list_item, parent, false);
                holder = new ViewHolder();
                holder.imageView = (RecyclingImageView) convertView.findViewById(R.id.item_image);
                holder.imageCheck = (ImageView) convertView.findViewById(R.id.item_check);
                convertView.setTag(holder);
            } else
                holder = (ViewHolder) convertView.getTag();
        } catch (Exception e) {
            Log.e(MainActivity.LOG_TAG, "Exception caught(ChooseImageAdapter): " + e.getMessage());
            return convertView;
        }

        if(isChecked(position)) {
            holder.imageCheck.setSelected(true);
        } else {
            holder.imageCheck.setSelected(false);
        }

        mImageFetcher.loadImage(item.getImageURL(), holder.imageView);

        return convertView;
    }

    public void setSelection(int position, boolean value) {
        selection.put(position, value);
    }

    public boolean isChecked(int position) {
        Boolean result = selection.get(position);
        return result == null ? false : result;
    }

    public void removeSelect(int position) {
        selection.remove(position);
    }

//    public void clearSelection() {
//        selection.clear();
//    }

    public boolean isSelectionEmpty() {
        return selection.size() == 0;
    }

    public ArrayList<String> getSelection() {
        if (isSelectionEmpty())
            return null;
        ArrayList<String> result = new ArrayList<>();
        for (Map.Entry<Integer, Boolean> entry : selection.entrySet()) {
            int key = entry.getKey();
            result.add(getItem(key).getImageURL());
        }

        return result;
    }


    public void changeSelection(View v, boolean bCheck) {
        ((ViewHolder)v.getTag()).imageCheck.setSelected(bCheck);
    }
}
