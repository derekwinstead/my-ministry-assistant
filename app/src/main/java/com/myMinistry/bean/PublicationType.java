package com.myMinistry.bean;

import com.myMinistry.utils.AppConstants;

import androidx.annotation.NonNull;

public class PublicationType {
    @NonNull
    private long id;
    @NonNull
    private String name;
    @NonNull
    private int isActive;
    @NonNull
    private int isDefault;

    public PublicationType() {
        this.id = AppConstants.CREATE_ID;
        this.name = "";
        this.isActive = AppConstants.ACTIVE;
        this.isDefault = AppConstants.INACTIVE;
    }

    public PublicationType(@NonNull String name, @NonNull int isActive, @NonNull int isDefault) {
        this.id = AppConstants.CREATE_ID;
        this.name = name;
        this.isActive = isActive;
        this.isDefault = isDefault;
    }

    public PublicationType(@NonNull long id, @NonNull String name, @NonNull int isActive, @NonNull int isDefault) {
        this.id = id;
        this.name = name;
        this.isActive = isActive;
        this.isDefault = isDefault;
    }

    public boolean isNew() {
        return this.id == AppConstants.CREATE_ID;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
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

    public boolean isDefault() {
        return isDefault != 0;
    }

    public void setIsDefault(int isDefault) {
        this.isDefault = isDefault;
    }
}