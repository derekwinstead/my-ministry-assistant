package com.myMinistry.model;

public class NavDrawerMenuItem {
    public String title;
    public int iconRes;
    public int id;
    public int is_active = 0;
    public int is_default = 0;

    public NavDrawerMenuItem(String title, int iconRes) {
        this.title = title;
        this.iconRes = iconRes;
    }

    public NavDrawerMenuItem(String title, int iconRes, int id) {
        this.title = title;
        this.iconRes = iconRes;
        this.id = id;
    }

    public NavDrawerMenuItem(String title, int iconRes, int id, int is_active) {
        this.title = title;
        this.iconRes = iconRes;
        this.id = id;
        this.is_active = is_active;
    }

    public NavDrawerMenuItem(String title, int iconRes, int id, int is_active, int is_default) {
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

    public int getIsActive() {
        return is_active;
    }

    public void setIsDefault(int var) {
        is_default = var;
    }

    public int getIsDefault() {
        return is_default;
    }
    */
}