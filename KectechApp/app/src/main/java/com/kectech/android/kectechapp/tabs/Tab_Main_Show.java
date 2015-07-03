package com.kectech.android.kectechapp.tabs;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kectech.android.kectechapp.R;

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
}
