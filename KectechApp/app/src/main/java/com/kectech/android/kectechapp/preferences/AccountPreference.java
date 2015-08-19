package com.kectech.android.kectechapp.preferences;

import android.content.Context;
import android.preference.Preference;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import com.kectech.android.kectechapp.R;
import com.kectech.android.kectechapp.activity.MainActivity;

/**
 * Created by Paul on 12/08/2015.
 * preference for account setting
 */
public class AccountPreference extends Preference {
    public AccountPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    // set layout in xml
    @Override
    protected void onBindView(@NonNull View view) {
        super.onBindView(view);
        final TextView tLabel = (TextView)view.findViewById(R.id.account_setting_label);
        if (tLabel != null)
            tLabel.setText("Account Name");

        final TextView tUser = (TextView)view.findViewById(R.id.account_setting_user);
        if (tUser != null)
            tUser.setText(MainActivity.USER);
    }
}
