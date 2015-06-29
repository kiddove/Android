package android.kectech.com.stylingactionbar.data;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.kectech.com.stylingactionbar.MainActivity;
import android.kectech.com.stylingactionbar.R;
import android.kectech.com.stylingactionbar.util.KecUtilities;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by Paul on 23/06/2015.
 * use to download the full image when user tapped an item in the list,
 * will be displayed in an activity
 * AsyncTask<params, progress, result>
 *     onPreExecute()
 *     doInBackground(Params)
 *     onProgressUpdate()
 *     onPostExecute(Result)
 */
public class DownLoadImageTask extends AsyncTask<String, Integer, Bitmap> {
    // Reference to the view which should receive the image
    private final WeakReference imageRef;
    private Activity context;
    private ProgressBar progressBar;
    public DownLoadImageTask(ImageView imageView, Activity context) {
        imageRef = new WeakReference(imageView);
        this.context = context;
        progressBar = (ProgressBar)context.findViewById(R.id.photo_activity_progressbar);
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
        Bitmap bitmap = null;

        try {
            String fileurl = params[0];

            URL url = new URL(fileurl);

            URLConnection connection = url.openConnection();

            connection.connect();
            InputStream inputSteam = new BufferedInputStream(url.openStream(), 10240);
            int length = connection.getContentLength();

            if (length <= 0)
                return null;
            //progressBar.setMax(100);

            // change the ui of progress bar
            progressBar.setMax(length);

            String localPath = KecUtilities.getLoaclFilePathFromURL(fileurl, MainActivity.PHOTO_SUB_FOLDER, context);
            if (localPath == null)
                return null;

            File file = new File(localPath);
            if(!file.exists()) {
                file.createNewFile();
            }
            //OutputStream outputStream = context.openFileOutput(localPath, Context.MODE_PRIVATE);
            FileOutputStream outstream = new FileOutputStream(file);
            byte buffer[] = new byte[1024 * 5];
            int dataSize;
            int loadedSize = 0;
            while ((dataSize = inputSteam.read(buffer))!= -1) {
                loadedSize += dataSize;
                publishProgress(loadedSize);
                outstream.write(buffer, 0, dataSize);
            }

            outstream.close();

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            File fileImage = new File(localPath);

            if (fileImage.exists()) {
                bitmap = BitmapFactory.decodeFile(fileImage.getAbsolutePath());
            }

        } catch (Exception e) {
            Log.e(MainActivity.LOGTAG, e.getMessage());
        }
        return bitmap;
    }

    /*
    * After completing background task, dismiss the progress dialog
    * */
    protected  void onPostExecute(Bitmap bitmap) {
        super.onPostExecute(bitmap);
        progressBar.setVisibility(View.GONE);
        if (isCancelled()) {
            bitmap = null;
        }

        ImageView imageView = (ImageView) imageRef.get();
        if (imageView != null && bitmap != null) {
            imageView.setImageBitmap(bitmap);
        } else
            Log.e(MainActivity.LOGTAG, "Error while downloading the image");

    }

    protected void onProgressUpdate(Integer... progress) {
        progressBar.setProgress(progress[0]);
    }

}
