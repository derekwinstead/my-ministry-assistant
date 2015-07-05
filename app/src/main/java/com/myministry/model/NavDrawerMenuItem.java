package com.myministry.model;

public class NavDrawerMenuItem {
    public String title;
    public int iconRes;
    public int id;

    public NavDrawerMenuItem(String _title, int _iconRes) {
        title = _title;
        iconRes = _iconRes;
    };

    public NavDrawerMenuItem(String _title, int _iconRes, int _id) {
        title = _title;
        iconRes = _iconRes;
        id = _id;
    }

    public void setTitle(String _title) {
        title = _title;
    }

    public void setIconRes(int _iconRes) {
        iconRes = _iconRes;
    }

    public String toString() {
        return title;
    }

    public int getID() {
        return id;
    }
}