package com.kectech.android.wyslink.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.kectech.android.kectechapp.BuildConfig;
import com.kectech.android.kectechapp.R;
import com.kectech.android.wyslink.activity.MainActivity;
import com.kectech.android.wyslink.activity.NewPostActivity;
import com.kectech.android.wyslink.activity.PhotoOfHallOfMainActivity;
import com.kectech.android.wyslink.adapter.PhotoListViewAdapter;
import com.kectech.android.wyslink.listitem.PhotoListItem;
import com.kectech.android.wyslink.thirdparty.CacheBitmap.ImageFetcher;
import com.kectech.android.wyslink.thirdparty.SwipeRefreshLayout;
import com.kectech.android.wyslink.thirdparty.SwipeRefreshLayoutDirection;
import com.kectech.android.wyslink.util.KecUtilities;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;


/**
 * Created by Paul on 16/06/2015.
 * use SwipeRefreshLayout to refresh (pull up and pull down)
 * use ListView
 * async download thumbs then show in ListView's items(may be more than one thumb in one item, depend on the json...)
 * tab an item to open an activity to show image(s).
 */
public class Tab_Main_Hall_Photo extends Fragment {

    private boolean bAllowNewPost = true;
    // list
    private ListView mListView;
    // adapter
    private PhotoListViewAdapter mPhotoAdapter;
    // swipe refresh layout
    private SwipeRefreshLayout mSwipeRefreshLayout;

    private int tabType = 0;
    private int tabId = 0;

    private String subFolder = null;

    //private Activity activity;

    private ImageFetcher mImageFetcher;

    public void setType(int tabType) {
        this.tabType = tabType;
    }

    public void setId(int tabId) {
        this.tabId = tabId;
    }

    public void createSubFolder() {
        String folder = MainActivity.USER + File.separator + MainActivity.HALL_SUB_FOLDER + File.separator + tabId + File.separator + MainActivity.PHOTO_SUB_FOLDER;
        if (KecUtilities.createSubFolders(folder)) {
            subFolder = folder;
        } else
            subFolder = null;

    }

    @Override
    @SuppressWarnings("deprecation")
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.tab_main_hall_photo, container, false);

        mListView = (ListView) v.findViewById(R.id.photo_tab_list);
        mSwipeRefreshLayout = (SwipeRefreshLayout) v.findViewById(R.id.photo_tab_swipe_refresh_layout);
        mSwipeRefreshLayout.setDirection(SwipeRefreshLayoutDirection.BOTH);

        // color of refresh spinner
        mSwipeRefreshLayout.setColorScheme(
                R.color.swipe_color_1,
                R.color.swipe_color_3,
                R.color.swipe_color_5);

        // list refresh listener
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh(SwipeRefreshLayoutDirection direction) {
                Refresh(direction);
            }
        });

        // item click listener
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                PhotoListItem photoListItem = mPhotoAdapter.getItem(position);

                // activate another activity to show full image
                final Intent intent = new Intent(getActivity(), PhotoOfHallOfMainActivity.class);

                // create parameters
                Bundle params = new Bundle();

                //ArrayList<String> thumbs = new ArrayList<>();
                ArrayList<String> images = new ArrayList<>();

                for (int i = 0; i < photoListItem.items.size(); i++) {
                    //thumbs.add(photoListItem.items.get(i).getThumbURL());
                    images.add(photoListItem.items.get(i).getImageURL());
                }
                //params.putStringArrayList(MainActivity.PHOTO_TAB_THUMB_URL_KEY, thumbs);
                params.putStringArrayList(MainActivity.PHOTO_TAB_IMAGE_URL_KEY, images);
                //params.putString(MainActivity.MAIN_HALL_PHOTO_FOLDER, subFolder);
//                params.putString(MainActivity.PHOTO_TAB_THUMB_URL_KEY, photoListItem.getThumbURL());
//                params.putString(MainActivity.PHOTO_TAB_IMAGE_URL_KEY, photoListItem.getImageURL());

                //intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

                intent.putExtras(params);
                try {
                    startActivity(intent);
                    getActivity().overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
                } catch (Exception e) {
                    Log.e(MainActivity.LOG_TAG, "Exception caught(tab_main_hall_photo---onCreateView): " + e.getMessage());
                }

            }
        });

        mImageFetcher = KecUtilities.getThumbFetcher(getActivity());

        mPhotoAdapter = new PhotoListViewAdapter(getActivity(), R.layout.photo_list_item, new ArrayList<PhotoListItem>(), mImageFetcher);
        mListView.setAdapter(mPhotoAdapter);

        if (bAllowNewPost) {
            ImageView floatButton = (ImageView) v.findViewById(R.id.photo_floating_button);
            floatButton.setVisibility(View.VISIBLE);
            floatButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startNewPostActivity();
                }
            });
        }

        initList();

        return v;
    }


    // refresh list
    public void Refresh(SwipeRefreshLayoutDirection direction) {

        // actually bottom and init can use same interface??
        if (direction == SwipeRefreshLayoutDirection.TOP) {
            new UpdatePhotoListTaskTop().execute("todo");
        } else if (direction == SwipeRefreshLayoutDirection.BOTTOM) {
            new UpdatePhotoListTaskBottom().execute("todo");
        } else
            // use as init
            new UpdatePhotoListTask().execute("todo");
    }

    // used to get the list item json file init, top_refresh, bottom_refresh, maybe need param when sending http request
    // to be continued...
    // may need to write 3 task
    private class UpdatePhotoListTask extends AsyncTask<String, Void, ArrayList<PhotoListItem>> {

        public UpdatePhotoListTask() {
        }

        @Override
        protected ArrayList<PhotoListItem> doInBackground(String... params) {
            // step1 Read from loacl if has data
            // step2 if not send http request
            // if updated write to local... after refresh... a lot of work to do
            // first try to request every time ...

            // for test
            String strURL = "http://173.236.36.10/cds/generateThumbnail_multi.php";

            try {
                URL url = new URL(strURL);

                URLConnection connection = url.openConnection();
                connection.setConnectTimeout(MainActivity.CONNECTION_TIMEOUT);
                connection.setReadTimeout(MainActivity.CONNECTION_TIMEOUT);
                connection.connect();

                InputStream inputStream = new BufferedInputStream(url.openStream(), MainActivity.DOWNLOAD_BUFFER);
                //int length = connection.getContentLength();
                String strResult = KecUtilities.readStringFromStream(inputStream);

                ArrayList<PhotoListItem> items = getListFromJson(strResult);

                if (strResult != null && subFolder != null && !items.isEmpty()) {
                    KecUtilities.writeTabLocalData(strResult, subFolder);
                }
                return items;
            } catch (SocketTimeoutException ste) {
                Log.e(MainActivity.LOG_TAG, "time out:" + ste.getMessage());
            } catch (IOException e) {
                Log.e(MainActivity.LOG_TAG, e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(ArrayList<PhotoListItem> result) {
            // no disk operation here, will stuck UI
            super.onPostExecute(result);
            onRefreshComplete(result);
            mSwipeRefreshLayout.setRefreshing(false);
        }
    }

    private class UpdatePhotoListTaskTop extends AsyncTask<String, Void, ArrayList<PhotoListItem>> {
        @Override
        protected ArrayList<PhotoListItem> doInBackground(String... params) {
            // for test
            String strURL = "http://173.236.36.10/cds/generateThumbnail_multi.php?type=top&count=5";

            try {
                URL url = new URL(strURL);

                URLConnection connection = url.openConnection();
                connection.setConnectTimeout(MainActivity.CONNECTION_TIMEOUT);
                connection.setReadTimeout(MainActivity.CONNECTION_TIMEOUT);
                connection.connect();

                InputStream inputStream = new BufferedInputStream(url.openStream(), MainActivity.DOWNLOAD_BUFFER);

                String strResult = KecUtilities.readStringFromStream(inputStream);

                ArrayList<PhotoListItem> items = getListFromJson(strResult);

                // read insert write
                if (items != null && !items.isEmpty()) {
                    if (subFolder != null) {
                        ArrayList<PhotoListItem> localData = null;
                        String strJson = KecUtilities.getTabLocalData(subFolder);
                        if (strJson != null && !strJson.isEmpty()) {
                            localData = getListFromJson(strJson);
                        }
                        for (int position = items.size() - 1; position >= 0; position--) {

                            PhotoListItem item = items.get(position);
                            if (localData != null)
                                localData.add(0, item);
                        }
                        // write to local not append, write
                        KecUtilities.writeTabLocalData(getJsonFromObject(localData), subFolder);
                    }
                    // UI
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
        protected void onPostExecute(ArrayList<PhotoListItem> result) {
            super.onPostExecute(result);

            onRefreshCompleteTop(result);
            mSwipeRefreshLayout.setRefreshing(false);
        }
    }

    private class UpdatePhotoListTaskBottom extends AsyncTask<String, Void, ArrayList<PhotoListItem>> {
        @Override
        protected ArrayList<PhotoListItem> doInBackground(String... params) {

            // for test
            String strURL = "http://173.236.36.10/cds/generateThumbnail_multi.php?type=bottom&count=5";

            try {
                URL url = new URL(strURL);

                URLConnection connection = url.openConnection();
                connection.setConnectTimeout(MainActivity.CONNECTION_TIMEOUT);
                connection.setReadTimeout(MainActivity.CONNECTION_TIMEOUT);
                connection.connect();

                InputStream inputStream = new BufferedInputStream(url.openStream(), MainActivity.DOWNLOAD_BUFFER);
                //int length = connection.getContentLength();

                String strResult = KecUtilities.readStringFromStream(inputStream);
                ArrayList<PhotoListItem> items = getListFromJson(strResult);

                if (items != null && !items.isEmpty()) {
                    if (subFolder != null) {
                        ArrayList<PhotoListItem> localData = null;
                        String strJson = KecUtilities.getTabLocalData(subFolder);
                        if (strJson != null && !strJson.isEmpty()) {
                            localData = getListFromJson(strJson);
                        }
                        for (PhotoListItem item : items) {
                            if (localData != null)
                                localData.add(item);
                        }
                        KecUtilities.writeTabLocalData(getJsonFromObject(localData), subFolder);
                    }
                    // UI
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
        protected void onPostExecute(ArrayList<PhotoListItem> result) {
            super.onPostExecute(result);
            onRefreshCompleteBottom(result);
            mSwipeRefreshLayout.setRefreshing(false);
        }
    }

    // for the first time when init using this
    private void onRefreshComplete(ArrayList<PhotoListItem> result) {
        if (result == null || subFolder == null || result.isEmpty())
            return;
        try {
            // first add to adapter and listView
//            mPhotoAdapter = new PhotoListViewAdapter(activity, R.layout.photo_list_item, result);
//            mListView.setAdapter(mPhotoAdapter);

            // now can start another task to load image async
            // we need url and position, if we use one thread to do all the download, so we store position in photolistitem.
            // be sure that the size of the array won't be too large, it's kind of waste the memory...


            // determine position
            //int position = 0;
            for (PhotoListItem item : result) {
                //item.setPosition(position);
                //position++;
                mPhotoAdapter.add(item);
            }
//            PhotoListItem[] items = new PhotoListItem[result.size()];
//            result.toArray(items);

            mListView.setAdapter(mPhotoAdapter);

            //new LoadHallPhotoListThumbsTask(mPhotoAdapter, mListView, subFolder).execute(items);

        } catch (Exception e) {
            Log.e(MainActivity.LOG_TAG, "Exception caught(tab_main_hall_photo---onRefreshComplete): " + e.getMessage());
        }
    }

    private void onRefreshCompleteTop(ArrayList<PhotoListItem> result) {
        if (result == null || result.isEmpty())
            return;

        try {
            for (int position = result.size() - 1; position >= 0; position--) {

                PhotoListItem item = result.get(position);
                mPhotoAdapter.insert(item, 0);
            }
            mPhotoAdapter.notifyDataSetChanged();
        } catch (Exception e) {
            Log.e(MainActivity.LOG_TAG, "Exception caught(tab_main_hall_photo---onRefreshCompleteTop): " + e.getMessage());
        }
    }

    private void onRefreshCompleteBottom(ArrayList<PhotoListItem> result) {
        if (result == null || result.isEmpty())
            return;
        try {
            for (PhotoListItem item : result) {
                mPhotoAdapter.add(item);
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
            Log.e(MainActivity.LOG_TAG, "Exception caught(tab_main_hall_photo---onRefreshCompleteBottom): " + e.getMessage());
        }
    }

    public ArrayList<PhotoListItem> getListFromJson(String strJson) {
        try {
            Gson gson = new Gson();

            Type typeOfObjects = new TypeToken<ArrayList<PhotoListItem>>() {
            }.getType();

            return gson.fromJson(strJson, typeOfObjects);
        } catch (Exception e) {
            Log.e(MainActivity.LOG_TAG, "Exception caught(tab_main_hall_photo---getListFromJson): " + e.getMessage());
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
            Log.e(MainActivity.LOG_TAG, "Exception caught(tab_main_hall_photo---getJsonFromObject): " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public void initList() {
        if (subFolder == null)
            return;
        new InitListTask().execute();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // for using different menu
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // clear the existing items, otherwise new item will be appended to it.
        menu.clear();
        if (bAllowNewPost)
            inflater.inflate(R.menu.menu_photo_tab, menu);
        else
            inflater.inflate(R.menu.menu_photo_tab_no_write_new_post, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    // detect when this fragment is visible
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if (BuildConfig.DEBUG) {
            if (isVisibleToUser) {
                Log.d(MainActivity.LOG_TAG, "tab_main_hall_photo becomes visible.");
            } else {
                Log.d(MainActivity.LOG_TAG, "tab_main_hall_photo becomes invisible.");
            }
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (BuildConfig.DEBUG)
            Log.d(MainActivity.LOG_TAG, "tab_main_hall_photo onStop.");
    }

    @Override
    public void onStart() {
        super.onStart();
        if (BuildConfig.DEBUG)
            Log.d(MainActivity.LOG_TAG, "tab_main_hall_photo onStart.");
    }

//    @Override
//    public void onAttach(Activity activity) {
//        super.onAttach(activity);
//        this.activity = activity;
//    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mImageFetcher != null)
            mImageFetcher.setExitTasksEarly(false);
        if (mPhotoAdapter != null)
            mPhotoAdapter.notifyDataSetChanged();
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
        //mImageFetcher = null;
    }

    private class InitListTask extends AsyncTask<Void, Void, ArrayList<PhotoListItem>> {
        @Override
        protected ArrayList<PhotoListItem> doInBackground(Void... params) {
            // read local file
            String strJson = KecUtilities.getTabLocalData(subFolder);

            ArrayList<PhotoListItem> items = null;
            if (strJson != null && !strJson.isEmpty()) {
                items = getListFromJson(strJson);
            }
            if (items != null && !items.isEmpty()) {
                return items;
            }else {
                Refresh(SwipeRefreshLayoutDirection.BOTH);
            }

            return null;
        }

        @Override
        protected void onPostExecute(ArrayList<PhotoListItem> result) {
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
            case R.id.photo_menu_item_add:
                // open a new activity, and get result here
                startNewPostActivity();
                return true;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void startNewPostActivity() {
        Intent intent = new Intent(getActivity(), NewPostActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        try {
            // do not finish, instead call startActivityForResult
            startActivityForResult(intent, MainActivity.NEW_POST_CODE);
            getActivity().overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
        } catch (Exception e) {
            Log.e(MainActivity.LOG_TAG, "Exception caught(tab_main_hall_photo---startNewPostActivity): " + e.getMessage());

        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == MainActivity.NEW_POST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                if (data != null) {
                    // 1. show in list first ?? maybe not now

                    // 2. send intent to async task
                    new UploadPostTask().execute(data);

                    // when task finish refresh top...
                }
            }
        }
    }


    // upload images and post to server
    @SuppressWarnings("deprecation")
    private class UploadPostTask extends AsyncTask<Intent, Void, Void> {
        // http://stackoverflow.com/questions/24239923/android-upload-image-and-json-using-multipartentitybuilder
        @Override
        protected Void doInBackground(Intent... params) {
            // 0 -- post can be null or empty
            // 1- 9 images
            Intent data = params[0];

            String strDesc = data.getStringExtra(MainActivity.POST_DESC);
            ArrayList<String> images = data.getStringArrayListExtra(MainActivity.POST_IMAGES);

            HttpClient httpClient = new DefaultHttpClient();
            //HttpPost httpPost = new HttpPost("http://192.168.9.23/testFingerPrint/recvjpg.php");
            HttpPost httpPost = new HttpPost("http://192.168.9.23:49573/receivemulti.ashx");

            try {
                MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create();
                multipartEntityBuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);

                if (strDesc != null && !TextUtils.isEmpty(strDesc))
                    //multipartEntityBuilder.addPart(MainActivity.POST_DESC, new StringBody(strDesc));
                    multipartEntityBuilder.addTextBody(MainActivity.POST_DESC, strDesc);


                for (String strImage : images) {
                    File file = new File(strImage);
                    multipartEntityBuilder.addPart(MainActivity.POST_IMAGES, new FileBody(file));
                }

                HttpEntity entity = multipartEntityBuilder.build();
                httpPost.setEntity(entity);

                HttpResponse response = httpClient.execute(httpPost);
                Log.i(MainActivity.LOG_TAG, "[http return --- status code: " + response.getStatusLine().getStatusCode() + ", message: " + EntityUtils.toString(response.getEntity()) + "]");
            } catch (NoSuchFieldError error) {
             Log.e(MainActivity.LOG_TAG, error.getMessage());

            } catch (Exception e) {
                Log.e(MainActivity.LOG_TAG, "Exception caught(tab_main_hall_photo---UploadPostTask---doInBackground): " + e.getMessage());
            }

            return null;
        }
    }
}