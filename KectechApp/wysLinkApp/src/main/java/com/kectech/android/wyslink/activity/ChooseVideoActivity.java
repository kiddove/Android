package com.kectech.android.wyslink.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.NetworkOnMainThreadException;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.kectech.android.wyslink.BuildConfig;
import com.kectech.android.wyslink.miscellaneous.VideoDownloadHttpEntity;
import com.kectech.android.wyslink.R;
import com.kectech.android.wyslink.adapter.ChooseVideoAdapter;
import com.kectech.android.wyslink.listeners.OnSwipeTouchListener;
import com.kectech.android.wyslink.listitem.ChooseVideoGridItem;
import com.kectech.android.wyslink.thirdparty.CacheBitmap.ImageFetcher;
import com.kectech.android.wyslink.thirdparty.CacheBitmap.Utils;
import com.kectech.android.wyslink.util.KecUtilities;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by Paul on 07/03/2016.
 * for choosing video
 */
public class ChooseVideoActivity extends Activity {
    private GridView mGridView;
    private ChooseVideoAdapter mAdapter;
    private ImageFetcher mImageFetcher;
    private TextView textDone;
    private TextView textPreview;
    private TextView fileName;
    private CurrentSelect currentSelect = new CurrentSelect();
    private int network_status = -1;

    private boolean bShowPrompt;

    private class CurrentSelect {
        public int position;
        public WeakReference<View> view;

        public CurrentSelect() {
            position = -1;
            view = null;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_choose_image);
//        if (BuildConfig.DEBUG) {
//            System.gc();
//        }
        // start get data first
        initList();

        mImageFetcher = KecUtilities.getThumbFetcher(this);
        mGridView = (GridView) findViewById(R.id.choose_img_gridView);

        // click listener
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                boolean isChecked = mAdapter.isChecked(position);
                onItemChecked(position, !isChecked, view);
                if (BuildConfig.DEBUG) {
                    String strName = mAdapter.getSelection();
                    if (!TextUtils.isEmpty(strName)) {
                        int last = strName.lastIndexOf("/");
                        if (last > 0) {
                            strName = strName.substring(last + 1, strName.length());
                            fileName.setText(strName);
                        }
                    }
                }
            }
        });

        mGridView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int scrollState) {
                // Pause fetcher to ensure smoother scrolling when flinging
                if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_FLING) {
                    // Before Honeycomb pause image loading on scroll to help with performance
                    if (!Utils.hasHoneycomb()) {
                        mImageFetcher.setPauseWork(true);
                    }
                } else {
                    mImageFetcher.setPauseWork(false);
                }
            }

            @Override
            public void onScroll(AbsListView absListView, int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount) {
            }
        });

        OnSwipeTouchListener swipeTouchListener = new OnSwipeTouchListener() {
            public void onSwipeOutLeft() {
                close();
            }
        };

        final View view = findViewById(R.id.choose_img_frame);
        view.setOnTouchListener(swipeTouchListener);
        mGridView.setOnTouchListener(swipeTouchListener);

        textDone = (TextView) findViewById(R.id.choose_img_done);
        textDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                complete();
            }
        });

        textPreview = (TextView) findViewById(R.id.choose_img_preview);
        textPreview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                preview();
            }
        });

        fileName = (TextView) findViewById(R.id.choose_img_name);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_choose_video, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case android.R.id.home: {
                close();
                return true;
            }
            case R.id.capture_video:
                captureVideo();
                //chooseVideo();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void onItemChecked(int position, boolean isChecked, View v) {
        if (currentSelect.position > -1) {
            mAdapter.removeSelect(currentSelect.position);
            mAdapter.changeSelection(currentSelect.view.get(), false);
            currentSelect.view.clear();
            currentSelect.position = -1;
        }
        if (isChecked) {
            mAdapter.setSelection(position, true);
            currentSelect.position = position;
            currentSelect.view = new WeakReference<>(v);
            // set check
        } else {
            mAdapter.removeSelect(position);
            currentSelect.position = -1;
            currentSelect.view.clear();
        }

        mAdapter.changeSelection(v, isChecked);
        if (isChecked) {
            textPreview.setTextColor(getResources().getColorStateList(R.color.bar_background));
            textDone.setTextColor(getResources().getColorStateList(R.color.tab_selected));
        } else {
            setTitle(R.string.prompt_navigation_back);

            textPreview.setTextColor(getResources().getColorStateList(R.color.post_img_background));
            textDone.setTextColor(getResources().getColorStateList(R.color.tab_selected_disable));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mImageFetcher != null)
            mImageFetcher.setExitTasksEarly(false);
        if (mAdapter != null)
            mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mImageFetcher != null) {
            mImageFetcher.setPauseWork(false);
            mImageFetcher.setExitTasksEarly(true);
            mImageFetcher.flushCache();
        }
    }

    public void initList() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
            // allow async task to run simultaneously
            new InitListTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        else
            new InitListTask().execute();
    }

    private class InitListTask extends AsyncTask<Void, Void, ArrayList<ChooseVideoGridItem>> {
        @Override
        protected ArrayList<ChooseVideoGridItem> doInBackground(Void... params) {
            return getGalleryVideos();
        }

        @Override
        protected void onPostExecute(ArrayList<ChooseVideoGridItem> result) {
            super.onPostExecute(result);
            if (isCancelled())
                return;

            if (result != null && !result.isEmpty())
                onRefreshComplete(result);
        }
    }

    private void onRefreshComplete(final ArrayList<ChooseVideoGridItem> result) {
        if (result == null)
            // means error occurred when sending http request, other wise result should be empty but not null
            return;
        if (mAdapter != null) {
            if (BuildConfig.DEBUG)
                Log.d(MainActivity.LOG_TAG, "ListView(mAdapter) already had data, and will be cleared...");

            mAdapter.clear();
            mAdapter.notifyDataSetChanged();
            if (!result.isEmpty()) {
                for (ChooseVideoGridItem item : result) {
                    mAdapter.add(item);
                }
                mAdapter.notifyDataSetChanged();
            }
            return;
        }

        try {
            mAdapter = new ChooseVideoAdapter(this, R.layout.choose_image_list_item, result, mImageFetcher);
            mGridView.setAdapter(mAdapter);
            // check if wifi, then give prompt, save user's choice
            if (needPrompt()) {
                checkNetworkStatus();
                //if (network_status == 2) {
                showPrompt();
                //}
            }
        } catch (Exception e) {
            Log.e(MainActivity.LOG_TAG, "Exception caught(ChooseVideoActivity---onRefreshComplete): " + e.getMessage());
        }
    }

    private void close() {
        finish();
        overridePendingTransition(R.anim.enter_from_left, R.anim.exit_to_right);
    }

    // use back button to navigate backward
    @Override
    public boolean onKeyDown(int keyCode, @NonNull KeyEvent event) {
        // check if the key event was the Back button and if there's history
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_BACK:
                    close();
                    //return super.onKeyDown(keyCode, event);
                    return true;
            }
        }

        // If it wasn't the Back key or there's no web page history, bubble up to the default
        // system behavior (probably exit the activity)
        return super.onKeyDown(keyCode, event);
    }

    private ArrayList<ChooseVideoGridItem> getGalleryVideos() {
        ArrayList<ChooseVideoGridItem> galleryList = new ArrayList<>();
        final String[] columns = {MediaStore.Video.Media.DATA,
                MediaStore.Video.Media._ID};
        final String[] thumb_columns = {MediaStore.Video.Thumbnails.DATA, MediaStore.Video.Thumbnails.VIDEO_ID};
        final String orderBy = MediaStore.Video.Media._ID;
        try {
            Cursor videoCursor = getContentResolver().query(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI, columns,
                    null, null, orderBy);
            try {
                if (videoCursor != null && videoCursor.getCount() > 0) {

                    while (videoCursor.moveToNext()) {
                        ChooseVideoGridItem item = new ChooseVideoGridItem();

                        int dataColumnIndex = videoCursor
                                .getColumnIndex(MediaStore.Video.Media.DATA);
                        item.setVideoURL(videoCursor.getString(dataColumnIndex));

                        // get thumbnail
                        int id = videoCursor.getInt(videoCursor.getColumnIndex(MediaStore.Video.Media._ID));
                        Cursor thumbCursor = getContentResolver().query(MediaStore.Video.Thumbnails.EXTERNAL_CONTENT_URI,
                                thumb_columns, MediaStore.Video.Thumbnails.VIDEO_ID + "=" + id, null, null);

                        try {
                            if (thumbCursor != null) {
                                if (thumbCursor.moveToFirst()) {
                                    item.setThumbURL(thumbCursor.getString(thumbCursor.getColumnIndex(MediaStore.Video.Thumbnails.DATA)));
                                } else {
                                    // create thumbnail
                                    int cou = thumbCursor.getCount();
                                    if (cou == 0) {
                                        MediaStore.Video.Thumbnails.getThumbnail(getContentResolver(), id,
                                                MediaStore.Images.Thumbnails.MICRO_KIND, null);
                                        //Log.d(MainActivity.LOG_TAG, "bu kai xin");
                                    }
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            try {
                                if (thumbCursor != null) {
                                    thumbCursor.close();
                                }
                            } catch (Exception e) {
                                Log.e(MainActivity.LOG_TAG, "Exception caught(ChooseVideoActivity---getGalleryVideos---getThumbnail): " + e.getMessage());
                            }
                        }
                        galleryList.add(item);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (videoCursor != null)
                        videoCursor.close();
                } catch (Exception e) {
                    Log.e(MainActivity.LOG_TAG, "Exception caught(ChooseVideoActivity---getGalleryVideos): " + e.getMessage());
                }
            }
        } catch (Exception e) {
            Log.e(MainActivity.LOG_TAG, e.getMessage());
        }

        // show newest photo at beginning of the list
        Collections.reverse(galleryList);
        return galleryList;
    }

    private void complete() {
        if (mAdapter == null) {
            close();
            return;
        }
        if (mAdapter.isSelectionEmpty())
            return;

        showInputDialog(mAdapter.getSelection());
    }

    private void preview() {
        // start photo activity, DO NOT finish current activity
        if (mAdapter.isSelectionEmpty())
            return;

        try {
            openVideoViewActivity(mAdapter.getSelection());
        } catch (Exception e) {
            Log.e(MainActivity.LOG_TAG, "Exception caught(ChooseVideoActivity---preview): " + e.getMessage());
        }
    }

    private void captureVideo() {
        // 1080p --> 480p
        Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        // only 0, 1 can be set o->3GP --- lowest, 1->720p --- highest.
        // takeVideoIntent.putExtra(android.provider.MediaStore.EXTRA_VIDEO_QUALITY, 1);
        if (takeVideoIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takeVideoIntent, MainActivity.REQUEST_VIDEO_CAPTURE);
        }
    }

    private void chooseVideo() {
        Intent intent = new Intent();
        intent.setType("video/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);   // ACTION_GET_CONTENT, ACTION_PICK
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(Intent.createChooser(intent, "Select a video"), MainActivity.REQUEST_VIDEO_CAPTURE);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == MainActivity.REQUEST_VIDEO_CAPTURE && resultCode == RESULT_OK) {
            Uri videoUri = data.getData();
            showInputDialog(getPath(videoUri));
        } else if (requestCode == MainActivity.REQUEST_SELECT_VIDEO && resultCode == RESULT_OK) {
            Uri videoUri = data.getData();
            //Log.d("select video %s", getPath(videoUri));

            // show input dialog
            showInputDialog(getPath(videoUri));
        }
    }

    private String getPath(Uri uri) {
        String[] projection = {MediaStore.Video.Media.DATA};
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        try {
            if (cursor != null) {
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
                cursor.moveToFirst();
                return cursor.getString(column_index);
            } else {
                return null;
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
            return null;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private void openVideoViewActivity(String video) {
        Intent intent = new Intent(this, VideoViewActivity.class);
        if (video != null)
            intent.putExtra(MainActivity.BUNDLE_KEY_CONTENT_URL,
                    video);
        startActivity(intent);
        overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
    }

    protected void showInputDialog(final String video) {
        if (TextUtils.isEmpty(video))
            return;
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        //use one of overloaded setDataSource() functions to set your data source
        final File file = new File(video);

        retriever.setDataSource(getApplicationContext(), Uri.fromFile(file));
        String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        long duration = Long.parseLong(time); // millisecond
        if (duration < 10 * 1000) {
            //
            new AlertDialog.Builder(ChooseVideoActivity.this)
                    .setTitle("Warning")
                    .setMessage("Can not upload video. Please choose those duration are greater than 10s.")
                    .create()
                    .show();
            return;
        }

        LayoutInflater layoutInflater = LayoutInflater.from(ChooseVideoActivity.this);
        View promptView = layoutInflater.inflate(R.layout.input_dialog, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ChooseVideoActivity.this);
        alertDialogBuilder.setView(promptView);

        final EditText edit_title = (EditText) promptView.findViewById(R.id.edit_title);
        final EditText edit_description = (EditText) promptView.findViewById(R.id.edit_description);
        alertDialogBuilder.setCancelable(false)
                .setPositiveButton("Upload", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String owner = MainActivity.USER;
                        String name = "name of video";
                        String title = edit_title.getText().toString();
                        String description = edit_description.getText().toString();

                        String post_string;
                        try {
                            JSONObject jsonObject = new JSONObject();
                            jsonObject.put("owner", !owner.isEmpty() ? owner : "owner");
                            jsonObject.put("title", !title.isEmpty() ? title : "title");
                            jsonObject.put("name", !name.isEmpty() ? name : "name");
                            jsonObject.put("description", !description.isEmpty() ? description : "description");
                            post_string = jsonObject.toString();
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                            return;
                        }

//                        // hide soft keyboard
//                        final InputMethodManager imm1 = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
//                        imm1.hideSoftInputFromWindow(promptView.getWindowToken(), 0);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
                            // allow async task to run simultaneously
                            new UploadTask(file.length()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, video, post_string);
                        else
                            new UploadTask(file.length()).execute(video, post_string);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        alertDialogBuilder.create().show();
    }

    public class UploadTask extends AsyncTask<String, Integer, Boolean> {
        //private Exception exception;
        private ProgressDialog progressDialog;
        private HttpClient httpClient;
        private long fileLength;

        public UploadTask(long fileLength) {
            this.fileLength = fileLength;
        }

        // once worked, encapsulated in a thread
        private boolean uploadVideo(String videoPath, String postString) throws ParseException, IOException {
            if (videoPath == null)
                return false;
            httpClient = new DefaultHttpClient();

            HttpPost httpPost = new HttpPost(getString(R.string.video_upload_url));

            FileBody fileBodyVideo = new FileBody(new File(videoPath));
            //StringBody owner = new StringBody(videoOwner, ContentType.TEXT_PLAIN);
            StringBody post = new StringBody(postString, ContentType.TEXT_PLAIN);

            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);

            builder.addPart("video", fileBodyVideo);
            //builder.addPart("owner", owner);
            builder.addPart("post_string", post);

            // progress listener
            VideoDownloadHttpEntity.ProgressListener progressListener = new VideoDownloadHttpEntity.ProgressListener() {
                @Override
                public void transferred(float progress) {
                    UploadTask.this.publishProgress((int) progress);
                }
            };
            try {
                //httpPost.setEntity(builder.build());
                httpPost.setEntity(new VideoDownloadHttpEntity(builder.build(), progressListener));
                //httpPost.abort();
            } catch (NoSuchFieldError e) {
                io.vov.vitamio.utils.Log.e("no such file %s", e.getMessage());
                return false;
            }

            try {
                //Log.d("executing request %s", httpPost.getRequestLine());
                HttpResponse response = httpClient.execute(httpPost);
                HttpEntity resEntity = response.getEntity();

                //Log.d("response %s", response.getStatusLine());
                if (resEntity != null) {
                    if (response.getStatusLine().getStatusCode() == HttpURLConnection.HTTP_OK) {
                        io.vov.vitamio.utils.Log.d("-----------%s-----------", EntityUtils.toString(resEntity));
                    }
                    resEntity.consumeContent();
                }
                return true;

            } catch (NetworkOnMainThreadException e) {
                e.printStackTrace();
                return false;
            } finally {
                httpClient.getConnectionManager().shutdown();
            }
        }

        @Override
        protected void onPreExecute() {
            this.progressDialog = new ProgressDialog(ChooseVideoActivity.this);
            this.progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            this.progressDialog.setCancelable(false);
            this.progressDialog.setMessage("File Size:" + KecUtilities.formatSize(fileLength));
            this.progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    cancel(true);
                    if (httpClient != null)
                        httpClient.getConnectionManager().shutdown();
                }
            });
            this.progressDialog.show();
        }

        @Override
        protected Boolean doInBackground(String... params) {
            // video, post_string
            try {
                return uploadVideo(params[0], params[1]);
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            httpClient = null;
            if (this.progressDialog != null) {
                this.progressDialog.dismiss();
            }
            if (!success) {
                Toast.makeText(getApplicationContext(), "upload failed.", Toast.LENGTH_SHORT).show();
            }
            if (success)
                close();
        }

        @Override
        protected void onCancelled() {
            //httpPost.abort();
            httpClient = null;
            if (this.progressDialog != null) {
                this.progressDialog.dismiss();
            }
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            this.progressDialog.setProgress(progress[0]);
        }
    }

    private void checkNetworkStatus() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);

        if (connectivityManager != null) {
            NetworkInfo wifi = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            NetworkInfo mobile = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

            if (wifi.isAvailable() && wifi.isConnectedOrConnecting()) {
                Log.d(MainActivity.LOG_TAG, "******* using wifi ******");
                network_status = 1;
            } else if (mobile.isAvailable() && mobile.isConnectedOrConnecting()) {
                Log.d(MainActivity.LOG_TAG, "******* using cellular data *******");
                network_status = 2;
            } else {
                Log.d(MainActivity.LOG_TAG, "******* no network *******");
                network_status = -1;
            }
        } else {
            Log.d(MainActivity.LOG_TAG, "******* no network *******");
            network_status = -1;
        }
    }

    private boolean needPrompt() {
        // read from sp first
        try {
            return getSharedPreferences(MainActivity.SHARED_PREFERENCE_KEY, android.content.Context.MODE_PRIVATE).getBoolean(MainActivity.NEED_PROMPT_KEY, true);
        } catch (Exception e) {
            e.printStackTrace();
            return true;
        }
    }

    private void showPrompt() {
        // show custom alert dialog
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.prompt_dialog, null);
        final CheckBox checkBox = (CheckBox) dialogView.findViewById(R.id.prompt_dialog_check);
        final TextView checkText = (TextView) dialogView.findViewById(R.id.prompt_dialog_check_text);
        checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bShowPrompt = !bShowPrompt;
                checkBox.setChecked(!bShowPrompt);
            }
        });

        checkText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bShowPrompt = !bShowPrompt;
                checkBox.setChecked(!bShowPrompt);
            }
        });

        dialogBuilder.setView(dialogView);
        dialogBuilder.setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // save user's choice
                        bShowPrompt = !checkBox.isChecked();
                        SharedPreferences userDetails = getSharedPreferences(MainActivity.SHARED_PREFERENCE_KEY, android.content.Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = userDetails.edit();
                        editor.putBoolean(MainActivity.NEED_PROMPT_KEY, bShowPrompt);
                        editor.apply();

                    }
                })
                .setNegativeButton("Settings", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //dialog.cancel();
                        startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                    }
                });

        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();
    }
}
