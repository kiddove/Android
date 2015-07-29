package com.kectech.android.kectechapp.listeners;

import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;

import com.kectech.android.kectechapp.activity.MainActivity;

/**
 * Created by Paul on 03/07/2015.
 * for WebView in video activity
 */
public class OnSwipeTouchListener implements View.OnTouchListener {

    private final GestureDetector gestureDetector = new GestureDetector(new GestureListener());

    public boolean onTouch(final View view, final MotionEvent motionEvent) {
        view.onTouchEvent(motionEvent);
        return gestureDetector.onTouchEvent(motionEvent);
    }

    private final class GestureListener extends SimpleOnGestureListener {

        private static final int SWIPE_THRESHOLD = 100;
        private static final int SWIPE_VELOCITY_THRESHOLD = 100;

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            try {
                float diffY = e2.getY() - e1.getY();
                float diffX = e2.getX() - e1.getX();
                if (Math.abs(diffX) > Math.abs(diffY)) {
                    if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffX > 0) {
                            onSwipeOutLeft();
                            return false;
                        }
//                        } else {
//                            onSwipeLeft();
//                        }
                    }
                }
            } catch (Exception e) {
                Log.e(MainActivity.LOG_TAG, "Exception caught: " + e.getMessage());
                e.printStackTrace();
            }
            return true;
        }
    }

    public void onSwipeOutLeft() {
    }

//    public void onSwipeLeft() {
//    }

//    public GestureDetector getGestureDetector(){
//        return  gestureDetector;
//    }
}

