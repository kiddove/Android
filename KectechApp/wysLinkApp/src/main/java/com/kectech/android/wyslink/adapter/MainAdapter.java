package com.kectech.android.wyslink.adapter;

import android.app.Fragment;
import android.app.FragmentManager;

import com.kectech.android.wyslink.fragments.Tab_Main_Show;
import com.kectech.android.wyslink.fragments.Tab_Main_Setting;
import com.kectech.android.wyslink.fragments.Tab_Main_Me;

import android.support.v13.app.FragmentPagerAdapter;


/**
 * Created by Paul on 10/06/2015.
 * create different fragments here in getItem().
 */
public class MainAdapter extends FragmentPagerAdapter {
    CharSequence Titles[]; // This will Store the Titles of the Tabs which are Going to be passed when ViewPagerAdapter is created
    int NumbOfTabs; // Store the number of tabs, this will also be passed when the ViewPagerAdapter is created


    // Build a Constructor and assign the passed Values to appropriate values in the class
    public MainAdapter(FragmentManager fm, CharSequence mTitles[], int mNumbOfTabs) {
        super(fm);

        this.Titles = mTitles;
        this.NumbOfTabs = mNumbOfTabs;

    }

    //This method return the fragment for the every position in the View Pager
    @Override
    public Fragment getItem(int position) {
        switch (position) {

            case 0: {
                return new Tab_Main_Show();
            }
            case 1: {
                return new Tab_Main_Me();
            }
            case 2: {
                return new Tab_Main_Setting();
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
