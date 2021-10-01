package com.cz3002.diseasesclinicalapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback {

    public static GoogleMap mMap;
    public static LatLng liveLocation;
    public static Marker curMarker;

    private LocationManager locationManager;
    private LocationListener locationListener;
    SupportMapFragment mapFragment;
    MarkerLogic generateMarker = new MarkerLogic(this);

    ClinicLogic clinicLogic = new ClinicLogic(this);
    SearchLogic searchLogic = new SearchLogic(this);

//    //live location permissions
//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//
//        if (requestCode == 1) {
//            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
//                    locationManager.requestLocationUpdates(locationManager.GPS_PROVIDER, 0, 0, locationListener);
//                }
//            }
//        }
//    }


    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);


        AutoCompleteTextView editText = findViewById(R.id.search);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, searchLogic.getClinicNames());
        editText.setAdapter(adapter);

        editText.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedItem = (String) parent.getItemAtPosition(position);
                searchLogic.findClinicSearch(selectedItem);
            }
        });


        mapFragment.getMapAsync(this);
    }


    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        LatLngBounds sgBounds = new LatLngBounds(
                new LatLng(1.2046, 103.5292), // SW bounds
                new LatLng(1.4585, 104.0490)  // NE bounds
        );


        /*// test all clinics
        ArrayList<JSONObject> all_clinic_data = clinicLogic.getClinics();
        for (JSONObject a : all_clinic_data) {
            try {
                String clinic_name = a.getString("name");
                *//*Double clinic_lat = a.getDouble("lati");
                Double clinic_long = a.getDouble("longi");
                LatLng clinicPos = new LatLng(clinic_lat,clinic_long);
                mMap.addMarker(new MarkerOptions().position(clinicPos).title("Clinic Location")
                        .icon(generateMarker.bitmapDescriptorFromVector(MapsActivity.this, R.drawable.ic_clinicmarker)));
*//*
                System.out.println(clinic_name);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        // test nearest 3 clinics
        try {
            ArrayList<JSONObject> nearest_clinic_data = clinicLogic.getNearestClinics(3);
            for (JSONObject b : nearest_clinic_data) {
                try {
                    String clinic_name = b.getString("name");
                   *//* Double clinic_lat = b.getDouble("lati");
                    Double clinic_long = b.getDouble("longi");
                    LatLng clinicPos = new LatLng(clinic_lat,clinic_long);
                    mMap.addMarker(new MarkerOptions().position(clinicPos).title("Clinic Location")
                            .icon(generateMarker.bitmapDescriptorFromVector(MapsActivity.this, R.drawable.ic_clinicmarker)));

*//*
                    System.out.println(clinic_name);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }*/


        //Live location
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {

                liveLocation = new LatLng(location.getLatitude(), location.getLongitude());
                if (curMarker != null) {
                    curMarker.remove();
                }
                curMarker = mMap.addMarker(new MarkerOptions().position(liveLocation).title("My Location")
                        .icon(generateMarker.bitmapDescriptorFromVector(MapActivity.this, R.drawable.ic_livemarker)));
            }
        };
        if (Build.VERSION.SDK_INT < 23) {
            locationManager.requestLocationUpdates(locationManager.GPS_PROVIDER, 0, 0, locationListener);
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1); //could be any number*//*);
            } else {
                locationManager.requestLocationUpdates(locationManager.GPS_PROVIDER, 0, 0, locationListener);
                Location lastKnownLocation = null;
                while (lastKnownLocation == null) {
                    lastKnownLocation = locationManager.getLastKnownLocation(locationManager.GPS_PROVIDER);
                    if (lastKnownLocation != null) {
                        liveLocation = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
                        break;
                    }
                }
            }
        }


        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.setLatLngBoundsForCameraTarget(sgBounds);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(liveLocation, 14));
        mMap.setMinZoomPreference(10.0f);
    }


}


