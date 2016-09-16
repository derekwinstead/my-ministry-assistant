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

    public long create(Publication publication) {
        open();

        ContentValues values = new ContentValues();
        values.put(MinistryContract.Literature.TYPE_OF_LIERATURE_ID, publication.getTypeId());
        values.put(MinistryContract.Literature.NAME, publication.getName());
        values.put(MinistryContract.Literature.ACTIVE, publication.isActive() ? MinistryService.ACTIVE : MinistryService.INACTIVE);
        values.put(MinistryContract.Literature.WEIGHT, publication.getWeight());

        long id = database.insert(TABLE_NAME, null, values);
        close();
        return id;
    }

    public boolean deletePublication(Publication publication) {
        // TODO: Delete all associated records from other tables too.
        open();
        long id = publication.getId();
        int affectedRows = database.delete(TABLE_NAME, MinistryContract.Literature._ID + " = ?", new String[]{id + ""});
        close();
        return affectedRows > 0;
    }

    public List<Publication> getAllPublications() {
        open();
        List<Publication> publicationList = new ArrayList<>();
        Cursor cursor = database.query(TABLE_NAME, MinistryContract.Literature.All_COLS, null, null, null, null, MinistryContract.Literature.DEFAULT_SORT);
        while (cursor.moveToNext()) {
            Publication publication = cursorToPublication(cursor);
            publicationList.add(publication);
        }
        // make sure to close the cursor
        cursor.close();
        close();
        return publicationList;
    }

    public Publication getPublication(int id) {
        open();
        Publication publication;
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE " + MinistryContract.Literature._ID + " =  " + id;
        Cursor cursor = database.rawQuery(sql, null);
        if (cursor.moveToNext()) {
            publication = cursorToPublication(cursor);
        } else {
            publication = new Publication();
        }
        cursor.close();
        close();
        return publication;
    }

    public void update(Publication publication) {
        open();
        ContentValues values = new ContentValues();
        values.put(MinistryContract.Literature.TYPE_OF_LIERATURE_ID, publication.getTypeId());
        values.put(MinistryContract.Literature.NAME, publication.getName());
        values.put(MinistryContract.Literature.ACTIVE, publication.isActive() ? MinistryService.ACTIVE : MinistryService.INACTIVE);
        values.put(MinistryContract.Literature.WEIGHT, publication.getWeight());

        database.update(TABLE_NAME, values, MinistryContract.Literature._ID + " = ?", new String[]{publication.getId() + ""});
        close();
    }

    private Publication cursorToPublication(Cursor cursor) {
        Publication publication = new Publication();
        publication.setId(cursor.getLong(cursor.getColumnIndex(MinistryContract.Literature._ID)));
        publication.setTypeId(cursor.getLong(cursor.getColumnIndex(MinistryContract.Literature.TYPE_OF_LIERATURE_ID)));
        publication.setName(cursor.getString(cursor.getColumnIndex(MinistryContract.Literature.NAME)));
        publication.setIsActive(cursor.getInt(cursor.getColumnIndex(MinistryContract.Literature.ACTIVE)));
        publication.setWeight(cursor.getInt(cursor.getColumnIndex(MinistryContract.Literature.WEIGHT)));
        return publication;
    }

    public void deleteAll() {
        open();
        database.delete(TABLE_NAME, null, null);
        close();
    }
}