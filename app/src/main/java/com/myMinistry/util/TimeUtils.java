package com.myMinistry.util;

import android.database.Cursor;

import com.myMinistry.provider.MinistryContract.UnionsNameAsRef;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Period;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class TimeUtils {
    public static final SimpleDateFormat dbDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    public static String getTimeLength(Calendar start, Calendar end, String h, String m) {
        Duration dur = new Duration(new DateTime(start), new DateTime(end));
        Period period = dur.toPeriod();

        PeriodFormatter retVal = new PeriodFormatterBuilder()
                .printZeroNever()
                .appendHours()
                .appendSuffix(h)
                .appendSeparator(" ")
                .appendMinutes()
                .appendSuffix(m)
                .toFormatter();

        return retVal.print(period);
    }

    public static String getTimeLength(Cursor cursor, String h, String m, boolean showMinutes) {
        SimpleDateFormat saveDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        Calendar start = Calendar.getInstance(Locale.getDefault());
        Calendar end = Calendar.getInstance(Locale.getDefault());

        Duration dur = new Duration(null, null);

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

        PeriodFormatter retVal;

        if (showMinutes) {
            retVal = new PeriodFormatterBuilder()
                    .printZeroRarelyFirst()
                    .appendHours()
                    .appendSuffix(h)
                    .appendSeparator(" ")
                    .appendMinutes()
                    .appendSuffix(m)
                    .toFormatter();
        } else {
            retVal = new PeriodFormatterBuilder()
                    .printZeroAlways()
                    .appendHours()
                    .appendSuffix(h)
                    .toFormatter();
        }

        return retVal.print(dur.toPeriod());
    }
}