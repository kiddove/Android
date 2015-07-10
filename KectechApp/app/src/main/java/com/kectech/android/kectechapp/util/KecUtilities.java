package com.kectech.android.kectechapp.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;

import com.kectech.android.kectechapp.activity.MainActivity;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

/**
 * Created by Paul on 25/06/2015.
 * all subFolder format like user/hall/video, means MainActivity.USER + File.separator + ....., without separator at beginning nor end.
 */
public class KecUtilities {

    public static String getLocalFilePathFromURL(String url, String subFolder, Context context) {
        // subfolder should be exist after calling createFolders
        if (url == null)
            return null;
        // use base64 to encode the url then use as filename store on local dir
        try {
            byte[] data = url.getBytes(MainActivity.ENCODING);

            String fileName = Base64.encodeToString(data, Base64.DEFAULT);
            File file = new File(context.getFilesDir() + File.separator + subFolder + File.separator + fileName);
            return file.getAbsolutePath();
        } catch (UnsupportedEncodingException uee) {
            uee.printStackTrace();
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Bitmap ReadFileFromLocal(String filePath) {
        Bitmap bitmap = null;

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

    // for save & load json file
    public static String getTabLocalData(String subFolder, Context context) {
        BufferedReader br;
        String strJson;
        try {
            StringBuffer output = new StringBuffer();
            String filePath = context.getFilesDir() + File.separator + subFolder + File.separator + "list.txt";
            File file = new File(filePath);
            if (!file.exists())
                return null;
            br = new BufferedReader(new FileReader(filePath));
            String line;
            while ((line = br.readLine()) != null) {
                output.append(line);
            }
            strJson = output.toString();

            return strJson;
        } catch (FileNotFoundException fne) {
            Log.e(MainActivity.LOGTAG, "File not found: " + fne.getMessage());
        } catch (Exception e) {
            Log.e(MainActivity.LOGTAG, e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public static void writeTabLocalData(String strJson, String subFolder, Context context) {

        try {
            File file = new File(context.getFilesDir() + File.separator + subFolder + File.separator + "list.txt");
            if (!file.exists()) {
                if (!file.createNewFile()) {
                    Log.e(MainActivity.LOGTAG, "create file failed.(" + subFolder + File.separator + "list.txt).");
                }
            }

            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(strJson);
            bw.flush();
            bw.close();
        } catch (Exception e) {
            Log.e(MainActivity.LOGTAG, e.getMessage());
            e.printStackTrace();
        }
    }

    public static String readStringFromStream(InputStream inputStream) {
        String strJson = null;
        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder total = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                total.append(line);
            }
            strJson = total.toString();
        } catch (Exception e) {
            Log.e(MainActivity.LOGTAG, "readStringFromStream occurs exception: " + e.getMessage());
        }
        return strJson;
    }

    public static boolean createFolders(Context context) {
        // one for each tab
        // hall, show, public, setting
        // inside each tab(except setting, for now only hall) there are video and photo

        // 4 tabs
        // under user
        try {
            File folder = new File(context.getFilesDir() + File.separator + MainActivity.USER);
            if (!folder.exists()) {
                if (!folder.mkdir()) {
                    Log.e(MainActivity.LOGTAG, "create folder failed(" + MainActivity.USER + ").");
                    return false;
                }
            }
            // hall
            folder = new File(context.getFilesDir() + File.separator + MainActivity.USER + File.separator + MainActivity.HALL_SUB_FOLDER);
            if (!folder.exists()) {
                if (!folder.mkdir()) {
                    Log.e(MainActivity.LOGTAG, "create folder failed(" + MainActivity.USER + File.separator + MainActivity.HALL_SUB_FOLDER + ").");
                    return false;
                }
            }

            // show
            folder = new File(context.getFilesDir() + File.separator + MainActivity.USER + File.separator + MainActivity.SHOW_SUB_FOLDER);
            if (!folder.exists()) {
                if (!folder.mkdir()) {
                    Log.e(MainActivity.LOGTAG, "create folder failed(" + MainActivity.USER + File.separator + MainActivity.SHOW_SUB_FOLDER + ").");
                    return false;
                }
            }

            // public
            folder = new File(context.getFilesDir() + File.separator + MainActivity.USER + File.separator + MainActivity.PUBLIC_SUB_FOLDER);
            if (!folder.exists()) {
                if (!folder.mkdir()) {
                    Log.e(MainActivity.LOGTAG, "create folder failed(" + MainActivity.USER + File.separator + MainActivity.PUBLIC_SUB_FOLDER + ").");
                    return false;
                }
            }

            // setting
            folder = new File(context.getFilesDir() + File.separator + MainActivity.USER + File.separator + MainActivity.SETTING_SUB_FOLDER);
            if (!folder.exists()) {
                if (!folder.mkdir()) {
                    Log.e(MainActivity.LOGTAG, "create folder failed(" + MainActivity.USER + File.separator + MainActivity.SETTING_SUB_FOLDER + ").");
                    return false;
                }
            }
            // now has id, every id has photo and video
//            // video and photo in hall
//            folder = new File(context.getFilesDir() + File.separator + MainActivity.USER + File.separator + MainActivity.HALL_SUB_FOLDER + File.separator + MainActivity.VIDEO_SUB_FOLDER);
//            if (!folder.exists()) {
//                if (!folder.mkdir()) {
//                    Log.e(MainActivity.LOGTAG, "create folder failed(" + MainActivity.USER + File.separator + MainActivity.HALL_SUB_FOLDER + File.separator + MainActivity.VIDEO_SUB_FOLDER + ").");
//                    return false;
//                }
//            }
//            folder = new File(context.getFilesDir() + File.separator + MainActivity.USER + File.separator + MainActivity.HALL_SUB_FOLDER + File.separator + MainActivity.PHOTO_SUB_FOLDER);
//            if (!folder.exists()) {
//                if (!folder.mkdir()) {
//                    Log.e(MainActivity.LOGTAG, "create folder failed(" + MainActivity.USER + File.separator + MainActivity.HALL_SUB_FOLDER + File.separator + MainActivity.PHOTO_SUB_FOLDER + ").");
//                    return false;
//                }
//            }
        } catch (Exception e) {
            Log.e(MainActivity.LOGTAG, e.getMessage());
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static boolean createSubFolders(Context context, String subFolder) {
        // one for each tab
        // hall, show, public, setting
        // inside each tab(except setting, for now only hall) there are video and photo

        // 4 tabs
        // under user
        try {
            File folder = new File(context.getFilesDir() + File.separator + subFolder);
            if (!folder.exists()) {
                if (!folder.mkdir()) {
                    Log.e(MainActivity.LOGTAG, "create folder failed(" + subFolder + ").");
                    return false;
                }
            }
        } catch (Exception e) {
            Log.e(MainActivity.LOGTAG, e.getMessage());
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
