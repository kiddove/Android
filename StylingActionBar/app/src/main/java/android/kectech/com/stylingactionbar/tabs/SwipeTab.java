package android.kectech.com.stylingactionbar.tabs;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.kectech.com.stylingactionbar.R;
import android.kectech.com.stylingactionbar.view.SwipeRefreshLayoutBasicFragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Paul on 16/06/2015.
 */
public class SwipeTab extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.swipe, container, false);

        if (savedInstanceState == null) {
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            SwipeRefreshLayoutBasicFragment fragment = new SwipeRefreshLayoutBasicFragment();
            transaction.replace(R.id.sample_content_fragment, fragment);
            transaction.commit();
        }
        return v;
    }
}