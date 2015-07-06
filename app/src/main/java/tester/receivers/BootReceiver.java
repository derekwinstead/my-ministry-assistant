package tester.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import tester.util.HelpUtils;
import tester.util.PrefUtils;

public class BootReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
			// We need to see if either schedules are active (daily or weekly)
			
			// daily
			if(PrefUtils.shouldDBBackupDaily(context)) {
				HelpUtils.setDailyAlarm(context);
			}
			
			// weekly
			if(PrefUtils.shouldDBBackupWeekly(context)) {
				HelpUtils.setWeeklyAlarm(context);
		    }
		}
	}
}