package com.kectech.android.kectechapp.data;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.kectech.android.kectechapp.R;
import com.kectech.android.kectechapp.activity.MainActivity;
import com.kectech.android.kectechapp.util.KecUtilities;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by Paul on 23/06/2015.
 * use to download the full image when user tapped an item in the list,
 * will be displayed in an activity
 * AsyncTask<params, progress, result>
 * onPreExecute()
 * doInBackground(Params)
 * onProgressUpdate()
 * onPostExecute(Result)
 */
public class DownLoadImageTask extends AsyncTask<String, Integer, Bitmap> {
    // Reference to the view which should receive the image
    private final WeakReference imageRef;
    // may be null, if finish is called
    private Activity context;
    private ProgressBar progressBar;
    private String subFolder;

    public DownLoadImageTask(ImageView imageView, Activity context, String subFolder) {
        this.imageRef = new WeakReference(imageView);
        this.context = context;
        this.progressBar = (ProgressBar) this.context.findViewById(R.id.photo_activity_progressbar);
        this.subFolder = subFolder;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        progressBar.setProgress(0);
        progressBar.setVisibility(View.VISIBLE);

    }

    @Override
    protected Bitmap doInBackground(String... params) {
        // down load and save to local
        // before start thread, must determined local data is null
        Bitmap bitmap = null;

        try {
            String file_url = params[0];
            URL url = new URL(file_url);

            try {
                URLConnection connection = url.openConnection();

                connection.setConnectTimeout(MainActivity.CONNECTION_TIMEOUT);
                connection.setReadTimeout(MainActivity.CONNECTION_TIMEOUT);

                connection.connect();
                InputStream inputSteam = new BufferedInputStream(url.openStream(), MainActivity.DOWNLOAD_BUFFER);
                int length = connection.getContentLength();

                if (length <= 0)
                    return null;
                //progressBar.setMax(100);

                // change the ui of progress bar
                progressBar.setMax(length);

                String localPath = KecUtilities.getLocalFilePathFromURL(file_url, subFolder);
                if (localPath == null)
                    return null;

                File file = new File(localPath);
                if (!file.exists()) {
                    if (!file.createNewFile()) {
                        Log.e(MainActivity.LOGTAG, "create file failed.");
                    }
                }
                //OutputStream outputStream = context.openFileOutput(localPath, Context.MODE_PRIVATE);
                FileOutputStream outstream = new FileOutputStream(file);
                byte buffer[] = new byte[MainActivity.DOWNLOAD_BUFFER];
                int dataSize;
                int loadedSize = 0;
                while ((dataSize = inputSteam.read(buffer)) != -1) {
                    if (isCancelled()) {
                        outstream.close();
                        return null;
                    }
                    loadedSize += dataSize;
                    publishProgress(loadedSize);
                    outstream.write(buffer, 0, dataSize);
                }

                outstream.close();

                //bitmap = KecUtilities.ReadFileFromLocal(localPath);
                // out of memory .. todo
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                File fileImage = new File(localPath);

                if (fileImage.exists()) {
                    bitmap = BitmapFactory.decodeFile(fileImage.getAbsolutePath());
                }
            } catch (SocketTimeoutException ste) {
                Log.d(MainActivity.LOGTAG, "time out: " + ste.getMessage());
            } catch (IOException ioe) {
                Log.e(MainActivity.LOGTAG, "IO exception: " + ioe.getMessage());
            }

        } catch (Exception e) {
            Log.e(MainActivity.LOGTAG, "Exception caught: " + e.getMessage());
        }
        return bitmap;
    }

    /*
    * After completing background task, dismiss the progress dialog
    * */
    protected void onPostExecute(Bitmap bitmap) {
        super.onPostExecute(bitmap);
        progressBar.setVisibility(View.GONE);
        if (isCancelled()) {
            bitmap = null;
        }

        ImageView imageView = (ImageView) imageRef.get();
        if (imageView != null && bitmap != null) {
            imageView.setImageBitmap(bitmap);
            //bitmap.recycle();
        } else
            Log.e(MainActivity.LOGTAG, "Error while downloading the image");

    }

    protected void onProgressUpdate(Integer... progress) {
        progressBar.setProgress(progress[0]);
    }

    @Override
    protected void onCancelled() {
        progressBar.setVisibility(View.GONE);
    }
}
