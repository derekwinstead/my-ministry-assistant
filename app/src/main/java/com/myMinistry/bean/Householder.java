package com.myMinistry.bean;

import com.myMinistry.utils.AppConstants;

import androidx.annotation.NonNull;

public class Householder {
    @NonNull
    private long id;
    @NonNull
    private String name;
    private String address;
    private String phoneMobile;
    private String phoneHome;
    private String phoneWork;
    private String phoneOther;
    @NonNull
    private int isActive;
    @NonNull
    private int isDefault;

    public Householder() {
        this.id = AppConstants.CREATE_ID;
        this.name = "";
        this.address = "";
        this.phoneMobile = "";
        this.phoneHome = "";
        this.phoneWork = "";
        this.phoneOther = "";
        this.isActive = AppConstants.ACTIVE;
        this.isDefault = AppConstants.INACTIVE;
    }

    public Householder(@NonNull String name, String address, String phoneMobile, String phoneHome, String phoneWork, String phoneOther, @NonNull int isActive, @NonNull int isDefault) {
        this.id = AppConstants.CREATE_ID;
        this.name = name;
        this.address = address;
        this.phoneMobile = phoneMobile;
        this.phoneHome = phoneHome;
        this.phoneWork = phoneWork;
        this.phoneOther = phoneOther;
        this.isActive = isActive;
        this.isDefault = isDefault;
    }

    public Householder(@NonNull long id, @NonNull String name, String address, String phoneMobile, String phoneHome, String phoneWork, String phoneOther, @NonNull int isActive, @NonNull int isDefault) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.phoneMobile = phoneMobile;
        this.phoneHome = phoneHome;
        this.phoneWork = phoneWork;
        this.phoneOther = phoneOther;
        this.isActive = isActive;
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

    public String getAddress() {
        return this.address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhoneMobile() {
        return this.phoneMobile;
    }

    public void setPhoneMobile(String phoneMobile) {
        this.phoneMobile = phoneMobile;
    }

    public String getPhoneHome() {
        return this.phoneHome;
    }

    public void setPhoneHome(String phoneHome) {
        this.phoneHome = phoneHome;
    }

    public String getPhoneWork() {
        return this.phoneWork;
    }

    public void setPhoneWork(String phoneWork) {
        this.phoneWork = phoneWork;
    }

    public String getPhoneOther() {
        return this.phoneOther;
    }

    public void setPhoneOther(String phoneOther) {
        this.phoneOther = phoneOther;
    }

    public boolean isActive() {
        return this.isActive != 0;
    }

    public void setIsActive(int isActive) {
        this.isActive = isActive;
    }

    public boolean isDefault() {
        return this.isDefault != 0;
    }
/*








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