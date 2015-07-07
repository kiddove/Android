package com.kectech.android.kectechapp.tabs;

import android.app.Fragment;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.SparseBooleanArray;
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
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.kectech.android.kectechapp.R;
import com.kectech.android.kectechapp.activity.HallOfMainActivity;
import com.kectech.android.kectechapp.activity.MainActivity;
import com.kectech.android.kectechapp.adapter.HallListViewAdapter;
import com.kectech.android.kectechapp.listitem.Tab_Main_Hall_ListItem;
import com.kectech.android.kectechapp.thirdparty.*;

import java.util.ArrayList;

/**
 * Created by Paul on 16/06/2015.
 * video tab implements by a ListView
 * use a SwipyRefreshLayout to fulfil pull down and pull up refresh
 * tab an item to open an activity to tab_main_show video page in a WebView
 */
public class Tab_Main_Hall extends Fragment {

    private static final int LIST_ITEM_COUNT = 20;

    private static final String SCAN_TAG = "SCAN";

    private ListView mListView;

    private HallListViewAdapter mVideoAdapter;

    private SwipyRefreshLayout mSwipyRefreshLayout;

    private ActionMode mMode;

    private int num = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.tab_main_hall, container, false);

        mListView = (ListView) v.findViewById(R.id.video_tab_list);
//        mListView.setItemsCanFocus(true);
        mSwipyRefreshLayout = (SwipyRefreshLayout) v.findViewById(R.id.video_tab_swipy_refresh_layout);
        mSwipyRefreshLayout.setDirection(SwipyRefreshLayoutDirection.BOTH);

        mSwipyRefreshLayout.setOnRefreshListener(new SwipyRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh(SwipyRefreshLayoutDirection direction) {
                //if (in edit mode or others do not refresh or just set listener to null or set direction to NONE?) NONE works OK.
                DummyRefresh(direction);
            }
        });

//        // click listener
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                if (mVideoAdapter.showCheckBox) {
                    boolean checked = mVideoAdapter.isChecked(position);
                    onItemChecked(mMode, position, !checked);

                } else {

                    Tab_Main_Hall_ListItem tabMainHallListItem = mVideoAdapter.getItem(position);
                    // get another activity to run  activity_main_hall
                    Intent intent = new Intent(getActivity(), HallOfMainActivity.class);

                    intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    //String url = "https://www.youtube.com/embed/quNeZeiO5pc";
                    String url = "http://192.168.9.40/demo/test.html";

                    if (tabMainHallListItem.getTitle().equals(SCAN_TAG)) {
                        url = tabMainHallListItem.getDesc();
                    }
                    intent.putExtra(MainActivity.EXTRA_MESSAGE_URL, url);
                    try {
                        startActivity(intent);
                    } catch (Exception e) {
                        Log.e(MainActivity.LOGTAG, e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
        });

        mSwipyRefreshLayout.setColorScheme(
                R.color.swipe_color_1, R.color.swipe_color_3,
                R.color.swipe_color_5);

        try {
            //ArrayList<Tab_Main_Hall_ListItem> listItems = new ArrayList<>(LIST_ITEM_COUNT);
            ArrayList<Tab_Main_Hall_ListItem> listItems = new ArrayList<>();
            for (int i = 0; i < LIST_ITEM_COUNT; i++) {
                Tab_Main_Hall_ListItem item = new Tab_Main_Hall_ListItem(R.drawable.ic_launcher, "title " + i, "desc " + i);
                listItems.add(item);
            }
            mVideoAdapter = new HallListViewAdapter(getActivity(), R.layout.tab_main_hall_list_item, listItems);
            mListView.setAdapter(mVideoAdapter);
        } catch (Exception e) {
            Log.e(MainActivity.LOGTAG, e.getMessage());
        }

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
                return prepareActionMode(mode, menu);
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                return actionItemClicked(mode, item);
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                destroyActionMode(mode);
            }
        });
        return v;
    }

    public ArrayList<Tab_Main_Hall_ListItem> AddContent(@Nullable SwipyRefreshLayoutDirection direction) {
        try {
            ArrayList<Tab_Main_Hall_ListItem> listItems = new ArrayList<>();
            for (int i = 0; i < LIST_ITEM_COUNT; i++) {
                if (direction == null) {
                    Tab_Main_Hall_ListItem item = new Tab_Main_Hall_ListItem(R.drawable.ic_launcher, "title", "desc");
                    listItems.add(item);
                } else if (direction == SwipyRefreshLayoutDirection.TOP) {
                    Tab_Main_Hall_ListItem item = new Tab_Main_Hall_ListItem(R.drawable.ic_launcher, "refresh", "top" + i);
                    listItems.add(item);
                } else if (direction == SwipyRefreshLayoutDirection.BOTTOM) {
                    Tab_Main_Hall_ListItem item = new Tab_Main_Hall_ListItem(R.drawable.ic_launcher, "refresh", "bottom" + i);
                    listItems.add(item);
                }
            }
            return listItems;
        } catch (Exception e) {
            Log.e(MainActivity.LOGTAG, e.getMessage());
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
    private class DummyBackgroundTask extends AsyncTask<String, Void, ArrayList<Tab_Main_Hall_ListItem>> {

        static final int TASK_DURATION = 3 * 1000; // 3 seconds

        private SwipyRefreshLayoutDirection direction;

        public DummyBackgroundTask(SwipyRefreshLayoutDirection direction) {
            this.direction = direction;
        }

        @Override
        protected ArrayList<Tab_Main_Hall_ListItem> doInBackground(String... params) {
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
        protected void onPostExecute(ArrayList<Tab_Main_Hall_ListItem> result) {
            super.onPostExecute(result);

            // Tell the Fragment that the refresh has completed
            if (direction == SwipyRefreshLayoutDirection.TOP)
                onRefreshCompleteTop(result);
            else if (direction == SwipyRefreshLayoutDirection.BOTTOM)
                onRefreshCompleteBottom(result);
        }

    }

    private void onRefreshCompleteTop(ArrayList<Tab_Main_Hall_ListItem> result) {

        // Remove all items from the ListAdapter, and then replace them with the new items
        //mVideoAdapter.clear();
        for (Tab_Main_Hall_ListItem item : result) {
            //mVideoAdapter.add(item);
            mVideoAdapter.insert(item, 0);
        }

        // Stop the refreshing indicator
        mSwipyRefreshLayout.setRefreshing(false);
    }

    private void onRefreshCompleteBottom(ArrayList<Tab_Main_Hall_ListItem> result) {

        for (Tab_Main_Hall_ListItem item : result) {
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

        if (!isVisibleToUser) {
            //hide cab
            if (mMode != null)
                mMode.finish();
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
        inflater.inflate(R.menu.menu_hall_tab, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    // addan item to list from scaned result
    public void AddItemToList(String url) {

        Tab_Main_Hall_ListItem tabMainHallListItem = new Tab_Main_Hall_ListItem(R.drawable.ic_launcher, SCAN_TAG, url);
        mVideoAdapter.insert(tabMainHallListItem, 0);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // if want to handled in fragment
        // must return false in activity
        int id = item.getItemId();

        switch (id) {
            case R.id.menu_video_tab_item_add:
                IntentIntegrator integrator = new IntentIntegrator(this);
                integrator.initiateScan();
                return true;
            case R.id.menu_video_tab_item_edit:
                mMode = getActivity().startActionMode(new ModeCallback());
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // deal with scan result
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        if (scanResult != null) {
            String scanContent = scanResult.getContents();

            if (scanContent != null) {
                Log.d(MainActivity.LOGTAG, "QR Scan Content: " + scanContent);

                if (!scanContent.isEmpty()) {
//            // insert into ....
                    try {
                        AddItemToList(scanContent);
                    } catch (Exception e) {
                        Log.e(MainActivity.LOGTAG, e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        String[] option = {"YES", "NO"};
        if (v.getId() == R.id.video_tab_list) {
            //AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
            menu.setHeaderTitle("Delete");
            for (int i = 0; i < option.length; i++) {
                menu.add(Menu.NONE, i, i, option[i]);
            }
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        //AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
        int menuItemIndex = item.getItemId();
        String[] option = {"YES", "NO"};

        Toast.makeText(getActivity(), option[menuItemIndex] + " selected.", Toast.LENGTH_SHORT).show();
        return true;
    }

    private final class ModeCallback implements ActionMode.Callback {

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            initActionMode(mode, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            boolean bRet = prepareActionMode(mode, menu);
            mVideoAdapter.notifyDataSetChanged();
            return bRet;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            destroyActionMode(mode);
            mVideoAdapter.notifyDataSetChanged();
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            return actionItemClicked(mode, item);
        }
    }

    ;

    private View getListItemViewByPosition(int position) {

//        int i1 = mListView.getFirstVisiblePosition();
//        int i2 = mListView.getHeaderViewsCount();

        final int firstPosition = mListView.getFirstVisiblePosition() - mListView.getHeaderViewsCount();
        final int lastPosition = mListView.getChildCount() - 1 + firstPosition;

        if (position < firstPosition || position > lastPosition) {
            return mVideoAdapter.getView(position, null, mListView);
        } else {
            final int childIndex = position - firstPosition;
            return mListView.getChildAt(childIndex);
        }
    }

    private void onItemChecked(ActionMode mode, int position, boolean checked) {
        if (checked) {
            num++;
            mVideoAdapter.setSelection(position, checked);
            // set check
        } else {
            num--;
            mVideoAdapter.removeSelect(position);
        }

//                View itemView = mListView.getChildAt(position);
        View itemView = getListItemViewByPosition(position);
        if (itemView != null) {
            CheckBox checkBox = (CheckBox) itemView.findViewById(R.id.tab_main_hall_list_item_check);
            checkBox.setChecked(checked);
        }

        mode.setTitle(num + " selected.");
    }

    private void initActionMode(ActionMode mode, Menu menu) {
        num = 0;
        MenuInflater inflater = mode.getMenuInflater();
        inflater.inflate(R.menu.menu_hall_tab_cab, menu);
    }

    private boolean prepareActionMode(ActionMode mode, Menu menu) {
        // Here, you can checked selected items to adapt available actions
        // set NONE
        mSwipyRefreshLayout.setDirection(SwipyRefreshLayoutDirection.NONE);
        mVideoAdapter.showCheckBox = true;
        return false;
    }

    private void destroyActionMode(ActionMode mode) {
        // set NONE to BOTH
        mSwipyRefreshLayout.setDirection(SwipyRefreshLayoutDirection.BOTH);
        mVideoAdapter.showCheckBox = false;
        mVideoAdapter.clear();
        num = 0;
    }

    private boolean actionItemClicked(ActionMode mode, MenuItem item) {
        switch (item.getItemId()) {
            case R.id.hall_tab_action_delete:
                Toast.makeText(getActivity(), num + " items should be deleted.", Toast.LENGTH_SHORT).show();
                return true;
            default:
                return false;
        }
    }
}
