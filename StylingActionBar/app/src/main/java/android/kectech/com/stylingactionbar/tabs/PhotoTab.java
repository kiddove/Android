package android.kectech.com.stylingactionbar.tabs;

import android.app.Fragment;
import android.content.Intent;
import android.kectech.com.stylingactionbar.MainActivity;
import android.kectech.com.stylingactionbar.PhotoActivity;
import android.kectech.com.stylingactionbar.R;
import android.kectech.com.stylingactionbar.adapter.PhotoListViewAdapter;
import android.kectech.com.stylingactionbar.data.LoadPhotoListThumbsTask;
import android.kectech.com.stylingactionbar.lib.SwipyRefreshLayoutDirection;
import android.kectech.com.stylingactionbar.listitem.PhotoListItem;
import android.kectech.com.stylingactionbar.util.KecUtilities;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.kectech.com.stylingactionbar.lib.SwipyRefreshLayout;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;


/**
 * Created by Paul on 16/06/2015.
 * use SwipyRefreshLayout to refresh (pull up and pull down)
 * use ListView
 * async download thumbs then show in ListView's items(may be more than one thumb in one item, depend on the json...)
 * tab an item to open an activity to show image(s).
 */
public class PhotoTab extends Fragment {

    private static final int LIST_ITEM_COUNT = 10;

    // list
    private ListView mListView;
    // adapter
    private PhotoListViewAdapter mPhotoAdapter;
    // swipyrefreshlayout
    private SwipyRefreshLayout mSwipyRefreshLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.photo, container, false);

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
                Intent intent = new Intent(getActivity(), PhotoActivity.class);

                // create parameters
                Bundle params = new Bundle();
                params.putString(MainActivity.PHOTO_TAB_THUMB_URL_KEY, photoListItem.getThumbURL());
                params.putString(MainActivity.PHOTO_TAB_IMAGE_URL_KEY, photoListItem.getImageURL());

                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

                intent.putExtras(params);
                try {
                    startActivity(intent);
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
            String strURL = "http://173.236.36.10/cds/generateThumbnail.php";

            try {
                URL url = new URL(strURL);

                URLConnection connection = url.openConnection();

                connection.connect();

                InputStream inputStream = new BufferedInputStream(url.openStream(), 10 * 1024);
                //int length = connection.getContentLength();
                return readStringFromStream(inputStream);
            } catch (Exception e) {
                Log.e(MainActivity.LOGTAG, e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            ArrayList<PhotoListItem> items = getListFromJson(result);

            if (result != null) {
                KecUtilities.writeTabLocalData(result, MainActivity.PHOTO_SUB_FOLDER, getActivity());
                onRefreshComplete(items);
            }
            mSwipyRefreshLayout.setRefreshing(false);
        }
    }

    private class UpdatePhotoListTaskTop extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            // for test
            String strURL = "http://173.236.36.10/cds/generateThumbnail.php?type=top&count=5";

            try {
                URL url = new URL(strURL);

                URLConnection connection = url.openConnection();

                connection.connect();

                InputStream inputStream = new BufferedInputStream(url.openStream(), 10 * 1024);

                return readStringFromStream(inputStream);

            } catch (Exception e) {
                Log.e(MainActivity.LOGTAG, e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            //Log.d(MainActivity.LOGTAG, "***   onPostExecute: " + result + "   ***");
            ArrayList<PhotoListItem> items = getListFromJson(result);

            onRefreshCompleteTop(items);
            mSwipyRefreshLayout.setRefreshing(false);
        }
    }

    private class UpdatePhotoListTaskBottom extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {

            // for test
            String strURL = "http://173.236.36.10/cds/generateThumbnail.php?type=bottom&count=5";

            try {
                URL url = new URL(strURL);

                URLConnection connection = url.openConnection();

                connection.connect();

                InputStream inputStream = new BufferedInputStream(url.openStream(), 10 * 1024);
                //int length = connection.getContentLength();

                return readStringFromStream(inputStream);

            } catch (Exception e) {
                Log.e(MainActivity.LOGTAG, e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            ArrayList<PhotoListItem> items = getListFromJson(result);

            // todo
            // write to local file
            onRefreshCompleteBottom(items);
            mSwipyRefreshLayout.setRefreshing(false);
        }
    }

    // for the first time when init using this
    private void onRefreshComplete(ArrayList<PhotoListItem> result) {
        if (result == null)
            return;
        if (mPhotoAdapter != null) {
            Log.d(MainActivity.LOGTAG, "ListView(mPhotoAdapter) already had data, will be cleared...");
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

            new LoadPhotoListThumbsTask(getActivity(), mPhotoAdapter, mListView).execute(items);

        } catch (Exception e) {
            Log.e(MainActivity.LOGTAG, e.getMessage());
        }

        mSwipyRefreshLayout.setRefreshing(false);
    }

    private void onRefreshCompleteTop(ArrayList<PhotoListItem> result) {
        if (result == null)
            return;
        ArrayList<PhotoListItem> localData = null;
        try {
            // read local data, must have some, because of init
            String strJson = KecUtilities.getTabLocalData(MainActivity.PHOTO_SUB_FOLDER, getActivity());

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
            KecUtilities.writeTabLocalData(getJsonFromObject(localData), MainActivity.PHOTO_SUB_FOLDER, getActivity());

            new LoadPhotoListThumbsTask(getActivity(), mPhotoAdapter, mListView).execute(items);

        } catch (Exception e) {
            Log.e(MainActivity.LOGTAG, e.getMessage());
        }

        mSwipyRefreshLayout.setRefreshing(false);
    }

    private void onRefreshCompleteBottom(ArrayList<PhotoListItem> result) {
        if (result == null)
            return;
        ArrayList<PhotoListItem> localData = null;
        try {
            // read local data, must have some, because of init
            String strJson = KecUtilities.getTabLocalData(MainActivity.PHOTO_SUB_FOLDER, getActivity());
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
            KecUtilities.writeTabLocalData(getJsonFromObject(localData), MainActivity.PHOTO_SUB_FOLDER, getActivity());
            new LoadPhotoListThumbsTask(getActivity(), mPhotoAdapter, mListView).execute(items);
        } catch (Exception e) {
            Log.e(MainActivity.LOGTAG, e.getMessage());
        }

        mSwipyRefreshLayout.setRefreshing(false);
    }

    public String readStringFromStream(InputStream inputStream) {
        String strJson = null;
        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder total = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                total.append(line);
            }
            strJson = total.toString();
        } catch (Exception e) {
            Log.e(MainActivity.LOGTAG, "readStringFromStream occurs exception: " + e.getMessage());
        }
        return strJson;
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
        // read local file
        String strJson = KecUtilities.getTabLocalData(MainActivity.PHOTO_SUB_FOLDER, getActivity());

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
}