package com.lena.tj;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lena.tj.dataobjects.DOSight;
import com.lena.tj.db.TravelJournalContract;
import com.lena.tj.db.TravelJournalDbHelper;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;

public class SightsListActivity extends AppCompatActivity {
    private ArrayList<String> sightsIds;
    private String mode;
    private ArrayAdapter<DOSight> arrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sights_list);

        String json = getIntent().getStringExtra(getString(R.string.travel_sights_ids));
        if (json != null) {
            Type type = new TypeToken<ArrayList<String>>() {
            }.getType();
            sightsIds = new Gson().fromJson(json, type);
            mode = getString(R.string.travel);
            TextView headerTextView = (TextView) findViewById(R.id.sights_header);
            headerTextView.setText(getIntent().getStringExtra(getString(R.string.travel_name)));
        } else {
            mode = getString(R.string.sight);
        }

        showSights(prepareSightsToShow());
    }

    private void showSights(final ArrayList<DOSight> sights) {
        arrayAdapter = new ArrayAdapter<DOSight>(this,
                android.R.layout.simple_list_item_1, sights) {
            @NonNull
            @Override
            public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView text1 = (TextView) view.findViewById(android.R.id.text1);

                text1.setText(sights.get(position).getDescription());
                return view;
            }
        };

        ListView allSightsListView = (ListView) findViewById(R.id.all_sights_listview);
        allSightsListView.setAdapter(arrayAdapter);
        allSightsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View itemClicked, int position, long id) {
                DOSight sight = arrayAdapter.getItem(position);

                Context context = getApplicationContext();
                Intent intent = new Intent(context, SightActivity.class);
                if (mode.equals(getString(R.string.travel))) {
                    intent.putExtra(getString(R.string.sight_mode), getString(R.string.travel));
                } else {
                    intent.putExtra(getString(R.string.sight_mode), getString(R.string.sight));
                }
                intent.putExtra(context.getString(R.string.sight), new Gson().toJson(sight));
                startActivity(intent);
            }
        });
    }

    private ArrayList<DOSight> prepareSightsToShow() {
        TravelJournalDbHelper dbHelper = new TravelJournalDbHelper(this);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = TravelJournalContract.Sight.SQL_LEFT_JOIN_PHOTO;

        if (mode.equals(R.string.travel) && sightsIds.size() > 0) {
            StringBuilder where = new StringBuilder();
            where.append(" WHERE ");
            int i = 0;
            for (String sightId : sightsIds) {
                where.append(TravelJournalContract.Sight.TEMP_SIGHT_ID + " = " + sightId + " ");
                if (i < sightsIds.size() - 1){
                    where.append(" OR ");
                }
                i++;
            }

            query = query + where.toString();
        }

        Cursor cursor = db.rawQuery(query, new String[]{});
        ArrayList<DOSight> sights = null;

        if (cursor != null) {
            long prevId = -1;
            sights = new ArrayList<>();

            if (cursor.moveToFirst()) {
                do {
                    long id = cursor.getLong(cursor.getColumnIndex(TravelJournalContract.Sight.TEMP_SIGHT_ID));
                    String desc = cursor.getString(cursor.getColumnIndex(TravelJournalContract.Sight.DESCRIPTION));
                    double latitude = cursor.getDouble(cursor.getColumnIndex(TravelJournalContract.Sight.LATITUDE));
                    double longitude = cursor.getDouble(cursor.getColumnIndex(TravelJournalContract.Sight.LONGITUDE));
                    String icon = cursor.getString(cursor.getColumnIndex(TravelJournalContract.Sight.ICON));
                   // long travel_id = cursor.getLong(cursor.getColumnIndex(TravelJournalContract.Sight.TRAVEL_ID));
                    long order = cursor.getLong(cursor.getColumnIndex(TravelJournalContract.Sight.ORDER));
                    String photoUri = cursor.getString(cursor.getColumnIndex(TravelJournalContract.Photo.URI));
                    long photoId = cursor.getLong(cursor.getColumnIndex(TravelJournalContract.Sight.TEMP_PHOTO_ID));

                    if (prevId == id) {
                        int index = sights.size() - 1;
                        DOSight sight = sights.get(index);
                        HashMap<Long, String> photos = sight.getPhotos();
                        photos.put(photoId, photoUri);
                        sight.setPhotos(photos);
                        sights.set(index, sight);
                    } else {
                        HashMap<Long, String> photos = null;

                        if (photoUri != null) {
                            photos = new HashMap<>();
                            photos.put(photoId, photoUri);
                        }

                        DOSight sight = new DOSight(id, desc, latitude, longitude, icon, order, photos);
                        sights.add(sight);
                        prevId = id;
                    }

                    String str = "";
                    for (String cn : cursor.getColumnNames()) {
                        str = str.concat(cn + " = " + cursor.getString(cursor.getColumnIndex(cn)) + "; ");
                    }
                    Log.d("Min", str);
                }
                while (cursor.moveToNext());
            }
        }

        return sights;
    }
}
