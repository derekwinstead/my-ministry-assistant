package com.myMinistry.model;

import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.widget.Toast;

import com.myMinistry.Helper;
import com.myMinistry.R;
import com.myMinistry.provider.MinistryContract.Publisher;
import com.myMinistry.provider.MinistryContract.Time;
import com.myMinistry.provider.MinistryDatabase;
import com.myMinistry.provider.MinistryService;
import com.myMinistry.utils.AppConstants;
import com.myMinistry.utils.FileUtils;
import com.myMinistry.utils.PrefUtils;
import com.myMinistry.utils.TimeUtils;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Locale;

public class AppUpdated extends AsyncTask<Void, Integer, Void> {
    private Context mContext;
    ProgressDialog mProgress;
    private int mProgressDialog = 1;

    public AppUpdated(Context context, int progressDialog) {
        this.mContext = context;
        this.mProgressDialog = progressDialog;

    }

    @Override
    public void onPreExecute() {
        mProgress = new ProgressDialog(mContext);
        mProgress.setMessage(mContext.getResources().getString(R.string.updating_app));
        if (mProgressDialog == ProgressDialog.STYLE_HORIZONTAL) {

            mProgress.setIndeterminate(false);
            mProgress.setMax(100);
            mProgress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            mProgress.setCancelable(true);
        }
        mProgress.show();
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        if (mProgressDialog == ProgressDialog.STYLE_HORIZONTAL) {
            mProgress.setProgress(values[0]);
        }
    }

    @Override
    protected Void doInBackground(Void... values) {
        doApplicationUpdatedWork();
		/*
		try {

			doApplicationUpdatedWork();

			for (int i = 1; i <= 10; i++) {
				publishProgress(i * 10);
				Thread.sleep(500);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		*/
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        mProgress.dismiss();
        Toast.makeText(mContext, R.string.toast_update_done, Toast.LENGTH_LONG).show();
    }

    private void doApplicationUpdatedWork() {
        int currentVersionNumber = 0;
        int savedVersionNumber = PrefUtils.getVersionNumber(mContext);

        try {
            currentVersionNumber = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0).versionCode;
        } catch (Exception e) {
        }

        /* Cleanup SharedPrefs - This will always check for the old "defaults" as well as cleanup the prefs to use the Android default. */
        // sp.upgradePrefs();

        if (savedVersionNumber <= 161) {
            Helper.renameDB(mContext);
            Helper.renameAndMoveBackups(mContext);

            File intDB = mContext.getDatabasePath(AppConstants.DATABASE_NAME);
            File extDB = FileUtils.getExternalDBFile(mContext, "auto-db-v" + MinistryDatabase.DATABASE_VERSION + "-1.db");

            /* Create a backup just in case */
            try {
                if (extDB != null) {
                    if (!extDB.exists())
                        extDB.createNewFile();

                    FileUtils.copyFile(intDB, extDB);
                }
            } catch (IOException e) {
            }

            /* This is to recalculate everyone's roll over time entries. */
            MinistryService database = new MinistryService(mContext);
            database.openWritable();
            //SimpleDateFormat dbDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Calendar start = Calendar.getInstance(Locale.getDefault());
            int pubID;

            /* Loop over each publisher for each available month to convert */
            if (!database.isOpen())
                database.openWritable();

            Cursor pubs = database.fetchAllPublishers();
            Cursor theDate;
            try {
                Thread.sleep(500);
            } catch (InterruptedException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
            for (pubs.moveToFirst(); !pubs.isAfterLast(); pubs.moveToNext()) {
                pubID = pubs.getInt(pubs.getColumnIndex(Publisher._ID));

                /* Get first time entry date for publisher */
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

        PrefUtils.setVersionNumber(mContext, currentVersionNumber);
    }
}