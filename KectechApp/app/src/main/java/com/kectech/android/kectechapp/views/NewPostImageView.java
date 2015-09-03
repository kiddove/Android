package com.kectech.android.kectechapp.views;

import android.content.Context;
import android.util.AttributeSet;

import com.kectech.android.kectechapp.thirdparty.CacheBitmap.RecyclingImageView;

/**
 * Created by Paul on 28/08/2015.
 * used in new post activity, has a boolean value to tell if the imageView is set some image or default
 */
public class NewPostImageView extends RecyclingImageView {

    public NewPostImageView(Context context) {
        super(context);
    }

    // must have this~!!
    public NewPostImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    //public boolean bDefault = true;
    public int position= 0;
}
