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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
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
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.lang.reflect.Array;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import kotlin.Metadata;
import kotlin.jvm.internal.Intrinsics;
import lombok.SneakyThrows;


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
    private TextView clinicnum;
    private TextView clinicopening;
    private Button qButton;
    SimpleDateFormat formatter = new SimpleDateFormat("HHmm");
    private static Marker clinicM1;
    private static Marker clinicM2;
    private static Marker  clinicMarkers[];
    private ImageView bottomNav;
    private ScrollView bottombar;
    private static boolean minimized;
    private FirebaseDatabaseManager dbMngr;
    private MapsManager mapsManager;
    private FirebaseUser loggedInUser;
    private PatientUser curPatientUser;
    private DatabaseReference userDbRef;
    private ArrayList<String> selectedListOfSymptoms;
    private TextView clinic1queueText,clinic2queueText,clinic3queueText;

    @SneakyThrows
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        checkMyPermission();
        selectedListOfSymptoms = getIntent().getStringArrayListExtra("symptoms");
        dbMngr = new FirebaseDatabaseManager(MapsActivity.this);
        loggedInUser = FirebaseAuth.getInstance().getCurrentUser();
        userDbRef = dbMngr.getDatabaseReference("app","Users",loggedInUser.getUid());
        userDbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue()!=null)
                {
                    curPatientUser = snapshot.getValue(PatientUser.class);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        mLocationClient = new FusedLocationProviderClient(this);
        mapsManager = new MapsManager(this,MapsActivity.this);
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




        Button centralizer = findViewById(R.id.centralizer);
        centralizer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getCurrLoc();
                //CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(curLoc, 13);
                //mMap.moveCamera(cameraUpdate);
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

                minimized=true;
                disableScroll();
                bottomNav = findViewById(R.id.minimize);
                bottomNav.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (minimized) {
                            System.out.println("true");
                            bottombar = findViewById(R.id.bottombar);
                            ViewGroup.LayoutParams bottombar2 = bottombar.getLayoutParams();
                            bottombar2.height = 1700;
                            bottombar.setLayoutParams(bottombar2);
                            minimized=false;
                            enableScroll();
                        }
                        else {
                            System.out.println("false");
                            bottombar = findViewById(R.id.bottombar);
                            ViewGroup.LayoutParams bottombar2 = bottombar.getLayoutParams();
                            bottombar2.height = 200;
                            bottombar.setLayoutParams(bottombar2);
                            minimized=true;
                            disableScroll();
                        }



                    }
                });


                //camera zoom to clinic marker
                LinearLayout card1 = findViewById(R.id.card1);
                LinearLayout card2 = findViewById(R.id.card2);
                LinearLayout card3 = findViewById(R.id.card3);
                card1.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(clinicMarkers[0].getPosition()));
                    }
                });

                card2.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(clinicMarkers[1].getPosition()));
                    }
                });

                card3.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(clinicMarkers[2].getPosition()));
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

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(curLoc, 13);
        mMap.addMarker(new MarkerOptions().position(curLoc).title("Current Location")
                .icon(mapsManager.bitmapDescriptorFromVector(MapsActivity.this, R.drawable.ic_livemarker)));
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
        clinic1queueText = findViewById(R.id.clinicqueue1);
        clinic2queueText = findViewById(R.id.clinicqueue2);
        clinic3queueText = findViewById(R.id.clinicqueue3);
        // test nearest 3 clinics
        dbMngr.getAllClinicInfo()
                .thenApply(dict->{
                    ArrayList<JSONObject> nearest_clinic_data = new ArrayList<JSONObject>();
                    for (Map.Entry<String,ClinicInfo> entry:dict.entrySet())
                    {try{

                        JSONObject clinic = new JSONObject();
                        String uuid = entry.getKey();
                        ClinicInfo clinicInfo = entry.getValue();
                        clinic.put("name", clinicInfo.getClinicName());
                        clinic.put("lati", clinicInfo.getLat());
                        clinic.put("longi", clinicInfo.getLng());
                        clinic.put("openingHour", clinicInfo.getOpeningHour());
                        clinic.put("closingHour", clinicInfo.getClosingHour());
                        clinic.put("address", clinicInfo.getAddress());
                        //clinic.put("postalCode", clinicPostal);
                        clinic.put("tel", clinicInfo.getTelNo());
                        clinic.put("uuid",uuid);
                        Log.d("uuid", uuid);
                        nearest_clinic_data.add(clinic);}
                        catch (Exception e)
                        {

                        }
                    }
                    try {
                        nearest_clinic_data = mapsManager.getNearestClinics(nearest_clinic_data);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    Log.d("hi",nearest_clinic_data.toString());
                    int cardno;
                    String cname;
                    String caddr;
                    String cnum;
                    String copening;
                    String cbutton;

                    clinicMarkers = new Marker[3];
                    cardno=0;
                    for (JSONObject b : nearest_clinic_data) {
                        cname = "clinicname";
                        caddr = "clinicaddr";
                        cnum = "clinicnum";
                        copening = "clinicopening";
                        cbutton = "clinicButton";

                        try {
                            //iterate each card number
                            cardno++;
                            cname += "" + cardno + "";
                            caddr += "" + cardno + "";
                            cnum += "" + cardno + "";
                            copening += "" + cardno + "";
                            cbutton +=""+cardno+"";
                            int cname_card = getResources().getIdentifier(cname, "id", getPackageName());
                            int caddr_card = getResources().getIdentifier(caddr, "id", getPackageName());
                            int cnum_card = getResources().getIdentifier(cnum, "id", getPackageName());
                            int copening_card = getResources().getIdentifier(copening, "id", getPackageName());
                            int cbutton_card = getResources().getIdentifier(cbutton, "id", getPackageName());
                            //Find View ID

                            listenForQueueChanges(b.getString("uuid"),cardno);

//                            switch (cardno)
//                            {
//                                case 1:
//                                    clinic1queueText = findViewById(cqueue_card);
//                                    listenForQueueChanges(b.getString("uuid"),clinic1queueText.getId());
//                                    break;
//                                case 2:
//                                    clinic2queueText = findViewById(cqueue_card);
//                                    listenForQueueChanges(b.getString("uuid"),clinic2queueText.getId());
//                                    break;
//                                case 3:
//                                    clinic3queueText = findViewById(caddr_card);
//                                    listenForQueueChanges(b.getString("uuid"),clinic3queueText.getId());
//                                    break;
//                            }
                            clinicname = findViewById(cname_card);
                            clinicaddr = findViewById(caddr_card);
                            clinicnum = findViewById(cnum_card);
                            clinicopening = findViewById(copening_card);
                            qButton = findViewById(cbutton_card);
                            //ArrayList<String> clinicTags = new ArrayList<String>();
                            String tagString = b.getString("uuid")+","+b.getString("name");
                            qButton.setTag(tagString);
                            qButton.setOnClickListener(new View.OnClickListener() {
                                @SneakyThrows
                                @Override
                                public void onClick(View v) {

                                    String tagString = (String)v.getTag();
                                    String[] tags = tagString.split(",");
                                    String clinicUUID = tags[0];
                                    String clinicName = tags[1];

                                    HttpRequestHandler hndlr = new HttpRequestHandler();
                                    hndlr.joinQueue(clinicUUID,loggedInUser.getUid())
                                            .thenApply(s->{
                                                dbMngr.addToNameDictionary(loggedInUser.getUid(),curPatientUser.getName());
                                                OngoingSymptomCard ongoingCard = new OngoingSymptomCard();
                                                ongoingCard.setClinicUID(clinicUUID);
                                                ongoingCard.setSymptoms(selectedListOfSymptoms);
                                                ongoingCard.setClinicName(clinicName);
                                                Date c = Calendar.getInstance().getTime();
                                                SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                                                String formattedDate = df.format(c);
                                                ongoingCard.setDate(formattedDate);
                                                ongoingCard.setStatus("Ongoing");
                                                curPatientUser.setOngoingCard(ongoingCard);
                                                dbMngr.getDatabaseReference("app","Users",loggedInUser.getUid())
                                                        .setValue(curPatientUser);
                                                Intent intent = new Intent(MapsActivity.this,PatientPage.class);
                                                startActivity(intent);
                                                return null;
                                            });

                                }
                            });
                            //Printing of info
                            String clinic_name = b.getString("name");
                            clinicname.setText(clinic_name);
                            Double clinic_lat = b.getDouble("lati");
                            Double clinic_long = b.getDouble("longi");
                            String clinic_addr = b.getString("address");
                            clinicaddr.setText(clinic_addr);
                            String clinic_num = b.getString("tel");
                            clinicnum.setText("Tel: " + clinic_num);


                            String clinic_open = b.getString("openingHour");
                            String clinic_close = b.getString("closingHour");
                            clinicopening.setText(clinic_open + "H - " + clinic_close + "H");

                            LatLng clinicPos = new LatLng(clinic_lat, clinic_long);
                            clinicMarkers[cardno - 1] = mMap.addMarker(new MarkerOptions().position(clinicPos).title(clinic_name)
                                    .icon(mapsManager.bitmapDescriptorFromVector(MapsActivity.this, R.drawable.ic_clinicmarker)));

                        } catch (JSONException e) {
                            e.printStackTrace();
                            //} catch (ParseException e) {
                            //e.printStackTrace();
                        }
                    }
                return null;});


        //get the View ID

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
    public void listenForQueueChanges(String clinicUID, int cardNo)
    {
        Log.d("test","test");
        dbMngr.clinicDatabase.getReference("clinicDictionary")
                .child(clinicUID)
                .child("clinicQueue")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.getValue()!=null){
                            GenericTypeIndicator<ArrayList<String>> t = new GenericTypeIndicator<ArrayList<String>>() {};
                            ArrayList<String> queue = snapshot.getValue(t);
                            String queueString = String.valueOf(queue.size())+ " in Queue";
                            if (cardNo==1)
                            {
                                clinic1queueText.setText(queueString);
                            }
                            if (cardNo==2)
                            {
                                clinic2queueText.setText(queueString);
                            }
                            if (cardNo==3)
                            {
                                clinic3queueText.setText(queueString);
                            }


                        }
                        else{
                            String queueString = "0 in Queue";
                            if (cardNo==1)
                            {
                                clinic1queueText.setText(queueString);
                            }
                            if (cardNo==2)
                            {
                                clinic2queueText.setText(queueString);
                            }
                            if (cardNo==3)
                            {
                                clinic3queueText.setText(queueString);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
    public void disableScroll(){
        ScrollView scrollView = findViewById(R.id.bottombar);
        scrollView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
    }
    public void enableScroll(){
        ScrollView scrollView = findViewById(R.id.bottombar);
        scrollView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return false;
            }
        });
    }

}
