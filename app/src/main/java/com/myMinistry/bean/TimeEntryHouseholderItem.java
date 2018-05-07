package com.myMinistry.bean;

import android.database.Cursor;

import com.myMinistry.provider.MinistryContract;
import com.myMinistry.provider.MinistryDatabase;

import java.util.ArrayList;

public class TimeEntryHouseholderItem {
    private long id = MinistryDatabase.CREATE_ID;
    private String name;
    private String notes;
    private int notesID;
    private ArrayList<PlacedPublication> placedPublications = new ArrayList<>();

    public TimeEntryHouseholderItem() {

    }

    public TimeEntryHouseholderItem(Cursor cursor) {
        id = cursor.getLong(cursor.getColumnIndex(MinistryContract.Time._ID));
        name = cursor.getString(cursor.getColumnIndex(MinistryContract.Householder.NAME));
        /*
        entryTypdId = cursor.getLong(cursor.getColumnIndex(MinistryContract.Time.ENTRY_TYPE_ID));
        entryTypeName = cursor.getString(cursor.getColumnIndex(MinistryContract.UnionsNameAsRef.TITLE));

        startDateAndTime = Calendar.getInstance(Locale.getDefault());
        endDateAndTime = Calendar.getInstance(Locale.getDefault());

        try {
            startDateAndTime.setTime(TimeUtils.dbDateFormat.parse(cursor.getString(cursor.getColumnIndex(MinistryContract.Time.DATE_START))));
            String[] splitTime = cursor.getString(cursor.getColumnIndex(MinistryContract.Time.TIME_START)).split(":");
            startDateAndTime.set(Calendar.HOUR_OF_DAY, Integer.valueOf(splitTime[0]));
            startDateAndTime.set(Calendar.MINUTE, Integer.valueOf(splitTime[1]));
        } catch (Exception e) {
            startDateAndTime = Calendar.getInstance(Locale.getDefault());
        }

        try {
            endDateAndTime.setTime(TimeUtils.dbDateFormat.parse(cursor.getString(cursor.getColumnIndex(MinistryContract.Time.DATE_END))));
            String[] splitTime = cursor.getString(cursor.getColumnIndex(MinistryContract.Time.TIME_END)).split(":");
            endDateAndTime.set(Calendar.HOUR_OF_DAY, Integer.valueOf(splitTime[0]));
            endDateAndTime.set(Calendar.MINUTE, Integer.valueOf(splitTime[1]));
        } catch (Exception e) {
            endDateAndTime = Calendar.getInstance(Locale.getDefault());
        }*/
    }

    public void addPlacedPublication(Cursor cursor) {
        placedPublications.add(new PlacedPublication(cursor));
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public boolean isNew() { return id == MinistryDatabase.CREATE_ID; }
}
