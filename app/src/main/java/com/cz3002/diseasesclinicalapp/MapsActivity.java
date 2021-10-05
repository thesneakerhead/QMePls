package com.cz3002.diseasesclinicalapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.places.Places;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import kotlin.Metadata;
import kotlin.jvm.internal.Intrinsics;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    boolean isPermissionGranted;
    public static GoogleMap mMap;
    private FusedLocationProviderClient mLocationClient;
    private FusedLocationProviderClient fusedLocationClient;
    private int GPS_REQUEST_CODE = 9001;
    public static LatLng curLoc;
    private TextView clinicname;
    private TextView clinicaddr;
    private TextView clinicpostal;
    private TextView clinicnum;
    private TextView clinicopening;
    SimpleDateFormat formatter = new SimpleDateFormat("HHmm");
    Date clinicOpen, clinicClose;

    MapsManager mapsManager = new MapsManager(this);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        checkMyPermission();


        mLocationClient = new FusedLocationProviderClient(this);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            initMap();
                        }
                    }
                });


        clinicname = findViewById(R.id.clinicname);
        clinicaddr = findViewById(R.id.clinicaddr);
        clinicpostal = findViewById(R.id.clinicpostal);
        clinicnum = findViewById(R.id.clinicnum);
        clinicopening = findViewById(R.id.clinicopening);

    }


    private void initMap() {
        if (isPermissionGranted) {
            if (isGPSenable()) {
                // Obtain the SupportMapFragment and get notified when the map is ready to be used.
                SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.map);

                AutoCompleteTextView editText = findViewById(R.id.search);
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                        android.R.layout.simple_list_item_1, mapsManager.getClinicNames());
                editText.setAdapter(adapter);

                editText.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        String selectedItem = (String) parent.getItemAtPosition(position);
                        //Boolean found = searchLogic.findClinicSearch(selectedItem);

                        // test search clinic
                        ArrayList<JSONObject> all_clinic_data = mapsManager.getClinics();
                        for (JSONObject a : all_clinic_data) {
                            try {
                                String clinic_name = a.getString("name");
                                if (clinic_name.equals(selectedItem)) {
                                    //System.out.println("True");
                                    Double clinic_lat = a.getDouble("lati");
                                    Double clinic_long = a.getDouble("longi");
                                    LatLng clinicPos = new LatLng(clinic_lat, clinic_long);
                                    mMap.addMarker(new MarkerOptions()
                                            .position(clinicPos)
                                            .title("Marker"));
                                    mMap.moveCamera(CameraUpdateFactory.newLatLng(clinicPos));
                                    break;
                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        //System.out.println(selectedItem);


                    }
                });

                mapFragment.getMapAsync(this);
                getCurrLoc();
            }
        }
    }

    private boolean isGPSenable() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        boolean providerEnable = locationManager.isProviderEnabled((LocationManager.GPS_PROVIDER));
        if (providerEnable) {
            return true;
        } else {
            AlertDialog alertDialog = new AlertDialog.Builder(this)
                    .setTitle("GPS Permission")
                    .setMessage("GPS is required for the app to work. Please enable GPS")
                    .setPositiveButton("Yes", ((dialogInterface, i) -> {
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivityForResult(intent, GPS_REQUEST_CODE);
                    }))
                    .setCancelable(false)
                    .show();
        }

        return false;
    }

    @SuppressLint("MissingPermission")
    private void getCurrLoc() {
        mLocationClient.getLastLocation().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Location location = task.getResult();
                gotoLocation(location.getLatitude(), location.getLongitude());
            }
        });
    }

    private void gotoLocation(double latitude, double longitude) {
        LatLng curLoc = new LatLng(latitude, longitude);

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(curLoc, 18);
        mMap.addMarker(new MarkerOptions().position(curLoc).title("Marker"));
        mMap.moveCamera(cameraUpdate);
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
    }

    private void checkMyPermission() {
        Dexter.withContext(this).withPermission(Manifest.permission.ACCESS_FINE_LOCATION).withListener(new PermissionListener() {
            @Override
            public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                Toast.makeText(MapsActivity.this, "Permission Granted", Toast.LENGTH_SHORT).show();
                isPermissionGranted = true;
            }

            @Override
            public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getPackageName(), "");
                intent.setData(uri);
                startActivity(intent);
            }

            @Override
            public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
                permissionToken.continuePermissionRequest();
            }
        }).check();
    }

    @SuppressLint("MissingPermssion")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);

        //MapsManager mapsManager = new MapsManager(this);
        // test nearest 3 clinics
        try {
            System.out.println("1st try");
            ArrayList<JSONObject> nearest_clinic_data = mapsManager.getNearestClinics(5);
            for (JSONObject b : nearest_clinic_data) {
                try {
                    System.out.println("2nd try");
                    String clinic_name = b.getString("name");
                    clinicname.setText(clinic_name);
                    Double clinic_lat = b.getDouble("lati");
                    Double clinic_long = b.getDouble("longi");
                    String clinic_addr = b.getString("address");
                    clinicaddr.setText(clinic_addr);
//                    String clinic_postal = b.getString("postalCode");
//                    clinicpostal.setText(clinic_postal);
                    String clinic_num = b.getString("tel");
                    clinicnum.setText(clinic_num);


                    String clinic_open = b.getString("openingHour");
                    clinicopening.setText(clinic_open);
                    String clinic_close = b.getString("closingHour");
                    //clinicclose.setText(clinic_close);
                    LatLng clinicPos = new LatLng(clinic_lat,clinic_long);
                    mMap.addMarker(new MarkerOptions().position(clinicPos).title("Clinic Location")
                            .icon(mapsManager.bitmapDescriptorFromVector(MapsActivity.this, R.drawable.ic_clinicmarker)));


                    System.out.println(clinic_name);
                } catch (JSONException e) {
                    e.printStackTrace();
                //} catch (ParseException e) {
                    //e.printStackTrace();
                }
            }
        } catch (JSONException | ParseException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GPS_REQUEST_CODE) {
            LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

            boolean providerEnable = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            if (providerEnable) {
                Toast.makeText(this, "GPS is enable", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Gps is not enabled", Toast.LENGTH_SHORT).show();
            }
        }
    }



}
