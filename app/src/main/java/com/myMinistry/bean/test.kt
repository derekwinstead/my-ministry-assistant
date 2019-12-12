package com.myMinistry.bean

import com.myMinistry.utils.AppConstants

data class tester(val id: Long, val name: String, val isActive: Int, val gender: String, val isDefault: Int)



class test {
    var id: Long = 0
    var name: String
    private var isActive: Int = 0
    var gender: String
    private var isDefault: Int = 0

    val isNew: Boolean
        get() = this.id == AppConstants.CREATE_ID.toLong()

    constructor(id: Long = AppConstants.CREATE_ID.toLong(), name: String = "", isActive: Int = AppConstants.ACTIVE, gender: String = "male", isDefault: Int = AppConstants.INACTIVE) {
        this.id = id
        this.name = name
        this.isActive = isActive
        this.gender = gender
        this.isDefault = isDefault
    }

    fun isActive(): Boolean {
        return this.isActive != 0
    }

    fun setIsActive(isActive: Int) {
        this.isActive = isActive
    }

    fun isDefault(): Boolean {
        return this.isDefault != 0
    }
}