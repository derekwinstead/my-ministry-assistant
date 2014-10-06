package com.myMinistry.provider;

import android.provider.BaseColumns;

import com.myMinistry.provider.MinistryDatabase.Tables;

public class MinistryContract {
	interface TimeColumns {
		/** PublisherID to link to publishers table. */
		String PUBLISHER_ID = "publisherID";
		/** EntryTypeID to link to entryType table. */
		String ENTRY_TYPE_ID = "entryType";
		/** Start Date of the time entry. */
		String DATE_START = "startDate";
		/** End Date of the time entry. */
		String DATE_END = "endDate";
		/** Start time of the entry. */
		String TIME_START = "startTime";
		/** End time of the entry. */
		String TIME_END = "endTime";
	}
	
	public static class Time implements TimeColumns, BaseColumns {
		public static final String[] All_COLS = new String[] {_ID,PUBLISHER_ID,ENTRY_TYPE_ID,DATE_START,DATE_END,TIME_START,TIME_END};
		public static final String SCRIPT_CREATE = "CREATE TABLE " + Tables.TIMES + " ("
									    			+		_ID				+ " INTEGER PRIMARY KEY AUTOINCREMENT"
													+ "," + PUBLISHER_ID	+ " INTEGER"
									    			+ "," + ENTRY_TYPE_ID	+ " INTEGER"
													+ "," + DATE_START		+ " TEXT"
													+ "," + DATE_END		+ " TEXT"
											    	+ "," + TIME_START		+ " TEXT"
													+ "," + TIME_END		+ " TEXT )";
	}
	
	interface RolloverColumns {
		/** PublisherID to link to publishers table. */
		String PUBLISHER_ID = "publisherID";
		/** Date of the roll over. */
		String DATE = "date";
		/** Amount of time to roll over. */
		String MINUTES = "minutes";
	}
	
	public static class Rollover implements RolloverColumns, BaseColumns {
		public static final String[] All_COLS = new String[] {_ID,PUBLISHER_ID,DATE,MINUTES};
		public static final String SCRIPT_CREATE = "CREATE TABLE " + Tables.ROLLOVER + " ("
									    			+ 		_ID				+ " INTEGER PRIMARY KEY AUTOINCREMENT"
									    			+ "," + PUBLISHER_ID	+ " INTEGER"
									    			+ "," + DATE			+ " TEXT"
									    			+ "," + MINUTES			+ " INTEGER )";
	}
	
	interface EntryTypeColumns {
		/** Name of the entry type. */
		String NAME = "name";
		/** Active flag. */
		String ACTIVE = "isActive";
		/** RBC flag. */
		String RBC = "isRBC";
		/** Sorting. */
		String SORT_ORDER = "sortOrder";
	}
	
	public static class EntryType implements EntryTypeColumns, BaseColumns {
		public static final String DEFAULT_SORT = ACTIVE + " DESC," + SORT_ORDER + " ASC," + NAME + " COLLATE NOCASE ASC";
		public static final String[] All_COLS = new String[] {_ID,NAME,ACTIVE,RBC,SORT_ORDER};
		public static final String SCRIPT_CREATE = "CREATE TABLE " + Tables.ENTRY_TYPES + " ("
													+ 		_ID			+ " INTEGER PRIMARY KEY AUTOINCREMENT"
													+ "," + NAME		+ " TEXT"
													+ "," + ACTIVE		+ " INTEGER"
													+ "," + RBC			+ " INTEGER"
													+ "," + SORT_ORDER	+ " INTEGER DEFAULT 0)";
	}

	interface HouseholderColumns {
		/** Name of the householder. */
		String NAME = "name";
		/** Address. */
		String ADDR = "address";
		/** Phone mobile. */
		String MOBILE_PHONE = "phoneMobile";
		/** Phone home. */
		String HOME_PHONE = "phoneHome";
		/** Phone work. */
		String WORK_PHONE = "phoneWork";
		/** Phone other. */
		String OTHER_PHONE = "phoneOther";
		/** Active flag. */
		String ACTIVE = "isActive";
		/** Sort order. */
		String SORT_ORDER = "sortOrder";
	}
	
	public static class Householder implements HouseholderColumns, BaseColumns {
		public static final String DEFAULT_SORT = HouseholderColumns.ACTIVE + " DESC," + HouseholderColumns.SORT_ORDER + " ASC, " + HouseholderColumns.NAME + " COLLATE NOCASE ASC";
		public static final String[] All_COLS = new String[] {_ID,NAME,ADDR,MOBILE_PHONE,HOME_PHONE,WORK_PHONE,OTHER_PHONE,ACTIVE};
		public static final String SCRIPT_CREATE = "CREATE TABLE " + Tables.HOUSEHOLDERS + " ("
													+ 		_ID				+ " INTEGER PRIMARY KEY AUTOINCREMENT"
													+ "," + NAME			+ " TEXT"
													+ "," + ADDR			+ " TEXT"
													+ "," + MOBILE_PHONE	+ " TEXT"
													+ "," + HOME_PHONE		+ " TEXT"
													+ "," + WORK_PHONE		+ " TEXT"
													+ "," + OTHER_PHONE		+ " TEXT"
													+ "," + ACTIVE			+ " INTEGER"
													+ "," + SORT_ORDER		+ " INTEGER DEFAULT 1)";
	}

	interface LiteratureColumns {
		/** Name of the literature. */
		String NAME = "name";
		/** LiteruateTypeID to link to literatureTypes tables. */
		String TYPE_OF_LIERATURE_ID = "typeID";
		/** Active flag. */
		String ACTIVE = "isActive";
		/** Count weight of literature. */
		String WEIGHT = "countWeight";
		/** Sort order. */
		String SORT_ORDER = "sortOrder";
	}
	
	public static class Literature implements LiteratureColumns, BaseColumns {
		public static final String DEFAULT_SORT = Qualified.LITERATURE_ACTIVE + " DESC," + Qualified.LITERATURE_SORT + " ASC," + Qualified.LITERATURE_NAME + " COLLATE NOCASE ASC";
		public static final String[] All_COLS = new String[] {_ID,NAME,TYPE_OF_LIERATURE_ID,ACTIVE,WEIGHT,SORT_ORDER};
		public static final String SCRIPT_CREATE = "CREATE TABLE " + Tables.LITERATURE + " ("
									    			+ 		_ID						+ " INTEGER PRIMARY KEY AUTOINCREMENT"
									    			+ "," + NAME					+ " TEXT"
									    			+ "," + TYPE_OF_LIERATURE_ID	+ " INTEGER"
									    			+ "," + ACTIVE					+ " INTEGER"
									    			+ "," + WEIGHT					+ " INTEGER DEFAULT 1"
									    			+ "," + SORT_ORDER				+ " INTEGER )";
	}

	interface LiteratureTypeColumns {
		/** Name of the literature. */
		String NAME = "name";
		/** Active flag. */
		String ACTIVE = "isActive";
		/** Sort order. */
		String SORT_ORDER = "sortOrder";
	}
	
	public static class LiteratureType implements LiteratureTypeColumns, BaseColumns {
		public static final String DEFAULT_SORT = LiteratureType.ACTIVE + " DESC, " + LiteratureType.SORT_ORDER + ", " + LiteratureType.NAME + " COLLATE NOCASE ASC";
		public static final String[] All_COLS = new String[] {_ID,NAME,ACTIVE,SORT_ORDER};
		public static final String SCRIPT_CREATE = "CREATE TABLE " + Tables.TYPES_OF_LIERATURE + " ("
													+ 		_ID			+ " INTEGER PRIMARY KEY AUTOINCREMENT"
													+ "," + NAME		+ " TEXT"
													+ "," + ACTIVE		+ " INTEGER"
													+ "," + SORT_ORDER	+ " INTEGER )";
	}

	interface PublisherColumns {
		/** Name of the publisher. */
		String NAME = "name";
		/** Active flag. */
		String ACTIVE = "isActive";
	}
	
	public static class Publisher implements PublisherColumns, BaseColumns {
		public static final String DEFAULT_SORT = PublisherColumns.ACTIVE + " DESC, " + PublisherColumns.NAME + " COLLATE NOCASE ASC";
		public static final String[] All_COLS = new String[] {_ID,NAME,ACTIVE};
		public static final String SCRIPT_CREATE = "CREATE TABLE " + Tables.PUBLISHERS + " ("
													+ 		_ID		+ " INTEGER PRIMARY KEY AUTOINCREMENT"
													+ "," + NAME	+ " TEXT"
													+ "," + ACTIVE	+ " INTEGER DEFAULT 1)";
	}
	
	interface LiteraturePlacedColumns {
		/** PublisherID to link to publishers table. */
		String PUBLISHER_ID = "publisherID";
		/** LiteratureID to link to literatureNames table. */
		String LITERATURE_ID = "litNameID";
		/** HouseholderID to link to householders table. */
		String HOUSEHOLDER_ID = "householderID";
		/** TimeID to link to times table. */
		String TIME_ID = "timeID";
		/** Number of literatures placed. */
		String COUNT = "count";
		/** Date of the literature placed. */
		String DATE = "datePlaced";
	}
	
	public static class LiteraturePlaced implements LiteraturePlacedColumns, BaseColumns {
		public static final String[] All_COLS = new String[] {_ID,PUBLISHER_ID,LITERATURE_ID,HOUSEHOLDER_ID,TIME_ID,COUNT,DATE};
		public static final String SCRIPT_CREATE = "CREATE TABLE " + Tables.PLACED_LITERATURE + " ("
									    			+ 		_ID				+ " INTEGER PRIMARY KEY AUTOINCREMENT"
									    			+ "," + PUBLISHER_ID	+ " INTEGER"
									    			+ "," + LITERATURE_ID	+ " INTEGER"
									    			+ "," + HOUSEHOLDER_ID	+ " INTEGER"
									    			+ "," + TIME_ID			+ " INTEGER"
									    			+ "," + COUNT			+ " INTEGER"
									    			+ "," + DATE			+ " TEXT )";
	}
	
	interface PioneeringColumns {
		/** PublisherID to link to publishers table. */
		String PUBLISHER_ID = "publisherID";
		String PIONEERING_TYPE_ID = "pioneeringTypeID";
		String YEAR_START = "yearStart";
		String MONTH_START = "monthStart";
		String YEAR_END = "yearEnd";
		String MONTH_END = "monthEnd";
		String MONTHLY_HOURS = "monthlyHours";
	}
	
	public static class Pioneering implements PioneeringColumns, BaseColumns {
		public static final String DEFAULT_SORT = null;
		public static final String[] All_COLS = new String[] {_ID,PUBLISHER_ID,PIONEERING_TYPE_ID,YEAR_START,MONTH_START,YEAR_END,MONTH_END,MONTHLY_HOURS};
		public static final String SCRIPT_CREATE = "CREATE TABLE " + Tables.PIONEERING + " ("
									    			+ 		_ID					+ " INTEGER PRIMARY KEY AUTOINCREMENT"
									    			+ "," + PUBLISHER_ID		+ " INTEGER"
									    			+ "," + PIONEERING_TYPE_ID	+ " INTEGER"
									    			+ "," + YEAR_START			+ " INTEGER"
									    			+ "," + MONTH_START			+ " INTEGER"
									    			+ "," + YEAR_END			+ " INTEGER"
									    			+ "," + MONTH_END			+ " INTEGER"
									    			+ "," + MONTHLY_HOURS		+ " INTEGER )";
	}
	
	interface PioneeringTypeColumns {
		String NAME = "name";
	}
	
	public static class PioneeringType implements PioneeringTypeColumns, BaseColumns {
		public static final String DEFAULT_SORT = PioneeringType.NAME + " COLLATE NOCASE ASC";
		public static final String[] All_COLS = new String[] {_ID,NAME};
		public static final String SCRIPT_CREATE = "CREATE TABLE " + Tables.TYPES_OF_PIONEERING + " ("
									    			+ 		_ID		+ " INTEGER PRIMARY KEY AUTOINCREMENT"
													+ "," + NAME	+ " TEXT )";
	}
	
	interface TimeHouseholderColumns {
		/** PublisherID to link to publishers table. */
		String TIME_ID = "timeID";
		String HOUSEHOLDER_ID = "householderID";
		String STUDY = "isStudy";
		String RETURN_VISIT = "isReturnVisit";
	}
	
	public static class TimeHouseholder implements TimeHouseholderColumns, BaseColumns {
		public static final String SCRIPT_CREATE = "CREATE TABLE " + Tables.TIME_HOUSEHOLDERS + " ("
									    			+		_ID				+ " INTEGER PRIMARY KEY AUTOINCREMENT"
													+ "," + TIME_ID			+ " INTEGER DEFAULT 0"
									    			+ "," + HOUSEHOLDER_ID	+ " INTEGER DEFAULT 0"
													+ "," + STUDY			+ " INTEGER DEFAULT 0"
													+ "," + RETURN_VISIT	+ " INTEGER DEFAULT 1)";
	}
	
	interface NotesColumns {
		String TIME_ID = "timeID";
		String HOUSEHOLDER_ID = "householderID";
		String NOTES = "notes";
	}
	
	public static class Notes implements NotesColumns, BaseColumns {
		public static final String[] All_COLS = new String[] {_ID,TIME_ID,HOUSEHOLDER_ID,NOTES};
		public static final String SCRIPT_CREATE = "CREATE TABLE " + Tables.NOTES + " ("
									    			+ 		_ID				+ " INTEGER PRIMARY KEY AUTOINCREMENT"
									    			+ "," + TIME_ID			+ " INTEGER"
									    			+ "," + HOUSEHOLDER_ID	+ " INTEGER"
									    			+ "," + NOTES			+ " TEXT)";
	}
	
	public interface UnionsNameAsRef {
		String _ID = "_id";
		String DATE = "date";
		String TYPE_ID = "unionTypeID";
		String TITLE = "title";
		String ACTIVE = "isActive";
		String NOTE_ID = "noteID";
		String NAME = "name";
		String DATE_START = "dateStart";
		String DATE_END = "dateEnd";
		String PUBLISHER_NAME = "publisherName";
		String HOUSEHOLDER_NAME = "householderName";
		String ENTRY_TYPE_NAME = "entryTypeName";
		String COUNT = "count";
		String HH_COUNT = "HHcount";
	}
	
	interface UnionsNameAsCols {
		String _ID = " as " + UnionsNameAsRef._ID;
		String DATE = " as " + UnionsNameAsRef.DATE;
		String TYPE_ID = " as " + UnionsNameAsRef.TYPE_ID;
		String TITLE = " as " + UnionsNameAsRef.TITLE;
		String ACTIVE = " as " + UnionsNameAsRef.ACTIVE;
		String NOTE_ID = " as " + UnionsNameAsRef.NOTE_ID;
		String NAME = " as " + UnionsNameAsRef.NAME;
		String DATE_START = " as " + UnionsNameAsRef.DATE_START;
		String DATE_END = " as " + UnionsNameAsRef.DATE_END;
		String PUBLISHER_NAME = " as " + UnionsNameAsRef.PUBLISHER_NAME;
		String HOUSEHOLDER_NAME = " as " + UnionsNameAsRef.HOUSEHOLDER_NAME;
		String ENTRY_TYPE_NAME = " as " + UnionsNameAsRef.ENTRY_TYPE_NAME;
		String COUNT = " as " + UnionsNameAsRef.COUNT;
		String HH_COUNT = " as " + UnionsNameAsRef.HH_COUNT;
	}
	
	interface Qualified {
		String LITERATURE_ID = Tables.LITERATURE + "." + Literature._ID;
		String LITERATURE_NAME = Tables.LITERATURE + "." + Literature.NAME;
		String LITERATURE_TYPE_ID_LINK = Tables.LITERATURE + "." + Literature.TYPE_OF_LIERATURE_ID;
		String LITERATURE_WEIGHT = Tables.LITERATURE + "." + Literature.WEIGHT;
		String LITERATURE_SORT = Tables.LITERATURE + "." + Literature.SORT_ORDER;
		String LITERATURE_ACTIVE = Tables.LITERATURE + "." + Literature.ACTIVE;
		
		String TYPE_OF_LITERATURE_ID = Tables.TYPES_OF_LIERATURE + "." + LiteratureType._ID;
		String TYPE_OF_LITERATURE_NAME = Tables.TYPES_OF_LIERATURE + "." + LiteratureType.NAME;
		
		String PLACED_LITERATURE_PUBLISHER_ID = Tables.PLACED_LITERATURE + "." + LiteraturePlaced.PUBLISHER_ID;
		String PLACED_LITERATURE_DATE = Tables.PLACED_LITERATURE + "." + LiteraturePlaced.DATE;
		String PLACED_LITERATURE_ID = Tables.PLACED_LITERATURE + "." + LiteraturePlaced._ID;
		String PLACED_LITERATURE_HOUSEHOLDER_ID = Tables.PLACED_LITERATURE + "." + LiteraturePlaced.HOUSEHOLDER_ID;
		String PLACED_LITERATURE_COUNT = Tables.PLACED_LITERATURE + "." + LiteraturePlaced.COUNT;
		String PLACED_LITERATURE_LIT_ID = Tables.PLACED_LITERATURE + "." + LiteraturePlaced.LITERATURE_ID;
		String PLACED_LITERATURE_TIME_ID = Tables.PLACED_LITERATURE + "." + LiteraturePlaced.TIME_ID;
		
		String ENTRY_TYPE_ID = Tables.ENTRY_TYPES + "." + EntryType._ID;
		String ENTRY_TYPE_RBC = Tables.ENTRY_TYPES + "." + EntryType.RBC;
		String ENTRY_TYPE_NAME = Tables.ENTRY_TYPES + "." + EntryType.NAME;
		
		String TIME_ID = Tables.TIMES + "." + Time._ID;
		String TIME_PUBLISHER_ID = Tables.TIMES + "." + Time.PUBLISHER_ID;
		String TIME_ENTRY_TYPE_ID = Tables.TIMES + "." + Time.ENTRY_TYPE_ID;
		String TIME_DATE_START = Tables.TIMES + "." + Time.DATE_START;
		String TIME_DATE_END = Tables.TIMES + "." + Time.DATE_END;
		String TIME_TIME_START = Tables.TIMES + "." + Time.TIME_START;
		String TIME_TIME_END = Tables.TIMES + "." + Time.TIME_END;
		
		String HOUSEHOLDER_NAME = Tables.HOUSEHOLDERS + "." + Householder.NAME;
		String HOUSEHOLDER_ID = Tables.HOUSEHOLDERS + "." + Householder._ID;
		
		String PUBLISHER_ID = Tables.PUBLISHERS + "." + Publisher._ID;
		String PUBLISHER_NAME = Tables.PUBLISHERS + "." + Publisher.NAME;
		
		String TIMEHOUSEHOLDER_ID = Tables.TIME_HOUSEHOLDERS + "." + TimeHouseholder._ID;
		String TIMEHOUSEHOLDER_TIME_ID = Tables.TIME_HOUSEHOLDERS + "." + TimeHouseholder.TIME_ID;
		String TIMEHOUSEHOLDER_HOUSEHOLDER_ID = Tables.TIME_HOUSEHOLDERS + "." + TimeHouseholder.HOUSEHOLDER_ID;
		String TIMEHOUSEHOLDER_IS_RETURN_VISIT = Tables.TIME_HOUSEHOLDERS + "." + TimeHouseholder.RETURN_VISIT;

		String NOTES_ID = Tables.NOTES + "." + Notes._ID;
		String NOTES_TIME_ID = Tables.NOTES + "." + Notes.TIME_ID;
		String NOTES_HOUSEHOLDER_ID = Tables.NOTES + "." + Notes.HOUSEHOLDER_ID;
		String NOTES_NOTES = Tables.NOTES + "." + Notes.NOTES;
		
		String PIONEERING_ID = Tables.PIONEERING + "." + Pioneering._ID;
		
		String TYPE_OF_PIONEERING_ID = Tables.TYPES_OF_PIONEERING + "." + PioneeringType._ID;
	}
	
	interface Joins {
		String LITERATURE_JOIN_PLACED_LITERATURE = " INNER JOIN " + Tables.LITERATURE + " ON " + Qualified.LITERATURE_ID + " = " + Qualified.PLACED_LITERATURE_LIT_ID;
		String TYPE_LITERATURE_JOIN_LITERATURE = " INNER JOIN " + Tables.TYPES_OF_LIERATURE + " ON " + Qualified.TYPE_OF_LITERATURE_ID + " = " + Qualified.LITERATURE_TYPE_ID_LINK;
		String TIME_JOIN_TIMEHOUSEHOLDER = " INNER JOIN " + Tables.TIMES + " ON " + Qualified.TIME_ID + " = " + Qualified.TIMEHOUSEHOLDER_TIME_ID;
		String PLACED_LITERATURE_ON_TIME = " INNER JOIN " + Tables.PLACED_LITERATURE + " ON " + Qualified.PLACED_LITERATURE_TIME_ID + " = " + Qualified.TIME_ID;
		String TIMEHOUSEHOLDER_JOIN_TIME = " INNER JOIN " + Tables.TIME_HOUSEHOLDERS + " ON " + Qualified.TIMEHOUSEHOLDER_TIME_ID + " = " + Qualified.TIME_ID;
		String TYPE_OF_PIONEERING_JOIN_PIONEERING = " INNER JOIN " + Tables.TYPES_OF_PIONEERING + " ON " + Qualified.TYPE_OF_PIONEERING_ID + " = " + Qualified.PIONEERING_ID;
		String PLACED_LITERATURE_ON_LITERATURE_NAMES = " INNER JOIN " + Tables.PLACED_LITERATURE + " ON " + Qualified.PLACED_LITERATURE_LIT_ID + " = " + Qualified.LITERATURE_ID;
		String TIME_ON_PLACED_LITERATURE = " INNER JOIN " + Tables.TIMES + " ON " + Qualified.TIME_ID + " = " + Qualified.PLACED_LITERATURE_TIME_ID;
		String ENTRY_TYPES_ON_TIME = " INNER JOIN " + Tables.ENTRY_TYPES + " ON " + Qualified.ENTRY_TYPE_ID + " = " + Qualified.TIME_ENTRY_TYPE_ID;
		String PUBLISHERS_ON_PLACED_LITERATURE = " INNER JOIN " + Tables.PUBLISHERS + " ON " + Qualified.PUBLISHER_ID + " = " + Qualified.PLACED_LITERATURE_PUBLISHER_ID;
		String PUBLISHERS_ON_TIME = " INNER JOIN " + Tables.PUBLISHERS + " ON " + Qualified.PUBLISHER_ID + " = " + Qualified.TIME_PUBLISHER_ID;
	}
	
	interface LeftJoins {
		String HOUSEHOLDERS_JOIN_TIMEHOUSEHOLDERS = " LEFT JOIN " + Tables.HOUSEHOLDERS + " ON " + Qualified.HOUSEHOLDER_ID + " = " + Qualified.TIMEHOUSEHOLDER_HOUSEHOLDER_ID;
		String HOUSEHOLDERS_JOIN_PLACED_LITERATURE = " LEFT JOIN " + Tables.HOUSEHOLDERS + " ON " + Qualified.HOUSEHOLDER_ID + " = " + Qualified.PLACED_LITERATURE_HOUSEHOLDER_ID;
		String ENTRY_TYPES_JOIN_TIME = " LEFT JOIN " + Tables.ENTRY_TYPES + " ON " + Qualified.ENTRY_TYPE_ID + " = " + Qualified.TIME_ENTRY_TYPE_ID;
		String NOTES_ON_TIMEHOUSEHOLDER_AND_TIME = " LEFT JOIN " + Tables.NOTES + " ON (" + Qualified.NOTES_HOUSEHOLDER_ID + " = " + Qualified.TIMEHOUSEHOLDER_HOUSEHOLDER_ID + " AND " + Qualified.NOTES_TIME_ID + " = " + Qualified.TIME_ID + ")";
	}
	
	private MinistryContract() { }
}