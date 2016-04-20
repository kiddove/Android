package com.kectech.android.wyslink.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;

import com.kectech.android.wyslink.BuildConfig;
import com.kectech.android.wyslink.R;
import com.kectech.android.wyslink.adapter.ChooseImageAdapter;
import com.kectech.android.wyslink.listeners.OnSwipeTouchListener;
import com.kectech.android.wyslink.listitem.ChooseImageGridItem;
import com.kectech.android.wyslink.thirdparty.CacheBitmap.ImageFetcher;
import com.kectech.android.wyslink.thirdparty.CacheBitmap.Utils;
import com.kectech.android.wyslink.util.KecUtilities;

import java.util.ArrayList;
import java.util.Collections;

/*
* DO NOT need CAB...
* */
public class ChooseImageActivity extends Activity {

    private GridView mGridView;
    private ChooseImageAdapter mAdapter;
    private ImageFetcher mImageFetcher;
    private int num = 0;
    private TextView textDone;
    private TextView textPreview;
    private int limit = MainActivity.IMAGE_LIMIT_NUMBER;

    @Override
    @SuppressWarnings("deprecation")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_image);
        if (BuildConfig.DEBUG) {
            System.gc();
        }
        // start get data first
        initList();
        Intent intent = getIntent();
        if (intent != null) {
            int imageCount = intent.getIntExtra(MainActivity.CHOOSE_IMAGE_PARAM, 0);
            limit = limit - imageCount;
        }
        mImageFetcher = KecUtilities.getThumbFetcher(this);
        mGridView = (GridView) findViewById(R.id.choose_img_gridView);

        // click listener
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                boolean isChecked = mAdapter.isChecked(position);
                onItemChecked(position, !isChecked, view);
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
                complete(false);
            }
        });

        textPreview = (TextView) findViewById(R.id.choose_img_preview);
        textPreview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                preview();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_choose_image, menu);
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
            case R.id.choose_image_done:
                complete(true);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void onItemChecked(int position, boolean isChecked, View v) {
        if (isChecked) {
            num++;
            mAdapter.setSelection(position, true);
            // set check
        } else {
            num--;
            mAdapter.removeSelect(position);
        }

        if (num > limit) {
            num--;
            mAdapter.removeSelect(position);
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Select a maximum of " + limit + " photos.")
                    .setCancelable(false)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            //do things
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();

        } else {

            mAdapter.changeSelection(v, isChecked);
            if (num > 0) {
                setTitle(num + " selected.");

                textPreview.setTextColor(getResources().getColorStateList(R.color.bar_background));
                textDone.setTextColor(getResources().getColorStateList(R.color.tab_selected));
            } else {
                setTitle(R.string.prompt_navigation_back);

                textPreview.setTextColor(getResources().getColorStateList(R.color.post_img_background));
                textDone.setTextColor(getResources().getColorStateList(R.color.tab_selected_disable));
            }
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

    private class InitListTask extends AsyncTask<Void, Void, ArrayList<ChooseImageGridItem>> {
        @Override
        protected ArrayList<ChooseImageGridItem> doInBackground(Void... params) {
            return getGalleryPhotos();
        }

        @Override
        protected void onPostExecute(ArrayList<ChooseImageGridItem> result) {
            super.onPostExecute(result);
            if (isCancelled())
                return;

            if (result != null && !result.isEmpty())
                onRefreshComplete(result);
        }
    }

    private void onRefreshComplete(final ArrayList<ChooseImageGridItem> result) {
        if (result == null)
            // means error occurred when sending http request, other wise result should be empty but not null
            return;
        if (mAdapter != null) {
            if (BuildConfig.DEBUG)
                Log.d(MainActivity.LOG_TAG, "ListView(mAdapter) already had data, and will be cleared...");

            mAdapter.clear();
            mAdapter.notifyDataSetChanged();
            if (!result.isEmpty()) {
                for (ChooseImageGridItem item : result) {
                    mAdapter.add(item);
                }
                mAdapter.notifyDataSetChanged();
            }
            return;
        }

        try {
            mAdapter = new ChooseImageAdapter(this, R.layout.choose_image_list_item, result, mImageFetcher);
            mGridView.setAdapter(mAdapter);

        } catch (Exception e) {
            Log.e(MainActivity.LOG_TAG, "Exception caught(ChooseImageActivity---onRefreshComplete): " + e.getMessage());
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

    @SuppressWarnings("deprecation")
    private ArrayList<ChooseImageGridItem> getGalleryPhotos() {
        ArrayList<ChooseImageGridItem> galleryList = new ArrayList<>();
        final String[] columns = {MediaStore.Images.Media.DATA,
                MediaStore.Images.Media._ID};
        final String orderBy = MediaStore.Images.Media._ID;
        try {
            Cursor imageCursor = getContentResolver().query(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns,
                    null, null, orderBy);
            try {
                if (imageCursor != null && imageCursor.getCount() > 0) {

                    while (imageCursor.moveToNext()) {
                        ChooseImageGridItem item = new ChooseImageGridItem();

                        int dataColumnIndex = imageCursor
                                .getColumnIndex(MediaStore.Images.Media.DATA);

                        item.setImageURL(imageCursor.getString(dataColumnIndex));

                        galleryList.add(item);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (imageCursor != null)
                        imageCursor.close();
                } catch (Exception e) {
                    Log.e(MainActivity.LOG_TAG, "Exception caught(ChooseImageActivity---getGalleryPhotos): " + e.getMessage());
                }
            }
        } catch (Exception e) {
            Log.e(MainActivity.LOG_TAG, e.getMessage());
        }

        // show newest photo at beginning of the list
        Collections.reverse(galleryList);
        return galleryList;
    }

    private void complete(boolean bFromActionBar) {
        if (mAdapter == null) {
            close();
            return;
        }
        if (!bFromActionBar && mAdapter.isSelectionEmpty())
            return;
        Intent intent = new Intent();
        try {
            intent.putStringArrayListExtra(MainActivity.CHOOSE_IMAGE_RESULT, mAdapter.getSelection());
            setResult(RESULT_OK, intent);
            close();
        } catch (Exception e) {
            Log.e(MainActivity.LOG_TAG, "Exception caught(ChooseImageActivity---complete): " + e.getMessage());
            close();
        }
    }

    private void preview() {
        // start photo activity, DO NOT finish current activity
        if (mAdapter.isSelectionEmpty())
            return;

        Intent intent = new Intent(this, PhotoOfShowOfMainActivity.class);

        Bundle params = new Bundle();
        params.putStringArrayList(MainActivity.PHOTO_TAB_IMAGE_URL_KEY, mAdapter.getSelection());

        intent.putExtras(params);
        try {
            startActivity(intent);
            overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
        } catch (Exception e) {
            Log.e(MainActivity.LOG_TAG, "Exception caught(ChooseImageActivity---preview): " + e.getMessage());
        }
    }
}
