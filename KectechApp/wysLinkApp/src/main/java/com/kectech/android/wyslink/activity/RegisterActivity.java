package com.kectech.android.wyslink.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.kectech.android.wyslink.BuildConfig;
import com.kectech.android.wyslink.R;
import com.kectech.android.wyslink.listeners.OnSwipeTouchListener;
import com.kectech.android.wyslink.thirdparty.CacheBitmap.ImageFetcher;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * A Register screen that offers register via email/password/nickname.
 * sing up
 */
public class RegisterActivity extends Activity {

    /**
     * Keep track of the register task to ensure we can cancel it if requested.
     */
    private UserActionTask mAuthTask = null;

    private boolean bValidUserName = false;
    // UI references.
    private EditText mEmailView;
    private EditText mPasswordView;
    private EditText mConfirmPasswordView;
    private EditText mNickNameView;
    private View mProgressView;
    private View mRegisterFormView;

    // 1 -- email existed
    private int ERROR_CODE = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
//        if (BuildConfig.DEBUG) {
//            System.gc();
//        }
        // when click done on confirm
        mConfirmPasswordView = (EditText) findViewById(R.id.confirm_password);
        mConfirmPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.register || id == EditorInfo.IME_NULL || id == EditorInfo.IME_ACTION_DONE) {
                    // the action key performs a "done" operation, typically meaning there is nothing more to input and the IME will be closed.
                    // but seems "OK" is for "done", done is for Enter...
                    // IME_NULL for Enter...
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_NULL || id == EditorInfo.IME_ACTION_NEXT) {
                    // the action key performs a "done" operation, typically meaning there is nothing more to input and the IME will be closed.
                    // but seems OK. is for "done", done is for Enter...
                    // IME_NULL for Enter...

                    String password = mPasswordView.getText().toString();
                    // Check for a valid password, if the user entered one.
                    if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
                        mPasswordView.setError(getString(R.string.error_invalid_password));
                        mPasswordView.requestFocus();
                        return true;
                    }
                }
                return false;
            }
        });

        mEmailView = (EditText) findViewById(R.id.email);
        mEmailView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_NULL || id == EditorInfo.IME_ACTION_NEXT) {
                    // the action key performs a "done" operation, typically meaning there is nothing more to input and the IME will be closed.
                    // but seems OK. is for "done", done is for Enter...
                    // IME_NULL for Enter...

                    String email = mEmailView.getText().toString();
                    // Check for a valid password, if the user entered one.
                    if (TextUtils.isEmpty(email)) {
//                        mEmailView.setError(getString(R.string.error_invalid_email));
//                        mEmailView.requestFocus();
                        return false;
                    } else if (!isEmailValid(email)) {
                        mEmailView.setError(getString(R.string.error_invalid_email));
                        mEmailView.requestFocus();
                        return true;
                    }
                    // check user...
                    checkEmailAddress();
                }
                return false;
            }
        });

        Button mEmailSignUpButton = (Button) findViewById(R.id.register_button);
        mEmailSignUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mRegisterFormView = findViewById(R.id.register_form);
        mProgressView = findViewById(R.id.login_progress);
        mNickNameView = (EditText) findViewById(R.id.nick_name);

        OnSwipeTouchListener swipeTouchListener = new OnSwipeTouchListener() {
            public void onSwipeOutLeft() {
                //getFragmentManager().popBackStack();
                close(true);
            }

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // hide soft keyboard when click non TextView area.
                // then determine if need  swipe out
                final InputMethodManager imm1 = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
                imm1.hideSoftInputFromWindow(v.getWindowToken(), 0);
                return super.onTouch(v, event);
            }
        };

        mRegisterFormView.setOnTouchListener(swipeTouchListener);

        TextView goBackToLogIn = (TextView) findViewById(R.id.alreadyMember);
        goBackToLogIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final InputMethodManager imm1 = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
                imm1.hideSoftInputFromWindow(v.getWindowToken(), 0);
                close(true);
            }
        });

        // set textWatcher, to clear error
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                mEmailView.setError(null);
                mPasswordView.setError(null);
                mConfirmPasswordView.setError(null);
                mNickNameView.setError(null);
            }
        };
        mEmailView.addTextChangedListener(textWatcher);

        mPasswordView.addTextChangedListener(textWatcher);

        mConfirmPasswordView.addTextChangedListener(textWatcher);

        mNickNameView.addTextChangedListener(textWatcher);
    }

    private void checkEmailAddress() {
        if (mAuthTask != null || bValidUserName) {
            return;
        }

        String email = mEmailView.getText().toString();

        mAuthTask = new UserActionTask();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
            // allow async task to run simultaneously
            mAuthTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, email);
        else
            mAuthTask.execute(email);
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    public void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }
        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        //hide keyboard
        final InputMethodManager imm1 = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm1.hideSoftInputFromWindow(mRegisterFormView.getWindowToken(), 0);

        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();
        String nickname = mNickNameView.getText().toString();

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            mEmailView.requestFocus();
            return;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            mEmailView.requestFocus();
            return;
        }

        // check nickname
        if (TextUtils.isEmpty(nickname)) {
            mNickNameView.setError(getString(R.string.error_field_required));
            mNickNameView.requestFocus();
            return;
        }

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(password)) {
            mPasswordView.setError(getString(R.string.error_field_required));
            mPasswordView.requestFocus();
            return;
        } else if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            mPasswordView.requestFocus();
            return;
        }

        // Check password match
        if (!isPasswordMatch()) {
            mConfirmPasswordView.setError(getString(R.string.error_different_password));
            mConfirmPasswordView.requestFocus();
            return;
        }

        // Show a progress spinner, and kick off a background task to
        // perform the user login attempt.
        showProgress(true);
        mAuthTask = new UserActionTask();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
            // allow async task to run simultaneously
            mAuthTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, email, password, nickname);
        else
            mAuthTask.execute(email, password, nickname);
    }

    private boolean isEmailValid(String email) {
        int length = email.length();
        int i1 = email.lastIndexOf("@");
        int i2 = email.lastIndexOf(".");
        // has "." & "@"
        // "." appear after "@"
        // "." is not the last
        // "." is not right behind "@"
        return i1 >= 0 && i2 - i1 > 1 && i2 != length - 1;
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 4;
    }

    private boolean isPasswordMatch() {
        return mConfirmPasswordView.getText().toString().compareTo(mPasswordView.getText().toString()) == 0;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mRegisterFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mRegisterFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mRegisterFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mRegisterFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserActionTask extends AsyncTask<String, Void, Boolean> {

        private int type = 0;

        @Override
        protected Boolean doInBackground(String... params) {
            // param[0] -- email
            // param[1] -- password
            // param[2] -- nick name, if register
            type = params.length;
            try {
                if (params.length == 3)
                    return registerNewUser(params);
                else if (params.length == 1) {
                    bValidUserName = checkUser(params);
                    return bValidUserName;
                }
            } catch (Exception e) {
                Log.e(MainActivity.LOG_TAG, "Exception: " + e.getMessage());
                e.printStackTrace();
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if (type == 3) {
                if (success) {
                    startMainActivity();
                } else {
                    if (ERROR_CODE == 1) {
                        mEmailView.setError(getString(R.string.error_existed_email));
                        mEmailView.requestFocus();
                    }
                }
            } else if (type == 1) {
                if (!success) {
                    mEmailView.setError(getString(R.string.error_existed_email));
                    mEmailView.requestFocus();
                }
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }

    private void startMainActivity() {

        // actually is go back to login activity and then go to main activity from login
        Intent intent = new Intent();
        intent.putExtra(MainActivity.CURRENT_USER_KEY, mEmailView.getText().toString());
        try {
            setResult(RESULT_OK, intent);
            close(false);
        } catch (Exception e) {
            Log.e(MainActivity.LOG_TAG, "Exception caught(RegisterActivity): " + e.getMessage());

        }
    }

    private boolean registerNewUser(String... params) {
        // length should be 3
        if (params.length != 3)
            return false;
        ERROR_CODE = 0;
        ImageFetcher.disableConnectionReuseIfNecessary();
        HttpURLConnection urlConnection = null;
        BufferedInputStream in = null;
        try {
            //final String urlString = "http://www.kdlinx.com/registor.ashx?handleType=4&email=xxx&pwd=xxx&nickname=xxx";
            //final String urlString = "http://198.105.216.190/registor.ashx?handleType=4&email=" + params[0] + "&pwd=" + params[1] + "&nickname=" + params[2];
            final String urlString = "http://206.190.141.88/registor.ashx?handleType=4&email=" + params[0] + "&pwd=" + params[1] + "&nickname=" + params[2];
            final URL url = new URL(urlString);
            urlConnection = (HttpURLConnection) url.openConnection();
            in = new BufferedInputStream(urlConnection.getInputStream(), MainActivity.DOWNLOAD_BUFFER);

            // byte array to store input
            byte[] contents = new byte[1024];
            int bytesRead;
            if ((bytesRead = in.read(contents)) != -1) {
                String s = new String(contents, 0, bytesRead);
                if (s.compareToIgnoreCase("true") == 0) {
                    ERROR_CODE = 0;
                    return true;
                } else if (s.compareToIgnoreCase("existed") == 0) {
                    ERROR_CODE = 1;
                    return false;
                }
            }

            return false;
        } catch (final IOException e) {
            Log.e(MainActivity.LOG_TAG, "Error in register new user - " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            try {
                if (in != null) {
                    in.close();
                }
            } catch (final IOException e) {
                Log.e(MainActivity.LOG_TAG, e.getMessage());
                e.printStackTrace();
            }
        }
        return false;
    }

    private boolean checkUser(String... params) {
        // length should be 1
        if (params.length != 1)
            return false;
        ImageFetcher.disableConnectionReuseIfNecessary();
        HttpURLConnection urlConnection = null;
        BufferedInputStream in = null;
        try {
            //final String urlString = "http://www.kdlinx.com/registor.ashx?handleType=4&email=xxx&pwd=xxx&nickname=xxx";
            //final String urlString = "http://198.105.216.190/registor.ashx?handleType=1&email=" + params[0];
            final String urlString = "http://206.190.141.88/registor.ashx?handleType=1&email=" + params[0];
            final URL url = new URL(urlString);
            urlConnection = (HttpURLConnection) url.openConnection();
            in = new BufferedInputStream(urlConnection.getInputStream(), MainActivity.DOWNLOAD_BUFFER);

            // byte array to store input
            byte[] contents = new byte[1024];
            int bytesRead;
            if ((bytesRead = in.read(contents)) != -1) {
                String s = new String(contents, 0, bytesRead);
                return s.compareToIgnoreCase("false") == 0;
            }
        } catch (final IOException e) {
            Log.e(MainActivity.LOG_TAG, "Error in check user - " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            try {
                if (in != null) {
                    in.close();
                }
            } catch (final IOException e) {
                Log.e(MainActivity.LOG_TAG, e.getMessage());
                e.printStackTrace();
            }
        }
        return false;
    }

    // use back button to navigate backward
    @Override
    public boolean onKeyDown(int keyCode, @NonNull KeyEvent event) {
        // check if the key event was the Back button and if there's history
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_BACK:
                    close(true);
                    //return super.onKeyDown(keyCode, event);
                    return true;
            }
        }

        // If it wasn't the Back key or there's no web page history, bubble up to the default
        // system behavior (probably exit the activity)
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case android.R.id.home: {
                close(true);
                return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void close(boolean bBackward) {
        finish();
        if (bBackward)
            overridePendingTransition(R.anim.enter_from_left, R.anim.exit_to_right);
        else
            overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
    }
}

