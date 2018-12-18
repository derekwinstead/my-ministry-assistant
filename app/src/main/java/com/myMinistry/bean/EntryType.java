package com.myMinistry.bean;

import com.myMinistry.utils.AppConstants;

import androidx.annotation.NonNull;

public class EntryType {
    @NonNull
    private long id;
    @NonNull
    private String name;
    @NonNull
    private int isActive;
    @NonNull
    private int isDefault;

    public EntryType() {
        this.id = AppConstants.CREATE_ID;
        this.name = "";
        this.isActive = AppConstants.ACTIVE;
        this.isDefault = AppConstants.INACTIVE;
    }

    public EntryType(@NonNull String name, @NonNull int isActive, @NonNull int isDefault) {
        this.id = AppConstants.CREATE_ID;
        this.name = name;
        this.isActive = isActive;
        this.isDefault = isDefault;
    }

    public EntryType(@NonNull long id, @NonNull String name, @NonNull int isActive, @NonNull int isDefault) {
        this.id = id;
        this.name = name;
        this.isActive = isActive;
        this.isDefault = isDefault;
    }

    public long getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public boolean isActive() {
        return this.isActive != 0;
    }

    public boolean isDefault() {
        return this.isDefault != 0;
    }

    /*
    public void setId(long id) {
        this.id = id;
    }

    public boolean isNew() {
        return id == AppConstants.CREATE_ID;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setIsActive(int isActive) {
        this.isActive = isActive;
    }

    public void setIsActive(boolean isActive) {
        this.isActive = (isActive) ? 1 : 0;
    }

    public void setIsDefault(int isDefault) {
        this.isDefault = isDefault;
    }

    public void setIsDefault(boolean isDefault) {
        this.isDefault = (isDefault) ? 1 : 0;
    }
    */
}