package com.kectech.android.wyslink.adapter;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.kectech.android.wyslink.R;
import com.kectech.android.wyslink.activity.MainActivity;
import com.kectech.android.wyslink.listitem.MainShowAddRecommendListItem;
import com.kectech.android.wyslink.thirdparty.CacheBitmap.ImageFetcher;
import com.kectech.android.wyslink.thirdparty.CacheBitmap.RecyclingImageView;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Paul on 16/06/2015.
 * custom adapter for video tab listView
 */
public class ShowAddRecommendListViewAdapter extends ArrayAdapter<MainShowAddRecommendListItem> {
    private Context context;
    public boolean showCheckBox = false;
    private ImageFetcher mImageFetcher;

    private HashMap<Integer, Boolean> selection = new HashMap<>();

    public ShowAddRecommendListViewAdapter(Context context, int resourceId, ArrayList<MainShowAddRecommendListItem> items, ImageFetcher imageFetcher) {
        super(context, resourceId, items);
        this.context = context;
        this.mImageFetcher = imageFetcher;
    }

    // private view holder class
    private static class ViewHolder {
        ImageView imageView;
        CheckBox checkBox;
        TextView txtTitle;
        TextView txtDesc;
        // for delete animation
        boolean needInflate;
        TextView txtMemo;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        MainShowAddRecommendListItem item = getItem(position);

        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

        try {
            if (convertView == null || ((ViewHolder) convertView.getTag()).needInflate) {
                convertView = layoutInflater.inflate(R.layout.tab_main_show_list_item, parent, false);
                holder = new ViewHolder();
                holder.imageView = (RecyclingImageView) convertView.findViewById(R.id.tab_main_show_list_item_img);
                //holder.imageView = null;
                holder.txtTitle = (TextView) convertView.findViewById(R.id.tab_main_show_list_item_title);
                holder.txtDesc = (TextView) convertView.findViewById(R.id.tab_main_show_list_item_desc);
                holder.checkBox = (CheckBox) convertView.findViewById(R.id.tab_main_show_list_item_check);
                holder.txtMemo = (TextView) convertView.findViewById(R.id.tab_main_show_list_item_memo);
                holder.needInflate = false;
                convertView.setTag(holder);
            } else
                holder = (ViewHolder) convertView.getTag();
        } catch (Exception e) {
            Log.e(MainActivity.LOG_TAG, "Exception caught(ShowListViewAdapter): " + e.getMessage());
            return convertView;
        }

        holder.txtDesc.setText(item.getDescription());
        holder.txtTitle.setText(item.getTitle());
        //holder.imageView.setImageBitmap(item.getImage());
        holder.checkBox.setChecked(isChecked(position));
        holder.txtMemo.setText(item.getMemo());
        if (showCheckBox)
            holder.checkBox.setVisibility(View.VISIBLE);
        else holder.checkBox.setVisibility(View.GONE);
        //holder.imageView = null;

        mImageFetcher.loadImage(item.getThumbURL(), holder.imageView);

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

    public void clearSelection() {
        selection.clear();
    }

    public boolean isSelectionEmpty() {
        return selection.size() == 0;
    }
}
