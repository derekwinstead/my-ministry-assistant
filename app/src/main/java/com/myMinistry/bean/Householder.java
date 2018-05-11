package com.myMinistry.bean;

import com.myMinistry.utils.AppConstants;

public class Householder {
    private long id = AppConstants.CREATE_ID;
    private String name = "";
    private String address = "";
    private String phoneMobile = "";
    private String phoneHome = "";
    private String phoneWork = "";
    private String phoneOther = "";
    private int isActive = AppConstants.ACTIVE;
    private int isDefault = AppConstants.INACTIVE;

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

    public boolean isActive() {
        return isActive != 0;
    }

    public void setIsActive(int isActive) {
        this.isActive = isActive;
    }

    public void setIsActive(boolean isActive) {
        this.isActive = (isActive) ? 1 : 0;
    }

    public boolean isDefault() {
        return isDefault != 0;
    }

    public void setIsDefault(int isDefault) {
        this.isDefault = isDefault;
    }

    public void setIsDefault(boolean isDefault) {
        this.isDefault = (isDefault) ? 1 : 0;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhoneMobile() {
        return phoneMobile;
    }

    public void setPhoneMobile(String phoneMobile) {
        this.phoneMobile = phoneMobile;
    }

    public String getPhoneHome() {
        return phoneHome;
    }

    public void setPhoneHome(String phoneHome) {
        this.phoneHome = phoneHome;
    }

    public String getPhoneWork() {
        return phoneWork;
    }

    public void setPhoneWork(String phoneWork) {
        this.phoneWork = phoneWork;
    }

    public String getPhoneOther() {
        return phoneOther;
    }

    public void setPhoneOther(String phoneOther) {
        this.phoneOther = phoneOther;
    }
}