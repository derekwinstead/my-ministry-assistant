package tester.provider;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;
import android.widget.Toast;

import tester.Helper;
import com.myMinistry.R;
import tester.provider.MinistryContract.EntryType;
import tester.provider.MinistryContract.Householder;
import tester.provider.MinistryContract.Literature;
import tester.provider.MinistryContract.LiteraturePlaced;
import tester.provider.MinistryContract.LiteratureType;
import tester.provider.MinistryContract.Notes;
import tester.provider.MinistryContract.Pioneering;
import tester.provider.MinistryContract.PioneeringType;
import tester.provider.MinistryContract.Publisher;
import tester.provider.MinistryContract.Rollover;
import tester.provider.MinistryContract.Time;
import tester.provider.MinistryContract.TimeHouseholder;
import tester.util.FileUtils;
import tester.util.TimeUtils;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Locale;

import static tester.util.LogUtils.makeLogTag;

public class MinistryDatabase extends SQLiteOpenHelper {
    private static MinistryDatabase mInstance = null;
    private static Context mContext = null;

    public static final String TAG = makeLogTag(MinistryDatabase.class);
    public static final String DATABASE_NAME = "myministry.db";
    public static final String DATABASE_NAME_OLD = "myministry";

    public static final int ID_BOOKS = 1;
    public static final int ID_BROCHURES = 2;
    public static final int ID_MAGAZINES = 3;
    public static final int ID_MEDIA = 4;
    public static final int ID_TRACTS = 5;

    public static final int MAX_PUBLICATION_TYPE_ID = ID_TRACTS;

    public static final int ID_ROLLOVER = 1;
    public static final int ID_BIBLE_STUDY = 2;
    public static final int ID_RETURN_VISIT = 3;
    public static final int ID_SERVICE = 4;
    public static final int ID_RBC = 5;

    public static final int ID_PIONEERING = 1;
    public static final int ID_PIONEERING_AUXILIARY = 2;
    public static final int ID_PIONEERING_AUXILIARY_SPECIAL = 3;

    public static final int ID_UNION_TYPE_PERSON = 1;
    public static final int ID_UNION_TYPE_PUBLICATION = 2;

    public static final int ID_DEFAULT_PUBLISHER = 1;

    public static final int CREATE_ID = -5;

    public static final int NO_HOUSEHOLDER_ID = -1;

    public static final int SORT_BY_ASC = 1;
    public static final int SORT_BY_DESC = 2;
    public static final int SORT_BY_POPULAR = 3;
    public static final int SORT_BY_DATE = 4;
    public static final int SORT_BY_DATE_DESC = 5;

    /** NOTE: carefully update onUpgrade() when bumping database versions to make sure user data is saved. */
    private static final int VER_LAUNCH = 1;
    private static final int VER_ADD_RETURN_VISITS = 2;
    private static final int VER_DB_RESTRUCTURE = 3;
    private static final int VER_ADD_NOTES = 4;
    private static final int VER_ADD_END_DATE = 5;
    private static final int VER_REMOVE_PIONEERING_TABLE = 6;
    private static final int VER_ADD_TRACTS = 7;
    private static final int VER_CONVERT_ROLLOVER_TO_MINUTES = 8;
    private static final int VER_REMOVE_TOTAL_TIME_COLUMN = 9;
    private static final int VER_ADD_PIONEERING = 10;
    private static final int VER_ADD_IS_RETURN_VISIT = 11;
    private static final int VER_ADD_HOUSEHOLDER_SORT_ORDER = 12;

    public static final int DATABASE_VERSION = VER_ADD_HOUSEHOLDER_SORT_ORDER;

    interface Tables {
        String ENTRY_TYPES = "entryTypes";
        String HOUSEHOLDERS = "householders";
        String PLACED_LITERATURE = "literaturePlaced";
        String TYPES_OF_LIERATURE = "literatureTypes";
        String LITERATURE = "literatureNames";
        String PUBLISHERS = "publishers";
        String ROLLOVER = "rolloverMinutes";
        String TIMES = "time";
        String PIONEERING = "pioneering";
        String TYPES_OF_PIONEERING = "pioneeringTypes";
        String TIME_HOUSEHOLDERS = "timeHouseholders";
        String NOTES = "notes";
    }

    public static MinistryDatabase getInstance(Context ctx) {
        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        // See this article for more information: http://bit.ly/6LRzfx
        if (mInstance == null)
            mInstance = new MinistryDatabase(ctx.getApplicationContext());
        return mInstance;
    }

    /**
     * Constructor should be private to prevent direct instantiation.
     * make call to static factory method "getInstance()" instead.
     */
    private MinistryDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        mContext = context;
    }

    private void createDefaults(SQLiteDatabase db) {
        ContentValues values = new ContentValues();
        values.put(EntryType._ID, MinistryDatabase.ID_ROLLOVER);
        values.put(EntryType.NAME, mContext.getResources().getString(R.string.default_rollover_time));
        values.put(EntryType.ACTIVE, MinistryService.INACTIVE);
        values.put(EntryType.RBC, MinistryService.INACTIVE);
        values.put(EntryType.SORT_ORDER, MinistryService.INACTIVE);

        db.insert(Tables.ENTRY_TYPES, null, values);

        values.put(EntryType._ID, MinistryDatabase.ID_BIBLE_STUDY);
        values.put(EntryType.NAME, mContext.getResources().getString(R.string.default_bible_study));
        values.put(EntryType.ACTIVE, MinistryService.ACTIVE);

        db.insert(Tables.ENTRY_TYPES, null, values);

        values.put(EntryType._ID, MinistryDatabase.ID_RETURN_VISIT);
        values.put(EntryType.NAME, mContext.getResources().getString(R.string.default_return_visit));

        db.insert(Tables.ENTRY_TYPES, null, values);

        values.put(EntryType._ID, MinistryDatabase.ID_SERVICE);
        values.put(EntryType.NAME, mContext.getResources().getString(R.string.default_ministry_service));

        db.insert(Tables.ENTRY_TYPES, null, values);

        values.put(EntryType._ID, MinistryDatabase.ID_RBC);
        values.put(EntryType.NAME, mContext.getResources().getString(R.string.default_rebuild_committee));
        values.put(EntryType.RBC, MinistryService.ACTIVE);

        db.insert(Tables.ENTRY_TYPES, null, values);

        values = new ContentValues();
        values.put(LiteratureType._ID, MinistryDatabase.ID_BOOKS);
        values.put(LiteratureType.NAME, mContext.getResources().getString(R.string.default_books));
        values.put(LiteratureType.ACTIVE, MinistryService.ACTIVE);
        values.put(LiteratureType.SORT_ORDER, MinistryService.INACTIVE);

        db.insert(Tables.TYPES_OF_LIERATURE, null, values);

        values.put(LiteratureType._ID, MinistryDatabase.ID_BROCHURES);
        values.put(LiteratureType.NAME, mContext.getResources().getString(R.string.default_booklets));

        db.insert(Tables.TYPES_OF_LIERATURE, null, values);

        values.put(LiteratureType._ID, MinistryDatabase.ID_MAGAZINES);
        values.put(LiteratureType.NAME, mContext.getResources().getString(R.string.default_magazines));

        db.insert(Tables.TYPES_OF_LIERATURE, null, values);

        values.put(LiteratureType._ID, MinistryDatabase.ID_MEDIA);
        values.put(LiteratureType.NAME, mContext.getResources().getString(R.string.default_media));

        db.insert(Tables.TYPES_OF_LIERATURE, null, values);

        values.put(LiteratureType._ID, MinistryDatabase.ID_TRACTS);
        values.put(LiteratureType.NAME, mContext.getResources().getString(R.string.default_tracts));

        db.insert(Tables.TYPES_OF_LIERATURE, null, values);

        createPioneeringTypeDefaults(db);
    }

    private void createPioneeringTypeDefaults(SQLiteDatabase db) {
        ContentValues values = new ContentValues();
        values.put(PioneeringType._ID, MinistryDatabase.ID_PIONEERING);
        values.put(PioneeringType.NAME, mContext.getResources().getString(R.string.default_pioneering));

        db.insert(Tables.TYPES_OF_PIONEERING, null, values);

        values.put(PioneeringType._ID, MinistryDatabase.ID_PIONEERING_AUXILIARY);
        values.put(PioneeringType.NAME, mContext.getResources().getString(R.string.default_pioneering_aux));

        db.insert(Tables.TYPES_OF_PIONEERING, null, values);

        values.put(PioneeringType._ID, MinistryDatabase.ID_PIONEERING_AUXILIARY_SPECIAL);
        values.put(PioneeringType.NAME, mContext.getResources().getString(R.string.default_pioneering_aux_special));

        db.insert(Tables.TYPES_OF_PIONEERING, null, values);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(EntryType.SCRIPT_CREATE);
        db.execSQL(Householder.SCRIPT_CREATE);
        db.execSQL(Publisher.SCRIPT_CREATE);
        db.execSQL(Rollover.SCRIPT_CREATE);
        db.execSQL(Time.SCRIPT_CREATE);
        db.execSQL(LiteratureType.SCRIPT_CREATE);
        db.execSQL(Notes.SCRIPT_CREATE);
        db.execSQL(LiteraturePlaced.SCRIPT_CREATE);
        db.execSQL(Literature.SCRIPT_CREATE);
        db.execSQL(TimeHouseholder.SCRIPT_CREATE);
        db.execSQL(PioneeringType.SCRIPT_CREATE);
        db.execSQL(Pioneering.SCRIPT_CREATE);

        createDefaults(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "onUpgrade() from " + oldVersion + " to " + newVersion);

        /** NOTE: This switch statement is designed to handle cascading database updates, starting at the current version and falling through
         *  to all future upgrade cases. Only use "break;" when you want to drop and recreate the entire database.
         */
        int version = oldVersion;

        switch (version) {
            case VER_LAUNCH:
                versionBackup(version);
                /** Version 2 added column for return visits. */
                db.execSQL("ALTER TABLE " + Tables.TIMES + " ADD COLUMN returnVisits INTEGER DEFAULT 0");
                version = VER_ADD_RETURN_VISITS;
            case VER_ADD_RETURN_VISITS:
                versionBackup(version);
                db.execSQL("ALTER TABLE " + Tables.ENTRY_TYPES + " RENAME TO " + Tables.ENTRY_TYPES + "_tmp");
                db.execSQL("ALTER TABLE " + Tables.HOUSEHOLDERS + " RENAME TO " + Tables.HOUSEHOLDERS + "_tmp");
                db.execSQL("ALTER TABLE " + Tables.LITERATURE + " RENAME TO " + Tables.LITERATURE + "_tmp");
                db.execSQL("ALTER TABLE " + Tables.PIONEERING + " RENAME TO " + Tables.PIONEERING + "_tmp");
                db.execSQL("ALTER TABLE " + Tables.PLACED_LITERATURE + " RENAME TO " + Tables.PLACED_LITERATURE + "_tmp");
                db.execSQL("ALTER TABLE " + Tables.PUBLISHERS + " RENAME TO " + Tables.PUBLISHERS + "_tmp");
                db.execSQL("ALTER TABLE " + Tables.ROLLOVER + " RENAME TO " + Tables.ROLLOVER + "_tmp");
                db.execSQL("ALTER TABLE " + Tables.TIMES + " RENAME TO " + Tables.TIMES + "_tmp");
                db.execSQL("ALTER TABLE " + Tables.TYPES_OF_LIERATURE + " RENAME TO " + Tables.TYPES_OF_LIERATURE + "_tmp");

                db.execSQL("DROP TABLE IF EXISTS " + Tables.ENTRY_TYPES);
                db.execSQL("DROP TABLE IF EXISTS " + Tables.HOUSEHOLDERS);
                db.execSQL("DROP TABLE IF EXISTS " + Tables.LITERATURE);
                db.execSQL("DROP TABLE IF EXISTS " + Tables.PLACED_LITERATURE);
                db.execSQL("DROP TABLE IF EXISTS " + Tables.PUBLISHERS);
                db.execSQL("DROP TABLE IF EXISTS " + Tables.ROLLOVER);
                db.execSQL("DROP TABLE IF EXISTS " + Tables.TIMES);
                db.execSQL("DROP TABLE IF EXISTS " + Tables.TYPES_OF_LIERATURE);
                db.execSQL("DROP TABLE IF EXISTS " + Tables.PIONEERING);

                onCreate(db);

                /** Hack to get around not creating the defaults so there aren't any records in the table */
                db.execSQL("DROP TABLE IF EXISTS " + Tables.ENTRY_TYPES);
                db.execSQL(EntryType.SCRIPT_CREATE);
                db.execSQL("DROP TABLE IF EXISTS " + Tables.TYPES_OF_LIERATURE);
                db.execSQL(LiteratureType.SCRIPT_CREATE);
                /** End Hack */

                updateEntryTypes(db);
                updateHouseholders(db);
                updateLiterature(db);
                updateLiteratureTypes(db);
                updateLiteraturePlaced(db);
                updatePublishers(db);
                updateTime(db);
                updateRollover(db);

                db.execSQL("DROP TABLE IF EXISTS " + Tables.ENTRY_TYPES + "_tmp");
                db.execSQL("DROP TABLE IF EXISTS " + Tables.HOUSEHOLDERS + "_tmp");
                db.execSQL("DROP TABLE IF EXISTS " + Tables.LITERATURE + "_tmp");
                db.execSQL("DROP TABLE IF EXISTS " + Tables.TYPES_OF_LIERATURE + "_tmp");
                db.execSQL("DROP TABLE IF EXISTS " + Tables.PLACED_LITERATURE + "_tmp");
                db.execSQL("DROP TABLE IF EXISTS " + Tables.PUBLISHERS + "_tmp");
                db.execSQL("DROP TABLE IF EXISTS " + Tables.ROLLOVER + "_tmp");
                db.execSQL("DROP TABLE IF EXISTS " + Tables.TIMES + "_tmp");
                db.execSQL("DROP TABLE IF EXISTS " + Tables.PIONEERING + "_tmp");

                version = VER_DB_RESTRUCTURE;
            case VER_DB_RESTRUCTURE:
                versionBackup(version);
                db.execSQL("DROP TABLE IF EXISTS " + Tables.NOTES);
                db.execSQL(Notes.SCRIPT_CREATE);
                version = VER_ADD_NOTES;
            case VER_ADD_NOTES:
                versionBackup(version);
                db.execSQL("ALTER TABLE " + Tables.TIMES + " RENAME TO " + Tables.TIMES + "_tmp");
                db.execSQL(Time.SCRIPT_CREATE);
                updateTimeV2(db);
                db.execSQL("DROP TABLE IF EXISTS " + Tables.TIMES + "_tmp");

                version = VER_ADD_END_DATE;
            case VER_ADD_END_DATE:
                versionBackup(version);
                db.execSQL("DROP TABLE IF EXISTS " + Tables.PIONEERING);
                version = VER_REMOVE_PIONEERING_TABLE;
            case VER_REMOVE_PIONEERING_TABLE:
                versionBackup(version);
                Cursor cursor = db.rawQuery("SELECT * FROM " + Tables.TYPES_OF_LIERATURE + " WHERE " + LiteratureType._ID + " = " + ID_TRACTS, null);
                ContentValues values = new ContentValues();

                if(cursor.moveToFirst()) {
                    values.put(LiteratureType.NAME, cursor.getString(cursor.getColumnIndex(LiteratureType.NAME)));
                    values.put(LiteratureType.ACTIVE, cursor.getInt(cursor.getColumnIndex(LiteratureType.ACTIVE)));
                    values.put(LiteratureType.SORT_ORDER, cursor.getInt(cursor.getColumnIndex(LiteratureType.SORT_ORDER)));

                    ContentValues updatevalues = new ContentValues();
                    updatevalues.put(Literature.TYPE_OF_LIERATURE_ID, db.insert(Tables.TYPES_OF_LIERATURE, null, values));
                    db.update(Tables.LITERATURE, updatevalues, Literature.TYPE_OF_LIERATURE_ID + "=" + ID_TRACTS, null);

                    values.put(LiteratureType.NAME, mContext.getResources().getString(R.string.default_tracts));
                    values.put(LiteratureType.ACTIVE, MinistryService.ACTIVE);
                    values.put(LiteratureType.SORT_ORDER, ID_TRACTS);

                    db.update(Tables.TYPES_OF_LIERATURE, values, BaseColumns._ID + "=" + ID_TRACTS, null);
                }
                else {
                    values.put(LiteratureType._ID, MinistryDatabase.ID_TRACTS);
                    values.put(LiteratureType.NAME, mContext.getResources().getString(R.string.default_tracts));
                    values.put(LiteratureType.ACTIVE, MinistryService.ACTIVE);
                    values.put(LiteratureType.SORT_ORDER, ID_TRACTS);

                    db.insert(Tables.TYPES_OF_LIERATURE, null, values);
                }
                cursor.close();

                version = VER_ADD_TRACTS;
            case VER_ADD_TRACTS:
                versionBackup(version);
                MinistryService database = new MinistryService(mContext);
                //SimpleDateFormat dbDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                Calendar start = Calendar.getInstance(Locale.getDefault());
                Calendar now = Calendar.getInstance(Locale.getDefault());
                int minutes = 0;
                int pubID = 0;
                boolean found = false;
                values = new ContentValues();
                values.put(Rollover.PUBLISHER_ID, 0);

                /** Loop over each publisher for each available month to convert */
                Cursor pubs = database.fetchAllPublishers(db);
                Cursor theDate, ro;

                for(pubs.moveToFirst();!pubs.isAfterLast();pubs.moveToNext()) {
                    found = false;
                    pubID = pubs.getInt(pubs.getColumnIndex(Publisher._ID));
                    values.put(Rollover.PUBLISHER_ID, pubID);

                    /** Get first RO date for publisher */
                    theDate = db.query(Tables.ROLLOVER, new String[] {Rollover._ID,Rollover.DATE}, Rollover.PUBLISHER_ID + " = " + pubID, null, null, null, Rollover.DATE, "1");

                    if(theDate.moveToFirst()) {
                        found = true;
                        try {
                            start.setTime(TimeUtils.dbDateFormat.parse(theDate.getString(theDate.getColumnIndex(Rollover.DATE))));
                        } catch (ParseException e) {
                            start = Calendar.getInstance(Locale.getDefault());
                        }
                    }
                    theDate.close();

                    if(found) {
                        do {
                            minutes += Integer.valueOf(Helper.getMinuteDuration(database.fetchListOfHoursForMonthForPublisher(db, TimeUtils.dbDateFormat.format(start.getTime()), pubID)));

                            if(minutes >= 60)
                                minutes -= 60;

                            values.put(Rollover.DATE, TimeUtils.dbDateFormat.format(start.getTime()));
                            values.put(Rollover.MINUTES, minutes);

                            /** Save the minutes back to the RO table */
                            ro = database.fetchRolloverRecord(db, pubID, TimeUtils.dbDateFormat.format(start.getTime()));
                            if(ro.moveToFirst())
                                database.saveRolloverMinutes(db, ro.getInt(ro.getColumnIndex(Rollover._ID)), values);
                            else
                                database.createRolloverMinutes(db, values);
                            ro.close();
                            start.add(Calendar.MONTH, 1);
                        } while(start.before(now));
                    }
                }

                pubs.close();

                version = VER_CONVERT_ROLLOVER_TO_MINUTES;
            case VER_CONVERT_ROLLOVER_TO_MINUTES:
                versionBackup(version);
                db.execSQL("ALTER TABLE " + Tables.TIMES + " RENAME TO " + Tables.TIMES + "_tmp");
                db.execSQL(Time.SCRIPT_CREATE);
                updateTimeV3(db);
                db.execSQL("DROP TABLE IF EXISTS " + Tables.TIMES + "_tmp");

                version = VER_REMOVE_TOTAL_TIME_COLUMN;
            case VER_REMOVE_TOTAL_TIME_COLUMN:
                versionBackup(version);
                db.execSQL("DROP TABLE IF EXISTS " + Tables.TYPES_OF_PIONEERING);
                db.execSQL("DROP TABLE IF EXISTS " + Tables.PIONEERING);

                db.execSQL(PioneeringType.SCRIPT_CREATE);
                db.execSQL(Pioneering.SCRIPT_CREATE);
                createPioneeringTypeDefaults(db);

                version = VER_ADD_PIONEERING;
            case VER_ADD_PIONEERING:
                versionBackup(version);
                boolean shouldAlterTable = true;
                Cursor checkCols = db.rawQuery("PRAGMA table_info(" + Tables.TIME_HOUSEHOLDERS + ")", null);
                for(checkCols.moveToFirst();!checkCols.isAfterLast();checkCols.moveToNext()) {
                    if(checkCols.getString(checkCols.getColumnIndex("name")).equals(TimeHouseholder.RETURN_VISIT)) {
                        shouldAlterTable = false;
                        break;
                    }
                }
                checkCols.close();

                if(shouldAlterTable)
                    db.execSQL("ALTER TABLE " + Tables.TIME_HOUSEHOLDERS + " ADD COLUMN " + TimeHouseholder.RETURN_VISIT + " INTEGER DEFAULT 1");

                version = VER_ADD_IS_RETURN_VISIT;
            case VER_ADD_IS_RETURN_VISIT:
                versionBackup(version);

                db.execSQL("ALTER TABLE " + Tables.HOUSEHOLDERS + " ADD COLUMN " + Householder.SORT_ORDER + " INTEGER DEFAULT 1");

                version = VER_ADD_HOUSEHOLDER_SORT_ORDER;
        }
    }

    public void updateEntryTypes(SQLiteDatabase db) {
        db.execSQL("INSERT INTO " + Tables.ENTRY_TYPES + " ("
                        + EntryType._ID
                        + "," + EntryType.NAME
                        + "," + EntryType.ACTIVE
                        + "," + EntryType.RBC
                        + "," + EntryType.SORT_ORDER
                        + ")"
                        + " SELECT "
                        + EntryType._ID
                        + "," + EntryType.NAME
                        + "," + EntryType.ACTIVE
                        + "," + EntryType.RBC
                        + "," + EntryType.SORT_ORDER
                        + " FROM " + Tables.ENTRY_TYPES + "_tmp"
        );
    }

    public void updateHouseholders(SQLiteDatabase db) {
        db.execSQL("INSERT INTO " + Tables.HOUSEHOLDERS + " ("
                        + Householder._ID
                        + "," + Householder.NAME
                        + "," + Householder.ADDR
                        + "," + Householder.MOBILE_PHONE
                        + "," + Householder.HOME_PHONE
                        + "," + Householder.WORK_PHONE
                        + "," + Householder.OTHER_PHONE
                        + "," + Householder.ACTIVE
                        + ")"
                        + " SELECT "
                        + Householder._ID
                        + "," + Householder.NAME
                        + "," + Householder.ADDR
                        + "," + Householder.MOBILE_PHONE
                        + "," + Householder.HOME_PHONE
                        + "," + Householder.WORK_PHONE
                        + "," + Householder.OTHER_PHONE
                        + "," + Householder.ACTIVE
                        + " FROM " + Tables.HOUSEHOLDERS + "_tmp"
        );
    }

    public void updateLiterature(SQLiteDatabase db) {
        db.execSQL("INSERT INTO " + Tables.LITERATURE + " ("
                        + Literature._ID
                        + "," + Literature.NAME
                        + "," + Literature.TYPE_OF_LIERATURE_ID
                        + "," + Literature.ACTIVE
                        + "," + Literature.WEIGHT
                        + "," + Literature.SORT_ORDER
                        + ")"
                        + " SELECT "
                        + Literature._ID
                        + "," + Literature.NAME
                        + "," + Literature.TYPE_OF_LIERATURE_ID
                        + "," + Literature.ACTIVE
                        + "," + Literature.WEIGHT
                        + "," + Literature.SORT_ORDER
                        + " FROM " + Tables.LITERATURE + "_tmp"
        );
    }

    public void updateLiteratureTypes(SQLiteDatabase db) {
        db.execSQL("INSERT INTO " + Tables.TYPES_OF_LIERATURE + " ("
                        + LiteratureType._ID
                        + "," + LiteratureType.NAME
                        + "," + LiteratureType.ACTIVE
                        + "," + LiteratureType.SORT_ORDER
                        + ")"
                        + " SELECT "
                        + LiteratureType._ID
                        + "," + LiteratureType.NAME
                        + "," + LiteratureType.ACTIVE
                        + "," + LiteratureType.SORT_ORDER
                        + " FROM " + Tables.TYPES_OF_LIERATURE + "_tmp"
        );
    }

    public void updateLiteraturePlaced(SQLiteDatabase db) {
        db.execSQL("INSERT INTO " + Tables.PLACED_LITERATURE + " ("
                        + LiteraturePlaced._ID
                        + "," + LiteraturePlaced.PUBLISHER_ID
                        + "," + LiteraturePlaced.LITERATURE_ID
                        + "," + LiteraturePlaced.HOUSEHOLDER_ID
                        + "," + LiteraturePlaced.TIME_ID
                        + "," + LiteraturePlaced.COUNT
                        + "," + LiteraturePlaced.DATE
                        + ")"
                        + " SELECT "
                        + LiteraturePlaced._ID
                        + "," + LiteraturePlaced.PUBLISHER_ID
                        + "," + LiteraturePlaced.LITERATURE_ID
                        + "," + LiteraturePlaced.HOUSEHOLDER_ID
                        + "," + LiteraturePlaced.TIME_ID
                        + "," + LiteraturePlaced.COUNT
                        + "," + LiteraturePlaced.DATE
                        + " FROM " + Tables.PLACED_LITERATURE + "_tmp"
        );
    }

    public void updatePublishers(SQLiteDatabase db) {
        db.execSQL("INSERT INTO " + Tables.PUBLISHERS + " ("
                        + Publisher._ID
                        + "," + Publisher.NAME
                        + "," + Publisher.ACTIVE
                        + ")"
                        + " SELECT "
                        + Publisher._ID
                        + "," + Publisher.NAME
                        + "," + Publisher.ACTIVE
                        + " FROM " + Tables.PUBLISHERS + "_tmp"
        );
    }

    public void updateTime(SQLiteDatabase db) {
        /** Let's get ALL the time entries and insert them into the new table timeHouseholders */
        Cursor times = db.rawQuery("SELECT * FROM " + Tables.TIMES + "_tmp", null);

        if(times.moveToFirst()) {
            int rvs, entryType = 0;
            ContentValues values = new ContentValues();
            do {
                entryType = times.getInt(times.getColumnIndex(Time.ENTRY_TYPE_ID));
                if(entryType == ID_BIBLE_STUDY) {
                    values.put(TimeHouseholder.TIME_ID, times.getInt(times.getColumnIndex(Time._ID)));
                    values.put(TimeHouseholder.HOUSEHOLDER_ID, times.getInt(times.getColumnIndex("householderID")));
                    values.put(TimeHouseholder.STUDY, 1);
                    db.insert(Tables.TIME_HOUSEHOLDERS,null,values);
                }
                else if(entryType == ID_RETURN_VISIT) {
                    values.put(TimeHouseholder.TIME_ID, times.getInt(times.getColumnIndex(Time._ID)));
                    values.put(TimeHouseholder.HOUSEHOLDER_ID, times.getInt(times.getColumnIndex("householderID")));
                    values.put(TimeHouseholder.STUDY, 0);
                    db.insert(Tables.TIME_HOUSEHOLDERS,null,values);
                    /** Change the type for the insert into the new table. */
                    ContentValues timeValues = new ContentValues();
                    timeValues.put(Time.ENTRY_TYPE_ID, 4);
                    db.update(Tables.TIMES + "_tmp", timeValues, Time._ID + "=" + times.getInt(times.getColumnIndex(Time._ID)), null);

                }
                else {
                    rvs = times.getInt(times.getColumnIndex("returnVisits"));
                    for(int i = 0; i < rvs; i++) {
                        values.put(TimeHouseholder.TIME_ID, times.getInt(times.getColumnIndex(Time._ID)));
                        values.put(TimeHouseholder.HOUSEHOLDER_ID, 0);
                        values.put(TimeHouseholder.STUDY, 0);
                        db.insert(Tables.TIME_HOUSEHOLDERS,null,values);
                    }
                }
            } while(times.moveToNext());
        }

        db.execSQL("INSERT INTO " + Tables.TIMES + " ("
                        + Time._ID
                        + "," + Time.PUBLISHER_ID
                        + "," + Time.ENTRY_TYPE_ID
                        + "," + Time.DATE_START
                        + "," + Time.TIME_START
                        + "," + Time.TIME_END
                        + ")"
                        + " SELECT "
                        + Time._ID
                        + "," + Time.PUBLISHER_ID
                        + "," + Time.ENTRY_TYPE_ID
                        + ",entryDate"
                        + "," + Time.TIME_START
                        + "," + Time.TIME_END
                        + " FROM " + Tables.TIMES + "_tmp"
        );
    }

    public void updateTimeV2(SQLiteDatabase db) {
        db.execSQL("INSERT INTO " + Tables.TIMES + " ("
                        + Time._ID
                        + "," + Time.PUBLISHER_ID
                        + "," + Time.ENTRY_TYPE_ID
                        + "," + Time.DATE_START
                        + "," + Time.DATE_END
                        + "," + Time.TIME_START
                        + "," + Time.TIME_END
                        + ")"
                        + " SELECT "
                        + Time._ID
                        + "," + Time.PUBLISHER_ID
                        + "," + Time.ENTRY_TYPE_ID
                        + "," + Time.DATE_START
                        + "," + Time.DATE_START
                        + "," + Time.TIME_START
                        + "," + Time.TIME_END
                        + " FROM " + Tables.TIMES + "_tmp"
        );
    }

    public void updateTimeV3(SQLiteDatabase db) {
        db.execSQL("INSERT INTO " + Tables.TIMES + " ("
                        + Time._ID
                        + "," + Time.PUBLISHER_ID
                        + "," + Time.ENTRY_TYPE_ID
                        + "," + Time.DATE_START
                        + "," + Time.DATE_END
                        + "," + Time.TIME_START
                        + "," + Time.TIME_END
                        + ")"
                        + " SELECT "
                        + Time._ID
                        + "," + Time.PUBLISHER_ID
                        + "," + Time.ENTRY_TYPE_ID
                        + "," + Time.DATE_START
                        + "," + Time.DATE_END
                        + "," + Time.TIME_START
                        + "," + Time.TIME_END
                        + " FROM " + Tables.TIMES + "_tmp"
        );
    }

    public void updateRollover(SQLiteDatabase db) {
        db.execSQL("INSERT INTO " + Tables.ROLLOVER + " ("
                        + Rollover._ID
                        + "," + Rollover.PUBLISHER_ID
                        + "," + Rollover.DATE
                        + "," + Rollover.MINUTES
                        + ")"
                        + " SELECT "
                        + Rollover._ID
                        + "," + Rollover.PUBLISHER_ID
                        + "," + Rollover.DATE
                        + "," + Rollover.MINUTES
                        + " FROM " + Tables.ROLLOVER + "_tmp"
        );
    }

    public static void versionBackup(int version) {
        File intDB = mContext.getDatabasePath(MinistryDatabase.DATABASE_NAME);
        File extDB = FileUtils.getExternalDBFile(mContext, "auto-db-v" + version + ".db");

        try {
            if(extDB != null) {
                if(!extDB.exists())
                    extDB.createNewFile();

                FileUtils.copyFile(intDB, extDB);
                Toast.makeText(mContext, mContext.getString(R.string.toast_db_updated_successfully), Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            Toast.makeText(mContext, mContext.getString(R.string.toast_export_text_error), Toast.LENGTH_SHORT).show();
        }
    }
}