package com.myMinistry.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.myMinistry.bean.Publisher;
import com.myMinistry.provider.MinistryContract;
import com.myMinistry.provider.MinistryDatabase;
import com.myMinistry.provider.MinistryService;

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

    public long create(Publisher publisher) {
        open();

        ContentValues values = new ContentValues();
        values.put(MinistryContract.Publisher.NAME, publisher.getName());
        values.put(MinistryContract.Publisher.ACTIVE, publisher.isActive() ? MinistryService.ACTIVE : MinistryService.INACTIVE);
        values.put(MinistryContract.Publisher.GENDER, publisher.getGender());
        values.put(MinistryContract.Publisher.DEFAULT, publisher.isDefault() ? MinistryService.ACTIVE : MinistryService.INACTIVE);

        long id = database.insert(TABLE_NAME, null, values);
        close();
        return id;
    }

    public boolean deletePublisher(Publisher publisher) {
        // TODO: Delete all associated records from other tables too.
        open();
        long id = publisher.getId();
        int affectedRows = database.delete(TABLE_NAME, MinistryContract.Publisher._ID + " = ?", new String[]{id + ""});
        close();
        return affectedRows > 0;
    }

    public List<Publisher> getAllPublishers() {
        open();
        List<Publisher> publisherList = new ArrayList<>();
        Cursor cursor = database.query(TABLE_NAME, MinistryContract.Publisher.All_COLS, null, null, null, null, MinistryContract.Publisher.DEFAULT_SORT);
        while (cursor.moveToNext()) {
            Publisher publisher = cursorToTask(cursor);
            publisherList.add(publisher);
        }
        // make sure to close the cursor
        cursor.close();
        close();
        return publisherList;
    }

    public Publisher getPublisher(int id) {
        open();
        Publisher publisher;
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE " + MinistryContract.Publisher._ID + " =  " + id;
        Cursor cursor = database.rawQuery(sql, null);
        if (cursor.moveToNext()) {
            publisher = cursorToTask(cursor);
        } else {
            publisher = new Publisher();
        }
        cursor.close();
        close();
        return publisher;
    }

    public void update(Publisher publisher) {
        open();
        ContentValues values = new ContentValues();
        values.put(MinistryContract.Publisher.NAME, publisher.getName());
        values.put(MinistryContract.Publisher.ACTIVE, publisher.isActive() ? MinistryService.ACTIVE : MinistryService.INACTIVE);
        values.put(MinistryContract.Publisher.GENDER, publisher.getGender());
        values.put(MinistryContract.Publisher.DEFAULT, publisher.isDefault() ? MinistryService.ACTIVE : MinistryService.INACTIVE);

        database.update(TABLE_NAME, values, MinistryContract.Publisher._ID + " = ?", new String[]{publisher.getId() + ""});
        close();
    }

    private Publisher cursorToTask(Cursor cursor) {
        Publisher publisher = new Publisher();
        publisher.setId(cursor.getLong(cursor.getColumnIndex(MinistryContract.Publisher._ID)));
        publisher.setName(cursor.getString(cursor.getColumnIndex(MinistryContract.Publisher.NAME)));
        publisher.setIsActive(cursor.getInt(cursor.getColumnIndex(MinistryContract.Publisher.ACTIVE)));
        publisher.setGender(cursor.getString(cursor.getColumnIndex(MinistryContract.Publisher.GENDER)));
        publisher.setIsDefault(cursor.getInt(cursor.getColumnIndex(MinistryContract.Publisher.DEFAULT)));
        return publisher;
    }

    public void deleteAll() {
        open();
        database.delete(TABLE_NAME, null, null);
        close();
    }
}