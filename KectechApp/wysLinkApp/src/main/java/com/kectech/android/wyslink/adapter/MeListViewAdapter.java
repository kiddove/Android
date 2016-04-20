package com.kectech.android.wyslink.adapter;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.kectech.android.wyslink.R;
import com.kectech.android.wyslink.listitem.MeListItem;

import java.util.ArrayList;

/**
 * Created by Paul on 14/04/2016.
 * ListAdapter for Me tab
 */
public class MeListViewAdapter extends ArrayAdapter<MeListItem> {
    public MeListViewAdapter(Context context, int resourceId, ArrayList<MeListItem> items) {
        super(context, resourceId, items);
    }

    public class ViewHolder {
        ImageView imageView;
        TextView txtTitle;
        TextView txtDesc;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        MeListItem item = getItem(position);
        LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.me_list_item, parent, false);
            holder = new ViewHolder();
            holder.imageView = (ImageView) convertView.findViewById(R.id.me_list_item_img);
            //holder.imageView = null;
            holder.txtTitle = (TextView) convertView.findViewById(R.id.me_list_item_title);
            holder.txtDesc = (TextView) convertView.findViewById(R.id.me_list_item_desc);
            convertView.setTag(holder);
        } else
            holder = (ViewHolder) convertView.getTag();

        holder.txtDesc.setText(item.getDescription());
        holder.txtTitle.setText(item.getTitle());

        if (item.getTitle().equalsIgnoreCase("private"))
            holder.imageView.setImageResource(R.drawable.ic_av_movie_red);
        else if (item.getTitle().equalsIgnoreCase("public"))
            holder.imageView.setImageResource(R.drawable.ic_av_movie_green);
        else
            holder.imageView.setImageResource(R.drawable.ic_av_movie_blue);

        return convertView;
    }
}
