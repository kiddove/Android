package com.kectech.android.kectechapp.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.webkit.WebView;

import com.kectech.android.kectechapp.R;
import com.kectech.android.kectechapp.listeners.OnSwipeTouchListener;
import com.kectech.android.kectechapp.thirdparty.HTML5WebView;

import java.lang.reflect.InvocationTargetException;


public class VideoOfHallOfMainActivity extends Activity {

    //public static final String subFolder = MainActivity.USER + File.separator + MainActivity.HALL_SUB_FOLDER + File.separator + MainActivity.VIDEO_SUB_FOLDER;

    HTML5WebView mWebView;

    //private OnSwipeTouchListener onSwipeTouchListener;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_videol);
        mWebView = new HTML5WebView(this);
        setContentView(mWebView.getLayout());

        OnSwipeTouchListener swipeTouchListener = new OnSwipeTouchListener() {
            public void onSwipeOutLeft() {
                //getFragmentManager().popBackStack();
                close();
            }
//            public void onSwipeLeft() {
//                //Toast.makeText(VideoOfHallOfMainActivity.this, "left", Toast.LENGTH_SHORT).show();
//            }
        };

        mWebView.setOnTouchListener(swipeTouchListener);

        // for using action bar back button
        //getActionBar().setDisplayHomeAsUpEnabled(true);
        // hide the icon on the left side on action bar
        //getActionBar().setDisplayShowHomeEnabled(false);

        // get url from where activate this activity (like in video tab click
        // receive the intent
        String url = "http://192.168.9.40/demo/test.html";
        Intent intent = getIntent();
        if (intent != null)
            url = intent.getStringExtra(MainActivity.VIDEO_OF_HALL_OF_MAIN_URL);

        if (mWebView != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                if (0 != (getApplicationInfo().flags &= ApplicationInfo.FLAG_DEBUGGABLE)) {
                    WebView.setWebContentsDebuggingEnabled(true);
                }
            }
            mWebView.loadUrl(url);
        }
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
                //NavUtils.navigateUpFromSameTask(this);
                //getFragmentManager().popBackStack();
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
                    if (mWebView.inCustomView()) {
                        mWebView.hideCustomView();
                        return true;
                    }
                    if (mWebView.canGoBack()) {
                        mWebView.goBack();
                    } else {
                        close();
                    }
                    return super.onKeyDown(keyCode, event);
            }
        }

        // If it wasn't the Back key or there's no web page history, bubble up to the default
        // system behavior (probably exit the activity)
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onPause() {
        super.onPause();

        // WebView threads never stopping
        // so we have to...
        try {
            Class.forName("android.webkit.WebView").getMethod("onPause", (Class[]) null).invoke(mWebView, (Object[]) null);
        } catch (ClassNotFoundException cnfe) {
            Log.e(MainActivity.LOGTAG, cnfe.getMessage());
            cnfe.printStackTrace();
        } catch (NoSuchMethodException nsme) {
            Log.e(MainActivity.LOGTAG, nsme.getMessage());
            nsme.printStackTrace();
        } catch (InvocationTargetException ite) {
            Log.e(MainActivity.LOGTAG, ite.getMessage());
            ite.printStackTrace();
        } catch (IllegalAccessException iae) {
            Log.e(MainActivity.LOGTAG, iae.getMessage());
            iae.printStackTrace();
        } catch (Exception e) {
            Log.e(MainActivity.LOGTAG, e.getMessage());
            e.printStackTrace();
        }
    }
    @Override
    public boolean dispatchTouchEvent(@NonNull MotionEvent ev){
        //onSwipeTouchListener.getGestureDetector().onTouchEvent(ev);
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public void onBackPressed(){
        if (mWebView.inCustomView()) {
            mWebView.hideCustomView();
            return;
        }
        if (mWebView.canGoBack()) {
            mWebView.goBack();
        } else {
            if (getFragmentManager().getBackStackEntryCount() == 0) {
                super.onBackPressed();
            } else {
                getFragmentManager().popBackStack();
            }
        }
    }

    private void close() {
        finish();
        overridePendingTransition(R.anim.enter_from_left, R.anim.exit_to_right);
    }
}
