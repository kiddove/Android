package com.kectech.android.wyslink.listitem;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.kectech.android.wyslink.R;

import java.util.ArrayList;

/**
 * Created by Paul on 14/04/2016.
 * List Item for Me tab
 */
public class MeListItem {
    private String title = null;
    private String description = null;

    public String getDescription() {
        return description;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
