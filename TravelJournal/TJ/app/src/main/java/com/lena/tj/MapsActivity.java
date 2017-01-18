package com.lena.tj;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Geocoder;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatDrawableManager;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlaceLikelihoodBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.Gson;
import com.lena.tj.dataobjects.DOSight;
import com.lena.tj.dataobjects.DOTravel;
import com.lena.tj.db.DbOperations;
import com.lena.tj.net.PostTask;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, NavigationView.OnNavigationItemSelectedListener,
        GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks, PostTask.MyAsyncResponse {
    public static final String LOG_TAG = "~Mimi~";
    private static final int REQUEST_NEW_ICON = 1;
    private static final int REQUEST_PERMISSION = 2;
    private static final int PLACE_PICKER_REQUEST = 3;
    private static final int REQUEST_NEW_SIGHT_BY_ADDRESS = 4;
    private static final float ANCHOR_X = 0.52f;
    private static final float ANCHOR_Y = 0.8f;

    private SupportMapFragment mapFragment;
    private GoogleMap map;

    private String sightDesc;
    private LatLng target;
    private int iconId;
    private String iconCode;
    private GoogleApiClient mGoogleApiClient;

    private LatLng from;
    private LatLng to;
    private String travelName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);

        if (drawer != null) {
            drawer.addDrawerListener(toggle);
            toggle.syncState();
            drawer.setScrimColor(Color.argb(200, 0, 0, 0));
        }

        NavigationView navigationView = (NavigationView) findViewById(R.id.navigation_view);
        if (navigationView != null) {
            navigationView.setNavigationItemSelectedListener(this);
        }


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this); // -> OnMapReady
        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .enableAutoManage(this, 0, this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mGoogleApiClient != null)
            mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }

    private void displayPlacePickerAndAddSight() {
        if (mGoogleApiClient == null || !mGoogleApiClient.isConnected())
            return;

        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
        try {
            startActivityForResult(builder.build(this), PLACE_PICKER_REQUEST);
        } catch (GooglePlayServicesRepairableException e) {
            Log.d("PlacesAPI Demo", "GooglePlayServicesRepairableException thrown");
        } catch (GooglePlayServicesNotAvailableException e) {
            Log.d("PlacesAPI Demo", "GooglePlayServicesNotAvailableException thrown");
        }
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION,
                            android.Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_PERMISSION);
            return;
        }
        map.setMyLocationEnabled(true);
        CameraPosition cameraPosition = CameraPosition.builder()
                .target(new LatLng(41.889, -87.622))
                .zoom(13)
                .bearing(90)
                .build();
        // Animate the change in camera view over 2 seconds
        map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition),
                5000, null);

        map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                new LatLng(53.0, 50.0), 2));

        String jsonTravel = getIntent().getStringExtra(getString(R.string.travel_data));
        String jsonSight = getIntent().getStringExtra(getString(R.string.sight_data));
        if (jsonTravel != null) {
            map.clear();
            drawTravel(new Gson().fromJson(jsonTravel, DOTravel.class));
        } else if (jsonSight != null) {
            map.clear();
            drawSight(new Gson().fromJson(jsonSight, DOSight.class));
        } else {
            openSights();
            openTravels();
        }
    }

    private void openSights() {
        ArrayList<DOSight> sights = DbOperations.getAllSeparateSights(this);

        for (DOSight sight : sights) {
            int icon = this.getResources().getIdentifier(sight.getIcon(), "drawable", this.getPackageName());
            map.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromBitmap(getBitmapFromVectorDrawable(MapsActivity.this, icon)))
                    .anchor(ANCHOR_X, ANCHOR_Y) // Anchors the marker on the bottom left
                    .position(new LatLng(sight.getLatitude(), sight.getLongitude())))
                    .setTitle(sight.getDescription());
        }
    }

    private void openTravels() {
        //TODO: In Service or Loader
        ArrayList<DOTravel> travels = DbOperations.getAllTravels(this);

        for (DOTravel travel : travels) {
            drawTravel(travel);
        }
    }

    private void drawSight(DOSight sight) {
        map.clear();
        int icon = this.getResources().getIdentifier(sight.getIcon(), "drawable", this.getPackageName());
        map.addMarker(new MarkerOptions()
                .icon(BitmapDescriptorFactory.fromBitmap(getBitmapFromVectorDrawable(MapsActivity.this, icon)))
                .anchor(ANCHOR_X, ANCHOR_Y) // Anchors the marker on the bottom left
                .position(new LatLng(sight.getLatitude(), sight.getLongitude())))
                .setTitle(sight.getDescription());
    }

    private void drawTravel(DOTravel travel) {
        Double prevLat = null;
        Double prevLon = null;

        for (DOSight sight : travel.getSights()) {
            int icon = this.getResources().getIdentifier(sight.getIcon(), "drawable", this.getPackageName());
            map.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromBitmap(getBitmapFromVectorDrawable(MapsActivity.this, icon)))
                    .anchor(ANCHOR_X, ANCHOR_Y) // Anchors the marker on the bottom left
                    .position(new LatLng(sight.getLatitude(), sight.getLongitude())))
                    .setTitle(sight.getDescription());
            if (prevLat != null) {
                drawLine(new LatLng(prevLat, prevLon), new LatLng(sight.getLatitude(), sight.getLongitude()), travel.getColor());
                prevLat = sight.getLatitude();
                prevLon = sight.getLongitude();
            } else {
                prevLat = sight.getLatitude();
                prevLon = sight.getLongitude();
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

                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer != null && drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_select_travels:
                DbOperations.printTravels(this);
                break;
            case R.id.action_select_sights:
                DbOperations.printSights(this);
                break;
            case R.id.action_select_photos:
                DbOperations.printPhotos(this);
                break;
            case R.id.action_select_travels_join:
                DbOperations.getAllTravels(this);

                //DbOperations.doSmth(this);
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.maps_travel_creation_layout);
        relativeLayout.setVisibility(View.INVISIBLE);

        if (id == R.id.create_travel) {
            createTravel();
        }
        else if (id == R.id.create_sight_by_address) {
            createSightByAddress();
        }
        else if (id == R.id.add_sight) {
            displayPlacePickerAndAddSight();
        }
        else if (id == R.id.add_sight_easy) {
            addSightEasy();
        }
        else if (id == R.id.add_sight_current) {
            guessCurrentPlace();
        }
        else if (id == R.id.travels_list) {
            startActivity(new Intent(this, TravelsListActivity.class));
        }
        else if (id == R.id.sights_list) {
            startActivity(new Intent(this, SightsListActivity.class));
        }
        else if (id == R.id.separated_sights_on_map) {
            map.clear();
            openSights();
        }
        else if (id == R.id.the_nearest_travel_to_me) {
            getTheNearestTravelToMe();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        if (drawer != null) {
            drawer.closeDrawer(GravityCompat.START);
        }
        return true;
    }

    private void getTheNearestTravelToMe() {
        final LatLng[] currentLocation = new LatLng[1];
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_PERMISSION);
            return;
        }
        PendingResult<PlaceLikelihoodBuffer> result = Places.PlaceDetectionApi.getCurrentPlace(mGoogleApiClient, null);
        result.setResultCallback(new ResultCallback<PlaceLikelihoodBuffer>() {
            @Override
            public void onResult(PlaceLikelihoodBuffer likelyPlaces) {
                PlaceLikelihood placeLikelihood = likelyPlaces.get(0);
                currentLocation[0] = placeLikelihood.getPlace().getLatLng();
                DOTravel travel = null;
                DbOperations.getTheNearestTravel1(MapsActivity.this, currentLocation[0]);
                map.clear();
                drawTravel(travel);
                Toast.makeText(MapsActivity.this, getString(R.string.maps_nearest_travel), Toast.LENGTH_LONG).show();

                //to avoid memory leaks
                likelyPlaces.release();
            }
        });
    }

    private void addSightEasy() {
        map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                Intent intent = new Intent(getApplicationContext(), IconChooserActivity.class);
                intent.putExtra(getString(R.string.sight_point), latLng);
                startActivityForResult(intent, REQUEST_NEW_ICON);
                map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                    @Override
                    public void onMapClick(LatLng latLng) {
                        // do nothing
                    }
                });
            }
        });
    }

    private void createTravel() {
        final Button fromButton = (Button) findViewById(R.id.maps_choose_from_button);
        final Button toButton = (Button) findViewById(R.id.maps_choose_to_button);
        final Button createTravelButton = (Button) findViewById(R.id.maps_create_travel_button);
        final Button clearTravelButton = (Button) findViewById(R.id.maps_clear_button);

        fromButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fromButton.setEnabled(false);
                toButton.setEnabled(false);
                createTravelButton.setEnabled(false);
                clearTravelButton.setEnabled(false);

                map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(Marker marker) {
                        from = marker.getPosition();

                        Geocoder geocoder;
                        List<android.location.Address> addresses = null;
                        geocoder = new Geocoder(MapsActivity.this, Locale.getDefault());

                        try {
                            addresses = geocoder.getFromLocation(from.latitude, from.longitude, 1);
                            // Here 1 represent max location result to returned, by documents it recommended 1 to 5
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        String address = addresses.get(0).getAddressLine(0);
                        String city = addresses.get(0).getLocality();
                        String state = addresses.get(0).getAdminArea();
                        String country = addresses.get(0).getCountryName();
                        TextView fromTextView = (TextView) findViewById(R.id.maps_from_textview);
                        fromTextView.setText(String.format(getString(R.string.maps_address_template),
                                country, state, city, address));
                        fromButton.setEnabled(true);
                        toButton.setEnabled(true);
                        createTravelButton.setEnabled(true);
                        clearTravelButton.setEnabled(true);
                        return false;
                    }
                });

                Toast.makeText(MapsActivity.this, getString(R.string.maps_choose_from_marker),
                        Toast.LENGTH_LONG).show();
            }
        });

        toButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fromButton.setEnabled(false);
                toButton.setEnabled(false);
                createTravelButton.setEnabled(false);
                clearTravelButton.setEnabled(false);

                map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(Marker marker) {
                        to = marker.getPosition();

                        Geocoder geocoder;
                        List<android.location.Address> addresses = null;
                        geocoder = new Geocoder(MapsActivity.this, Locale.getDefault());

                        try {
                            addresses = geocoder.getFromLocation(to.latitude, to.longitude, 1);
                            // Here 1 represent max location result to returned, by documents it recommended 1 to 5
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        String address = addresses.get(0).getAddressLine(0);
                        String city = addresses.get(0).getLocality();
                        String state = addresses.get(0).getAdminArea();
                        String country = addresses.get(0).getCountryName();
                        TextView toTextView = (TextView) findViewById(R.id.maps_to_textview);
                        toTextView.setText(String.format(getString(R.string.maps_address_template),
                                country, state, city, address));
                        fromButton.setEnabled(true);
                        toButton.setEnabled(true);
                        createTravelButton.setEnabled(true);
                        clearTravelButton.setEnabled(true);
                        return false;
                    }
                });

                Toast.makeText(MapsActivity.this, getString(R.string.maps_choose_to_marker),
                        Toast.LENGTH_LONG).show();
            }
        });

        createTravelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (from != null && to != null && !from.equals(to)) {
                    askTravelNameAndInsert();
                }
                else {
                    Toast.makeText(MapsActivity.this, getString(R.string.travel_wrong_points), Toast.LENGTH_LONG).show();
                }
            }
        });

        clearTravelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView fromTV = (TextView) findViewById(R.id.maps_from_textview);
                fromTV.setText("");
                from = null;
                TextView toTV = (TextView) findViewById(R.id.maps_to_textview);
                toTV.setText("");
                to = null;
            }
        });

        RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.maps_travel_creation_layout);
        relativeLayout.setVisibility(View.VISIBLE);


    }

    private void drawLine(LatLng from, LatLng to,  int color) {
        PolylineOptions polylineOptions = new PolylineOptions();
        polylineOptions.add(from, to);
        int lineColor;
        if (color == -1) {
            Random rnd = new Random();
            lineColor = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
        } else {
            lineColor = color;
        }
        polylineOptions.color(lineColor);
        map.addPolyline(polylineOptions);

        RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.maps_travel_creation_layout);
        relativeLayout.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PLACE_PICKER_REQUEST && resultCode == RESULT_OK) {
            Place place = PlacePicker.getPlace(this, data);
            String toastMsg = String.format("Place: %s", place.getName());
            Toast.makeText(this, toastMsg, Toast.LENGTH_LONG).show();

            Intent intent = new Intent(getApplicationContext(), IconChooserActivity.class);
            intent.putExtra(getString(R.string.sight_point), place.getLatLng());
            startActivityForResult(intent, REQUEST_NEW_ICON);
        }
        else if (requestCode == REQUEST_NEW_ICON && resultCode == Activity.RESULT_OK) {
            iconCode = data.getStringExtra(IconChooserActivity.RESULT_ICON_ID);
            iconId = this.getResources().getIdentifier(iconCode, "drawable", this.getPackageName());
            target = data.getExtras().getParcelable(getString(R.string.sight_point));
            askSightNameAndInsert();
        }
        else if (requestCode == REQUEST_NEW_SIGHT_BY_ADDRESS && resultCode == Activity.RESULT_OK) {
            LatLng placeLatLng = data.getParcelableExtra(getString(R.string.sight_point));

            Intent intent = new Intent(getApplicationContext(), IconChooserActivity.class);
            intent.putExtra(getString(R.string.sight_point), placeLatLng);
            startActivityForResult(intent, REQUEST_NEW_ICON);
        }
    }

    private void createSightByAddress() {
        Intent intent = new Intent(getApplicationContext(), SearchActivity.class);
        startActivityForResult(intent, REQUEST_NEW_SIGHT_BY_ADDRESS);
    }

    private void displayPlace(Place place) {
        if (place == null)
            return;

        String content = "";
        if (!TextUtils.isEmpty(place.getName())) {
            content += "Name: " + place.getName() + "\n";
        }
        if (!TextUtils.isEmpty(place.getAddress())) {
            content += "Address: " + place.getAddress() + "\n";
        }
        if (!TextUtils.isEmpty(place.getPhoneNumber())) {
            content += "Phone: " + place.getPhoneNumber();
        }

        Toast.makeText(this, content, Toast.LENGTH_LONG).show();  //mTextView.setText( content );
    }

    private void guessCurrentPlace() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_PERMISSION);
            return;
        }
        PendingResult<PlaceLikelihoodBuffer> result = Places.PlaceDetectionApi.getCurrentPlace(mGoogleApiClient, null);
        result.setResultCallback(new ResultCallback<PlaceLikelihoodBuffer>() {
            @Override
            public void onResult(PlaceLikelihoodBuffer likelyPlaces) {
                PlaceLikelihood placeLikelihood = likelyPlaces.get(0);
                String content = "";
                if (placeLikelihood != null && placeLikelihood.getPlace() != null && !TextUtils.isEmpty(placeLikelihood.getPlace().getName()))
                    content = "Most likely place: " + placeLikelihood.getPlace().getName() + "\n";
                if (placeLikelihood != null)
                    content += "Percent change of being there: " + (int) (placeLikelihood.getLikelihood() * 100) + "%";
                Log.d(LOG_TAG, "guessCurrentPlace content: " + content);

                Intent intent = new Intent(getApplicationContext(), IconChooserActivity.class);
                intent.putExtra(getString(R.string.sight_point), placeLikelihood.getPlace().getLatLng());
                startActivityForResult(intent, REQUEST_NEW_ICON);

                //to avoid memory leaks
                likelyPlaces.release();
            }
        });
    }

    private void askTravelNameAndInsert(){
        if (!DbOperations.isTravelNameExists(this, from, to)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getString(R.string.travel_set_name));
            final EditText input = new EditText(this);
            input.setInputType(InputType.TYPE_CLASS_TEXT);
            builder.setView(input);

            builder.setPositiveButton(getString(R.string.dialog_ok), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    travelName = input.getText().toString();
                    int color = DbOperations.createTravel(MapsActivity.this, from, to, travelName);
                    if (color != -1)
                        drawLine(from, to, color);
                    else {
                        Toast.makeText(MapsActivity.this, "Can t create travel..", Toast.LENGTH_LONG).show();
                    }
                }
            });
            builder.setNegativeButton(getString(R.string.dialog_cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                        @Override
                        public void onMapClick(LatLng latLng) {
                            //nothing, unregister
                        }
                    });
                    dialog.cancel();
                }
            });

            builder.show();
        } else {
            int color = DbOperations.createTravel(MapsActivity.this, from, to, null);
            if (color != -1){
                drawLine(from, to, color);
            }
            else {
                Toast.makeText(MapsActivity.this, "Can t modify travel..", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void askSightNameAndInsert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.sight_set_description));
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);
        builder.setPositiveButton(getString(R.string.dialog_ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                sightDesc = input.getText().toString();

                map.moveCamera(CameraUpdateFactory.newLatLng(target));
                map.addMarker(new MarkerOptions()
                        .title(sightDesc)
                        .icon(BitmapDescriptorFactory.fromBitmap(getBitmapFromVectorDrawable(MapsActivity.this, iconId)))
                        .anchor(ANCHOR_X, ANCHOR_Y) // Anchors the marker on the bottom left
                        .position(target));
                map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                    @Override
                    public void onMapClick(LatLng latLng) {
                        //nothing, unregister
                    }
                });

                DbOperations.insertNewSight(MapsActivity.this, sightDesc, iconCode, target.latitude, target.longitude);
            }
        });
        builder.setNegativeButton(getString(R.string.dialog_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                    @Override
                    public void onMapClick(LatLng latLng) {
                        //nothing, unregister
                    }
                });

                dialog.cancel();
            }
        });

        builder.show();
    }


    private void findPlaceById(String id) {
        if (TextUtils.isEmpty(id) || mGoogleApiClient == null || !mGoogleApiClient.isConnected())
            return;

        Places.GeoDataApi.getPlaceById(mGoogleApiClient, id).setResultCallback(new ResultCallback<PlaceBuffer>() {
            @Override
            public void onResult(PlaceBuffer places) {
                if (places.getStatus().isSuccess()) {
                    Place place = places.get(0);
                    displayPlace(place);
                    // mPredictTextView.setText( "" );
                    //mAdapter.clear();
                }

                //Release the PlaceBuffer to prevent a memory leak
                places.release();
            }
        });
    }

    public static Bitmap getBitmapFromVectorDrawable(Context context, int drawableId) {
        Drawable drawable = AppCompatDrawableManager.get().getDrawable(context, drawableId);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            drawable = (DrawableCompat.wrap(drawable)).mutate();
        }

        Bitmap bitmap = Bitmap.createBitmap(drawable.getMinimumWidth(),
                drawable.getMinimumHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this, "onConnectionFailed " + connectionResult.toString(), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void processFinish(String serverJson) {
        //PlacePicker.
    }
}
