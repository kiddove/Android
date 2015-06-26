package android.kectech.com.stylingactionbar.adapter;

import android.app.Fragment;
import android.app.FragmentManager;
import android.kectech.com.stylingactionbar.tabs.HomeTab;
import android.kectech.com.stylingactionbar.tabs.PhotoTab;
import android.kectech.com.stylingactionbar.view.SwipeRefreshLayoutBasicFragment;
import android.kectech.com.stylingactionbar.tabs.VideoTab;
import android.support.v13.app.FragmentPagerAdapter;


/**
 * Created by Paul on 10/06/2015.
 */
public class ViewPagerAdapter extends FragmentPagerAdapter {
    CharSequence Titles[]; // This will Store the Titles of the Tabs which are Going to be passed when ViewPagerAdapter is created
    int NumbOfTabs; // Store the number of tabs, this will also be passed when the ViewPagerAdapter is created


    // Build a Constructor and assign the passed Values to appropriate values in the class
    public ViewPagerAdapter(FragmentManager fm,CharSequence mTitles[], int mNumbOfTabsumb) {
        super(fm);

        this.Titles = mTitles;
        this.NumbOfTabs = mNumbOfTabsumb;

    }

    //This method return the fragment for the every position in the View Pager
    @Override
    public Fragment getItem(int position) {

//        if(position == 0) // if the position is 0 we are returning the First tab
//        {
//            HomeTab subscriberTab = new HomeTab();
//            return subscriberTab;
//        }
//        else             // As we are having 2 tabs if the position is now 0 it must be 1 so we are returning second tab
//        {
//            VideoTab videoTab = new VideoTab();
//            return videoTab;
//        }
        switch (position) {
            case 2: {
                HomeTab homeTab = new HomeTab();
                return homeTab;
            }
            case 0: {
                VideoTab videoTab = new VideoTab();
                return videoTab;
            }
            case 1: {
                PhotoTab photoTab = new PhotoTab();
                return photoTab;
            }
            case 3: {
                SwipeRefreshLayoutBasicFragment sf = new SwipeRefreshLayoutBasicFragment();
                return sf;
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
