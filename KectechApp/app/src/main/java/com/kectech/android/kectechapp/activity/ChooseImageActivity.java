package com.kectech.android.kectechapp.activity;

import android.annotation.TargetApi;
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
import android.view.ViewTreeObserver;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;

import com.kectech.android.kectechapp.BuildConfig;
import com.kectech.android.kectechapp.R;
import com.kectech.android.kectechapp.adapter.ChooseImageAdapter;
import com.kectech.android.kectechapp.listeners.OnSwipeTouchListener;
import com.kectech.android.kectechapp.listitem.ChooseImageListItem;
import com.kectech.android.kectechapp.thirdparty.CacheBitmap.ImageFetcher;
import com.kectech.android.kectechapp.thirdparty.CacheBitmap.Utils;
import com.kectech.android.kectechapp.util.KecUtilities;

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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_image);
        if (BuildConfig.DEBUG)
        {
            System.gc();
        }
        // start get data first
        initList();
        mImageFetcher = KecUtilities.getThumbFetcher(this);
        mGridView = (GridView)findViewById(R.id.choose_img_gridView);

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

        final int mImageThumbSize = getResources().getDimensionPixelSize(R.dimen.image_thumbnail_size);
        final int mImageThumbSpacing = getResources().getDimensionPixelSize(R.dimen.image_thumbnail_spacing);
        // This listener is used to get the final width of the GridView and then calculate the
        // number of columns and the width of each column. The width of each column is variable
        // as the GridView has stretchMode=columnWidth. The column width is used to set the height
        // of each view so we get nice square thumbnails.
        mGridView.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
                    @Override
                    public void onGlobalLayout() {
                        if (mAdapter != null && mAdapter.getNumColumns() == 0) {
                            final int numColumns = (int) Math.floor(
                                    mGridView.getWidth() / (mImageThumbSize + mImageThumbSpacing));
                            if (numColumns > 0) {
                                final int columnWidth =
                                        (mGridView.getWidth() / numColumns) - mImageThumbSpacing;
                                mAdapter.setNumColumns(numColumns);
                                mAdapter.setItemHeight(columnWidth);
                                if (BuildConfig.DEBUG) {
                                    Log.d(MainActivity.LOG_TAG, "onCreateView - numColumns set to " + numColumns);
                                }
                                if (Utils.hasJellyBean()) {
                                    mGridView.getViewTreeObserver()
                                            .removeOnGlobalLayoutListener(this);
                                } else {
                                    mGridView.getViewTreeObserver()
                                            .removeGlobalOnLayoutListener(this);
                                }
                            }
                        }
                    }
                });

        textDone = (TextView)findViewById(R.id.choose_img_done);
        textDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                complete();
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
                complete();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void onItemChecked(int position, boolean isChecked, View v) {
        if (isChecked) {
            num++;
            mAdapter.setSelection(position, isChecked);
            // set check
        } else {
            num--;
            mAdapter.removeSelect(position);
        }

        if (num > 9) {
            num--;
            mAdapter.removeSelect(position);
            // TODO: 21/08/2015 alert user, change nothing.
//            AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
//            //builder1.setTitle("Title");
//            builder1.setMessage("Select a maximum of 9 photos.");
//            builder1.setCancelable(true);
//            builder1.setNeutralButton(android.R.string.ok,
//                    new DialogInterface.OnClickListener() {
//                        public void onClick(DialogInterface dialog, int id) {
//                            dialog.cancel();
//                        }
//                    });
//
//            AlertDialog alert11 = builder1.create();
//            alert11.show();
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Select a maximum of 9 photos.")
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
            // // TODO: 21/08/2015
            //  change tile.... if num > 0
            if (num > 0) {
                setTitle(num + " selected.");

                // change textView color
                //TextView textPreview = (TextView)findViewById(R.id.choose_img_preview);

                //textPreview.setTextColor(getResources().getColorStateList(R.color.bar_background));
                textDone.setTextColor(getResources().getColorStateList(R.color.tab_selected));
            }
            else {
                setTitle(R.string.prompt_navigation_back);

                //TextView textPreview = (TextView)findViewById(R.id.choose_img_preview);

                //textPreview.setTextColor(getResources().getColorStateList(R.color.post_img_background));
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
        new InitListTask().execute();
    }

    private class InitListTask extends AsyncTask<Void, Void, ArrayList<ChooseImageListItem>> {
        @Override
        protected ArrayList<ChooseImageListItem> doInBackground(Void... params) {

            // TODO: 21/08/2015  put the latest images at the first...
//            ArrayList<ChooseImageListItem> items = new ArrayList<>();
//            for (String s : Images.imageThumbUrls) {
//                ChooseImageListItem item = new ChooseImageListItem();
//                item.setImageURL(s);
//                items.add(item);
//            }
//            return items;

            return getGalleryPhotos();
            //return items;
        }

        @Override
        protected void onPostExecute(ArrayList<ChooseImageListItem> result) {
            super.onPostExecute(result);
            if (isCancelled())
                return;

            if (result != null && !result.isEmpty())
                onRefreshComplete(result);
        }
    }

    private void onRefreshComplete(final ArrayList<ChooseImageListItem> result) {
        if (result == null)
            // means error occurred when sending http request, other wise result should be empty but not null
            return;
        if (mAdapter != null) {
            if (BuildConfig.DEBUG)
                Log.d(MainActivity.LOG_TAG, "ListView(mAdapter) already had data, and will be cleared...");

            mAdapter.clear();
            mAdapter.notifyDataSetChanged();
            if (!result.isEmpty()) {
                for(ChooseImageListItem item: result) {
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
            Log.e(MainActivity.LOG_TAG, "Exception caught: " + e.getMessage());
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
                    return super.onKeyDown(keyCode, event);
            }
        }

        // If it wasn't the Back key or there's no web page history, bubble up to the default
        // system behavior (probably exit the activity)
        return super.onKeyDown(keyCode, event);
    }

    private ArrayList<ChooseImageListItem> getGalleryPhotos() {
        ArrayList<ChooseImageListItem> galleryList = new ArrayList<>();
        final String[] columns = { MediaStore.Images.Media.DATA,
                MediaStore.Images.Media._ID };
        final String orderBy = MediaStore.Images.Media._ID;
        Cursor imageCursor = managedQuery(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns,
                null, null, orderBy);
        try {
            if (imageCursor != null && imageCursor.getCount() > 0) {

                while (imageCursor.moveToNext()) {
                    ChooseImageListItem item = new ChooseImageListItem();

                    int dataColumnIndex = imageCursor
                            .getColumnIndex(MediaStore.Images.Media.DATA);

                    item.setImageURL(imageCursor.getString(dataColumnIndex));

                    galleryList.add(item);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            imageCursor.close();
        }

        // show newest photo at beginning of the list
        Collections.reverse(galleryList);
        return galleryList;
    }

    private void complete() {
        Intent intent = new Intent();
        try {
            intent.putStringArrayListExtra(MainActivity.CHOOSE_IMAGE_RESULT, mAdapter.getSelection());
            setResult(RESULT_OK, intent);
            close();
        } catch (Exception e) {
            Log.e(MainActivity.LOG_TAG, "Exception caught: " + e.getMessage());

        }
    }
}
