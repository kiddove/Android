package android.kectech.com.stylingactionbar.adapter;

import android.app.Activity;
import android.content.Context;
import android.kectech.com.stylingactionbar.R;
import android.kectech.com.stylingactionbar.listitem.PhotoListItem;
import android.kectech.com.stylingactionbar.view.ScaleImageView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Paul on 25/06/2015.
 * for ListView in the photo
 */
public class PhotoListViewAdapter extends ArrayAdapter<PhotoListItem> {

    private Context context;

    public PhotoListViewAdapter(Context context, int resourceId, ArrayList<PhotoListItem> items) {
        super(context, resourceId, items);
        this.context = context;
    }

    // private holder class
    private class ViewHolder {
        ScaleImageView imageView;
        TextView txtTile;
        TextView txtDesc;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;

        PhotoListItem item = getItem(position);
        LayoutInflater layoutInflater = (LayoutInflater)context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.photo_list_item, null);
            holder = new ViewHolder();

            holder.imageView = (ScaleImageView)convertView.findViewById(R.id.photo_list_item_img);
            holder.txtTile = (TextView)convertView.findViewById(R.id.photo_list_item_title);
            holder.txtDesc = (TextView)convertView.findViewById(R.id.photo_list_item_desc);
            convertView.setTag(holder);

        } else {
            holder = (ViewHolder)convertView.getTag();
        }

        holder.imageView.setImageBitmap(item.getThumbNail());
        holder.txtTile.setText(item.getTitle());
        holder.txtDesc.setText(item.getDescription());

        return convertView;
    }
}
