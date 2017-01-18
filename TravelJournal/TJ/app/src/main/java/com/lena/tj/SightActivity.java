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
import com.lena.tj.db.DbOperations;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Map;

public class SightActivity extends AppCompatActivity {
    private static final int REQUEST_PERMISSION = 2;
    private DOSight sight;
    private String mode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sight);

        mode = getIntent().getStringExtra(getString(R.string.sight_mode));

        final String json = getIntent().getStringExtra(getString(R.string.sight));
        if (json != null) {
            updateSightView(new Gson().fromJson(json, DOSight.class));
        }

        Button deleteButton = (Button) findViewById(R.id.sight_button_delete);
        if (mode.equals(getString(R.string.travel))) {
            deleteButton.setEnabled(false);
        } else {
            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    deleteSight();
                }
            });
        }

        Button showOnMapButton = (Button) findViewById(R.id.sight_button_on_map);
        showOnMapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSightOnMap();
            }
        });
    }

    private void showSightOnMap() {
        Gson gson = new Gson();
        Intent intent = new Intent(this, MapsActivity.class);
        intent.putExtra(getString(R.string.sight_data), gson.toJson(sight));
        startActivity(intent);
    }

    private void updateSightView(DOSight newSight) {
        sight = newSight;

        TextView descTextView = (TextView) findViewById(R.id.sight_desc);
        descTextView.setText(newSight.getDescription());

        TextView latitudeTextView = (TextView) findViewById(R.id.sight_latitude);
        latitudeTextView.setText(Double.toString(newSight.getLatitude()));

        TextView longitudeTextView = (TextView) findViewById(R.id.sight_longitude);
        longitudeTextView.setText(Double.toString(newSight.getLongitude()));

        TextView orderTextView = (TextView) findViewById(R.id.sight_order);
        TextView travelNameTextView = (TextView) findViewById(R.id.sight_travel_name_textview);

        if (mode.equals(getString(R.string.travel))) {
            orderTextView.setText(Double.toString(newSight.getOrder()));
           // travelNameTextView.setText(Double.toString(newSight.getTravelId()));
        }
        else {
            TextView orderHeaderTextView = (TextView) findViewById(R.id.sight_order_textview);
            TextView travelNameHeaderTextView = (TextView) findViewById(R.id.sight_travel_textview);
            orderTextView.setVisibility(View.INVISIBLE);
            orderHeaderTextView.setVisibility(View.INVISIBLE);
            travelNameTextView.setVisibility(View.INVISIBLE);
            travelNameHeaderTextView.setVisibility(View.INVISIBLE);
        }

        addImageViews();
    }

    private void addImageViews() {
        int permissionCheck = ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE);

        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_PERMISSION);
        } else {
            showImages();
        }
    }

    private void showImages() {
        if (sight.getPhotos() != null) {
            for (Map.Entry<Long, String> photoObj : sight.getPhotos().entrySet()) {
                final Uri path = Uri.parse(photoObj.getValue());
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
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted
                    showImages();
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
        }
    }

    private void deleteSight() {
        DbOperations.deleteSight(this, sight);

        Intent intent = new Intent(this, MapsActivity.class);
        startActivity(intent);
    }
}
