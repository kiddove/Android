package android.kectech.com.stylingactionbar.data;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.kectech.com.stylingactionbar.MainActivity;
import android.kectech.com.stylingactionbar.R;
import android.kectech.com.stylingactionbar.adapter.PhotoListViewAdapter;
import android.kectech.com.stylingactionbar.listitem.PhotoListItem;
import android.kectech.com.stylingactionbar.util.KecUtilities;
import android.kectech.com.stylingactionbar.view.ScaleImageView;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

/**
 * Created by Paul on 25/06/2015.
 * used for loading thumbs for the photo tab ListView's item
 * params (for the task not the constructor)-- object, tunmbs url? listitem? postion? String?
 * progress -- Void
 * result -- bitmap
 */
public class LoadPhotoListThumbsTask extends AsyncTask<PhotoListItem, PhotoListItem, Bitmap> {
    private static final String LOGTAG = "LoadPhotoListThumbsTask";
    // Reference to the view which should receive the image
    private final WeakReference adapterRef;
    private final WeakReference listRef;
    private Activity activity;

    public LoadPhotoListThumbsTask(Activity activity, PhotoListViewAdapter adapter, ListView listView) {
        this.adapterRef = new WeakReference(adapter);
        this.listRef = new WeakReference(listView);
        this.activity = activity;
    }

    @Override
    protected Bitmap doInBackground(PhotoListItem... params) {
        Bitmap bitmap = null;
        if (params.length < 1)
            return bitmap;
        try {

            for (int i = 0; i < params.length; i++) {

                PhotoListItem item = params[i];

                String localPath = KecUtilities.getLoaclFilePathFromURL(item.getThumbURL(), MainActivity.PHOTO_SUB_FOLDER, activity);

                // read from locl first?
                if (localPath == null)
                    return bitmap;

                URL url = new URL(item.getThumbURL());

                URLConnection connection = url.openConnection();

                connection.connect();
                InputStream inputSteam = new BufferedInputStream(url.openStream(), 10240);
                int length = connection.getContentLength();

                if (length <= 0)
                    return bitmap;

                File file = new File(localPath);
                if (!file.exists()) {
                    file.createNewFile();
                }
                //OutputStream outputStream = context.openFileOutput(localPath, Context.MODE_PRIVATE);
                FileOutputStream outstream = new FileOutputStream(file);
                byte buffer[] = new byte[1024 * 5];
                int dataSize;
                while ((dataSize = inputSteam.read(buffer)) != -1) {
                    outstream.write(buffer, 0, dataSize);
                }

                outstream.close();

                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                File fileImage = new File(localPath);

                if (fileImage.exists()) {
                    bitmap = BitmapFactory.decodeFile(fileImage.getAbsolutePath());
                    params[i].setThumbNail(bitmap);
                }

                // update UI to show thumbnail
                publishProgress(params[i]);
            }

        } catch (Exception e) {
            Log.e(LOGTAG, e.getMessage());
        }

        return bitmap;
    }

    @Override
    protected void onPostExecute(Bitmap result) {
        super.onPostExecute(result);

        if (isCancelled()) {
            result = null;
        }
//         // for single
//        if (adapterRef != null && listRef != null) {
//
//            PhotoListViewAdapter adapter = (PhotoListViewAdapter)adapterRef.get();
//            ListView listView = (ListView)listRef.get();
//            adapter.getItem(position).setThumbNail(result);
//            if (result != null) {
//
//                int i1 = listView.getFirstVisiblePosition();
//                int i2 = listView.getLastVisiblePosition();
//                View v = listView.getChildAt(position - listView.getFirstVisiblePosition());
//                if (v != null) {
//                    ImageView imgView = (ImageView) v.findViewById(R.id.photo_list_item_img);
//                    if (imgView != null)
//                        imgView.setImageBitmap(result);
//                }
//
//            } else
//                Log.e(LOGTAG, "result is nulls, download failed.");
//        }
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onProgressUpdate(PhotoListItem... item) {
        // can not update UI in doInBackground...
        // so if use one thread to download multi files
        // update ui here
        if (adapterRef != null && listRef != null) {

            PhotoListViewAdapter adapter = (PhotoListViewAdapter)adapterRef.get();
            ListView listView = (ListView)listRef.get();
            Bitmap bitmap = item[0].getThumbNail();
            adapter.getItem(item[0].getPosition()).setThumbNail(bitmap);
            if (bitmap != null) {
//                int i1 = listView.getFirstVisiblePosition();
//                int i2 = listView.getLastVisiblePosition();
                View v = listView.getChildAt(item[0].getPosition() - listView.getFirstVisiblePosition());
                if (v != null) {
                    ScaleImageView imgView = (ScaleImageView) v.findViewById(R.id.photo_list_item_img);
                    if (imgView != null)
                        imgView.setImageBitmap(bitmap);
                }
            } else
                Log.e(LOGTAG, "result is nulls, download failed.");
        }
    }
}
