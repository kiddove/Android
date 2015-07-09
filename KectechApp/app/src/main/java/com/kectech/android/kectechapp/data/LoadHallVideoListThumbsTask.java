package com.kectech.android.kectechapp.data;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;

import com.kectech.android.kectechapp.R;
import com.kectech.android.kectechapp.activity.MainActivity;
import com.kectech.android.kectechapp.adapter.VideoListViewAdapter;
import com.kectech.android.kectechapp.listitem.VideoListItem;
import com.kectech.android.kectechapp.util.KecUtilities;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.net.URLConnection;


/**
 * Created by Paul on 25/06/2015.
 * used for loading thumbs for the photo tab ListView's item
 * params (for the task not the constructor)-- object, thumbs url? listitem position? String?
 * progress -- to notify which item need to refresh, still use listitem
 * result -- bitmap
 */
public class LoadHallVideoListThumbsTask extends AsyncTask<VideoListItem, VideoListItem, Bitmap> {
    // Reference to the view which should receive the image
    private final WeakReference adapterRef;
    private final WeakReference listRef;
    private Activity activity;
    private String subFolder = null;

    public LoadHallVideoListThumbsTask(Activity activity, VideoListViewAdapter adapter, ListView listView, @Nullable String subFolder) {
        this.adapterRef = new WeakReference(adapter);
        this.listRef = new WeakReference(listView);
        this.activity = activity;
        this.subFolder = subFolder;
    }

    @Override
    protected Bitmap doInBackground(VideoListItem... params) {
        if (params.length < 1 || subFolder == null)
            return null;
        Bitmap bitmap = null;
        try {

            for (int i = 0; i < params.length; i++) {

                VideoListItem item = params[i];


                // todo change folder
                String localPath = KecUtilities.getLocalFilePathFromURL(item.getThumbURL(), subFolder, activity);

                if (localPath == null)
                    return bitmap;
                // read from local first
                bitmap = KecUtilities.ReadFileFromLocal(localPath);
                if (bitmap == null) {

                    URL url = new URL(item.getThumbURL());

                    URLConnection connection = url.openConnection();

                    connection.connect();
                    InputStream inputSteam = new BufferedInputStream(url.openStream(), 10240);
                    int length = connection.getContentLength();

                    if (length <= 0)
                        return null;

                    File file = new File(localPath);
                    if (!file.exists()) {
                        if (!file.createNewFile()) {
                            Log.e(MainActivity.LOGTAG, "create file failed.");
                        }
                    }

                    FileOutputStream outputStream = new FileOutputStream(file);
                    byte buffer[] = new byte[1024 * 5];
                    int dataSize;
                    while ((dataSize = inputSteam.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, dataSize);
                    }

                    outputStream.flush();
                    outputStream.close();

                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inPreferredConfig = Bitmap.Config.ARGB_8888;

                    bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                    //item.items.get(j).setThumbNail(bitmap);
                    // update UI to show thumbnail
                }
                item.setImage(bitmap);

                publishProgress(item);
            }

        } catch (Exception e) {
            Log.e(MainActivity.LOGTAG, e.getMessage());
        }

        return bitmap;
    }

    @Override
    protected void onPostExecute(Bitmap result) {
        super.onPostExecute(result);
//
//        if (isCancelled()) {
//            result = null;
//        }
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onProgressUpdate(VideoListItem... item) {
        // can not update UI in doInBackground...
        // so if use one thread to download multi files
        // update ui here
        // in this thread, to notify UI show the thumb image
//        if (adapterRef != null && listRef != null) {
//
        VideoListViewAdapter adapter = (VideoListViewAdapter) adapterRef.get();
        ListView listView = (ListView) listRef.get();
        Bitmap bitmap = item[0].getImage();
        if (bitmap != null) {
//                int i1 = listView.getFirstVisiblePosition();
//                int i2 = listView.getLastVisiblePosition();
            View v = listView.getChildAt(item[0].getPosition() - listView.getFirstVisiblePosition());
            if (v != null) {
                ImageView imgView = (ImageView) v.findViewById(R.id.hall_video_list_item_img);
                if (imgView != null) {
                    imgView.setImageBitmap(bitmap);
                } else {
                    Log.d(MainActivity.LOGTAG, "not cool at all.");
                }
            }
        } else
            Log.e(MainActivity.LOGTAG, "result is nulls, download failed.");
    }
}
