package com.myMinistry.bean;

import com.myMinistry.provider.MinistryDatabase;

public class TimeEntry {
    private long id = MinistryDatabase.CREATE_ID;
    private String name = "DEREK!";

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public boolean isNew() {
        return id == MinistryDatabase.CREATE_ID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
