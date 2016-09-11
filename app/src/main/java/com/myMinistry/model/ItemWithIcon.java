package com.myMinistry.model;

public class ItemWithIcon {
    public String title;
    public int iconRes;
    public int id;
    public int is_active = 0;
    public int is_default = 0;
    public int count = 0;

    public ItemWithIcon(String title, int iconRes) {
        this.title = title;
        this.iconRes = iconRes;
    }

    public ItemWithIcon(int id, String title, int count) {
        this.title = title;
        this.count = count;
        this.id = id;
    }

    /*
        public ItemWithIcon(String _title, int _iconRes, int _id) {
            title = _title;
            iconRes = _iconRes;
            id = _id;
        }

        public ItemWithIcon(String _title, int _iconRes, int _id, int _is_active) {
            title = _title;
            iconRes = _iconRes;
            id = _id;
            is_active = _is_active;
        }
    */
    public ItemWithIcon(String title, int iconRes, int id, int is_active, int is_default) {
        this.title = title;
        this.iconRes = iconRes;
        this.id = id;
        this.is_active = is_active;
        this.is_default = is_default;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setIconRes(int iconRes) {
        this.iconRes = iconRes;
    }

    public String toString() {
        return title;
    }

    public int getID() {
        return id;
    }

    /*
        public void setIsActive(int var) {
            is_active = var;
        }
    */
    public int getIsActive() {
        return is_active;
    }

    /*
        public void setIsDefault(int var) {
            is_default = var;
        }
    */
    public int getIsDefault() {
        return is_default;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getCount() {
        return String.valueOf(this.count);
    }
}