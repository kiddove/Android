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
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.JsonReader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.kectech.com.stylingactionbar.lib.SwipyRefreshLayout;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Paul on 16/06/2015.
 * use SwipyRefreshLayout to refresh (pull up and pull down)
 * use ListView
 * async download thumbs then show in ListView's items(may be more than one thumb in one item, depend on the json...)
 * tab an item to open an activity to show image(s).
 */
public class PhotoTab extends Fragment {

    private static final int LIST_ITEM_COUNT = 10;
    private static final String LOGTAG = "PhotoTabLog";

    // list
    private ListView mListView;
    // adapter
    private PhotoListViewAdapter mPhotoAdapter;
    // swipyrefreshlayout
    private SwipyRefreshLayout mSwipyRefreshLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v =inflater.inflate(R.layout.photo,container,false);

        mListView = (ListView)v.findViewById(R.id.photo_tab_list);
        mSwipyRefreshLayout = (SwipyRefreshLayout)v.findViewById(R.id.photo_tab_swipy_refresh_layout);
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
                if (intent != null) {
                    intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

                    intent.putExtras(params);
                    try {
                        startActivity(intent);
                    } catch (Exception e) {
                        Log.e(LOGTAG, e.getMessage());
                    }
                }
            }
        });

        Refresh(SwipyRefreshLayoutDirection.BOTH);

        return v;
    }


    // refresh list
    public void Refresh(SwipyRefreshLayoutDirection direction) {
        // todo
        // to be continued...
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
    private class UpdatePhotoListTask extends AsyncTask<String, Void, List<PhotoListItem>> {

        public UpdatePhotoListTask() {
        }

        @Override
        protected List<PhotoListItem> doInBackground(String... params) {


            // todo
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

                return readJsonFromStream(inputStream);

            } catch (MalformedURLException mue) {
                Log.e(LOGTAG, mue.getMessage());
            } catch (Exception e) {
                Log.e(LOGTAG, e.getMessage());
            }

            return null;
        }

        @Override
        protected void onPostExecute(List<PhotoListItem> result) {
            super.onPostExecute(result);

            // todo
            onRefreshComplete(result);
        }
    }

    private class UpdatePhotoListTaskTop extends AsyncTask<String, Void, List<PhotoListItem>> {
        @Override
        protected List<PhotoListItem> doInBackground(String... params) {
            // todo
            // for test
            String strURL = "http://173.236.36.10/cds/generateThumbnail.php?type=top&count=5";

            try {
                URL url = new URL(strURL);

                URLConnection connection = url.openConnection();

                connection.connect();

                InputStream inputStream = new BufferedInputStream(url.openStream(), 10 * 1024);

                return readJsonFromStream(inputStream);

            } catch (MalformedURLException mue) {
                Log.e(LOGTAG, mue.getMessage());
            } catch (Exception e) {
                Log.e(LOGTAG, e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<PhotoListItem> result) {
            super.onPostExecute(result);

            // todo
            onRefreshCompleteTop(result);
            mSwipyRefreshLayout.setRefreshing(false);
        }
    }

    private class UpdatePhotoListTaskBottom extends AsyncTask<String, Void, List<PhotoListItem>> {
        @Override
        protected List<PhotoListItem> doInBackground(String... params) {


            // todo
            // for test
            String strURL = "http://173.236.36.10/cds/generateThumbnail.php?type=bottom&count=5";

            try {
                URL url = new URL(strURL);

                URLConnection connection = url.openConnection();

                connection.connect();

                InputStream inputStream = new BufferedInputStream(url.openStream(), 10 * 1024);
                //int length = connection.getContentLength();

                return readJsonFromStream(inputStream);

            } catch (MalformedURLException mue) {
                Log.e(LOGTAG, mue.getMessage());
            } catch (Exception e) {
                Log.e(LOGTAG, e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<PhotoListItem> result) {
            super.onPostExecute(result);

            // todo
            onRefreshCompleteBottom(result);
            mSwipyRefreshLayout.setRefreshing(false);
        }
    }

    // parse json string(inputstream)
    private List<PhotoListItem> readJsonFromStream(InputStream in) throws IOException {
        JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
        try {
            return readPhotoItemArray(reader);
        } finally {
            reader.close();
        }
    }

    private List<PhotoListItem> readPhotoItemArray(JsonReader reader) throws IOException {
        List<PhotoListItem> listItems = new ArrayList();
        int i = 0;
        reader.beginArray();
        while (reader.hasNext()) {
//            PhotoListItem item = readPhotoItem(reader);
//            // position should be determined when insert/add to adapter/list
//            item.setPosition(i);
//            listItems.add(item);
//            i++;

            listItems.add(readPhotoItem(reader));
        }
        reader.endArray();
        return listItems;
    }

    public PhotoListItem readPhotoItem(JsonReader reader) throws IOException {
        String title = null;
        String desc = null;
        String thumbURL = null;
        String imageURL = null;
        reader.beginObject();
        while (reader.hasNext()) {
            String key = reader.nextName();
            if (key.equals("image")) {
                imageURL = reader.nextString();
            } else if (key.equals("thumb")) {
                thumbURL = reader.nextString();
            } else if (key.equals("title")) {
                title = reader.nextString();
            } else if (key.equals("desc")) {
                desc = reader.nextString();
            } else
                reader.skipValue();
        }
        reader.endObject();
        Log.i(LOGTAG, thumbURL);
        return new PhotoListItem(imageURL, thumbURL, title, desc, null, 0);
    }

    // for the first time when init using this
    private void onRefreshComplete(List<PhotoListItem> result) {
        if (mPhotoAdapter != null) {
            Log.d(LOGTAG, "ListView(mPhotoAdapter) already had data, will be cleared...");
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
            Log.e(LOGTAG, e.getMessage());
        }

        mSwipyRefreshLayout.setRefreshing(false);
    }

    private void onRefreshCompleteTop(List<PhotoListItem> result) {
        try {
            // first add/insert into adapter/list
            // suppose result is ordered.
            // should insert into list from the last item...

            PhotoListItem[] items = new PhotoListItem[result.size()];
            for (int position = result.size() - 1; position >= 0; position--) {

                PhotoListItem item = result.get(position);
                mPhotoAdapter.insert(item, 0);
                item.setPosition(position);
                items[position] = item;
            }


            new LoadPhotoListThumbsTask(getActivity(), mPhotoAdapter, mListView).execute(items);

        } catch (Exception e) {
            Log.e(LOGTAG, e.getMessage());
        }

        mSwipyRefreshLayout.setRefreshing(false);
    }

    private void onRefreshCompleteBottom(List<PhotoListItem> result) {
        try {
            // 1. add/insert into adapter/list and set the correct position
            int position = mPhotoAdapter.getCount();
            for (PhotoListItem item : result) {
                item.setPosition(position);
                mPhotoAdapter.add(item);
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

            new LoadPhotoListThumbsTask(getActivity(), mPhotoAdapter, mListView).execute(items);
        } catch (Exception e) {
            Log.e(LOGTAG, e.getMessage());
        }

        mSwipyRefreshLayout.setRefreshing(false);
    }
}