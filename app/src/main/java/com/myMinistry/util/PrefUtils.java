package com.myMinistry.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.myMinistry.provider.MinistryDatabase;

import java.util.Calendar;
import java.util.TimeZone;

/**
 * Utilities and constants related to app preferences.
 */
public class PrefUtils  {
    private static final String PREF_PUBLISHER_NAME			= "publisher_name";
    private static final String PREF_VERSION_NUMBER			= "version_number";
    private static final String PREF_HAS_OPENED_BEFORE		= "has_opened_before";
    private static final String PREF_AUTO_BACKUPS           = "db_do_auto_backups";
    private static final String PREF_BACKUP_DAILY_TIME		= "db_backup_daily_time";
    private static final String PREF_BACKUP_WEEKLY_TIME		= "db_backup_weekly_time";
    private static final String PREF_BACKUP_WEEKLY_WEEKDAY	= "db_backup_weekly_weekday";
    private static final String PREF_PUBLISHER_ID			= "publisher_id";
    private static final String PREF_SUMMARY_MONTH			= "saved_month";
    private static final String PREF_SUMMARY_YEAR			= "saved_year";
    private static final String PREF_LOCALE					= "locale";

    public static TimeZone getDisplayTimeZone(Context context) {
        //TimeZone defaultTz = TimeZone.getDefault();
        return TimeZone.getDefault();
        //return (isUsingLocalTime(context) && defaultTz != null)
        //? defaultTz : Config.CONFERENCE_TIMEZONE;
    }

    /*
    public static void init(final Context context) {
        // Check what year we're configured for
        //SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        //int conferenceYear = sp.getInt(PREF_CONFERENCE_YEAR, 0);

        if (conferenceYear != Config.CONFERENCE_YEAR) {
            LOGD(TAG, "App not yet set up for " + PREF_CONFERENCE_YEAR + ". Resetting data.");
            // Application is configured for a different conference year. Reset preferences.
            sp.edit().clear().putInt(PREF_CONFERENCE_YEAR, Config.CONFERENCE_YEAR).commit();
        }

    }
      */

    public static boolean hasOpenedBefore(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(PREF_HAS_OPENED_BEFORE, false);
    }

    public static void markOpenedBefore(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putBoolean(PREF_HAS_OPENED_BEFORE, true).commit();
    }

    public static void registerOnSharedPreferenceChangeListener(final Context context,
                                                                SharedPreferences.OnSharedPreferenceChangeListener listener) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.registerOnSharedPreferenceChangeListener(listener);
    }

    public static void unrgisterOnSharedPreferenceChangeListener(final Context context,
                                                                 SharedPreferences.OnSharedPreferenceChangeListener listener) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.unregisterOnSharedPreferenceChangeListener(listener);
    }

    public static int getSummaryMonth(final Context context, Calendar date) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getInt(PREF_SUMMARY_MONTH, date.get(Calendar.MONTH));
    }

    public static void setSummaryMonth(final Context context, Calendar date) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putInt(PREF_SUMMARY_MONTH, date.get(Calendar.MONTH)).commit();
    }

    public static int getSummaryYear(final Context context, Calendar date) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getInt(PREF_SUMMARY_YEAR, date.get(Calendar.YEAR));
    }

    public static void setSummaryYear(final Context context, Calendar date) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putInt(PREF_SUMMARY_YEAR, date.get(Calendar.YEAR)).commit();
    }

    public static void setSummaryMonthAndYear(final Context context, Calendar date) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putInt(PREF_SUMMARY_MONTH, date.get(Calendar.MONTH)).commit();
        sp.edit().putInt(PREF_SUMMARY_YEAR, date.get(Calendar.YEAR)).commit();
    }

    public static int getPublisherId(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getInt(PREF_PUBLISHER_ID, MinistryDatabase.CREATE_ID);
    }

    public static void setPublisherId(final Context context, int publisherId) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putInt(PREF_PUBLISHER_ID, publisherId).commit();
    }

    public static String getPublisherName(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        //return sp.getString(PREF_PUBLISHER_NAME, context.getResources().getString(R.string.navdrawer_item_select_publisher));
        return "string";
    }

    public static void setPublisherName(final Context context, String name) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putString(PREF_PUBLISHER_NAME, name).commit();
    }

    public static int getVersionNumber(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getInt(PREF_VERSION_NUMBER, 0);
    }

    public static void setVersionNumber(final Context context, int versionNumber) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putInt(PREF_VERSION_NUMBER, versionNumber).commit();
    }

    public static boolean shouldDBAutoBackup(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(PREF_AUTO_BACKUPS, false);
    }

    public static void setDBAutoBackup(final Context context, boolean shouldBackUp) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putBoolean(PREF_AUTO_BACKUPS, shouldBackUp).commit();
    }
/*
    public static boolean shouldDBBackupDaily(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(PREF_BACKUP_DAILY, false);
    }

    public static void setDBBackupDaily(final Context context, boolean shouldBackUp) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putBoolean(PREF_BACKUP_DAILY, shouldBackUp).commit();
    }
*/
    public static String getDBBackupDailyTime(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getString(PREF_BACKUP_DAILY_TIME, "");
    }

    public static void setDBBackupDailyTime(final Context context, String backupTime) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putString(PREF_BACKUP_DAILY_TIME, backupTime).commit();
    }
/*
    public static boolean shouldDBBackupWeekly(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(PREF_BACKUP_WEEKLY, false);
    }

    public static void setDBBackupWeekly(final Context context, boolean shouldBackUp) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putBoolean(PREF_BACKUP_WEEKLY, shouldBackUp).commit();
    }
*/
    public static String getDBBackupWeeklyTime(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getString(PREF_BACKUP_WEEKLY_TIME, "");
    }

    public static void setDBBackupWeeklyTime(final Context context, String backupTime) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putString(PREF_BACKUP_WEEKLY_TIME, backupTime).commit();
    }

    public static int getDBBackupWeeklyWeekday(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getInt(PREF_BACKUP_WEEKLY_WEEKDAY, 0);
    }

    public static void setDBBackupWeeklyWeekday(final Context context, int weekday) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putInt(PREF_BACKUP_WEEKLY_WEEKDAY, weekday).commit();
    }
/*
    public static int getEntryTypeSort(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getInt(PREF_ENTRY_TYPE_SORT, 0);
    }

    public static void setEntryTypeSort(final Context context, int sortId) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putInt(PREF_ENTRY_TYPE_SORT, sortId).commit();
    }
*/
    /*
    public static int getPublicationTypeSort(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getInt(PREF_PUBLICATION_TYPE_SORT, 0);
    }

    public static void setPublicationTypeSort(final Context context, int sortId) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putInt(PREF_PUBLICATION_TYPE_SORT, sortId).commit();
    }
*/
    /*
    public static int getPublicationSort(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getInt(PREF_PUBLICATION_SORT, 0);
    }

    public static void setPublicationSort(final Context context, int sortId) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putInt(PREF_PUBLICATION_SORT, sortId).commit();
    }
*/
    /*
    public static int getHouseholderSort(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getInt(PREF_HOUSEHOLDER_SORT, 0);
    }

    public static void setHouseholderSort(final Context context, int sortId) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putInt(PREF_HOUSEHOLDER_SORT, sortId).commit();
    }
*/
    public static boolean shouldCalculateRolloverTime(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        //return sp.getBoolean(context.getString(R.string.pref_key_rollover), true);
        return false;
    }

    public static boolean shouldShowMinutesInTotals(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        //return sp.getBoolean(context.getString(R.string.pref_key_show_minutes), true);
        return false;
    }

    public static String getLocale(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getString(PREF_LOCALE, "en");
        //Locale.getDefault().toString()
    }

    public static void setLocale(final Context context, String locale) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putString(PREF_LOCALE, locale).commit();
    }
}