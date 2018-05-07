package com.myMinistry.bean;

import android.database.Cursor;

import com.myMinistry.provider.MinistryContract;
import com.myMinistry.provider.MinistryDatabase;
import com.myMinistry.util.TimeUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class TimeEntryItem {
    private long id, entryTypdId = MinistryDatabase.CREATE_ID;
    private Calendar startDateAndTime, endDateAndTime;
    private String entryTypeName;
    //private ArrayList<>  = ArrayList<>;
    private ArrayList<TimeEntryHouseholderItem> entry_householder_and_placements = new ArrayList<>();

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

    public void setEntryHouseholderItems(Cursor cursor) {
        entry_householder_and_placements.clear();
        int old_householder_id = MinistryDatabase.CREATE_ID - 1; // Creating a number that will not exist for initial comparisons
        TimeEntryHouseholderItem householderItem;

        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            // A new householder
            if (cursor.getInt(cursor.getColumnIndex(MinistryContract.TimeHouseholder.HOUSEHOLDER_ID)) != old_householder_id) {
                /*
                // If there is already a an existing item we need to add it to the ArrayList before resetting the
                if(entry_householder_and_placements.size() > 0) {
                    entry_householder_and_placements.add(householderItem);
                }
                */
                old_householder_id = cursor.getInt(cursor.getColumnIndex(MinistryContract.TimeHouseholder.HOUSEHOLDER_ID));


                householderItem = new TimeEntryHouseholderItem(cursor);
                entry_householder_and_placements.add(householderItem);
            }

            entry_householder_and_placements.get(entry_householder_and_placements.size() - 1).addPlacedPublication(cursor);
        }

        //householderItem
        //TimeEntryHouseholderItem
        //PlacedPublication
                /*
                ********************TimeEntryHouseholderItem*******************
                 private long id = MinistryDatabase.CREATE_ID;
    private String name;
    private String notes;
    private int notesID;
    private ArrayList<Publication> publications_placed = new ArrayList<>();
                 */

    }


//        entryItems = database.fetchHouseholderAndPlacedPublicationsByTimeId(timeEntryItem.getId());
/*
        // Load up the array list for the adapter
        for(entries.moveToFirst(); !entries.isAfterLast(); entries.moveToNext()) {
            TimeEntryItem timeEntryItem = new TimeEntryItem(entries);
            entryItems = database.fetchHouseholderAndPlacedPublicationsByTimeId(timeEntryItem.getId());
            timeEntryItem.setEntryHouseholderItems(entryItems);
            time_entries_arraylist.add(timeEntryItem);


        for(entryItems.moveToFirst(); !entryItems.isAfterLast(); entryItems.moveToNext()) {

        }
*/


    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public boolean isNew() { return id == MinistryDatabase.CREATE_ID; }

    public long getEntryTypeId() { return entryTypdId; }
    public void setEntryTypeId(long entryTypdId) { this.entryTypdId = entryTypdId; }

    public String getEntryTypeName() { return entryTypeName; }
    public void setEntryTypeName(String name) { this.entryTypeName = name; }

    public Calendar getStartDateAndTime() { return startDateAndTime; }
    public Calendar getEndDateAndTime() { return endDateAndTime; }
}
