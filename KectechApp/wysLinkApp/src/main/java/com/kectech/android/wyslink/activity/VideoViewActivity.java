package com.kectech.android.wyslink.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;

import com.kectech.android.kectechapp.R;

import io.vov.vitamio.LibsChecker;
import io.vov.vitamio.MediaPlayer;
import io.vov.vitamio.utils.Log;
import io.vov.vitamio.widget.MediaController;
import io.vov.vitamio.widget.VideoView;

public class VideoViewActivity extends Activity {
    private VideoView videoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!LibsChecker.checkVitamioLibs(this))
            return;
        setContentView(R.layout.activity_videoview);

        Intent intent = getIntent();
        Uri contentUri = null;
        if (intent != null) {
            try {
                String strURL = intent.getStringExtra(MainActivity.BUNDLE_KEY_CONTENT_URL);
                 if (strURL != null)
                    contentUri = Uri.parse(strURL);
            } catch (Exception e) {
                Log.e("%s", e.getMessage());
            }
        }

        videoView = (VideoView)findViewById(R.id.myVideo);
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
        }
        else
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
                    return super.onKeyDown(keyCode, event);
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
        }
        return super.onOptionsItemSelected(item);
    }
}
