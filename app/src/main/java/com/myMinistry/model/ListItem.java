package com.myMinistry.model;

public class ListItem {
    public String title;
    public String subtitle;
    public int iconRes;
    public int id;

    public ListItem(int id, int iconRes, String title, String subtitle) {
        this.id = id;
        this.iconRes = iconRes;
        this.title = title;
        this.subtitle = subtitle;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    /*
        public void setSubTitle(String _title) {
            title = _title;
        }
    */
    public void setIconRes(int iconRes) {
        this.iconRes = iconRes;
    }

    public String toString() {
        return title;
    }

    /*
        public String getSubTitle() {
            return subtitle;
        }
    */
    public int getID() {
        return id;
    }
}