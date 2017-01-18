package com.lena.tj;

import android.content.Intent;
import android.content.pm.PackageManager;
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
import com.lena.tj.dataobjects.DOSight;
import com.lena.tj.dataobjects.DOTravel;
import com.lena.tj.db.DbOperations;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class TravelActivity extends AppCompatActivity {
    private static final int REQUEST_PERMISSION = 2;
    private DOTravel travel;

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

        Button deleteButton = (Button) findViewById(R.id.travel_button_delete);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteTravel();
            }
        });
    }

    private void showSights() {
        Gson gson = new Gson();
        Intent intent = new Intent(this, SightsListActivity.class);
        intent.putExtra(getString(R.string.travel_data), gson.toJson(getSights()));
        startActivity(intent);
    }

    private void showOnTheMap() {
        Gson gson = new Gson();
        Intent intent = new Intent(this, MapsActivity.class);
        intent.putExtra(getString(R.string.travel_data), gson.toJson(travel));
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
            showImages(getPhotos());
        }
    }

    private HashMap<Long, String> getPhotos() {
        HashMap<Long, String> photos = new HashMap<>();

        for (DOSight sight : travel.getSights()) {
            photos.putAll(sight.getPhotos());
        }

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
                    showImages(getPhotos());
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
        }
    }

    private ArrayList<DOSight> getSights() {
        ArrayList<DOSight> sights = new ArrayList<>();

        for (DOSight sight : travel.getSights()) {
            sights.add(sight);
        }

        return sights;
    }

    private void deleteTravel() {
        DbOperations.deleteTravel(this, travel);
        startActivity(new Intent(this, MapsActivity.class));
    }
}
