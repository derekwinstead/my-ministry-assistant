package com.myministry.model;

public class ItemWithDate {
    public int id;
    public String date;
    public String title;
    public int count;

    public ItemWithDate(int id, String title, String date) {
        this.id = id;
        this.date = date;
        this.title = title;
    };

    public ItemWithDate(int id, String title, int count) {
        this.id = id;
        this.title = title;
        this.count = count;
    };

    public void setTitle(String title) {
        this.title = title;
    }

    public int getID() {
        return this.id;
    }

    public String getCount() {
        return String.valueOf(this.count);
    }

    public String getDate() {
        return this.date;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String toString() {
        return this.title;
    }
}