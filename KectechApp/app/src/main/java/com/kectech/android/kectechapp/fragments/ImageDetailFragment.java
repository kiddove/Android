/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kectech.android.kectechapp.fragments;

import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.kectech.android.kectechapp.R;
import com.kectech.android.kectechapp.activity.PhotoOfHallOfMainActivity;
import com.kectech.android.kectechapp.thirdparty.CacheBitmap.ImageWorker;
import com.kectech.android.kectechapp.thirdparty.CacheBitmap.Utils;
import com.kectech.android.kectechapp.thirdparty.ScaleImageView;
import com.kectech.android.kectechapp.thirdparty.universalimageloader.core.ImageLoader;
import com.kectech.android.kectechapp.thirdparty.universalimageloader.core.imageaware.ImageAware;
import com.kectech.android.kectechapp.thirdparty.universalimageloader.core.imageaware.ImageViewAware;


/**
 * This fragment will populate the children of the ViewPager from {@link com.kectech.android.kectechapp.activity.PhotoOfHallOfMainActivity}.
 */
public class ImageDetailFragment extends Fragment {
    private static final String IMAGE_DATA_EXTRA = "extra_image_data";
    private String mImageUrl;
    private ScaleImageView mImageView;
    //private ImageFetcher mImageFetcher;

    /**
     * Factory method to generate a new instance of the fragment given an image number.
     *
     * @param imageUrl The image url to load
     * @return A new instance of ImageDetailFragment with imageNum extras
     */
    public static ImageDetailFragment newInstance(String imageUrl) {
        final ImageDetailFragment f = new ImageDetailFragment();

        final Bundle args = new Bundle();
        args.putString(IMAGE_DATA_EXTRA, imageUrl);
        f.setArguments(args);

        return f;
    }

    /**
     * Empty constructor as per the Fragment documentation
     */
    public ImageDetailFragment() {}

    /**
     * Populate image using a url from extras, use the convenience factory method
     * {@link ImageDetailFragment#newInstance(String)} to create this fragment.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mImageUrl = getArguments() != null ? getArguments().getString(IMAGE_DATA_EXTRA) : null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        // Inflate and locate the main ImageView
        final View v = inflater.inflate(R.layout.photo_activity_image_fragment, container, false);
        mImageView = (ScaleImageView) v.findViewById(R.id.photo_activity_image_fragment_imageView);
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (PhotoOfHallOfMainActivity.class.isInstance(getActivity())) {
//            mImageFetcher = ((PhotoOfHallOfMainActivity) getActivity()).getImageFetcher();
//            mImageFetcher.loadImage(mImageUrl, mImageView);
            ImageAware imageAware = new ImageViewAware(mImageView, false);

            // TODO: 26/08/2015 separate from different ways, web sd card, content provider, asset, resource, to be continued...
            if (mImageUrl.contains("http://"))
                ImageLoader.getInstance().displayImage(mImageUrl, imageAware);
            else
                ImageLoader.getInstance().displayImage("file://" + mImageUrl, imageAware);
        }

        // Pass clicks on the ImageView to the parent activity to handle
        if (OnClickListener.class.isInstance(getActivity()) && Utils.hasHoneycomb()) {
            mImageView.setOnClickListener((OnClickListener) getActivity());
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
//        mImageFetcher = null;
        if (mImageView != null) {
            // Cancel any pending image work
            ImageWorker.cancelWork(mImageView);
            mImageView.setImageDrawable(null);
        }
    }
}
