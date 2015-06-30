package android.kectech.com.stylingactionbar;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.kectech.com.stylingactionbar.lib.HTML5WebView;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.widget.Toast;

import java.lang.reflect.InvocationTargetException;


public class VideoActivity extends Activity {

    HTML5WebView mWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_video);
        mWebView = new HTML5WebView(this);
        setContentView(mWebView.getLayout());

        // for using action bar back button
        getActionBar().setDisplayHomeAsUpEnabled(true);
        // hide the icon on the left side on action bar
        getActionBar().setDisplayShowHomeEnabled(false);

        // get url from where activate this activity (like in video tab click
        // receive the intent
        String url = "http://192.168.9.40/demo/test.html";
        Intent intent = getIntent();
        if (intent != null)
            url = intent.getStringExtra(MainActivity.EXTRA_MESSAGE_URL);

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
        getMenuInflater().inflate(R.menu.menu_video_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        switch (id) {
            case android.R.id.home: {
                //NavUtils.navigateUpFromSameTask(this);
                getFragmentManager().popBackStack();
                finish();
                return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }
        //return super.onOptionsItemSelected(item);
    }

    // use back button to navigate backward
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
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
                        getFragmentManager().popBackStackImmediate();
                        finish();
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
            Toast t = Toast.makeText(this, "ClassNotFoundException ",
                    Toast.LENGTH_SHORT);
            t.show();
        } catch (NoSuchMethodException nsme) {
            Toast t = Toast.makeText(this, "NoSuchMethodException: " + nsme.getMessage(),
                    Toast.LENGTH_SHORT);
            t.show();
        } catch (InvocationTargetException ite) {
            Toast t = Toast.makeText(this, "InvocationTargetException: " + ite.getMessage(),
                    Toast.LENGTH_SHORT);
            t.show();
        } catch (IllegalAccessException iae) {
            Toast t = Toast.makeText(this, "IllegalAccessException: " + iae.getMessage(),
                    Toast.LENGTH_SHORT);
            t.show();
        } catch (Exception e) {
            Toast t = Toast.makeText(this, "Exception: " + e.getMessage(),
                    Toast.LENGTH_SHORT);
            t.show();
        }
    }
}
