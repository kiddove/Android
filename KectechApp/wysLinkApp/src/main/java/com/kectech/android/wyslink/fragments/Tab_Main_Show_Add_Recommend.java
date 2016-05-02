package com.kectech.android.wyslink.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.ActionMode;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ListView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.kectech.android.wyslink.BuildConfig;
import com.kectech.android.wyslink.R;
import com.kectech.android.wyslink.activity.MainActivity;
import com.kectech.android.wyslink.adapter.ShowAddRecommendListViewAdapter;
import com.kectech.android.wyslink.listitem.MainShowAddRecommendListItem;
import com.kectech.android.wyslink.thirdparty.CacheBitmap.ImageFetcher;
import com.kectech.android.wyslink.thirdparty.CacheBitmap.Utils;
import com.kectech.android.wyslink.thirdparty.SwipeRefreshLayout;
import com.kectech.android.wyslink.thirdparty.SwipeRefreshLayoutDirection;
import com.kectech.android.wyslink.util.KecUtilities;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;

/**
 * Created by Paul on 20/04/2016.
 * recommend showroom tab implements by a ListView
 * use a SwipeRefreshLayout to fulfil pull down and pull up refresh
 */

public class Tab_Main_Show_Add_Recommend extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    private ListView mListView;

    private ShowAddRecommendListViewAdapter mAdapter;

    private SwipeRefreshLayout mSwipeRefreshLayout;

    private ActionMode mMode;

    private int num = 0;

    private ImageFetcher mImageFetcher;

    //private ArrayList<Integer> mDeleteList = new ArrayList<>();

    private String subFolder;

    @Override
    @SuppressWarnings("deprecation")
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.tab_main_show, container, false);
        subFolder = MainActivity.USER + File.separator + MainActivity.SHOW_SUB_FOLDER + File.separator + getString(R.string.label_recommend_showroom);
        mListView = (ListView) v.findViewById(R.id.show_tab_list);

        mSwipeRefreshLayout = (SwipeRefreshLayout) v.findViewById(R.id.show_tab_swipe_refresh_layout);
        mSwipeRefreshLayout.setDirection(SwipeRefreshLayoutDirection.BOTH);
        mSwipeRefreshLayout.setOnRefreshListener(this);

//        // click listener
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                if (mAdapter.showCheckBox) {
                    boolean checked = mAdapter.isChecked(position);
                    onItemChecked(mMode, position, !checked);
                    // if no selected
                    if (mAdapter.isSelectionEmpty()) {
                        stopActionMode();
                    }
                }
            }
        });
        mListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int scrollState) {
                // Pause fetcher to ensure smoother scrolling when flinging
                if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_FLING) {
                    // Before Honeycomb pause image loading on scroll to help with performance
                    if (!Utils.hasHoneycomb()) {
                        mImageFetcher.setPauseWork(true);
                    }
                } else {
                    mImageFetcher.setPauseWork(false);
                }
            }

            @Override
            public void onScroll(AbsListView absListView, int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount) {
            }
        });

        mSwipeRefreshLayout.setColorScheme(
                R.color.swipe_color_1,
                R.color.swipe_color_3,
                R.color.swipe_color_5);

//        ImageCache.ImageCacheParams cacheParams =
//                new ImageCache.ImageCacheParams(getActivity(), IMAGE_CACHE_DIR);
//
//        cacheParams.setMemCacheSizePercent(0.05f); // Set memory cache to 5% of app memory
//
//        // The ImageFetcher takes care of loading images into our ImageView children asynchronously
//        mImageFetcher = new ImageFetcher(getActivity(), 100);
//        //mImageFetcher.setLoadingImage(R.drawable.empty_photo);
//        mImageFetcher.addImageCache(getActivity().getFragmentManager(), cacheParams);
        mImageFetcher = KecUtilities.getThumbFetcher(getActivity());

        initList();

        // long click to delete
        // method 1
        //mListView.setLongClickable(true);
//        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
//            @Override
//            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
//                //Toast.makeText(getActivity(), "will delete " + position, Toast.LENGTH_SHORT).show();
//                mMode = getActivity().startActionMode(new ModeCallback());
//                //return true will be prevent click event to be continue
//                return true;
//            }
//        });

        // method 2
        //registerForContextMenu(mListView);

        // method 3
        mListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        mListView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {

            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
                onItemChecked(mode, position, checked);
                mMode = mode;
            }

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                initActionMode(mode, menu);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return prepareActionMode();
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                return actionItemClicked(item);
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                destroyActionMode();
                mMode = null;
            }
        });

        return v;
    }


    // detect when this fragment is visible
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if (isVisibleToUser) {
            if (BuildConfig.DEBUG)
                Log.d(MainActivity.LOG_TAG, "tab_main_show_add_recommend becomes visible.");
            // todo if visible refresh data
        } else {
            //hide cab
            if (BuildConfig.DEBUG)
                Log.d(MainActivity.LOG_TAG, "tab_main_show_add_recommend becomes invisible.");
            stopActionMode();
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
        inflater.inflate(R.menu.menu_show_add_recommend, menu);
        //super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // if want to handled in fragment
        // must return false in activity
        int id = item.getItemId();

        switch (id) {
            case R.id.menu_show_tab_item_add:
                mMode = getActivity().startActionMode(new ModeCallback());
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        String[] option = {"YES", "NO"};
        if (v.getId() == R.id.show_tab_list) {
            //AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
            menu.setHeaderTitle("Follow");
            for (int i = 0; i < option.length; i++) {
                menu.add(Menu.NONE, i, i, option[i]);
            }
        }
    }

//    @Override
//    public boolean onContextItemSelected(MenuItem item) {
//        //AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
//        int menuItemIndex = item.getItemId();
//        String[] option = {"YES", "NO"};
//
//        Toast.makeText(activity, option[menuItemIndex] + " selected.", Toast.LENGTH_SHORT).show();
//        return true;
//    }

    private final class ModeCallback implements ActionMode.Callback {

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            initActionMode(mode, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            boolean bRet = prepareActionMode();
            if (mAdapter != null)
                mAdapter.notifyDataSetChanged();
            return bRet;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            destroyActionMode();
            if (mAdapter != null) {
                mAdapter.notifyDataSetChanged();
                mMode = null;
            }
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            return actionItemClicked(item);
        }
    }

    private View getListItemViewByPosition(int position) {

//        int i1 = mListView.getFirstVisiblePosition();
//        int i2 = mListView.getHeaderViewsCount();

        final int firstPosition = mListView.getFirstVisiblePosition() - mListView.getHeaderViewsCount();
        final int lastPosition = mListView.getChildCount() - 1 + firstPosition;

        if (position < firstPosition || position > lastPosition) {
            return mAdapter.getView(position, null, mListView);
        } else {
            final int childIndex = position - firstPosition;
            return mListView.getChildAt(childIndex);
        }
    }

    private void onItemChecked(ActionMode mode, int position, boolean checked) {
        if (checked) {
            num++;
            mAdapter.setSelection(position, true);
            // set check
        } else {
            num--;
            mAdapter.removeSelect(position);
        }

//                View itemView = mListView.getChildAt(position);
        View itemView = getListItemViewByPosition(position);
        if (itemView != null) {
            CheckBox checkBox = (CheckBox) itemView.findViewById(R.id.tab_main_show_list_item_check);
            checkBox.setChecked(checked);
        }

        mode.setTitle(num + " selected.");
    }

    private void initActionMode(ActionMode mode, Menu menu) {
        num = 0;
        MenuInflater inflater = mode.getMenuInflater();
        inflater.inflate(R.menu.menu_show_add_recommend_cab, menu);
    }

    private boolean prepareActionMode() {
        // Here, you can checked selected items to adapt available actions
        // set NONE
        mSwipeRefreshLayout.setDirection(SwipeRefreshLayoutDirection.NONE);
        if (mAdapter != null)
            mAdapter.showCheckBox = true;
        return false;
    }

    private void destroyActionMode() {
        // set NONE to BOTH
        mSwipeRefreshLayout.setDirection(SwipeRefreshLayoutDirection.BOTH);
        if (mAdapter != null) {
            mAdapter.showCheckBox = false;
            mAdapter.clearSelection();
            num = 0;
        }
    }

    private boolean actionItemClicked(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.show_add_recommend_tab_action_done:
                returnToCaller();
                return true;
            default:
                return false;
        }
    }

    // for download thumbs
    // may need to write 3 task
    private class UpdateThumbListTask extends AsyncTask<Integer, Void, ArrayList<MainShowAddRecommendListItem>> {

        public UpdateThumbListTask() {
        }

        @Override
        protected ArrayList<MainShowAddRecommendListItem> doInBackground(Integer... params) {
            // step1 Read from local if has data
            // step2 if not send http request
            // if updated write to local... after refresh... a lot of work to do
            // first try to request every time ...
            // for test
            //String strURL = "http://173.236.36.10/cds/generateThumbnail.php";
            //String strURL = "http://198.105.216.190/generateThumbnail.ashx?id=&count=6&user=" + MainActivity.USER;
            String strURL = "http://206.190.141.88/generateSRList.ashx?count=100&user=" + MainActivity.USER;

            try {
                URL url = new URL(strURL);

                URLConnection connection = url.openConnection();
                connection.setConnectTimeout(MainActivity.CONNECTION_TIMEOUT);
                connection.setReadTimeout(MainActivity.CONNECTION_TIMEOUT);
                connection.connect();

                InputStream inputStream = new BufferedInputStream(url.openStream(), MainActivity.DOWNLOAD_BUFFER);
                //int length = connection.getContentLength();
                String strResult = KecUtilities.readStringFromStream(inputStream);
                ArrayList<MainShowAddRecommendListItem> items = getListFromJson(strResult);
                if (items == null)
                    return new ArrayList<>();
                else if (!items.isEmpty()) {
                    KecUtilities.writeTabLocalData(strResult, subFolder);
                    return items;
                } else
                    return items;

            } catch (SocketTimeoutException ste) {
                Log.e(MainActivity.LOG_TAG, "time out:" + ste.getMessage());
            } catch (IOException e) {
                Log.e(MainActivity.LOG_TAG, e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(ArrayList<MainShowAddRecommendListItem> result) {
            super.onPostExecute(result);

            onRefreshComplete(result);
            mSwipeRefreshLayout.setRefreshing(false);
        }
    }

    private class UpdateThumbListTaskTop extends AsyncTask<Integer, Void, ArrayList<MainShowAddRecommendListItem>> {
        @Override
        protected ArrayList<MainShowAddRecommendListItem> doInBackground(Integer... params) {
            // for test
            int id = params[0];
//            String strURL = "http://173.236.36.10/cds/generateThumbnail.php?type=top&count=5";
            //String strURL = "http://198.105.216.190/generateThumbnail.ashx?id=" + id + "&count=2" + "&direction=after&user=" + MainActivity.USER;
            String strURL = "http://206.190.141.88/generateSRList.ashx?id=" + id + "&count=5&direction=after&user=" + MainActivity.USER;

            try {
                URL url = new URL(strURL);

                URLConnection connection = url.openConnection();
                connection.setConnectTimeout(MainActivity.CONNECTION_TIMEOUT);
                connection.setReadTimeout(MainActivity.CONNECTION_TIMEOUT);
                connection.connect();

                InputStream inputStream = new BufferedInputStream(url.openStream(), MainActivity.DOWNLOAD_BUFFER);

                String strResult = KecUtilities.readStringFromStream(inputStream);
                ArrayList<MainShowAddRecommendListItem> items = getListFromJson(strResult);
                if (items != null && !items.isEmpty()) {
                    ArrayList<MainShowAddRecommendListItem> localData = null;
                    String strJson = KecUtilities.getTabLocalData(subFolder);

                    if (strJson != null && !strJson.isEmpty()) {
                        localData = getListFromJson(strJson);
                    }
                    // first add/insert into adapter/list
                    // suppose result is ordered.
                    // should insert into list from the last item...

                    //MainShowAddRecommendListItem[] items = new MainShowAddRecommendListItem[result.size()];
                    for (int position = items.size() - 1; position >= 0; position--) {

                        MainShowAddRecommendListItem item = items.get(position);
                        if (localData != null)
                            localData.add(0, item);
                    }
                    // write to local not append, write
                    KecUtilities.writeTabLocalData(getJsonFromObject(localData), subFolder);

                    return items;
                }

            } catch (SocketTimeoutException ste) {
                Log.e(MainActivity.LOG_TAG, "time out: " + ste.getMessage());
            } catch (IOException e) {
                Log.e(MainActivity.LOG_TAG, e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(ArrayList<MainShowAddRecommendListItem> result) {
            super.onPostExecute(result);

            if (isCancelled())
                return;

            onRefreshCompleteTop(result);
            mSwipeRefreshLayout.setRefreshing(false);
        }
    }

    private class UpdateThumbListTaskBottom extends AsyncTask<Integer, Void, ArrayList<MainShowAddRecommendListItem>> {
        @Override
        protected ArrayList<MainShowAddRecommendListItem> doInBackground(Integer... params) {

            int id = params[0];
            // for test
            //String strURL = "http://173.236.36.10/cds/generateThumbnail.php?type=bottom&count=5";
            //String strURL = "http://198.105.216.190/generateThumbnail.ashx?id=" + id + "&count=2" + "&direction=before&user=" + MainActivity.USER;
            String strURL = "http://206.190.141.88/generateSRList.ashx?id=" + id + "&count=5&direction=before&user=" + MainActivity.USER;

            try {
                URL url = new URL(strURL);

                URLConnection connection = url.openConnection();
                connection.setConnectTimeout(MainActivity.CONNECTION_TIMEOUT);
                connection.setReadTimeout(MainActivity.CONNECTION_TIMEOUT);

                connection.connect();

                InputStream inputStream = new BufferedInputStream(url.openStream(), MainActivity.DOWNLOAD_BUFFER);
                //int length = connection.getContentLength();

                String strResult = KecUtilities.readStringFromStream(inputStream);
                ArrayList<MainShowAddRecommendListItem> items = getListFromJson(strResult);
                if (items != null && !items.isEmpty()) {
                    ArrayList<MainShowAddRecommendListItem> localData = null;
                    String strJson = KecUtilities.getTabLocalData(subFolder);
                    if (strJson != null && !strJson.isEmpty()) {
                        localData = getListFromJson(strJson);
                    }

                    for (MainShowAddRecommendListItem item : items) {

                        if (localData != null)
                            localData.add(item);

                    }

                    KecUtilities.writeTabLocalData(getJsonFromObject(localData), subFolder);

                    // UI
                    return items;
                }

            } catch (SocketTimeoutException ste) {
                Log.e(MainActivity.LOG_TAG, "time out: " + ste.getMessage());
            } catch (IOException e) {
                Log.e(MainActivity.LOG_TAG, e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(ArrayList<MainShowAddRecommendListItem> result) {
            super.onPostExecute(result);
            if (isCancelled())
                return;

            onRefreshCompleteBottom(result);
            mSwipeRefreshLayout.setRefreshing(false);
        }
    }

    // refresh list
    public void Refresh(SwipeRefreshLayoutDirection direction) {
        // actually bottom and init can use same interface??
        if (direction == SwipeRefreshLayoutDirection.TOP && mAdapter != null) {
            if (mAdapter.getCount() > 0) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
                    // allow async task to run simultaneously
                    new UpdateThumbListTaskTop().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, mAdapter.getItem(0).getId());
                else
                    new UpdateThumbListTaskTop().execute(mAdapter.getItem(0).getId());

            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
                    // allow async task to run simultaneously
                    new UpdateThumbListTaskTop().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, 0);
                else
                    new UpdateThumbListTaskTop().execute(0);
            }
        } else if (direction == SwipeRefreshLayoutDirection.BOTTOM && mAdapter != null) {
            if (mAdapter.getCount() > 0) {
                int i = mAdapter.getCount();

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
                    // allow async task to run simultaneously
                    new UpdateThumbListTaskBottom().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, mAdapter.getItem(i - 1).getId());
                else
                    new UpdateThumbListTaskBottom().execute(mAdapter.getItem(i - 1).getId());
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
                    // allow async task to run simultaneously
                    new UpdateThumbListTaskBottom().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, 0);
                else
                    new UpdateThumbListTaskBottom().execute(0);
            }
        } else {
            if (mAdapter != null)
                mAdapter.clear();
            // use as init

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
                // allow async task to run simultaneously
                new UpdateThumbListTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, 0);
            else
                new UpdateThumbListTask().execute(0);
        }
    }

    public void initList() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
            // allow async task to run simultaneously
            new InitListTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        else
            new InitListTask().execute();
    }

    public ArrayList<MainShowAddRecommendListItem> getListFromJson(String strJson) {
        try {
            Gson gson = new Gson();

            Type typeOfObjects = new TypeToken<ArrayList<MainShowAddRecommendListItem>>() {
            }.getType();

            return gson.fromJson(strJson, typeOfObjects);
        } catch (Exception e) {
            Log.e(MainActivity.LOG_TAG, "Exception caught(tab_main_show_add_recommend---getListFromJson): " + e.getMessage());
        }
        return null;
    }

    public static String getJsonFromObject(ArrayList<MainShowAddRecommendListItem> items) {
        try {
            Gson gson = new Gson();

            Type typeOfObjects = new TypeToken<ArrayList<MainShowAddRecommendListItem>>() {
            }.getType();

            return gson.toJson(items, typeOfObjects);
        } catch (Exception e) {
            Log.e(MainActivity.LOG_TAG, "Exception caught(tab_main_show_add_recommend---getJsonFromObject): " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    // for the first time when init using this, or update when sth wrong with list, need refresh again
    private void onRefreshComplete(ArrayList<MainShowAddRecommendListItem> result) {
        if (result == null)
            // means error occurred when sending http request, other wise result should be empty but not null
            return;
        if (mAdapter != null) {
            if (BuildConfig.DEBUG)
                Log.d(MainActivity.LOG_TAG, "ListView(mAdapter) already had data, and will be cleared...");

            mAdapter.clear();
            mAdapter.notifyDataSetChanged();
            if (!result.isEmpty()) {
                for (MainShowAddRecommendListItem item : result) {
                    mAdapter.add(item);

                }
                mAdapter.notifyDataSetChanged();
            }
            return;
        }

        try {

            // first add to adapter and listView
            mAdapter = new ShowAddRecommendListViewAdapter(getActivity(), R.layout.tab_main_show_list_item, result, mImageFetcher);
            mListView.setAdapter(mAdapter);

//            // now can start another task to load image async
//            // we need url and position, if we use one thread to do all the download, so we store position in listitem.
//            // be sure that the size of the array won't be too large, it's kind of waste the memory...
//
//
//            // determine position
//            int position = 0;
//            for (MainShowAddRecommendListItem item : result) {
//                item.setPosition(position);
//                position++;
//            }
//            MainShowAddRecommendListItem[] items = new MainShowAddRecommendListItem[result.size()];
//            result.toArray(items);
//
//            new LoadHallListThumbsTask(mListView).execute(items);
////            LoadHallListThumbsTask task = new LoadHallListThumbsTask(mListView);
////            task.execute(items);
////            currentTask.add(task);

        } catch (Exception e) {
            Log.e(MainActivity.LOG_TAG, "Exception caught(tab_main_show_add_recommend---onRefreshComplete): " + e.getMessage());
        }
    }

    private void onRefreshCompleteTop(ArrayList<MainShowAddRecommendListItem> result) {
        if (result == null || result.isEmpty())
            return;
        try {
            for (int position = result.size() - 1; position >= 0; position--) {

                MainShowAddRecommendListItem item = result.get(position);
                mAdapter.insert(item, 0);

            }
        } catch (Exception e) {
            Log.e(MainActivity.LOG_TAG, "Exception caught(tab_main_show_add_recommend---onRefreshCompleteTop): " + e.getMessage());
        }
    }

    private void onRefreshCompleteBottom(ArrayList<MainShowAddRecommendListItem> result) {
        if (result == null || result.isEmpty())
            return;

        try {
            for (MainShowAddRecommendListItem item : result) {
                mAdapter.add(item);
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
            Log.e(MainActivity.LOG_TAG, "Exception caught(tab_main_show_add_recommend---onRefreshCompleteBottom): " + e.getMessage());
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (BuildConfig.DEBUG)
            Log.d(MainActivity.LOG_TAG, "tab_main_show_add_recommend_add_recommend onStop.");

//        for (LoadHallListThumbsTask task : currentTask) {
//            task.cancel(true);
//        }
//        currentTask.clear();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (BuildConfig.DEBUG)
            Log.d(MainActivity.LOG_TAG, "tab_main_show_add_recommend_add_recommend onStart.");
    }

    private void stopActionMode() {
        if (mMode != null) {
            mMode.finish();
            mMode = null;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mImageFetcher != null)
            mImageFetcher.setExitTasksEarly(false);
        if (mAdapter != null)
            mAdapter.notifyDataSetChanged();
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

    @Override
    public void onRefresh(SwipeRefreshLayoutDirection direction) {
        //if (in edit mode or others do not refresh or just set listener to null or set direction to NONE?) NONE works OK.
        Refresh(direction);
    }

    private class InitListTask extends AsyncTask<Void, Void, ArrayList<MainShowAddRecommendListItem>> {
        @Override
        protected ArrayList<MainShowAddRecommendListItem> doInBackground(Void... params) {

            if (!KecUtilities.createSubFolders(subFolder))
                return null;
            String strJson = KecUtilities.getTabLocalData(subFolder);

            ArrayList<MainShowAddRecommendListItem> items = null;

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
        protected void onPostExecute(ArrayList<MainShowAddRecommendListItem> result) {
            super.onPostExecute(result);
            if (isCancelled())
                return;

            if (result != null && !result.isEmpty())
                onRefreshComplete(result);
        }
    }

    private void returnToCaller() {

        if (mAdapter == null || mAdapter.isSelectionEmpty()) {
            stopActionMode();
            return;
        }

        String strName = "";
        try {
            for (int i = mAdapter.getCount() - 1; i >= 0; i--) {
                if (!mAdapter.isChecked(i))
                    continue;
                strName += URLEncoder.encode(mAdapter.getItem(i).getName(), MainActivity.ENCODING);
                strName += ";";
            }
        } catch (UnsupportedEncodingException uee) {
            uee.printStackTrace();
            stopActionMode();
            return;
        }

        stopActionMode();

        if (!TextUtils.isEmpty(strName)) {
            // remove last ;
            strName = strName.substring(0, strName.length() - 1);
        }
        Intent intent = new Intent();
        intent.putExtra(MainActivity.SHOWROOM_NAME, strName);

        getActivity().setResult(Activity.RESULT_OK, intent);
        getActivity().finish();
        getActivity().overridePendingTransition(R.anim.enter_from_left, R.anim.exit_to_right);
    }
}
