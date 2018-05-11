package com.myMinistry.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.myMinistry.bean.Publisher;
import com.myMinistry.provider.MinistryContract;
import com.myMinistry.provider.MinistryDatabase;
import com.myMinistry.utils.AppConstants;

import java.util.ArrayList;
import java.util.List;

public class PublisherDAO {
    // Database fields
    private SQLiteDatabase database;
    private MinistryDatabase dbHelper;
    public static final String TABLE_NAME = "publishers";

    public PublisherDAO(Context context) {
        dbHelper = new MinistryDatabase(context.getApplicationContext());
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public long create(Publisher bean) {
        open();

        ContentValues values = new ContentValues();
        values.put(MinistryContract.Publisher.NAME, bean.getName());
        values.put(MinistryContract.Publisher.ACTIVE, bean.isActive() ? AppConstants.ACTIVE : AppConstants.INACTIVE);
        values.put(MinistryContract.Publisher.GENDER, bean.getGender());
        values.put(MinistryContract.Publisher.DEFAULT, bean.isDefault() ? AppConstants.ACTIVE : AppConstants.INACTIVE);

        long id = database.insert(TABLE_NAME, null, values);
        close();
        return id;
    }

    public boolean deletePublisher(Publisher bean) {
        // TODO: Delete all associated records from other tables too.
        open();
        long id = bean.getId();
        int affectedRows = database.delete(TABLE_NAME, MinistryContract.Publisher._ID + " = ?", new String[]{id + ""});
        close();
        return affectedRows > 0;
    }

    public List<Publisher> getAllPublishers() {
        open();
        List<Publisher> beanList = new ArrayList<>();
        Cursor cursor = database.query(TABLE_NAME, MinistryContract.Publisher.All_COLS, null, null, null, null, MinistryContract.Publisher.DEFAULT_SORT);
        while (cursor.moveToNext()) {
            Publisher bean = cursorToPublisher(cursor);
            beanList.add(bean);
        }
        // make sure to close the cursor
        cursor.close();
        close();
        return beanList;
    }

    public Publisher getPublisher(int id) {
        open();
        Publisher bean;
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE " + MinistryContract.Publisher._ID + " =  " + id;
        Cursor cursor = database.rawQuery(sql, null);
        if (cursor.moveToNext()) {
            bean = cursorToPublisher(cursor);
        } else {
            bean = new Publisher();
        }
        cursor.close();
        close();
        return bean;
    }

    public void update(Publisher bean) {
        open();
        ContentValues values = new ContentValues();
        values.put(MinistryContract.Publisher.NAME, bean.getName());
        values.put(MinistryContract.Publisher.ACTIVE, bean.isActive() ? AppConstants.ACTIVE : AppConstants.INACTIVE);
        values.put(MinistryContract.Publisher.GENDER, bean.getGender());
        values.put(MinistryContract.Publisher.DEFAULT, bean.isDefault() ? AppConstants.ACTIVE : AppConstants.INACTIVE);

        database.update(TABLE_NAME, values, MinistryContract.Publisher._ID + " = ?", new String[]{bean.getId() + ""});
        close();
    }

    private Publisher cursorToPublisher(Cursor cursor) {
        Publisher bean = new Publisher();
        bean.setId(cursor.getLong(cursor.getColumnIndex(MinistryContract.Publisher._ID)));
        bean.setName(cursor.getString(cursor.getColumnIndex(MinistryContract.Publisher.NAME)));
        bean.setIsActive(cursor.getInt(cursor.getColumnIndex(MinistryContract.Publisher.ACTIVE)));
        bean.setGender(cursor.getString(cursor.getColumnIndex(MinistryContract.Publisher.GENDER)));
        bean.setIsDefault(cursor.getInt(cursor.getColumnIndex(MinistryContract.Publisher.DEFAULT)));
        return bean;
    }

    public void deleteAll() {
        open();
        database.delete(TABLE_NAME, null, null);
        close();
    }
}