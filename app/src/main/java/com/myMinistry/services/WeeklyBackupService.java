package com.myMinistry.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.widget.Toast;

import com.myMinistry.utils.AppConstants;
import com.myMinistry.utils.FileUtils;

import java.io.File;
import java.io.IOException;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class WeeklyBackupService extends Service {
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        File intDB = getApplicationContext().getDatabasePath(AppConstants.DATABASE_NAME);
        File extDB = FileUtils.getExternalDBFile(getApplicationContext(), "auto-backup-weekly.db");

        try {
            if (extDB != null) {
                if (!extDB.exists())
                    extDB.createNewFile();

                FileUtils.copyFile(intDB, extDB);
            }
        } catch (IOException e) {
            Toast.makeText(getApplicationContext(), "Error during daily backup", Toast.LENGTH_LONG).show();
        }

        return Service.START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}