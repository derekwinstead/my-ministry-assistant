package com.myMinistry.model;

public class NavDrawerMenuItem {
    public String title;
    public int iconRes;
    public int id;
    public int is_active = 0;
    public int is_default = 0;

    public NavDrawerMenuItem(String _title, int _iconRes) {
        title = _title;
        iconRes = _iconRes;
    }

    public NavDrawerMenuItem(String _title, int _iconRes, int _id) {
        title = _title;
        iconRes = _iconRes;
        id = _id;
    }

    public NavDrawerMenuItem(String _title, int _iconRes, int _id, int _is_active) {
        title = _title;
        iconRes = _iconRes;
        id = _id;
        is_active = _is_active;
    }

    public NavDrawerMenuItem(String _title, int _iconRes, int _id, int _is_active, int _is_default) {
        title = _title;
        iconRes = _iconRes;
        id = _id;
        is_active = _is_active;
        is_default = _is_default;
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

    public void setIsActive(int var) {
        is_active = var;
    }

    public int getIsActive() {
        return is_active;
    }

    public void setIsDefault(int var) {
        is_default = var;
    }

    public int getIsDefault() {
        return is_default;
    }
}