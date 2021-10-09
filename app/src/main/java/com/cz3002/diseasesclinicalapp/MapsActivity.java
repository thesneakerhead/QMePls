package com.cz3002.diseasesclinicalapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.navigation.Navigation;

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
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;
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
import com.google.android.gms.maps.model.Marker;
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

import java.lang.reflect.Array;
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
    private LinearLayout cliniccard;
    private TextView clinicname;
    private TextView clinicaddr;
    private TextView clinicpostal;
    private TextView clinicnum;
    private TextView clinicopening;
    SimpleDateFormat formatter = new SimpleDateFormat("HHmm");
    private static Marker clinicM1;
    private static Marker clinicM2;
    private static Marker  clinicMarkers[];

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




    }

    //onclicklistener for cliniccard
        //camerazoom to card

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

                LinearLayout card1 = findViewById(R.id.card1);
                LinearLayout card2 = findViewById(R.id.card2);
                LinearLayout card3 = findViewById(R.id.card3);

                card1.setOnTouchListener(new View.OnTouchListener()
                {

                    @Override
                    public boolean onTouch(View v, MotionEvent event)
                    {
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(clinicMarkers[0].getPosition()));
                        return false;
                    }

                });

                card2.setOnTouchListener(new View.OnTouchListener()
                {

                    @Override
                    public boolean onTouch(View v, MotionEvent event)
                    {
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(clinicMarkers[1].getPosition()));
                        return false;
                    }

                });

                card3.setOnTouchListener(new View.OnTouchListener()
                {

                    @Override
                    public boolean onTouch(View v, MotionEvent event)
                    {
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(clinicMarkers[2].getPosition()));
                        return false;
                    }

                });



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

                        //if no duplicate inside currentSearchList and not nearest default 3(){
                            //CLINIC CARD 1:
                      //  }


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
        int cardno;
        String cname;
        String caddr;
        String cnum;
        String copening;
        clinicMarkers = new Marker[3];
        // test nearest 3 clinics
        try {
            ArrayList<JSONObject> nearest_clinic_data = mapsManager.getNearestClinics();
            cardno=0;
            for (JSONObject b : nearest_clinic_data) {
                cname="clinicname";
                caddr= "clinicaddr";
                cnum= "clinicnum";
                copening = "clinicopening";

                try {
                    //iterate each card number
                    cardno++;
                    cname += "" + cardno + "";
                    caddr += "" + cardno + "";
                    cnum += "" + cardno + "";
                    copening += "" + cardno + "";

                    //get the View ID
                    int  cname_card = getResources().getIdentifier(cname, "id", getPackageName());
                    int  caddr_card = getResources().getIdentifier(caddr, "id", getPackageName());
                    int  cnum_card = getResources().getIdentifier(cnum, "id", getPackageName());
                    int  copening_card = getResources().getIdentifier(copening, "id", getPackageName());
                    //Find View ID
                    clinicname = findViewById(cname_card);
                    clinicaddr = findViewById(caddr_card);
                    clinicnum = findViewById(cnum_card);
                    clinicopening = findViewById(copening_card);

                    //Printing of info
                    String clinic_name = b.getString("name");
                    clinicname.setText(clinic_name);
                    Double clinic_lat = b.getDouble("lati");
                    Double clinic_long = b.getDouble("longi");
                    String clinic_addr = b.getString("address");
                    clinicaddr.setText(clinic_addr);
                    String clinic_num = b.getString("tel");
                    clinicnum.setText("Tel: "+clinic_num);


                    String clinic_open = b.getString("openingHour");
                    String clinic_close = b.getString("closingHour");
                    clinicopening.setText(clinic_open+ "H - "+clinic_close+"H");

                    LatLng clinicPos = new LatLng(clinic_lat,clinic_long);
                    clinicMarkers[cardno-1] = mMap.addMarker(new MarkerOptions().position(clinicPos).title(clinic_name)
                            .icon(mapsManager.bitmapDescriptorFromVector(MapsActivity.this, R.drawable.ic_clinicmarker)));

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
