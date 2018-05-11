package com.myMinistry.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.myMinistry.R;

import java.util.Calendar;
import java.util.Locale;

public class PrefUtils {
    public static int getSummaryMonth(final Context context, Calendar date) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getInt(AppConstants.PREF_SUMMARY_MONTH, date.get(Calendar.MONTH));
    }

    public static int getSummaryYear(final Context context, Calendar date) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getInt(AppConstants.PREF_SUMMARY_YEAR, date.get(Calendar.YEAR));
    }

    public static void setSummaryMonthAndYear(final Context context, Calendar date) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putInt(AppConstants.PREF_SUMMARY_MONTH, date.get(Calendar.MONTH)).apply();
        sp.edit().putInt(AppConstants.PREF_SUMMARY_YEAR, date.get(Calendar.YEAR)).apply();
    }

    public static int getPublisherId(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getInt(AppConstants.PREF_PUBLISHER_ID, AppConstants.CREATE_ID);
    }

    public static void setPublisherId(final Context context, int publisherId) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putInt(AppConstants.PREF_PUBLISHER_ID, publisherId).apply();
    }

    public static int getVersionNumber(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getInt(AppConstants.PREF_VERSION_NUMBER, 0);
    }

    public static void setVersionNumber(final Context context, int versionNumber) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putInt(AppConstants.PREF_VERSION_NUMBER, versionNumber).apply();
    }

    public static boolean shouldDBAutoBackup(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(AppConstants.PREF_AUTO_BACKUPS, false);
    }

    public static void setDBAutoBackup(final Context context, boolean shouldBackUp) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putBoolean(AppConstants.PREF_AUTO_BACKUPS, shouldBackUp).apply();
    }

    public static String getDBBackupDailyTime(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getString(AppConstants.PREF_BACKUP_DAILY_TIME, "");
    }

    public static void setDBBackupDailyTime(final Context context, String backupTime) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putString(AppConstants.PREF_BACKUP_DAILY_TIME, backupTime).apply();
    }

    public static String getDBBackupWeeklyTime(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getString(AppConstants.PREF_BACKUP_WEEKLY_TIME, "");
    }

    public static void setDBBackupWeeklyTime(final Context context, String backupTime) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putString(AppConstants.PREF_BACKUP_WEEKLY_TIME, backupTime).apply();
    }

    public static int getDBBackupWeeklyWeekday(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getInt(AppConstants.PREF_BACKUP_WEEKLY_WEEKDAY, 0);
    }

    public static void setDBBackupWeeklyWeekday(final Context context, int weekday) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putInt(AppConstants.PREF_BACKUP_WEEKLY_WEEKDAY, weekday).apply();
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
        return sp.getString(AppConstants.PREF_LOCALE, Locale.getDefault().toString());
    }

    public static void setLocale(final Context context, String locale) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putString(AppConstants.PREF_LOCALE, locale).apply();
    }
}