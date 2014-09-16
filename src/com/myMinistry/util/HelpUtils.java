package com.myMinistry.util;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Locale;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.PendingIntent;
import android.content.ContentValues;
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
import com.myMinistry.provider.MinistryContract.Literature;
import com.myMinistry.provider.MinistryContract.LiteratureType;
import com.myMinistry.provider.MinistryContract.Publisher;
import com.myMinistry.provider.MinistryContract.Time;
import com.myMinistry.provider.MinistryDatabase;
import com.myMinistry.provider.MinistryService;
import com.myMinistry.services.DailyBackupService;
import com.myMinistry.services.WeeklyBackupService;

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

    public static void showChangeLog(Activity activity) {
        FragmentManager fm = activity.getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        Fragment prev = fm.findFragmentByTag("dialog_changelog");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        new ChangeLogDialog().show(ft, "dialog_changelog");
    }

    public static class ChangeLogDialog extends DialogFragment {

        public ChangeLogDialog() {
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            WebView webView = new WebView(getActivity());
            webView.loadUrl("file:///android_asset/changelog.html");
            

    		/*
    		 * This isn't supported directly but here is what I have done...

Separate your files into groups by country code (like what you would do for normal resource files) and then create a localized string in each of your localized string.xml files called something like "prefix" (where prefix would be "en" for English for example).

Then when you build your asset filenames simple use something like getString("prefix") + "-" + "<name-of-asset->.

At least some variation of the above should work for you.
    		 */

            return new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.menu_change_log)
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
		} catch (Exception e) {}
		
		return currentVersionNumber > PrefUtils.getVersionNumber(context) ? true : false;
	}
	
	public static void doApplicationUpdatedWork(Context mContext) {
		int currentVersionNumber = 0;
		int savedVersionNumber = PrefUtils.getVersionNumber(mContext);
		
		try {
			currentVersionNumber = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0).versionCode;
		} catch (Exception e) {}
		
		/** Cleanup SharedPrefs - This will always check for the old "defaults" as well as cleanup the prefs to use the Android default. */
		// sp.upgradePrefs();
		
		if(savedVersionNumber <= 161) {
			Helper.renameDB(mContext);
			Helper.renameAndMoveBackups(mContext);
			
			File intDB = mContext.getDatabasePath(MinistryDatabase.DATABASE_NAME);
			File extDB = FileUtils.getExternalDBFile(mContext, "auto-db-v" + MinistryDatabase.DATABASE_VERSION + "-1.db");
			
			/** Create a backup just in case */
			try {
				if(extDB != null) {
					if(!extDB.exists())
						extDB.createNewFile();
					
					FileUtils.copyFile(intDB, extDB);
				}
			} catch (IOException e) { }
			
			/** This is to recalculate everyone's roll over time entries. */
			processRolloverTime(mContext);
		}
		
		PrefUtils.setVersionNumber(mContext,currentVersionNumber);
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
		//long start = time.getTimeInMillis() + interval;
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
		
		while(true) {
			if(time.get(Calendar.DAY_OF_WEEK) == weekday)
				break;
			
			time.add(Calendar.DAY_OF_MONTH, 1);
		}
		
		int alarmType = AlarmManager.RTC;
		long interval = AlarmManager.INTERVAL_DAY * 7;
		//long start = time.getTimeInMillis() + interval;
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
	
	public static void sortPublicationTypes(Context context, int how_to_sort) {
		MinistryService database = new MinistryService(context);
		Cursor cursor = null;
		
		if(!database.isOpen())
			database.openWritable();
		
		if(how_to_sort == MinistryDatabase.SORT_BY_ASC)
			cursor = database.fetchAllPublicationTypes("ASC");
		else if(how_to_sort == MinistryDatabase.SORT_BY_DESC)
			cursor = database.fetchAllPublicationTypes("DESC");
		else if(how_to_sort == MinistryDatabase.SORT_BY_POPULAR)
			cursor = database.fetchAllPublicationTypesByPopularity();
		
		ContentValues values = new ContentValues();
		for(cursor.moveToFirst();!cursor.isAfterLast();cursor.moveToNext()) {
			values.put(LiteratureType.SORT_ORDER, cursor.getPosition());
			database.savePublicationType(cursor.getLong(cursor.getColumnIndex(LiteratureType._ID)), values);
		}
		cursor.close();
		database.close();
	}
	
	public static void sortPublications(Context context, int how_to_sort) {
		MinistryService database = new MinistryService(context);
		Cursor cursor = null;
		
		if(!database.isOpen())
			database.openWritable();
		
		if(how_to_sort == MinistryDatabase.SORT_BY_ASC)
			cursor = database.fetchAllPublications("ASC");
		else if(how_to_sort == MinistryDatabase.SORT_BY_DESC)
			cursor = database.fetchAllPublications("DESC");
		else if(how_to_sort == MinistryDatabase.SORT_BY_POPULAR)
			cursor = database.fetchPublicationsByPopularity();
		
		ContentValues values = new ContentValues();
		for(cursor.moveToFirst();!cursor.isAfterLast();cursor.moveToNext()) {
			values.put(Literature.SORT_ORDER, cursor.getPosition());
			database.saveLiterature(cursor.getLong(cursor.getColumnIndex(Literature._ID)), values);
		}
		cursor.close();
		database.close();
	}
	
	public static void processRolloverTime(Context mContext) {
		MinistryService database = new MinistryService(mContext);
		database.openWritable();
    	Calendar start = Calendar.getInstance(Locale.getDefault());
    	int pubID = 0;
    	
    	/** Loop over each publisher for each available month to convert */
    	if(!database.isOpen())
    		database.openWritable();
    	
    	Cursor pubs = database.fetchAllPublishers();
    	Cursor theDate;
    	try {
			Thread.sleep(500);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
    	for(pubs.moveToFirst();!pubs.isAfterLast();pubs.moveToNext()) {
    		pubID = pubs.getInt(pubs.getColumnIndex(Publisher._ID));
    		
    		/** Get first time entry date for publisher */
    		theDate = database.fetchPublisherFirstTimeEntry(pubID);
			
			if(theDate.moveToFirst()) {
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
	}
}