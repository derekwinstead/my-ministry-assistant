package com.myMinistry.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.myMinistry.util.HelpUtils;
import com.myMinistry.util.PrefUtils;

public class BootReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
			if (PrefUtils.shouldDBAutoBackup(context)) {
				// Daily
				HelpUtils.setDailyAlarm(context);
				// Weekly
				HelpUtils.setWeeklyAlarm(context);
			}
		}
	}
}