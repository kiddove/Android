package com.kectech.android.kectechapp.util;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.kectech.android.kectechapp.R;
import com.kectech.android.kectechapp.activity.MainActivity;
import com.kectech.android.kectechapp.thirdparty.CacheBitmap.ImageCache;
import com.kectech.android.kectechapp.thirdparty.CacheBitmap.ImageFetcher;
import com.kectech.android.kectechapp.thirdparty.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.kectech.android.kectechapp.thirdparty.universalimageloader.core.DisplayImageOptions;
import com.kectech.android.kectechapp.thirdparty.universalimageloader.core.ImageLoader;
import com.kectech.android.kectechapp.thirdparty.universalimageloader.core.ImageLoaderConfiguration;
import com.kectech.android.kectechapp.thirdparty.universalimageloader.core.assist.ImageScaleType;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by Paul on 25/06/2015.
 * all subFolder format like user/hall/video, means MainActivity.USER + File.separator + ....., without separator at beginning nor end.
 */
public class KecUtilities {
    private static Context context = null;
    private static ImageFetcher thumb = null;

    public static void init(Context context) {
        KecUtilities.context = context;
    }
    public static ImageFetcher getThumbFetcher(Activity activity) {
        if (thumb != null)
            return thumb;

        ImageCache.ImageCacheParams cacheParams =
                new ImageCache.ImageCacheParams(activity, "thumbs");

        cacheParams.setMemCacheSizePercent(0.10f); // Set memory cache to 10% of app memory

        // The ImageFetcher takes care of loading images into our ImageView children asynchronously
        //thumb = new ImageFetcher(activity, 100);
        thumb = new ImageFetcher(activity, context.getResources().getDimensionPixelSize(R.dimen.image_thumbnail_size));
        thumb.setLoadingImage(R.drawable.ic_default_image);
        thumb.setImageFadeIn(false);
        thumb.addImageCache(activity.getFragmentManager(), cacheParams);

        return thumb;
    }

    public static void clearCache() {
        if (thumb != null)
            thumb.clearCache();
        ImageLoader.getInstance().clearMemoryCache();
        ImageLoader.getInstance().clearDiskCache();
    }
    public static void closeCache() {
        if (thumb != null) {
            thumb.closeCache();
            thumb = null;
        }
    }

    // for save & load json file
    public static String getTabLocalData(String subFolder) {
        BufferedReader br = null;
        String strJson;
        try {
            if (context == null)
                return null;
            StringBuilder output = new StringBuilder();
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
            Log.e(MainActivity.LOG_TAG, "File not found: " + fne.getMessage());
            fne.printStackTrace();
        } catch (NullPointerException | IOException npe) {
            Log.e(MainActivity.LOG_TAG, npe.getMessage());
            npe.printStackTrace();
        } finally {
            try {
                if (br != null)
                    br.close();
            } catch (IOException ioe) {
                Log.e(MainActivity.LOG_TAG, ioe.getMessage());
            }
        }
        return null;
    }

    public static void writeTabLocalData(String strJson, String subFolder) {

        try {
            if (context == null) {
                Log.e(MainActivity.LOG_TAG, "context is null.");
                return;
            }

            File file = new File(context.getFilesDir() + File.separator + subFolder + File.separator + "list.txt");
            if (!file.exists()) {
                if (!file.createNewFile()) {
                    Log.e(MainActivity.LOG_TAG, "create file failed.(" + subFolder + File.separator + "list.txt).");
                }
            }

            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(strJson);
            bw.flush();
            bw.close();
            fw.close();
        } catch (NullPointerException | IOException npe) {
            Log.e(MainActivity.LOG_TAG, npe.getMessage());
            npe.printStackTrace();
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
        } catch (IOException ioe) {
            Log.e(MainActivity.LOG_TAG, "readStringFromStream occurs exception: " + ioe.getMessage());
        } catch (Exception e) {
            Log.e(MainActivity.LOG_TAG, "Exception caught: " + e.getMessage());
            e.printStackTrace();
        }
        return strJson;
    }

    public static void deleteLocalFile() {
        // delete folder [user]
        if (context == null)
            return;
        File folder = new File(context.getFilesDir() + File.separator + MainActivity.USER);
        deleteRecursive(folder);
    }

    private static boolean deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory())
            for (File child : fileOrDirectory.listFiles())
                deleteRecursive(child);

        return fileOrDirectory.delete();
    }

    public static boolean createFolders() {
        // one for each tab
        // hall, show, public, setting
        // inside each tab(except setting, for now only hall) there are video and photo

        // 4 tabs
        // under user
        try {
            if (context == null)
                return false;
            File folder = new File(context.getFilesDir() + File.separator + MainActivity.USER);
            if (!folder.exists()) {
                if (!folder.mkdir()) {
                    Log.e(MainActivity.LOG_TAG, "create folder failed(" + MainActivity.USER + ").");
                    return false;
                }
            }

            // hall
            folder = new File(context.getFilesDir() + File.separator + MainActivity.USER + File.separator + MainActivity.HALL_SUB_FOLDER);
            if (!folder.exists()) {
                if (!folder.mkdir()) {
                    Log.e(MainActivity.LOG_TAG, "create folder failed(" + MainActivity.USER + File.separator + MainActivity.HALL_SUB_FOLDER + ").");
                    return false;
                }
            }

            // show
            folder = new File(context.getFilesDir() + File.separator + MainActivity.USER + File.separator + MainActivity.SHOW_SUB_FOLDER);
            if (!folder.exists()) {
                if (!folder.mkdir()) {
                    Log.e(MainActivity.LOG_TAG, "create folder failed(" + MainActivity.USER + File.separator + MainActivity.SHOW_SUB_FOLDER + ").");
                    return false;
                }
            }

            // public
            folder = new File(context.getFilesDir() + File.separator + MainActivity.USER + File.separator + MainActivity.PUBLIC_SUB_FOLDER);
            if (!folder.exists()) {
                if (!folder.mkdir()) {
                    Log.e(MainActivity.LOG_TAG, "create folder failed(" + MainActivity.USER + File.separator + MainActivity.PUBLIC_SUB_FOLDER + ").");
                    return false;
                }
            }

            // setting
            folder = new File(context.getFilesDir() + File.separator + MainActivity.USER + File.separator + MainActivity.SETTING_SUB_FOLDER);
            if (!folder.exists()) {
                if (!folder.mkdir()) {
                    Log.e(MainActivity.LOG_TAG, "create folder failed(" + MainActivity.USER + File.separator + MainActivity.SETTING_SUB_FOLDER + ").");
                    return false;
                }
            }
            // now has id, every id has photo and video
//            // video and photo in hall
//            folder = new File(context.getFilesDir() + File.separator + MainActivity.USER + File.separator + MainActivity.HALL_SUB_FOLDER + File.separator + MainActivity.VIDEO_SUB_FOLDER);
//            if (!folder.exists()) {
//                if (!folder.mkdir()) {
//                    Log.e(MainActivity.LOG_TAG, "create folder failed(" + MainActivity.USER + File.separator + MainActivity.HALL_SUB_FOLDER + File.separator + MainActivity.VIDEO_SUB_FOLDER + ").");
//                    return false;
//                }
//            }
//            folder = new File(context.getFilesDir() + File.separator + MainActivity.USER + File.separator + MainActivity.HALL_SUB_FOLDER + File.separator + MainActivity.PHOTO_SUB_FOLDER);
//            if (!folder.exists()) {
//                if (!folder.mkdir()) {
//                    Log.e(MainActivity.LOG_TAG, "create folder failed(" + MainActivity.USER + File.separator + MainActivity.HALL_SUB_FOLDER + File.separator + MainActivity.PHOTO_SUB_FOLDER + ").");
//                    return false;
//                }
//            }
        } catch (Exception e) {
            Log.e(MainActivity.LOG_TAG, "Exception caught: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static boolean createSubFolders(String subFolder) {
        // one for each tab
        // hall, show, public, setting
        // inside each tab(except setting, for now only hall) there are video and photo

        // 4 tabs
        // under user
        try {
            if (context == null)
                return false;
            File folder = new File(context.getFilesDir() + File.separator + subFolder);
            if (!folder.exists()) {
                if (!folder.mkdir()) {
                    Log.e(MainActivity.LOG_TAG, "create folder failed(" + subFolder + ").");
                    return false;
                }
            }
        } catch (Exception e) {
            Log.e(MainActivity.LOG_TAG, "Exception caught: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static void initImageLoader() {
        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                .cacheOnDisk(true).imageScaleType(ImageScaleType.EXACTLY)
                .bitmapConfig(Bitmap.Config.RGB_565).build();
        ImageLoaderConfiguration.Builder builder = new ImageLoaderConfiguration.Builder(
                context).defaultDisplayImageOptions(defaultOptions).memoryCache(
                new WeakMemoryCache());

        ImageLoaderConfiguration config = builder.build();
        ImageLoader imageLoader = ImageLoader.getInstance();
        imageLoader.init(config);
    }
}
