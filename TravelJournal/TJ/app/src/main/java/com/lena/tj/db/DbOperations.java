package com.lena.tj.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.lena.tj.MapsActivity;
import com.lena.tj.dataobjects.DOSight;
import com.lena.tj.dataobjects.DOTravel;

import java.util.ArrayList;

import static com.lena.tj.db.TravelJournalContract.Sight.SELECT_FIRST_SIGHT_OF_TRAVEL;
import static com.lena.tj.db.TravelJournalContract.Sight.SELECT_LAST_SIGHT_OF_TRAVEL;
import static com.lena.tj.db.TravelJournalContract.Sight.UPDATE_INCREMENT_ORDER;

public class DbOperations {
    public static void insertNewSight(Context context, String desc, String iconCode, double latitude, double longitude) {
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

    public static ArrayList<DOSight> getAllSeparateSights(Context context) {
        ArrayList<DOSight> sights = new ArrayList<>();
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

        String travelIdIsEmpty = TravelJournalContract.Sight.TRAVEL_ID + " IS NULL";

        Cursor cursor = db.query(
                TravelJournalContract.Sight.TABLE_NAME,  // The table to query
                projection,                               // The columns to return
                travelIdIsEmpty,                          // The columns for the WHERE clause
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

                    Log.d(MapsActivity.LOG_TAG, "Record: id = " + id + ", name: " + desc + ", icon: " + icon +
                            "\nlat: " + lat + ", long: " + lon);

                    sights.add(new DOSight(id, desc, lat, lon, icon));
                }
                while (cursor.moveToNext());
            }
        }

        cursor.close();
        db.close();

        return sights;
    }

    /**
     * Create new travel if travelIds of both points equals NULL
     * and modify old travel if add new point to start or end of old travel
     * Only one travel maybe between same sights/points
     */
    public static boolean createTravel(Context context, LatLng from, LatLng to, String name) {
        TravelJournalDbHelper mDbHelper = new TravelJournalDbHelper(context);
        long newTravelId = -1;
        DOSight fromSight = null;
        DOSight toSight = null;
        boolean result = false;

        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        String[] projection = new String[]{
                TravelJournalContract.Sight._ID,
                TravelJournalContract.Sight.LATITUDE,
                TravelJournalContract.Sight.LONGITUDE,
                TravelJournalContract.Sight.TRAVEL_ID,
                TravelJournalContract.Sight.ORDER
        };

        String where = " (" + TravelJournalContract.Sight.LATITUDE + " = ? AND " +
                TravelJournalContract.Sight.LONGITUDE + " = ?) OR " +
                "(" + TravelJournalContract.Sight.LATITUDE + " = ? AND " +
                TravelJournalContract.Sight.LONGITUDE + " = ?)";

        Cursor cursor = db.query(TravelJournalContract.Sight.TABLE_NAME, projection, where,
                new String[]{
                        String.valueOf(from.latitude),
                        String.valueOf(from.longitude),
                        String.valueOf(to.latitude),
                        String.valueOf(to.longitude),
                }, null, null, null);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    Long id = cursor.getLong(cursor.getColumnIndex(TravelJournalContract.Sight._ID));
                    Double lat = cursor.getDouble(cursor.getColumnIndex(TravelJournalContract.Sight.LATITUDE));
                    Double lon = cursor.getDouble(cursor.getColumnIndex(TravelJournalContract.Sight.LONGITUDE));
                    Long travel_id = null;
                    if (!cursor.isNull(cursor.getColumnIndex(TravelJournalContract.Sight.TRAVEL_ID))) {
                        travel_id = cursor.getLong(cursor.getColumnIndex(TravelJournalContract.Sight.TRAVEL_ID));
                    }

                    Long order = null;
                    if (!cursor.isNull(cursor.getColumnIndex(TravelJournalContract.Sight.ORDER))) {
                        order = cursor.getLong(cursor.getColumnIndex(TravelJournalContract.Sight.ORDER));
                    }
                    if (lat.equals(from.latitude) && lon.equals(from.longitude)) {
                        fromSight = new DOSight(id, "", lat, lon, "", travel_id, order, null);
                    } else {
                        toSight = new DOSight(id, "", lat, lon, "", travel_id, order, null);
                    }
                }
                while (cursor.moveToNext());
            }
        }

        if (fromSight != null && toSight != null) {
            if (fromSight.getTravelId() == null && toSight.getTravelId() == null) {
                // Create new Travel

                db = mDbHelper.getWritableDatabase();
                //insert into Travel
                ContentValues values = new ContentValues();
                values.put(TravelJournalContract.Travel.NAME, name);
                newTravelId = db.insert(TravelJournalContract.Travel.TABLE_NAME, null, values);

                values = new ContentValues();
                values.put(TravelJournalContract.Sight.TRAVEL_ID, newTravelId);
                values.put(TravelJournalContract.Sight.ORDER, 1);
                String whereClause = TravelJournalContract.Sight._ID + " = ? ";
                db.update(TravelJournalContract.Sight.TABLE_NAME, values, whereClause, new String[]{
                        String.valueOf(fromSight.getId())});

                values = new ContentValues();
                values.put(TravelJournalContract.Sight.TRAVEL_ID, newTravelId);
                values.put(TravelJournalContract.Sight.ORDER, 2);
                db.update(TravelJournalContract.Sight.TABLE_NAME, values, whereClause, new String[]{
                        String.valueOf(toSight.getId())});
                result = true;
            } else if (!fromSight.getTravelId().equals(toSight.getTravelId())) {
                if (fromSight.getTravelId() == null && toSight.getTravelId() != null) {
                    // Modify old travel

                    Cursor orderCur = db.rawQuery(SELECT_FIRST_SIGHT_OF_TRAVEL,
                            new String[]{String.valueOf(fromSight.getTravelId())});
                    long firstSightId = -1;

                    if (orderCur != null) {
                        if (orderCur.moveToFirst()) {
                            firstSightId = orderCur.getLong(orderCur.getColumnIndex(TravelJournalContract.Sight._ID));
                        }
                    }

                    if (firstSightId == fromSight.getId()) {// if sight id == 1
                        db = mDbHelper.getWritableDatabase();
                        db.execSQL(UPDATE_INCREMENT_ORDER, new String[]{
                                String.valueOf(fromSight.getTravelId())});

                        ContentValues values = new ContentValues();
                        values.put(TravelJournalContract.Sight.TRAVEL_ID, newTravelId);
                        values.put(TravelJournalContract.Sight.ORDER, 1);
                        String whereClause = " (" + TravelJournalContract.Sight._ID + " = ? ) ";
                        db.update(TravelJournalContract.Sight.TABLE_NAME, values, whereClause, new String[]{
                                String.valueOf(toSight.getId())});
                        result = true;
                    }
                } else if (fromSight.getTravelId() != null && toSight.getTravelId() == null) {
                    // Modify old travel

                    Cursor orderCur = db.rawQuery(SELECT_LAST_SIGHT_OF_TRAVEL,
                            new String[]{String.valueOf(fromSight.getTravelId())});
                    long lastSightOrder = -1;
                    long lastSightId = -1;

                    if (orderCur != null) {
                        if (orderCur.moveToFirst()) {
                            lastSightId = orderCur.getLong(orderCur.getColumnIndex(TravelJournalContract.Sight._ID));
                            lastSightOrder = orderCur.getLong(orderCur.getColumnIndex(TravelJournalContract.Sight.ORDER));
                        }
                    }

                    if (lastSightId == fromSight.getId()) {// if sight id == 1
                        db = mDbHelper.getWritableDatabase();

                        ContentValues values = new ContentValues();
                        values.put(TravelJournalContract.Sight.TRAVEL_ID, newTravelId);
                        values.put(TravelJournalContract.Sight.ORDER, lastSightOrder + 1);
                        String whereClause = " (" + TravelJournalContract.Sight._ID + " = ? ) ";
                        db.update(TravelJournalContract.Sight.TABLE_NAME, values, whereClause, new String[]{
                                String.valueOf(toSight.getId())});

                        result = true;
                    }
                }
            }
        }

        db.close();

        return result;
    }

    public static boolean isTravelNameExists(Context context, LatLng from, LatLng to) {
        boolean result = false;

        TravelJournalDbHelper mDbHelper = new TravelJournalDbHelper(context);
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        String[] projection = new String[]{TravelJournalContract.Sight.TRAVEL_ID};
        String where = " (" + TravelJournalContract.Sight.LATITUDE + " = ? AND " +
                TravelJournalContract.Sight.LONGITUDE + " = ?) OR " +
                "(" + TravelJournalContract.Sight.LATITUDE + " = ? AND " +
                TravelJournalContract.Sight.LONGITUDE + " = ?)";
        Cursor cursor = db.query(TravelJournalContract.Sight.TABLE_NAME, projection, where,
                new String[]{
                        String.valueOf(from.latitude),
                        String.valueOf(from.longitude),
                        String.valueOf(to.latitude),
                        String.valueOf(to.longitude),
                }, null, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    boolean isTravelIdNull = cursor.isNull(cursor.getColumnIndex(TravelJournalContract.Sight.TRAVEL_ID));
                    if (!isTravelIdNull) {
                        result = true;
                    }
                }
                while (cursor.moveToNext());
            }
        }

        return result;
    }

    public static ArrayList<DOTravel> getAllTravels(Context context) {
        ArrayList<DOTravel> travels = new ArrayList<>();
        TravelJournalDbHelper mDbHelper = new TravelJournalDbHelper(context);
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(TravelJournalContract.Travel.SQL_GET_ALL_TRAVELS, new String[]{});
        Log.d(MapsActivity.LOG_TAG, "\tid\t\ttrav_name\t\tsightId\t\t\tsightIcon\t\t\tsightLat\t\t\t" +
                "sightLon\t\t\tsightOrder\t\t\tphotoId\t\t\tphotoUri");

        if (cursor != null) {
            long prevTravelId = -1;
            long prevSightId = -1;
            DOTravel travel = null;

            if (cursor.moveToFirst()) {
                do {
                    Long id = cursor.getLong(cursor.getColumnIndex(TravelJournalContract.Travel._ID));
                    String name = cursor.getString(cursor.getColumnIndex(TravelJournalContract.Travel.NAME));
                    Long sightId = cursor.getLong(cursor.getColumnIndex(TravelJournalContract.Sight.TEMP_SIGHT_ID));
                    String sightIcon = cursor.getString(cursor.getColumnIndex(TravelJournalContract.Sight.ICON));
                    Double sightLat = cursor.getDouble(cursor.getColumnIndex(TravelJournalContract.Sight.LATITUDE));
                    Double sightLon = cursor.getDouble(cursor.getColumnIndex(TravelJournalContract.Sight.LONGITUDE));
                    Long sightOrder = cursor.getLong(cursor.getColumnIndex(TravelJournalContract.Sight.ORDER));
                    Long photoId = null;
                    if (!cursor.isNull(cursor.getColumnIndex(TravelJournalContract.Sight.TEMP_PHOTO_ID))) {
                        photoId = cursor.getLong(cursor.getColumnIndex(TravelJournalContract.Sight.TEMP_PHOTO_ID));
                    }

                    String photoUri = cursor.getString(cursor.getColumnIndex(TravelJournalContract.Photo.URI));

                    Log.d(MapsActivity.LOG_TAG, id + "\t\t" + name + "\t\t" + "\t\t" + sightId +
                            "\t\t" + sightIcon + "\t\t" + sightLat + "\t\t" + sightLon + "\t\t" +
                            sightOrder + "\t\t" + photoId + "\t\t" + photoUri);

                    if (prevTravelId == id) {// same travel
                        if (prevSightId == sightId) {// same sight
                            travel.addPhotoToTheLastSight(photoId, photoUri);
                        } else {// new sight
                            DOSight sight = new DOSight(sightId, sightIcon, sightLat, sightLon, sightIcon, id, sightOrder, null);
                            if (photoId != null) {
                                sight.addPhoto(photoId, photoUri);
                            }
                            travel.addSight(sight);

                            prevSightId = sightId;
                        }
                    }
                    else {
                        if (travel != null) {
                            travels.add(travel);
                        }

                        DOSight sight = new DOSight(sightId, sightIcon, sightLat, sightLon, sightIcon, id, sightOrder, null);
                        if (photoId != null) {
                            sight.addPhoto(photoId, photoUri);
                        }
                        travel = new DOTravel(id, name, null);
                        travel.addSight(sight);

                        prevSightId = sightId;
                        prevTravelId = id;
                        //добавляем фото в список фоток
                        //если фото закончились - добавляем список фоток в сайт
                        // и добавляем сайт в список сайтов
                        //если сайты закончились - добавляем сайты в тревел
                        //если тревелы закончились - добавляем тревел в тревелы
                    }
                }
                while (cursor.moveToNext());
            }

            cursor.close();
        }

        db.close();
        return travels;
    }

    public static void printTravels(Context context) {
        TravelJournalDbHelper mDbHelper = new TravelJournalDbHelper(context);

        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        String[] projection = new String[]{
                TravelJournalContract.Travel._ID,
                TravelJournalContract.Travel.NAME
        };
        Cursor cursor = db.query(TravelJournalContract.Travel.TABLE_NAME, projection, null, null, null, null, null);
        Log.d(MapsActivity.LOG_TAG, "\tid\t\tname\t\t");

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    Long id = cursor.getLong(cursor.getColumnIndex(TravelJournalContract.Travel._ID));
                    String name = cursor.getString(cursor.getColumnIndex(TravelJournalContract.Travel.NAME));

                    Log.d(MapsActivity.LOG_TAG, id + "\t\t" + name + "\t\t");
                }
                while (cursor.moveToNext());
            }

            cursor.close();
        }

        db.close();
        Log.d(MapsActivity.LOG_TAG, "--------------- e n d --------------");
    }

    public static void printSights(Context context) {
        TravelJournalDbHelper mDbHelper = new TravelJournalDbHelper(context);

        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        String[] projection = new String[]{
                TravelJournalContract.Sight._ID,
                TravelJournalContract.Sight.DESCRIPTION,
                TravelJournalContract.Sight.ICON,
                TravelJournalContract.Sight.LATITUDE,
                TravelJournalContract.Sight.LONGITUDE,
                TravelJournalContract.Sight.TRAVEL_ID,
                TravelJournalContract.Sight.ORDER
        };
        Cursor cursor = db.query(TravelJournalContract.Sight.TABLE_NAME, projection, null, null, null, null, null);
        Log.d(MapsActivity.LOG_TAG, "\tid\t\tdesc\t\ticon\t\tlat\t\tlon\t\ttravId\t\torder\t");

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    Long id = cursor.getLong(cursor.getColumnIndex(TravelJournalContract.Sight._ID));
                    String desc = cursor.getString(cursor.getColumnIndex(TravelJournalContract.Sight.DESCRIPTION));
                    String icon = cursor.getString(cursor.getColumnIndex(TravelJournalContract.Sight.ICON));
                    Double lat = cursor.getDouble(cursor.getColumnIndex(TravelJournalContract.Sight.LATITUDE));
                    Double lon = cursor.getDouble(cursor.getColumnIndex(TravelJournalContract.Sight.LONGITUDE));
                    Long travel_id = cursor.getLong(cursor.getColumnIndex(TravelJournalContract.Sight.TRAVEL_ID));
                    Long order = cursor.getLong(cursor.getColumnIndex(TravelJournalContract.Sight.ORDER));

                    Log.d(MapsActivity.LOG_TAG, id + "\t\t" + desc + "\t\t" + icon + "\t\t" + lat +
                            "\t\t" + lon + "\t\t" + travel_id + "\t\t" + order + "\t\t");
                }
                while (cursor.moveToNext());
            }

            cursor.close();
        }

        db.close();
        Log.d(MapsActivity.LOG_TAG, "--------------- e n d --------------");
    }

    public static void printPhotos(Context context) {
        TravelJournalDbHelper mDbHelper = new TravelJournalDbHelper(context);
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        String[] projection = new String[]{
                TravelJournalContract.Photo._ID,
                TravelJournalContract.Photo.URI
        };
        Cursor cursor = db.query(TravelJournalContract.Photo.TABLE_NAME, projection, null, null, null, null, null);
        Log.d(MapsActivity.LOG_TAG, "\tid\t\tdesc\t\ticon\t\tlat\t\tlon\t\ttravId\t\torder\t");

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    Long id = cursor.getLong(cursor.getColumnIndex(TravelJournalContract.Photo._ID));
                    String uri = cursor.getString(cursor.getColumnIndex(TravelJournalContract.Photo.URI));

                    Log.d(MapsActivity.LOG_TAG, id + "\t\t" + uri + "\t\t");
                }
                while (cursor.moveToNext());
            }

            cursor.close();
        }

        db.close();
        Log.d(MapsActivity.LOG_TAG, "--------------- e n d --------------");
    }
}
