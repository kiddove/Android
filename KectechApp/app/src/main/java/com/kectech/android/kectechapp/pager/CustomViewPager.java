package com.kectech.android.kectechapp.pager;

import android.content.Context;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.kectech.android.kectechapp.listeners.OnSwipeOutListener;

/**
 * Created by Paul on 03/07/2015.
 * custom pager to implement swipe left go back to last activity
 */
public class CustomViewPager extends ViewPager {

    float mStartDragX;
    OnSwipeOutListener mOnSwipeOutListener;

    public CustomViewPager(Context context) {
        super(context);
    }

    public CustomViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setOnSwipeOutListener(OnSwipeOutListener listener) {
        mOnSwipeOutListener = listener;
    }

    private void onSwipeOutAtLeft() {
        if (mOnSwipeOutListener != null) {
            mOnSwipeOutListener.onSwipeOutAtLeft();
        }
    }
//
//    private void onSwipeOutAtEnd() {
//        if (mOnSwipeOutListener!=null) {
//            mOnSwipeOutListener.onSwipeOutAtEnd();
//        }
//    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getAction() & MotionEventCompat.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                mStartDragX = ev.getX();
                break;
        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {

        if (getCurrentItem() == 0 || getCurrentItem() == getAdapter().getCount() - 1) {
            final int action = ev.getAction();
            float x = ev.getX();
            switch (action & MotionEventCompat.ACTION_MASK) {
                case MotionEvent.ACTION_MOVE:
                    break;
                case MotionEvent.ACTION_UP:
                    if (getCurrentItem() == 0 && x > mStartDragX) {
                        onSwipeOutAtLeft();
                    }
//                    if (getCurrentItem()==getAdapter().getCount()-1 && x<mStartDragX){
//                        onSwipeOutAtEnd();
//                    }
                    break;
            }
        } else {
            mStartDragX = 0;
        }
        return super.onTouchEvent(ev);

    }
}
