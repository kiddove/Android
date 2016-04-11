package com.kectech.android.wyslink.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.share.Sharer;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;
import com.kectech.android.wyslink.BuildConfig;
import com.kectech.android.wyslink.R;


import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

import io.vov.vitamio.LibsChecker;
import io.vov.vitamio.MediaPlayer;
import io.vov.vitamio.widget.MediaController;
import io.vov.vitamio.widget.VideoView;

public class VideoViewActivity extends Activity {
    private VideoView videoView;
    private String videoURL;

    // for test facebook sdk
    private CallbackManager callbackManager;
    private ShareDialog shareDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        if (BuildConfig.DEBUG)
//        {
//            System.gc();
//        }
        // TODO: 29/03/2016 StrictMode policy violation try to do it async 
        if (!LibsChecker.checkVitamioLibs(this))
            return;
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

        // rtmp will cause fb sdk -- href is not properly formatted, error code 100
        videoURL = "http://206.190.133.140/VideoStorage/kiddove_gmail_com/1449593792775_D.mp4";
        setContentView(R.layout.activity_videoview);

        Intent intent = getIntent();
        Uri contentUri = null;
        if (intent != null) {
            try {
                videoURL = intent.getStringExtra(MainActivity.BUNDLE_KEY_CONTENT_URL);
                if (videoURL != null)
                    contentUri = Uri.parse(videoURL);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else
            videoURL = null;

        videoView = (VideoView) findViewById(R.id.myVideo);
        View mProgress = findViewById(R.id.loading_progress);
        videoView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                videoView.updateControl();
            }
        });
        if (contentUri == null) {
            String vidAddress = "http://206.190.133.140/VideoStorage/kiddove_gmail_com/1449593792775_D.mp4";
            //String vidAddress = "http://192.168.9.13/video.mp4";
            videoView.setVideoPath(vidAddress);
        } else
            videoView.setVideoURI(contentUri);
        videoView.setMediaBufferingIndicator(mProgress);

        videoView.setMediaController(new MediaController(this));

        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.setPlaybackSpeed(1.0f);
                mp.setLooping(true);
                mp.setUseCache(true);
            }
        });

        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {

            }
        });
    }

    // use back button to navigate backward
    @Override
    public boolean onKeyDown(int keyCode, @NonNull KeyEvent event) {
        // check if the key event was the Back button and if there's history
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_BACK:
                    close();
                    //return super.onKeyDown(keyCode, event);
                    return true;
            }
        }

        // If it wasn't the Back key or there's no web page history, bubble up to the default
        // system behavior (probably exit the activity)
        return super.onKeyDown(keyCode, event);
    }

    private void close() {
        finish();
        overridePendingTransition(R.anim.enter_from_left, R.anim.exit_to_right);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case android.R.id.home: {
                close();
                return true;
            }
            case R.id.video_share_facebook: {
                shareViaFacebook("Remarkable video", "go check it out.");
                return true;
            }
            case R.id.video_share_twitter: {
                shareViaTwitter("What an amazing video.");
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_video_view_activity, menu);
        return true;
    }

    private void shareViaTwitter(String text) {
        if (videoURL == null || TextUtils.isEmpty(videoURL))
            return;
        // Create intent using ACTION_VIEW and a normal Twitter url:
        String tweetUrl = String.format("https://twitter.com/intent/tweet?text=%s&url=%s",
                urlEncode(text),
                urlEncode(videoURL));
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(tweetUrl));

        // Narrow down to official Twitter app, if available:
        List<ResolveInfo> matches = getPackageManager().queryIntentActivities(intent, 0);
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

    private void shareViaFacebook(String title, String description) {
        if (videoURL == null || TextUtils.isEmpty(videoURL))
            return;
        if (ShareDialog.canShow(ShareLinkContent.class)) {
            ShareLinkContent linkContent = new ShareLinkContent.Builder()
                    .setContentTitle(title)
                    .setContentDescription(description)
                    .setContentUrl(Uri.parse("http://www.wyslink.com"))
                    .build();

            if (shareDialog.canShow(linkContent, ShareDialog.Mode.NATIVE)) {
                shareDialog.show(linkContent, ShareDialog.Mode.NATIVE);
            } else if (shareDialog.canShow(linkContent, ShareDialog.Mode.WEB)) {
                shareDialog.show(linkContent, ShareDialog.Mode.WEB);
            } else
                shareDialog.show(linkContent, ShareDialog.Mode.AUTOMATIC);
        }
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {

        callbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }
}
