package com.myMinistry.provider;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.provider.BaseColumns;

import com.myMinistry.Helper;
import com.myMinistry.provider.MinistryContract.EntryType;
import com.myMinistry.provider.MinistryContract.Householder;
import com.myMinistry.provider.MinistryContract.Joins;
import com.myMinistry.provider.MinistryContract.LeftJoins;
import com.myMinistry.provider.MinistryContract.Literature;
import com.myMinistry.provider.MinistryContract.LiteraturePlaced;
import com.myMinistry.provider.MinistryContract.LiteratureType;
import com.myMinistry.provider.MinistryContract.Notes;
import com.myMinistry.provider.MinistryContract.PioneeringType;
import com.myMinistry.provider.MinistryContract.Publisher;
import com.myMinistry.provider.MinistryContract.Qualified;
import com.myMinistry.provider.MinistryContract.Rollover;
import com.myMinistry.provider.MinistryContract.Time;
import com.myMinistry.provider.MinistryContract.TimeHouseholder;
import com.myMinistry.provider.MinistryContract.UnionsNameAsCols;
import com.myMinistry.provider.MinistryContract.UnionsNameAsRef;
import com.myMinistry.provider.MinistryDatabase.Tables;
import com.myMinistry.util.FileUtils;
import com.myMinistry.util.TimeUtils;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Locale;

public class MinistryService {
    public static final int ACTIVE = 1;
    public static final int INACTIVE = 0;
    private final Context context;
    private static SQLiteDatabase sqlDB;

    public MinistryService(Context context) {
        this.context = context;
    }

    public MinistryService openWritable() throws SQLException {
        sqlDB = MinistryDatabase.getInstance(context).getWritableDatabase();
        return this;
    }

    public boolean isOpen() {
        return sqlDB == null ? false : sqlDB.isOpen();
    }

    public void close() {
        if(sqlDB != null && isOpen())
            sqlDB.close();
    }

    public Cursor fetchActivePublishers() {
        return sqlDB.query(Tables.PUBLISHERS, new String[] {Publisher._ID, Publisher.NAME, Publisher.ACTIVE, Publisher.GENDER }, Publisher.ACTIVE + "=" + ACTIVE, null, null, null, Publisher.DEFAULT_SORT, null);
    }

    public Cursor fetchAllPublishers(SQLiteDatabase db) {
        return db.query(Tables.PUBLISHERS, new String[] {Publisher._ID, Publisher.NAME, Publisher.ACTIVE, Publisher.GENDER }, null, null, null, null, Publisher.DEFAULT_SORT, null);
    }

    public Cursor fetchAllPublishers() {
        return sqlDB.query(Tables.PUBLISHERS, new String[] {Publisher._ID, Publisher.NAME, Publisher.ACTIVE, Publisher.GENDER }, null, null, null, null, Publisher.DEFAULT_SORT, null);
    }

    public Cursor fetchAllPublishersWithActivityDates() {
        String sql =	"SELECT " + Publisher._ID + "," + Publisher.NAME + UnionsNameAsCols.TITLE + "," + Publisher.ACTIVE + UnionsNameAsCols.ACTIVE
                + ", (SELECT " + Qualified.TIME_DATE_START + " FROM " + Tables.TIMES + " WHERE " + Qualified.TIME_PUBLISHER_ID + " = " + Qualified.PUBLISHER_ID + " ORDER BY " + Qualified.TIME_DATE_START + " DESC LIMIT 1)" + UnionsNameAsCols.DATE
                + "," + MinistryDatabase.ID_UNION_TYPE_PERSON + UnionsNameAsCols.TYPE_ID
                + " FROM " + Tables.PUBLISHERS
                + " ORDER BY " + Publisher.DEFAULT_SORT;

        return sqlDB.rawQuery(sql, null);
    }

    public int fetchBooksPlacedCountForPublisher(String formattedDate, String timeFrame, int publisherId) {
        int retVal = 0;
        String sql	=	"SELECT SUM(" + Qualified.PLACED_LITERATURE_COUNT + " * " + Qualified.LITERATURE_WEIGHT + ") AS " + LiteraturePlaced.COUNT
                + " FROM " + Tables.PLACED_LITERATURE
                + Joins.LITERATURE_JOIN_PLACED_LITERATURE
                + Joins.TYPE_LITERATURE_JOIN_LITERATURE
                + " WHERE " + Qualified.TYPE_OF_LITERATURE_ID + " = 1"
                + " AND " + Qualified.PLACED_LITERATURE_PUBLISHER_ID + " = " + publisherId
                + " AND date(" + Qualified.PLACED_LITERATURE_DATE + ") >= date('" + formattedDate + "','start of month')"
                + " AND date(" + Qualified.PLACED_LITERATURE_DATE + ") < date('" + formattedDate + "','start of month','+1 " + timeFrame + "')";

        Cursor record = sqlDB.rawQuery(sql, null);
        if(record.moveToFirst())
            retVal = record.getInt(0);
        if(record != null && !record.isClosed())
            record.close();
        return retVal;
    }

    public int fetchBrochuresPlacedCountForPublisher(String formattedDate, String timeFrame, int publisherId) {
        int retVal = 0;
        String sql	=	"SELECT SUM(" + Qualified.PLACED_LITERATURE_COUNT + " * " + Qualified.LITERATURE_WEIGHT + ") AS " + LiteraturePlaced.COUNT
                + " FROM " + Tables.PLACED_LITERATURE
                + Joins.LITERATURE_JOIN_PLACED_LITERATURE
                + Joins.TYPE_LITERATURE_JOIN_LITERATURE
                + " WHERE " + Qualified.TYPE_OF_LITERATURE_ID + " = 2"
                + " AND " + Qualified.PLACED_LITERATURE_PUBLISHER_ID + " = " + publisherId
                + " AND date(" + Qualified.PLACED_LITERATURE_DATE + ") >= date('" + formattedDate + "','start of month')"
                + " AND date(" + Qualified.PLACED_LITERATURE_DATE + ") < date('" + formattedDate + "','start of month','+1 " + timeFrame + "')";

        Cursor record = sqlDB.rawQuery(sql, null);
        if(record.moveToFirst())
            retVal = record.getInt(0);
        if(record != null && !record.isClosed())
            record.close();
        return retVal;
    }

    public int fetchMagazinesPlacedCountForPublisher(String formattedDate, String timeFrame, int publisherId) {
        int retVal = 0;
        String sql	=	"SELECT SUM(" + Qualified.PLACED_LITERATURE_COUNT + " * " + Qualified.LITERATURE_WEIGHT + ") AS " + LiteraturePlaced.COUNT
                + " FROM " + Tables.PLACED_LITERATURE
                + Joins.LITERATURE_JOIN_PLACED_LITERATURE
                + Joins.TYPE_LITERATURE_JOIN_LITERATURE
                + " WHERE " + Qualified.TYPE_OF_LITERATURE_ID + " = 3"
                + " AND " + Qualified.PLACED_LITERATURE_PUBLISHER_ID + " = " + publisherId
                + " AND date(" + Qualified.PLACED_LITERATURE_DATE + ") >= date('" + formattedDate + "','start of month')"
                + " AND date(" + Qualified.PLACED_LITERATURE_DATE + ") < date('" + formattedDate + "','start of month','+1 " + timeFrame + "')";

        Cursor record = sqlDB.rawQuery(sql, null);
        if(record.moveToFirst())
            retVal = record.getInt(0);
        if(record != null && !record.isClosed())
            record.close();
        return retVal;
    }

    public int fetchReturnVisitCountForPublisher(String formattedDate, String timeFrame, int publisherId) {
        int retVal = 0;
        String sql = 	"SELECT COUNT(" + Qualified.TIMEHOUSEHOLDER_ID + ")"
                + " FROM " + Tables.TIME_HOUSEHOLDERS
                + Joins.TIME_JOIN_TIMEHOUSEHOLDER
                + " WHERE " + Time.PUBLISHER_ID + " = " + publisherId
                + " AND date(" + Time.DATE_START + ") >= date('" + formattedDate + "','start of month')"
                + " AND date(" + Time.DATE_START + ") < date('" + formattedDate + "','start of month','+1 " + timeFrame + "')"
                + " AND " + Qualified.TIMEHOUSEHOLDER_IS_RETURN_VISIT + " = 1"
                + " AND " + Qualified.TIMEHOUSEHOLDER_HOUSEHOLDER_ID + " <> " + MinistryDatabase.NO_HOUSEHOLDER_ID;

        Cursor record = sqlDB.rawQuery(sql, null);

        if(record.moveToFirst())
            retVal = record.getInt(0);

        if(record != null && !record.isClosed())
            record.close();
        return retVal;
    }

    public int fetchStudyCountForPublisher(String formattedDate, String timeFrame, int publisherId) {
        int retVal = 0;
        String sql	= 	"SELECT " + Qualified.TIMEHOUSEHOLDER_ID
                + " , strftime('%m', date(startDate)) as month"
                + " FROM " + Tables.TIME_HOUSEHOLDERS
                + Joins.TIME_JOIN_TIMEHOUSEHOLDER
                + " WHERE " + Time.PUBLISHER_ID + " = " + publisherId
                + " AND " + TimeHouseholder.STUDY + " = " + ACTIVE
                + " AND date(" + Time.DATE_START + ") >= date('" + formattedDate + "','start of month')"
                + " AND date(" + Time.DATE_START + ") < date('" + formattedDate + "','start of month','+1 " + timeFrame + "')"
                + " AND " + Qualified.TIMEHOUSEHOLDER_HOUSEHOLDER_ID + " <> 0"
                + " AND " + Qualified.TIMEHOUSEHOLDER_IS_RETURN_VISIT + " = 1"
                + " GROUP BY month, " + TimeHouseholder.HOUSEHOLDER_ID;

        Cursor record = sqlDB.rawQuery(sql, null);
        retVal = record.getCount();
        if(record != null && !record.isClosed())
            record.close();
        return retVal;
    }

    public Cursor fetchListOfHoursForPublisher(String formattedDate, int publisherId, String timeFrame) {
        String sql1 =	"SELECT " + Time.DATE_START + "||\" \"||" + Time.TIME_START + UnionsNameAsCols.DATE_START + "," + Time.DATE_END + "||\" \"||" + Time.TIME_END + UnionsNameAsCols.DATE_END + "," + Qualified.TIME_ID
                + " FROM " + Tables.TIMES
                + " INNER JOIN " + Tables.ENTRY_TYPES + " ON " + Qualified.ENTRY_TYPE_ID + " = " + Qualified.TIME_ENTRY_TYPE_ID
                + " WHERE date(" + Time.DATE_START + ") >= date('" + formattedDate + "','start of month')"
                + " AND date(" + Time.DATE_START + ") < date('" + formattedDate + "','start of month','+1 " + timeFrame + "')"
                + " AND " + Qualified.TIME_PUBLISHER_ID + " = " + publisherId
                + " AND " + Qualified.ENTRY_TYPE_RBC + " <> 1"
                + " UNION "
                + " SELECT " + Time.DATE_START + "||\" \"||" + Time.TIME_START + UnionsNameAsCols.DATE_START + "," + Time.DATE_END + "||\" \"||" + Time.TIME_END + UnionsNameAsCols.DATE_END + "," + Time._ID
                + " FROM " + Tables.TIMES
                + " WHERE date(" + Time.DATE_START + ") >= date('" + formattedDate + "','start of month')"
                + " AND date(" + Time.DATE_START + ") < date('" + formattedDate + "','start of month','+1 " + timeFrame + "')"
                + " AND " + Time.PUBLISHER_ID + " = " + publisherId
                + " AND " + Time.ENTRY_TYPE_ID + " = 0";

        return sqlDB.rawQuery(sql1, null);
    }

    public Cursor fetchListOfHoursForPublisherNoRollover(String formattedDate, int publisherId, String timeFrame) {
        String sql1 =	"SELECT " + Time.DATE_START + "||\" \"||" + Time.TIME_START + UnionsNameAsCols.DATE_START + "," + Time.DATE_END + "||\" \"||" + Time.TIME_END + UnionsNameAsCols.DATE_END + "," + Qualified.TIME_ID
                + " FROM " + Tables.TIMES
                + " INNER JOIN " + Tables.ENTRY_TYPES + " ON " + Qualified.ENTRY_TYPE_ID + " = " + Qualified.TIME_ENTRY_TYPE_ID
                + " WHERE date(" + Time.DATE_START + ") >= date('" + formattedDate + "','start of month')"
                + " AND date(" + Time.DATE_START + ") < date('" + formattedDate + "','start of month','+1 " + timeFrame + "')"
                + " AND " + Qualified.TIME_PUBLISHER_ID + " = " + publisherId
                + " AND " + Qualified.ENTRY_TYPE_RBC + " <> 1"
                + " AND " + Qualified.TIME_ENTRY_TYPE_ID + " <> " + MinistryDatabase.ID_ROLLOVER
                + " UNION "
                + " SELECT " + Time.DATE_START + "||\" \"||" + Time.TIME_START + UnionsNameAsCols.DATE_START + "," + Time.DATE_END + "||\" \"||" + Time.TIME_END + UnionsNameAsCols.DATE_END + "," + Time._ID
                + " FROM " + Tables.TIMES
                + " WHERE date(" + Time.DATE_START + ") >= date('" + formattedDate + "','start of month')"
                + " AND date(" + Time.DATE_START + ") < date('" + formattedDate + "','start of month','+1 " + timeFrame + "')"
                + " AND " + Time.PUBLISHER_ID + " = " + publisherId
                + " AND " + Time.ENTRY_TYPE_ID + " = 0";

        return sqlDB.rawQuery(sql1, null);
    }

    public Cursor fetchListOfRBCHoursForPublisher(String formattedDate, int publisherId) {
        String sql1 =	"SELECT " + Time.DATE_START + "||\" \"||" + Time.TIME_START + UnionsNameAsCols.DATE_START + "," + Time.DATE_END + "||\" \"||" + Time.TIME_END + UnionsNameAsCols.DATE_END + "," + Qualified.TIME_ID
                + " FROM " + Tables.TIMES
                + " INNER JOIN " + Tables.ENTRY_TYPES + " ON " + Qualified.ENTRY_TYPE_ID + " = " + Qualified.TIME_ENTRY_TYPE_ID
                + " WHERE date(" + Time.DATE_START + ") >= date('" + formattedDate + "','start of month')"
                + " AND date(" + Time.DATE_START + ") < date('" + formattedDate + "','start of month','+1 month')"
                + " AND " + Qualified.TIME_PUBLISHER_ID + " = " + publisherId
                + " AND " + Qualified.ENTRY_TYPE_RBC + " = 1";

        return sqlDB.rawQuery(sql1, null);
    }

    public Cursor fetchListOfRBCHoursForPublisher(String formattedDate, int publisherId, String timeFrame) {
        String sql1 =	"SELECT " + Time.DATE_START + "||\" \"||" + Time.TIME_START + UnionsNameAsCols.DATE_START + "," + Time.DATE_END + "||\" \"||" + Time.TIME_END + UnionsNameAsCols.DATE_END + "," + Qualified.TIME_ID
                + " FROM " + Tables.TIMES
                + " INNER JOIN " + Tables.ENTRY_TYPES + " ON " + Qualified.ENTRY_TYPE_ID + " = " + Qualified.TIME_ENTRY_TYPE_ID
                + " WHERE date(" + Time.DATE_START + ") >= date('" + formattedDate + "','start of month')"
                + " AND date(" + Time.DATE_START + ") < date('" + formattedDate + "','start of month','+1 " + timeFrame + "')"
                + " AND " + Qualified.TIME_PUBLISHER_ID + " = " + publisherId
                + " AND " + Qualified.ENTRY_TYPE_RBC + " = 1";

        return sqlDB.rawQuery(sql1, null);
    }

    public int fetchRecordCountOfRBCHoursForMonthForPublisher(String formattedDate, int publisherId) {
        String sql =	"SELECT COUNT(" + Qualified.TIME_ID + ")"
                + " FROM " + Tables.TIMES
                + Joins.ENTRY_TYPES_ON_TIME
                + " WHERE date(" + Qualified.TIME_DATE_START + ") >= date('" + formattedDate + "','start of month')"
                + " AND date(" + Qualified.TIME_DATE_START + ") < date('" + formattedDate + "','start of month','+1 month')"
                + " AND " + Qualified.TIME_PUBLISHER_ID + " = " + publisherId
                + " AND " + Qualified.ENTRY_TYPE_RBC + " = 1";
        Cursor cursor = sqlDB.rawQuery(sql, null);
        if(cursor.moveToFirst())
            return cursor.getInt(0);
        else
            return 0;

    }

    public Cursor fetchListOfHoursForMonthForPublisher(SQLiteDatabase db, String formattedDate, int publisherId) {
        String sql1 =	"SELECT " + Time.DATE_START + "||\" \"||" + Time.TIME_START + UnionsNameAsCols.DATE_START + "," + Time.DATE_END + "||\" \"||" + Time.TIME_END + UnionsNameAsCols.DATE_END + "," + Qualified.TIME_ID
                + " FROM " + Tables.TIMES
                + " INNER JOIN " + Tables.ENTRY_TYPES + " ON " + Qualified.ENTRY_TYPE_ID + " = " + Qualified.TIME_ENTRY_TYPE_ID
                + " WHERE date(" + Time.DATE_START + ") >= date('" + formattedDate + "','start of month')"
                + " AND date(" + Time.DATE_START + ") < date('" + formattedDate + "','start of month','+1 month')"
                + " AND " + Qualified.TIME_PUBLISHER_ID + " = " + publisherId
                + " AND " + Qualified.ENTRY_TYPE_RBC + " <> 1"
                + " UNION "
                + " SELECT " + Time.DATE_START + "||\" \"||" + Time.TIME_START + UnionsNameAsCols.DATE_START + "," + Time.DATE_END + "||\" \"||" + Time.TIME_END + UnionsNameAsCols.DATE_END + "," + Time._ID
                + " FROM " + Tables.TIMES
                + " WHERE date(" + Time.DATE_START + ") >= date('" + formattedDate + "','start of month')"
                + " AND date(" + Time.DATE_START + ") < date('" + formattedDate + "','start of month','+1 month')"
                + " AND " + Time.PUBLISHER_ID + " = " + publisherId
                + " AND " + Time.ENTRY_TYPE_ID + " = 0";

        return db.rawQuery(sql1, null);
    }

    public Cursor fetchHoursForYearForPublisher(String formattedDate, int publisherId) {
        String sql1 =	"SELECT " + Time.DATE_START + "||\" \"||" + Time.TIME_START + UnionsNameAsCols.DATE_START + "," + Time.DATE_END + "||\" \"||" + Time.TIME_END + UnionsNameAsCols.DATE_END + "," + Qualified.TIME_ID
                + " FROM " + Tables.TIMES
                + " INNER JOIN " + Tables.ENTRY_TYPES + " ON " + Qualified.ENTRY_TYPE_ID + " = " + Qualified.TIME_ENTRY_TYPE_ID
                + " WHERE date(" + Time.DATE_START + ") >= date('" + formattedDate + "','start of month')"
                + " AND date(" + Time.DATE_START + ") < date('" + formattedDate + "','start of month','+1 year')"
                + " AND " + Qualified.TIME_PUBLISHER_ID + " = " + publisherId
                + " AND " + Qualified.ENTRY_TYPE_RBC + " <> 1"
                + " UNION "
                + " SELECT " + Time.DATE_START + "||\" \"||" + Time.TIME_START + UnionsNameAsCols.DATE_START + "," + Time.DATE_END + "||\" \"||" + Time.TIME_END + UnionsNameAsCols.DATE_END + "," + Time._ID
                + " FROM " + Tables.TIMES
                + " WHERE date(" + Time.DATE_START + ") >= date('" + formattedDate + "','start of month')"
                + " AND date(" + Time.DATE_START + ") < date('" + formattedDate + "','start of month','+1 year')"
                + " AND " + Time.PUBLISHER_ID + " = " + publisherId
                + " AND " + Time.ENTRY_TYPE_ID + " = 0";

        return sqlDB.rawQuery(sql1, null);
    }

    public Cursor fetchPublisher(int _id) {
        return sqlDB.query(Tables.PUBLISHERS
                , new String[]{Publisher._ID, Publisher.NAME, Publisher.ACTIVE, Publisher.GENDER}
                , Publisher._ID + " = " + _id
                , null
                , null
                , null
                , null
                , "1");
    }

    public long createPublisher(ContentValues values) {
        return sqlDB.insert(Tables.PUBLISHERS, null, values);
    }

    public long createPublication(ContentValues values) {
        return sqlDB.insert(Tables.LITERATURE,null,values);
    }

    public Cursor fetchActiveHouseholders() {
        return sqlDB.query(Tables.HOUSEHOLDERS, new String[]{Householder._ID, Householder.NAME}, Householder.ACTIVE + " = 1", null, null, null, Householder.DEFAULT_SORT, null);
    }

    public Cursor fetchAllHouseholdersWithActivityDates() {
        String sql =	"SELECT " + Householder._ID + "," + Householder.NAME + UnionsNameAsCols.TITLE + "," + Householder.ACTIVE + UnionsNameAsCols.ACTIVE
                + ", (SELECT " + Qualified.TIME_DATE_START + " FROM " + Tables.TIMES + Joins.TIMEHOUSEHOLDER_JOIN_TIME + " WHERE " + Qualified.TIMEHOUSEHOLDER_HOUSEHOLDER_ID + " = " + Qualified.HOUSEHOLDER_ID + " ORDER BY " + Qualified.TIME_DATE_START + " DESC LIMIT 1)" + UnionsNameAsCols.DATE
                + "," + MinistryDatabase.ID_UNION_TYPE_PERSON + UnionsNameAsCols.TYPE_ID
                + " FROM " + Tables.HOUSEHOLDERS
                + " ORDER BY " + Householder.DEFAULT_SORT;

        return sqlDB.rawQuery(sql, null);
    }

    public Cursor fetchHouseholder(int _id) {
        return sqlDB.query(Tables.HOUSEHOLDERS
                , new String[]{Householder._ID, Householder.NAME, Householder.ADDR, Householder.MOBILE_PHONE, Householder.HOME_PHONE, Householder.WORK_PHONE, Householder.OTHER_PHONE, Householder.ACTIVE}
                , Householder._ID + " = " + _id
                , null
                , null
                , null
                , null
                , "1");
    }

    public long createHouseholder(ContentValues values) {
        return sqlDB.insert(Tables.HOUSEHOLDERS, null, values);
    }

    public Cursor fetchTimeEntry(int _id) {
        return sqlDB.query(Tables.TIMES
                , new String[]{Time._ID, Time.PUBLISHER_ID, Time.ENTRY_TYPE_ID, Time.DATE_START, Time.DATE_END, Time.TIME_START, Time.TIME_END}
                , Time._ID + " = " + _id
                , null
                , null
                , null
                , null
                , "1");
    }

    public Cursor fetchActiveEntryTypes() {
        return sqlDB.query(Tables.ENTRY_TYPES, new String[] {EntryType._ID,EntryType.NAME}, EntryType.ACTIVE + "=" + ACTIVE, null, null, null, EntryType.DEFAULT_SORT, null);
    }

    public Cursor fetchAllEntryTypes() {
        return sqlDB.query(Tables.ENTRY_TYPES, new String[] {EntryType._ID,EntryType.NAME,EntryType.ACTIVE}, null, null, null, null, EntryType.DEFAULT_SORT);
    }

    public Cursor fetchAllEntryTypes(String sort) {
        String sql =	"SELECT " + EntryType._ID + "," + EntryType.NAME + "," + EntryType.ACTIVE
                + " FROM " + Tables.ENTRY_TYPES
                + " ORDER BY " + EntryType.NAME + " " + sort;

        return sqlDB.rawQuery(sql, null);
    }

    public Cursor fetchAllEntryTypesByPopularity() {
        String sql =	"SELECT " + EntryType._ID + ", (SELECT COUNT(" + Qualified.TIME_ID + ") FROM " + Tables.TIMES + " WHERE " + Qualified.TIME_ENTRY_TYPE_ID + "=" + Qualified.ENTRY_TYPE_ID + ") AS thecount"
                + " FROM " + Tables.ENTRY_TYPES
                + " ORDER BY thecount DESC";

        return sqlDB.rawQuery(sql, null);
    }

    public Cursor fetchAllHouseholdersWithActivityDates(String sort) {
        String sql =	"SELECT " + Householder._ID + "," + Householder.NAME + UnionsNameAsCols.TITLE + "," + Householder.ACTIVE + UnionsNameAsCols.ACTIVE
                + ", (SELECT " + Qualified.TIME_DATE_START + " FROM " + Tables.TIMES + Joins.TIMEHOUSEHOLDER_JOIN_TIME + " WHERE " + Qualified.TIMEHOUSEHOLDER_HOUSEHOLDER_ID + " = " + Qualified.HOUSEHOLDER_ID + " ORDER BY " + Qualified.TIME_DATE_START + " DESC LIMIT 1)" + UnionsNameAsCols.DATE
                + "," + MinistryDatabase.ID_UNION_TYPE_PERSON + UnionsNameAsCols.TYPE_ID
                + " FROM " + Tables.HOUSEHOLDERS
                + " ORDER BY " + UnionsNameAsRef.DATE + " " + sort;

        return sqlDB.rawQuery(sql, null);
    }

    public Cursor fetchAllHouseholders(String sort) {
        String sql =	"SELECT " + Householder._ID
                + " FROM " + Tables.HOUSEHOLDERS
                + " ORDER BY " + Householder.NAME + " " + sort;

        return sqlDB.rawQuery(sql, null);
    }

    public Cursor fetchAllPublicationTypes() {
        return sqlDB.query(Tables.TYPES_OF_LIERATURE, new String[]{LiteratureType._ID, LiteratureType.NAME, LiteratureType.ACTIVE, LiteratureType.DEFAULT}, null, null, null, null, LiteratureType.DEFAULT_SORT);
    }

    public Cursor fetchAllPublicationTypes(String sort) {
        String sql =	"SELECT " + LiteratureType._ID + "," + LiteratureType.NAME + "," + LiteratureType.ACTIVE
                + " FROM " + Tables.TYPES_OF_LIERATURE
                + " ORDER BY " + LiteratureType.NAME + " " + sort;

        return sqlDB.rawQuery(sql, null);
    }

    public Cursor fetchAllPublicationTypesByPopularity() {
        String sql =	"SELECT " + LiteratureType._ID + ", (SELECT COUNT(" + Qualified.PLACED_LITERATURE_ID + ") FROM " + Tables.PLACED_LITERATURE
                + Joins.LITERATURE_JOIN_PLACED_LITERATURE
                + " WHERE " + Qualified.LITERATURE_TYPE_ID_LINK + "=" + Qualified.TYPE_OF_LITERATURE_ID + ") AS thecount"
                + " FROM " + Tables.TYPES_OF_LIERATURE
                + " ORDER BY thecount DESC";

        return sqlDB.rawQuery(sql, null);
    }

    public Cursor fetchAllEntryTypesButID(int id) {
        return sqlDB.query(Tables.ENTRY_TYPES, new String[]{EntryType._ID, EntryType.NAME}, EntryType._ID + " NOT IN (" + id + "," + MinistryDatabase.ID_ROLLOVER + ")", null, null, null, EntryType.DEFAULT_SORT);
    }

    public Cursor fetchEntryType(int _id) {
        return sqlDB.query(Tables.ENTRY_TYPES
                ,new String[] {EntryType._ID,EntryType.NAME,EntryType.ACTIVE,EntryType.RBC,EntryType.SORT_ORDER}
                ,EntryType._ID + " = " + _id
                ,null
                ,null
                ,null
                ,null
                ,"1");
    }

    public Cursor fetchActiveTypesOfLiterature() {
        return sqlDB.query(Tables.TYPES_OF_LIERATURE, LiteratureType.All_COLS, LiteratureType.ACTIVE + "=" + ACTIVE, null, null, null, LiteratureType.DEFAULT_SORT, null);
    }

    public Cursor fetchTypesOfLiteratureCountsForPublisher(int publisherId, String formattedDate, String timeFrame) {
        String sql =  "SELECT " + Qualified.TYPE_OF_LITERATURE_ID + "," + Qualified.TYPE_OF_LITERATURE_NAME +	","
                + "(SELECT SUM(" + Qualified.PLACED_LITERATURE_COUNT + " * " + Qualified.LITERATURE_WEIGHT + ") AS " + LiteraturePlaced.COUNT
                + " FROM " + Tables.PLACED_LITERATURE
                + Joins.LITERATURE_JOIN_PLACED_LITERATURE
                + " WHERE " + Qualified.LITERATURE_TYPE_ID_LINK + " = " + Qualified.TYPE_OF_LITERATURE_ID
                + " AND " + Qualified.PLACED_LITERATURE_PUBLISHER_ID + " = " + publisherId
                + " AND date(" + Qualified.PLACED_LITERATURE_DATE + ") >= date('" + formattedDate + "','start of month')"
                + " AND date(" + Qualified.PLACED_LITERATURE_DATE + ") < date('" + formattedDate + "','start of month','+1 " + timeFrame + "'))"
                + " FROM " + Tables.TYPES_OF_LIERATURE
                + " WHERE " + LiteratureType.ACTIVE + " = " + ACTIVE
                + " AND " + LiteratureType._ID + " <> " + MinistryDatabase.ID_VIDEOS_TO_SHOW
                + " ORDER BY " + LiteratureType.DEFAULT_SORT;

        return sqlDB.rawQuery(sql, null);
    }

    public int fetchPlacementsCountForPublisher(int publisherId, String formattedDate, String timeFrame) {
        int retVal = 0;
        String sql =  "SELECT SUM(" + Qualified.PLACED_LITERATURE_COUNT + " * " + Qualified.LITERATURE_WEIGHT + ") AS " + LiteraturePlaced.COUNT
                + " FROM " + Tables.PLACED_LITERATURE
                + Joins.LITERATURE_JOIN_PLACED_LITERATURE
                + " WHERE " + Qualified.PLACED_LITERATURE_PUBLISHER_ID + " = " + publisherId
                + " AND date(" + Qualified.PLACED_LITERATURE_DATE + ") >= date('" + formattedDate + "','start of month')"
                + " AND date(" + Qualified.PLACED_LITERATURE_DATE + ") < date('" + formattedDate + "','start of month','+1 " + timeFrame + "')"
                + " AND " + Qualified.LITERATURE_TYPE_ID_LINK + " <> " + MinistryDatabase.ID_VIDEOS_TO_SHOW;

        Cursor record = sqlDB.rawQuery(sql, null);
        if(record.moveToFirst())
            retVal = record.getInt(0);
        if(record != null && !record.isClosed())
            record.close();
        return retVal;
    }

    public int fetchVideoShowingsCountForPublisher(int publisherId, String formattedDate, String timeFrame) {
        int retVal = 0;
        String sql =  "SELECT SUM(" + Qualified.PLACED_LITERATURE_COUNT + " * " + Qualified.LITERATURE_WEIGHT + ") AS " + LiteraturePlaced.COUNT
                + " FROM " + Tables.PLACED_LITERATURE
                + Joins.LITERATURE_JOIN_PLACED_LITERATURE
                + " WHERE " + Qualified.PLACED_LITERATURE_PUBLISHER_ID + " = " + publisherId
                + " AND date(" + Qualified.PLACED_LITERATURE_DATE + ") >= date('" + formattedDate + "','start of month')"
                + " AND date(" + Qualified.PLACED_LITERATURE_DATE + ") < date('" + formattedDate + "','start of month','+1 " + timeFrame + "')"
                + " AND " + Qualified.LITERATURE_TYPE_ID_LINK + " = " + MinistryDatabase.ID_VIDEOS_TO_SHOW;

        Cursor record = sqlDB.rawQuery(sql, null);
        if(record.moveToFirst())
            retVal = record.getInt(0);
        if(record != null && !record.isClosed())
            record.close();
        return retVal;
    }

    public Cursor fetchEntryTypeCountsForPublisher(int publisherId, String formattedDate, String timeFrame) {
        String sql =  "SELECT " + EntryType._ID + "," + EntryType.NAME + "," + fetchStudyCountForPublisher(formattedDate, timeFrame, publisherId)
                + " FROM " + Tables.ENTRY_TYPES
                + " WHERE " + EntryType._ID + "=" + MinistryDatabase.ID_BIBLE_STUDY
                + " UNION "
                + " SELECT " + EntryType._ID + "," + EntryType.NAME + "," + fetchReturnVisitCountForPublisher(formattedDate, timeFrame, publisherId)
                + " FROM " + Tables.ENTRY_TYPES
                + " WHERE " + EntryType._ID + "=" + MinistryDatabase.ID_RETURN_VISIT
                + " UNION "
                + " SELECT " + EntryType._ID + "," + EntryType.NAME + "," + fetchRecordCountOfRBCHoursForMonthForPublisher(formattedDate, publisherId)
                + " FROM " + Tables.ENTRY_TYPES
                + " WHERE " + EntryType._ID + "=" + MinistryDatabase.ID_RBC;

        return sqlDB.rawQuery(sql, null);
    }

    public Cursor fetchTypeOfLiterature(int _id) {
        return sqlDB.query(Tables.TYPES_OF_LIERATURE
                ,new String[] {LiteratureType._ID,LiteratureType.NAME,LiteratureType.ACTIVE}
                ,LiteratureType._ID + " = " + _id
                ,null
                ,null
                ,null
                ,null
                ,"1");
    }

    public Cursor fetchLiteratureByType(int _typeID) {
        String sql =	"SELECT " + Qualified.LITERATURE_ID + "," + Qualified.LITERATURE_NAME + "," + Qualified.LITERATURE_ACTIVE
                + ", (SELECT " + Qualified.TIME_DATE_START + " FROM " + Tables.TIMES + Joins.PLACED_LITERATURE_ON_TIME + " WHERE " + Qualified.PLACED_LITERATURE_LIT_ID + " = " + Qualified.LITERATURE_ID + " ORDER BY " + Qualified.TIME_DATE_START + " DESC LIMIT 1) AS " + Time.DATE_START
                + " FROM " + Tables.LITERATURE
                + " WHERE " + Qualified.LITERATURE_TYPE_ID_LINK + " = " + _typeID
                + " ORDER BY " + Literature.DEFAULT_SORT;

        return sqlDB.rawQuery(sql, null);
    }

    public Cursor fetchAllPublications(String sort) {
        String sql =	"SELECT " + Qualified.LITERATURE_ID + "," + Qualified.LITERATURE_NAME
                + " FROM " + Tables.LITERATURE
                + " ORDER BY " + Qualified.LITERATURE_NAME + " " + sort;

        return sqlDB.rawQuery(sql, null);
    }

    public Cursor fetchPublicationsByPopularity() {
        String sql =	"SELECT " + Literature._ID + ", (SELECT SUM(" + Qualified.PLACED_LITERATURE_COUNT + "*" + Qualified.LITERATURE_WEIGHT + ") FROM " + Tables.PLACED_LITERATURE + " WHERE " + Qualified.PLACED_LITERATURE_LIT_ID + "=" + Qualified.LITERATURE_ID + ") AS thecount"
                + " FROM " + Tables.LITERATURE
                + " ORDER BY thecount DESC";

        return sqlDB.rawQuery(sql, null);
    }

    public Cursor fetchLiteratureByTypeWithActivityDates(int _typeID) {
        String sql =	"SELECT " + Qualified.LITERATURE_ID + "," + Qualified.LITERATURE_NAME + UnionsNameAsCols.TITLE + "," + Qualified.LITERATURE_ACTIVE + UnionsNameAsCols.ACTIVE
                + ", (SELECT " + Qualified.TIME_DATE_START + " FROM " + Tables.TIMES + Joins.PLACED_LITERATURE_ON_TIME + " WHERE " + Qualified.PLACED_LITERATURE_LIT_ID + " = " + Qualified.LITERATURE_ID + " ORDER BY " + Qualified.TIME_DATE_START + " DESC LIMIT 1)" +  UnionsNameAsCols.DATE
                + "," + MinistryDatabase.ID_UNION_TYPE_PERSON + UnionsNameAsCols.TYPE_ID
                + " FROM " + Tables.LITERATURE
                + " WHERE " + Qualified.LITERATURE_TYPE_ID_LINK + " = " + _typeID
                + " ORDER BY " + Literature.DEFAULT_SORT;

        return sqlDB.rawQuery(sql, null);
    }

    public Cursor fetchLiteratureByID(int _id) {
        return sqlDB.query(Tables.LITERATURE, new String[] {Literature._ID,Literature.NAME,Literature.TYPE_OF_LIERATURE_ID,Literature.ACTIVE,Literature.WEIGHT}, Literature._ID + " = " + _id, null, null, null, null, null);
    }

    public long createLiterature(ContentValues values) {
        return sqlDB.insert(Tables.LITERATURE,null,values);
    }

    public Cursor fetchRolloverRecord(int publisherId, String date) {
        return sqlDB.query(Tables.ROLLOVER, Rollover.All_COLS, Rollover.PUBLISHER_ID + " = " + publisherId + " AND " + Rollover.DATE + " = date('" + date + "','start of month')", null, null, null, null, "1");
    }

    public int fetchRolloverMinutes(int publisherId, String date) {
        int retVal = 0;
        Cursor record = sqlDB.query(Tables.ROLLOVER, new String[] {Rollover.MINUTES}, Rollover.PUBLISHER_ID + " = " + publisherId + " AND " + Rollover.DATE + " = date('" + date + "','start of month')", null, null, null, null, "1");
        if(record.moveToFirst())
            retVal = record.getInt(0);
        if(record != null && !record.isClosed())
            record.close();
        return retVal;
    }

    public long createRolloverMinutes(ContentValues values) {
        return sqlDB.insert(Tables.ROLLOVER,null,values);
    }

    public int saveRolloverMinutes(int _id, ContentValues values) {
        return sqlDB.update(Tables.ROLLOVER, values, BaseColumns._ID + " = " + _id, null);
    }

    public Cursor fetchRolloverRecord(SQLiteDatabase db, int publisherId, String date) {
        return db.query(Tables.ROLLOVER, Rollover.All_COLS, Rollover.PUBLISHER_ID + " = " + publisherId + " AND " + Rollover.DATE + " = date('" + date + "','start of month')", null, null, null, null, "1");
    }

    public long createRolloverMinutes(SQLiteDatabase db, ContentValues values) {
        return db.insert(Tables.ROLLOVER,null,values);
    }

    public int saveRolloverMinutes(SQLiteDatabase db, int _id, ContentValues values) {
        return db.update(Tables.ROLLOVER, values, BaseColumns._ID + " = " + _id, null);
    }



    public Cursor fetchMostRecentRolloverDate(int publisherId) {
        return sqlDB.query(Tables.ROLLOVER, new String[] {Rollover._ID,Rollover.PUBLISHER_ID,Rollover.DATE,Rollover.MINUTES}, Rollover.PUBLISHER_ID + " = " + publisherId, null, null, null, "date(" + Rollover.DATE + ") DESC", "1");
    }

    public Cursor fetchRolloverTimeEntry(int publisherId, String formattedDate) {
        return sqlDB.query(Tables.TIMES
                ,new String[] {Time._ID,Time.PUBLISHER_ID,Time.ENTRY_TYPE_ID,Time.DATE_START,Time.DATE_END,Time.TIME_START,Time.TIME_START}
                ,Time.PUBLISHER_ID + " = " + publisherId + " AND " + Time.ENTRY_TYPE_ID + " = " + MinistryDatabase.ID_ROLLOVER + " AND " + Time.DATE_START + " = '" + formattedDate + "'"
                ,null
                ,null
                ,null
                ,null
                ,"1");
    }

    public void removeTimeEntry(int _id) {
        sqlDB.delete(Tables.TIMES, Time._ID + " = " + _id, null);
    }

    public void removeTimeEntryDeep(int timeID) {
        sqlDB.delete(Tables.NOTES, Notes.TIME_ID + " = " + timeID, null);
        sqlDB.delete(Tables.TIMES, Time._ID + " = " + timeID, null);
        sqlDB.delete(Tables.PLACED_LITERATURE, LiteraturePlaced.TIME_ID + " = " + timeID, null);
        sqlDB.delete(Tables.TIME_HOUSEHOLDERS, TimeHouseholder.TIME_ID + " = " + timeID, null);
    }

    public long createEntryType(ContentValues values) {
        return sqlDB.insert(Tables.ENTRY_TYPES,null,values);
    }

    public long createLiteratureType(ContentValues values) {
        return sqlDB.insert(Tables.TYPES_OF_LIERATURE,null,values);
    }

    public Cursor fetchTimeEntriesByPublisherAndMonth(int publisherId, String date, String timeFrame) {
        String sql = 	"SELECT " + Qualified.TIME_ID
                + "," + Qualified.TIME_DATE_START
                + "," + Qualified.TIME_DATE_END
                + "," + Qualified.TIME_TIME_START
                + "," + Qualified.TIME_TIME_END
                + "," + Qualified.ENTRY_TYPE_NAME + UnionsNameAsCols.TITLE
                + "," + Qualified.TIME_ENTRY_TYPE_ID
                + " FROM " + Tables.TIMES
                + " LEFT OUTER JOIN " + Tables.ENTRY_TYPES + " ON " + Qualified.ENTRY_TYPE_ID + " = " + Qualified.TIME_ENTRY_TYPE_ID
                + " WHERE date(" + Time.DATE_START + ") >= date('" + date + "','start of month')"
                + " AND date(" + Time.DATE_START + ") < date('" + date + "','start of month','+1 " + timeFrame + "')"
                + " AND time." + Time.PUBLISHER_ID + " = " + publisherId
                + " ORDER BY date(" + Time.DATE_START + ") DESC, time(" + Time.TIME_END + ") DESC";

        return sqlDB.rawQuery(sql, null);
    }

    public Cursor fetchTimeEntriesByPublisher(int publisherId) {
        String sql = 	"SELECT " + Qualified.TIME_ID
                + "," + Qualified.TIME_DATE_START
                + "," + Qualified.TIME_DATE_END
                + "," + Qualified.TIME_TIME_START
                + "," + Qualified.TIME_TIME_END
                + "," + Qualified.ENTRY_TYPE_NAME + UnionsNameAsCols.TITLE
                + "," + Qualified.TIME_ENTRY_TYPE_ID
                + " FROM " + Tables.TIMES
                + " LEFT OUTER JOIN " + Tables.ENTRY_TYPES + " ON " + Qualified.ENTRY_TYPE_ID + " = " + Qualified.TIME_ENTRY_TYPE_ID
                + " ORDER BY date(" + Time.DATE_START + ") DESC, time(" + Time.TIME_END + ") DESC";

        return sqlDB.rawQuery(sql, null);
    }

    public Cursor fetchTimeEntriesByPublisherAndMonthNoRollover(int publisherId, String date, String timeFrame) {
        String sql = 	"SELECT " + Qualified.TIME_ID
                + "," + Qualified.TIME_DATE_START
                + "," + Qualified.TIME_DATE_END
                + "," + Qualified.TIME_TIME_START
                + "," + Qualified.TIME_TIME_END
                + "," + Qualified.ENTRY_TYPE_NAME + UnionsNameAsCols.TITLE
                + "," + Qualified.TIME_ENTRY_TYPE_ID
                + " FROM " + Tables.TIMES
                + " LEFT OUTER JOIN " + Tables.ENTRY_TYPES + " ON " + Qualified.ENTRY_TYPE_ID + " = " + Qualified.TIME_ENTRY_TYPE_ID
                + " WHERE date(" + Time.DATE_START + ") >= date('" + date + "','start of month')"
                + " AND date(" + Time.DATE_START + ") < date('" + date + "','start of month','+1 " + timeFrame + "')"
                + " AND time." + Time.PUBLISHER_ID + " = " + publisherId
                + " AND time." + Time.ENTRY_TYPE_ID + " <> " + MinistryDatabase.ID_ROLLOVER
                + " ORDER BY date(" + Time.DATE_START + ") DESC, time(" + Time.TIME_END + ") DESC";

        return sqlDB.rawQuery(sql, null);
    }

    public long createPlacedLiterature(ContentValues values) {
        return sqlDB.insert(Tables.PLACED_LITERATURE,null,values);
    }

    public void deleteTimeByID(int rowID) {
        sqlDB.delete(Tables.TIMES, "_id = " + rowID, null);
        sqlDB.delete(Tables.PLACED_LITERATURE, LiteraturePlaced.TIME_ID + "=" + rowID, null);
    }

    public boolean importDatabase(String dbPath, String packageName) throws IOException {
        /** Close the SQLiteOpenHelper so it will commit the created empty database to internal storage. */
        close();
        File newDb = new File(dbPath);
        File oldDb = new File(Environment.getDataDirectory(), "/data/" + packageName + "/databases/" + MinistryDatabase.DATABASE_NAME);

        if (newDb.exists()) {
            FileUtils.copyFile(newDb, oldDb);
            /** Access the copied database so SQLiteHelper will cache it and mark it as created. */
            sqlDB.close();
            return true;
        }
        else
            return false;
    }

    public boolean importDatabase(File newDB, File oldDB) throws IOException {
        /** Close the SQLiteOpenHelper so it will commit the created empty database to internal storage. */
        close();
        try {
            FileUtils.copyFile(newDB, oldDB);
            /** Access the copied database so SQLiteHelper will cache it and mark it as created. */
            close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public int saveLiterature(long _id, ContentValues values) {
        return sqlDB.update(Tables.LITERATURE, values, BaseColumns._ID + "=" + _id, null);
    }

    public int saveHouseholder(long _id, ContentValues values) {
        return sqlDB.update(Tables.HOUSEHOLDERS, values, BaseColumns._ID + "=" + _id, null);
    }

    public int savePublisher(long _id, ContentValues values) {
        return sqlDB.update(Tables.PUBLISHERS, values, BaseColumns._ID + "=" + _id, null);
    }

    public int saveTimeHouseholder(long _id, ContentValues values) {
        return sqlDB.update(Tables.TIME_HOUSEHOLDERS, values, BaseColumns._ID + "=" + _id, null);
    }

    public int saveTime(long _id, ContentValues values) {
        return sqlDB.update(Tables.TIMES, values, BaseColumns._ID + "=" + _id, null);
    }

    public int savePlacedLiterature(long _id, ContentValues values) {
        return sqlDB.update(Tables.PLACED_LITERATURE, values, BaseColumns._ID + "=" + _id, null);
    }

    public long createTimeHouseholder(ContentValues values) {
        return sqlDB.insert(Tables.TIME_HOUSEHOLDERS,null,values);
    }

    public long createTime(ContentValues values) {
        return sqlDB.insert(Tables.TIMES,null,values);
    }

    public int saveNotes(long _id, ContentValues values) {
        return sqlDB.update(Tables.NOTES, values, BaseColumns._ID + "=" + _id, null);
    }

    public long createNotes(ContentValues values) {
        return sqlDB.insert(Tables.NOTES,null,values);
    }

    public void deleteNoteByID(int _id) {
        sqlDB.delete(Tables.NOTES, Notes._ID + " = " + _id, null);
    }

    public int savePublicationType(long _id, ContentValues values) {
        return sqlDB.update(Tables.TYPES_OF_LIERATURE, values, BaseColumns._ID + "=" + _id, null);
    }

    public long createPublicationType(ContentValues values) {
        return sqlDB.insert(Tables.TYPES_OF_LIERATURE,null,values);
    }

    public void clearPublicationTypeDefault() {
        String sql = "UPDATE " + Tables.TYPES_OF_LIERATURE + " SET " + LiteratureType.DEFAULT + " = " + MinistryService.INACTIVE;
        sqlDB.execSQL(sql);
    }

    public int saveEntryType(long _id, ContentValues values) {
        return sqlDB.update(Tables.ENTRY_TYPES, values, BaseColumns._ID + "=" + _id, null);
    }

    public void deleteNoteByTimeAndHouseholderID(int timeID, int householderID) {
        sqlDB.delete(Tables.NOTES, Notes.TIME_ID + " = " + timeID + " AND " + Notes.HOUSEHOLDER_ID + " = " + householderID, null);
    }

    public Cursor fetchTimeHouseholdersForTimeByID(int timeID) {
        String sql =	"SELECT " + Qualified.TIMEHOUSEHOLDER_ID + "," + Qualified.TIMEHOUSEHOLDER_HOUSEHOLDER_ID + "," + Qualified.HOUSEHOLDER_NAME + "," + TimeHouseholder.STUDY + "," + Qualified.NOTES_ID + UnionsNameAsCols.NOTE_ID + "," + Notes.NOTES + "," + Qualified.TIMEHOUSEHOLDER_IS_RETURN_VISIT
                + " FROM " + Tables.TIMES
                + " LEFT JOIN " + Tables.TIME_HOUSEHOLDERS + " ON (" + Qualified.TIMEHOUSEHOLDER_TIME_ID + " IN (0," + timeID + "))"
                + LeftJoins.HOUSEHOLDERS_JOIN_TIMEHOUSEHOLDERS
                + " LEFT JOIN " + Tables.NOTES + " ON (" + Qualified.NOTES_HOUSEHOLDER_ID + " = " + Qualified.TIMEHOUSEHOLDER_HOUSEHOLDER_ID + " AND " + Qualified.NOTES_TIME_ID + " = " + Qualified.TIME_ID + ")"
                + " WHERE " + Qualified.TIME_ID + " = " + timeID
                + " AND " + Qualified.TIME_ENTRY_TYPE_ID + " <> " + MinistryDatabase.ID_ROLLOVER;
                //+ " AND " + Qualified.TIMEHOUSEHOLDER_ID + " IS NOT NULL";

        return sqlDB.rawQuery(sql, null);
    }

    public Cursor fetchActivityForHouseholder(int householderID) {
        String sql = 	"SELECT " + Qualified.TIME_ID
                + "," + Qualified.TIME_DATE_START
                + "," + Qualified.TIME_DATE_END
                + "," + Qualified.TIME_TIME_START
                + "," + Qualified.TIME_TIME_END
                + "," + Qualified.ENTRY_TYPE_NAME + " || ' - ' || " + Qualified.PUBLISHER_NAME + UnionsNameAsCols.TITLE
                + " FROM " + Tables.TIME_HOUSEHOLDERS
                + Joins.TIME_JOIN_TIMEHOUSEHOLDER
                + Joins.ENTRY_TYPES_ON_TIME
                + Joins.PUBLISHERS_ON_TIME
                + " WHERE " + Qualified.TIMEHOUSEHOLDER_HOUSEHOLDER_ID + "=" + householderID
                + " ORDER BY " + Qualified.TIME_DATE_START + " DESC, " + Qualified.TIME_TIME_START + " DESC, " + Qualified.ENTRY_TYPE_NAME;

        return sqlDB.rawQuery(sql, null);
    }

    public Cursor fetchActivityForHouseholderOLDDDDDDDDDDD(int householderID) {
        String sql = 	"SELECT " + Qualified.TIME_ID + "," + Qualified.TIME_DATE_START + "," + Qualified.NOTES_NOTES
                + ", " + Qualified.PUBLISHER_NAME + UnionsNameAsCols.PUBLISHER_NAME
                + ", " + Qualified.ENTRY_TYPE_NAME + UnionsNameAsCols.ENTRY_TYPE_NAME
                + ", (SELECT COUNT(" + Qualified.PLACED_LITERATURE_ID + ") FROM " + Tables.PLACED_LITERATURE + " WHERE " + Qualified.PLACED_LITERATURE_TIME_ID + " = " + Qualified.TIME_ID + " AND " + Qualified.PLACED_LITERATURE_HOUSEHOLDER_ID + " = " + Qualified.TIMEHOUSEHOLDER_HOUSEHOLDER_ID + ")" + UnionsNameAsCols.COUNT
                + " FROM " + Tables.TIME_HOUSEHOLDERS
                + Joins.TIME_JOIN_TIMEHOUSEHOLDER
                + Joins.ENTRY_TYPES_ON_TIME
                + Joins.PUBLISHERS_ON_TIME
                + LeftJoins.NOTES_ON_TIMEHOUSEHOLDER_AND_TIME
                + " WHERE " + Qualified.TIMEHOUSEHOLDER_HOUSEHOLDER_ID + "=" + householderID
                + " ORDER BY " + Qualified.TIME_DATE_START + " DESC, " + Qualified.TIME_TIME_START + " DESC, " + Qualified.ENTRY_TYPE_NAME;

        return sqlDB.rawQuery(sql, null);
    }

    public Cursor fetchActivityForPublisher(int publisherId) {
        String sql = 	"SELECT " + Qualified.TIME_ID
                + "," + Qualified.TIME_DATE_START
                + "," + Qualified.TIME_DATE_END
                + "," + Qualified.TIME_TIME_START
                + "," + Qualified.TIME_TIME_END
                + "," + Qualified.ENTRY_TYPE_NAME + UnionsNameAsCols.TITLE
                + " FROM " + Tables.TIMES
                + Joins.ENTRY_TYPES_ON_TIME
                + " WHERE " + Qualified.TIME_PUBLISHER_ID + "=" + publisherId
                + " AND " + Qualified.TIME_ENTRY_TYPE_ID + " <> " + MinistryDatabase.ID_ROLLOVER
                + " ORDER BY " + Qualified.TIME_DATE_START + " DESC," + Qualified.TIME_TIME_START + " DESC";

        return sqlDB.rawQuery(sql, null);
    }

    public Cursor fetchActivityForLiterature(int literatureID) {
        String sql = "SELECT " + Qualified.TIME_ID
                + "," + Qualified.TIME_DATE_START
                + "," + Qualified.TIME_DATE_END
                + "," + Qualified.TIME_TIME_START
                + "," + Qualified.TIME_TIME_END
                + "," + Qualified.ENTRY_TYPE_NAME + " || ' - ' || " + Qualified.PUBLISHER_NAME + UnionsNameAsCols.TITLE
                + " FROM " + Tables.TIMES
                + Joins.PLACED_LITERATURE_ON_TIME
                + Joins.ENTRY_TYPES_ON_TIME
                + Joins.PUBLISHERS_ON_PLACED_LITERATURE
                + " WHERE " + Qualified.PLACED_LITERATURE_LIT_ID + " = " + literatureID
                + " ORDER BY " + Qualified.TIME_DATE_START + " DESC, " + Qualified.TIME_TIME_START + " DESC, " + Qualified.ENTRY_TYPE_NAME;

        return sqlDB.rawQuery(sql, null);
    }

    public Cursor fetchActivityForLiteratureASDZfasdfasdfasdfasdf(int literatureID) {
        String sql =	"SELECT "+ Qualified.TIME_ID + UnionsNameAsCols._ID
                + ", " + Qualified.TIME_DATE_START + UnionsNameAsCols.DATE
                + ", " + Qualified.PUBLISHER_NAME + UnionsNameAsCols.PUBLISHER_NAME
                + ", " + Qualified.PLACED_LITERATURE_COUNT
                + ", " + Qualified.HOUSEHOLDER_NAME + UnionsNameAsCols.HOUSEHOLDER_NAME
                + ", " + Qualified.ENTRY_TYPE_NAME + UnionsNameAsCols.ENTRY_TYPE_NAME

                + " FROM " + Tables.LITERATURE
                + Joins.PLACED_LITERATURE_ON_LITERATURE_NAMES
                + Joins.TIME_ON_PLACED_LITERATURE
                + Joins.ENTRY_TYPES_ON_TIME
                + Joins.PUBLISHERS_ON_PLACED_LITERATURE
                + LeftJoins.HOUSEHOLDERS_JOIN_PLACED_LITERATURE
                + " WHERE " + Qualified.LITERATURE_ID + " = " + literatureID
                + " ORDER BY " + Qualified.TIME_DATE_START + " DESC, " + Qualified.ENTRY_TYPE_NAME + ", " + Qualified.HOUSEHOLDER_NAME;

        return sqlDB.rawQuery(sql, null);
    }

    public int fetchTimeHouseholderID(int timeID, int householderID) {
        int retVal = 0;
        String sql = 	"SELECT " + TimeHouseholder._ID
                + " FROM " + Tables.TIME_HOUSEHOLDERS
                + " WHERE " + TimeHouseholder.TIME_ID + "=" + timeID
                + " AND " + TimeHouseholder.HOUSEHOLDER_ID + "=" + householderID;
        Cursor cursor = sqlDB.rawQuery(sql, null);
        if(cursor.moveToFirst())
            retVal = cursor.getInt(cursor.getColumnIndex(TimeHouseholder._ID));
        cursor.close();
        return retVal;
    }

    public void deleteTimeHouseholderOrphans(int timeID, long[] householderIDs) {
        StringBuilder builder = new StringBuilder();
        builder.append("0");
        for(long i : householderIDs) {
            builder.append(",");
            builder.append(i);
        }
        sqlDB.delete(Tables.TIME_HOUSEHOLDERS, TimeHouseholder.TIME_ID + "=" + timeID + " AND " + TimeHouseholder._ID + " NOT IN (" + builder.toString() + ")", null);
    }

    public void deleteTimeHouseholderLiteraturePlacedOrphans(int timeID, int[] householderIDs) {
        StringBuilder builder = new StringBuilder();
        builder.append(MinistryDatabase.CREATE_ID);
        for(int i : householderIDs) {
            builder.append(",");
            builder.append(i);
        }
        sqlDB.delete(Tables.PLACED_LITERATURE, LiteraturePlaced.TIME_ID + "=" + timeID + " AND " + LiteraturePlaced.HOUSEHOLDER_ID + " NOT IN (" + builder.toString() + ")", null);
    }

    public void deleteTimeHouseholderNotesOrphans(int timeID, int[] householderIDs) {
        StringBuilder builder = new StringBuilder();
        builder.append(MinistryDatabase.CREATE_ID);
        for(int i : householderIDs) {
            builder.append(",");
            builder.append(i);
        }
        sqlDB.delete(Tables.NOTES, Notes.TIME_ID + "=" + timeID + " AND " + Notes.HOUSEHOLDER_ID + " NOT IN (" + builder.toString() + ")", null);
    }

    public int fetchPlacedLitByTimeAndHouseholderAndLitID(int timeID, int householderID, int litID) {
        int retVal = 0;
        String sql = 	"SELECT " + LiteraturePlaced._ID
                + " FROM " + Tables.PLACED_LITERATURE
                + " WHERE " + LiteraturePlaced.TIME_ID + "=" + timeID
                + " AND " + LiteraturePlaced.HOUSEHOLDER_ID + "=" + householderID
                + " AND " + LiteraturePlaced.LITERATURE_ID + "=" + litID;
        Cursor cursor = sqlDB.rawQuery(sql, null);
        if(cursor.moveToFirst())
            retVal = cursor.getInt(cursor.getColumnIndex(LiteraturePlaced._ID));
        cursor.close();
        return retVal;
    }

    public Cursor fetchPlacedLitByTimeAndHouseholderID(int timeID, int householderID) {
        String sql = 	"SELECT " + Qualified.PLACED_LITERATURE_ID + "," + Qualified.PLACED_LITERATURE_LIT_ID + "," + Qualified.PLACED_LITERATURE_COUNT + "," + Qualified.LITERATURE_NAME + "," + Qualified.LITERATURE_TYPE_ID_LINK
                + " FROM " + Tables.PLACED_LITERATURE
                + Joins.LITERATURE_JOIN_PLACED_LITERATURE
                + " WHERE " + LiteraturePlaced.TIME_ID + "=" + timeID
                + " AND " + LiteraturePlaced.HOUSEHOLDER_ID + "=" + householderID
                + " ORDER BY " + Literature.DEFAULT_SORT;
        return sqlDB.rawQuery(sql, null);
    }

    public void deletePlacedLiteratureOrphans(int timeID, int householderID, long[] placedIDs) {
        StringBuilder builder = new StringBuilder();
        builder.append("0");
        if(placedIDs != null) {
            for(long i : placedIDs) {
                builder.append(",");
                builder.append(i);
            }
        }
        String sql = 	"DELETE FROM " + Tables.PLACED_LITERATURE
                + " WHERE " + LiteraturePlaced.TIME_ID + "=" + timeID
                + " AND " + LiteraturePlaced.HOUSEHOLDER_ID + "=" + householderID
                + " AND " + LiteraturePlaced._ID + " NOT IN (" + builder.toString() + ")";
        sqlDB.rawQuery(sql, null);
    }

    public void deleteLiteratureByID(int litID) {
        sqlDB.delete(Tables.PLACED_LITERATURE, LiteraturePlaced.LITERATURE_ID + " = " + litID, null);
        sqlDB.delete(Tables.LITERATURE, Literature._ID + " = " + litID, null);
    }

    public void deleteHouseholderByID(int _id) {
        sqlDB.delete(Tables.PLACED_LITERATURE, LiteraturePlaced.HOUSEHOLDER_ID + " = " + _id, null);
        sqlDB.delete(Tables.TIME_HOUSEHOLDERS, TimeHouseholder.HOUSEHOLDER_ID + " = " + _id, null);
        sqlDB.delete(Tables.HOUSEHOLDERS, Householder._ID + " = " + _id, null);
    }

    public void deletePublisherByID(int _id) {
        String sql = "DELETE FROM " + Tables.TIME_HOUSEHOLDERS + " WHERE " + TimeHouseholder.TIME_ID + " IN (SELECT " + Time._ID + " FROM " + Tables.TIMES + " WHERE " + Time.PUBLISHER_ID + " = " + _id + ")";
        sqlDB.rawQuery(sql, null);

        sqlDB.delete(Tables.TIMES, Time.PUBLISHER_ID + " = " + _id, null);
        sqlDB.delete(Tables.PLACED_LITERATURE, LiteraturePlaced.PUBLISHER_ID + " = " + _id, null);
        sqlDB.delete(Tables.ROLLOVER, Rollover.PUBLISHER_ID + " = " + _id, null);
        sqlDB.delete(Tables.PUBLISHERS, Publisher._ID + " = " + _id, null);
    }

    public Cursor fetchNotesByTimeAndHousehodlerID(int timeID, int householderID) {
        String sql =	"SELECT " + Notes._ID + "," + Notes.NOTES
                + " FROM " + Tables.NOTES
                + " WHERE " + Notes.TIME_ID + " = " + timeID
                + " AND " + Notes.HOUSEHOLDER_ID + " = " + householderID;
        return sqlDB.rawQuery(sql, null);
    }

    public Cursor fetchPublicationTypeByID(long _id) {
        String sql =	"SELECT " + LiteratureType._ID + "," + LiteratureType.NAME + "," + LiteratureType.ACTIVE
                + " FROM " + Tables.TYPES_OF_LIERATURE
                + " WHERE " + LiteratureType._ID + " = " + _id;
        return sqlDB.rawQuery(sql, null);
    }

    public void reassignPublications(int origID, int newID) {
        ContentValues values = new ContentValues();
        values.put(Literature.TYPE_OF_LIERATURE_ID, newID);
        sqlDB.update(Tables.LITERATURE, values, Literature.TYPE_OF_LIERATURE_ID + "=" + origID, null);
    }

    public void removePublicationType(int id) {
        sqlDB.delete(Tables.TYPES_OF_LIERATURE, LiteratureType._ID + " = " + id, null);
    }

    public void reassignEntryType(int origID, int newID) {
        ContentValues values = new ContentValues();
        values.put(Time.ENTRY_TYPE_ID, newID);
        sqlDB.update(Tables.TIMES, values, Time.ENTRY_TYPE_ID + "=" + origID, null);
    }

    public Cursor fetchDefaultPublicationTypes() {
        return sqlDB.query(Tables.TYPES_OF_LIERATURE, new String[] {LiteratureType._ID,LiteratureType.NAME}, LiteratureType._ID + "<=" + MinistryDatabase.MAX_PUBLICATION_TYPE_ID, null, null, null, LiteratureType.DEFAULT_SORT);
    }

    public void deleteEntryTypeByID(int _id) {
        sqlDB.delete(Tables.ENTRY_TYPES, EntryType._ID + " = " + _id, null);
    }

    public void processRolloverTime(int publisherId, Calendar requestedStartDate) {
        //SimpleDateFormat saveDateFormat	= new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        /** We'll start off by going back one month from the requested start date. This will ensure we always have accurate rollover minutes. */
        Calendar nextMonth = Calendar.getInstance(Locale.getDefault());
        Calendar start = Calendar.getInstance(Locale.getDefault());
        start.set(requestedStartDate.get(Calendar.YEAR), requestedStartDate.get(Calendar.MONTH), 1);
        start.add(Calendar.MONTH, -1);
        nextMonth.add(Calendar.MONTH, 1);

        ContentValues timeValues = new ContentValues();
        timeValues.put(Time.PUBLISHER_ID, publisherId);
        timeValues.put(Time.ENTRY_TYPE_ID, MinistryDatabase.ID_ROLLOVER);
        timeValues.put(Time.TIME_START, "00:00"); // Midnight

        ContentValues roValues = new ContentValues();
        roValues.put(Rollover.PUBLISHER_ID, publisherId);

        Cursor ro, time;
        int minutesRO = 0;
        int minutesTime = 0;
        int oneHour = 60;
        int roID = MinistryDatabase.CREATE_ID;
        boolean isFirstLoop = true;

        do {
            /** Let's get our minutes and rollover._id from the db for the publisher and date. */
            ro = fetchRolloverRecord(publisherId, TimeUtils.dbDateFormat.format(start.getTime()));
            if(ro.moveToFirst()) {
                roID = ro.getInt(ro.getColumnIndex(Rollover._ID));
                if(isFirstLoop) {
                    minutesRO = ro.getInt(ro.getColumnIndex(Rollover.MINUTES));
                    isFirstLoop = false;
                }
            }
            else {
                roID = MinistryDatabase.CREATE_ID;
            }

            ro.close();

            minutesTime = Integer.valueOf(Helper.getMinuteDuration(fetchListOfHoursForPublisherNoRollover(TimeUtils.dbDateFormat.format(start.getTime()), publisherId, "month")));

            /** Is there already a rollover time entry for this month? If so we need that time._id to update or delete. */
            time = fetchRolloverTimeEntry(publisherId, TimeUtils.dbDateFormat.format(start.getTime()));

            /** The sum of both minutes is over an hour */
            if(minutesTime + minutesRO >= oneHour) {
                /** The time entry should be for the needed minutes to put the total time to the next hour. */
                timeValues.put(Time.TIME_END, "00:" + String.valueOf(oneHour - minutesTime));

                minutesRO = minutesTime + minutesRO - oneHour;

                timeValues.put(Time.DATE_START, TimeUtils.dbDateFormat.format(start.getTime()));
                timeValues.put(Time.DATE_END, TimeUtils.dbDateFormat.format(start.getTime()));

                if(time.moveToFirst())
                    saveTime(time.getLong(time.getColumnIndex(Time._ID)), timeValues);
                else
                    createTime(timeValues);
            }
            else {
                minutesRO += minutesTime;
                if (time.moveToFirst()) {
                    /** We have a time record that needs to be deleted since there won't be a time entry for this month. */
                    removeTimeEntryDeep(time.getInt(time.getColumnIndex(Time._ID)));
                }
            }

            time.close();

            /** No matter what we'll make sure a rollover record exists for this publisher and date. */
            roValues.put(Rollover.DATE, TimeUtils.dbDateFormat.format(start.getTime()));
            roValues.put(Rollover.MINUTES, minutesRO);

            if(roID != MinistryDatabase.CREATE_ID)
                saveRolloverMinutes(roID, roValues);
            else
                createRolloverMinutes(roValues);

            start.add(Calendar.MONTH, 1);
        } while(start.before(nextMonth));
    }

    public long createPioneeringType(ContentValues values) {
        return sqlDB.insert(Tables.TYPES_OF_PIONEERING, null, values);
    }

    public int savePioneeringType(long _id, ContentValues values) {
        return sqlDB.update(Tables.TYPES_OF_PIONEERING, values, BaseColumns._ID + "=" + _id, null);
    }

    public void deletePioneeringType(long _id) {
        sqlDB.delete(Tables.TYPES_OF_PIONEERING, BaseColumns._ID + " = " + _id, null);
    }

    public Cursor fetchActivePioneeringTypes() {
        return sqlDB.query(Tables.TYPES_OF_PIONEERING, PioneeringType.All_COLS, null, null, null, null, PioneeringType.DEFAULT_SORT);
    }

    public Cursor fetchPublisherFirstTimeEntry(int publisherId) {
        return sqlDB.query(Tables.TIMES, new String[] {Time.DATE_START}, Time.PUBLISHER_ID + "=" + publisherId, null, null, null, Time.DATE_START, "1");
    }
}