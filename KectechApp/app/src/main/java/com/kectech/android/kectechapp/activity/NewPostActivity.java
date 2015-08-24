package com.kectech.android.kectechapp.activity;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.media.Image;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.kectech.android.kectechapp.BuildConfig;
import com.kectech.android.kectechapp.R;
import com.kectech.android.kectechapp.listeners.OnSwipeTouchListener;
import com.kectech.android.kectechapp.thirdparty.CacheBitmap.ImageFetcher;
import com.kectech.android.kectechapp.util.KecUtilities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;


public class NewPostActivity extends Activity {

    private View mPostFrame;
    private ImageFetcher mImageFetcher;
    private final static int CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 100;
    private final static int CHOOSE_IMAGE_REQUEST_CODE = 200;
    private final int mImageIDs[] = {R.id.post_img1, R.id.post_img2, R.id.post_img3, R.id.post_img4, R.id.post_img5, R.id.post_img6, R.id.post_img7, R.id.post_img8, R.id.post_img9};
//    private View.OnClickListener mListener;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_post);

        if (BuildConfig.DEBUG)
        {
            System.gc();
        }
//        mListener = new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                // TODO: 24/08/2015 if has img, do nothing or preview in the future, then start choose image activity.
//                startChooseImageActivity();
//            }
//        };
//        // set all imageView onClick listener when show default image.
//        for (int i = 0; i < 9; i++) {
//            ImageView imageView = (ImageView)findViewById(mImageIDs[i]);
//            imageView.setOnClickListener(mListener);
//        }

        mImageFetcher = KecUtilities.getThumbFetcher(this);
        mPostFrame = findViewById(R.id.post_frame);
        // hide soft keyboard when click non TextView area.
        mPostFrame.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final InputMethodManager imm1 = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
                imm1.hideSoftInputFromWindow(v.getWindowToken(), 0);
                return false;
            }
        });

        Button submit = (Button)findViewById(R.id.post_submit);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent intent = new Intent();
//                intent.setType("image/*");
//                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
//                intent.setAction(Intent.ACTION_GET_CONTENT);
//                startActivityForResult(Intent.createChooser(intent,"Select Picture"), 1);
            }
        });

        OnSwipeTouchListener swipeTouchListener = new OnSwipeTouchListener() {
            public void onSwipeOutLeft() {
                //getFragmentManager().popBackStack();
                close(true);
            }
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // hide soft keyboard when click non TextView area.
                // then determine if need  swipe out
                final InputMethodManager imm1 = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
                imm1.hideSoftInputFromWindow(v.getWindowToken(), 0);
                return super.onTouch(v, event);
            }
        };

        mPostFrame.setOnTouchListener(swipeTouchListener);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_new_post, menu);
        return true;
    }

    // use back button to navigate backward
    @Override
    public boolean onKeyDown(int keyCode, @NonNull KeyEvent event) {
        // check if the key event was the Back button and if there's history
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_BACK:
                    close(true);
                    return super.onKeyDown(keyCode, event);
            }
        }

        // If it wasn't the Back key or there's no web page history, bubble up to the default
        // system behavior (probably exit the activity)
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case android.R.id.home: {
                close(true);
                return true;
            }
            case R.id.menu_new_post_choose_photo:
                // hide keyboard
                // start choose images activity
                startChooseImageActivity();
                return true;
            case R.id.menu_new_post_take_photo:
                captureImage();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void close(boolean bBackward) {
        finish();
        if (bBackward)
            overridePendingTransition(R.anim.enter_from_left, R.anim.exit_to_right);
        else
            overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            try {
                clearSelection();
                if (requestCode == CHOOSE_IMAGE_REQUEST_CODE) {
                    ArrayList<String> result = data.getStringArrayListExtra(MainActivity.CHOOSE_IMAGE_RESULT);
                    if (result != null) {
                        for (int i = 0; i < result.size(); i++) {
                            ImageView imageView = (ImageView) findViewById(mImageIDs[i]);
                            mImageFetcher.loadImage(result.get(i), imageView);
                            imageView.setVisibility(View.VISIBLE);
                        }
                    }
                } else if (requestCode == CAMERA_CAPTURE_IMAGE_REQUEST_CODE) {
                    new getCapturedImageTask().execute(data);
                }
            } catch (Exception e) {
                Log.e(MainActivity.LOG_TAG, "Exception caught: " + e.getMessage());
            }
        }
    }
    @Override
    public void onResume() {
        super.onResume();
        mImageFetcher.setExitTasksEarly(false);
    }

    @Override
    public void onPause() {
        super.onPause();
        mImageFetcher.setPauseWork(false);
        mImageFetcher.setExitTasksEarly(true);
        mImageFetcher.flushCache();
    }

    private void startChooseImageActivity() {
        Intent intent = new Intent(this, ChooseImageActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        try {
            // do not finish, instead call startActivityForResult
            startActivityForResult(intent, CHOOSE_IMAGE_REQUEST_CODE);
            overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
        } catch (Exception e) {
            Log.e(MainActivity.LOG_TAG, "Exception caught: " + e.getMessage());

        }
    }

    private void clearSelection() {
        for (int i = 0; i < 9; i++) {
            ImageView imageView = (ImageView) findViewById(mImageIDs[i]);
            imageView.setVisibility(View.GONE);
        }
    }

    private void captureImage() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null)
            startActivityForResult(intent, CAMERA_CAPTURE_IMAGE_REQUEST_CODE);
    }

    private String getPath(Uri uri) {

        String[] projection = { MediaStore.Images.Media.DATA };

        Cursor cursor = getContentResolver().query(uri, projection, null, null,null);
        try
        {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();

            return cursor.getString(column_index);
        } finally {
            cursor.close();
        }
    }

    private class getCapturedImageTask extends AsyncTask<Intent, Void, String> {

        @Override
        protected String doInBackground(Intent... param) {
            return getPath(param[0].getData());
        }

        @Override
        protected void onPostExecute(String strImage) {
            ImageView imageView = (ImageView) findViewById(mImageIDs[0]);
            mImageFetcher.loadImage(strImage, imageView);
            imageView.setVisibility(View.VISIBLE);
        }
    }

    // after submit
    // should return to post activity(photo activity)
    // then do the upload work...
    // put the latest post in the beginning. show progress bar until success or fail.


    // upload images and post to server
    private class uploadToServer extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {
            // 0 -- post can be null or empty
            // 1- 9 images

        }
    }
}
