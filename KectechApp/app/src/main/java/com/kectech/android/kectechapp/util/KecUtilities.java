package com.kectech.android.kectechapp.util;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;

import com.kectech.android.kectechapp.activity.MainActivity;
import com.kectech.android.kectechapp.thirdparty.CacheBitmap.ImageCache;
import com.kectech.android.kectechapp.thirdparty.CacheBitmap.ImageFetcher;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

/**
 * Created by Paul on 25/06/2015.
 * all subFolder format like user/hall/video, means MainActivity.USER + File.separator + ....., without separator at beginning nor end.
 */
public class KecUtilities {
    public static Context context = null;
    private static ImageFetcher thumb = null;
    private static ImageFetcher image = null;

    public static ImageFetcher getImageFetcher(Activity activity) {
        if (image != null)
            return image;

        // Fetch screen height and width, to use as our max size when loading images as this
        // activity runs full screen
        final DisplayMetrics displayMetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        final int height = displayMetrics.heightPixels;
        final int width = displayMetrics.widthPixels;

        // For this sample we'll use half of the longest width to resize our images. As the
        // image scaling ensures the image is larger than this, we should be left with a
        // resolution that is appropriate for both portrait and landscape. For best image quality
        // we shouldn't divide by 2, but this will use more memory and require a larger memory
        // cache.
        final int longest = (height > width ? height : width) / 2;


        ImageCache.ImageCacheParams cacheParams =
                new ImageCache.ImageCacheParams(activity, "images");

        cacheParams.setMemCacheSizePercent(0.1f); // Set memory cache to 5% of app memory

            // The ImageFetcher takes care of loading images into our ImageView children asynchronously
        image = new ImageFetcher(activity, longest);
        //image.setLoadingImage(R.drawable.empty_photo);
        image.setImageFadeIn(false);
        image.addImageCache(activity.getFragmentManager(), cacheParams);

        return image;
    }

    public static ImageFetcher getThumbFetcher(Activity activity) {
        if (thumb != null)
            return thumb;

        ImageCache.ImageCacheParams cacheParams =
                new ImageCache.ImageCacheParams(activity, "thumbs");

        cacheParams.setMemCacheSizePercent(0.05f); // Set memory cache to 5% of app memory

        // The ImageFetcher takes care of loading images into our ImageView children asynchronously
        thumb = new ImageFetcher(activity, 100);
        //mImageFetcher.setLoadingImage(R.drawable.empty_photo);
        thumb.setImageFadeIn(false);
        thumb.addImageCache(activity.getFragmentManager(), cacheParams);

        return thumb;
    }

    public static void clearCache() {
        if (thumb != null)
            thumb.clearCache();
        if (image != null)
            image.clearCache();
    }
    public static void closeCache() {
        if (thumb != null) {
            thumb.closeCache();
            thumb = null;
        }
        if (image != null) {
            image.closeCache();
            image = null;
        }
    }

    public static String getLocalFilePathFromURL(String url, String subFolder) {
        // subfolder should be exist after calling createFolders
        if (url == null || context == null)
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
            Log.e(MainActivity.LOG_TAG, "Exception caught: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public static Bitmap ReadFileFromLocal(String filePath) {
        Bitmap bitmap = null;

        if (filePath != null) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            //options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            options.inPreferredConfig = Bitmap.Config.RGB_565;
            File file = new File(filePath);

            if (file.exists()) {
                try {
                    bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                } catch (OutOfMemoryError ome) {
                    // only load 100 * 100
                    try {
                        // only read dimensions and type no real data.
                        BitmapFactory.Options o = new BitmapFactory.Options();
                        o.inJustDecodeBounds = true;
                        BitmapFactory.decodeStream(new FileInputStream(file), null, o);

                        final int width = 80;
                        final int height = 80;
                        int scale = 1;
                        while (o.outWidth / scale / 2 >= width && o.outHeight / scale / 2 >= height)
                            scale *= 2;

                        // Decode with inSampleSize
                        BitmapFactory.Options o2 = new BitmapFactory.Options();
                        o2.inSampleSize = scale;
                        return BitmapFactory.decodeStream(new FileInputStream(file), null, o2);

                    } catch (OutOfMemoryError ome_stream) {
                        //
                        Log.e(MainActivity.LOG_TAG, "OutOfMemoryError second time.");

                    } catch (FileNotFoundException fnfe) {
                        Log.e(MainActivity.LOG_TAG, "file not found when trying load scaled img: " + fnfe.getMessage());
                        fnfe.printStackTrace();
                    }
                }
            }
        }
        return bitmap;
    }

    // for save & load json file
    public static String getTabLocalData(String subFolder) {
        BufferedReader br = null;
        String strJson;
        try {
            if (context == null)
                return null;
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
            Log.e(MainActivity.LOG_TAG, "File not found: " + fne.getMessage());
            fne.printStackTrace();

        } catch (NullPointerException npe) {
            Log.e(MainActivity.LOG_TAG, npe.getMessage());
            npe.printStackTrace();
        } catch (IOException ioe) {
            Log.e(MainActivity.LOG_TAG, ioe.getMessage());
            ioe.printStackTrace();
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
        } catch (NullPointerException npe) {
            Log.e(MainActivity.LOG_TAG, npe.getMessage());
            npe.printStackTrace();
        } catch (IOException ioe) {
            Log.e(MainActivity.LOG_TAG, ioe.getMessage());
            ioe.printStackTrace();
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
}
