package android.kectech.com.stylingactionbar;

import android.app.Activity;
import android.kectech.com.stylingactionbar.adapter.ViewPagerAdapter;
import android.kectech.com.stylingactionbar.view.SlidingTabLayout;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;


public class MainActivity extends Activity {

    // declaring view and variables
    //Toolbar toolbar;
    ViewPager pager;
    ViewPagerAdapter adapter;
    SlidingTabLayout tabs;
    //CharSequence Titles[]={"Home", "Video", "Photo", "Swipe"};
    CharSequence Titles[] = {"Video", "Photo", "Home", "Swipe"};
    int NumOfTabs = 4;

    // for communicate with other activities
    public final static String EXTRA_MESSAGE_URL = "com.kectech.message.url";
    public final static String PHOTO_TAB_THUMB_URL_KEY = "thumbURL";
    // may have plenty
    public final static String PHOTO_TAB_IMAGE_URL_KEY = "imageURL";

    public final static String PHOTO_SUB_FOLDER = "Photo";

    // use for log tag
    public final static String LOGTAG = "kectech_log";

    // default encoding for files
    public final static String ENCODING = "UTF-8";

    // default user
    public final static String USER = "default";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        Log.d(MainActivity.LOGTAG, "Max memory is " + maxMemory + "KB");

        // Creating The Toolbar and setting it as the Toolbar for the activity

//        //// need style.xml parent = Theme.AppCompat.Light.NoActionBar
//        toolbar = (Toolbar) findViewById(R.id.tool_bar);
//
//        //setSupportActionBar(toolbar);
//        setActionBar(toolBar);
        // hide the icon on the left side on action bar
        try {
            getActionBar().setDisplayShowHomeEnabled(false);
            // hide the tile text
            getActionBar().setDisplayShowTitleEnabled(false);
            // Creating The ViewPagerAdapter and Passing Fragment Manager, Titles fot the Tabs and Number Of Tabs.
            adapter = new ViewPagerAdapter(getFragmentManager(), Titles, NumOfTabs);

            // Assigning ViewPager View and setting the adapter
            pager = (ViewPager) findViewById(R.id.pager);
            pager.setAdapter(adapter);

            // Assiging the Sliding Tab Layout View
            tabs = (SlidingTabLayout) findViewById(R.id.tabs);
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
            case R.id.menu_video_tab_item_add:
                // handle in fragment
                // return false here
                return false;
//            case R.id.menu_item_quit:
//                finish();
//                System.exit(0);
//                return true;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }
}
