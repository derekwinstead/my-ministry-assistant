package com.myMinistry.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.myMinistry.bean.Publication;
import com.myMinistry.provider.MinistryContract;
import com.myMinistry.provider.MinistryDatabase;
import com.myMinistry.provider.MinistryService;

import java.util.ArrayList;
import java.util.List;

import static com.myMinistry.R.menu.publication;

public class PublicationDAO {
    // Database fields
    private SQLiteDatabase database;
    private MinistryDatabase dbHelper;
    public static final String TABLE_NAME = "literatureNames";

    public PublicationDAO(Context context) {
        dbHelper = new MinistryDatabase(context.getApplicationContext());
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public long create(Publication bean) {
        open();

        ContentValues values = new ContentValues();
        values.put(MinistryContract.Literature.TYPE_OF_LIERATURE_ID, bean.getTypeId());
        values.put(MinistryContract.Literature.NAME, bean.getName());
        values.put(MinistryContract.Literature.ACTIVE, bean.isActive() ? MinistryService.ACTIVE : MinistryService.INACTIVE);
        values.put(MinistryContract.Literature.WEIGHT, bean.getWeight());

        long id = database.insert(TABLE_NAME, null, values);
        close();
        return id;
    }

    public boolean deletePublication(Publication bean) {
        // TODO: Delete all associated records from other tables too.
        open();
        long id = bean.getId();
        int affectedRows = database.delete(TABLE_NAME, MinistryContract.Literature._ID + " = ?", new String[]{id + ""});
        close();
        return affectedRows > 0;
    }

    public List<Publication> getAllPublications() {
        open();
        List<Publication> beanList = new ArrayList<>();
        Cursor cursor = database.query(TABLE_NAME, MinistryContract.Literature.All_COLS, null, null, null, null, MinistryContract.Literature.DEFAULT_SORT);
        while (cursor.moveToNext()) {
            Publication bean = cursorToPublication(cursor);
            beanList.add(bean);
        }
        // make sure to close the cursor
        cursor.close();
        close();
        return beanList;
    }

    public Publication getPublication(int id) {
        open();
        Publication bean;
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE " + MinistryContract.Literature._ID + " =  " + id;
        Cursor cursor = database.rawQuery(sql, null);
        if (cursor.moveToNext()) {
            bean = cursorToPublication(cursor);
        } else {
            bean = new Publication();
        }
        cursor.close();
        close();
        return bean;
    }

    public void update(Publication bean) {
        open();
        ContentValues values = new ContentValues();
        values.put(MinistryContract.Literature.TYPE_OF_LIERATURE_ID, bean.getTypeId());
        values.put(MinistryContract.Literature.NAME, bean.getName());
        values.put(MinistryContract.Literature.ACTIVE, bean.isActive() ? MinistryService.ACTIVE : MinistryService.INACTIVE);
        values.put(MinistryContract.Literature.WEIGHT, bean.getWeight());

        database.update(TABLE_NAME, values, MinistryContract.Literature._ID + " = ?", new String[]{bean.getId() + ""});
        close();
    }

    private Publication cursorToPublication(Cursor cursor) {
        Publication bean = new Publication();
        bean.setId(cursor.getLong(cursor.getColumnIndex(MinistryContract.Literature._ID)));
        bean.setTypeId(cursor.getLong(cursor.getColumnIndex(MinistryContract.Literature.TYPE_OF_LIERATURE_ID)));
        bean.setName(cursor.getString(cursor.getColumnIndex(MinistryContract.Literature.NAME)));
        bean.setIsActive(cursor.getInt(cursor.getColumnIndex(MinistryContract.Literature.ACTIVE)));
        bean.setWeight(cursor.getInt(cursor.getColumnIndex(MinistryContract.Literature.WEIGHT)));
        return bean;
    }

    public void deleteAll() {
        open();
        database.delete(TABLE_NAME, null, null);
        close();
    }
}