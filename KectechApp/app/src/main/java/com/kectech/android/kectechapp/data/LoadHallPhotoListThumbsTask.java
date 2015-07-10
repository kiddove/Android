package com.kectech.android.kectechapp.data;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import com.kectech.android.kectechapp.activity.MainActivity;
import com.kectech.android.kectechapp.adapter.PhotoListViewAdapter;
import com.kectech.android.kectechapp.listitem.PhotoListItem;
import com.kectech.android.kectechapp.listitem.PhotoProgressUpdate;
import com.kectech.android.kectechapp.thirdparty.ScaleImageView;
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
public class LoadHallPhotoListThumbsTask extends AsyncTask<PhotoListItem, PhotoProgressUpdate, Integer> {
    // Reference to the view which should receive the image
    private final WeakReference adapterRef;
    private final WeakReference listRef;
    private Activity activity;
    private String subFolder;

    public LoadHallPhotoListThumbsTask(Activity activity, PhotoListViewAdapter adapter, ListView listView, String subFolder) {
        this.adapterRef = new WeakReference(adapter);
        this.listRef = new WeakReference(listView);
        this.activity = activity;
        this.subFolder = subFolder;
    }

    @Override
    protected Integer doInBackground(PhotoListItem... params) {
        if (params.length < 1)
            return -1;
        try {

            for (PhotoListItem item : params) {
                if (isCancelled())
                    return -1;
//            for (int i = 0; i < params.length; i++) {
//
//                PhotoListItem item = params[i];

                // multi photo thumbs
                for (int j = 0; j < item.items.size(); j++) {
                    String localPath = KecUtilities.getLocalFilePathFromURL(item.items.get(j).getThumbURL(), subFolder, activity);

                    if (localPath == null)
                        continue;
                    // read from local first
                    Bitmap bitmap = KecUtilities.ReadFileFromLocal(localPath);
                    if (bitmap == null) {

                        URL url = new URL(item.items.get(j).getThumbURL());

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

                    PhotoProgressUpdate ppu = new PhotoProgressUpdate();
                    ppu.position = item.getPosition();
                    ppu.index = j;
                    ppu.bitmap = bitmap;
                    publishProgress(ppu);
                }
            }

        } catch (Exception e) {
            Log.e(MainActivity.LOGTAG, e.getMessage());
        }

        return 0;
    }

    @Override
    protected void onPostExecute(Integer noUse) {
        super.onPostExecute(noUse);
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
    protected void onProgressUpdate(PhotoProgressUpdate... ppu) {
        // can not update UI in doInBackground...
        // so if use one thread to download multi files
        // update ui here
        // in this thread, to notify UI show the thumb image
//        if (adapterRef != null && listRef != null) {

        PhotoListViewAdapter adapter = (PhotoListViewAdapter) adapterRef.get();
        ListView listView = (ListView) listRef.get();
        Bitmap bitmap = ppu[0].bitmap;
        adapter.getItem(ppu[0].position).items.get(ppu[0].index).setThumbNail(bitmap);
        if (bitmap != null) {
//                int i1 = listView.getFirstVisiblePosition();
//                int i2 = listView.getLastVisiblePosition();
            View v = listView.getChildAt(ppu[0].position - listView.getFirstVisiblePosition());
            if (v != null) {
                ScaleImageView imgView = (ScaleImageView) v.findViewById(MainActivity.imageId[ppu[0].index]);
                if (imgView != null) {
                    imgView.setImageBitmap(bitmap);
                    imgView.setVisibility(View.VISIBLE);
                } else {
                    Log.d(MainActivity.LOGTAG, "not cool at all.");
                }
            }
        } else
            Log.e(MainActivity.LOGTAG, "result is nulls, download failed.");
    }
//    }
}
