package com.kectech.android.kectechapp.adapter;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v13.app.FragmentPagerAdapter;

import com.kectech.android.kectechapp.activity.MainActivity;
import com.kectech.android.kectechapp.tabs.Tab_Main_Hall_Photo;
import com.kectech.android.kectechapp.tabs.Tab_Main_Hall_Video;
import com.kectech.android.kectechapp.util.KecUtilities;

import java.io.File;

/**
 * Created by Paul on 02/07/2015.
 */
public class HallOfMainAdapter extends FragmentPagerAdapter {
    CharSequence Titles[]; // This will Store the Titles of the Tabs which are Going to be passed when ViewPagerAdapter is created
    int NumbOfTabs; // Store the number of tabs, this will also be passed when the ViewPagerAdapter is created

    int tabType;
    int tabId;
    Context context;

    // Build a Constructor and assign the passed Values to appropriate values in the class
    public HallOfMainAdapter(FragmentManager fm, CharSequence mTitles[], int mNumbOfTabs, int tabType, int tabId, Context context) {
        super(fm);

        this.Titles = mTitles;
        this.NumbOfTabs = mNumbOfTabs;
        this.tabType = tabType;
        this.tabId = tabId;
        this.context = context;
    }

    //This method return the fragment for the every position in the View Pager
    @Override
    public Fragment getItem(int position) {
        switch (position) {

            case 0: {
                Tab_Main_Hall_Video tabVideo =  new Tab_Main_Hall_Video();
                tabVideo.setType(tabType);
                tabVideo.setId(tabId);
                tabVideo.createSubFolder(context);
                return tabVideo;
            }
            case 1: {
                Tab_Main_Hall_Photo tabPhoto =  new Tab_Main_Hall_Photo();
                tabPhoto.setType(tabType);
                tabPhoto.setId(tabId);
                tabPhoto.createSubFolder(context);
                return tabPhoto;
            }
            default:
                return null;
        }

    }

    // This method return the titles for the Tabs in the Tab Strip

    @Override
    public CharSequence getPageTitle(int position) {
        return Titles[position];
    }

    // This method return the Number of tabs for the tabs Strip

    @Override
    public int getCount() {
        return NumbOfTabs;
    }
}
