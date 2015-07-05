package com.myministry.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.text.format.DateUtils;

import com.myministry.provider.MinistryContract.UnionsNameAsRef;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Period;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Formatter;
import java.util.Locale;
import java.util.TimeZone;

public class TimeUtils {
    public static final SimpleDateFormat dbDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    //private static final int SECOND = 1000;
    //private static final int MINUTE = 60 * SECOND;
    //private static final int HOUR = 60 * MINUTE;
    //private static final int DAY = 24 * HOUR;

    private static final SimpleDateFormat[] ACCEPTED_TIMESTAMP_FORMATS = {
            new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.US),
            new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy", Locale.US),
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US),
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z", Locale.US),
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.US),
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US),
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss Z", Locale.US)
    };

    private static final SimpleDateFormat VALID_IFMODIFIEDSINCE_FORMAT =
            new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.US);

    public static Date parseTimestamp(String timestamp) {
        for (SimpleDateFormat format : ACCEPTED_TIMESTAMP_FORMATS) {
            format.setTimeZone(TimeZone.getTimeZone("GMT"));
            try {
                return format.parse(timestamp);
            } catch (ParseException ex) {
                continue;
            }
        }

        // All attempts to parse have failed
        return null;
    }

    public static boolean isValidFormatForIfModifiedSinceHeader(String timestamp) {
        try {
            return VALID_IFMODIFIEDSINCE_FORMAT.parse(timestamp)!=null;
        } catch (Exception ex) {
            return false;
        }
    }

    public static long timestampToMillis(String timestamp, long defaultValue) {
        if (TextUtils.isEmpty(timestamp)) {
            return defaultValue;
        }
        Date d = parseTimestamp(timestamp);
        return d == null ? defaultValue : d.getTime();
    }

    @SuppressLint("NewApi")
    public static String formatShortDate(Context context, Date date) {
        StringBuilder sb = new StringBuilder();
        Formatter formatter = new Formatter(sb);
        return DateUtils.formatDateRange(context, formatter, date.getTime(), date.getTime(),
                DateUtils.FORMAT_ABBREV_ALL | DateUtils.FORMAT_NO_YEAR,
                PrefUtils.getDisplayTimeZone(context).getID()).toString();
    }

    public static String formatShortTime(Context context, Date time) {
        DateFormat format = DateFormat.getTimeInstance(DateFormat.SHORT);
        TimeZone tz = PrefUtils.getDisplayTimeZone(context);
        if (tz != null) {
            format.setTimeZone(tz);
        }
        return format.format(time);
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
        Duration durchange = new Duration(null, null);

        for(cursor.moveToFirst();!cursor.isAfterLast();cursor.moveToNext()) {
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

            durchange = new Duration(new DateTime(start), new DateTime(end));
            dur = dur.withDurationAdded(durchange, 1);
        }

        cursor.close();

        PeriodFormatter retVal;

        if(showMinutes) {
            retVal = new PeriodFormatterBuilder()
                    .printZeroRarelyFirst()
                    .appendHours()
                    .appendSuffix(h)
                    .appendSeparator(" ")
                    .appendMinutes()
                    .appendSuffix(m)
                    .toFormatter();
        }
        else {
            retVal = new PeriodFormatterBuilder()
                    .printZeroAlways()
                    .appendHours()
                    .appendSuffix(h)
                    .toFormatter();
        }

        return retVal.print(dur.toPeriod());
    }
}