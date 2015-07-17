package com.kectech.android.kectechapp.activity;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;

import com.kectech.android.kectechapp.R;
import com.kectech.android.kectechapp.adapter.MainAdapter;
import com.kectech.android.kectechapp.thirdparty.SlidingTabLayout;
import com.kectech.android.kectechapp.util.KecUtilities;


public class MainActivity extends Activity {

    // strings
    // for communicate with other activities
    // hall activity of main
    public final static String VIDEO_OF_HALL_OF_MAIN_URL = "video_hall_of_main_url";
    public final static String HALL_OF_MAIN_TYPE = "event_hall_type";
    public final static String HALL_OF_MAIN_ID = "event_hall_id";   //  used as timestamp... no id
    public final static String HALL_OF_MAIN_NAME = "event_hall_name";
    public final static String HALL_OF_MAIN_FOLLOW = "event_hall_follow_name";

    public final static String PHOTO_TAB_THUMB_URL_KEY = "thumbURL";
    // may have plenty
    public final static String PHOTO_TAB_IMAGE_URL_KEY = "imageURL";
    public final static String MAIN_HALL_PHOTO_FOLDER = "tab_photo_hall_main_folder";

    public final static String PHOTO_SUB_FOLDER = "Photo";
    public final static String VIDEO_SUB_FOLDER = "Video";
    public final static String HALL_SUB_FOLDER = "Hall";
    public final static String SHOW_SUB_FOLDER = "Show";
    public final static String PUBLIC_SUB_FOLDER = "Public";
    public final static String SETTING_SUB_FOLDER = "Setting";

    // use for log tag
    public final static String LOGTAG = "kectech_log";

    // default encoding for files
    public final static String ENCODING = "UTF-8";

    // default user
    public final static String USER = "kevin@kectech.com"; // kdlinx@kdlinx.com, kevin@kectech.com

    public final static int imageId[] = {R.id.hall_photo_list_item_img0, R.id.hall_photo_list_item_img1, R.id.hall_photo_list_item_img2,
            R.id.hall_photo_list_item_img3, R.id.hall_photo_list_item_img4, R.id.hall_photo_list_item_img5,
            R.id.hall_photo_list_item_img6, R.id.hall_photo_list_item_img7, R.id.hall_photo_list_item_img8,};

    // variables
    // declaring view and variables
    //Toolbar toolbar;
    ViewPager pager;
    MainAdapter adapter;
    SlidingTabLayout tabs;
    CharSequence Titles[] = {"Hall", "Shows", "Public", "Me"};
    int NumOfTabs = 4;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        KecUtilities.context = this;
        if (!KecUtilities.createFolders()) {
            Log.e(MainActivity.LOGTAG, "create folders failed.");
            finish();
            System.exit(0);
            return;
        }

        // get available memory size
        int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        Log.d(MainActivity.LOGTAG, "Max memory is " + maxMemory + "KB");
        try {
            //getActionBar().setDisplayShowHomeEnabled(false);
            // hide the tile text
            //getActionBar().setDisplayShowTitleEnabled(false);
            // Creating The ViewPagerAdapter and Passing Fragment Manager, Titles fot the Tabs and Number Of Tabs.
            adapter = new MainAdapter(getFragmentManager(), Titles, NumOfTabs);

            // Assigning ViewPager View and tab_main_setting the adapter
            pager = (ViewPager) findViewById(R.id.pager);
            pager.setAdapter(adapter);

            // Assigning the Sliding Tab Layout View
            tabs = (SlidingTabLayout) findViewById(R.id.tabs);
            tabs.setDistributeEvenly(true); // To make the Tabs Fixed set this true, This makes the tabs Space Evenly in Available width

            // use this interface to use you own view, e.g. can add icon instead of text
            // for pageView or titleView? we will see
            //tabs.setCustomTabView(R.layout.tab_title, R.id.tab_title_text);
            // do NOT use 0 for 2nd param, will cause exception
            //tabs.setCustomTabView(R.layout.tab_title, R.id.tab_title_text);
            //tabs.setCustomTabView(R.layout.tab_title, 0);

            // change background color...
            //tabs.setBackgroundColor();
            // Setting Custom Color for the Scroll bar indicator of the Tab View
            tabs.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
                @Override
                public int getIndicatorColor(int position) {
                    return getResources().getColor(R.color.tab_selected);
                }
            });

            // Easy and effective solution for non dynamic view pagers
            pager.setOffscreenPageLimit(NumOfTabs);
            // Setting the ViewPager For the SlidingTabsLayout
            tabs.setViewPager(pager);

        } catch (NullPointerException npe) {
            Log.e(MainActivity.LOGTAG, npe.getMessage());
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id) {
            case R.id.menu_item_search:
                return true;
            case R.id.menu_hall_tab_item_add:
                // handle in fragment
                // return false here
                return false;
            case R.id.menu_item_quit:
                finish();
                System.exit(0);
                return true;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    // use back button to navigate backward
    @Override
    public boolean onKeyDown(int keyCode, @NonNull KeyEvent event) {
        // check if the key event was the Back button and if there's history
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_BACK:
                    //getFragmentManager().popBackStackImmediate();
                    //finish();
                    return super.onKeyDown(keyCode, event);
            }
        }

        // If it wasn't the Back key or there's no web page history, bubble up to the default
        // system behavior (probably exit the activity)
        return super.onKeyDown(keyCode, event);
    }
}
