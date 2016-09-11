package com.myMinistry.model;

public class ListItem {
    public String title;
    public String subtitle;
    public int iconRes;
    public int id;

    public ListItem(int _id, int _iconRes, String _title, String _subtitle) {
        id = _id;
        iconRes = _iconRes;
        title = _title;
        subtitle = _subtitle;
    }

    public void setTitle(String _title) {
        title = _title;
    }

    public void setSubTitle(String _title) {
        title = _title;
    }

    public void setIconRes(int _iconRes) {
        iconRes = _iconRes;
    }

    public String toString() {
        return title;
    }

    public String getSubTitle() {
        return subtitle;
    }

    public int getID() {
        return id;
    }
}