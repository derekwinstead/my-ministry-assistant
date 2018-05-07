package com.myMinistry.util;

import android.database.Cursor;

import com.myMinistry.provider.MinistryContract.UnionsNameAsRef;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Interval;
import org.joda.time.Period;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class TimeUtils {
    public static final SimpleDateFormat dbDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    public static final SimpleDateFormat fullMonthFormat = new SimpleDateFormat("MMMM", Locale.getDefault());
    public static final SimpleDateFormat shortDayOfWeekFormat = new SimpleDateFormat("EEE", Locale.getDefault());
    public static final SimpleDateFormat dayOfMonthFormat = new SimpleDateFormat("d", Locale.getDefault());
    public static final SimpleDateFormat monthAndYearFormat = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());

    public static String getDayOfWeek(Calendar cal) {
        DateTime dt = new DateTime(cal.getTimeInMillis());
        DateTime.Property pDoW = dt.dayOfWeek();
        return pDoW.getAsShortText(); // returns "Mon", "Tue", etc.
    }

    public static String getStartAndEndTimes(Calendar calStart, Calendar calEnd) {
        DateTime start = new DateTime(calStart.getTimeInMillis());
        DateTime end = new DateTime(calEnd.getTimeInMillis());
        Interval interval = new Interval(start, end);
        Period period = interval.toPeriod();

        StringBuilder builder = new StringBuilder();

        return builder.append(start.toString("h:mm")).append(" - ").append(end.toString("h:mm a")).toString();
/*
        PeriodFormatter retVal = new PeriodFormatterBuilder()
                .printZeroNever()
                .appendHours()
                .appendSuffix(h)
                .appendSeparator(" ")
                .appendMinutes()
                .appendSuffix(m)
                .toFormatter();

        return retVal.print(period);

        return "";*/
    }

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