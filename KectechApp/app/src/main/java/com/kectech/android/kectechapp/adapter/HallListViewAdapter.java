package com.kectech.android.kectechapp.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import com.kectech.android.kectechapp.R;
import com.kectech.android.kectechapp.listitem.Tab_Main_Hall_ListItem;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Paul on 16/06/2015.
 * custom adapter for video tab listView
 */
public class HallListViewAdapter extends ArrayAdapter<Tab_Main_Hall_ListItem> {
    private  Context context;
    public boolean showCheckBox = false;

    private HashMap<Integer, Boolean> selection = new HashMap<Integer, Boolean>();
    public HallListViewAdapter(Context context, int resourceId, ArrayList<Tab_Main_Hall_ListItem> items) {
        super(context, resourceId, items);
        this.context = context;
    }

    // private view holder class
    private class ViewHolder {
        ImageView imageView;
        CheckBox checkBox;
        TextView txtTitle;
        TextView txtDesc;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        Tab_Main_Hall_ListItem item = getItem(position);

        LayoutInflater layoutInflater = (LayoutInflater)context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.tab_main_hall_list_item, null);
            holder = new ViewHolder();
            holder.imageView = (ImageView)convertView.findViewById(R.id.tab_main_hall_list_item_img);
            //holder.imageView = null;
            holder.txtTitle = (TextView)convertView.findViewById(R.id.tab_main_hall_list_item_title);
            holder.txtDesc = (TextView)convertView.findViewById(R.id.tab_main_hall_list_item_desc);
            holder.checkBox = (CheckBox)convertView.findViewById(R.id.tab_main_hall_list_item_check);
            convertView.setTag(holder);
        }
        else
            holder = (ViewHolder)convertView.getTag();

        holder.txtDesc.setText(item.getDesc());
        holder.txtTitle.setText(item.getTitle());
        holder.imageView.setImageBitmap(item.getImage());
        holder.checkBox.setChecked(isChecked(position));
        if (showCheckBox)
            holder.checkBox.setVisibility(View.VISIBLE);
        else holder.checkBox.setVisibility(View.GONE);
        //holder.imageView = null;

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

    public void clear() {
        selection.clear();
    }
}
