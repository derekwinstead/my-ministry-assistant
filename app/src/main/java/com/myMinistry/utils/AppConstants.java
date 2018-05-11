package com.myMinistry.utils;

public class AppConstants {
    public static final String ARG_NAME = "name";
    public static final String ARG_IS_ACTIVE = "is_active";
    public static final String ARG_NOTES = "notes";
    public static final String ARG_PUBLICATION_TYPE_ID = "publication_type_id";
    public static final String ARG_PUBLICATION_NAME = "publication_name";
    public static final String ARG_HOUR_OF_DAY = "hour_of_day";
    public static final String ARG_MINUTES = "minutes";
    public static final String ARG_TIME_ID = "time_id";
    public static final String ARG_PUBLISHER_ID = "publisher_id";
    public static final String ARG_PUBLICATION_ID = "publication_id";
    public static final String ARG_HOUSEHOLDER_ID = "householder_id";
    public static final String ARG_ENTRY_TYPE_ID = "entry_type_id";

    public static final String ARG_SHOW_FLOW = "show_flow";

    public static final int ACTIVE = 1;
    public static final int INACTIVE = 0;

    public static final String PREF_VERSION_NUMBER = "version_number";
    public static final String PREF_AUTO_BACKUPS = "db_do_auto_backups";
    public static final String PREF_BACKUP_DAILY_TIME = "db_backup_daily_time";
    public static final String PREF_BACKUP_WEEKLY_TIME = "db_backup_weekly_time";
    public static final String PREF_BACKUP_WEEKLY_WEEKDAY = "db_backup_weekly_weekday";
    public static final String PREF_PUBLISHER_ID = "publisher_id";
    public static final String PREF_SUMMARY_MONTH = "saved_month";
    public static final String PREF_SUMMARY_YEAR = "saved_year";
    public static final String PREF_LOCALE = "locale";


    public static final String DATABASE_NAME = "myministry.db";
    public static final String DATABASE_NAME_OLD = "myministry";

    public static final int ID_PUBLICATION_TYPE_BOOKS = 1;
    public static final int ID_PUBLICATION_TYPE_BROCHURES = 2;
    public static final int ID_PUBLICATION_TYPE_MAGAZINES = 3;
    public static final int ID_PUBLICATION_TYPE_MEDIA = 4;
    public static final int ID_PUBLICATION_TYPE_TRACTS = 5;
    public static final int ID_PUBLICATION_TYPE_VIDEOS_TO_SHOW = 6;

    public static final int ID_ENTRY_TYPE_ROLLOVER = 1;
    public static final int ID_ENTRY_TYPE_BIBLE_STUDY = 2;
    public static final int ID_ENTRY_TYPE_RETURN_VISIT = 3;
    public static final int ID_ENTRY_TYPE_SERVICE = 4;
    public static final int ID_ENTRY_TYPE_RBC = 5;

    public static final int ID_PIONEERING = 1;
    public static final int ID_PIONEERING_AUXILIARY = 2;
    public static final int ID_PIONEERING_AUXILIARY_SPECIAL = 3;

    public static final int CREATE_ID = -5;

    public static final int NO_HOUSEHOLDER_ID = -1;

/*
    public static final int ID_PIONEERING = 1;
    public static final int ID_PIONEERING_AUXILIARY = 2;
    public static final int ID_PIONEERING_AUXILIARY_SPECIAL = 3;
    */

/*
    public static final int ID_ROLLOVER = 1;
    public static final int ID_BIBLE_STUDY = 2;
    public static final int ID_RETURN_VISIT = 3;
    public static final int ID_SERVICE = 4;
    public static final int ID_RBC = 5;
    */
/*
    public static final int ID_BOOKS = 1;
    public static final int ID_BROCHURES = 2;
    public static final int ID_MAGAZINES = 3;
    public static final int ID_MEDIA = 4;
    public static final int ID_TRACTS = 5;
    public static final int ID_VIDEOS_TO_SHOW = 6;
*/
    /*
        public static final String STATUS_CODE_FAILED = "failed";

        public static final int API_STATUS_CODE_LOCAL_ERROR = 0;

        public static final String DB_NAME = "mindorks_mvp.db";
        public static final String PREF_NAME = "mindorks_pref";

        public static final long NULL_INDEX = -1L;

        public static final String SEED_DATABASE_OPTIONS = "seed/options.json";
        public static final String SEED_DATABASE_QUESTIONS = "seed/questions.json";

        public static final String TIMESTAMP_FORMAT = "yyyyMMdd_HHmmss";
    */
    private AppConstants() {
        // This utility class is not publicly instantiable
    }
}