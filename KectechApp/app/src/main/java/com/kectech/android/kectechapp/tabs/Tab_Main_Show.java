package com.kectech.android.kectechapp.tabs;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kectech.android.kectechapp.R;
import com.kectech.android.kectechapp.activity.MainActivity;

/**
 * Created by Paul on 10/06/2015.
 * Show Tab of main activity
 */
public class Tab_Main_Show extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.tab_main_show, container, false);
        return v;
    }
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//
//        // for using different menu
//        setHasOptionsMenu(true);
//    }
//
//    // will be appended to current menu...
//    @Override
//    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
//        // clear the existing items, otherwise new item will be appended to it.
//        menu.clear();
//        inflater.inflate(R.menu.menu_main, menu);
//        super.onCreateOptionsMenu(menu, inflater);
//    }

    // detect when this fragment is visible
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if (isVisibleToUser) {
            Log.d(MainActivity.LOGTAG, "tab_main_show becomes visible.");
        } else {
            Log.d(MainActivity.LOGTAG, "tab_main_show becomes invisible.");
        }

    }
}
