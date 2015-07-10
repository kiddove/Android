package com.kectech.android.kectechapp.tabs;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.kectech.android.kectechapp.R;
import com.kectech.android.kectechapp.activity.MainActivity;
import com.kectech.android.kectechapp.activity.PhotoOfHallOfMainActivity;
import com.kectech.android.kectechapp.adapter.PhotoListViewAdapter;
import com.kectech.android.kectechapp.data.LoadHallPhotoListThumbsTask;
import com.kectech.android.kectechapp.listitem.PhotoListItem;
import com.kectech.android.kectechapp.thirdparty.SwipyRefreshLayout;
import com.kectech.android.kectechapp.thirdparty.SwipyRefreshLayoutDirection;
import com.kectech.android.kectechapp.util.KecUtilities;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;


/**
 * Created by Paul on 16/06/2015.
 * use SwipyRefreshLayout to refresh (pull up and pull down)
 * use ListView
 * async download thumbs then show in ListView's items(may be more than one thumb in one item, depend on the json...)
 * tab an item to open an activity to show image(s).
 */
public class Tab_Main_Hall_Photo extends Fragment {

    // list
    private ListView mListView;
    // adapter
    private PhotoListViewAdapter mPhotoAdapter;
    // swipyrefreshlayout
    private SwipyRefreshLayout mSwipyRefreshLayout;

    private int tabType = 0;
    private int tabId = 0;

    private String subFolder = null;

    public void setType(int tabType) {
        this.tabType = tabType;
    }

    public void setId(int tabId) {
        this.tabId = tabId;
    }

    public void createSubFolder(Context context) {
        String folder = MainActivity.USER + File.separator + MainActivity.HALL_SUB_FOLDER + File.separator + tabId + File.separator + MainActivity.PHOTO_SUB_FOLDER;
        if (KecUtilities.createSubFolders(context, folder)) {
            subFolder = folder;
        } else
            subFolder = null;

    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.tab_main_hall_photo, container, false);

        mListView = (ListView) v.findViewById(R.id.photo_tab_list);
        mSwipyRefreshLayout = (SwipyRefreshLayout) v.findViewById(R.id.photo_tab_swipy_refresh_layout);
        mSwipyRefreshLayout.setDirection(SwipyRefreshLayoutDirection.BOTH);

        // color of refresh spinner
        mSwipyRefreshLayout.setColorScheme(
                R.color.swipe_color_1,
                R.color.swipe_color_3,
                R.color.swipe_color_5);

        // list refresh listener
        mSwipyRefreshLayout.setOnRefreshListener(new SwipyRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh(SwipyRefreshLayoutDirection direction) {
                Refresh(direction);
            }
        });

        // item click listener
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                PhotoListItem photoListItem = mPhotoAdapter.getItem(position);

                // activate another activity to show full image
                Intent intent = new Intent(getActivity(), PhotoOfHallOfMainActivity.class);

                // create parameters
                Bundle params = new Bundle();

                ArrayList<String> thumbs = new ArrayList<>();
                ArrayList<String> images = new ArrayList<>();

                for (int i = 0; i < photoListItem.items.size(); i++) {
                    thumbs.add(photoListItem.items.get(i).getThumbURL());
                    images.add(photoListItem.items.get(i).getImageURL());
                }
                params.putStringArrayList(MainActivity.PHOTO_TAB_THUMB_URL_KEY, thumbs);
                params.putStringArrayList(MainActivity.PHOTO_TAB_IMAGE_URL_KEY, images);
                params.putString(MainActivity.MAIN_HALL_PHOTO_FOLDER, subFolder);
//                params.putString(MainActivity.PHOTO_TAB_THUMB_URL_KEY, photoListItem.getThumbURL());
//                params.putString(MainActivity.PHOTO_TAB_IMAGE_URL_KEY, photoListItem.getImageURL());

                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

                intent.putExtras(params);
                try {
                    startActivity(intent);
                    getActivity().overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
                } catch (Exception e) {
                    Log.e(MainActivity.LOGTAG, e.getMessage());
                }

            }
        });

        initList();

        return v;
    }


    // refresh list
    public void Refresh(SwipyRefreshLayoutDirection direction) {

        // actually bottom and init can use same interface??
        if (direction == SwipyRefreshLayoutDirection.TOP) {
            new UpdatePhotoListTaskTop().execute("todo");
        } else if (direction == SwipyRefreshLayoutDirection.BOTTOM) {
            new UpdatePhotoListTaskBottom().execute("todo");
        } else
            // use as init
            new UpdatePhotoListTask().execute("todo");
    }

    // used to get the list item json file init, top_refresh, bottom_refresh, maybe need param when sending http request
    // to be continued...
    // may need to write 3 task
    private class UpdatePhotoListTask extends AsyncTask<String, Void, String> {

        public UpdatePhotoListTask() {
        }

        @Override
        protected String doInBackground(String... params) {
            // step1 Read from loacl if has data
            // step2 if not send http request
            // if updated write to local... after refresh... a lot of work to do
            // first try to request every time ...

            // for test
            String strURL = "http://173.236.36.10/cds/generateThumbnail_multi.php";

            try {
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
            ArrayList<PhotoListItem> items = getListFromJson(result);

            if (result != null && subFolder != null) {
                KecUtilities.writeTabLocalData(result, subFolder, getActivity());
                onRefreshComplete(items);
            }
            mSwipyRefreshLayout.setRefreshing(false);
        }
    }

    private class UpdatePhotoListTaskTop extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            // for test
            String strURL = "http://173.236.36.10/cds/generateThumbnail_multi.php?type=top&count=5";

            try {
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

            ArrayList<PhotoListItem> items = getListFromJson(result);

            onRefreshCompleteTop(items);
            mSwipyRefreshLayout.setRefreshing(false);
        }
    }

    private class UpdatePhotoListTaskBottom extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {

            // for test
            String strURL = "http://173.236.36.10/cds/generateThumbnail_multi.php?type=bottom&count=5";

            try {
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
            ArrayList<PhotoListItem> items = getListFromJson(result);

            onRefreshCompleteBottom(items);
            mSwipyRefreshLayout.setRefreshing(false);
        }
    }

    // for the first time when init using this
    private void onRefreshComplete(ArrayList<PhotoListItem> result) {
        if (result == null || subFolder == null)
            return;
        if (mPhotoAdapter != null) {
            Log.d(MainActivity.LOGTAG, "ListView(mPhotoAdapter) already had data, and will be cleared...");
        }
        try {
            // first add to adapter and listview
            mPhotoAdapter = new PhotoListViewAdapter(getActivity(), R.layout.photo_list_item, result);
            mListView.setAdapter(mPhotoAdapter);

            // now can start another task to load image async
            // we need url and position, if we use one thread to do all the download, so we store position in photolistitem.
            // be sure that the size of the array won't be too large, it's kind of waste the memory...


            // determine position
            int position = 0;
            for (PhotoListItem item : result) {
                item.setPosition(position);
                position++;
            }
            PhotoListItem[] items = new PhotoListItem[result.size()];
            result.toArray(items);

            new LoadHallPhotoListThumbsTask(getActivity(), mPhotoAdapter, mListView, subFolder).execute(items);

        } catch (Exception e) {
            Log.e(MainActivity.LOGTAG, e.getMessage());
        }

        mSwipyRefreshLayout.setRefreshing(false);
    }

    private void onRefreshCompleteTop(ArrayList<PhotoListItem> result) {
        if (result == null || subFolder == null)
            return;
        ArrayList<PhotoListItem> localData = null;
        try {
            // read local data, must have some, because of init
            String strJson = KecUtilities.getTabLocalData(subFolder, getActivity());

            if (strJson != null && !strJson.isEmpty()) {
                localData = getListFromJson(strJson);
            }
            // first add/insert into adapter/list
            // suppose result is ordered.
            // should insert into list from the last item...

            PhotoListItem[] items = new PhotoListItem[result.size()];
            for (int position = result.size() - 1; position >= 0; position--) {

                PhotoListItem item = result.get(position);
                mPhotoAdapter.insert(item, 0);
                if (localData != null)
                    localData.add(0, item);
                item.setPosition(position);
                items[position] = item;
            }


            // write to local not append, write
            KecUtilities.writeTabLocalData(getJsonFromObject(localData), subFolder, getActivity());

            new LoadHallPhotoListThumbsTask(getActivity(), mPhotoAdapter, mListView, subFolder).execute(items);

        } catch (Exception e) {
            Log.e(MainActivity.LOGTAG, e.getMessage());
        }

        mSwipyRefreshLayout.setRefreshing(false);
    }

    private void onRefreshCompleteBottom(ArrayList<PhotoListItem> result) {
        if (result == null || subFolder == null)
            return;
        ArrayList<PhotoListItem> localData = null;
        try {
            // read local data, must have some, because of init
            String strJson = KecUtilities.getTabLocalData(subFolder, getActivity());
            if (strJson != null && !strJson.isEmpty()) {
                localData = getListFromJson(strJson);
            }
            // 1. add/insert into adapter/list and set the correct position
            int position = mPhotoAdapter.getCount();
            for (PhotoListItem item : result) {
                item.setPosition(position);
                mPhotoAdapter.add(item);
                if (localData != null)
                    localData.add(item);
                position++;
            }

            if (result.size() > 0) {
                final int currentPosition = mListView.getFirstVisiblePosition();
                mListView.setSelection(currentPosition + 1);
                mListView.post(new Runnable() {
                    @Override
                    public void run() {
                        mListView.smoothScrollToPosition(currentPosition + 1);
                    }
                });
            }

            PhotoListItem[] items = new PhotoListItem[result.size()];
            result.toArray(items);
            // write to local not append, write
            KecUtilities.writeTabLocalData(getJsonFromObject(localData), subFolder, getActivity());
            new LoadHallPhotoListThumbsTask(getActivity(), mPhotoAdapter, mListView, subFolder).execute(items);
        } catch (Exception e) {
            Log.e(MainActivity.LOGTAG, e.getMessage());
        }

        mSwipyRefreshLayout.setRefreshing(false);
    }

    public ArrayList<PhotoListItem> getListFromJson(String strJson) {
        try {
            Gson gson = new Gson();

            Type typeOfObjects = new TypeToken<ArrayList<PhotoListItem>>() {
            }.getType();

            return gson.fromJson(strJson, typeOfObjects);
        } catch (Exception e) {
            Log.e(MainActivity.LOGTAG, e.getMessage());
        }
        return null;
    }

    public String getJsonFromObject(ArrayList<PhotoListItem> items) {
        try {
            Gson gson = new Gson();

            Type typeOfObjects = new TypeToken<ArrayList<PhotoListItem>>() {
            }.getType();

            return gson.toJson(items, typeOfObjects);
        } catch (Exception e) {
            Log.e(MainActivity.LOGTAG, e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public void initList() {
        if (subFolder == null)
            return;
        // read local file
        String strJson = KecUtilities.getTabLocalData(subFolder, getActivity());

        if (strJson != null && !strJson.isEmpty()) {
            ArrayList<PhotoListItem> items = getListFromJson(strJson);
            if (items != null) {
                onRefreshComplete(items);
            }
        } else {
            Refresh(SwipyRefreshLayoutDirection.BOTH);
        }
    }

//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//
//        // for using different menu
//        setHasOptionsMenu(true);
//    }

    // detect when this fragment is visible
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if (isVisibleToUser) {
            Log.d(MainActivity.LOGTAG, "tab_main_hall_photo becomes visible.");
        } else {
            Log.d(MainActivity.LOGTAG, "tab_main_hall_photo becomes invisible.");
        }
    }
}