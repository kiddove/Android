package com.kectech.android.kectechapp.tabs;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.kectech.android.kectechapp.R;
import com.kectech.android.kectechapp.activity.MainActivity;
import com.kectech.android.kectechapp.activity.VideoOfHallOfMainActivity;
import com.kectech.android.kectechapp.adapter.VideoListViewAdapter;
import com.kectech.android.kectechapp.data.LoadHallVideoListThumbsTask;
import com.kectech.android.kectechapp.listitem.VideoListItem;
import com.kectech.android.kectechapp.thirdparty.SwipyRefreshLayout;
import com.kectech.android.kectechapp.thirdparty.SwipyRefreshLayoutDirection;
import com.kectech.android.kectechapp.util.KecUtilities;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;

/**
 * Created by Paul on 10/06/2015.
 * video tab on activity of hall
 */
public class Tab_Main_Hall_Video extends Fragment {

    private ListView mListView;

    private VideoListViewAdapter mVideoAdapter;

    private SwipyRefreshLayout mSwipyRefreshLayout;

    private int tabId = 0;
    private String tabName;
    private String strType;
    private String tabFollow;

    private String subFolder = null;
    private Activity activity;

    public void setType(int tabType) {

        switch (tabType) {
            case 1:
                // public
                break;
            case 2:
                // showroom
                break;
            case 3:
                // eventhall
                strType = "&eh=";
                break;
            default:
                break;
        }
    }

    public void setId(int tabId) {
        this.tabId = tabId;
    }

    public void setName(String name) {
        this.tabName = name;
    }

    public void setFollow(String follow) {
        this.tabFollow = follow;
    }

    public void createSubFolder() {
        String folder = MainActivity.USER + File.separator + MainActivity.HALL_SUB_FOLDER + File.separator + tabId + File.separator + MainActivity.VIDEO_SUB_FOLDER;
        if (KecUtilities.createSubFolders(folder)) {
            subFolder = folder;
        } else
            subFolder = null;

    }
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.tab_main_hall_video, container, false);

        mListView = (ListView) v.findViewById(R.id.video_tab_list);
        mSwipyRefreshLayout = (SwipyRefreshLayout) v.findViewById(R.id.video_tab_swipy_refresh_layout);
        mSwipyRefreshLayout.setDirection(SwipyRefreshLayoutDirection.BOTH);

        mSwipyRefreshLayout.setOnRefreshListener(new SwipyRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh(SwipyRefreshLayoutDirection direction) {
                Refresh(direction);
            }
        });

//        // click listener
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                VideoListItem videoListItem = mVideoAdapter.getItem(position);
                // get another activity to run
                Intent intent = new Intent(activity, VideoOfHallOfMainActivity.class);

                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

                // todo set correct url of video page, now all items are test url.
                intent.putExtra(MainActivity.VIDEO_OF_HALL_OF_MAIN_URL, videoListItem.getVideoUrl());
                startActivity(intent);
                activity.overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
            }
        });

        mSwipyRefreshLayout.setColorScheme(
                R.color.swipe_color_1, R.color.swipe_color_3,
                R.color.swipe_color_5);

        initList();

        return v;
    }

    public void initList() {
        // read from local first
        if (subFolder == null)
            return;
        String strJson = KecUtilities.getTabLocalData(subFolder);

        ArrayList<VideoListItem> items = null;
        if (strJson != null && !strJson.isEmpty()) {
            items = getListFromJson(strJson);
        }
        if (items != null && !items.isEmpty()) {
            onRefreshComplete(items);
        } else {
            Refresh(SwipyRefreshLayoutDirection.BOTH);
        }
    }

    public ArrayList<VideoListItem> getListFromJson(String strJson) {
        try {
            Gson gson = new Gson();

            Type typeOfObjects = new TypeToken<ArrayList<VideoListItem>>() {
            }.getType();

            return gson.fromJson(strJson, typeOfObjects);
        } catch (Exception e) {
            Log.e(MainActivity.LOGTAG, e.getMessage());
        }
        return null;
    }

    public static String getJsonFromObject(ArrayList<VideoListItem> items) {
        try {
            Gson gson = new Gson();

            Type typeOfObjects = new TypeToken<ArrayList<VideoListItem>>() {
            }.getType();

            return gson.toJson(items, typeOfObjects);
        } catch (Exception e) {
            Log.e(MainActivity.LOGTAG, e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    // for the first time when init using this
    private void onRefreshComplete(ArrayList<VideoListItem> result) {
        if (result == null || result.isEmpty())
            return;
        if (mVideoAdapter != null) {
            Log.d(MainActivity.LOGTAG, "ListView(mAdapter) already had data, and will be cleared...");
        }
        try {
            if (activity == null)
                return;
            // first add to adapter and listView
            mVideoAdapter = new VideoListViewAdapter(activity, R.layout.video_list_item, result);
            mListView.setAdapter(mVideoAdapter);

            // now can start another task to load image async
            // we need url and position, if we use one thread to do all the download, so we store position in listitem.
            // be sure that the size of the array won't be too large, it's kind of waste the memory...


            // determine position
            int position = 0;
            for (VideoListItem item : result) {
                item.setPosition(position);
                position++;
            }
            VideoListItem[] items = new VideoListItem[result.size()];
            result.toArray(items);

            new LoadHallVideoListThumbsTask(mListView, subFolder).execute(items);

        } catch (NullPointerException npe) {
            Log.e(MainActivity.LOGTAG, npe.getMessage());
            npe.printStackTrace();
        }
        catch (Exception e) {
            Log.e(MainActivity.LOGTAG, e.getMessage());
            e.printStackTrace();
        }

        mSwipyRefreshLayout.setRefreshing(false);
    }

    private void onRefreshCompleteTop(ArrayList<VideoListItem> result) {
        if (result == null || subFolder == null || result.isEmpty())
            return;
        ArrayList<VideoListItem> localData = null;
        try {
            // read local data, must have some, because of init
            String strJson = KecUtilities.getTabLocalData(subFolder);

            if (strJson != null && !strJson.isEmpty()) {
                localData = getListFromJson(strJson);
            }
            // first add/insert into adapter/list
            // suppose result is ordered.
            // should insert into list from the last item...

            VideoListItem[] items = new VideoListItem[result.size()];
            for (int position = result.size() - 1; position >= 0; position--) {

                VideoListItem item = result.get(position);
                mVideoAdapter.insert(item, 0);
                if (localData != null)
                    localData.add(0, item);
                item.setPosition(position);
                items[position] = item;
            }


            // write to local not append, write
            KecUtilities.writeTabLocalData(getJsonFromObject(localData), subFolder);

            new LoadHallVideoListThumbsTask(mListView, subFolder).execute(items);

        } catch (Exception e) {
            Log.e(MainActivity.LOGTAG, e.getMessage());
        }

        mSwipyRefreshLayout.setRefreshing(false);
    }

    private void onRefreshCompleteBottom(ArrayList<VideoListItem> result) {
        if (result == null || subFolder == null || result.isEmpty())
            return;
        ArrayList<VideoListItem> localData = null;
        try {
            // read local data, must have some, because of init
            String strJson = KecUtilities.getTabLocalData(subFolder);
            if (strJson != null && !strJson.isEmpty()) {
                localData = getListFromJson(strJson);
            }
            // 1. add/insert into adapter/list and set the correct position
            int position = mVideoAdapter.getCount();
            for (VideoListItem item : result) {
                item.setPosition(position);
                mVideoAdapter.add(item);
                if (localData != null)
                    localData.add(item);
                position++;
            }

            //if (result.size() > 0) {
                final int currentPosition = mListView.getFirstVisiblePosition();
                mListView.setSelection(currentPosition + 1);
                mListView.post(new Runnable() {
                    @Override
                    public void run() {
                        mListView.smoothScrollToPosition(currentPosition + 1);
                    }
                });
            //}

            VideoListItem[] items = new VideoListItem[result.size()];
            result.toArray(items);
            // write to local not append, write
            KecUtilities.writeTabLocalData(getJsonFromObject(localData), subFolder);
            new LoadHallVideoListThumbsTask(mListView, subFolder).execute(items);
        } catch (Exception e) {
            Log.e(MainActivity.LOGTAG, e.getMessage());
        }

        mSwipyRefreshLayout.setRefreshing(false);
    }

    // detect when this fragment is visible
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if (isVisibleToUser) {
            Log.d(MainActivity.LOGTAG, "tab_main_hall_video becomes visible.");
        } else {
            Log.d(MainActivity.LOGTAG, "tab_main_hall_video becomes invisible.");
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
        inflater.inflate(R.menu.menu_main, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // if want to handled in fragment
        // must return false in activity

        return super.onOptionsItemSelected(item);
    }

    // refresh list
    public void Refresh(SwipyRefreshLayoutDirection direction) {

        // actually bottom and init can use same interface??
        if (direction == SwipyRefreshLayoutDirection.TOP) {
            new UpdateThumbListTaskTop().execute(mVideoAdapter.getItem(0).getId());
        } else if (direction == SwipyRefreshLayoutDirection.BOTTOM) {
            int i = mVideoAdapter.getCount();
            new UpdateThumbListTaskBottom().execute(mVideoAdapter.getItem(i - 1).getId());
        } else
            // use as init
            new UpdateThumbListTask().execute(0);
    }

    // for download thumbs
    // may need to write 3 task
    private class UpdateThumbListTask extends AsyncTask<Integer, Void, String> {

        public UpdateThumbListTask() {
        }

        @Override
        protected String doInBackground(Integer... params) {
            if (strType == null || strType.isEmpty())
                return null;
            try {

                //String strURL = "http://173.236.36.10/cds/generateVideoListThumb.php?tabtype=" + tabType;
                String strURL = "http://198.105.216.190/generateVideolist.ashx?id=&count=6&user=" + tabFollow + strType + URLEncoder.encode(tabName, MainActivity.ENCODING);;

                URL url = new URL(strURL);

                URLConnection connection = url.openConnection();

                connection.connect();

                InputStream inputStream = new BufferedInputStream(url.openStream(), 10 * 1024);
                //int length = connection.getContentLength();
                return KecUtilities.readStringFromStream(inputStream);
            } catch (Exception e) {
                Log.e(MainActivity.LOGTAG, e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            ArrayList<VideoListItem> items = getListFromJson(result);

            if (result != null && !items.isEmpty()) {
                KecUtilities.writeTabLocalData(result, subFolder);
                onRefreshComplete(items);
            }
            mSwipyRefreshLayout.setRefreshing(false);
        }
    }

    private class UpdateThumbListTaskTop extends AsyncTask<Integer, Void, String> {
        @Override
        protected String doInBackground(Integer... params) {
            if (strType == null || strType.isEmpty())
                return null;
            try {
                int id = params[0];
                String strURL = "http://198.105.216.190/generateVideolist.ashx?id=" + id + "&count=2&direction=after&user=" + tabFollow + strType + URLEncoder.encode(tabName, MainActivity.ENCODING);;
                //String strURL = "http://173.236.36.10/cds/generateVideoListThumb.php?type=top&count=5&tabtype=" + tabType;
                URL url = new URL(strURL);

                URLConnection connection = url.openConnection();

                connection.connect();

                InputStream inputStream = new BufferedInputStream(url.openStream(), 10 * 1024);

                return KecUtilities.readStringFromStream(inputStream);

            } catch (Exception e) {
                Log.e(MainActivity.LOGTAG, e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            if (isCancelled())
                return;
            ArrayList<VideoListItem> items = getListFromJson(result);

            if (items != null && !items.isEmpty())
                onRefreshCompleteTop(items);
            mSwipyRefreshLayout.setRefreshing(false);
        }
    }

    private class UpdateThumbListTaskBottom extends AsyncTask<Integer, Void, String> {
        @Override
        protected String doInBackground(Integer... params) {

            if (strType == null || strType.isEmpty())
                return null;
            try {
                int id = params[0];
                String strURL = "http://198.105.216.190/generateVideolist.ashx?id=" + id + "&count=2&direction=after&user=" + tabFollow + strType + URLEncoder.encode(tabName, MainActivity.ENCODING);
                //String strURL = "http://173.236.36.10/cds/generateVideoListThumb.php?type=bottom&count=5&tabtype=" + tabType;
                URL url = new URL(strURL);

                URLConnection connection = url.openConnection();

                connection.connect();

                InputStream inputStream = new BufferedInputStream(url.openStream(), 10 * 1024);
                //int length = connection.getContentLength();

                return KecUtilities.readStringFromStream(inputStream);

            } catch (Exception e) {
                Log.e(MainActivity.LOGTAG, e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (isCancelled())
                return;

            ArrayList<VideoListItem> items = getListFromJson(result);

            if (items != null && !items.isEmpty())
                onRefreshCompleteBottom(items);

            mSwipyRefreshLayout.setRefreshing(false);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(MainActivity.LOGTAG, "tab_main_hall_video onPause.");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(MainActivity.LOGTAG, "tab_main_hall_video onStop.");
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(MainActivity.LOGTAG, "tab_main_hall_video onStart.");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(MainActivity.LOGTAG, "tab_main_hall_video onResume.");
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        this.activity = null;
    }
}
