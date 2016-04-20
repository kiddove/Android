package com.kectech.android.wyslink.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;

import com.facebook.FacebookSdk;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.kectech.android.wyslink.R;
import com.kectech.android.wyslink.adapter.MainAdapter;
import com.kectech.android.wyslink.service.RegistrationIntentService;
import com.kectech.android.wyslink.thirdparty.SlidingTabLayout;
import com.kectech.android.wyslink.util.KecUtilities;

import java.io.File;


public class MainActivity extends Activity {

    // for choose video activity
    public static final int REQUEST_VIDEO_CAPTURE = 1;
    public static final int REQUEST_SELECT_VIDEO = 2;
    public static final String BUNDLE_KEY_CONTENT_URL = "content_url";
    public static final String BUNDLE_KEY_CONTENT_URL_ENCODE = "content_url_encode";
    public static final String BUNDLE_KEY_SHARE_TYPE = "share_type";
    public static final String BUNDLE_KEY_SHARE_TITLE = "share_title";
    public static final String BUNDLE_KEY_SHARE_DESCRIPTION = "share_description";
    // strings
    // for register activity result
    public final static int REGISTER_REQUEST_CODE = 6008;
    public final static int NEW_POST_CODE = 6009;
    public final static int ADD_SHOWROOM_CODE = 6010;
    // for communicate with other activities
    // hall activity of main
    public final static String VIDEO_OF_SHOW_OF_MAIN_URL = "video_show_of_main_url";
    public final static String SHOW_OF_MAIN_SHOWROOM_NAME = "show_showroom_name";
    public final static String SHOW_OF_MAIN_SHOWROOM_OWNER = "show_showroom_owner";
    // may have plenty
    public final static String PHOTO_TAB_IMAGE_URL_KEY = "imageURL";
    public final static String PHOTO_TAB_IMAGE_INDEX_KEY = "imageIndex";

    public final static String PHOTO_SUB_FOLDER = "Photo";
    public final static String VIDEO_SUB_FOLDER = "Video";
    public final static String SHOW_SUB_FOLDER = "Show";
    public final static String SETTING_SUB_FOLDER = "Setting";
    public final static String ME_SUB_FOLDER = "Me";

    // for network
    public final static int CONNECTION_TIMEOUT = 3000;
    public final static int DOWNLOAD_BUFFER = 1024 * 100;

    // use for log tag
    public final static String LOG_TAG = "wysLink";

    // default encoding for files
    public final static String ENCODING = "UTF-8";

    // use between activities
    public final static String CHOOSE_IMAGE_RESULT = "choose_image_result";
    public final static String CHOOSE_IMAGE_PARAM = "choose_image_result";
    public final static String POST_DESC = "post_desc";
    public final static String POST_IMAGES = "post_images";
    public final static String SHOWROOM_NAME= "showroom_name";

    // default user
    public final static String SHARED_PREFERENCE_KEY = "USER_INFO";
    public final static String USER_NAME_SET_KEY = "USERNAME";
    public final static String CURRENT_USER_KEY = "CURRENT";
    public final static String CURRENT_LOGIN_STATUS_KEY = "LOGIN";
    public static String USER = ""; // kdlinx@kdlinx.com, kevin@kectech.com
    public final static String NEED_PROMPT_KEY = "PROMPT";

    // invariant
    public final static String NEW_POST_DEFAULT_IMAGE = "new_post_default_image";

    public final static int IMAGE_LIMIT_NUMBER = 9;
    public final static int imageId[] = {
            R.id.show_photo_list_item_img0, R.id.show_photo_list_item_img1, R.id.show_photo_list_item_img2,
            R.id.show_photo_list_item_img3, R.id.show_photo_list_item_img4, R.id.show_photo_list_item_img5,
            R.id.show_photo_list_item_img6, R.id.show_photo_list_item_img7, R.id.show_photo_list_item_img8
    };

    public static String SHOW_OF_MAIN_SUBFOLDER = MainActivity.USER + File.separator + MainActivity.SHOW_SUB_FOLDER;

    // for GCM
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    // variables
    // declaring view and variables
    //Toolbar toolbar;
    ViewPager pager;
    MainAdapter adapter;
    SlidingTabLayout tabs;
    //    CharSequence Titles[] = {"Hall", "Shows", "Public", "Me"};
//    int NumOfTabs = 4;
    CharSequence Titles[] = {"Shows", "Me", "Settings"};
    int NumOfTabs = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // if what then start login activity
        // else start main
        boolean bAuto = getSharedPreferences(MainActivity.SHARED_PREFERENCE_KEY, android.content.Context.MODE_PRIVATE).getBoolean(MainActivity.CURRENT_LOGIN_STATUS_KEY, false);
        if (!bAuto) {
            // start login activity
            startLogInActivity();
            return;
        }

        FacebookSdk.sdkInitialize(getApplicationContext());

        USER = getSharedPreferences(MainActivity.SHARED_PREFERENCE_KEY, android.content.Context.MODE_PRIVATE).getString(MainActivity.CURRENT_USER_KEY, "");
        SHOW_OF_MAIN_SUBFOLDER = MainActivity.USER + File.separator + MainActivity.SHOW_SUB_FOLDER;

        KecUtilities.init(getApplicationContext());
        if (!KecUtilities.createFolders()) {
            Log.e(MainActivity.LOG_TAG, "create folders failed.");
            finish();
            System.exit(0);
            return;
        }

        // init image loader
        KecUtilities.initImageLoader();
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
            Log.e(MainActivity.LOG_TAG, npe.getMessage());
        }

        // for GCM
        if (checkPlayServices()) {
            // Start IntentService to register this application with GCM.
            Intent register_intent = new Intent(this, RegistrationIntentService.class);
            startService(register_intent);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_main, menu);
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
//            case R.id.menu_item_search:
//                return true;
            case R.id.menu_show_tab_item_add:
                // handle in fragment
                // return false here
                return false;
            case R.id.menu_show_tab_item_logout:
            case R.id.menu_setting_logout:
                // return false to deal with it in fragment (Tab_Main_Show)

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
                    // allow async task to run simultaneously
                    new logOutTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                else
                    new logOutTask().execute();
                return true;
            //case R.id.menu_item_quit:   // from main_menu
            case R.id.menu_show_tab_item_quit:  // from tab_hall_menu
            case R.id.menu_setting_quit:
                KecUtilities.closeCache();
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
                    //this cause activity exit....
//                    KecUtilities.closeCache();
//                    finish();
//                    System.exit(0);
//                    return super.onKeyDown(keyCode, event);

                    // for preventing close the app, so next when start will continue the specific activity. avoid call onCreate();
                    // http://stackoverflow.com/questions/6514657/prevent-back-button-from-closing-my-application
                    moveTaskToBack( true );
                    return true;

            }
        }

        // If it wasn't the Back key or there's no web page history, bubble up to the default
        // system behavior (probably exit the activity)
        return super.onKeyDown(keyCode, event);
    }

    public void startLogInActivity() {
        // start login activity
        Intent intent = new Intent(this, LoginActivity.class);

        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        try {
            startActivity(intent);
            finish();
            overridePendingTransition(R.anim.enter_from_left, R.anim.exit_to_right);
        } catch (Exception e) {
            Log.e(MainActivity.LOG_TAG, "Exception caught(MainActivity---startLoginActivity): " + e.getMessage());
        }
    }

    public class logOutTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            try {

                SharedPreferences userDetails = getSharedPreferences(MainActivity.SHARED_PREFERENCE_KEY, android.content.Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = userDetails.edit();
                editor.putString(MainActivity.CURRENT_USER_KEY, null);
                editor.putBoolean(MainActivity.CURRENT_LOGIN_STATUS_KEY, false);
                editor.apply();

                KecUtilities.closeCache();

            } catch (Exception e) {
                Log.e(MainActivity.LOG_TAG, e.getMessage());
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            startLogInActivity();
        }

        @Override
        protected void onCancelled() {
        }
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
            } else {
                Log.i(LOG_TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }
}
