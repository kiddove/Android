package com.kectech.android.wyslink.preferences;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.preference.Preference;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.kectech.android.wyslink.R;
import com.kectech.android.wyslink.activity.MainActivity;

/**
 * Created by Paul on 10/08/2015.
 * for show current app version in setting
 */
public class VersionPreference extends Preference {
    // this constructor is really important
    // DO NOT use VersionPreference(Context context) instead
    // will cause inflating error
    public VersionPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        final PackageManager packageManager = context.getPackageManager();
        String versionName = "Demo";
        if (packageManager != null) {
            try {
                PackageInfo packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
                versionName = packageInfo.versionName;
            } catch (PackageManager.NameNotFoundException e) {
                Log.e(MainActivity.LOG_TAG, "package name not found.");
            }
        }

        setSummary(versionName);
    }

    @Override
    protected void onBindView(@NonNull View view) {
        super.onBindView(view);
        final TextView t1 = (TextView)view.findViewById(R.id.current_version1);
        if (t1 != null)
            t1.setText(getContext().getString(R.string.is_up_to_date));
    }
}
