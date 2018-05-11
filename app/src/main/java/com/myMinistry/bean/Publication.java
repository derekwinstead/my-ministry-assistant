package com.myMinistry.bean;

import com.myMinistry.utils.AppConstants;

public class Publication {
    private long id = AppConstants.CREATE_ID;
    private long typeId = AppConstants.CREATE_ID;
    private String name = "";
    private int isActive = AppConstants.ACTIVE;
    private int weight = 1;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public boolean isNew() {
        return id == AppConstants.CREATE_ID;
    }

    public long getTypeId() {
        return typeId;
    }

    public void setTypeId(long typeId) {
        this.typeId = typeId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isActive() {
        return isActive != 0;
    }

    public void setIsActive(int isActive) {
        this.isActive = isActive;
    }

    public void setIsActive(boolean isActive) {
        this.isActive = (isActive) ? 1 : 0;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }
}