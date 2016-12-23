package com.lena.tj;

import android.content.Intent;
import android.content.pm.PackageManager;
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
import com.lena.tj.dataobjects.DOSight;
import com.lena.tj.db.TravelJournalContract;
import com.lena.tj.db.TravelJournalDbHelper;

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
                    deleteSight(sight);
                }
            });
        }
    }

    private void updateSightView(DOSight newSight) {
        TextView descTextView = (TextView) findViewById(R.id.sight_desc);
        descTextView.setText(newSight.getDescription());

        TextView latitudeTextView = (TextView) findViewById(R.id.sight_latitude);
        latitudeTextView.setText(Double.toString(newSight.getLatitude()));

        TextView longitudeTextView = (TextView) findViewById(R.id.sight_longitude);
        longitudeTextView.setText(Double.toString(newSight.getLongitude()));

        TextView orderTextView = (TextView) findViewById(R.id.sight_order);
        orderTextView.setText(Double.toString(newSight.getOrder()));

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

    private void deleteSight(DOSight mSight) {
        TravelJournalDbHelper dbHelper = new TravelJournalDbHelper(this);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        for (Map.Entry<Long, String> photo : mSight.getPhotos().entrySet()) {
            int delCount = db.delete(TravelJournalContract.Photo.TABLE_NAME,
                    TravelJournalContract.Photo._ID + " = " + photo.getKey(), null);
        }

        int delCount = db.delete(TravelJournalContract.Sight.TABLE_NAME,
                TravelJournalContract.Sight._ID + " = " + mSight.getId(), null);

        Intent intent = new Intent(this, SightsListActivity.class);
        startActivity(intent);
    }
}
