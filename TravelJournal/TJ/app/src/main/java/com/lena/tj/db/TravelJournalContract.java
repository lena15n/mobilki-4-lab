package com.lena.tj.db;

import android.provider.BaseColumns;

public final class TravelJournalContract {
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "TravelJournal.db";

    private static final String TEXT_TYPE = " TEXT ";
    private static final String INTEGER_TYPE = " INTEGER ";
    private static final String REAL_TYPE = " REAL ";
    private static final String COMMA_SEP = ",";

    // To prevent someone from accidentally instantiating the contract class,
    // make the constructor private.
    private TravelJournalContract() {}

    public static final class Travel implements BaseColumns {
        public static final String TABLE_NAME       = "travel";
        public static final String NAME = "name";
        public static final String LINE_COLOR = "color";

        public static final String SQL_CREATE_TRAVEL = "CREATE TABLE " +
                TABLE_NAME + " (" +
                _ID + " INTEGER PRIMARY KEY" + COMMA_SEP +
                NAME + TEXT_TYPE + COMMA_SEP +
                LINE_COLOR + " )";
        public static final String SQL_DELETE_TRAVEL = "DROP TABLE IF EXISTS " + TABLE_NAME;

        private Travel (){}
    }

    public static final class Sight implements BaseColumns {
        public static final String TABLE_NAME       = "sight";
        public static final String DESCRIPTION = "description";
        public static final String ICON = "icon";
        public static final String LATITUDE = "latitude";
        public static final String LONGITUDE = "longitude";
        public static final String ORDER = "order";
        public static final String TRAVEL_ID = "travel_id";

        public static final String TEMP_TABLE_NAME = "temp_column";
        public static final String TEMP_SIGHT_ID = "sight_id";
        public static final String TEMP_PHOTO_ID = "photo_id";


        public static final String SQL_CREATE_SIGHT = "CREATE TABLE " +
                TABLE_NAME + " (" +
                _ID + " INTEGER PRIMARY KEY," +
                DESCRIPTION + TEXT_TYPE + COMMA_SEP +
                ICON + TEXT_TYPE + COMMA_SEP +
                LATITUDE + REAL_TYPE + COMMA_SEP +
                LONGITUDE + REAL_TYPE + COMMA_SEP +
                ORDER + INTEGER_TYPE + COMMA_SEP +
                TRAVEL_ID + INTEGER_TYPE + " )";

        public static final String SQL_LEFT_JOIN_PHOTO = "SELECT " +
                TABLE_NAME + "." + DESCRIPTION + COMMA_SEP +
                TABLE_NAME + "." + ICON + COMMA_SEP +
                TABLE_NAME + "." + LATITUDE + COMMA_SEP +
                TABLE_NAME + "." + LONGITUDE + COMMA_SEP +
                TABLE_NAME + "." + TRAVEL_ID + COMMA_SEP +
                TABLE_NAME + "." + ORDER + COMMA_SEP +
                TABLE_NAME + "." + _ID + " AS " + TEMP_SIGHT_ID + COMMA_SEP +
                Photo.TABLE_NAME + "." + Photo._ID + " AS " + TEMP_PHOTO_ID + COMMA_SEP +
                Photo.TABLE_NAME + "." + Photo.URI +
                " FROM " + TABLE_NAME + " LEFT JOIN " + Photo.TABLE_NAME +
                " ON " + TABLE_NAME + "." + _ID + " = " + Photo.TABLE_NAME + "." + Photo.SIGHT_ID;

        public static final String SQL_DELETE_SIGHT = "DROP TABLE IF EXISTS " + TABLE_NAME;

        private Sight (){}
    }

    public static final class Photo implements BaseColumns {
        public static final String TABLE_NAME      = "photo";
        public static final String URI = "uri";
        public static final String SIGHT_ID = "sight_id";

        public static final String SQL_CREATE_PHOTO = "CREATE TABLE " +
                TABLE_NAME + " (" +
                _ID + " INTEGER PRIMARY KEY," +
                URI + TEXT_TYPE + COMMA_SEP +
                SIGHT_ID + INTEGER_TYPE + " )";
        public static final String SQL_DELETE_PHOTO = "DROP TABLE IF EXISTS " + TABLE_NAME;

        private Photo (){}
    }
}