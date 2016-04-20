package com.kectech.android.wyslink.activity;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.kectech.android.wyslink.BuildConfig;
import com.kectech.android.wyslink.R;
import com.kectech.android.wyslink.transformer.FadePageTransformer;
import com.kectech.android.wyslink.pager.CustomViewPager;
import com.kectech.android.wyslink.listeners.OnSwipeOutListener;
import com.kectech.android.wyslink.fragments.ImageDetailFragment;
import com.kectech.android.wyslink.thirdparty.CacheBitmap.Utils;

import java.util.ArrayList;

/**
 * Created by Paul on 25/06/2015.
 * need to do
 * set background thumbnail first
 * then download the full image
 * do some research on intent param
 */
public class PhotoOfShowOfMainActivity extends Activity implements OnSwipeOutListener, View.OnClickListener {
//, View.OnClickListener {

    private int imageCount = 1;
    //private ArrayList<View> viewList;
    //private String[] URLs = null;
    Bundle URLs = null;
    // for dots
    private ArrayList<View> dots;
    private int previous = 0;

    //public String subFolder = null;

    //private ImageFetcher mImageFetcherImage;

    private CustomViewPager viewPager;

    @Override
    @SuppressWarnings("deprecation")
    protected void onCreate(Bundle savedInstanceState) {

        if (BuildConfig.DEBUG) {
            System.gc();
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);

        //createFetcher();
        // for load thumb
        //mImageFetcherImage = KecUtilities.getThumbFetcher(this);

        int position = 0;
        try {
            // for using action bar back button
            //getActionBar().setDisplayHomeAsUpEnabled(true);
            // hide the icon on the left side on action bar
            //getActionBar().setDisplayShowHomeEnabled(false);

            // get url from where activate this activity (like in hall tab click
            // receive the intent
            //String url = "http://192.168.9.40/demo/test.html";
            Intent intent = getIntent();
            if (intent != null) {
                URLs = intent.getExtras();
                ArrayList<String> images = URLs.getStringArrayList(MainActivity.PHOTO_TAB_IMAGE_URL_KEY);
                position = URLs.getInt(MainActivity.PHOTO_TAB_IMAGE_INDEX_KEY, 0);
                if (images != null)
                    imageCount = images.size();
                else
                    imageCount = 0;

                // pager
                viewPager = (CustomViewPager) findViewById(R.id.photo_activity_viewpager);
                viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                    @Override
                    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                    }

                    @Override
                    public void onPageSelected(int position) {
                        setCurrentPage(position);
                    }

                    @Override
                    public void onPageScrollStateChanged(int state) {

                    }
                });

                viewPager.setOnSwipeOutListener(this);
                // fade away
                viewPager.setPageTransformer(false, new FadePageTransformer());

                viewPager.setAdapter(new MyPagerAdapter(getFragmentManager(), images));

                //viewPager.setCurrentItem(position);
            }

            // for now only one image

            // init
            dots = new ArrayList<>();

            // dots
            LinearLayout dotsLayout = (LinearLayout) findViewById(R.id.photo_activity_dots_group);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(32, 32);
            params.setMargins(6, 0, 6, 0);


            for (int i = 0; i < imageCount; i++) {
                // dots
                View dot = new View(this);
                dot.setLayoutParams(params);
                dot.setBackgroundResource(R.drawable.dot_normal);
                dotsLayout.addView(dot);
                dots.add(dot);
            }
        } catch (Exception e) {
            Log.e(MainActivity.LOG_TAG, "Exception caught(PhotoOfShowOfMainActivity--onCreate): " + e.getMessage());
            return;
        }

        viewPager.setOffscreenPageLimit(imageCount);
        // if set right after setAdapter, will cause NullPointerException
        viewPager.setCurrentItem(position);
        setCurrentPage(position);

        viewPager.setPageMargin((int) getResources().getDimension(R.dimen.activity_horizontal_margin));
        //mPager.setOffscreenPageLimit(2);

        // Set up activity to go full screen
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Enable some additional newer visibility and ActionBar features to create a more
        // immersive photo viewing experience
        if (Utils.hasHoneycomb()) {
            final ActionBar actionBar = getActionBar();
            if (actionBar != null) {
                // Hide and show the ActionBar as the visibility changes
                viewPager.setOnSystemUiVisibilityChangeListener(
                        new View.OnSystemUiVisibilityChangeListener() {
                            @Override
                            public void onSystemUiVisibilityChange(int vis) {
                                if ((vis & View.SYSTEM_UI_FLAG_LOW_PROFILE) != 0) {
                                    actionBar.hide();
                                } else {
                                    actionBar.show();
                                }
                            }
                        });

                // Start low profile mode and hide ActionBar
                viewPager.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
                actionBar.hide();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_hall_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }

        switch (id) {
            case android.R.id.home: {
                close();
                return true;
            }
//            case R.id.menu_item_quit:
//                mImageFetcherImage.clearCache();
//                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
        //return super.onOptionsItemSelected(item);
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

    // page adapter
    private class MyPagerAdapter extends FragmentPagerAdapter {

        private final ArrayList<String> mUrls;

        public MyPagerAdapter(FragmentManager fm, ArrayList<String> urls) {
            super(fm);
            this.mUrls = urls;
        }

        @Override
        public int getCount() {
            return mUrls.size();
        }

        @Override
        public Fragment getItem(int position) {
            return ImageDetailFragment.newInstance(mUrls.get(position));
        }

    }

    // set current
    public void setCurrentPage(int position) {

        dots.get(previous).setBackgroundResource(R.drawable.dot_normal);
        dots.get(position).setBackgroundResource(R.drawable.dot_selected);
        previous = position;
    }

    @Override
    public void onSwipeOutAtLeft() {
        close();
    }

    @Override
    public void onBackPressed() {
        if (getFragmentManager().getBackStackEntryCount() == 0) {
            super.onBackPressed();
        } else {
            getFragmentManager().popBackStack();
        }
    }

    private void close() {
        finish();
        overridePendingTransition(R.anim.enter_from_left, R.anim.exit_to_right);
    }

    @Override
    public void onResume() {
        super.onResume();
        //mImageFetcherImage.setExitTasksEarly(false);
    }

    @Override
    protected void onPause() {
        super.onPause();
        //mImageFetcherImage.setExitTasksEarly(true);
        //mImageFetcherImage.flushCache();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //mImageFetcherImage.closeCache();
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public void onClick(View v) {
        final int vis = viewPager.getSystemUiVisibility();
        if ((vis & View.SYSTEM_UI_FLAG_LOW_PROFILE) != 0) {
            viewPager.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
        } else {
            viewPager.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
        }
    }
}
