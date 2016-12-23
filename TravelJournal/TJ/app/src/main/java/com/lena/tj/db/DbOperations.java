package com.lena.tj.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class DbOperations {
    public static void insertNewSight(Context context, String desc, String iconCode, double latitude, double longitude){
        TravelJournalDbHelper mDbHelper = new TravelJournalDbHelper(context);
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        //insert into Record
        ContentValues values = new ContentValues();
        values.put(TravelJournalContract.Sight.DESCRIPTION, desc);
        values.put(TravelJournalContract.Sight.ICON, iconCode);
        values.put(TravelJournalContract.Sight.LATITUDE, latitude);
        values.put(TravelJournalContract.Sight.LONGITUDE, longitude);
        long newRecordId = db.insert(TravelJournalContract.Sight.TABLE_NAME, null, values);
        db.close();
        verify(context);
    }

    private static void verify(Context context) {
        TravelJournalDbHelper dbHelper = new TravelJournalDbHelper(context);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String[] projection = {
                TravelJournalContract.Sight._ID,
                TravelJournalContract.Sight.DESCRIPTION,
                TravelJournalContract.Sight.ICON,
                TravelJournalContract.Sight.LATITUDE,
                TravelJournalContract.Sight.LONGITUDE
        };

        // How you want the results sorted in the resulting Cursor
        String sortOrder =
                TravelJournalContract.Sight._ID + " ASC";

        Cursor cursor = db.query(
                TravelJournalContract.Sight.TABLE_NAME,  // The table to query
                projection,                               // The columns to return
                null,                                     // The columns for the WHERE clause
                null,                                     // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                sortOrder                                 // The sort order
        );

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    long id = cursor.getLong(cursor.getColumnIndex(TravelJournalContract.Sight._ID));
                    String desc = cursor.getString(cursor.getColumnIndex(TravelJournalContract.Sight.DESCRIPTION));
                    String icon = cursor.getString(cursor.getColumnIndex(TravelJournalContract.Sight.ICON));
                    double lat = cursor.getDouble(cursor.getColumnIndex(TravelJournalContract.Sight.LATITUDE));
                    double lon = cursor.getDouble(cursor.getColumnIndex(TravelJournalContract.Sight.LONGITUDE));

                    Log.d("Mi", "Record: id = " + id + ", name: " + desc + ", icon: " + icon +
                            "\nlat: " + lat + ", long: " + lon);
                }
                while (cursor.moveToNext());
            }
        }

        cursor.close();
        db.close();
    }

}
