package com.kectech.android.kectechapp.activity;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.kectech.android.kectechapp.BuildConfig;
import com.kectech.android.kectechapp.R;
import com.kectech.android.kectechapp.listeners.OnSwipeTouchListener;
import com.kectech.android.kectechapp.thirdparty.CacheBitmap.ImageFetcher;
import com.kectech.android.kectechapp.util.KecUtilities;
import com.kectech.android.kectechapp.views.ExpandableHeightGridView;
import com.kectech.android.kectechapp.views.NewPostImageView;

import java.util.ArrayList;


public class NewPostActivity extends Activity {

    private ImageFetcher mImageFetcher;
    private final static int CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 100;
    private final static int CHOOSE_IMAGE_REQUEST_CODE = 200;
    //private final int mImageIDs[] = {R.id.post_img1, R.id.post_img2, R.id.post_img3, R.id.post_img4, R.id.post_img5, R.id.post_img6, R.id.post_img7, R.id.post_img8, R.id.post_img9};
    //private ArrayList<String> mImages;
    private final static int IMAGE_COUNT_LIMIT = 9;

    private NewPostGridAdapter mAdapter;
    private boolean bReachLimit = false;
    int current = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_post);

        if (BuildConfig.DEBUG) {
            System.gc();
        }

//        View.OnClickListener mListener = new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                hideSoftKeyboard();
//                // popup menu, to delete/preview or to take photo/choose photo.
//
//                if (v instanceof NewPostImageView) {
//                    //Creating the instance of PopupMenu
//                    if (((NewPostImageView) v).bDefault) {
//                        PopupMenu popup = new PopupMenu(NewPostActivity.this, v);
//                        //Inflating the Popup using xml file
//                        //Inflating the Popup using xml file
//                        //if (((NewPostImageView) v).bDefault)
//                            popup.getMenuInflater()
//                                    .inflate(R.menu.menu_new_post_popup_choose_or_take, popup.getMenu());
////                    else
////                        popup.getMenuInflater()
////                                .inflate(R.menu.menu_new_post_popup_preview_or_delete, popup.getMenu());
//
//                        //registering popup with OnMenuItemClickListener
//                        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
//                            public boolean onMenuItemClick(MenuItem item) {
//                                onOptionsItemSelected(item);
//                                return true;
//                            }
//                        });
//
//                        popup.show(); //showing popup menu
//                    }
//                }
//            }
//        };

//        // set all imageView onClick listener when show default image.
//        for (int i = 0; i < IMAGE_COUNT_LIMIT; i++) {
//            NewPostImageView imageView = (NewPostImageView) findViewById(mImageIDs[i]);
//            imageView.setOnClickListener(mListener);
//            imageView.id = i;
//        }

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

        //mImages = new ArrayList<>();
        ArrayList<String> defaultImage = new ArrayList<>();
        defaultImage.add(MainActivity.NEW_POST_DEFAULT_IMAGE);
        mAdapter = new NewPostGridAdapter(this, R.layout.new_post_grid_item, defaultImage);

        TextWatcher textWatcher = new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                int len = s.toString().length();
                TextView showNumbers = (TextView) findViewById(R.id.post_numbers);
                showNumbers.setText(len + " / 140");
            }
        };

        EditText editText = (EditText) findViewById(R.id.post_desc);
        editText.addTextChangedListener(textWatcher);

        ExpandableHeightGridView gridView = (ExpandableHeightGridView) findViewById(R.id.new_post_img_gridView);
        gridView.setExpanded(true);
        gridView.setAdapter(mAdapter);

//        // click listener
//        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            public void onItemClick(AdapterView<?> parent, View view,
//                                    int position, long id) {
//                hideSoftKeyboard();
//                PopupMenu popup = new PopupMenu(NewPostActivity.this, view);
//                String strUrl = mAdapter.getItem(position);
//                if (strUrl.compareToIgnoreCase(MainActivity.NEW_POST_DEFAULT_IMAGE) == 0) {
//                    // popup menu
//                    popup.getMenuInflater().inflate(R.menu.menu_new_post_popup_choose_or_take, popup.getMenu());
//                } else {
//
//                    current = position;
//                    popup.getMenuInflater().inflate(R.menu.menu_new_post_popup_preview_or_delete, popup.getMenu());
//                }
//                //registering popup with OnMenuItemClickListener
//                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
//                    public boolean onMenuItemClick(MenuItem item) {
//                        onOptionsItemSelected(item);
//                        return true;
//                    }
//                });
//
//                popup.show(); //showing popup menu
//            }
//        });
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
            case R.id.menu_new_post_delete: {
                mAdapter.remove(mAdapter.getItem(current));
                if (bReachLimit) {
                    mAdapter.add(MainActivity.NEW_POST_DEFAULT_IMAGE);
                    bReachLimit =false;
                }
                return true;
            }
            case R.id.menu_new_post_preview:
                preview(current);
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
//                    for (String s : result) {
//                        mImages.add(s);
//                    }
                    setImages(result);
                } else if (requestCode == CAMERA_CAPTURE_IMAGE_REQUEST_CODE) {
                    new getCapturedImageTask().execute(data);
                }
            } catch (Exception e) {
                Log.e(MainActivity.LOG_TAG, "Exception caught(NewPostActivity---onActivityResult): " + e.getMessage());
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
        if (bReachLimit) {
            showMaxAlert();
            return;
        }

        new StartChoosePhotoActivityTask().execute();
    }

    private void startChooseImageActivity() {
        Intent intent = new Intent(this, ChooseImageActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra(MainActivity.CHOOSE_IMAGE_PARAM, mAdapter.getCount() - 1);
        try {
            // do not finish, instead call startActivityForResult
            startActivityForResult(intent, CHOOSE_IMAGE_REQUEST_CODE);
            overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
        } catch (Exception e) {
            Log.e(MainActivity.LOG_TAG, "Exception caught(NewPostActivity---startChooseImageActivity): " + e.getMessage());
        }
    }

//    private void clearSelection() {
//        for (int i = 0; i < IMAGE_COUNT_LIMIT; i++) {
//            NewPostImageView imageView = (NewPostImageView) findViewById(mImageIDs[i]);
//            imageView.setVisibility(View.GONE);
//            imageView.bDefault = true;
//        }
//        //mImages.clear();
//    }

    private void captureImage() {

        if (bReachLimit) {
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
//            if (mImages.size() < IMAGE_COUNT_LIMIT) {
//                int position = mImages.size();
//                NewPostImageView imageView = (NewPostImageView) findViewById(mImageIDs[position]);
//                mImageFetcher.loadImage(strImage, imageView);
//                imageView.setVisibility(View.VISIBLE);
//                imageView.bDefault = false;
//                mImages.add(strImage);
//
//                setDefaultImageView(mImages.size());
//            }

            int position = mAdapter.getCount() - 1;
            mAdapter.insert(strImage, position);

            if (mAdapter.getCount() > IMAGE_COUNT_LIMIT) {
                mAdapter.remove(MainActivity.NEW_POST_DEFAULT_IMAGE);
                bReachLimit = true;
            } else {
                bReachLimit = false;
            }
        }
    }

    private void uploadToServer() {
        // actually is return to tab_main_photo, and let the fragment deal with upload
        TextView textView = (TextView) findViewById(R.id.post_desc);
        String strDesc = textView.getText().toString();
        if (TextUtils.isEmpty(strDesc)) {
            if (mAdapter.getCount() == 1) {
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
            ArrayList<String> images = new ArrayList<>();
            for (int i = 0; i < mAdapter.getCount(); i++) {
                if (mAdapter.getItem(i).compareToIgnoreCase(MainActivity.NEW_POST_DEFAULT_IMAGE) == 0)
                    continue;
                images.add(mAdapter.getItem(i));
            }
            intent.putStringArrayListExtra(MainActivity.POST_IMAGES, images);
            setResult(RESULT_OK, intent);
            closeByTask(false);
        } catch (Exception e) {
            Log.e(MainActivity.LOG_TAG, "Exception caught(NewPostActivity---uploadToServer): " + e.getMessage());
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

//    private void setDefaultImageView(int position) {
//        if (position >= IMAGE_COUNT_LIMIT)
//            return;
//        NewPostImageView imageView = (NewPostImageView) findViewById(mImageIDs[position]);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            imageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_post_default_img, getTheme()));
//            imageView.setBackground(getResources().getDrawable(R.drawable.new_post_image_background_frame, getTheme()));
//        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
//            imageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_post_default_img));
//            imageView.setBackground(getResources().getDrawable(R.drawable.new_post_image_background_frame));
//        } else {
//            imageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_post_default_img));
//            imageView.setBackgroundDrawable(getResources().getDrawable(R.drawable.new_post_image_background_frame));
//        }
//        imageView.bDefault = true;
//        imageView.setVisibility(View.VISIBLE);
//    }

    private void setImages(ArrayList<String> result) {
        if (result != null) {
//            clearSelection();
//            for (int i = 0; i < mImages.size(); i++) {
//                NewPostImageView imageView = (NewPostImageView) findViewById(mImageIDs[i]);
//                mImageFetcher.loadImage(mImages.get(i), imageView);
//                imageView.setVisibility(View.VISIBLE);
//                imageView.bDefault = false;
//            }
//            // set next one to default
//            if (mImages.size() < IMAGE_COUNT_LIMIT) {
//                setDefaultImageView(mImages.size());
//            }


            int total = result.size() + mAdapter.getCount();
            if (mAdapter.getCount() == 1) {
                // default image always be the last one, except total > IMAGE_COUNT_LIMIT, remove default;
                // always insert reversely
                for (int position = result.size() - 1; position >= 0; position--) {
                    mAdapter.insert(result.get(position), 0);
                }
            } else {
                // add to the rear, before default
                for (String s : result) {
                    int last = mAdapter.getCount() - 1;
                    mAdapter.insert(s, last);
                }
            }

            if (total > IMAGE_COUNT_LIMIT) {
                mAdapter.remove(MainActivity.NEW_POST_DEFAULT_IMAGE);
                bReachLimit = true;
            } else {
                bReachLimit = false;
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

    private void preview(int position) {
        // start photo activity, DO NOT finish current activity

        Intent intent = new Intent(this, PhotoOfHallOfMainActivity.class);
        ArrayList<String> images = new ArrayList<>();
        for (int i = 0; i < mAdapter.getCount(); i++) {
            if (mAdapter.getItem(i).compareToIgnoreCase(MainActivity.NEW_POST_DEFAULT_IMAGE) == 0)
                continue;
            images.add(mAdapter.getItem(i));
        }
        Bundle params = new Bundle();
        params.putStringArrayList(MainActivity.PHOTO_TAB_IMAGE_URL_KEY, images);
        params.putInt(MainActivity.PHOTO_TAB_IMAGE_INDEX_KEY, position);

        intent.putExtras(params);
        try {
            startActivity(intent);
            overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
        } catch (Exception e) {
            Log.e(MainActivity.LOG_TAG, "Exception caught(NewPostActivity---preview): " + e.getMessage());
        }
    }

    private class NewPostGridAdapter extends ArrayAdapter<String> {

        public NewPostGridAdapter(Context context, int resourceId, ArrayList<String> items) {
            super(context, resourceId, items);
        }

        // private view holder class
        private class ViewHolder {
            NewPostImageView imageView;
            NewPostImageView removeButton;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            String strUrl = getItem(position);

            LayoutInflater layoutInflater = (LayoutInflater)getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

            try {
                if (convertView == null) {
                    convertView = layoutInflater.inflate(R.layout.new_post_grid_item, parent, false);
                    holder = new ViewHolder();
                    holder.imageView = (NewPostImageView) convertView.findViewById(R.id.item_image);
                    holder.removeButton = (NewPostImageView)convertView.findViewById(R.id.item_remove);
                    convertView.setTag(holder);
                } else
                    holder = (ViewHolder) convertView.getTag();
            } catch (Exception e) {
                Log.e(MainActivity.LOG_TAG, "Exception caught(NewPostGridAdapter): " + e.getMessage());
                return convertView;
            }

            if (strUrl.compareToIgnoreCase(MainActivity.NEW_POST_DEFAULT_IMAGE) == 0) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    holder.imageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_post_default_img, getTheme()));
                    holder.imageView.setBackground(getResources().getDrawable(R.drawable.new_post_image_background_frame, getTheme()));
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    holder.imageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_post_default_img));
                    holder.imageView.setBackground(getResources().getDrawable(R.drawable.new_post_image_background_frame));
                } else {
                    holder.imageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_post_default_img));
                    holder.imageView.setBackgroundDrawable(getResources().getDrawable(R.drawable.new_post_image_background_frame));
                }
                holder.removeButton.setVisibility(View.GONE);
            } else {
                mImageFetcher.loadImage(strUrl, holder.imageView);
                holder.removeButton.setVisibility(View.VISIBLE);
            }

            holder.imageView.position = position;
            holder.removeButton.position = position;

            holder.removeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    if (v instanceof NewPostImageView) {
                        current = ((NewPostImageView)v).position;

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                            // try use some animation
                            // but need api 16....
                            v.animate().setDuration(300).alpha(0).withEndAction(new Runnable() {
                                @Override
                                public void run() {
                                    mAdapter.remove(mAdapter.getItem(current));
                                    v.setAlpha(1);
                                    if (bReachLimit) {
                                        mAdapter.add(MainActivity.NEW_POST_DEFAULT_IMAGE);
                                        bReachLimit = false;
                                    }
                                }
                            });
                        } else {
                            // no animation
                            mAdapter.remove(mAdapter.getItem(current));
                            if (bReachLimit) {
                                mAdapter.add(MainActivity.NEW_POST_DEFAULT_IMAGE);
                                bReachLimit = false;
                            }

                            // setHasTransientState also need api 16
                            // but for ViewCompact can use static setHasTransientState(v, false);
                            // however, wo are using view.... so

//                            ObjectAnimator animator = ObjectAnimator.ofFloat(v, View.ALPHA, 0);
//                            animator.setDuration(300);
//                            v.setHasTransientState(true);
//
//                            animator.addListener(new Animator.AnimatorListener() {
//                                @Override
//                                public void onAnimationStart(Animator animation) {
//
//                                }
//
//                                @Override
//                                public void onAnimationEnd(Animator animation) {
//                                    mAdapter.remove(mAdapter.getItem(current));
//                                    if (bReachLimit) {
//                                        mAdapter.add(MainActivity.NEW_POST_DEFAULT_IMAGE);
//                                        bReachLimit = false;
//                                    }
//                                    v.setAlpha(1);
//                                    v.setHasTransientState(false);
//                                }
//
//                                @Override
//                                public void onAnimationCancel(Animator animation) {
//
//                                }
//
//                                @Override
//                                public void onAnimationRepeat(Animator animation) {
//
//                                }
//                            });
//
//
//                            animator.start();
                        }
                    }
                }
            });

            holder.imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (v instanceof NewPostImageView) {
                        current = ((NewPostImageView)v).position;

                        hideSoftKeyboard();

                        String strUrl = mAdapter.getItem(current);
                        if (strUrl.compareToIgnoreCase(MainActivity.NEW_POST_DEFAULT_IMAGE) == 0) {
                            // popup menu
                            PopupMenu popup = new PopupMenu(NewPostActivity.this, v);
                            popup.getMenuInflater().inflate(R.menu.menu_new_post_popup_choose_or_take, popup.getMenu());
                            //registering popup with OnMenuItemClickListener
                            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                                public boolean onMenuItemClick(MenuItem item) {
                                    onOptionsItemSelected(item);
                                    return true;
                                }
                            });
                            popup.show(); //showing popup menu
                        } else {
                            preview(current);
                        }

                    }
                }
            });

            return convertView;
        }

    }
}
