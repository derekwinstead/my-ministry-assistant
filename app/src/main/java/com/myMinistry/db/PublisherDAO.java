package com.myMinistry.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.myMinistry.bean.Publisher;
import com.myMinistry.provider.MinistryContract;
import com.myMinistry.provider.MinistryDatabase;
import com.myMinistry.utils.HelpUtils;

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
        values.put(MinistryContract.Publisher.ACTIVE, HelpUtils.booleanConversionsToInt(bean.isActive()));
        values.put(MinistryContract.Publisher.GENDER, bean.getGender());
        values.put(MinistryContract.Publisher.DEFAULT, HelpUtils.booleanConversionsToInt(bean.isDefault()));

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
        values.put(MinistryContract.Publisher.ACTIVE, HelpUtils.booleanConversionsToInt(bean.isActive()));
        values.put(MinistryContract.Publisher.GENDER, bean.getGender());
        values.put(MinistryContract.Publisher.DEFAULT, HelpUtils.booleanConversionsToInt(bean.isDefault()));

        database.update(TABLE_NAME, values, MinistryContract.Publisher._ID + " = ?", new String[]{bean.getId() + ""});
        close();
    }

    private Publisher cursorToPublisher(Cursor cursor) {
        return new Publisher(
                cursor.getLong(cursor.getColumnIndex(MinistryContract.Publisher._ID))
                , cursor.getString(cursor.getColumnIndex(MinistryContract.Publisher.NAME))
                , HelpUtils.booleanConversionsToInt(cursor.getInt(cursor.getColumnIndex(MinistryContract.Publisher.ACTIVE)))
                , cursor.getString(cursor.getColumnIndex(MinistryContract.Publisher.GENDER))
                , cursor.getInt(cursor.getColumnIndex(MinistryContract.Publisher.DEFAULT))
        );
    }
/*
    public void deleteAll() {
        open();
        database.delete(TABLE_NAME, null, null);
        close();
    }
    */
}