package com.lena.tj.db;

import android.provider.BaseColumns;

public final class TravelJournalContract {
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "TravelJournal.db";

    private static final String TEXT_TYPE = " TEXT ";
    private static final String INTEGER_TYPE = " INTEGER ";
    private static final String REAL_TYPE = " REAL ";
    private static final String COMMA_SEP = ", ";

    // To prevent someone from accidentally instantiating the contract class,
    // make the constructor private.
    private TravelJournalContract() {}

    public static final class Travel implements BaseColumns {
        public static final String TABLE_NAME       = "travel";
        public static final String NAME = "name";
        public static final String COLOR = "color";
        public static final String TEMP_ID = "tr_id";

        public static final String SQL_CREATE_TRAVEL = "CREATE TABLE " +
                TABLE_NAME + " (" +
                _ID + " INTEGER PRIMARY KEY" + COMMA_SEP +
                NAME + TEXT_TYPE + COMMA_SEP +
                COLOR + INTEGER_TYPE + " )";
        public static final String SQL_DELETE_TRAVEL = "DROP TABLE IF EXISTS " + TABLE_NAME;

        public static final String SQL_GET_ALL_TRAVELS = " SELECT " +
                _ID + COMMA_SEP +
                NAME + COMMA_SEP +
                COLOR + COMMA_SEP +
                Sight.TEMP_TABLE_NAME + "." + Sight.TEMP_SIGHT_ID + COMMA_SEP +
                Sight.TEMP_TABLE_NAME + "." + Sight.LATITUDE + COMMA_SEP +
                Sight.TEMP_TABLE_NAME + "." + Sight.DESCRIPTION + COMMA_SEP +
                Sight.TEMP_TABLE_NAME + "." + Sight.ICON + COMMA_SEP +
                Sight.TEMP_TABLE_NAME + "." + Sight.LATITUDE + COMMA_SEP +
                Sight.TEMP_TABLE_NAME + "." + Sight.LONGITUDE + COMMA_SEP +
                Sight.TEMP_TABLE_NAME + "." + Sight.ORDER + COMMA_SEP +
                Sight.TEMP_TABLE_NAME + "." + Sight.TEMP_PHOTO_ID + " AS " + Sight.TEMP_PHOTO_ID + COMMA_SEP +
                Sight.TEMP_TABLE_NAME + "." + Photo.URI +
                " FROM " + TABLE_NAME +
                " LEFT JOIN ( SELECT " +
                    Sight.TABLE_NAME + "." + Sight._ID + " AS " + Sight.TEMP_SIGHT_ID + COMMA_SEP +
                    Sight.TABLE_NAME + "." + Sight.LATITUDE + COMMA_SEP +
                    Sight.TABLE_NAME + "." + Sight.DESCRIPTION + COMMA_SEP +
                    Sight.TABLE_NAME + "." + Sight.ICON + COMMA_SEP +
                    Sight.TABLE_NAME + "." + Sight.LATITUDE + COMMA_SEP +
                    Sight.TABLE_NAME + "." + Sight.LONGITUDE + COMMA_SEP +
                    Sight.TABLE_NAME + "." + Sight.ORDER + COMMA_SEP +
                    Sight.TABLE_NAME + "." + Sight.TRAVEL_ID + COMMA_SEP +
                    Photo.TABLE_NAME + "." + Photo._ID + " AS " + Sight.TEMP_PHOTO_ID + COMMA_SEP +
                    Photo.TABLE_NAME + "." + Photo.URI +
                    " FROM " + Sight.TABLE_NAME + " LEFT JOIN " + Photo.TABLE_NAME +
                    " ON " + Sight.TABLE_NAME + "." + Sight._ID + " = " +
                    Photo.TABLE_NAME + "." + Photo._ID +
                " ) AS " + Sight.TEMP_TABLE_NAME + " ON " +
                TABLE_NAME + "." + _ID + " = " + Sight.TEMP_TABLE_NAME + "." + Sight.TRAVEL_ID +
                " ORDER BY " + _ID + ", " + Sight.ORDER;

        public static final String SELECT_COLOR_OF_TRAVEL = " SELECT " + COLOR +
                " FROM " + TABLE_NAME +
                " WHERE " + _ID + " = ?";

        public static final String SQL_GET_THE_NEAREST_TRAVEL = "SELECT * " +
                "FROM ( " + SQL_GET_ALL_TRAVELS + ") " +
                " WHERE " + _ID + " = (" + Sight.SQL_FIND_THE_NEAREST_TRAVEL_ID + ") ";

        private Travel (){}
    }

    public static final class Sight implements BaseColumns {
        public static final String TABLE_NAME       = "sight";
        public static final String DESCRIPTION = "description";
        public static final String ICON = "icon";
        public static final String LATITUDE = "latitude";
        public static final String LONGITUDE = "longitude";
        public static final String ORDER = "order_in_travel";
        public static final String TRAVEL_ID = "travel_id";

        public static final String TEMP_TABLE_NAME = "temp_table";
        public static final String TEMP_SIGHT_ID = "sight_id";
        public static final String TEMP_PHOTO_ID = "photo_id";
        public static final String TEMP_COLUMN = "temp";

        private static final double ACCURACY = 0.0000000000001;//10^-6 max

        public static final String SQL_CREATE_SIGHT = "CREATE TABLE " + TABLE_NAME + " (" +
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

        public static final String SELECT_FIRST_SIGHT_OF_TRAVEL = " SELECT " +
                TABLE_NAME + "." + _ID + ", MIN( " +
                TABLE_NAME + "." + ORDER + ") AS " + Sight.TEMP_COLUMN +
                " FROM " + TABLE_NAME +
                " WHERE ( " + TABLE_NAME + "." + TRAVEL_ID + " = ? ) ";

        public static final String SELECT_LAST_SIGHT_OF_TRAVEL = " SELECT " +
                TABLE_NAME + "." + _ID + ", MAX( " +
                TABLE_NAME + "." + ORDER + ") AS " + Sight.TEMP_COLUMN +
                " FROM " + TABLE_NAME +
                " WHERE ( " + TABLE_NAME + "." + TRAVEL_ID + " = ? ) ";

        public static final String UPDATE_INCREMENT_ORDER = " UPDATE " + TABLE_NAME +
                " SET " + ORDER + " = " +
                ORDER + " + 1" +
                " WHERE ( " + TRAVEL_ID + " = ?) ";

        public static final String SELECT_SMTH = " SELECT * FROM " +
                TABLE_NAME +
                " WHERE ABS(" + //LATITUDE + " = 22.59371151533926 OR " +
                LONGITUDE + " - ?) < 0.000000000000001 ";//10^-6 max

        public static final String WHERE_LAT_LONG = " ( " +
                "ABS(" + LATITUDE + "  - ?) < " + ACCURACY + " AND " +
                "ABS(" + LONGITUDE + " - ?) < " + ACCURACY + ") OR (" +
                "ABS(" + LATITUDE + "  - ?) < " + ACCURACY + " AND " +
                "ABS(" + LONGITUDE + " - ?) < " + ACCURACY + ")";

        public static final String SQL_FIND_THE_NEAREST_TRAVEL_ID_TEMP = " SELECT " +
                TRAVEL_ID + COMMA_SEP +
                " MIN( " +
                    "(" + LATITUDE + " - ?" + ")*(" + LATITUDE + " - ?" + ") + " +
                    "(" + LONGITUDE + " - ?" + ")*(" + LONGITUDE + " - ?)" +
                ") AS " + TEMP_COLUMN +
                " FROM " + TABLE_NAME +
                " WHERE " + TRAVEL_ID + " IS NOT NULL ";

        public static final String SQL_FIND_THE_NEAREST_TRAVEL_ID = "SELECT " +
                TRAVEL_ID +
                " FROM (SELECT " +
                TRAVEL_ID + COMMA_SEP +
                _ID + COMMA_SEP +
                " MIN( " +
                    "(" + LATITUDE + " - ?" + ")*(" + LATITUDE + " - ?" + ") + " +
                    "(" + LONGITUDE + " - ?" + ")*(" + LONGITUDE + " - ?)" +
                ") AS " + TEMP_COLUMN +
                " FROM " + TABLE_NAME +
                " WHERE " + TRAVEL_ID + " IS NOT NULL)";

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