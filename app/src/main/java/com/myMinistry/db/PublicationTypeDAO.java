package com.myMinistry.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.myMinistry.bean.PublicationType;
import com.myMinistry.provider.MinistryContract;
import com.myMinistry.provider.MinistryDatabase;
import com.myMinistry.provider.MinistryService;

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

    public long create(PublicationType publicationType) {
        open();

        ContentValues values = new ContentValues();
        values.put(MinistryContract.LiteratureType.NAME, publicationType.getName());
        values.put(MinistryContract.LiteratureType.ACTIVE, publicationType.isActive() ? MinistryService.ACTIVE : MinistryService.INACTIVE);
        values.put(MinistryContract.LiteratureType.DEFAULT, publicationType.isDefault() ? MinistryService.ACTIVE : MinistryService.INACTIVE);

        long id = database.insert(TABLE_NAME, null, values);
        close();
        return id;
    }

    public boolean deletePublicationType(PublicationType publicationType) {
        // TODO: Delete all associated records from other tables too.
        open();
        long id = publicationType.getId();
        int affectedRows = database.delete(TABLE_NAME, MinistryContract.LiteratureType._ID + " = ?", new String[]{id + ""});
        close();
        return affectedRows > 0;
    }

    public List<PublicationType> getAllPublicationTypes() {
        open();
        List<PublicationType> publicationTypeList = new ArrayList<>();
        Cursor cursor = database.query(TABLE_NAME, MinistryContract.LiteratureType.All_COLS, null, null, null, null, MinistryContract.LiteratureType.DEFAULT_SORT);
        while (cursor.moveToNext()) {
            PublicationType publicationType = cursorToPublicationType(cursor);
            publicationTypeList.add(publicationType);
        }
        // make sure to close the cursor
        cursor.close();
        close();
        return publicationTypeList;
    }

    public PublicationType getPublicationType(int id) {
        open();
        PublicationType publicationType;
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE " + MinistryContract.LiteratureType._ID + " =  " + id;
        Cursor cursor = database.rawQuery(sql, null);
        if (cursor.moveToNext()) {
            publicationType = cursorToPublicationType(cursor);
        } else {
            publicationType = new PublicationType();
        }
        cursor.close();
        close();
        return publicationType;
    }

    public void update(PublicationType publicationType) {
        open();
        ContentValues values = new ContentValues();
        values.put(MinistryContract.LiteratureType.NAME, publicationType.getName());
        values.put(MinistryContract.LiteratureType.ACTIVE, publicationType.isActive() ? MinistryService.ACTIVE : MinistryService.INACTIVE);
        values.put(MinistryContract.LiteratureType.DEFAULT, publicationType.isDefault() ? MinistryService.ACTIVE : MinistryService.INACTIVE);

        database.update(TABLE_NAME, values, MinistryContract.LiteratureType._ID + " = ?", new String[]{publicationType.getId() + ""});
        close();
    }

    private PublicationType cursorToPublicationType(Cursor cursor) {
        PublicationType publicationType = new PublicationType();
        publicationType.setId(cursor.getLong(cursor.getColumnIndex(MinistryContract.LiteratureType._ID)));
        publicationType.setName(cursor.getString(cursor.getColumnIndex(MinistryContract.LiteratureType.NAME)));
        publicationType.setIsActive(cursor.getInt(cursor.getColumnIndex(MinistryContract.LiteratureType.ACTIVE)));
        publicationType.setIsDefault(cursor.getInt(cursor.getColumnIndex(MinistryContract.LiteratureType.DEFAULT)));
        return publicationType;
    }

    public void deleteAll() {
        open();
        database.delete(TABLE_NAME, null, null);
        close();
    }
}