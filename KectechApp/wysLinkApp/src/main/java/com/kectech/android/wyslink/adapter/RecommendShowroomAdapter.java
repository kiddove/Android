package com.kectech.android.wyslink.adapter;

import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v13.app.FragmentPagerAdapter;

import com.kectech.android.wyslink.fragments.Tab_Main_Show_Add_Recommend;
import com.kectech.android.wyslink.fragments.Tab_Main_Show_Photo;
import com.kectech.android.wyslink.fragments.Tab_Main_Show_Video;

/**
 * Created by Paul on 02/07/2015.
 * adapter for hall activity contains one or tow tabs.
 */
public class RecommendShowroomAdapter extends FragmentPagerAdapter {
    CharSequence Titles[]; // This will Store the Titles of the Tabs which are Going to be passed when ViewPagerAdapter is created
    int NumbOfTabs; // Store the number of tabs, this will also be passed when the ViewPagerAdapter is created

    // Build a Constructor and assign the passed Values to appropriate values in the class
    public RecommendShowroomAdapter(FragmentManager fm, CharSequence mTitles[], int mNumbOfTabs) {
        super(fm);

        this.Titles = mTitles;
        this.NumbOfTabs = mNumbOfTabs;
    }

    //This method return the fragment for the every position in the View Pager
    @Override
    public Fragment getItem(int position) {
        switch (position) {

            case 0: {
                Tab_Main_Show_Add_Recommend tabRecommend =  new Tab_Main_Show_Add_Recommend();
                return tabRecommend;
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