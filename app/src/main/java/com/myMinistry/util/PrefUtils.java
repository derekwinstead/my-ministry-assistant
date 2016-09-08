package com.myMinistry.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.myMinistry.R;
import com.myMinistry.provider.MinistryDatabase;

import java.util.Calendar;
import java.util.Locale;

public class PrefUtils {
    private static final String PREF_VERSION_NUMBER = "version_number";
    private static final String PREF_AUTO_BACKUPS = "db_do_auto_backups";
    private static final String PREF_BACKUP_DAILY_TIME = "db_backup_daily_time";
    private static final String PREF_BACKUP_WEEKLY_TIME = "db_backup_weekly_time";
    private static final String PREF_BACKUP_WEEKLY_WEEKDAY = "db_backup_weekly_weekday";
    private static final String PREF_PUBLISHER_ID = "publisher_id";
    private static final String PREF_SUMMARY_MONTH = "saved_month";
    private static final String PREF_SUMMARY_YEAR = "saved_year";
    private static final String PREF_LOCALE = "locale";

    public static int getSummaryMonth(final Context context, Calendar date) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getInt(PREF_SUMMARY_MONTH, date.get(Calendar.MONTH));
    }

    public static int getSummaryYear(final Context context, Calendar date) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getInt(PREF_SUMMARY_YEAR, date.get(Calendar.YEAR));
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

    public static String getDBBackupDailyTime(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getString(PREF_BACKUP_DAILY_TIME, "");
    }

    public static void setDBBackupDailyTime(final Context context, String backupTime) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putString(PREF_BACKUP_DAILY_TIME, backupTime).commit();
    }

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

    public static boolean shouldCalculateRolloverTime(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(context.getString(R.string.pref_key_rollover), true);
    }

    public static boolean shouldShowMinutesInTotals(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(context.getString(R.string.pref_key_show_minutes), true);
    }

    public static String getLocale(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getString(PREF_LOCALE, Locale.getDefault().toString());
    }

    public static void setLocale(final Context context, String locale) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putString(PREF_LOCALE, locale).commit();
    }
}