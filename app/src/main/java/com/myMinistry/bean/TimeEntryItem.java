package com.myMinistry.bean;

import android.database.Cursor;

import com.myMinistry.provider.MinistryContract;
import com.myMinistry.provider.MinistryDatabase;
import com.myMinistry.util.TimeUtils;

import java.util.Calendar;
import java.util.Locale;

public class TimeEntryItem {
    private long id, entryTypdId = MinistryDatabase.CREATE_ID;
    private Calendar startDateAndTime, endDateAndTime;
    private String entryTypeName;
    public String cleaned;

    public TimeEntryItem(Cursor cursor) {
        id = cursor.getLong(cursor.getColumnIndex(MinistryContract.Time._ID));
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
        }
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public boolean isNew() { return id == MinistryDatabase.CREATE_ID; }

    public long getEntryTypeId() { return entryTypdId; }
    public void setEntryTypeId(long entryTypdId) { this.entryTypdId = entryTypdId; }

    public String getEntryTypeName() { return entryTypeName; }
    public void setEntryTypeName(String name) { this.entryTypeName = name; }

    public Calendar getStartDateAndTime() { return startDateAndTime; }
    public Calendar getEndDateAndTime() { return endDateAndTime; }

    public void setCal(String value) { this.cleaned = value; }
}
