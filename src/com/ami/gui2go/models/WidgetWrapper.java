package com.ami.gui2go.models;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.ami.gui2go.R;

public class WidgetWrapper {
    private TextView tag = null;
    private ImageView icon = null;
    private View row = null;

    public WidgetWrapper(View row) {
        this.row = row;
    }

    TextView getName() {
        if (tag == null) {
            tag = (TextView) row.findViewById(R.id.name);
        }
        return (tag);
    }

    ImageView getFlag() {
        if (icon == null) {
            icon = (ImageView) row.findViewById(R.id.flag);
        }
        return (icon);
    }

    public void populateFrom(Widget item) {
        getName().setText(item.getTag());
        getFlag().setImageResource(item.getIcon());

    }

}