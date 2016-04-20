package com.kectech.android.wyslink.activity;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.support.annotation.NonNull;
import android.view.KeyEvent;
import android.view.MenuItem;

import com.kectech.android.wyslink.BuildConfig;
import com.kectech.android.wyslink.R;


/**
 * Created by Paul on 10/08/2015.
 * account activity in setting tab(ME)
 */
public class AccountPreferenceActivity extends PreferenceActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (BuildConfig.DEBUG)
        {
            System.gc();
        }
        overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
        addPreferencesFromResource(R.xml.account_preference);
    }

    //    @Override
////    <!-- These settings headers are only used on tablets. -->
//    public void onBuildHeader(List<Header> target) {
//        loadHeadersFromResource(R.xml.account_preference_header, target);
//    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            //case R.id.menu_item_quit:
            case android.R.id.home: {
                close();
                return true;
            }
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
                    close();
                    //return super.onKeyDown(keyCode, event);
                    return true;
            }
        }

        // If it wasn't the Back key or there's no web page history, bubble up to the default
        // system behavior (probably exit the activity)
        return super.onKeyDown(keyCode, event);
    }

    private void close() {
        finish();
        overridePendingTransition(R.anim.enter_from_left, R.anim.exit_to_right);
    }
}
