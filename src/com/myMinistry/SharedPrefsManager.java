package com.myMinistry;


public class SharedPrefsManager {
/*	
	private static final String KEY_BACKUP_DAILY = "db_backup_daily";
	private static final String KEY_BACKUP_DAILY_TIME = "db_backup_daily_time";
	private static final String KEY_BACKUP_WEEKLY = "db_backup_weekly";
	private static final String KEY_BACKUP_WEEKLY_TIME = "db_backup_weekly_time";
	private static final String KEY_BACKUP_WEEKLY_WEEKDAY = "db_backup_weekly_weekday";
	private static final String KEY_ENTRY_TYPE_SORT = "entry_type_sort";
	private static final String KEY_PUBLICATION_TYPE_SORT = "entry_type_sort";
	
	public static final String KEY_PUBLISHER_ID = "publisher_id";
	public static final String KEY_MONTH = "saved_month";
	public static final String KEY_YEAR = "saved_year";
	
	private final SharedPreferences sharedPrefs;
	
	private Context mContext;
	
	public SharedPrefsManager(Context context) {
		mContext = context;
		sharedPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
	}
	
	
	
	public void cleanPrefs() {
		int pubID = getPublisherID();
		String pubName = getPublisherName();
		
		
		boolean rollover = shouldCalculateRolloverTime();
		int month = getMonth(Calendar.getInstance());
		int year = getYear(Calendar.getInstance());
		boolean backup_daily = shouldDBBackupDaily();
		String backup_daily_time = getDBBackupDailyTime();
		boolean backup_weekly = shouldDBBackupWeekly();
		String backup_weekly_time = getDBBackupWeeklyTime();
		int weekday = getDBBackupWeeklyWeekday();
		int entry_type= getEntryTypeSort();
		int pub_type= getPubTypeSort();
		boolean showMins = shouldShowMinutesInTotals();
		
		sharedPrefs.edit().clear().commit();
		
		setPublisherID(pubID);
		
		setMonth(month);
		setYear(year);
		setPublisherName(pubName);
		
		setCalculateRolloverTime(rollover);
		setDBBackupDaily(backup_daily);
		setDBBackupDailyTime(backup_daily_time);
		setDBBackupWeekly(backup_weekly);
		setDBBackupWeeklyTime(backup_weekly_time);
		setDBBackupWeeklyWeekday(weekday);
		setEntryTypeSort(entry_type);
		setPubTypeSort(pub_type);
		setShowMinutesInTotals(showMins);
	}
	
	public void upgradePrefs() {
		
		If you get SharedPreferences instance via Context.getSharedPreferences("X"), then your file will be named X.xml.

		It will be located at /data/data/com.your.package.name/shared_prefs/X.xml. You can just delete that file from the location. Also check /data/data/com.your.package.name/shared_prefs/X.bak file, and if it exists, delete it too.

		But be aware, that SharedPreferences instance saves all data in memory. So you'll need to clear preferences first, commit changes and only then delete preferences backing file.

		This should be enough to implement your design decision.
		
		
		SharedPreferences oldSP = mContext.getSharedPreferences("defaults", Context.MODE_PRIVATE);
		
		if(oldSP.contains("publisherID")) {
			int pubID = oldSP.getInt("publisherID", MinistryDatabase.CREATE_ID);
			String pubName = oldSP.getString("publisherName", mContext.getResources().getString(R.string.navdrawer_item_select_publisher));
			
			
			int month = oldSP.getInt("month", Calendar.getInstance().get(Calendar.MONTH));
			int year = oldSP.getInt("year", Calendar.getInstance().get(Calendar.YEAR));
			
			setPublisherID(pubID);
			setPublisherName(pubName);
			
			
			setMonth(month);
			setYear(year);
		}
		
		cleanPrefs();
	}
*/
}