package com.kectech.android.kectechapp.tabs;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
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
import com.kectech.android.kectechapp.BuildConfig;
import com.kectech.android.kectechapp.R;
import com.kectech.android.kectechapp.activity.HallOfMainActivity;
import com.kectech.android.kectechapp.activity.MainActivity;
import com.kectech.android.kectechapp.adapter.HallListViewAdapter;
import com.kectech.android.kectechapp.listitem.Tab_Main_Hall_ListItem;
import com.kectech.android.kectechapp.thirdparty.*;
import com.kectech.android.kectechapp.thirdparty.CacheBitmap.ImageFetcher;
import com.kectech.android.kectechapp.thirdparty.CacheBitmap.Utils;
import com.kectech.android.kectechapp.util.KecUtilities;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Created by Paul on 16/06/2015.
 * video tab implements by a ListView
 * use a SwipeRefreshLayout to fulfil pull down and pull up refresh
 * tab an item to open an activity to tab_main_show video page in a WebView
 */
public class Tab_Main_Hall extends Fragment implements SwipeRefreshLayout.OnRefreshListener{

    private ListView mListView;

    private HallListViewAdapter mAdapter;

    private SwipeRefreshLayout mSwipeRefreshLayout;

    private ActionMode mMode;

    private int num = 0;
    private Activity activity;

    private ImageFetcher mImageFetcher;

    private ArrayList<Integer> mDeleteList = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.tab_main_hall, container, false);

        mListView = (ListView) v.findViewById(R.id.hall_tab_list);

        mSwipeRefreshLayout = (SwipeRefreshLayout) v.findViewById(R.id.hall_tab_swipe_refresh_layout);
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
                        Log.e(MainActivity.LOG_TAG, "Exception caught: " + e.getMessage());
                        e.printStackTrace();
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
                Log.d(MainActivity.LOG_TAG, "tab_main_hall becomes visible.");
            // todo if visible refresh data
        } else {
            //hide cab
            if (BuildConfig.DEBUG)
                Log.d(MainActivity.LOG_TAG, "tab_main_hall becomes invisible.");
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

// http://198.105.216.190/generateFollow.ashx?handle=insert&url=ix6XDhkqFOMZ5uQGNprYWH4Id9o35A8cGPoyg8FxyPI=&follow=xxx.xxx.com&type=3
        // http://www.kdlinx.com/EventLogIn.aspx?id=wp7k/E13UFU/VFyVfWN1TbehBEpyFiwKmZVRD7Y3cRaN7W1YPyzcmpRcAN7GqG8G

        // kdlinx@kdlinx.com kdlinx12345


        // send http request then refresh top
        int pos = url.indexOf("id=");
        String eventId = url.substring(pos + 3);
        String strURl = "http://198.105.216.190/generateFollow.ashx?handle=insert&url=" + eventId + "&username=" + MainActivity.USER + "&type=3";
        new AddNewEventTask().execute(strURl);
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
            case R.id.menu_hall_tab_item_logout:
                // returen false in main activity, so deal with it in fragment
                String s = "123";
                return true;
            // handle in mainactivity
//            case R.id.menu_hall_tab_item_quit:
//                // clear cache
//                //mImageFetcher.clearCache();
//                if (BuildConfig.DEBUG)
//                    KecUtilities.clearCache();
//                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // deal with scan result
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        if (scanResult != null) {
            String scanContent = scanResult.getContents();

            if (scanContent != null) {
                if (BuildConfig.DEBUG) {
                    Log.d(MainActivity.LOG_TAG, "QR Scan Content: " + scanContent);
//                    IntentIntegrator integrator = new IntentIntegrator(this);
//                    integrator.shareText(scanContent);
//                    return;
                }

                if (!scanContent.isEmpty()) {
//            // insert into ....
                    try {
                        // todo scan result, to be continued...
                        AddItemToList(scanContent);
                    } catch (Exception e) {
                        Log.e(MainActivity.LOG_TAG, "Exception caught: " + e.getMessage());
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
        if (mAdapter == null || mAdapter.isSelectionEmpty()) {
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
                // http://www.kdlinx.com/enerateFollow.ashx?handle=delete&id=21

                String strIdList = "";
                for (int id : mDeleteList) {
                    strIdList += Integer.toString(id);
                    strIdList += ";";
                }
                if (!strIdList.isEmpty()) {
                    //strIdList = strIdList.substring(0, strIdList.length() - 1);
                    String strURL = "http://198.105.216.190/generateFollow.ashx?handle=delete&id=" + strIdList;
                    new DeleteEventTask().execute(strURL);
                    // do this in another thread
//                    Thread thread = new Thread() {
//                        @Override
//                        public void run() {
//                            ArrayList<Tab_Main_Hall_ListItem> localData = new ArrayList<>();
//                            for (int i = 0; i < mAdapter.getCount(); i++)
//                            // remember to clear bitmap... other wise the json will be so huge...
//                            // if set null directly, seems image will be null in the list
//                            // so... new item..
//                            {
//                                Tab_Main_Hall_ListItem item = new Tab_Main_Hall_ListItem(mAdapter.getItem(i));
//                                localData.add(item);
//                            }
//                            // write to local
//                            KecUtilities.writeTabLocalData(Tab_Main_Hall.getJsonFromObject(localData), MainActivity.HALL_OF_MAIN_SUBFOLDER);
//
//                            localData.clear();
//                        }
//                    };
//                    thread.start();
                }
                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        mDeleteList.clear();
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

            mDeleteList.add(mAdapter.getItem(i).getId());
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
    private class UpdateThumbListTask extends AsyncTask<Integer, Void, ArrayList<Tab_Main_Hall_ListItem>> {

        public UpdateThumbListTask() {
        }

        @Override
        protected ArrayList<Tab_Main_Hall_ListItem> doInBackground(Integer... params) {
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
                connection.setConnectTimeout(MainActivity.CONNECTION_TIMEOUT);
                connection.setReadTimeout(MainActivity.CONNECTION_TIMEOUT);
                connection.connect();

                InputStream inputStream = new BufferedInputStream(url.openStream(), MainActivity.DOWNLOAD_BUFFER);
                //int length = connection.getContentLength();
                String strResult = KecUtilities.readStringFromStream(inputStream);
                ArrayList<Tab_Main_Hall_ListItem> items = getListFromJson(strResult);
                if (items == null)
                    return new ArrayList<>();
                else if (!items.isEmpty()) {
                    KecUtilities.writeTabLocalData(strResult, MainActivity.HALL_OF_MAIN_SUBFOLDER);
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
        protected void onPostExecute(ArrayList<Tab_Main_Hall_ListItem> result) {
            super.onPostExecute(result);

            onRefreshComplete(result);
            mSwipeRefreshLayout.setRefreshing(false);
        }
    }

    private class UpdateThumbListTaskTop extends AsyncTask<Integer, Void, ArrayList<Tab_Main_Hall_ListItem>> {
        @Override
        protected ArrayList<Tab_Main_Hall_ListItem> doInBackground(Integer... params) {
            // for test
            int id = params[0];
//            String strURL = "http://173.236.36.10/cds/generateThumbnail.php?type=top&count=5";
            String strURL = "http://198.105.216.190/generateThumbnail.ashx?id=" + id + "&count=2" + "&direction=after&user=" + MainActivity.USER;

            try {
                URL url = new URL(strURL);

                URLConnection connection = url.openConnection();
                connection.setConnectTimeout(MainActivity.CONNECTION_TIMEOUT);
                connection.setReadTimeout(MainActivity.CONNECTION_TIMEOUT);
                connection.connect();

                InputStream inputStream = new BufferedInputStream(url.openStream(), MainActivity.DOWNLOAD_BUFFER);

                String strResult = KecUtilities.readStringFromStream(inputStream);
                ArrayList<Tab_Main_Hall_ListItem> items = getListFromJson(strResult);
                if (items != null && !items.isEmpty()) {
                    ArrayList<Tab_Main_Hall_ListItem> localData = null;
                    String strJson = KecUtilities.getTabLocalData(MainActivity.HALL_OF_MAIN_SUBFOLDER);

                    if (strJson != null && !strJson.isEmpty()) {
                        localData = getListFromJson(strJson);
                    }
                    // first add/insert into adapter/list
                    // suppose result is ordered.
                    // should insert into list from the last item...

                    //Tab_Main_Hall_ListItem[] items = new Tab_Main_Hall_ListItem[result.size()];
                    for (int position = items.size() - 1; position >= 0; position--) {

                        Tab_Main_Hall_ListItem item = items.get(position);
                        if (localData != null)
                            localData.add(0, item);
                    }
                    // write to local not append, write
                    KecUtilities.writeTabLocalData(getJsonFromObject(localData), MainActivity.HALL_OF_MAIN_SUBFOLDER);

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
        protected void onPostExecute(ArrayList<Tab_Main_Hall_ListItem> result) {
            super.onPostExecute(result);

            if (isCancelled())
                return;

            onRefreshCompleteTop(result);
            mSwipeRefreshLayout.setRefreshing(false);
        }
    }

    private class UpdateThumbListTaskBottom extends AsyncTask<Integer, Void, ArrayList<Tab_Main_Hall_ListItem>> {
        @Override
        protected ArrayList<Tab_Main_Hall_ListItem> doInBackground(Integer... params) {

            int id = params[0];
            // for test
            //String strURL = "http://173.236.36.10/cds/generateThumbnail.php?type=bottom&count=5";
            String strURL = "http://198.105.216.190/generateThumbnail.ashx?id=" + id + "&count=2" + "&direction=before&user=" + MainActivity.USER;

            try {
                URL url = new URL(strURL);

                URLConnection connection = url.openConnection();
                connection.setConnectTimeout(MainActivity.CONNECTION_TIMEOUT);
                connection.setReadTimeout(MainActivity.CONNECTION_TIMEOUT);

                connection.connect();

                InputStream inputStream = new BufferedInputStream(url.openStream(), MainActivity.DOWNLOAD_BUFFER);
                //int length = connection.getContentLength();

                String strResult = KecUtilities.readStringFromStream(inputStream);
                ArrayList<Tab_Main_Hall_ListItem> items = getListFromJson(strResult);
                if (items != null && !items.isEmpty()) {
                    ArrayList<Tab_Main_Hall_ListItem> localData = null;
                    String strJson = KecUtilities.getTabLocalData(MainActivity.HALL_OF_MAIN_SUBFOLDER);
                    if (strJson != null && !strJson.isEmpty()) {
                        localData = getListFromJson(strJson);
                    }

                    for (Tab_Main_Hall_ListItem item : items) {

                        if (localData != null)
                            localData.add(item);

                    }

                    KecUtilities.writeTabLocalData(getJsonFromObject(localData), MainActivity.HALL_OF_MAIN_SUBFOLDER);

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
        protected void onPostExecute(ArrayList<Tab_Main_Hall_ListItem> result) {
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
            if (mAdapter.getCount() > 0)
                new UpdateThumbListTaskTop().execute(mAdapter.getItem(0).getId());
            else
                new UpdateThumbListTaskTop().execute(0);
        } else if (direction == SwipeRefreshLayoutDirection.BOTTOM && mAdapter != null) {
            if (mAdapter.getCount() > 0) {
                int i = mAdapter.getCount();
                new UpdateThumbListTaskBottom().execute(mAdapter.getItem(i - 1).getId());
            } else
                new UpdateThumbListTaskBottom().execute(0);
        } else {
            if (mAdapter != null)
                mAdapter.clear();
            // use as init
            new UpdateThumbListTask().execute(0);
        }
    }

    public void initList() {
        new InitListTask().execute();
    }

    public ArrayList<Tab_Main_Hall_ListItem> getListFromJson(String strJson) {
        try {
            Gson gson = new Gson();

            Type typeOfObjects = new TypeToken<ArrayList<Tab_Main_Hall_ListItem>>() {
            }.getType();

            return gson.fromJson(strJson, typeOfObjects);
        } catch (Exception e) {
            Log.e(MainActivity.LOG_TAG, "Exception caught: " + e.getMessage());
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
            Log.e(MainActivity.LOG_TAG, "Exception caught: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    // for the first time when init using this, or update when sth wrong with list, need refresh again
    private void onRefreshComplete(ArrayList<Tab_Main_Hall_ListItem> result) {
        if (result == null)
            // means error occurred when sending http request, other wise result should be empty but not null
            return;
        if (mAdapter != null) {
            if (BuildConfig.DEBUG)
                Log.d(MainActivity.LOG_TAG, "ListView(mAdapter) already had data, and will be cleared...");

            mAdapter.clear();
            mAdapter.notifyDataSetChanged();
            if (!result.isEmpty()) {
                for(Tab_Main_Hall_ListItem item: result) {
                    mAdapter.add(item);

                }
                mAdapter.notifyDataSetChanged();
            }
            return;
        }

        try {

            // first add to adapter and listView
            mAdapter = new HallListViewAdapter(activity, R.layout.tab_main_hall_list_item, result, mImageFetcher);
            mListView.setAdapter(mAdapter);

//            // now can start another task to load image async
//            // we need url and position, if we use one thread to do all the download, so we store position in listitem.
//            // be sure that the size of the array won't be too large, it's kind of waste the memory...
//
//
//            // determine position
//            int position = 0;
//            for (Tab_Main_Hall_ListItem item : result) {
//                item.setPosition(position);
//                position++;
//            }
//            Tab_Main_Hall_ListItem[] items = new Tab_Main_Hall_ListItem[result.size()];
//            result.toArray(items);
//
//            new LoadHallListThumbsTask(mListView).execute(items);
////            LoadHallListThumbsTask task = new LoadHallListThumbsTask(mListView);
////            task.execute(items);
////            currentTask.add(task);

        } catch (Exception e) {
            Log.e(MainActivity.LOG_TAG, "Exception caught: " + e.getMessage());
        }
    }

    private void onRefreshCompleteTop(ArrayList<Tab_Main_Hall_ListItem> result) {
        if (result == null || result.isEmpty())
            return;
        try {
            for (int position = result.size() - 1; position >= 0; position--) {

                Tab_Main_Hall_ListItem item = result.get(position);
                mAdapter.insert(item, 0);

            }
        } catch (Exception e) {
            Log.e(MainActivity.LOG_TAG, "Exception caught: " + e.getMessage());
        }
    }

    private void onRefreshCompleteBottom(ArrayList<Tab_Main_Hall_ListItem> result) {
        if (result == null || result.isEmpty())
            return;

        try {

            for (Tab_Main_Hall_ListItem item : result) {
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
            Log.e(MainActivity.LOG_TAG, "Exception caught: " + e.getMessage());
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (BuildConfig.DEBUG)
            Log.d(MainActivity.LOG_TAG, "tab_main_hall onStop.");

//        for (LoadHallListThumbsTask task : currentTask) {
//            task.cancel(true);
//        }
//        currentTask.clear();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (BuildConfig.DEBUG)
            Log.d(MainActivity.LOG_TAG, "tab_main_hall onStart.");
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

    private class InitListTask extends AsyncTask<Void, Void, ArrayList<Tab_Main_Hall_ListItem>> {
        @Override
        protected ArrayList<Tab_Main_Hall_ListItem> doInBackground(Void... params) {

            String strJson = KecUtilities.getTabLocalData(MainActivity.HALL_OF_MAIN_SUBFOLDER);

            ArrayList<Tab_Main_Hall_ListItem> items = null;

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
        protected void onPostExecute(ArrayList<Tab_Main_Hall_ListItem> result) {
            super.onPostExecute(result);
            if (isCancelled())
                return;

            if (result != null && !result.isEmpty())
                onRefreshComplete(result);
        }
    }

    public class AddNewEventTask extends AsyncTask<String, Void, Boolean> {

        @Override
        protected Boolean doInBackground(String... params) {

            try {
                ImageFetcher.disableConnectionReuseIfNecessary();
                HttpURLConnection urlConnection = null;
                BufferedInputStream in = null;
                try {
                    final String urlString = params[0];
                    final URL url = new URL(urlString);
                    urlConnection = (HttpURLConnection) url.openConnection();
                    in = new BufferedInputStream(urlConnection.getInputStream(), MainActivity.DOWNLOAD_BUFFER);

                    // byte array to store input
                    byte[] contents = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = in.read(contents)) != -1) {
                        String s = new String(contents, 0, bytesRead);
                        if (s.compareToIgnoreCase("true") == 0)
                            return true;
                        else
                            return false;
                    }

                    return true;
                } catch (final IOException e) {
                    Log.e(MainActivity.LOG_TAG, "Error in add item - " + e.getMessage());
                    e.printStackTrace();
                } finally {
                    if (urlConnection != null) {
                        urlConnection.disconnect();
                    }
                    try {
                        if (in != null) {
                            in.close();
                        }
                    } catch (final IOException e) {}
                }
                return false;
            } catch (Exception e) {
                Log.e(MainActivity.LOG_TAG, "Exception: " + e.getMessage());
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            if (success)
                Refresh(SwipeRefreshLayoutDirection.TOP);
//            else
//                Refresh(SwipeRefreshLayoutDirection.BOTH);
        }

        @Override
        protected void onCancelled() {
        }
    }

    public class DeleteEventTask extends AsyncTask<String, Void, Boolean> {

        @Override
        protected Boolean doInBackground(String... params) {

            try {
                // write local data first, then send http request
                ArrayList<Tab_Main_Hall_ListItem> localData = new ArrayList<>();
                for (int i = 0; i < mAdapter.getCount(); i++)
                // remember to clear bitmap... other wise the json will be so huge...
                // if set null directly, seems image will be null in the list
                // so... new item..
                {
                    localData.add(mAdapter.getItem(i));
                }
                // write to local
                KecUtilities.writeTabLocalData(Tab_Main_Hall.getJsonFromObject(localData), MainActivity.HALL_OF_MAIN_SUBFOLDER);

                localData.clear();

                // send request
                ImageFetcher.disableConnectionReuseIfNecessary();
                HttpURLConnection urlConnection = null;
                BufferedInputStream in = null;
                try {
                    final String urlString = params[0];
                    final URL url = new URL(urlString);
                    urlConnection = (HttpURLConnection) url.openConnection();
                    in = new BufferedInputStream(urlConnection.getInputStream(), MainActivity.DOWNLOAD_BUFFER);

                    // byte array to store input
                    byte[] contents = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = in.read(contents)) != -1) {
                        String s = new String(contents, 0, bytesRead);
                        if (s.compareToIgnoreCase("true") == 0)
                            return true;
                        else
                            return false;
                    }

                    return true;
                } catch (final IOException e) {
                    Log.e(MainActivity.LOG_TAG, "Error in delete item - " + e.getMessage());
                    e.printStackTrace();
                } finally {
                    if (urlConnection != null) {
                        urlConnection.disconnect();
                    }
                    try {
                        if (in != null) {
                            in.close();
                        }
                    } catch (final IOException e) {}
                }
                return false;
            } catch (Exception e) {
                Log.e(MainActivity.LOG_TAG, "Exception: " + e.getMessage());
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            if (!success) {
                Refresh(SwipeRefreshLayoutDirection.BOTH);
            }
        }

        @Override
        protected void onCancelled() {
        }
    }
}
