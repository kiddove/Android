package android.kectech.com.stylingactionbar;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.kectech.com.stylingactionbar.util.KecUtilities;
import android.kectech.com.stylingactionbar.view.ScaleImageView;
import android.kectech.com.stylingactionbar.data.DownLoadImageTask;
import android.media.Image;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Paul on 25/06/2015.
 * need to do
 * set background thumbnail first
 * then download the full image
 * do some research on intent param
 */
public class PhotoActivity extends Activity {

    private static final int imageCount = 1;
    private ViewPager viewPager;
    private List<View> viewList;
    private MyPagerAdapter adapter;
    private Activity context = null;
    //private String[] URLs = null;
    Bundle URLs = null;
    // for dots
    private ArrayList<View> dots;
    private int previous = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);

        context = this;

        // for using action bar back button
        getActionBar().setDisplayHomeAsUpEnabled(true);
        // hide the icon on the left side on action bar
        getActionBar().setDisplayShowHomeEnabled(false);

        // get url from where activate this activity (like in video tab click
        // receive the intent
        //String url = "http://192.168.9.40/demo/test.html";
        Intent intent = getIntent();
        if (intent != null) {
            URLs = intent.getExtras();
        }

        // for now only one image

        // init
        LayoutInflater layoutInflater = getLayoutInflater().from(this);
        viewList = new ArrayList<View>();
        dots = new ArrayList<View>();

        // dots
        LinearLayout dotsLayout = (LinearLayout)findViewById(R.id.photo_activity_dots_group);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(32, 32);
        params.setMargins(6, 0, 6, 0);

        try {
            for (int i = 0; i < imageCount; i++) {
                View v = layoutInflater.inflate(R.layout.photo_activity_image_fragment, null);
                // here load thumb first if has..
                ScaleImageView imageView = (ScaleImageView)v.findViewById(R.id.photo_activity_image_fragment_imageview);
                if (imageView != null) {
                    if (URLs != null) {
                        String strThumbURL = URLs.getString(MainActivity.PHOTO_TAB_THUMB_URL_KEY);
                        String thumbLocalPath = KecUtilities.getLoaclFilePathFromURL(strThumbURL, MainActivity.PHOTO_SUB_FOLDER, context);
                        Bitmap thumbBitmap = KecUtilities.ReadFileFromLocal(thumbLocalPath);
                        if (thumbBitmap != null) {
                            imageView.setImageBitmap(thumbBitmap);
                        }
                    }
                }
                viewList.add(v);

                // dots
                View dot = new View(this);
                dot.setLayoutParams(params);
                dot.setBackgroundResource(R.drawable.dot_normal);
                dotsLayout.addView(dot);
                dots.add(dot);
            }
        }
        catch (Exception e) {
            Log.e("yougemaoyong", e.getMessage());
            return;
        }


        // default select
        dots.get(0).setBackgroundResource(R.drawable.dot_selected);
        context = this;

        // pager
        viewPager = (ViewPager)findViewById(R.id.photo_activity_viewpager);
        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                setCurrentPage(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        adapter = new MyPagerAdapter(viewList);
        viewPager.setAdapter(adapter);
        //viewPager.setCurrentItem(0);
        setCurrentPage(0);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_video_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        switch (id) {
            case android.R.id.home: {
                //NavUtils.navigateUpFromSameTask(this);
                getFragmentManager().popBackStack();
                finish();
                return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }
        //return super.onOptionsItemSelected(item);
    }

    // use back button to navigate backword
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // check if the key event was the Back button and if there's history
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_BACK:
                    getFragmentManager().popBackStackImmediate();
                    finish();
                    return super.onKeyDown(keyCode, event);
            }
        }

        // If it wasn't the Back key or there's no web page history, bubble up to the default
        // system behavior (probably exit the activity)
        return super.onKeyDown(keyCode, event);
    }

    // page adapter
    private class MyPagerAdapter extends PagerAdapter {

        private List<View> mListView;

        public MyPagerAdapter(List<View> mListView) {
            super();
            this.mListView = mListView;
        }


        public void destroyItem(View arg0, int arg1, Object arg2) {
            // TODO Auto-generated method stub
            ((ViewGroup)arg0).removeView(mListView.get(arg1));
        }

        @Override
        public void finishUpdate(View arg0) {
            // TODO Auto-generated method stub

        }


        public int getCount() {
            // TODO Auto-generated method stub
            return mListView.size();
        }

        @Override
        public Object instantiateItem(View arg0, int arg1) {
            // TODO Auto-generated method stub
            ((ViewGroup)arg0).addView(mListView.get(arg1), 0);
            return mListView.get(arg1);
        }


        public boolean isViewFromObject(View arg0, Object arg1) {
            // TODO Auto-generated method stub
            return arg0==(arg1);
        }

        @Override
        public void restoreState(Parcelable arg0, ClassLoader arg1) {
            // TODO Auto-generated method stub

        }

        @Override
        public Parcelable saveState() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public void startUpdate(View arg0) {
            // TODO Auto-generated method stub

        }
    }
//    private Bitmap ReadFileFromLocal(String fileurl) {
//        Bitmap bitmap = null;
//        InputStream inputSteam = null;
//        String subfolder = "photo";
//
//        // use base64 to encode the url then use as filename store on local dir
//        byte[] data = null;
//        try {
//            data = fileurl.getBytes("UTF-8");
//        } catch (UnsupportedEncodingException usee) {
//            usee.printStackTrace();
//            return null;
//        }
//        String fileName = Base64.encodeToString(data, Base64.DEFAULT);
//        if (fileName != null) {
//            //FileOutputStream outputStream = context.openFileOutput(filePath, context.MODE_PRIVATE);
//            BitmapFactory.Options options = new BitmapFactory.Options();
//            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
////            File file = new File(context.getFilesDir() + File.separator
////                    + fileName);
//            File file = new File(context.getFilesDir() + File.separator
//                    + subfolder + File.separator + fileName);
//
//            if (file.exists()) {
//                bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
//            }
//        }
//
//        return bitmap;
//    }

    // set current
    public void setCurrentPage(int position) {
        dots.get(previous).setBackgroundResource(R.drawable.dot_normal);
        dots.get(position).setBackgroundResource(R.drawable.dot_selected);
        previous = position;

        TextView tv = (TextView) viewList.get(position).findViewById(R.id.photo_activity_image_fragment_textview);
        tv.setText("Image" + position);

        ScaleImageView imageView = (ScaleImageView) viewList.get(position).findViewById(R.id.photo_activity_image_fragment_imageview);
        // download image async
        //String url = String.format("http://173.236.36.10/cds/samples/pets/%02d.jpg", position + 1);
        String imageURL = URLs.getString(MainActivity.PHOTO_TAB_IMAGE_URL_KEY);
        String localPath = KecUtilities.getLoaclFilePathFromURL(imageURL, MainActivity.PHOTO_SUB_FOLDER, context);
        Bitmap bitmap = KecUtilities.ReadFileFromLocal(localPath);
        //Bitmap bitmap = null;
        if (imageView != null && bitmap != null) {
            imageView.setImageBitmap(bitmap);
        }else {
            new DownLoadImageTask(imageView, context).execute(imageURL);
        }
    }
}
