package com.lena.tj;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.lena.tj.dataobjects.DOTravel;
import com.lena.tj.db.TravelJournalContract;
import com.lena.tj.db.TravelJournalDbHelper;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class TravelActivity extends AppCompatActivity {
    private static final int REQUEST_PERMISSION = 2;
    private DOTravel travel;
    ArrayList<Long> photosIds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_travel);

        final String json = getIntent().getStringExtra(getString(R.string.travel));
        if (json != null) {
            travel = getTravelFromJSON(json);
            updateTravelView(travel);
        }

        Button showSightsButton = (Button) findViewById(R.id.travel_button_show_sights);
        showSightsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSights();
            }
        });

        Button showOnTheMapButton = (Button) findViewById(R.id.travel_button_show_on_the_map);
        showOnTheMapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showOnTheMap();
            }
        });

        Button deleteButton = (Button) findViewById(R.id.sight_button_delete);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteTravel(travel);
            }
        });
    }

    private void showSights() {
        Gson gson = new Gson();
        Intent intent = new Intent(this, SightsListActivity.class);
        intent.putExtra(getString(R.string.travel_sights_ids), gson.toJson(loadSightsIds(this)));
        startActivity(intent);
    }

    private void showOnTheMap() {
        Gson gson = new Gson();
        Intent intent = new Intent(this, MapsActivity.class);
        intent.putExtra(getString(R.string.travel_sights_ids), gson.toJson(loadSightsIds(this)));
        startActivity(intent);
    }

    private DOTravel getTravelFromJSON(String json) {
        Gson gson = new Gson();
        return gson.fromJson(json, DOTravel.class);
    }

    private void updateTravelView(DOTravel newTravel) {
        TextView nameTextView = (TextView) findViewById(R.id.travel_name);
        nameTextView.setText(newTravel.getName());

        addImageViews();
    }

    private void addImageViews() {
        int permissionCheck = ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE);

        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_PERMISSION);
        } else {
            showImages(loadPhotos(this));
        }
    }

    private HashMap<Long, String> loadPhotos(Context context) {
        HashMap<Long, String> photos = new HashMap<>();

        TravelJournalDbHelper dbHelper = new TravelJournalDbHelper(context);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String[] projection = {
                TravelJournalContract.Photo._ID,
                TravelJournalContract.Photo.URI
        };

        Cursor cursor = db.query(
                TravelJournalContract.Sight.TABLE_NAME,  // The table to query
                projection,                               // The columns to return
                null,                                     // The columns for the WHERE clause
                null,                                     // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                null                                      // The sort order
        );

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    long id = cursor.getLong(cursor.getColumnIndex(TravelJournalContract.Photo._ID));
                    String uri = cursor.getString(cursor.getColumnIndex(TravelJournalContract.Photo._ID));
                    photos.put(id, uri);
                }
                while (cursor.moveToNext());
            }
        }

        cursor.close();
        db.close();

        photosIds.addAll(photos.keySet());

        return photos;
    }

    private void showImages(HashMap<Long, String> travelPhotoPathes) {
        for (Map.Entry<Long, String> photoPath : travelPhotoPathes.entrySet()) {
            final Uri path = Uri.parse(photoPath.getValue());
            InputStream imageStream = null;
            try {
                imageStream = getContentResolver().openInputStream(path);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            Bitmap photo = BitmapFactory.decodeStream(imageStream);
            ImageView imageView = new ImageView(this);
            imageView.setLayoutParams(new android.view.ViewGroup.LayoutParams(200, 200));
            imageView.setImageBitmap(photo);
            imageView.setPadding(10, 10, 5, 0);
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_VIEW);
                    intent.setDataAndType(path, "image");
                    startActivity(intent);
                }
            });
            LinearLayout linearLayout = (LinearLayout) findViewById(R.id.show_imageviews_layout);
            linearLayout.addView(imageView);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted
                    showImages(loadPhotos(this));
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
        }
    }

    private ArrayList<Long> loadSightsIds(Context context) {
        ArrayList<Long> sightsIds = new ArrayList<>();

        TravelJournalDbHelper dbHelper = new TravelJournalDbHelper(context);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String[] projection = {
                TravelJournalContract.Sight._ID,
        };

        Cursor cursor = db.query(
                TravelJournalContract.Sight.TABLE_NAME,  // The table to query
                projection,                               // The columns to return
                null,                                     // The columns for the WHERE clause
                null,                                     // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                null                                      // The sort order
        );

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    long id = cursor.getLong(cursor.getColumnIndex(TravelJournalContract.Sight._ID));
                    sightsIds.add(id);
                }
                while (cursor.moveToNext());
            }
        }

        cursor.close();
        db.close();

        return sightsIds;
    }

    private void deleteTravel(DOTravel mTravel) {
        ArrayList<Long> sightsIds = loadSightsIds(this);

        TravelJournalDbHelper dbHelper = new TravelJournalDbHelper(this);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        for (Long photoId : photosIds) {
            int delCount = db.delete(TravelJournalContract.Photo.TABLE_NAME,
                    TravelJournalContract.Photo._ID + " = " + photoId, null);
        }

        for (Long sightId : sightsIds) {
            int delCount = db.delete(TravelJournalContract.Sight.TABLE_NAME,
                    TravelJournalContract.Photo._ID + " = " + sightId, null);
        }

        int delCount = db.delete(TravelJournalContract.Travel.TABLE_NAME,
                TravelJournalContract.Sight._ID + " = " + mTravel.getId(), null);

        db.close();

        Intent intent = new Intent(this, TravelsListActivity.class);
        startActivity(intent);
    }
}
