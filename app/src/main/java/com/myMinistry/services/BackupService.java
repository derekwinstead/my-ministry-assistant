package com.myMinistry.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.widget.Toast;

import com.myMinistry.provider.MinistryDatabase;
import com.myMinistry.util.FileUtils;

import java.io.File;
import java.io.IOException;

public class BackupService extends Service {
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        File intDB = getApplicationContext().getDatabasePath(MinistryDatabase.DATABASE_NAME);
        // Daily
        File extDBDaily = FileUtils.getExternalDBFile(getApplicationContext(), "auto-backup-daily.db");
        // Weekly
        File extDBWeekly = FileUtils.getExternalDBFile(getApplicationContext(), "auto-backup-weekly.db");

        try {
            if(extDBDaily != null) {
                if(!extDBDaily.exists())
                    extDBDaily.createNewFile();

                FileUtils.copyFile(intDB, extDBDaily);
            }
        } catch (IOException e) {
            Toast.makeText(getApplicationContext(), "Error during weekly backup", Toast.LENGTH_LONG).show();
        }

        try {
            if(extDBWeekly != null) {
                if(!extDBWeekly.exists())
                    extDBWeekly.createNewFile();

                FileUtils.copyFile(intDB, extDBWeekly);
            }
        } catch (IOException e) {
            Toast.makeText(getApplicationContext(), "Error during weekly backup", Toast.LENGTH_LONG).show();
        }

        return Service.START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}