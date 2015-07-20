package com.kectech.android.kectechapp.tabs;

import android.app.Activity;
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
import java.net.SocketTimeoutException;
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

    private ListView mListView;

    private HallListViewAdapter mAdapter;

    private SwipyRefreshLayout mSwipyRefreshLayout;

    private ActionMode mMode;

    private int num = 0;
    private Activity activity;

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
                    // if no selected
                    if (mAdapter.isSelectionEmpty()) {
                        stopActionMode();
                    }
                } else {

                    Tab_Main_Hall_ListItem tabMainHallListItem = mAdapter.getItem(position);
                    // get another activity to run  activity_main_hall
                    Intent intent = new Intent(activity, HallOfMainActivity.class);

                    intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    intent.putExtra(MainActivity.HALL_OF_MAIN_TYPE, tabMainHallListItem.getType());
                    intent.putExtra(MainActivity.HALL_OF_MAIN_ID, tabMainHallListItem.getId());
                    intent.putExtra(MainActivity.HALL_OF_MAIN_NAME, tabMainHallListItem.getName());
                    intent.putExtra(MainActivity.HALL_OF_MAIN_FOLLOW, tabMainHallListItem.getFollow());

                    try {
                        startActivity(intent);
                        //getActivity().overridePendingTransition(0, 0);
                        activity.overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
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


    // detect when this fragment is visible
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if (isVisibleToUser) {
            Log.d(MainActivity.LOGTAG, "tab_main_hall becomes visible.");
            // todo if visible refresh data
        } else {
            //hide cab
            Log.d(MainActivity.LOGTAG, "tab_main_hall becomes invisible.");
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
        inflater.inflate(R.menu.menu_hall_tab, menu);
        MenuItem menuItem = menu.findItem(R.id.menu_hall_tab_item_add);
        if (menuItem != null) {
            inflater.inflate(R.menu.hall_tab_add_submenu, menuItem.getSubMenu());
        }
        //super.onCreateOptionsMenu(menu, inflater);
    }

    // add an item to list from scanned result
    public void AddItemToList(String url) {


//        // todo get scan url or input id , or sth  to be continued...
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
                mMode = activity.startActionMode(new ModeCallback());
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
                        // todo scan result, to be continued...
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

        Toast.makeText(activity, option[menuItemIndex] + " selected.", Toast.LENGTH_SHORT).show();
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
            stopActionMode();
            return;
        }
        // first start animation, when animation ends, delete and finish

        AnimationSet as = new AnimationSet(true);
        as.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {

                stopActionMode();
                // do this in another thread
                Thread thread = new Thread() {
                    @Override
                    public void run() {
                        ArrayList<Tab_Main_Hall_ListItem> localData = new ArrayList<>();
                        for (int i = 0; i < mAdapter.getCount(); i++)
                        // remember to clear bitmap... other wise the json will be so huge...
                        // if set null directly, seems image will be null in the list
                        // so... new item..
                        {
                            Tab_Main_Hall_ListItem item = new Tab_Main_Hall_ListItem(mAdapter.getItem(i));
                            localData.add(item);
                        }
                        // write to local
                        KecUtilities.writeTabLocalData(Tab_Main_Hall.getJsonFromObject(localData), HallOfMainActivity.subFolder);

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
    private class UpdateThumbListTask extends AsyncTask<Integer, Void, String> {

        public UpdateThumbListTask() {
        }

        @Override
        protected String doInBackground(Integer... params) {
            // step1 Read from local if has data
            // step2 if not send http request
            // if updated write to local... after refresh... a lot of work to do
            // first try to request every time ...
            // for test
            //String strURL = "http://173.236.36.10/cds/generateThumbnail.php";
            String strURL = "http://198.105.216.190/generateThumbnail.ashx?id=&count=6&user=" + MainActivity.USER;

            try {
                URL url = new URL(strURL);

                URLConnection connection = url.openConnection();

                connection.connect();

                InputStream inputStream = new BufferedInputStream(url.openStream(), MainActivity.DOWNLOAD_BUFFER);
                //int length = connection.getContentLength();
                return KecUtilities.readStringFromStream(inputStream);
            } catch (SocketTimeoutException ste) {
                Log.d(MainActivity.LOGTAG, "time out:" + ste.getMessage());
            } catch (Exception e) {
                Log.e(MainActivity.LOGTAG, e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            ArrayList<Tab_Main_Hall_ListItem> items = getListFromJson(result);

            if (result != null && !items.isEmpty()) {
                KecUtilities.writeTabLocalData(result, HallOfMainActivity.subFolder);
                onRefreshComplete(items);
            }
            mSwipyRefreshLayout.setRefreshing(false);
        }
    }

    private class UpdateThumbListTaskTop extends AsyncTask<Integer, Void, String> {
        @Override
        protected String doInBackground(Integer... params) {
            // for test
            int id = params[0];
//            String strURL = "http://173.236.36.10/cds/generateThumbnail.php?type=top&count=5";
            String strURL = "http://198.105.216.190/generateThumbnail.ashx?id=" + id + "&count=2" + "&direction=after&user=" + MainActivity.USER;

            try {
                URL url = new URL(strURL);

                URLConnection connection = url.openConnection();

                connection.connect();

                InputStream inputStream = new BufferedInputStream(url.openStream(), MainActivity.DOWNLOAD_BUFFER);

                return KecUtilities.readStringFromStream(inputStream);

            } catch (SocketTimeoutException ste) {
                Log.d(MainActivity.LOGTAG, "time out: " + ste.getMessage());
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

            ArrayList<Tab_Main_Hall_ListItem> items = getListFromJson(result);

            if (items != null && !items.isEmpty())
                onRefreshCompleteTop(items);
            mSwipyRefreshLayout.setRefreshing(false);
        }
    }

    private class UpdateThumbListTaskBottom extends AsyncTask<Integer, Void, String> {
        @Override
        protected String doInBackground(Integer... params) {

            int id = params[0];
            // for test
            //String strURL = "http://173.236.36.10/cds/generateThumbnail.php?type=bottom&count=5";
            String strURL = "http://198.105.216.190/generateThumbnail.ashx?id=" + id + "&count=2" + "&direction=before&user=" + MainActivity.USER;

            try {
                URL url = new URL(strURL);

                URLConnection connection = url.openConnection();

                connection.connect();

                InputStream inputStream = new BufferedInputStream(url.openStream(), MainActivity.DOWNLOAD_BUFFER);
                //int length = connection.getContentLength();

                return KecUtilities.readStringFromStream(inputStream);

            } catch (SocketTimeoutException ste) {
                Log.d(MainActivity.LOGTAG, "time out: " + ste.getMessage());
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
            ArrayList<Tab_Main_Hall_ListItem> items = getListFromJson(result);
            if (items != null && !items.isEmpty())
                onRefreshCompleteBottom(items);
            mSwipyRefreshLayout.setRefreshing(false);
        }
    }

    // refresh list
    public void Refresh(SwipyRefreshLayoutDirection direction) {

        // actually bottom and init can use same interface??
        if (direction == SwipyRefreshLayoutDirection.TOP) {
            new UpdateThumbListTaskTop().execute(mAdapter.getItem(0).getId());
        } else if (direction == SwipyRefreshLayoutDirection.BOTTOM) {
            int i = mAdapter.getCount();
            new UpdateThumbListTaskBottom().execute(mAdapter.getItem(i - 1).getId());
        } else
            // use as init
            new UpdateThumbListTask().execute(0);
    }

    public void initList() {
        // only called on createView, so sync with server here.
        // clear local file

        // or request a version of list file, if changed, then start all over??
        // todo to be continued, right now just simply start it over.
        String strJson = KecUtilities.getTabLocalData(HallOfMainActivity.subFolder);

        ArrayList<Tab_Main_Hall_ListItem> items = null;

        if (strJson != null && !strJson.isEmpty()) {
            items = getListFromJson(strJson);
        }
        if (items != null && !items.isEmpty()) {
            onRefreshComplete(items);
        } else {
            Refresh(SwipyRefreshLayoutDirection.BOTH);
        }
//        Refresh(SwipyRefreshLayoutDirection.BOTH);
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
        if (result == null || result.isEmpty())
            return;
        if (mAdapter != null) {
            Log.d(MainActivity.LOGTAG, "ListView(mAdapter) already had data, and will be cleared...");
        }
        try {
            // first add to adapter and listView
            mAdapter = new HallListViewAdapter(activity, R.layout.tab_main_hall_list_item, result);
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

            new LoadHallListThumbsTask(mListView).execute(items);
//            LoadHallListThumbsTask task = new LoadHallListThumbsTask(mListView);
//            task.execute(items);
//            currentTask.add(task);

        } catch (Exception e) {
            Log.e(MainActivity.LOGTAG, e.getMessage());
        }

        mSwipyRefreshLayout.setRefreshing(false);
    }

    private void onRefreshCompleteTop(ArrayList<Tab_Main_Hall_ListItem> result) {
        if (result == null || result.isEmpty())
            return;
        ArrayList<Tab_Main_Hall_ListItem> localData = null;
        try {
            // read local data, must have some, because of init
            String strJson = KecUtilities.getTabLocalData(HallOfMainActivity.subFolder);

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
            KecUtilities.writeTabLocalData(getJsonFromObject(localData), HallOfMainActivity.subFolder);

            new LoadHallListThumbsTask(mListView).execute(items);
//            LoadHallListThumbsTask task = new LoadHallListThumbsTask(mListView);
//            task.execute(items);
//            currentTask.add(task);

        } catch (Exception e) {
            Log.e(MainActivity.LOGTAG, e.getMessage());
        }

        mSwipyRefreshLayout.setRefreshing(false);
    }

    private void onRefreshCompleteBottom(ArrayList<Tab_Main_Hall_ListItem> result) {
        if (result == null || result.isEmpty())
            return;
        ArrayList<Tab_Main_Hall_ListItem> localData = null;
        try {
            // read local data, must have some, because of init
            String strJson = KecUtilities.getTabLocalData(HallOfMainActivity.subFolder);
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

            if (!result.isEmpty()) {
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
            KecUtilities.writeTabLocalData(getJsonFromObject(localData), HallOfMainActivity.subFolder);
            new LoadHallListThumbsTask(mListView).execute(items);

//            LoadHallListThumbsTask task = new LoadHallListThumbsTask(mListView);
//            task.execute(items);
//            currentTask.add(task);

        } catch (Exception e) {
            Log.e(MainActivity.LOGTAG, e.getMessage());
        }

        mSwipyRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(MainActivity.LOGTAG, "tab_main_hall onPause.");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(MainActivity.LOGTAG, "tab_main_hall onStop.");

//        for (LoadHallListThumbsTask task : currentTask) {
//            task.cancel(true);
//        }
//        currentTask.clear();
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(MainActivity.LOGTAG, "tab_main_hall onStart.");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(MainActivity.LOGTAG, "tab_main_hall onResume.");
    }

    private void stopActionMode() {
        if (mMode != null) {
            mMode.finish();
            mMode = null;
        }
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
