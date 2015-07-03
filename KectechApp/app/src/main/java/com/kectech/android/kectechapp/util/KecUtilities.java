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
        File folder = new File(context.getFilesDir() + File.separator + MainActivity.USER);
        if (!folder.exists()) {
            folder.mkdir();
        }
        folder = new File(context.getFilesDir() + File.separator + MainActivity.USER + File.separator + subFolder);
        if (!folder.exists()) {
            folder.mkdir();
        }
        // use base64 to encode the url then use as filename store on local dir
        byte[] data = null;
        try {
            data = url.getBytes(MainActivity.ENCODING);
        } catch (UnsupportedEncodingException usee) {
            usee.printStackTrace();
            return null;
        }
        String fileName = Base64.encodeToString(data, Base64.DEFAULT);
        File file = new File(context.getFilesDir() + File.separator + MainActivity.USER + File.separator
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

    public static String getTabLocalData(String subFolder, Context context) {
        BufferedReader br = null;
        String strJson = null;
        try {
            StringBuffer output = new StringBuffer();
            String filePath = context.getFilesDir() + File.separator + MainActivity.USER + File.separator + subFolder + File.separator + subFolder + ".txt";
            br = new BufferedReader(new FileReader(filePath));
            String line = "";
            while ((line = br.readLine()) != null) {
                output.append(line + "\n");
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
            File folder = new File(context.getFilesDir() + File.separator + MainActivity.USER);
            if (!folder.exists()) {
                folder.mkdir();
            }
            folder = new File(context.getFilesDir() + File.separator + MainActivity.USER + File.separator + subFolder);
            if (!folder.exists()) {
                folder.mkdir();
            }

            File file = new File(context.getFilesDir() + File.separator + MainActivity.USER + File.separator + subFolder + File.separator + subFolder + ".txt");
            if (!file.exists()) {
                file.createNewFile();
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
}
