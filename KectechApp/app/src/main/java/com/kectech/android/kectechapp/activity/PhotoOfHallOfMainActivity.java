package com.kectech.android.kectechapp.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.kectech.android.kectechapp.R;
import com.kectech.android.kectechapp.adapter.FadePageTransformer;
import com.kectech.android.kectechapp.data.DownLoadImageTask;
import com.kectech.android.kectechapp.pager.CustomViewPager;
import com.kectech.android.kectechapp.listeners.OnSwipeOutListener;
import com.kectech.android.kectechapp.thirdparty.ScaleImageView;
import com.kectech.android.kectechapp.util.KecUtilities;

import java.util.ArrayList;

/**
 * Created by Paul on 25/06/2015.
 * need to do
 * set background thumbnail first
 * then download the full image
 * do some research on intent param
 */
public class PhotoOfHallOfMainActivity extends Activity implements OnSwipeOutListener {

    private int imageCount = 1;
    private ArrayList<View> viewList;
    private Activity context = null;
    //private String[] URLs = null;
    Bundle URLs = null;
    // for dots
    private ArrayList<View> dots;
    private int previous = 0;

    public String subFolder = null;
    private DownLoadImageTask preTask;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);

        context = this;

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
                ArrayList<String> thumbs = URLs.getStringArrayList(MainActivity.PHOTO_TAB_THUMB_URL_KEY);
                imageCount = thumbs.size();
                subFolder = URLs.getString(MainActivity.MAIN_HALL_PHOTO_FOLDER, null);
            }

            // for now only one image

            // init
            LayoutInflater layoutInflater = LayoutInflater.from(this);
            viewList = new ArrayList<>();
            dots = new ArrayList<>();

            // dots
            LinearLayout dotsLayout = (LinearLayout) findViewById(R.id.photo_activity_dots_group);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(32, 32);
            params.setMargins(6, 0, 6, 0);


            for (int i = 0; i < imageCount; i++) {
                View v = layoutInflater.inflate(R.layout.photo_activity_image_fragment, null);
                // here load thumb first if has..
                ScaleImageView imageView = (ScaleImageView) v.findViewById(R.id.photo_activity_image_fragment_imageView);
                if (imageView != null) {
//                    imageView.setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//                            close();
//                        }
//                    });
                    if (URLs != null && subFolder != null) {
                        String strThumbURL = URLs.getStringArrayList(MainActivity.PHOTO_TAB_THUMB_URL_KEY).get(i);
                        String thumbLocalPath = KecUtilities.getLocalFilePathFromURL(strThumbURL, subFolder);
                        Bitmap thumbBitmap = KecUtilities.ReadFileFromLocal(thumbLocalPath);
                        if (thumbBitmap != null) {
                            imageView.setImageBitmap(thumbBitmap);
                            //thumbBitmap.recycle();
                        }
                    }
                }
                viewList.add(v);

                // dots
                View dot = new View(this);
                dot.setLayoutParams(params);
                dot.setBackgroundResource(R.drawable.dot_normal);
                dotsLayout.addView(dot);
                dots.add(dot);
            }
        } catch (Exception e) {
            Log.e(MainActivity.LOGTAG, "Exception caught: " + e.getMessage());
            return;
        }


        // default select
        dots.get(0).setBackgroundResource(R.drawable.dot_selected);

        // pager
        CustomViewPager viewPager = (CustomViewPager) findViewById(R.id.photo_activity_viewpager);
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

        viewPager.setAdapter(new MyPagerAdapter(viewList));

        setCurrentPage(0);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_hall_activity, menu);
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
                    return super.onKeyDown(keyCode, event);
            }
        }

        // If it wasn't the Back key or there's no web page history, bubble up to the default
        // system behavior (probably exit the activity)
        return super.onKeyDown(keyCode, event);
    }

    // page adapter
    private class MyPagerAdapter extends PagerAdapter {

        private ArrayList<View> mListView;

        public MyPagerAdapter(ArrayList<View> mListView) {
            super();
            this.mListView = mListView;
        }


        public void destroyItem(View arg0, int arg1, Object arg2) {
            // TODO Auto-generated method stub
            ((ViewGroup) arg0).removeView(mListView.get(arg1));
        }

        @Override
        public void finishUpdate(View arg0) {
            // TODO Auto-generated method stub

        }


        public int getCount() {
            // TODO Auto-generated method stub
            return mListView.size();
        }

        @Override
        public Object instantiateItem(View arg0, int arg1) {
            // TODO Auto-generated method stub
            ((ViewGroup) arg0).addView(mListView.get(arg1), 0);
            return mListView.get(arg1);
        }


        public boolean isViewFromObject(View arg0, Object arg1) {
            // TODO Auto-generated method stub
            return arg0 == (arg1);
        }

        @Override
        public void restoreState(Parcelable arg0, ClassLoader arg1) {
            // TODO Auto-generated method stub

        }

        @Override
        public Parcelable saveState() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public void startUpdate(View arg0) {
            // TODO Auto-generated method stub

        }
    }

    // set current
    public void setCurrentPage(int position) {
        if (subFolder == null)
            return;
        if (preTask != null) {
            preTask.cancel(true);
            preTask = null;
        }
        dots.get(previous).setBackgroundResource(R.drawable.dot_normal);
        dots.get(position).setBackgroundResource(R.drawable.dot_selected);
        previous = position;

        ScaleImageView imageView = (ScaleImageView) viewList.get(position).findViewById(R.id.photo_activity_image_fragment_imageView);
        // download image async
        String imageURL = URLs.getStringArrayList(MainActivity.PHOTO_TAB_IMAGE_URL_KEY).get(position);
        String localPath = KecUtilities.getLocalFilePathFromURL(imageURL, subFolder);
        Bitmap bitmap = KecUtilities.ReadFileFromLocal(localPath);
        //Bitmap bitmap = null;
        if (imageView != null && bitmap != null) {
            imageView.setImageBitmap(bitmap);
            //bitmap.recycle();
        } else {
            preTask = new DownLoadImageTask(imageView, context, subFolder);
            preTask.execute(imageURL);
        }
    }

    @Override
    public void onSwipeOutAtLeft() {
        close();
    }

    @Override
    public void onBackPressed(){
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
}
