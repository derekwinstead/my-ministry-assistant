package com.myMinistry.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.myMinistry.bean.Householder;
import com.myMinistry.provider.MinistryContract;
import com.myMinistry.provider.MinistryDatabase;
import com.myMinistry.utils.AppConstants;

import java.util.ArrayList;
import java.util.List;

public class HouseholderDAO {
    // Database fields
    private SQLiteDatabase database;
    private MinistryDatabase dbHelper;
    public static final String TABLE_NAME = "householders";

    public HouseholderDAO(Context context) {
        dbHelper = new MinistryDatabase(context.getApplicationContext());
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public long create(Householder bean) {
        open();

        ContentValues values = new ContentValues();
        values.put(MinistryContract.Householder.NAME, bean.getName());
        values.put(MinistryContract.Householder.ADDR, bean.getAddress());
        values.put(MinistryContract.Householder.MOBILE_PHONE, bean.getPhoneMobile());
        values.put(MinistryContract.Householder.HOME_PHONE, bean.getPhoneHome());
        values.put(MinistryContract.Householder.WORK_PHONE, bean.getPhoneWork());
        values.put(MinistryContract.Householder.OTHER_PHONE, bean.getPhoneOther());
        values.put(MinistryContract.Householder.ACTIVE, bean.isActive() ? AppConstants.ACTIVE : AppConstants.INACTIVE);
        values.put(MinistryContract.Householder.DEFAULT, bean.isDefault() ? AppConstants.ACTIVE : AppConstants.INACTIVE);

        long id = database.insert(TABLE_NAME, null, values);
        close();
        return id;
    }

    public boolean deleteHouseholder(Householder bean) {
        // TODO: Delete all associated records from other tables too.
        open();
        long id = bean.getId();
        int affectedRows = database.delete(TABLE_NAME, MinistryContract.Householder._ID + " = ?", new String[]{id + ""});
        close();
        return affectedRows > 0;
    }

    public List<Householder> getAllHouseholders() {
        open();
        List<Householder> beanList = new ArrayList<>();
        Cursor cursor = database.query(TABLE_NAME, MinistryContract.Householder.All_COLS, null, null, null, null, MinistryContract.Householder.DEFAULT_SORT);
        while (cursor.moveToNext()) {
            Householder bean = cursorToHouseholder(cursor);
            beanList.add(bean);
        }
        // make sure to close the cursor
        cursor.close();
        close();
        return beanList;
    }

    public Householder getHouseholder(int id) {
        open();
        Householder bean;
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE " + MinistryContract.Householder._ID + " =  " + id;
        Cursor cursor = database.rawQuery(sql, null);
        if (cursor.moveToNext()) {
            bean = cursorToHouseholder(cursor);
        } else {
            bean = new Householder();
        }
        cursor.close();
        close();
        return bean;
    }

    public void update(Householder bean) {
        open();
        ContentValues values = new ContentValues();
        values.put(MinistryContract.Householder.NAME, bean.getName());
        values.put(MinistryContract.Householder.ADDR, bean.getAddress());
        values.put(MinistryContract.Householder.MOBILE_PHONE, bean.getPhoneMobile());
        values.put(MinistryContract.Householder.HOME_PHONE, bean.getPhoneHome());
        values.put(MinistryContract.Householder.WORK_PHONE, bean.getPhoneWork());
        values.put(MinistryContract.Householder.OTHER_PHONE, bean.getPhoneOther());
        values.put(MinistryContract.Householder.ACTIVE, bean.isActive() ? AppConstants.ACTIVE : AppConstants.INACTIVE);
        values.put(MinistryContract.Householder.DEFAULT, bean.isDefault() ? AppConstants.ACTIVE : AppConstants.INACTIVE);

        database.update(TABLE_NAME, values, MinistryContract.Householder._ID + " = ?", new String[]{bean.getId() + ""});
        close();
    }

    private Householder cursorToHouseholder(Cursor cursor) {
        Householder bean = new Householder();
        bean.setId(cursor.getLong(cursor.getColumnIndex(MinistryContract.Householder._ID)));
        bean.setName(cursor.getString(cursor.getColumnIndex(MinistryContract.Householder.NAME)));
        bean.setAddress(cursor.getString(cursor.getColumnIndex(MinistryContract.Householder.ADDR)));
        bean.setPhoneMobile(cursor.getString(cursor.getColumnIndex(MinistryContract.Householder.MOBILE_PHONE)));
        bean.setPhoneHome(cursor.getString(cursor.getColumnIndex(MinistryContract.Householder.HOME_PHONE)));
        bean.setPhoneWork(cursor.getString(cursor.getColumnIndex(MinistryContract.Householder.WORK_PHONE)));
        bean.setPhoneOther(cursor.getString(cursor.getColumnIndex(MinistryContract.Householder.OTHER_PHONE)));
        bean.setIsActive(cursor.getInt(cursor.getColumnIndex(MinistryContract.Householder.ACTIVE)));
        bean.setIsDefault(cursor.getInt(cursor.getColumnIndex(MinistryContract.Householder.DEFAULT)));
        return bean;
    }

    public void deleteAll() {
        open();
        database.delete(TABLE_NAME, null, null);
        close();
    }
}