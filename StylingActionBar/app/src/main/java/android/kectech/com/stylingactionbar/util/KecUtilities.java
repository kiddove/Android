package android.kectech.com.stylingactionbar.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import java.io.File;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

/**
 * Created by Paul on 25/06/2015.
 * getlocalfilepath
 * save?
 * load?
 */
public class KecUtilities {
    public static String getLoaclFilePathFromURL(String url, String subFolder, Context context) {
        //Test if subfolder exists and if not create
        File folder = new File(context.getFilesDir() + File.separator + subFolder);
        if(!folder.exists()){
            folder.mkdir();
        }
        // use base64 to encode the url then use as filename store on local dir
        byte[] data = null;
        try {
            data = url.getBytes("UTF-8");
        } catch (UnsupportedEncodingException usee) {
            usee.printStackTrace();
            return null;
        }
        String fileName = Base64.encodeToString(data, Base64.DEFAULT);
        File file = new File(context.getFilesDir() + File.separator
                + subFolder + File.separator + fileName);
        return file.getAbsolutePath();
    }

    public static Bitmap ReadFileFromLocal(String filePath) {
        Bitmap bitmap = null;
        InputStream inputSteam = null;

        if (filePath != null) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            File file = new File(filePath);

            if (file.exists()) {
                bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
            }
        }

        return bitmap;
    }
}
