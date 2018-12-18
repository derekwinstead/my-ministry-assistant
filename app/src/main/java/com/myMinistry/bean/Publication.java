package com.myMinistry.bean;

import com.myMinistry.utils.AppConstants;

import androidx.annotation.NonNull;

public class Publication {
    @NonNull
    private long id;
    @NonNull
    private long typeId;
    @NonNull
    private String name;
    @NonNull
    private int isActive;
    @NonNull
    private int weight;

    public Publication() {
        this.id = AppConstants.CREATE_ID;
        this.typeId = AppConstants.CREATE_ID;
        this.name = "";
        this.isActive = AppConstants.ACTIVE;
        this.weight = 1;
    }

    public Publication(@NonNull long typeId, @NonNull String name, @NonNull int isActive, @NonNull int weight) {
        this.id = AppConstants.CREATE_ID;
        this.typeId = typeId;
        this.name = name;
        this.isActive = isActive;
        this.weight = weight;
    }

    public Publication(@NonNull long id, @NonNull long typeId, @NonNull String name, @NonNull int isActive, @NonNull int weight) {
        this.id = id;
        this.typeId = typeId;
        this.name = name;
        this.isActive = isActive;
        this.weight = weight;
    }

    public boolean isNew() {
        return id == AppConstants.CREATE_ID;
    }

    public long getId() {
        return this.id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getTypeId() {
        return this.typeId;
    }

    public void setTypeId(long typeId) {
        this.typeId = typeId;
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

    public int getWeight() {
        return this.weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    /*



    public void setIsActive(boolean isActive) {
        this.isActive = (isActive) ? 1 : 0;
    }


    */
}