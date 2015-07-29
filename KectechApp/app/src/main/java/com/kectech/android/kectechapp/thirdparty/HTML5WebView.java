package com.kectech.android.kectechapp.thirdparty;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.GeolocationPermissions;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;

import com.kectech.android.kectechapp.R;
import com.kectech.android.kectechapp.activity.MainActivity;

public class HTML5WebView extends WebView {
	
	private Context 							mContext;
	private MyWebChromeClient					mWebChromeClient;
	private View								mCustomView;
	private FrameLayout							mCustomViewContainer;
	private WebChromeClient.CustomViewCallback 	mCustomViewCallback;

	private FrameLayout							mLayout;
	
    static final String LOGTAG = "HTML5WebView";
	    
	private void init(Context context) {
		mContext = context;		
		Activity a = (Activity) mContext;
		
		mLayout = new FrameLayout(context);

		FrameLayout browserFrameLayout = (FrameLayout) LayoutInflater.from(a).inflate(R.layout.activity_video_html5, null);
		FrameLayout contentView = (FrameLayout) browserFrameLayout.findViewById(R.id.main_content);
		mCustomViewContainer = (FrameLayout) browserFrameLayout.findViewById(R.id.fullscreen_custom_content);
		
		mLayout.addView(browserFrameLayout, COVER_SCREEN_PARAMS);


	       
	    // Configure the webView
	    WebSettings s = getSettings();
	    //s.setBuiltInZoomControls(true);
	    s.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NARROW_COLUMNS);
	    s.setUseWideViewPort(true);
	    s.setLoadWithOverviewMode(true);
	    s.setSavePassword(true);
	    s.setSaveFormData(true);
	    s.setJavaScriptEnabled(true);

//	    // enable navigator.geolocation
//	    s.setGeolocationEnabled(true);
//	    s.setGeolocationDatabasePath("/data/data/org.itri.html5webview/databases/");

	    // enable Web Storage: localStorage, sessionStorage
	    s.setDomStorageEnabled(true);
        mWebChromeClient = new MyWebChromeClient();
        setWebChromeClient(mWebChromeClient);

        setWebViewClient(new MyWebViewClient());
	    contentView.addView(this);
	}

	public HTML5WebView(Context context) {
		super(context);
		init(context);
	}

	public HTML5WebView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public HTML5WebView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}
	
	public FrameLayout getLayout() {
		return mLayout;
	}
	
    public boolean inCustomView() {
		return (mCustomView != null);
	}
    
    public void hideCustomView() {
		mWebChromeClient.onHideCustomView();
	}
    
    @Override
    public boolean onKeyDown(int keyCode, @NonNull KeyEvent event) {
    	if (keyCode == KeyEvent.KEYCODE_BACK) {
    		if ((mCustomView == null) && canGoBack()){
    			goBack();
    			return true;
    		}
    	}
    	return super.onKeyDown(keyCode, event);
    }

    private class MyWebChromeClient extends WebChromeClient {
		private Bitmap 		mDefaultVideoPoster;
		private View 		mVideoProgressView;
    	
    	@Override
		public void onShowCustomView(View view, CustomViewCallback callback)
		{
			//Log.i(LOG_TAG, "here in on ShowCustomView");
	        HTML5WebView.this.setVisibility(View.GONE);
	        
	        // if a view already exists then immediately terminate the new one
	        if (mCustomView != null) {
	            callback.onCustomViewHidden();
	            return;
	        }
	        
	        mCustomViewContainer.addView(view);
	        mCustomView = view;
	        mCustomViewCallback = callback;
	        mCustomViewContainer.setVisibility(View.VISIBLE);
		}
		
		@Override
		public void onHideCustomView() {
			
			if (mCustomView == null)
				return;	       
			
			// Hide the custom view.
			mCustomView.setVisibility(View.GONE);
			
			// Remove the custom view from its container.
			mCustomViewContainer.removeView(mCustomView);
			mCustomView = null;
			mCustomViewContainer.setVisibility(View.GONE);
			mCustomViewCallback.onCustomViewHidden();
			
			HTML5WebView.this.setVisibility(View.VISIBLE);
			
	        //Log.i(LOG_TAG, "set it to webVew");
		}
		
		@Override
		public Bitmap getDefaultVideoPoster() {
			//Log.i(LOG_TAG, "here in on getDefaultVideoPoster");
            try {
                if (mDefaultVideoPoster == null) {
                    mDefaultVideoPoster = BitmapFactory.decodeResource(
                            getResources(), R.drawable.default_video_poster);
                }
            } catch (OutOfMemoryError ome) {
                Log.e(MainActivity.LOG_TAG, "load poster failed.(out of memory)");
            }
			return mDefaultVideoPoster;
		}
		
		@Override
		public View getVideoLoadingProgressView() {
			//Log.i(LOG_TAG, "here in on getVideoLoadingProgressView");
			
	        if (mVideoProgressView == null) {
	            LayoutInflater inflater = LayoutInflater.from(mContext);
	            mVideoProgressView = inflater.inflate(R.layout.video_loading_progress, null);
	        }
	        return mVideoProgressView; 
		}
    	
//    	 @Override
//         tab_main_public void onReceivedTitle(WebView view, String title) {
//            ((Activity) mContext).setTitle(title);
//         }

         @Override
         public void onProgressChanged(WebView view, int newProgress) {
        	 ((Activity) mContext).getWindow().setFeatureInt(Window.FEATURE_PROGRESS, newProgress*100);
         }
         
         @Override
         public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
             callback.invoke(origin, true, false);
         }
    }
	
	private class MyWebViewClient extends WebViewClient {
	    @Override
	    public boolean shouldOverrideUrlLoading(WebView view, String url) {
	    	Log.i(LOGTAG, "shouldOverrideUrlLoading: "+url);
	        view.loadUrl(url);
	        return true;
	    }
//        @Override
//        public void onPageFinished(WebView view, String url) {
//
//        }
    }
	
	static final FrameLayout.LayoutParams COVER_SCREEN_PARAMS =
        new FrameLayout.LayoutParams( ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
}