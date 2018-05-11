package com.myMinistry.ui.householders;

import android.database.Cursor;

import com.myMinistry.provider.MinistryContract;

public class HouseholderItem {
    public int id;
    public String name;
    public String date;
    public String last_active_string;

    public HouseholderItem(Cursor cursor) {
        this.id = cursor.getInt(cursor.getColumnIndex(MinistryContract.Householder._ID));
        this.name = cursor.getString(cursor.getColumnIndex(MinistryContract.UnionsNameAsRef.TITLE));
        this.date = cursor.getString(cursor.getColumnIndex(MinistryContract.UnionsNameAsRef.DATE));
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLastActiveString() {
        return this.last_active_string;
    }

    public void setLastActiveString(String last_active_string) {
        this.last_active_string = last_active_string;
    }

    public int getID() {
        return this.id;
    }

    public String getDate() {
        return this.date;
    }

    public String getName() {
        return name;
    }
}