package com.kectech.android.kectechapp.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.kectech.android.kectechapp.BuildConfig;
import com.kectech.android.kectechapp.R;
import com.kectech.android.kectechapp.listeners.OnSwipeTouchListener;
import com.kectech.android.kectechapp.thirdparty.CacheBitmap.ImageFetcher;
import com.kectech.android.kectechapp.util.KecUtilities;
import com.kectech.android.kectechapp.views.NewPostImageView;

import java.util.ArrayList;


public class NewPostActivity extends Activity {

    private ImageFetcher mImageFetcher;
    private final static int CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 100;
    private final static int CHOOSE_IMAGE_REQUEST_CODE = 200;
    private final int mImageIDs[] = {R.id.post_img1, R.id.post_img2, R.id.post_img3, R.id.post_img4, R.id.post_img5, R.id.post_img6, R.id.post_img7, R.id.post_img8, R.id.post_img9};
    private ArrayList<String> mImages;
    private final static int IMAGE_COUNT_LIMIT = 9;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_post);

        if (BuildConfig.DEBUG) {
            System.gc();
        }
        View.OnClickListener mListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                hideSoftKeyboard();
                // popup menu, to delete/preview or to take photo/choose photo.

                if (v instanceof NewPostImageView) {
                    //Creating the instance of PopupMenu
                    if (((NewPostImageView) v).bDefault) {
                        PopupMenu popup = new PopupMenu(NewPostActivity.this, v);
                        //Inflating the Popup using xml file
                        //Inflating the Popup using xml file
                        //if (((NewPostImageView) v).bDefault)
                            popup.getMenuInflater()
                                    .inflate(R.menu.menu_new_post_popup_choose_or_take, popup.getMenu());
//                    else
//                        popup.getMenuInflater()
//                                .inflate(R.menu.menu_new_post_popup_preview_or_delete, popup.getMenu());

                        //registering popup with OnMenuItemClickListener
                        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                            public boolean onMenuItemClick(MenuItem item) {
                                onOptionsItemSelected(item);
                                return true;
                            }
                        });

                        popup.show(); //showing popup menu
                    }
                }
            }
        };
        // set all imageView onClick listener when show default image.
        for (int i = 0; i < IMAGE_COUNT_LIMIT; i++) {
            NewPostImageView imageView = (NewPostImageView) findViewById(mImageIDs[i]);
            imageView.setOnClickListener(mListener);
            imageView.id = i;
        }

        mImageFetcher = KecUtilities.getThumbFetcher(this);
        View mPostFrame = findViewById(R.id.post_frame);
        // hide soft keyboard when click non TextView area.
        mPostFrame.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final InputMethodManager imm1 = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
                imm1.hideSoftInputFromWindow(v.getWindowToken(), 0);
                return false;
            }
        });

//        Button submit = (Button)findViewById(R.id.post_submit);
        TextView submit = (TextView) findViewById(R.id.post_submit);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadToServer();
            }
        });

        OnSwipeTouchListener swipeTouchListener = new OnSwipeTouchListener() {
            public void onSwipeOutLeft() {
                //close(true);
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

        mImages = new ArrayList<>();

        TextWatcher textWatcher = new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
                //TODO
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
                //TODO
            }

            @Override
            public void afterTextChanged(Editable s) {
                int len = s.toString().length();
                TextView showNumbers = (TextView) findViewById(R.id.post_numbers);
                showNumbers.setText(len + "/140");
            }
        };

        EditText editText = (EditText) findViewById(R.id.post_desc);
        editText.addTextChangedListener(textWatcher);
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
                closeByTask(true);
                return true;
            }
            case R.id.menu_new_post_choose_photo:
                startChooseImageActivityByTask();
                return true;
            case R.id.menu_new_post_take_photo:
                captureImage();
                return true;
            case R.id.menu_new_post_send:
                uploadToServer();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void closeByTask(boolean bBackward) {
        new ExitActivityTask().execute(bBackward);

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
                if (requestCode == CHOOSE_IMAGE_REQUEST_CODE) {
                    ArrayList<String> result = data.getStringArrayListExtra(MainActivity.CHOOSE_IMAGE_RESULT);
                    for (String s : result) {
                        mImages.add(s);
                    }
                    setImages();
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

    private void startChooseImageActivityByTask() {
        // first hide keyboard...
        // then start
        if (mImages.size() == IMAGE_COUNT_LIMIT) {
            showMaxAlert();
            return;
        }

        new StartChoosePhotoActivityTask().execute();
    }

    private void startChooseImageActivity() {
        Intent intent = new Intent(this, ChooseImageActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra(MainActivity.CHOOSE_IMAGE_PARAM, mImages.size());
        try {
            // do not finish, instead call startActivityForResult
            startActivityForResult(intent, CHOOSE_IMAGE_REQUEST_CODE);
            overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
        } catch (Exception e) {
            Log.e(MainActivity.LOG_TAG, "Exception caught: " + e.getMessage());
        }
    }

    private void clearSelection() {
        for (int i = 0; i < IMAGE_COUNT_LIMIT; i++) {
            NewPostImageView imageView = (NewPostImageView) findViewById(mImageIDs[i]);
            imageView.setVisibility(View.GONE);
            imageView.bDefault = true;
        }
        //mImages.clear();
    }

    private void captureImage() {

        if (mImages.size() == IMAGE_COUNT_LIMIT) {
            showMaxAlert();
            return;
        }

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null)
            startActivityForResult(intent, CAMERA_CAPTURE_IMAGE_REQUEST_CODE);
    }

    private String getPath(Uri uri) {

        String[] projection = {MediaStore.Images.Media.DATA};

        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        try {
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
            // always add to the rear if not full
            if (mImages.size() < IMAGE_COUNT_LIMIT) {
                int position = mImages.size();
                NewPostImageView imageView = (NewPostImageView) findViewById(mImageIDs[position]);
                mImageFetcher.loadImage(strImage, imageView);
                imageView.setVisibility(View.VISIBLE);
                imageView.bDefault = false;
                mImages.add(strImage);

                setDefaultImageView(mImages.size());
            }
        }
    }

    private void uploadToServer() {
        // actually is return to tab_main_photo, and let the fragment deal with upload
        TextView textView = (TextView) findViewById(R.id.post_desc);
        String strDesc = textView.getText().toString();
        if (TextUtils.isEmpty(strDesc)) {
            if (mImages == null || mImages.size() == 0) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("At least share a word or a photo.")
                        .setCancelable(false)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                //do things
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();
                return;
            }
        }

        try {
            Intent intent = new Intent();
            intent.putExtra(MainActivity.POST_DESC, strDesc);
            intent.putStringArrayListExtra(MainActivity.POST_IMAGES, mImages);
            setResult(RESULT_OK, intent);
            closeByTask(false);
        } catch (Exception e) {
            Log.e(MainActivity.LOG_TAG, "Exception caught: " + e.getMessage());
        }
    }

    private class ExitActivityTask extends AsyncTask<Boolean, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Boolean... param) {
            Boolean bValue = param[0];
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Log.d(MainActivity.LOG_TAG, e.getMessage());
            }
            return bValue;
        }

        @Override
        protected void onPostExecute(Boolean bBackward) {
            close(bBackward);
        }

        @Override
        protected void onPreExecute() {
            hideSoftKeyboard();
        }
    }

    private class StartChoosePhotoActivityTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... param) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Log.d(MainActivity.LOG_TAG, e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            startChooseImageActivity();
        }

        @Override
        protected void onPreExecute() {
            hideSoftKeyboard();
        }
    }

    private void hideSoftKeyboard() {
        try {
            final InputMethodManager imm1 = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
            imm1.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        } catch (NullPointerException npe) {
            Log.e(MainActivity.LOG_TAG, npe.getMessage());
        }
    }

    private void setDefaultImageView(int position) {
        if (position >= IMAGE_COUNT_LIMIT)
            return;
        NewPostImageView imageView = (NewPostImageView) findViewById(mImageIDs[position]);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            imageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_post_default_img, getTheme()));
            imageView.setBackground(getResources().getDrawable(R.drawable.new_post_image_background_frame, getTheme()));
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            imageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_post_default_img));
            imageView.setBackground(getResources().getDrawable(R.drawable.new_post_image_background_frame));
        } else {
            imageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_post_default_img));
            imageView.setBackgroundDrawable(getResources().getDrawable(R.drawable.new_post_image_background_frame));
        }
        imageView.bDefault = true;
        imageView.setVisibility(View.VISIBLE);
    }

    private void setImages() {
        if (mImages != null) {
            clearSelection();
            for (int i = 0; i < mImages.size(); i++) {
                NewPostImageView imageView = (NewPostImageView) findViewById(mImageIDs[i]);
                mImageFetcher.loadImage(mImages.get(i), imageView);
                imageView.setVisibility(View.VISIBLE);
                imageView.bDefault = false;
            }
            // set next one to default
            if (mImages.size() < IMAGE_COUNT_LIMIT) {
                setDefaultImageView(mImages.size());
            }
        }
    }

    private void showMaxAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Share a maximum of " + IMAGE_COUNT_LIMIT + " photos.")
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //do things
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }
}
