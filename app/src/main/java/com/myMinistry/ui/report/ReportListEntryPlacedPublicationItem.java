package com.myMinistry.ui.report;

import android.database.Cursor;

import com.myMinistry.provider.MinistryContract;
import com.myMinistry.utils.AppConstants;

public class ReportListEntryPlacedPublicationItem {
    private long id = AppConstants.CREATE_ID;
    private long publisherId, publicationNameId, publicationTypeId, householderId, timeId;
    private int count;
    private String date_palced;
    private String householderName, publicationName;
    private int isActive = AppConstants.ACTIVE;
    private int weight = 1;

    public ReportListEntryPlacedPublicationItem(Cursor cursor) {
        this.householderName = cursor.getString(cursor.getColumnIndex(MinistryContract.Householder.NAME));
        this.publicationName = cursor.getString(cursor.getColumnIndex(MinistryContract.UnionsNameAsRef.TITLE));
        this.count = cursor.getInt(cursor.getColumnIndex(MinistryContract.LiteraturePlaced.COUNT));
    }

    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }
    public boolean isNew() {
        return id == AppConstants.CREATE_ID;
    }

    public String getHouseholderName() { return householderName; }
    public void setHouseholderName(String name) { this.householderName = name; }

    public String getPublicationName() { return publicationName; }
    public void setPublicationName(String name) { this.publicationName = name; }

    public boolean isActive() { return isActive != 0; }
    public void setIsActive(int isActive) { this.isActive = isActive; }
    public void setIsActive(boolean isActive) { this.isActive = (isActive) ? 1 : 0; }

    public int getWeight() { return weight; }
    public void setWeight(int weight) { this.weight = weight; }

    public int getCount() { return count; }
    public void setCount(int count) { this.count = count; }
}