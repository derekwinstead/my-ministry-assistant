package com.myMinistry.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.myMinistry.bean.EntryType;
import com.myMinistry.provider.MinistryContract;
import com.myMinistry.provider.MinistryDatabase;
import com.myMinistry.utils.HelpUtils;

import java.util.ArrayList;
import java.util.List;

public class EntryTypeDAO {
    // Database fields
    private SQLiteDatabase database;
    private MinistryDatabase dbHelper;
    public static final String TABLE_NAME = "entryTypes";

    public EntryTypeDAO(Context context) {
        dbHelper = new MinistryDatabase(context.getApplicationContext());
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public long create(EntryType bean) {
        open();

        ContentValues values = new ContentValues();
        values.put(MinistryContract.EntryType.NAME, bean.getName());
        values.put(MinistryContract.EntryType.ACTIVE, HelpUtils.booleanConversionsToInt(bean.isActive()));
        values.put(MinistryContract.EntryType.DEFAULT, HelpUtils.booleanConversionsToInt(bean.isDefault()));

        long id = database.insert(TABLE_NAME, null, values);
        close();
        return id;
    }

    public boolean deleteEntryType(EntryType bean) {
        // TODO: Delete all associated records from other tables too.
        open();
        long id = bean.getId();
        int affectedRows = database.delete(TABLE_NAME, MinistryContract.EntryType._ID + " = ?", new String[]{id + ""});
        close();
        return affectedRows > 0;
    }

    public List<EntryType> getAllEntryTypes() {
        open();
        List<EntryType> beanList = new ArrayList<>();
        Cursor cursor = database.query(TABLE_NAME, MinistryContract.EntryType.All_COLS, null, null, null, null, MinistryContract.EntryType.DEFAULT_SORT);
        while (cursor.moveToNext()) {
            EntryType bean = cursorToEntryType(cursor);
            beanList.add(bean);
        }
        // make sure to close the cursor
        cursor.close();
        close();
        return beanList;
    }

    public EntryType getEntryType(int id) {
        open();
        EntryType bean;
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE " + MinistryContract.EntryType._ID + " =  " + id;
        Cursor cursor = database.rawQuery(sql, null);
        if (cursor.moveToNext()) {
            bean = cursorToEntryType(cursor);
        } else {
            bean = new EntryType();
        }
        cursor.close();
        close();
        return bean;
    }

    public void update(EntryType bean) {
        open();
        ContentValues values = new ContentValues();
        values.put(MinistryContract.LiteratureType.NAME, bean.getName());
        values.put(MinistryContract.LiteratureType.ACTIVE, HelpUtils.booleanConversionsToInt(bean.isActive()));
        values.put(MinistryContract.LiteratureType.DEFAULT, HelpUtils.booleanConversionsToInt(bean.isDefault()));

        database.update(TABLE_NAME, values, MinistryContract.EntryType._ID + " = ?", new String[]{bean.getId() + ""});
        close();
    }

    private EntryType cursorToEntryType(Cursor cursor) {
        return new EntryType(
                cursor.getLong(cursor.getColumnIndex(MinistryContract.EntryType._ID))
                , cursor.getString(cursor.getColumnIndex(MinistryContract.EntryType.NAME))
                , HelpUtils.booleanConversionsToInt(cursor.getInt(cursor.getColumnIndex(MinistryContract.EntryType.ACTIVE)))
                , HelpUtils.booleanConversionsToInt(cursor.getInt(cursor.getColumnIndex(MinistryContract.EntryType.DEFAULT)))
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