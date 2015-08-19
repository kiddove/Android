package com.kectech.android.kectechapp.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.kectech.android.kectechapp.BuildConfig;
import com.kectech.android.kectechapp.R;
import com.kectech.android.kectechapp.thirdparty.CacheBitmap.ImageFetcher;
import com.kectech.android.kectechapp.thirdparty.CacheBitmap.Utils;
import com.kectech.android.kectechapp.util.KecUtilities;

/**
 * A login screen that offers login via email/password.
 * log in
 */
public class LoginActivity extends Activity {

    private UserLoginTask mAuthTask = null;

    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;
    private Set<String> username;
    private String current_user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (BuildConfig.DEBUG) {
            Utils.enableStrictMode();
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        new checkAutoLogInTask().execute();

//        boolean bAuto = getSharedPreferences(MainActivity.SHARED_PREFERENCE_KEY, MODE_PRIVATE).getBoolean(MainActivity.CURRENT_LOGIN_STATUS_KEY, false);
//        current_user = getSharedPreferences(MainActivity.SHARED_PREFERENCE_KEY, MODE_PRIVATE).getString(MainActivity.CURRENT_USER_KEY, "");
//        if (bAuto) {
//            startMainActivity();
//            return;
//        }

        // for autocomplete
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);
        new getAutoCompleteListTask().execute();
        //username = getSharedPreferences(MainActivity.SHARED_PREFERENCE_KEY, MODE_PRIVATE).getStringSet(MainActivity.USER_NAME_SET_KEY, null);
        // Set up the login form.
        //populateAutoComplete();

        //mEmailView.setText("paul@wyslink.com");
        //mEmailView.setText("kevin@kectech.com");

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL || id == EditorInfo.IME_ACTION_DONE) {
                    // the action key performs a "done" operation, typically meaning there is nothing more to input and the IME will be closed.
                    // but seems OK. is for "done", done is for Enter...
                    // IME_NULL for Enter...
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        TextView mSignUpButton = (TextView) findViewById(R.id.email_sign_up);
        mSignUpButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                startRegisterActivity();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);

        // hide soft keyboard when click non TextView area.
        mLoginFormView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final InputMethodManager imm1 = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
                imm1.hideSoftInputFromWindow(v.getWindowToken(), 0);
                return false;
            }
        });
    }

    private void populateAutoComplete() {
        //getLoaderManager().initLoader(0, null, this);
        if (username != null && username.size() > 0) {
            List<String> emailAddressCollection = new ArrayList<>(username);
                    ArrayAdapter < String > adapter =
                            new ArrayAdapter<>(LoginActivity.this,
                                    android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

            mEmailView.setAdapter(adapter);
        }
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
//        // Reset errors.
//        mEmailView.setError(null);
//        mPasswordView.setError(null);
//
        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(password)) {
            mPasswordView.setError(getString(R.string.error_field_required));
            focusView = mPasswordView;
            cancel = true;
        } else if (!isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuthTask = new UserLoginTask();
            mAuthTask.execute(email, password);
        }


        //hide keyboard
        final InputMethodManager imm1 = (InputMethodManager)getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm1.hideSoftInputFromWindow(mLoginFormView.getWindowToken(), 0);
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

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
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
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<String, Void, Boolean> {

        @Override
        protected Boolean doInBackground(String... params) {
            // param[0] -- email
            // param[1] -- password
            // param[2] -- nick name, if register
            try {
                return userLogIn(params);
            } catch (Exception e) {
                Log.e(MainActivity.LOG_TAG, "Exception: " + e.getMessage());
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if (success) {
                new saveStateTask().execute(mEmailView.getText().toString());
            } else {
                mPasswordView.setError(getString(R.string.error_incorrect_password));
                mPasswordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }

    private void startMainActivity() {

        Intent intent = new Intent(this, MainActivity.class);

        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra(MainActivity.CURRENT_USER, current_user);
        try {
            startActivity(intent);
            finish();
            overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
        } catch (Exception e) {
            Log.e(MainActivity.LOG_TAG, "Exception caught: " + e.getMessage());

        }
    }

    private boolean userLogIn(String... params) {
        // length should be 2
        if (params.length != 2)
            return false;
        ImageFetcher.disableConnectionReuseIfNecessary();
        HttpURLConnection urlConnection = null;
        BufferedInputStream in = null;
        try {
            //final String urlString = "http://www.kdlinx.com/registor.ashx?handleType=4&email=xxx&pwd=xxx&nickname=xxx";
            final String urlString = "http://198.105.216.190/registor.ashx?handleType=20&username=" + params[0] + "&pwd=" + params[1];
            final URL url = new URL(urlString);
            urlConnection = (HttpURLConnection) url.openConnection();
            in = new BufferedInputStream(urlConnection.getInputStream(), MainActivity.DOWNLOAD_BUFFER);

            // byte array to store input
            byte[] contents = new byte[1024];
            int bytesRead;
            if ((bytesRead = in.read(contents)) != -1) {
                String s = new String(contents, 0, bytesRead);
                return s.compareToIgnoreCase("true") == 0;
            }

            return true;
        } catch (final IOException e) {
            Log.e(MainActivity.LOG_TAG, "Error in log in - " + e.getMessage());
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

    private void startRegisterActivity() {
        Intent intent = new Intent(this, RegisterActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        try {
            // do not finish, instead call startActivityForResult
            startActivityForResult(intent, MainActivity.REGISTER_REQUEST_CODE);
            overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
        } catch (Exception e) {
            Log.e(MainActivity.LOG_TAG, "Exception caught: " + e.getMessage());

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == MainActivity.REGISTER_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                if (data != null) {
                    // set email then new task
                    //current_user = data.getStringExtra(MainActivity.CURRENT_USER);
                    mEmailView.setText(data.getStringExtra(MainActivity.CURRENT_USER));
                    new saveStateTask().execute(mEmailView.getText().toString());
//                    Intent intent = new Intent(this, MainActivity.class);
//                    intent.putExtra(MainActivity.CURRENT_USER, data.getStringExtra(MainActivity.CURRENT_USER));
//                    intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
//                    try {
//                        startActivity(intent);
//                        finish();
//                        overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
//                    } catch (Exception e) {
//                        Log.e(MainActivity.LOG_TAG, "Exception caught: " + e.getMessage());
//
//                    }
                }
            }
//            else if (resultCode == RESULT_CANCELED) {
//                // do nothing
//            }
        }
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("StrictMode", "destroy login activity.");
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d("StrictMode", "LA start.");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d("StrictMode", "LA stop.");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("StrictMode", "LA resume.");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d("StrictMode", "LA pause.");
    }

    @Override
    public void onRestart() {
        super.onRestart();
        Log.d("StrictMode", "LA restart.");
    }


    public class checkAutoLogInTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                boolean bAuto = getSharedPreferences(MainActivity.SHARED_PREFERENCE_KEY, MODE_PRIVATE).getBoolean(MainActivity.CURRENT_LOGIN_STATUS_KEY, false);
                current_user = getSharedPreferences(MainActivity.SHARED_PREFERENCE_KEY, MODE_PRIVATE).getString(MainActivity.CURRENT_USER_KEY, "");
                return bAuto;

            } catch (Exception e) {
                Log.e(MainActivity.LOG_TAG, e.getMessage());
                e.printStackTrace();
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean autoLogIn) {
            if (autoLogIn)
                startMainActivity();
        }

        @Override
        protected void onCancelled() {
        }
    }

    public class saveStateTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {
            try {
                //current_user = mEmailView.getText().toString();
                current_user = params[0];
                // test
                if (!TextUtils.isEmpty(current_user)) {
                    SharedPreferences userDetails = getSharedPreferences(MainActivity.SHARED_PREFERENCE_KEY, MODE_PRIVATE);
                    if (username == null)
                        username = new HashSet<>();
                    username.add(current_user);
                    SharedPreferences.Editor editor = userDetails.edit();
                    editor.putString(MainActivity.CURRENT_USER_KEY, current_user);
                    editor.putBoolean(MainActivity.CURRENT_LOGIN_STATUS_KEY, true);
                    editor.putStringSet(MainActivity.USER_NAME_SET_KEY, username);
                    editor.apply();
                }
            } catch (Exception e) {
                Log.e(MainActivity.LOG_TAG, e.getMessage());
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            startMainActivity();
        }

        @Override
        protected void onCancelled() {
        }
    }

    public class getAutoCompleteListTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            try {
                username = getSharedPreferences(MainActivity.SHARED_PREFERENCE_KEY, MODE_PRIVATE).getStringSet(MainActivity.USER_NAME_SET_KEY, null);

            } catch (Exception e) {
                Log.e(MainActivity.LOG_TAG, e.getMessage());
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            populateAutoComplete();
        }

        @Override
        protected void onCancelled() {
        }
    }

    // use back button to navigate backward
    @Override
    public boolean onKeyDown(int keyCode, @NonNull KeyEvent event) {
        // check if the key event was the Back button and if there's history
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_BACK:
                    KecUtilities.closeCache();
                    finish();
                    System.exit(0);
                    return super.onKeyDown(keyCode, event);
            }
        }

        // If it wasn't the Back key or there's no web page history, bubble up to the default
        // system behavior (probably exit the activity)
        return super.onKeyDown(keyCode, event);
    }
}

