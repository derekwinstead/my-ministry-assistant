package com.myMinistry.utils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.webkit.WebView;

import com.myMinistry.Helper;
import com.myMinistry.R;
import com.myMinistry.provider.MinistryContract.Publisher;
import com.myMinistry.provider.MinistryContract.Time;
import com.myMinistry.provider.MinistryDatabase;
import com.myMinistry.provider.MinistryService;
import com.myMinistry.services.DailyBackupService;
import com.myMinistry.services.WeeklyBackupService;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class HelpUtils {
    public static void showOpenSourceLicenses(Activity activity) {
        FragmentManager fm = activity.getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        Fragment prev = fm.findFragmentByTag("dialog_licenses");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        new OpenSourceLicensesDialog().show(ft, "dialog_licenses");
    }

    public static int booleanConversionsToInt(@NonNull boolean value) {
        return value ? 1 : 0;

    }

    public static int booleanConversionsToInt(@NonNull int value) {
        return (value > 0) ? 1 : 0;
    }

    public static class OpenSourceLicensesDialog extends DialogFragment {

        public OpenSourceLicensesDialog() {
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            WebView webView = new WebView(getActivity());
            webView.loadUrl("file:///android_asset/licenses.html");

            return new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.pref_about_licenses)
                    .setView(webView)
                    .setPositiveButton(R.string.ok,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    dialog.dismiss();
                                }
                            }
                    )
                    .create();
        }
    }

    public static boolean isApplicationUpdated(Context context) {
        int currentVersionNumber = 0;
        try {
            currentVersionNumber = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return currentVersionNumber > PrefUtils.getVersionNumber(context);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void doApplicationUpdatedWork(Context mContext) {
        int currentVersionNumber = 0;
        int savedVersionNumber = PrefUtils.getVersionNumber(mContext);

        try {
            currentVersionNumber = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0).versionCode;
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (savedVersionNumber <= 161) {
            Helper.renameDB(mContext);
            Helper.renameAndMoveBackups(mContext);

            File intDB = mContext.getDatabasePath(AppConstants.DATABASE_NAME);
            File extDB = FileUtils.getExternalDBFile(mContext, "auto-db-v" + MinistryDatabase.DATABASE_VERSION + "-1.db");

            // Create a backup just in case
            try {
                if (extDB != null) {
                    if (!extDB.exists())
                        extDB.createNewFile();

                    FileUtils.copyFile(intDB, extDB);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            // This is to recalculate everyone's roll over time entries.
            processRolloverTime(mContext);
        }

        PrefUtils.setVersionNumber(mContext, currentVersionNumber);
    }

    public static void setDailyAlarm(Context context) {
        Calendar time = Calendar.getInstance(Locale.getDefault());
        int year = time.get(Calendar.YEAR);
        int month = time.get(Calendar.MONTH);
        int day = time.get(Calendar.DAY_OF_MONTH);

        try {
            time.setTime(DateFormat.getTimeFormat(context).parse(PrefUtils.getDBBackupDailyTime(context)));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        time.set(year, month, day);

        int alarmType = AlarmManager.RTC;
        long interval = AlarmManager.INTERVAL_DAY;
        long start = time.getTimeInMillis();

        PendingIntent pi = PendingIntent.getService(context, 0, new Intent(context, DailyBackupService.class), PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.setInexactRepeating(alarmType, start, interval, pi);
    }

    public static void setWeeklyAlarm(Context context) {
        Calendar time = Calendar.getInstance(Locale.getDefault());
        int year = time.get(Calendar.YEAR);
        int month = time.get(Calendar.MONTH);
        int day = time.get(Calendar.DAY_OF_MONTH);
        int weekday = PrefUtils.getDBBackupWeeklyWeekday(context);

        try {
            time.setTime(DateFormat.getTimeFormat(context).parse(PrefUtils.getDBBackupWeeklyTime(context)));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        time.set(year, month, day);

        while (true) {
            if (time.get(Calendar.DAY_OF_WEEK) == weekday)
                break;

            time.add(Calendar.DAY_OF_MONTH, 1);
        }

        int alarmType = AlarmManager.RTC;
        long interval = AlarmManager.INTERVAL_DAY * 7;
        long start = time.getTimeInMillis();

        PendingIntent pi = PendingIntent.getService(context, 0, new Intent(context, WeeklyBackupService.class), PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.setInexactRepeating(alarmType, start, interval, pi);
    }

    public static void disableDailyAlarm(Context context) {
        PendingIntent pi = PendingIntent.getService(context, 0, new Intent(context, DailyBackupService.class), PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.cancel(pi);
    }

    public static void disableWeeklyAlarm(Context context) {
        PendingIntent pi = PendingIntent.getService(context, 0, new Intent(context, WeeklyBackupService.class), PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.cancel(pi);
    }

    public static void processRolloverTime(Context mContext) {
        MinistryService database = new MinistryService(mContext);
        database.openWritable();
        Calendar start = Calendar.getInstance(Locale.getDefault());
        int pubID;

        // Loop over each publisher for each available month to convert
        if (!database.isOpen())
            database.openWritable();

        Cursor pubs = database.fetchAllPublishers();
        Cursor theDate;
        try {
            Thread.sleep(500);
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }
        for (pubs.moveToFirst(); !pubs.isAfterLast(); pubs.moveToNext()) {
            pubID = pubs.getInt(pubs.getColumnIndex(Publisher._ID));

            // Get first time entry date for publisher
            theDate = database.fetchPublisherFirstTimeEntry(pubID);

            if (theDate.moveToFirst()) {
                try {
                    start.setTime(TimeUtils.dbDateFormat.parse(theDate.getString(theDate.getColumnIndex(Time.DATE_START))));
                    database.processRolloverTime(pubID, start);
                } catch (ParseException e) {
                    start = Calendar.getInstance(Locale.getDefault());
                }
            }
            theDate.close();
        }
        pubs.close();
        database.close();
    }
}