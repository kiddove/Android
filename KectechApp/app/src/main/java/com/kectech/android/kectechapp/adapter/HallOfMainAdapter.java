package com.kectech.android.kectechapp.adapter;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.support.v13.app.FragmentPagerAdapter;

import com.kectech.android.kectechapp.tabs.Tab_Main_Hall_Photo;
import com.kectech.android.kectechapp.tabs.Tab_Main_Hall_Video;

/**
 * Created by Paul on 02/07/2015.
 * adapter for hall activity contains one or tow tabs.
 */
public class HallOfMainAdapter extends FragmentPagerAdapter {
    CharSequence Titles[]; // This will Store the Titles of the Tabs which are Going to be passed when ViewPagerAdapter is created
    int NumbOfTabs; // Store the number of tabs, this will also be passed when the ViewPagerAdapter is created

    int tabType;
    int tabId;  // for storage
    String tabName;
    String tabFollow;

    // Build a Constructor and assign the passed Values to appropriate values in the class
    public HallOfMainAdapter(FragmentManager fm, CharSequence mTitles[], int mNumbOfTabs, int tabType, int tabId, String tabName, String tabFollow) {
        super(fm);

        this.Titles = mTitles;
        this.NumbOfTabs = mNumbOfTabs;
        this.tabType = tabType;
        this.tabId = tabId;
        this.tabName = tabName;
        this.tabFollow = tabFollow;
    }

    //This method return the fragment for the every position in the View Pager
    @Override
    public Fragment getItem(int position) {
        switch (position) {

            case 0: {
                Tab_Main_Hall_Video tabVideo =  new Tab_Main_Hall_Video();
                tabVideo.setType(tabType);
                tabVideo.setId(tabId);
                tabVideo.setName(tabName);
                tabVideo.setFollow(tabFollow);
                tabVideo.createSubFolder();
                return tabVideo;
            }
            case 1: {
                Tab_Main_Hall_Photo tabPhoto =  new Tab_Main_Hall_Photo();
                tabPhoto.setType(tabType);
                tabPhoto.setId(tabId);
                tabPhoto.createSubFolder();
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
