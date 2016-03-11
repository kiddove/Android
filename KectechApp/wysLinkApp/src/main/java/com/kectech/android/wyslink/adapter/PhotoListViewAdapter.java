package com.kectech.android.wyslink.adapter;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.kectech.android.kectechapp.R;
import com.kectech.android.wyslink.activity.MainActivity;
import com.kectech.android.wyslink.listitem.PhotoListItem;
import com.kectech.android.wyslink.thirdparty.CacheBitmap.ImageFetcher;

import java.util.ArrayList;


/**
 * Created by Paul on 25/06/2015.
 * for ListView in the photo
 */
public class PhotoListViewAdapter extends ArrayAdapter<PhotoListItem> {

    private Context context;
    private ImageFetcher mImageFetcher;

    public PhotoListViewAdapter(Context context, int resourceId, ArrayList<PhotoListItem> items, ImageFetcher imageFetcher) {
        super(context, resourceId, items);
        this.context = context;
        this.mImageFetcher = imageFetcher;
    }

    // private holder class
    private class ViewHolder {
        ArrayList<ImageView> imageViews;
        TextView txtTile;
        TextView txtDesc;
        public ViewHolder () {
            imageViews = new ArrayList<>();
            txtTile = null;
            txtDesc = null;
        }
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        PhotoListItem item = getItem(position);
        LayoutInflater layoutInflater = (LayoutInflater)context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

        try {
            if (convertView == null) {
                /* There is no view at this position, we create a new one.
           In this case by inflating an xml layout */
                convertView = layoutInflater.inflate(R.layout.photo_list_item, parent, false);
                holder = new ViewHolder();

                try {
                    // according to item.items.count show or hide imageView, default is gone.
                    for (int i = 0; i < MainActivity.IMAGE_LIMIT_NUMBER; i++) {
                        ImageView imageView = (ImageView) convertView.findViewById(MainActivity.imageId[i]);
                        // change layout will cause get tag the wrong content.
                        // according to http://stackoverflow.com/questions/12018997/why-does-getview-return-wrong-convertview-objects-on-separatedlistadapter
                        //imageView.setVisibility(View.VISIBLE);
                        holder.imageViews.add(imageView);
                    }
                    holder.txtTile = (TextView) convertView.findViewById(R.id.hall_photo_list_item_title);
                    holder.txtDesc = (TextView) convertView.findViewById(R.id.hall_photo_list_item_desc);
                    convertView.setTag(holder);

//                    // how about after setTag
//                    // seems works
//                    for (int i = 0; i < item.items.size(); i++) {
//                        holder.imageViews.get(i).setVisibility(View.VISIBLE);
//                    }


                    //Log.d(MainActivity.LOG_TAG, "init position: " + position + ", items.size: " + item.items.size() + ", imageViews.size: " + holder.imageViews.size());
                } catch (IndexOutOfBoundsException iobe) {
                    Log.e(MainActivity.LOG_TAG, "position: " + position + ", items.size: " + item.items.size() + ", imageViews.size: " + holder.imageViews.size() + "\n" + iobe.getMessage());
                    iobe.printStackTrace();
                }
                //Log.d(MainActivity.LOG_TAG, "new tag at position: " + position + ", items.size: " + item.items.size() + ", imageViews.size: " + holder.imageViews.size());

            } else {
                /* We recycle a View that already exists */
                holder = (ViewHolder) convertView.getTag();
            }
            for (int i = 0; i < MainActivity.IMAGE_LIMIT_NUMBER; i++) {
                if (i < item.items.size()) {
//                    Bitmap bitmap = item.items.get(i).getThumbNail();
//
//                    holder.imageViews.get(i).setImageBitmap(bitmap);
//                    if (bitmap != null) {
//                        holder.imageViews.get(i).setVisibility(View.VISIBLE);
//                    } else
//                        holder.imageViews.get(i).setVisibility(View.GONE);

                    holder.imageViews.get(i).setVisibility(View.VISIBLE);
                    mImageFetcher.loadImage(item.items.get(i).getThumbURL(), holder.imageViews.get(i));

                } else {
                    holder.imageViews.get(i).setImageBitmap(null);
                    holder.imageViews.get(i).setVisibility(View.GONE);
                }
            }
                //Log.d(MainActivity.LOG_TAG, "old tag at position: " + position + ", items.size: " + item.items.size() + ", imageViews.size: " + holder.imageViews.size());


            holder.txtTile.setText(item.getTitle());
            holder.txtDesc.setText(item.getDescription());
        } catch (Exception e) {
            Log.e(MainActivity.LOG_TAG, "Exception caught(PhotoListViewAdapter): " + e.getMessage());
            e.printStackTrace();
        }
        return convertView;
    }
}
