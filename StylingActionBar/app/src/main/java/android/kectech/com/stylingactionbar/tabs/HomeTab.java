package android.kectech.com.stylingactionbar.tabs;

import android.app.Fragment;
import android.kectech.com.stylingactionbar.R;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Paul on 10/06/2015.
 * Home Tab or Subscriber Tab
 */
public class HomeTab extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v =inflater.inflate(R.layout.subscriber,container,false);
        return v;
    }
}
