package com.kectech.android.wyslink.adapter;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.kectech.android.kectechapp.R;
import com.kectech.android.wyslink.activity.MainActivity;
import com.kectech.android.wyslink.listitem.Tab_Main_Hall_ListItem;
import com.kectech.android.wyslink.thirdparty.CacheBitmap.ImageFetcher;
import com.kectech.android.wyslink.thirdparty.CacheBitmap.RecyclingImageView;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Paul on 16/06/2015.
 * custom adapter for video tab listView
 */
public class HallListViewAdapter extends ArrayAdapter<Tab_Main_Hall_ListItem> {
    public static final int ANIMATION_DURATION = 100;
    private Context context;
    public boolean showCheckBox = false;
    private ImageFetcher mImageFetcher;

    private HashMap<Integer, Boolean> selection = new HashMap<>();

    public HallListViewAdapter(Context context, int resourceId, ArrayList<Tab_Main_Hall_ListItem> items, ImageFetcher imageFetcher) {
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
        Tab_Main_Hall_ListItem item = getItem(position);

        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

        try {
            if (convertView == null || ((ViewHolder) convertView.getTag()).needInflate) {
                convertView = layoutInflater.inflate(R.layout.tab_main_hall_list_item, parent, false);
                holder = new ViewHolder();
                holder.imageView = (RecyclingImageView) convertView.findViewById(R.id.tab_main_hall_list_item_img);
                //holder.imageView = null;
                holder.txtTitle = (TextView) convertView.findViewById(R.id.tab_main_hall_list_item_title);
                holder.txtDesc = (TextView) convertView.findViewById(R.id.tab_main_hall_list_item_desc);
                holder.checkBox = (CheckBox) convertView.findViewById(R.id.tab_main_hall_list_item_check);
                holder.txtMemo = (TextView) convertView.findViewById(R.id.tab_main_hall_list_item_memo);
                holder.needInflate = false;
                convertView.setTag(holder);
            } else
                holder = (ViewHolder) convertView.getTag();
        } catch (Exception e) {
            Log.e(MainActivity.LOG_TAG, "Exception caught(HallListViewAdapter): " + e.getMessage());
            return convertView;
        }

        holder.txtDesc.setText(item.getDesc());
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

    public Animation deleteCell(final View v, final int index) {
        Animation.AnimationListener al = new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                //Log.d(MainActivity.LOG_TAG, "start delete animation at " + index);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                remove(getItem(index));
                ViewHolder vh = (ViewHolder) v.getTag();
                vh.needInflate = true;
                notifyDataSetChanged();
                //Log.d(MainActivity.LOG_TAG, "finish delete animation at " + index);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        };

        return collapse(v, al);
    }

    private Animation collapse(final View v, Animation.AnimationListener al) {
        final int initialHeight = v.getMeasuredHeight();

        Animation anim = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                if (interpolatedTime == 1) {
                    v.setVisibility(View.GONE);
                } else {
                    if (v.getLayoutParams() == null)
                        return;
                    v.getLayoutParams().height = initialHeight - (int) (initialHeight * interpolatedTime);
                    v.requestLayout();
                }
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        if (al != null) {
            anim.setAnimationListener(al);
        }
        anim.setDuration(ANIMATION_DURATION);
        return anim;
//        v.startAnimation(anim);
    }
}
