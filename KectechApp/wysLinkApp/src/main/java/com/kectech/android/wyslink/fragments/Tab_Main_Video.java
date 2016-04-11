package com.kectech.android.wyslink.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.share.Sharer;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.kectech.android.wyslink.BuildConfig;
import com.kectech.android.wyslink.R;
import com.kectech.android.wyslink.activity.ChooseVideoActivity;
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
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Paul on 10/06/2015.
 * video tab on activity of hall
 */
public class Tab_Main_Video extends Fragment {

    private ListView mListView;

    private VideoListViewAdapter mVideoAdapter;

    private SwipeRefreshLayout mSwipeRefreshLayout;

    private int tabId = 0;
    private String tabName;
    private String strType;
    private String tabFollow;

    private String subFolder = null;
    private Activity activity;

    private ImageFetcher mImageFetcher;

    //private CustomTabActivityHelper mCustomTabActivityHelper;
    // for test facebook sdk
    private CallbackManager callbackManager;
    private ShareDialog shareDialog;

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
        View v = inflater.inflate(R.layout.tab_main_video, container, false);

        // for test facebook sdk
        callbackManager = CallbackManager.Factory.create();
        FacebookCallback<Sharer.Result> callback =
                new FacebookCallback<Sharer.Result>() {
                    @Override
                    public void onCancel() {
                        Log.d(MainActivity.LOG_TAG, "Canceled");
                    }

                    @Override
                    public void onError(FacebookException error) {
                        Log.d(MainActivity.LOG_TAG, String.format("Error: %s", error.toString()));
                    }

                    @Override
                    public void onSuccess(Sharer.Result result) {
                        Log.d(MainActivity.LOG_TAG, "Success!");
                    }
                };
        shareDialog = new ShareDialog(this);
        shareDialog.registerCallback(callbackManager, callback);

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
                String strVideo = null;
                if (strUrl.indexOf("?url=") > 0) {
                    if (strUrl.indexOf("&tl=") > 0) {
                        strVideo = strUrl.substring(strUrl.indexOf("?url=") + 5, strUrl.indexOf("&tl="));
                    } else {
                        strVideo = strUrl.substring(strUrl.indexOf("?url=") + 5, strUrl.length() - 1);
                    }
                }

                //strVideo = strUrl.substring(strUrl.indexOf("?url=") + 5, strUrl.indexOf("&tl="));
                Intent intent = new Intent(activity, VideoViewActivity.class);
                intent.putExtra(MainActivity.BUNDLE_KEY_CONTENT_URL, KecUtilities.decryptUrl(strVideo));

                startActivity(intent);
                activity.overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
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
            Log.e(MainActivity.LOG_TAG, "Exception caught(tab_main_video---getListFromJson): " + e.getMessage());
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
            Log.e(MainActivity.LOG_TAG, "Exception caught(tab_main_video---getJsonFromObject): " + e.getMessage());
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
            if (activity == null)
                return;
            // first add to adapter and listView
            mVideoAdapter = new VideoListViewAdapter(activity, R.layout.video_list_item, result, mImageFetcher);
            mListView.setAdapter(mVideoAdapter);

        } catch (NullPointerException npe) {
            Log.e(MainActivity.LOG_TAG, npe.getMessage());
            npe.printStackTrace();
        } catch (Exception e) {
            Log.e(MainActivity.LOG_TAG, "Exception caught(tab_main_video---onRefreshComplete): " + e.getMessage());
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
            Log.e(MainActivity.LOG_TAG, "Exception caught(tab_main_video---onRefreshCompleteTop): " + e.getMessage());
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
            Log.e(MainActivity.LOG_TAG, "Exception caught(tab_main_video---onRefreshCompleteBottom): " + e.getMessage());
        }
    }

    // detect when this fragment is visible
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if (BuildConfig.DEBUG) {
            if (isVisibleToUser) {
                Log.d(MainActivity.LOG_TAG, "tab_main_video becomes visible.");
            } else {
                Log.d(MainActivity.LOG_TAG, "tab_main_video becomes invisible.");
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
        inflater.inflate(R.menu.menu_my_video_tab, menu);
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
            Log.d(MainActivity.LOG_TAG, "tab_main_video onStop.");
    }

    @Override
    public void onStart() {
        super.onStart();
        //mCustomTabActivityHelper.bindCustomTabsService(getActivity());
        if (BuildConfig.DEBUG)
            Log.d(MainActivity.LOG_TAG, "tab_main_video onStart.");
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id) {
//            case R.id.menu_item_search:
//                return true;
            case R.id.video_menu_item_add_new_video:
                // open a new activity, and get result here
                startChooseVideoActivity();
                return true;
            case R.id.video_share_facebook:
                shareViaFacebook("Panda", "play around", "http://www.wyslink.com/video.aspx?url=/XpIMmZkJJIUF1vCVQWuvf3Fri8FxfJ6MiNgnx/EK2lQIbpWV/h4DyjG%2BqE7ixs09V/oBtEvOVCZX41AkBXEQBG308WZOzaj");
                return true;
            case R.id.video_share_twitter:
                shareViaTwitter("panda", "http://www.wyslink.com/video.aspx?url=/XpIMmZkJJIUF1vCVQWuvf3Fri8FxfJ6MiNgnx/EK2lQIbpWV/h4DyjG%2BqE7ixs09V/oBtEvOVCZX41AkBXEQBG308WZOzaj");
                return true;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    public View getActionBarView() {
        Window window = getActivity().getWindow();
        View v = window.getDecorView();
        int resId = getResources().getIdentifier("action_bar_container", "id", "android");
        return v.findViewById(resId);
    }

    private void startChooseVideoActivity() {

        Intent intent = new Intent(getActivity(), ChooseVideoActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra(MainActivity.BUNDLE_KEY_CONTENT_URL, "http://www.wyslink.com/");
        try {
            // do not finish, instead call startActivityForResult
            //startActivityForResult(intent, MainActivity.NEW_POST_CODE);
            startActivity(intent);
            getActivity().overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
        } catch (Exception e) {
            Log.e(MainActivity.LOG_TAG, "Exception caught(tab_main_video---startChooseVideoActivity): " + e.getMessage());

        }
    }

//    private void showShareMenu() {
//        //Creating the instance of PopupMenu
//        PopupMenu popup = new PopupMenu(getActivity(), menuItemView);
//        //Inflating the Popup using xml file
//        popup.getMenuInflater()
//                .inflate(R.menu.menu_video_share, popup.getMenu());
//
//        //registering popup with OnMenuItemClickListener
//        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
//            public boolean onMenuItemClick(MenuItem item) {
//
//                return true;
//            }
//        });
//
//        popup.show(); //showing popup menu
//    }

    private void share(String nameApp, String imagePath) {
        List<Intent> targetedShareIntents = new ArrayList<Intent>();
        Intent share = new Intent(android.content.Intent.ACTION_SEND);
        share.setType("image/jpeg");
        List<ResolveInfo> resInfo = getActivity().getPackageManager().queryIntentActivities(share, 0);
        if (!resInfo.isEmpty()) {
            for (ResolveInfo info : resInfo) {
                Intent targetedShare = new Intent(android.content.Intent.ACTION_SEND);
                targetedShare.setType("image/jpeg"); // put here your mime type

                if (info.activityInfo.packageName.toLowerCase().contains(nameApp) ||
                        info.activityInfo.name.toLowerCase().contains(nameApp)) {
                    targetedShare.putExtra(Intent.EXTRA_TEXT, "My body of post/email");
                    targetedShare.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(imagePath)));
                    targetedShare.setPackage(info.activityInfo.packageName);
                    targetedShareIntents.add(targetedShare);
                }
            }

            Intent chooserIntent = Intent.createChooser(targetedShareIntents.remove(0), "Select app to share");
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, targetedShareIntents.toArray(new Parcelable[targetedShareIntents.size()]));
            startActivity(chooserIntent);
        }
    }

    private void shareViaTwitter(String text, String url) {
        // Create intent using ACTION_VIEW and a normal Twitter url:
        String tweetUrl = String.format("https://twitter.com/intent/tweet?text=%s&url=%s",
                urlEncode(text),
                urlEncode(url));
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(tweetUrl));

        // Narrow down to official Twitter app, if available:
        List<ResolveInfo> matches = getActivity().getPackageManager().queryIntentActivities(intent, 0);
        for (ResolveInfo info : matches) {
            if (info.activityInfo.packageName.toLowerCase().startsWith("com.twitter")) {
                intent.setPackage(info.activityInfo.packageName);
            }
        }

        startActivity(intent);
    }

    public static String urlEncode(String s) {
        try {
            return URLEncoder.encode(s, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            Log.wtf(MainActivity.LOG_TAG, "UTF-8 should always be supported", e);
            throw new RuntimeException("URLEncoder.encode() failed for " + s);
        }
    }

    private void shareViaFacebook(String title, String description, String url) {
        if (ShareDialog.canShow(ShareLinkContent.class)) {
            ShareLinkContent linkContent = new ShareLinkContent.Builder()
                    .setContentTitle(title)
                    .setContentDescription(description)
                    .setContentUrl(Uri.parse(url))
                    .build();

//            shareDialog.show(linkContent, ShareDialog.Mode.WEB);

            if (shareDialog.canShow(linkContent, ShareDialog.Mode.NATIVE)) {
                shareDialog.show(linkContent, ShareDialog.Mode.NATIVE);
            } else if (shareDialog.canShow(linkContent, ShareDialog.Mode.WEB)) {
                shareDialog.show(linkContent, ShareDialog.Mode.WEB);
            } else
                shareDialog.show(linkContent, ShareDialog.Mode.AUTOMATIC);
        }
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }
}