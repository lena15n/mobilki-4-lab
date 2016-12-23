package com.lena.tj.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import static com.lena.tj.db.TravelJournalContract.DATABASE_NAME;
import static com.lena.tj.db.TravelJournalContract.DATABASE_VERSION;
import static com.lena.tj.db.TravelJournalContract.Photo.SQL_CREATE_PHOTO;
import static com.lena.tj.db.TravelJournalContract.Photo.SQL_DELETE_PHOTO;
import static com.lena.tj.db.TravelJournalContract.Sight.SQL_CREATE_SIGHT;
import static com.lena.tj.db.TravelJournalContract.Sight.SQL_DELETE_SIGHT;
import static com.lena.tj.db.TravelJournalContract.Travel.SQL_CREATE_TRAVEL;
import static com.lena.tj.db.TravelJournalContract.Travel.SQL_DELETE_TRAVEL;

public class TravelJournalDbHelper extends SQLiteOpenHelper {
    // If you change the database schema, you must increment the database version.

    public TravelJournalDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_TRAVEL);
        db.execSQL(SQL_CREATE_SIGHT);
        db.execSQL(SQL_CREATE_PHOTO);
    }
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(SQL_DELETE_TRAVEL);
        db.execSQL(SQL_DELETE_SIGHT);
        db.execSQL(SQL_DELETE_PHOTO);
        onCreate(db);
    }
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}