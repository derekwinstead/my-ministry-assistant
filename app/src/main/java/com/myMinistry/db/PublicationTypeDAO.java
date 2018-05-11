package com.myMinistry.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.myMinistry.bean.PublicationType;
import com.myMinistry.provider.MinistryContract;
import com.myMinistry.provider.MinistryDatabase;
import com.myMinistry.utils.AppConstants;

import java.util.ArrayList;
import java.util.List;

public class PublicationTypeDAO {
    // Database fields
    private SQLiteDatabase database;
    private MinistryDatabase dbHelper;
    public static final String TABLE_NAME = "literatureTypes";

    public PublicationTypeDAO(Context context) {
        dbHelper = new MinistryDatabase(context.getApplicationContext());
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public long create(PublicationType bean) {
        open();

        ContentValues values = new ContentValues();
        values.put(MinistryContract.LiteratureType.NAME, bean.getName());
        values.put(MinistryContract.LiteratureType.ACTIVE, bean.isActive() ? AppConstants.ACTIVE : AppConstants.INACTIVE);
        values.put(MinistryContract.LiteratureType.DEFAULT, bean.isDefault() ? AppConstants.ACTIVE : AppConstants.INACTIVE);

        long id = database.insert(TABLE_NAME, null, values);
        close();
        return id;
    }

    public boolean deletePublicationType(PublicationType bean) {
        // TODO: Delete all associated records from other tables too.
        open();
        long id = bean.getId();
        int affectedRows = database.delete(TABLE_NAME, MinistryContract.LiteratureType._ID + " = ?", new String[]{id + ""});
        close();
        return affectedRows > 0;
    }

    public List<PublicationType> getAllPublicationTypes() {
        open();
        List<PublicationType> beanList = new ArrayList<>();
        Cursor cursor = database.query(TABLE_NAME, MinistryContract.LiteratureType.All_COLS, null, null, null, null, MinistryContract.LiteratureType.DEFAULT_SORT);
        while (cursor.moveToNext()) {
            PublicationType bean = cursorToPublicationType(cursor);
            beanList.add(bean);
        }
        // make sure to close the cursor
        cursor.close();
        close();
        return beanList;
    }

    public PublicationType getPublicationType(int id) {
        open();
        PublicationType bean;
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE " + MinistryContract.LiteratureType._ID + " =  " + id;
        Cursor cursor = database.rawQuery(sql, null);
        if (cursor.moveToNext()) {
            bean = cursorToPublicationType(cursor);
        } else {
            bean = new PublicationType();
        }
        cursor.close();
        close();
        return bean;
    }

    public void update(PublicationType bean) {
        open();
        ContentValues values = new ContentValues();
        values.put(MinistryContract.LiteratureType.NAME, bean.getName());
        values.put(MinistryContract.LiteratureType.ACTIVE, bean.isActive() ? AppConstants.ACTIVE : AppConstants.INACTIVE);
        values.put(MinistryContract.LiteratureType.DEFAULT, bean.isDefault() ? AppConstants.ACTIVE : AppConstants.INACTIVE);

        database.update(TABLE_NAME, values, MinistryContract.LiteratureType._ID + " = ?", new String[]{bean.getId() + ""});
        close();
    }

    private PublicationType cursorToPublicationType(Cursor cursor) {
        PublicationType bean = new PublicationType();
        bean.setId(cursor.getLong(cursor.getColumnIndex(MinistryContract.LiteratureType._ID)));
        bean.setName(cursor.getString(cursor.getColumnIndex(MinistryContract.LiteratureType.NAME)));
        bean.setIsActive(cursor.getInt(cursor.getColumnIndex(MinistryContract.LiteratureType.ACTIVE)));
        bean.setIsDefault(cursor.getInt(cursor.getColumnIndex(MinistryContract.LiteratureType.DEFAULT)));
        return bean;
    }

    public void deleteAll() {
        open();
        database.delete(TABLE_NAME, null, null);
        close();
    }
}