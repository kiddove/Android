package com.kectech.android.wyslink.fragments;


import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.kectech.android.wyslink.BuildConfig;
import com.kectech.android.wyslink.R;
import com.kectech.android.wyslink.activity.ChooseVideoActivity;
import com.kectech.android.wyslink.activity.MainActivity;
import com.kectech.android.wyslink.activity.MeOfMainActivity;
import com.kectech.android.wyslink.adapter.MeListViewAdapter;
import com.kectech.android.wyslink.listitem.MeListItem;

import java.util.ArrayList;


/**
 * Created by Paul on 10/06/2015.
 * video tab on activity of hall
 */
public class Tab_Main_Me extends Fragment {

    private MeListViewAdapter mListAdapter;

    @Override
    @SuppressWarnings("deprecation")
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.tab_main_me, container, false);

        ListView mListView = (ListView) v.findViewById(R.id.me_tab_list);

//        // click listener
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                MeListItem meListItem = mListAdapter.getItem(position);
                String strType = meListItem.getTitle(); // private, public, showroom

                Intent intent = new Intent(getActivity(), MeOfMainActivity.class);
                intent.putExtra(MainActivity.SHOW_OF_MAIN_SHOWROOM_NAME, strType);

                startActivity(intent);
                getActivity().overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
            }
        });

        initList();

        mListView.setAdapter(mListAdapter);

        return v;
    }

    public void initList() {
        String showroomName = getActivity().getSharedPreferences(MainActivity.SHARED_PREFERENCE_KEY, android.content.Context.MODE_PRIVATE).getString(MainActivity.CURRENT_SHOWROOM_NAME_KEY, "");
        ArrayList<MeListItem> defaultListItems = new ArrayList<>();
        MeListItem itemPrivate = new MeListItem();
        MeListItem itemPublic = new MeListItem();
        MeListItem itemShowroom = new MeListItem();
        itemPrivate.setTitle(getActivity().getString(R.string.tab_main_me_title_private));
        itemPrivate.setDescription(getActivity().getString(R.string.tab_main_me_description_private));

        itemPublic.setTitle(getActivity().getString(R.string.tab_main_me_title_public));
        itemPublic.setDescription(getActivity().getString(R.string.tab_main_me_description_public));

        itemShowroom.setTitle(getActivity().getString(R.string.tab_main_me_title_showroom));
        itemShowroom.setDescription(showroomName);

        defaultListItems.add(itemPrivate);
        defaultListItems.add(itemPublic);

        if (!TextUtils.isEmpty(showroomName))
            defaultListItems.add(itemShowroom);

        mListAdapter = new MeListViewAdapter(getActivity(), R.layout.me_list_item, defaultListItems);
    }

    // detect when this fragment is visible
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if (BuildConfig.DEBUG) {
            if (isVisibleToUser) {
                Log.d(MainActivity.LOG_TAG, "tab_main_video becomes visible.");
            } else {
                Log.d(MainActivity.LOG_TAG, "tab_main_video becomes invisible.");
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // for using different menu
        setHasOptionsMenu(true);
    }

    // will be appended to current menu...
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // clear the existing items, otherwise new item will be appended to it.
        menu.clear();
        inflater.inflate(R.menu.menu_my_video_tab, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onStop() {
        super.onStop();
        //mCustomTabActivityHelper.unbindCustomTabsService(getActivity());
        if (BuildConfig.DEBUG)
            Log.d(MainActivity.LOG_TAG, "tab_main_me onStop.");
    }

    @Override
    public void onStart() {
        super.onStart();
        //mCustomTabActivityHelper.bindCustomTabsService(getActivity());
        if (BuildConfig.DEBUG)
            Log.d(MainActivity.LOG_TAG, "tab_main_me onStart.");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id) {
//            case R.id.menu_item_search:
//                return true;
            case R.id.video_menu_item_add_new_video:
                // open a new activity, and get result here
                startChooseVideoActivity();
                return true;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void startChooseVideoActivity() {

        Intent intent = new Intent(getActivity(), ChooseVideoActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        try {
            // do not finish, instead call startActivityForResult
            //startActivityForResult(intent, MainActivity.NEW_POST_CODE);
            startActivity(intent);
            getActivity().overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
        } catch (Exception e) {
            Log.e(MainActivity.LOG_TAG, "Exception caught(tab_main_video---startChooseVideoActivity): " + e.getMessage());

        }
    }
}