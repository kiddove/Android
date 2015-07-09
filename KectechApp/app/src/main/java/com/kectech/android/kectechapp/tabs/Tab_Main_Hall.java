package com.kectech.android.kectechapp.tabs;

import android.app.Fragment;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.ActionMode;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.kectech.android.kectechapp.R;
import com.kectech.android.kectechapp.activity.HallOfMainActivity;
import com.kectech.android.kectechapp.activity.MainActivity;
import com.kectech.android.kectechapp.adapter.HallListViewAdapter;
import com.kectech.android.kectechapp.data.LoadHallListThumbsTask;
import com.kectech.android.kectechapp.listitem.Tab_Main_Hall_ListItem;
import com.kectech.android.kectechapp.thirdparty.*;
import com.kectech.android.kectechapp.util.KecUtilities;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Paul on 16/06/2015.
 * video tab implements by a ListView
 * use a SwipyRefreshLayout to fulfil pull down and pull up refresh
 * tab an item to open an activity to tab_main_show video page in a WebView
 */
public class Tab_Main_Hall extends Fragment {

    private static final String SCAN_TAG = "SCAN";

    private ListView mListView;

    private HallListViewAdapter mAdapter;

    private SwipyRefreshLayout mSwipyRefreshLayout;

    private ActionMode mMode;

    private int num = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.tab_main_hall, container, false);

        mListView = (ListView) v.findViewById(R.id.hall_tab_list);
//        mListView.setItemsCanFocus(true);
        mSwipyRefreshLayout = (SwipyRefreshLayout) v.findViewById(R.id.hall_tab_swipy_refresh_layout);
        mSwipyRefreshLayout.setDirection(SwipyRefreshLayoutDirection.BOTH);

        mSwipyRefreshLayout.setOnRefreshListener(new SwipyRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh(SwipyRefreshLayoutDirection direction) {
                //if (in edit mode or others do not refresh or just set listener to null or set direction to NONE?) NONE works OK.
                Refresh(direction);
            }
        });

//        // click listener
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                if (mAdapter.showCheckBox) {
                    boolean checked = mAdapter.isChecked(position);
                    onItemChecked(mMode, position, !checked);

                } else {

                    Tab_Main_Hall_ListItem tabMainHallListItem = mAdapter.getItem(position);
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
                        //getActivity().overridePendingTransition(0, 0);
                        getActivity().overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
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

//    public ArrayList<Tab_Main_Hall_ListItem> AddContent(@Nullable SwipyRefreshLayoutDirection direction) {
//        try {
//            ArrayList<Tab_Main_Hall_ListItem> listItems = new ArrayList<>();
//            for (int i = 0; i < LIST_ITEM_COUNT; i++) {
//                if (direction == null) {
//                    Tab_Main_Hall_ListItem item = new Tab_Main_Hall_ListItem(R.drawable.ic_launcher, "title", "desc");
//                    listItems.add(item);
//                } else if (direction == SwipyRefreshLayoutDirection.TOP) {
//                    Tab_Main_Hall_ListItem item = new Tab_Main_Hall_ListItem(R.drawable.ic_launcher, "refresh", "top" + i);
//                    listItems.add(item);
//                } else if (direction == SwipyRefreshLayoutDirection.BOTTOM) {
//                    Tab_Main_Hall_ListItem item = new Tab_Main_Hall_ListItem(R.drawable.ic_launcher, "refresh", "bottom" + i);
//                    listItems.add(item);
//                }
//            }
//            return listItems;
//        } catch (Exception e) {
//            Log.e(MainActivity.LOGTAG, e.getMessage());
//            return null;
//        }
//    }
//
//    public void DummyRefresh(SwipyRefreshLayoutDirection direction) {
//        new DummyBackgroundTask(direction).execute("whatever");
//    }
//
//    /**
//     * Dummy {@link AsyncTask} which simulates a long running task to fetch new cheeses.
//     * the first parameter is in execute(param); can be a view holder... for async use to load the image in list view
//     */
//    private class DummyBackgroundTask extends AsyncTask<String, Void, ArrayList<Tab_Main_Hall_ListItem>> {
//
//        static final int TASK_DURATION = 3 * 1000; // 3 seconds
//
//        private SwipyRefreshLayoutDirection direction;
//
//        public DummyBackgroundTask(SwipyRefreshLayoutDirection direction) {
//            this.direction = direction;
//        }
//
//        @Override
//        protected ArrayList<Tab_Main_Hall_ListItem> doInBackground(String... params) {
//            // Sleep for a small amount of time to simulate a background-task
//            try {
//                Thread.sleep(TASK_DURATION);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//
//            // Return a new random list of cheeses
//            //return Cheeses.randomList(LIST_ITEM_COUNT);
//            return AddContent(this.direction);
//        }
//
//        @Override
//        protected void onPostExecute(ArrayList<Tab_Main_Hall_ListItem> result) {
//            super.onPostExecute(result);
//
//            // Tell the Fragment that the refresh has completed
//            if (direction == SwipyRefreshLayoutDirection.TOP)
//                onRefreshCompleteTop(result);
//            else if (direction == SwipyRefreshLayoutDirection.BOTTOM)
//                onRefreshCompleteBottom(result);
//        }
//
//    }
//
//    private void onRefreshCompleteTop(ArrayList<Tab_Main_Hall_ListItem> result) {
//
//        // Remove all items from the ListAdapter, and then replace them with the new items
//        //mVideoAdapter.clear();
//        for (Tab_Main_Hall_ListItem item : result) {
//            //mVideoAdapter.add(item);
//            mVideoAdapter.insert(item, 0);
//        }
//
//        // Stop the refreshing indicator
//        mSwipyRefreshLayout.setRefreshing(false);
//    }
//
//    private void onRefreshCompleteBottom(ArrayList<Tab_Main_Hall_ListItem> result) {
//
//        for (Tab_Main_Hall_ListItem item : result) {
//            mVideoAdapter.add(item);
//        }
//
//        // Stop the refreshing indicator
//        mSwipyRefreshLayout.setRefreshing(false);
//
//        // scroll to proper position
//        if (result.size() > 0) {
//
//            final int position = mListView.getFirstVisiblePosition();
//            mListView.setSelection(position + 1);
//            mListView.post(new Runnable() {
//                @Override
//                public void run() {
//                    mListView.smoothScrollToPosition(position + 1);
//                }
//            });
//        }
//
//    }

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
            if (mMode != null) {
                mMode.finish();
                mMode = null;
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
        inflater.inflate(R.menu.menu_hall_tab, menu);
        MenuItem menuItem = menu.findItem(R.id.menu_hall_tab_item_add);
        if (menuItem != null) {
            inflater.inflate(R.menu.hall_tab_add_submenu, menuItem.getSubMenu());
        }
        //super.onCreateOptionsMenu(menu, inflater);
    }

    // add an item to list from scanned result
    public void AddItemToList(String url) {


//        // todo
//        Tab_Main_Hall_ListItem tabMainHallListItem = new Tab_Main_Hall_ListItem(R.drawable.ic_launcher, SCAN_TAG, url);
//        mAdapter.insert(tabMainHallListItem, 0);
        Log.d(MainActivity.LOGTAG, url);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // if want to handled in fragment
        // must return false in activity
        int id = item.getItemId();

        switch (id) {
            case R.id.menu_hall_tab_item_edit:
                mMode = getActivity().startActionMode(new ModeCallback());
                return true;
            case R.id.hall_tab_add_sub_scan:
                IntentIntegrator integrator = new IntentIntegrator(this);
                integrator.initiateScan();
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
                        // todo
                        //AddItemToList(scanContent);
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
        if (v.getId() == R.id.hall_tab_list) {
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
            boolean bRet = prepareActionMode();
            mAdapter.notifyDataSetChanged();
            return bRet;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            destroyActionMode();
            mAdapter.notifyDataSetChanged();
            mMode = null;
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
            mAdapter.setSelection(position, checked);
            // set check
        } else {
            num--;
            mAdapter.removeSelect(position);
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

    private boolean prepareActionMode() {
        // Here, you can checked selected items to adapt available actions
        // set NONE
        mSwipyRefreshLayout.setDirection(SwipyRefreshLayoutDirection.NONE);
        mAdapter.showCheckBox = true;
        return false;
    }

    private void destroyActionMode() {
        // set NONE to BOTH
        mSwipyRefreshLayout.setDirection(SwipyRefreshLayoutDirection.BOTH);
        mAdapter.showCheckBox = false;
        mAdapter.clear();
        num = 0;
    }

    private boolean actionItemClicked(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.hall_tab_action_delete:
                // do in another thread?
                //Toast.makeText(getActivity(), num + " items should be deleted.", Toast.LENGTH_SHORT).show();
                deleteFromList();
                return true;
            default:
                return false;
        }
    }

    private void deleteFromList() {
        if (mAdapter.isSelectionEmpty()) {
            return;
        }

        // todo
        // first start animation, when animation ends, delete and finish

        AnimationSet as = new AnimationSet(true);
        as.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {

                mMode.finish();
                mMode = null;
                // do this in another thread
                Thread thread = new Thread() {
                    @Override
                    public void run() {
                        ArrayList<Tab_Main_Hall_ListItem> localData = new ArrayList<Tab_Main_Hall_ListItem>();
                        for (int i = 0; i < mAdapter.getCount(); i++)
                        // remember to clear bitmap... other wise the json will be so huge...
                        // if set null directly, seems image will be null in the list
                        // so... new item..
                        {
                            Tab_Main_Hall_ListItem item = new Tab_Main_Hall_ListItem(mAdapter.getItem(i));
                            localData.add(item);
                        }
                        // write to local
                        KecUtilities.writeTabLocalData(Tab_Main_Hall.getJsonFromObject(localData), HallOfMainActivity.subFolder, getActivity());

                        localData.clear();
                    }
                };
                thread.start();
                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        //for (int i = 0; i < mAdapter.getCount(); i++) {
        for (int i = mAdapter.getCount() - 1; i >= 0; i--) {
            if (!mAdapter.isChecked(i))
                continue;
            // after remove, objects will change
            // so the next one will be wrong position
            // thus delete from the last
//
            // start animation
            // need use view holder, and must wait until pre one done....
            Animation animation = mAdapter.deleteCell(getListItemViewByPosition(i), i);
            // this is not working, order is not correct
            //animation.setStartOffset(j * HallListViewAdapter.ANIMATION_DURATION);
            as.addAnimation(animation);

//            mAdapter.remove(mAdapter.getItem(i));
//            mAdapter.notifyDataSetChanged();

        }

        List<Animation> la = as.getAnimations();
        for (int i = 0; i < la.size(); i++) {
            la.get(i).setStartOffset(i * HallListViewAdapter.ANIMATION_DURATION);
        }
        mListView.startAnimation(as);

    }

    // for download thumbs
    // may need to write 3 task
    private class UpdateThumbListTask extends AsyncTask<String, Void, String> {

        public UpdateThumbListTask() {
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
                return KecUtilities.readStringFromStream(inputStream);
            } catch (Exception e) {
                Log.e(MainActivity.LOGTAG, e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            ArrayList<Tab_Main_Hall_ListItem> items = getListFromJson(result);

            if (result != null) {
                KecUtilities.writeTabLocalData(result, HallOfMainActivity.subFolder, getActivity());
                onRefreshComplete(items);
            }
            mSwipyRefreshLayout.setRefreshing(false);
        }
    }

    private class UpdateThumbListTaskTop extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            // for test
            String strURL = "http://173.236.36.10/cds/generateThumbnail.php?type=top&count=5";

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

            ArrayList<Tab_Main_Hall_ListItem> items = getListFromJson(result);

            onRefreshCompleteTop(items);
            mSwipyRefreshLayout.setRefreshing(false);
        }
    }

    private class UpdateThumbListTaskBottom extends AsyncTask<String, Void, String> {
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

                return KecUtilities.readStringFromStream(inputStream);

            } catch (Exception e) {
                Log.e(MainActivity.LOGTAG, e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            ArrayList<Tab_Main_Hall_ListItem> items = getListFromJson(result);

            // todo
            // write to local file
            onRefreshCompleteBottom(items);
            mSwipyRefreshLayout.setRefreshing(false);
        }
    }

    // refresh list
    public void Refresh(SwipyRefreshLayoutDirection direction) {

        // actually bottom and init can use same interface??
        if (direction == SwipyRefreshLayoutDirection.TOP) {
            new UpdateThumbListTaskTop().execute("todo");
        } else if (direction == SwipyRefreshLayoutDirection.BOTTOM) {
            new UpdateThumbListTaskBottom().execute("todo");
        } else
            // use as init
            new UpdateThumbListTask().execute("todo");
    }

    public void initList() {
        // read local file
        String strJson = KecUtilities.getTabLocalData(HallOfMainActivity.subFolder, getActivity());

        if (strJson != null && !strJson.isEmpty()) {
            ArrayList<Tab_Main_Hall_ListItem> items = getListFromJson(strJson);
            if (items != null) {
                onRefreshComplete(items);
            }
        } else {
            Refresh(SwipyRefreshLayoutDirection.BOTH);
        }
    }

    public ArrayList<Tab_Main_Hall_ListItem> getListFromJson(String strJson) {
        try {
            Gson gson = new Gson();

            Type typeOfObjects = new TypeToken<ArrayList<Tab_Main_Hall_ListItem>>() {
            }.getType();

            return gson.fromJson(strJson, typeOfObjects);
        } catch (Exception e) {
            Log.e(MainActivity.LOGTAG, e.getMessage());
        }
        return null;
    }

    public static String getJsonFromObject(ArrayList<Tab_Main_Hall_ListItem> items) {
        try {
            Gson gson = new Gson();

            Type typeOfObjects = new TypeToken<ArrayList<Tab_Main_Hall_ListItem>>() {
            }.getType();

            return gson.toJson(items, typeOfObjects);
        } catch (Exception e) {
            Log.e(MainActivity.LOGTAG, e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    // for the first time when init using this
    private void onRefreshComplete(ArrayList<Tab_Main_Hall_ListItem> result) {
        if (result == null)
            return;
        if (mAdapter != null) {
            Log.d(MainActivity.LOGTAG, "ListView(mAdapter) already had data, and will be cleared...");
        }
        try {
            // first add to adapter and listView
            mAdapter = new HallListViewAdapter(getActivity(), R.layout.tab_main_hall_list_item, result);
            mListView.setAdapter(mAdapter);

            // now can start another task to load image async
            // we need url and position, if we use one thread to do all the download, so we store position in listitem.
            // be sure that the size of the array won't be too large, it's kind of waste the memory...


            // determine position
            int position = 0;
            for (Tab_Main_Hall_ListItem item : result) {
                item.setPosition(position);
                position++;
            }
            Tab_Main_Hall_ListItem[] items = new Tab_Main_Hall_ListItem[result.size()];
            result.toArray(items);

            new LoadHallListThumbsTask(getActivity(), mAdapter, mListView).execute(items);

        } catch (Exception e) {
            Log.e(MainActivity.LOGTAG, e.getMessage());
        }

        mSwipyRefreshLayout.setRefreshing(false);
    }

    private void onRefreshCompleteTop(ArrayList<Tab_Main_Hall_ListItem> result) {
        if (result == null)
            return;
        ArrayList<Tab_Main_Hall_ListItem> localData = null;
        try {
            // read local data, must have some, because of init
            String strJson = KecUtilities.getTabLocalData(HallOfMainActivity.subFolder, getActivity());

            if (strJson != null && !strJson.isEmpty()) {
                localData = getListFromJson(strJson);
            }
            // first add/insert into adapter/list
            // suppose result is ordered.
            // should insert into list from the last item...

            Tab_Main_Hall_ListItem[] items = new Tab_Main_Hall_ListItem[result.size()];
            for (int position = result.size() - 1; position >= 0; position--) {

                Tab_Main_Hall_ListItem item = result.get(position);
                mAdapter.insert(item, 0);
                if (localData != null)
                    localData.add(0, item);
                item.setPosition(position);
                items[position] = item;
            }


            // write to local not append, write
            KecUtilities.writeTabLocalData(getJsonFromObject(localData), HallOfMainActivity.subFolder, getActivity());

            new LoadHallListThumbsTask(getActivity(), mAdapter, mListView).execute(items);

        } catch (Exception e) {
            Log.e(MainActivity.LOGTAG, e.getMessage());
        }

        mSwipyRefreshLayout.setRefreshing(false);
    }

    private void onRefreshCompleteBottom(ArrayList<Tab_Main_Hall_ListItem> result) {
        if (result == null)
            return;
        ArrayList<Tab_Main_Hall_ListItem> localData = null;
        try {
            // read local data, must have some, because of init
            String strJson = KecUtilities.getTabLocalData(HallOfMainActivity.subFolder, getActivity());
            if (strJson != null && !strJson.isEmpty()) {
                localData = getListFromJson(strJson);
            }
            // 1. add/insert into adapter/list and set the correct position
            int position = mAdapter.getCount();
            for (Tab_Main_Hall_ListItem item : result) {
                item.setPosition(position);
                mAdapter.add(item);
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

            Tab_Main_Hall_ListItem[] items = new Tab_Main_Hall_ListItem[result.size()];
            result.toArray(items);
            // write to local not append, write
            KecUtilities.writeTabLocalData(getJsonFromObject(localData), HallOfMainActivity.subFolder, getActivity());
            new LoadHallListThumbsTask(getActivity(), mAdapter, mListView).execute(items);
        } catch (Exception e) {
            Log.e(MainActivity.LOGTAG, e.getMessage());
        }

        mSwipyRefreshLayout.setRefreshing(false);
    }

}
