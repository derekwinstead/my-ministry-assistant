package com.myMinistry;

import android.content.Context;
import android.database.Cursor;
import android.os.Environment;
import android.text.format.DateUtils;
import android.util.TypedValue;

import com.myMinistry.provider.MinistryContract.UnionsNameAsRef;
import com.myMinistry.provider.MinistryDatabase;
import com.myMinistry.util.FileUtils;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Period;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class Helper {
    public static Calendar roundMinutesAndHour(Calendar time, int TIME_PICKER_INTERVAL) {
        int minute = time.get(Calendar.MINUTE);
        int hour = time.get(Calendar.HOUR_OF_DAY);

        if (time.get(Calendar.MINUTE) % TIME_PICKER_INTERVAL != 0) {
            int minuteFloor = minute - (minute % TIME_PICKER_INTERVAL);
            minute = minuteFloor + (minute == minuteFloor + 1 ? TIME_PICKER_INTERVAL : 0);
            if (minute == 60) {
                hour++;
                minute = 0;
            }
        }

        time.set(Calendar.HOUR_OF_DAY, hour);
        time.set(Calendar.MINUTE, minute);
        return time;
    }

    public static boolean renameDB(Context context) {
        /** Create the file */
        File oldDB = context.getDatabasePath(MinistryDatabase.DATABASE_NAME_OLD);
        /** If the file exists we want to rename it to our new DB name :) */
        if (oldDB.exists())
            oldDB.renameTo(context.getDatabasePath(MinistryDatabase.DATABASE_NAME));

        return true;
    }

    public static void renameAndMoveBackups(Context context) {
        boolean deleteFile;
        boolean deleteDir = true;

        File filePath = FileUtils.getExternalDBFile(context, "");
        File dbPath = new File(FileUtils.getExternalDBFile(context, "").getParent(), "databases");
        File dbPathOLD = new File(Environment.getExternalStorageDirectory() + "/Android/data/" + context.getPackageName() + ".backup/databases");

        if (!filePath.exists())
            filePath.mkdirs();

        /** Check for old incorrect package name folder structure. */
        if (dbPathOLD.exists()) {
            if (dbPathOLD.exists() && dbPathOLD.canWrite()) {
                if (dbPathOLD.listFiles() != null) {
                    for (File file : dbPathOLD.listFiles()) {
                        deleteFile = true;
                        try {
                            FileUtils.copyFile(file, new File(filePath, file.getName()));
                        } catch (IOException e) {
                            deleteFile = false;
                            deleteDir = false;
                            e.printStackTrace();
                        }
                        if (deleteFile)
                            file.delete();
                    }
                }
                if (deleteDir) {
                    dbPathOLD.delete();
                    new File(dbPathOLD.getParent()).delete();
                }
            }
        }

        /** Check for correct package name but using "databases" folder instead of Android default "files". */
        if (dbPath.exists() && dbPath.canWrite()) {
            deleteDir = true;
            if (dbPath.listFiles() != null) {
                for (File file : dbPath.listFiles()) {
                    deleteFile = true;
                    try {
                        FileUtils.copyFile(file, new File(filePath, file.getName()));
                    } catch (IOException e) {
                        deleteFile = false;
                        deleteDir = false;
                        e.printStackTrace();
                    }
                    if (deleteFile)
                        file.delete();
                }
            }
            if (deleteDir)
                dbPath.delete();
        }
    }

    public static double getDifference(Calendar start, Calendar end) {
        Duration dur = new Duration(new DateTime(start), new DateTime(end));
        Period per = dur.toPeriod();
        return (double) per.getHours() + ((double) per.getMinutes() / 60.0);
    }

    public static String getMinuteDuration(Cursor cursor) {
        SimpleDateFormat saveDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        Calendar start = Calendar.getInstance(Locale.getDefault());
        Calendar end = Calendar.getInstance(Locale.getDefault());

        Duration dur = new Duration(new DateTime(start), new DateTime(start));

        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            try {
                start.setTime(saveDateFormat.parse(cursor.getString(cursor.getColumnIndex(UnionsNameAsRef.DATE_START))));
            } catch (Exception e) {
                start = Calendar.getInstance(Locale.getDefault());
            }
            try {
                end.setTime(saveDateFormat.parse(cursor.getString(cursor.getColumnIndex(UnionsNameAsRef.DATE_END))));
            } catch (Exception e) {
                end = Calendar.getInstance(Locale.getDefault());
            }

            Duration durchange = new Duration(new DateTime(start), new DateTime(end));
            dur = dur.withDurationAdded(durchange, 1);
        }

        cursor.close();
        PeriodFormatter retVal = new PeriodFormatterBuilder()
                .printZeroAlways()
                .appendMinutes()
                .toFormatter();
        return retVal.print(dur.toPeriod());
    }

    public static int clearBackups(Context context) {
        File extDBDir = FileUtils.getExternalDBFile(context, "");

        FilenameFilter filter = new FilenameFilter() {
            public boolean accept(File dir, String filename) {
                File sel = new File(dir, filename);
                return !(filename.contains("auto")) && !(sel.isDirectory());
            }
        };

        if (extDBDir != null && extDBDir.exists()) {
            File[] files = extDBDir.listFiles(filter);
            if (files.length > 1) {
                File lastModifiedFile = files[0];
                for (int i = 1; i < files.length; i++) {
                    if (lastModifiedFile.lastModified() < files[i].lastModified()) {
                        lastModifiedFile = files[i];
                    }
                }

                final String excludeFileName = lastModifiedFile.getName();
                filter = new FilenameFilter() {
                    public boolean accept(File dir, String filename) {
                        return !(filename.contains("auto")) && !(filename.contains(excludeFileName));
                    }
                };

                files = extDBDir.listFiles(filter);

                for (File file : files) {
                    file.delete();
                }

                return 1;
            } else
                return 2;
        } else
            return 0;
    }

    public static int dipsToPix(Context context, float dps) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dps, context.getResources().getDisplayMetrics());
    }

    public static int getIconResIDByLitTypeID(int litTypeID) {
        switch (litTypeID) {
            case MinistryDatabase.ID_BOOKS:
                return R.drawable.ic_action_book;
            case MinistryDatabase.ID_MAGAZINES:
                return R.drawable.ic_mag;
            case MinistryDatabase.ID_MEDIA:
                return R.drawable.ic_media;
            case MinistryDatabase.ID_TRACTS:
                return R.drawable.ic_tracts;
            case MinistryDatabase.ID_BROCHURES:
                return R.drawable.ic_booklets;
            case MinistryDatabase.ID_VIDEOS_TO_SHOW:
                return R.drawable.ic_video;
            default:
                return R.drawable.ic_action_default;
        }
    }

    /**
     * Returns a String formatted in the default locale of the device. Looks like "Ddd, Mmm dd, H:MMTT - H:MMTT
     *
     * @param context the {@link android.content.Context} that this is running within.
     * @param start   the starting date and time of this entry.
     * @param end     the ending date and time of this entry.
     */
    public static String buildTimeEntryDateAndTime(Context context, Calendar start, Calendar end) {
        return DateUtils.formatDateRange(context, start.getTimeInMillis(), end.getTimeInMillis(), DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_MONTH | DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_WEEKDAY | DateUtils.FORMAT_ABBREV_WEEKDAY | DateUtils.FORMAT_NO_YEAR);
    }
}