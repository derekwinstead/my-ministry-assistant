package com.myMinistry.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.myMinistry.bean.Householder;
import com.myMinistry.provider.MinistryContract;
import com.myMinistry.provider.MinistryDatabase;
import com.myMinistry.provider.MinistryService;

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

    public long create(Householder householder) {
        open();

        ContentValues values = new ContentValues();
        values.put(MinistryContract.Householder.NAME, householder.getName());
        values.put(MinistryContract.Householder.ADDR, householder.getAddress());
        values.put(MinistryContract.Householder.MOBILE_PHONE, householder.getPhoneMobile());
        values.put(MinistryContract.Householder.HOME_PHONE, householder.getPhoneHome());
        values.put(MinistryContract.Householder.WORK_PHONE, householder.getPhoneWork());
        values.put(MinistryContract.Householder.OTHER_PHONE, householder.getPhoneOther());
        values.put(MinistryContract.Householder.ACTIVE, householder.isActive() ? MinistryService.ACTIVE : MinistryService.INACTIVE);
        values.put(MinistryContract.Householder.DEFAULT, householder.isDefault() ? MinistryService.ACTIVE : MinistryService.INACTIVE);

        long id = database.insert(TABLE_NAME, null, values);
        close();
        return id;
    }

    public boolean deleteHouseholder(Householder householder) {
        // TODO: Delete all associated records from other tables too.
        open();
        long id = householder.getId();
        int affectedRows = database.delete(TABLE_NAME, MinistryContract.Householder._ID + " = ?", new String[]{id + ""});
        close();
        return affectedRows > 0;
    }

    public List<Householder> getAllHouseholders() {
        open();
        List<Householder> householderList = new ArrayList<>();
        Cursor cursor = database.query(TABLE_NAME, MinistryContract.Householder.All_COLS, null, null, null, null, MinistryContract.Householder.DEFAULT_SORT);
        while (cursor.moveToNext()) {
            Householder householder = cursorToHouseholder(cursor);
            householderList.add(householder);
        }
        // make sure to close the cursor
        cursor.close();
        close();
        return householderList;
    }

    public Householder getHouseholder(int id) {
        open();
        Householder householder;
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE " + MinistryContract.Householder._ID + " =  " + id;
        Cursor cursor = database.rawQuery(sql, null);
        if (cursor.moveToNext()) {
            householder = cursorToHouseholder(cursor);
        } else {
            householder = new Householder();
        }
        cursor.close();
        close();
        return householder;
    }

    public void update(Householder householder) {
        open();
        ContentValues values = new ContentValues();
        values.put(MinistryContract.Householder.NAME, householder.getName());
        values.put(MinistryContract.Householder.ADDR, householder.getAddress());
        values.put(MinistryContract.Householder.MOBILE_PHONE, householder.getPhoneMobile());
        values.put(MinistryContract.Householder.HOME_PHONE, householder.getPhoneHome());
        values.put(MinistryContract.Householder.WORK_PHONE, householder.getPhoneWork());
        values.put(MinistryContract.Householder.OTHER_PHONE, householder.getPhoneOther());
        values.put(MinistryContract.Householder.ACTIVE, householder.isActive() ? MinistryService.ACTIVE : MinistryService.INACTIVE);
        values.put(MinistryContract.Householder.DEFAULT, householder.isDefault() ? MinistryService.ACTIVE : MinistryService.INACTIVE);

        database.update(TABLE_NAME, values, MinistryContract.Householder._ID + " = ?", new String[]{householder.getId() + ""});
        close();
    }

    private Householder cursorToHouseholder(Cursor cursor) {
        Householder householder = new Householder();
        householder.setId(cursor.getLong(cursor.getColumnIndex(MinistryContract.Householder._ID)));
        householder.setName(cursor.getString(cursor.getColumnIndex(MinistryContract.Householder.NAME)));
        householder.setAddress(cursor.getString(cursor.getColumnIndex(MinistryContract.Householder.ADDR)));
        householder.setPhoneMobile(cursor.getString(cursor.getColumnIndex(MinistryContract.Householder.MOBILE_PHONE)));
        householder.setPhoneHome(cursor.getString(cursor.getColumnIndex(MinistryContract.Householder.HOME_PHONE)));
        householder.setPhoneWork(cursor.getString(cursor.getColumnIndex(MinistryContract.Householder.WORK_PHONE)));
        householder.setPhoneOther(cursor.getString(cursor.getColumnIndex(MinistryContract.Householder.OTHER_PHONE)));
        householder.setIsActive(cursor.getInt(cursor.getColumnIndex(MinistryContract.Householder.ACTIVE)));
        householder.setIsDefault(cursor.getInt(cursor.getColumnIndex(MinistryContract.Householder.DEFAULT)));
        return householder;
    }

    public void deleteAll() {
        open();
        database.delete(TABLE_NAME, null, null);
        close();
    }
}