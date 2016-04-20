package com.kectech.android.wyslink.activity;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.kectech.android.wyslink.BuildConfig;
import com.kectech.android.wyslink.R;
import com.kectech.android.wyslink.listeners.OnSwipeTouchListener;

public class AddShowroomActivity extends Activity {

    private EditText nameEdit;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_showroom);

        RelativeLayout scanLayout = (RelativeLayout) findViewById(R.id.layout_scan);
        if (scanLayout != null) {
            scanLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    nameEdit.setError(null);
                    final InputMethodManager imm1 = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
                    imm1.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    callScanApp();
                }
            });
        }

        RelativeLayout recommendLayout = (RelativeLayout) findViewById(R.id.layout_list);
        if (recommendLayout != null) {
            recommendLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    nameEdit.setError(null);
                    final InputMethodManager imm1 = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
                    imm1.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    startRecommendShowroomActivity();
                }
            });
        }

        OnSwipeTouchListener swipeTouchListener = new OnSwipeTouchListener() {
            public void onSwipeOutLeft() {
                close();
            }

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // hide soft keyboard when click non TextView area.
                // then determine if need  swipe out
                nameEdit.setError(null);
                final InputMethodManager imm1 = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
                imm1.hideSoftInputFromWindow(v.getWindowToken(), 0);
                return super.onTouch(v, event);
            }
        };

        final View view = findViewById(R.id.add_showroom_frame);
        view.setOnTouchListener(swipeTouchListener);

        nameEdit = (EditText) findViewById(R.id.edit_showroom);
        if (nameEdit != null ) {
            nameEdit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        String strName = nameEdit.getText().toString();
                        if (TextUtils.isEmpty(strName)) {
                            nameEdit.setError(getString(R.string.prompt_empty_showroom_name));
                        } else
                            returnToCaller(strName);
                        return true;
                    }
                    return false;
                }
            });
        }
    }

    private void callScanApp() {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.initiateScan();
    }

    // deal with scan result
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        if (scanResult != null) {
            String scanContent = scanResult.getContents();

            if (scanContent != null) {
                if (BuildConfig.DEBUG) {
                    Log.d(MainActivity.LOG_TAG, "QR Scan Content: " + scanContent);
//                    IntentIntegrator integrator = new IntentIntegrator(this);
//                    integrator.shareText(scanContent);
//                    return;
                }

                if (!scanContent.isEmpty()) {
//            // insert into ....
                    try {
                        // todo scan result, to be continued..., return to call fragment
                        // abstract the showroom name from url...
                        returnToCaller(scanContent);
                    } catch (Exception e) {
                        Log.e(MainActivity.LOG_TAG, "Exception caught(AddShowroomActivity---onActivityResult): " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
        }
        if (requestCode == MainActivity.ADD_SHOWROOM_CODE && resultCode == RESULT_OK) {
            String strNames = intent.getStringExtra(MainActivity.SHOWROOM_NAME);
            returnToCaller(strNames);
        }

    }

    private void close() {
        finish();
        overridePendingTransition(R.anim.enter_from_left, R.anim.exit_to_right);
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

    private void returnToCaller(String strName) {
        Intent intent = new Intent();
        try {
            intent.putExtra(MainActivity.SHOWROOM_NAME, strName);
            setResult(RESULT_OK, intent);
            close();
        } catch (Exception e) {
            Log.e(MainActivity.LOG_TAG, "Exception caught(ChooseImageActivity---complete): " + e.getMessage());
            close();
        }
    }

    private void startRecommendShowroomActivity() {
        Intent intent = new Intent(this, RecommendShowroomActivity.class);
        try {
            startActivityForResult(intent, MainActivity.ADD_SHOWROOM_CODE);
            overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
        } catch (Exception e) {
            Log.e(MainActivity.LOG_TAG, "Exception caught(AddShowroomActivity---startRecommendShowRoomActivity): " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            //case R.id.menu_item_quit:
            case android.R.id.home: {
                close();
                return true;
            }
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
