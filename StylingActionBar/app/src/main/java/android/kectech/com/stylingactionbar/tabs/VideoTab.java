package android.kectech.com.stylingactionbar.tabs;

import android.content.Intent;
import android.kectech.com.stylingactionbar.MainActivity;
import android.kectech.com.stylingactionbar.R;
import android.kectech.com.stylingactionbar.VideoActivity;
import android.kectech.com.stylingactionbar.listitem.VideoListItem;
import android.kectech.com.stylingactionbar.adapter.VideoListViewAdapter;
import android.kectech.com.stylingactionbar.lib.SwipyRefreshLayout;
import android.kectech.com.stylingactionbar.lib.SwipyRefreshLayoutDirection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by Paul on 16/06/2015.
 * video tab implements by a ListView
 * use a SwipyRefreshLayout to fullfil pull down and pull up refresh
 * tab an item to open an activity to show video page in a WebView
 */
public class VideoTab extends Fragment {
    //private ArrayAdapter<String> mListAdapter;
    private static final int LIST_ITEM_COUNT = 20;

    private static final String SCAN_TAG = "SCAN";

    private ListView mListView;

    private VideoListViewAdapter mVideoAdapter;

    private SwipyRefreshLayout mSwipyRefreshLayout;
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.video, container, false);

        mListView = (ListView) v.findViewById(R.id.video_tab_list);
        mSwipyRefreshLayout = (SwipyRefreshLayout)v.findViewById(R.id.video_tab_swipy_refresh_layout);
        mSwipyRefreshLayout.setDirection(SwipyRefreshLayoutDirection.BOTH);

        mSwipyRefreshLayout.setOnRefreshListener(new SwipyRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh(SwipyRefreshLayoutDirection direction) {
                DummyRefresh(direction);
            }
        });

//        // click listener
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Toast t = Toast.makeText(getActivity(), "select " + position,
                        Toast.LENGTH_SHORT);
                t.show();

                VideoListItem videoListItem = mVideoAdapter.getItem(position);
                // get another activity to run
                Intent intent = new Intent(getActivity(), VideoActivity.class);

                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                //String url = "https://www.youtube.com/embed/quNeZeiO5pc";
                String url = "http://192.168.9.40/demo/test.html";

                if (videoListItem.getTitle().equals(SCAN_TAG)) {
                    url = videoListItem.getDesc();
                }
                intent.putExtra(MainActivity.EXTRA_MESSAGE_URL, url);
                startActivity(intent);
            }
        });

        mSwipyRefreshLayout.setColorScheme(
                R.color.swipe_color_1, R.color.swipe_color_3,
                R.color.swipe_color_5);

        try {
            ArrayList<VideoListItem> listItems = new ArrayList<>(LIST_ITEM_COUNT);
            for (int i = 0; i < LIST_ITEM_COUNT; i++) {
                VideoListItem item = new VideoListItem(R.drawable.ic_launcher, "title", "desc");
                listItems.add(item);
            }
            mVideoAdapter = new VideoListViewAdapter(getActivity(), R.layout.video_list_item, listItems);
            mListView.setAdapter(mVideoAdapter);
        }
        catch (Exception e) {
            Log.d(MainActivity.LOGTAG, e.getMessage());
        }
        return v;
    }

    public ArrayList<VideoListItem> AddContent(@Nullable SwipyRefreshLayoutDirection direction) {
        try {
            ArrayList<VideoListItem> listItems = new ArrayList<>(LIST_ITEM_COUNT);
            for (int i = 0; i < LIST_ITEM_COUNT; i++) {
                if (direction == null) {
                    VideoListItem item = new VideoListItem(R.drawable.ic_launcher, "title", "desc");
                    listItems.add(item);
                } else if (direction == SwipyRefreshLayoutDirection.TOP) {
                    VideoListItem item = new VideoListItem(R.drawable.ic_launcher, "refresh", "top" + i);
                    listItems.add(item);
                } else if (direction == SwipyRefreshLayoutDirection.BOTTOM) {
                    VideoListItem item = new VideoListItem(R.drawable.ic_launcher, "refresh", "bottom" + i);
                    listItems.add(item);
                }
            }
            return listItems;
        }
        catch (Exception e) {
            Log.d(MainActivity.LOGTAG, e.getMessage());
            return null;
        }
    }

    public void DummyRefresh(SwipyRefreshLayoutDirection direction) {
        new DummyBackgroundTask(direction).execute("whatever");
    }
    /**
     * Dummy {@link AsyncTask} which simulates a long running task to fetch new cheeses.
     * the first parameter is in execute(param); can be a view holder... for async use to load the image in list view
     */
    private class DummyBackgroundTask extends AsyncTask<String, Void, ArrayList<VideoListItem>> {

        static final int TASK_DURATION = 3 * 1000; // 3 seconds

        private SwipyRefreshLayoutDirection direction;
        public DummyBackgroundTask(SwipyRefreshLayoutDirection direction) {
            this.direction = direction;
        }
        @Override
        protected ArrayList<VideoListItem> doInBackground(String... params) {
            // Sleep for a small amount of time to simulate a background-task
            try {
                Thread.sleep(TASK_DURATION);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // Return a new random list of cheeses
            //return Cheeses.randomList(LIST_ITEM_COUNT);
            return AddContent(this.direction);
        }

        @Override
        protected void onPostExecute(ArrayList<VideoListItem> result) {
            super.onPostExecute(result);

            // Tell the Fragment that the refresh has completed
            if (direction == SwipyRefreshLayoutDirection.TOP)
                onRefreshCompleteTop(result);
            else if (direction == SwipyRefreshLayoutDirection.BOTTOM)
                onRefreshCompleteBottom(result);
        }

    }
    private void onRefreshCompleteTop(ArrayList<VideoListItem> result) {

        // Remove all items from the ListAdapter, and then replace them with the new items
        //mVideoAdapter.clear();
        for (VideoListItem item : result) {
            //mVideoAdapter.add(item);
            mVideoAdapter.insert(item, 0);
        }

        // Stop the refreshing indicator
        mSwipyRefreshLayout.setRefreshing(false);
    }

    private void onRefreshCompleteBottom(ArrayList<VideoListItem> result) {

        for (VideoListItem item : result) {
            mVideoAdapter.add(item);
        }

        // Stop the refreshing indicator
        mSwipyRefreshLayout.setRefreshing(false);

        // scroll to proper position
        if (result.size() > 0) {

            final int position = mListView.getFirstVisiblePosition();
            mListView.setSelection(position + 1);
            mListView.post(new Runnable() {
                @Override
                public void run() {
                    mListView.smoothScrollToPosition(position + 1);
                }
            });
        }

    }

    // detect when this fragment is visible
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

//        if (isVisibleToUser) {
//            Log.i(MainActivity.LOGTAG, "Video Tab visible need refresh data..");
//            // todo
//            // get data...
//        }
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

    // addan item to list from scaned result
    public void AddItemToList(String url) {

        VideoListItem videoListItem = new VideoListItem(R.drawable.ic_launcher, SCAN_TAG, url);
        mVideoAdapter.insert(videoListItem, 0);
    }
}
