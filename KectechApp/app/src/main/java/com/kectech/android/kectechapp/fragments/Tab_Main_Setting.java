package com.kectech.android.kectechapp.fragments;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;

import com.kectech.android.kectechapp.R;
import com.kectech.android.kectechapp.activity.MainActivity;

/**
 * Created by Paul on 10/06/2015.
 * Setting Tab of main activity
 */
//public class Tab_Main_Setting extends Fragment {
//    @Override
//    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//        return inflater.inflate(R.layout.tab_main_setting, container, false);
//    }
//
//    // detect when this fragment is visible
//    @Override
//    public void setUserVisibleHint(boolean isVisibleToUser) {
//        super.setUserVisibleHint(isVisibleToUser);
//
//        if (BuildConfig.DEBUG) {
//            if (isVisibleToUser) {
//                Log.d(MainActivity.LOG_TAG, "tab_main_setting becomes visible.");
//            } else {
//                Log.d(MainActivity.LOG_TAG, "tab_main_setting becomes invisible.");
//            }
//        }
//    }
//}

public class Tab_Main_Setting extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);
        // todo read from sf
        findPreference("preference_account_name").setSummary(MainActivity.USER);

        // version
        final PackageManager packageManager = getActivity().getPackageManager();
        String versionName = "Demo";
        if (packageManager != null) {
            try {
                PackageInfo packageInfo = packageManager.getPackageInfo(getActivity().getPackageName(), 0);
                versionName = packageInfo.versionName;
            } catch (PackageManager.NameNotFoundException e) {
                Log.e(MainActivity.LOG_TAG, "package name not found.");
            }
        }

        findPreference("preference_version").setSummary(versionName);
        // for using different menu
        setHasOptionsMenu(true);
    }
    // will be appended to current menu...
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // clear the existing items, otherwise new item will be appended to it.
        //menu.clear();
        inflater.inflate(R.menu.menu_setting, menu);
        super.onCreateOptionsMenu(menu, inflater);

        // all click handled in main activity
    }

}
