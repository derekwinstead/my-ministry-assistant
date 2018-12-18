package com.myMinistry.bean;

import com.myMinistry.utils.AppConstants;

import androidx.annotation.NonNull;

public class Publisher {
    @NonNull
    private long id;
    @NonNull
    private String name;
    @NonNull
    private int isActive;
    @NonNull
    private String gender;
    @NonNull
    private int isDefault;

    public Publisher() {
        this.id = AppConstants.CREATE_ID;
        this.name = "";
        this.isActive = AppConstants.ACTIVE;
        this.gender = "male";
        this.isDefault = AppConstants.INACTIVE;
    }

    public Publisher(@NonNull String name, @NonNull int isActive, @NonNull String gender, @NonNull int isDefault) {
        this.id = AppConstants.CREATE_ID;
        this.name = name;
        this.isActive = isActive;
        this.gender = gender;
        this.isDefault = isDefault;
    }

    public Publisher(@NonNull long id, @NonNull String name, @NonNull int isActive, @NonNull String gender, @NonNull int isDefault) {
        this.id = id;
        this.name = name;
        this.isActive = isActive;
        this.gender = gender;
        this.isDefault = isDefault;
    }

    public boolean isNew() {
        return this.id == AppConstants.CREATE_ID;
    }

    public long getId() {
        return this.id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isActive() {
        return this.isActive != 0;
    }

    public void setIsActive(int isActive) {
        this.isActive = isActive;
    }

    public String getGender() {
        return this.gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public boolean isDefault() {
        return this.isDefault != 0;
    }
}