package com.kectech.android.wyslink.preferences;

import android.content.Context;
import android.preference.Preference;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import com.kectech.android.wyslink.R;
import com.kectech.android.wyslink.activity.MainActivity;

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
            tLabel.setText(getContext().getString(R.string.title_activity_account));

        final TextView tUser = (TextView)view.findViewById(R.id.account_setting_user);
        if (tUser != null)
            tUser.setText(MainActivity.USER);
    }
}
