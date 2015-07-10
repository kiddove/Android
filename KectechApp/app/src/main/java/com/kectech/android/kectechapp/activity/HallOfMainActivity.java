package com.kectech.android.kectechapp.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;

import com.kectech.android.kectechapp.R;
import com.kectech.android.kectechapp.adapter.HallOfMainAdapter;
import com.kectech.android.kectechapp.pager.CustomViewPager;
import com.kectech.android.kectechapp.listeners.OnSwipeOutListener;
import com.kectech.android.kectechapp.thirdparty.SlidingTabLayout;
import com.kectech.android.kectechapp.util.KecUtilities;

import java.io.File;

/**
 * Created by Paul on 02/07/2015.
 * it is the activity show up when tapped on hall tab list items
 * contains two tabs for now
 * video/photo -- video / bbs
 */
public class HallOfMainActivity extends Activity implements OnSwipeOutListener {

    public static final String subFolder = MainActivity.USER + File.separator + MainActivity.HALL_SUB_FOLDER;
    CustomViewPager pager;
    HallOfMainAdapter adapter;
    SlidingTabLayout tabs;
    CharSequence Titles[] = {"Video", "BBS"};
    int NumOfTabs = 2;
    int tabType = 0;
    int tabId = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_hall);

        try {

            // depends on type, may be different num of tabs
            // for test
            // todo to be continued...
            Intent intent = getIntent();
            if (intent != null) {
                tabType = intent.getIntExtra(MainActivity.HALL_OF_MAIN_TYPE, 0);
                tabId = intent.getIntExtra(MainActivity.HALL_OF_MAIN_ID, 0);
                NumOfTabs = tabType % 2 == 0 ? 2 : 1;
                KecUtilities.createSubFolders(this, MainActivity.USER + File.separator + MainActivity.HALL_SUB_FOLDER + File.separator + tabId);
            }


            // for using action bar back button
            //getActionBar().setDisplayHomeAsUpEnabled(true);
            // hide the icon on the left side on action bar
            //getActionBar().setDisplayShowHomeEnabled(false);

            // Creating The ViewPagerAdapter and Passing Fragment Manager, Titles fot the Tabs and Number Of Tabs.
            adapter = new HallOfMainAdapter(getFragmentManager(), Titles, NumOfTabs, tabType, tabId, this);

            // Assigning ViewPager View and setting the adapter
            pager = (CustomViewPager) findViewById(R.id.hall_activity_pager);
            pager.setOnSwipeOutListener(this);
            pager.setAdapter(adapter);

            // Assiging the Sliding Tab Layout View
            tabs = (SlidingTabLayout) findViewById(R.id.hall_activity_tabs);
            tabs.setDistributeEvenly(true); // To make the Tabs Fixed set this true, This makes the tabs Space Evenly in Available width

            // use this interface to use you own view, e.g. can add icon instead of text
            // for pageview or titleview? we will see
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

        switch (id) {
            case R.id.menu_item_search:
                return true;
            case R.id.menu_hall_tab_item_add:
                // handle in fragment
                // return false here
                return false;
//            not working here
//            case R.id.menu_item_quit:
//                System.exit(0);
//                return true;
            case android.R.id.home: {
//                NavUtils.navigateUpFromSameTask(this);
//                getFragmentManager().popBackStack();
                close();
                return true;
            }
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
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
}
