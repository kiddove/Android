package android.kectech.com.stylingactionbar.data;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.kectech.com.stylingactionbar.MainActivity;
import android.kectech.com.stylingactionbar.R;
import android.kectech.com.stylingactionbar.util.KecUtilities;
import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by Paul on 23/06/2015.
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
    //private ProgressDialog progressDialog;
    private ProgressBar progressBar;
    public DownLoadImageTask(ImageView imageView, Activity context) {
        imageRef = new WeakReference(imageView);
        this.context = context;

//        progressDialog = new ProgressDialog(context);
//        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

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

            //Log.i("Progress", "file length: " + length);
            if (length <= 0)
                return bitmap;
            //progressBar.setMax(100);

            progressBar.setMax(length);

            String localPath = KecUtilities.getLoaclFilePathFromURL(fileurl, MainActivity.PHOTO_SUB_FOLDER, context);
            if (localPath == null)
                return bitmap;

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
            Log.e("Error", e.getMessage());
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

        if (imageRef != null) {
            ImageView imageView = (ImageView)imageRef.get();
            if (imageView != null && bitmap != null) {
                imageView.setImageBitmap(bitmap);
            } else
                Log.e("Error", "Error while downloading the image");
        }
    }

//    private String getLoaclFilePath(String url) {
//        String subFolder = "photo";
//        //Test if subfolder exists and if not create
//        File folder = new File(context.getFilesDir() + File.separator + subFolder);
//        if(!folder.exists()){
//            folder.mkdir();
//        }
//        // use base64 to encode the url then use as filename store on local dir
//        byte[] data = null;
//        try {
//            data = url.getBytes("UTF-8");
//        } catch (UnsupportedEncodingException usee) {
//            usee.printStackTrace();
//            return null;
//        }
//        String fileName = Base64.encodeToString(data, Base64.DEFAULT);
//        File file = new File(context.getFilesDir() + File.separator
//                + subFolder + File.separator + fileName);
//        return file.getAbsolutePath();
////        return fileName;
//    }
//
//    private void WriteToFileInSubfolder(String fileurl, Bitmap bitmap){
//        String subfolder = "photo";
//
//        //Test if subfolder exists and if not create
//        File folder = new File(context.getFilesDir() + File.separator + subfolder);
//        if(!folder.exists()){
//            folder.mkdir();
//        }
//
//        // use base64 to encode the url then use as filename store on local dir
//        byte[] data = null;
//        try {
//            data = fileurl.getBytes("UTF-8");
//        } catch (UnsupportedEncodingException usee) {
//            usee.printStackTrace();
//            return;
//        }
//        String fileName = Base64.encodeToString(data, Base64.DEFAULT);
//        File file = new File(context.getFilesDir() + File.separator
//                + subfolder + File.separator + fileName);
//
//        FileOutputStream outstream;
//
//        try{
//            if(!file.exists()){
//                file.createNewFile();
//            }
//
//            //commented line throws an exception if filename contains a path separator
//            //outstream = context.openFileOutput(filename, Context.MODE_PRIVATE);
//            outstream = new FileOutputStream(file);
//            // todo
//            // what about other format
//            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outstream);
//            outstream.flush();
//            outstream.close();
//
//        }catch(IOException e){
//            e.printStackTrace();
//        }
//    }

    protected void onProgressUpdate(Integer... progress) {
        progressBar.setProgress(progress[0]);

        //Log.i("Progress",  "Now at: " + progress[0].toString());
    }

}
