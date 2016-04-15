package com.kectech.android.wyslink.fragments;
import android.app.Fragment;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.kectech.android.wyslink.BuildConfig;
import com.kectech.android.wyslink.R;
import com.kectech.android.wyslink.activity.MainActivity;
import com.kectech.android.wyslink.activity.VideoViewActivity;
import com.kectech.android.wyslink.adapter.VideoListViewAdapter;
import com.kectech.android.wyslink.listitem.VideoListItem;
import com.kectech.android.wyslink.thirdparty.CacheBitmap.ImageFetcher;
import com.kectech.android.wyslink.thirdparty.SwipeRefreshLayout;
import com.kectech.android.wyslink.thirdparty.SwipeRefreshLayoutDirection;
import com.kectech.android.wyslink.util.KecUtilities;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.SocketTimeoutException;
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

    private SwipeRefreshLayout mSwipeRefreshLayout;

    private int tabId = 0;
    private String tabName;
    private String strType;
    private String tabFollow;

    private String subFolder = null;

    private ImageFetcher mImageFetcher;

    //private CustomTabActivityHelper mCustomTabActivityHelper;

    public void setType(int tabType) {

        switch (tabType) {
            case 1:
                // public
                break;
            case 2:
                // showroom
                break;
            case 3:
                // event hall
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
    @SuppressWarnings("deprecation")
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.tab_main_hall_video, container, false);

        mListView = (ListView) v.findViewById(R.id.video_tab_list);
        mSwipeRefreshLayout = (SwipeRefreshLayout) v.findViewById(R.id.video_tab_swipe_refresh_layout);
        mSwipeRefreshLayout.setDirection(SwipeRefreshLayoutDirection.BOTH);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh(SwipeRefreshLayoutDirection direction) {
                Refresh(direction);
            }
        });

//        // click listener
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                VideoListItem videoListItem = mVideoAdapter.getItem(position);
                String strUrl = videoListItem.getVideoUrl();
                String strVideo = strUrl.substring(strUrl.indexOf("?url=") + 5, strUrl.indexOf("&tl="));
                Intent intent = new Intent(getActivity(), VideoViewActivity.class);
                intent.putExtra(MainActivity.BUNDLE_KEY_SHARE_TITLE, videoListItem.getTitle());
                intent.putExtra(MainActivity.BUNDLE_KEY_SHARE_DESCRIPTION, videoListItem.getDescription());
                intent.putExtra(MainActivity.BUNDLE_KEY_CONTENT_URL_ENCODE, strUrl);
                intent.putExtra(MainActivity.BUNDLE_KEY_CONTENT_URL, KecUtilities.decryptUrl(strVideo));
                // rtmp is not accepted by fb sdk. cause error code 100, href is not properly formatted

//                // get another activity to run
//                Intent intent = new Intent(activity, VideoOfHallOfMainActivity.class);
//
//                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
//
//                intent.putExtra(MainActivity.VIDEO_OF_HALL_OF_MAIN_URL, videoListItem.getVideoUrl());
                startActivity(intent);
                getActivity().overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
            }
        });

        mSwipeRefreshLayout.setColorScheme(
                R.color.swipe_color_1,
                R.color.swipe_color_3,
                R.color.swipe_color_5);

        mImageFetcher = KecUtilities.getThumbFetcher(getActivity());
        initList();

        //mCustomTabActivityHelper = new CustomTabActivityHelper();
        return v;
    }

    public void initList() {
        // read from local first
        if (subFolder == null)
            return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
            // allow async task to run simultaneously
            new InitListTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        else
            new InitListTask().execute();
    }

    public ArrayList<VideoListItem> getListFromJson(String strJson) {
        try {
            Gson gson = new Gson();

            Type typeOfObjects = new TypeToken<ArrayList<VideoListItem>>() {
            }.getType();

            return gson.fromJson(strJson, typeOfObjects);
        } catch (Exception e) {
            Log.e(MainActivity.LOG_TAG, "Exception caught(tab_main_hall_video---getListFromJson): " + e.getMessage());
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
            Log.e(MainActivity.LOG_TAG, "Exception caught(tab_main_hall_video---getJsonFromObject): " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    // for the first time when init using this
    private void onRefreshComplete(ArrayList<VideoListItem> result) {
        if (result == null || result.isEmpty())
            return;
        if (mVideoAdapter != null) {
            if (BuildConfig.DEBUG)
                Log.d(MainActivity.LOG_TAG, "ListView(mAdapter) already had data, and will be cleared...");
        }
        try {
            // first add to adapter and listView
            mVideoAdapter = new VideoListViewAdapter(getActivity(), R.layout.video_list_item, result, mImageFetcher);
            mListView.setAdapter(mVideoAdapter);
        } catch (NullPointerException npe) {
            Log.e(MainActivity.LOG_TAG, npe.getMessage());
            npe.printStackTrace();
        } catch (Exception e) {
            Log.e(MainActivity.LOG_TAG, "Exception caught(tab_main_hall_video---onRefreshComplete): " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void onRefreshCompleteTop(ArrayList<VideoListItem> result) {
        if (result == null || result.isEmpty())
            return;
        try {
            for (int position = result.size() - 1; position >= 0; position--) {
                VideoListItem item = result.get(position);
                mVideoAdapter.insert(item, 0);
            }
        } catch (Exception e) {
            Log.e(MainActivity.LOG_TAG, "Exception caught(tab_main_hall_video---onRefreshCompleteTop): " + e.getMessage());
        }
    }

    private void onRefreshCompleteBottom(ArrayList<VideoListItem> result) {
        if (result == null || result.isEmpty())
            return;
        try {
            for (VideoListItem item : result) {

                mVideoAdapter.add(item);
            }

            final int currentPosition = mListView.getFirstVisiblePosition();
            mListView.setSelection(currentPosition + 1);
            mListView.post(new Runnable() {
                @Override
                public void run() {
                    mListView.smoothScrollToPosition(currentPosition + 1);
                }
            });
        } catch (Exception e) {
            Log.e(MainActivity.LOG_TAG, "Exception caught(tab_main_hall_video---onRefreshCompleteBottom): " + e.getMessage());
        }
    }

    // detect when this fragment is visible
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if (BuildConfig.DEBUG) {
            if (isVisibleToUser) {
                Log.d(MainActivity.LOG_TAG, "tab_main_hall_video becomes visible.");
            } else {
                Log.d(MainActivity.LOG_TAG, "tab_main_hall_video becomes invisible.");
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
        inflater.inflate(R.menu.menu_video_tab, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    // refresh list
    public void Refresh(SwipeRefreshLayoutDirection direction) {

        // actually bottom and init can use same interface??
        if (direction == SwipeRefreshLayoutDirection.TOP) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
                // allow async task to run simultaneously
                new UpdateThumbListTaskTop().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, mVideoAdapter.getItem(0).getId());
            else
                new UpdateThumbListTaskTop().execute(mVideoAdapter.getItem(0).getId());
        } else if (direction == SwipeRefreshLayoutDirection.BOTTOM) {
            int i = mVideoAdapter.getCount();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
                // allow async task to run simultaneously
                new UpdateThumbListTaskBottom().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, mVideoAdapter.getItem(i - 1).getId());
            else
                new UpdateThumbListTaskBottom().execute(mVideoAdapter.getItem(i - 1).getId());
        } else
            // use as init
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
                // allow async task to run simultaneously
                new UpdateThumbListTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, 0);
            else
                new UpdateThumbListTask().execute(0);
    }

    // for download thumbs
    // may need to write 3 task
    private class UpdateThumbListTask extends AsyncTask<Integer, Void, ArrayList<VideoListItem>> {

        public UpdateThumbListTask() {
        }

        @Override
        protected ArrayList<VideoListItem> doInBackground(Integer... params) {
            if (strType == null || strType.isEmpty())
                return null;
            try {

                //String strURL = "http://173.236.36.10/cds/generateVideoListThumb.php?tabtype=" + tabType;
                String strURL = "http://198.105.216.190/generateVideolist.ashx?id=&count=6&user=" + tabFollow + strType + URLEncoder.encode(tabName, MainActivity.ENCODING);

                URL url = new URL(strURL);

                URLConnection connection = url.openConnection();
                connection.setConnectTimeout(MainActivity.CONNECTION_TIMEOUT);
                connection.setReadTimeout(MainActivity.CONNECTION_TIMEOUT);
                connection.connect();

                InputStream inputStream = new BufferedInputStream(url.openStream(), MainActivity.DOWNLOAD_BUFFER);
                //int length = connection.getContentLength();
                String strResult = KecUtilities.readStringFromStream(inputStream);

                ArrayList<VideoListItem> items = getListFromJson(strResult);
                if (items != null && !items.isEmpty()) {
                    if (subFolder != null)
                        KecUtilities.writeTabLocalData(strResult, subFolder);
                    return items;
                }

            } catch (SocketTimeoutException ste) {
                Log.e(MainActivity.LOG_TAG, "time out:" + ste.getMessage());
            } catch (IOException e) {
                Log.e(MainActivity.LOG_TAG, e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(ArrayList<VideoListItem> result) {
            super.onPostExecute(result);
            onRefreshComplete(result);
            mSwipeRefreshLayout.setRefreshing(false);
        }
    }

    private class UpdateThumbListTaskTop extends AsyncTask<Integer, Void, ArrayList<VideoListItem>> {
        @Override
        protected ArrayList<VideoListItem> doInBackground(Integer... params) {
            if (strType == null || strType.isEmpty())
                return null;
            try {
                int id = params[0];
                String strURL = "http://198.105.216.190/generateVideolist.ashx?id=" + id + "&count=2&direction=after&user=" + tabFollow + strType + URLEncoder.encode(tabName, MainActivity.ENCODING);
                //String strURL = "http://173.236.36.10/cds/generateVideoListThumb.php?type=top&count=5&tabtype=" + tabType;
                URL url = new URL(strURL);

                URLConnection connection = url.openConnection();
                connection.setConnectTimeout(MainActivity.CONNECTION_TIMEOUT);
                connection.setReadTimeout(MainActivity.CONNECTION_TIMEOUT);
                connection.connect();

                InputStream inputStream = new BufferedInputStream(url.openStream(), MainActivity.DOWNLOAD_BUFFER);

                String strResult = KecUtilities.readStringFromStream(inputStream);

                ArrayList<VideoListItem> items = getListFromJson(strResult);

                if (items != null && !items.isEmpty()) {
                    if (subFolder != null) {
                        ArrayList<VideoListItem> localData = null;
                        String strJson = KecUtilities.getTabLocalData(subFolder);

                        if (strJson != null && !strJson.isEmpty()) {
                            localData = getListFromJson(strJson);
                        }

                        for (int position = items.size() - 1; position >= 0; position--) {

                            VideoListItem item = items.get(position);
                            if (localData != null)
                                localData.add(0, item);
                        }
                        KecUtilities.writeTabLocalData(getJsonFromObject(localData), subFolder);
                    }
                    return items;
                }

            } catch (SocketTimeoutException ste) {
                Log.e(MainActivity.LOG_TAG, "time out:" + ste.getMessage());
            } catch (IOException e) {
                Log.e(MainActivity.LOG_TAG, e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(ArrayList<VideoListItem> result) {
            super.onPostExecute(result);

            if (isCancelled())
                return;
            onRefreshCompleteTop(result);
            mSwipeRefreshLayout.setRefreshing(false);
        }
    }

    private class UpdateThumbListTaskBottom extends AsyncTask<Integer, Void, ArrayList<VideoListItem>> {
        @Override
        protected ArrayList<VideoListItem> doInBackground(Integer... params) {

            if (strType == null || strType.isEmpty())
                return null;
            try {
                int id = params[0];
                String strURL = "http://198.105.216.190/generateVideolist.ashx?id=" + id + "&count=2&direction=before&user=" + tabFollow + strType + URLEncoder.encode(tabName, MainActivity.ENCODING);
                //String strURL = "http://173.236.36.10/cds/generateVideoListThumb.php?type=bottom&count=5&tabtype=" + tabType;
                URL url = new URL(strURL);

                URLConnection connection = url.openConnection();
                connection.setConnectTimeout(MainActivity.CONNECTION_TIMEOUT);
                connection.setReadTimeout(MainActivity.CONNECTION_TIMEOUT);
                connection.connect();

                InputStream inputStream = new BufferedInputStream(url.openStream(), MainActivity.DOWNLOAD_BUFFER);
                //int length = connection.getContentLength();

                String strResult = KecUtilities.readStringFromStream(inputStream);
                ArrayList<VideoListItem> items = getListFromJson(strResult);
                if (items != null && !items.isEmpty()) {
                    if (subFolder != null) {
                        ArrayList<VideoListItem> localData = null;
                        String strJson = KecUtilities.getTabLocalData(subFolder);
                        if (strJson != null && !strJson.isEmpty()) {
                            localData = getListFromJson(strJson);
                        }
                        for (VideoListItem item : items) {
                            if (localData != null)
                                localData.add(item);
                        }
                        KecUtilities.writeTabLocalData(getJsonFromObject(localData), subFolder);
                    }
                    return items;
                }

            } catch (SocketTimeoutException ste) {
                Log.e(MainActivity.LOG_TAG, "time out:" + ste.getMessage());
            } catch (IOException e) {
                Log.e(MainActivity.LOG_TAG, e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(ArrayList<VideoListItem> result) {
            super.onPostExecute(result);
            if (isCancelled())
                return;
            onRefreshCompleteBottom(result);

            mSwipeRefreshLayout.setRefreshing(false);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        //mCustomTabActivityHelper.unbindCustomTabsService(getActivity());
        if (BuildConfig.DEBUG)
            Log.d(MainActivity.LOG_TAG, "tab_main_hall_video onStop.");
    }

    @Override
    public void onStart() {
        super.onStart();
        //mCustomTabActivityHelper.bindCustomTabsService(getActivity());
        if (BuildConfig.DEBUG)
            Log.d(MainActivity.LOG_TAG, "tab_main_hall_video onStart.");
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mImageFetcher != null)
            mImageFetcher.setExitTasksEarly(false);
        if (mVideoAdapter != null)
            mVideoAdapter.notifyDataSetChanged();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mImageFetcher != null) {
            mImageFetcher.setPauseWork(false);
            mImageFetcher.setExitTasksEarly(true);
            mImageFetcher.flushCache();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //mImageFetcher.closeCache();
    }

    private class InitListTask extends AsyncTask<Void, Void, ArrayList<VideoListItem>> {
        @Override
        protected ArrayList<VideoListItem> doInBackground(Void... params) {
            // read local file
            String strJson = KecUtilities.getTabLocalData(subFolder);

            ArrayList<VideoListItem> items = null;
            if (strJson != null && !strJson.isEmpty()) {
                items = getListFromJson(strJson);
            }
            if (items != null && !items.isEmpty()) {
                return items;
            } else {
                Refresh(SwipeRefreshLayoutDirection.BOTH);
            }

            return null;
        }

        @Override
        protected void onPostExecute(ArrayList<VideoListItem> result) {
            super.onPostExecute(result);
            if (isCancelled())
                return;

            onRefreshComplete(result);
        }
    }
}