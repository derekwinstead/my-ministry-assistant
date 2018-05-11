package com.myMinistry.ui.report;

import com.myMinistry.utils.AppConstants;

public class ReportSummaryPublicationItem {
    private long id = AppConstants.CREATE_ID;
    private String name;
    private int count;

    public ReportSummaryPublicationItem(String name, int count) {
        this.name = name;
        this.count = count;
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

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public void setCount(int count) {
        this.count = count;
    }
    public int getCount() {
        return this.count;
    }
/*
    public String toString() {
        return this.name;
    }
    */
}