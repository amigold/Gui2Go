package com.ami.gui2go.models;

public class Widget {
    private String tag;
    private int icon;

    public Widget(String tag, int icon) {
        this.setTag(tag);
        this.setIcon(icon);
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }
}
