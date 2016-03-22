package com.kectech.android.wyslink.fragments;

import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.PreferenceFragment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;

import com.kectech.android.kectechapp.BuildConfig;
import com.kectech.android.kectechapp.R;
import com.kectech.android.wyslink.activity.MainActivity;

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

public class Tab_Main_Setting extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);

        // for using different menu
        setHasOptionsMenu(true);
    }

    private void setValue() {
        // account
        findPreference("preference_account_name").setSummary(getActivity().getSharedPreferences(MainActivity.SHARED_PREFERENCE_KEY, android.content.Context.MODE_PRIVATE).getString(MainActivity.CURRENT_USER_KEY, MainActivity.USER));

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

        // Show Prompt
        boolean bShowPrompt = getActivity().getSharedPreferences(MainActivity.SHARED_PREFERENCE_KEY, android.content.Context.MODE_PRIVATE).getBoolean(MainActivity.NEED_PROMPT_KEY, true);
        CheckBoxPreference cp = (CheckBoxPreference)findPreference("needPrompt");
        cp.setChecked(bShowPrompt);
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
    @Override
    public void onResume() {
        super.onResume();
        setValue();
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

    }

    @Override
    public void onPause() {
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        // just update all
        CheckBoxPreference cp = (CheckBoxPreference) findPreference("needPrompt");
        boolean bShowPrompt = cp.isChecked();
        SharedPreferences userDetails = getActivity().getSharedPreferences(MainActivity.SHARED_PREFERENCE_KEY, android.content.Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = userDetails.edit();
        editor.putBoolean(MainActivity.NEED_PROMPT_KEY, bShowPrompt);
        editor.apply();
    }
}
