package com.cz3002.diseasesclinicalapp;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableArrayList;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

public class FirebaseDatabaseManager {
    public FirebaseApp appDBApp;
    public FirebaseApp clinicDBApp;
    public FirebaseDatabase appDatabase;
    public FirebaseDatabase clinicDatabase;
    public ObservableArrayList<ClinicInfo> clinicInfos;
    private ApplicationInfo ai;
    public FirebaseDatabaseManager(Context context) throws PackageManager.NameNotFoundException {
        ai = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
        clinicInfos = new ObservableArrayList<ClinicInfo>();
        try
        {
            this.appDBApp=FirebaseApp.getInstance("appDB");
            this.clinicDBApp = FirebaseApp.getInstance("clinicDB");
            this.appDatabase = FirebaseDatabase.getInstance(this.appDBApp);
            this.clinicDatabase=FirebaseDatabase.getInstance(this.clinicDBApp);
        }
        catch(IllegalStateException e)
        {
            instatiateAppDatabase(context);
            instantiateClinicDatabase(context);

        }
    }

    public void instatiateAppDatabase(Context context)
    {

        FirebaseOptions options = new FirebaseOptions.Builder()
                .setApplicationId(ai.metaData.getString("AppAppId"))
                .setApiKey(ai.metaData.getString("AppApiKey"))
                .setDatabaseUrl(ai.metaData.getString("AppDbURL"))
                .build();
        FirebaseApp.initializeApp(context, options,"appDB");
        FirebaseApp initApp = FirebaseApp.getInstance("appDB");
        this.appDBApp=initApp;
        this.appDatabase = FirebaseDatabase.getInstance(initApp);
    }
    public void instantiateClinicDatabase(Context context)
    {
        FirebaseOptions options = new FirebaseOptions.Builder()
                .setApplicationId(ai.metaData.getString("ClinicAppId"))
                .setApiKey(ai.metaData.getString("ClinicApiKey"))
                .setDatabaseUrl(ai.metaData.getString("ClinicDbURL"))
                .build();
        FirebaseApp.initializeApp(context, options,"clinicDB");
        FirebaseApp initApp = FirebaseApp.getInstance("clinicDB");
        this.clinicDBApp=initApp;
        this.clinicDatabase = FirebaseDatabase.getInstance(initApp);
    }

    public DatabaseReference getDatabaseReference(String dbName, String parentReference,String childReference)
    {
        DatabaseReference dbRef = null;
        if (dbName.equals("app"))
        {
            dbRef = this.appDatabase.getReference(parentReference).child(childReference);
            return dbRef;
        }
        else if (dbName.equals("clinic"))
        {
            dbRef = this.clinicDatabase.getReference(parentReference).child(childReference);
            return dbRef;
        }
        return dbRef;
    }
    public DatabaseReference getDatabaseReference(String dbName, String parentReference, String childReferenceOne,String childReferenceTwo)
    {
        DatabaseReference dbRef = null;
        if (dbName.equals("app"))
        {
            dbRef = this.appDatabase.getReference(parentReference).child(childReferenceOne).child(childReferenceTwo);
            return dbRef;
        }
        else if (dbName.equals("clinic"))
        {
            dbRef = this.clinicDatabase.getReference(parentReference).child(childReferenceOne).child(childReferenceTwo);
            return dbRef;
        }
        return dbRef;
    }
    public DatabaseReference getDatabaseReference(String dbName, String parentReference, String childReferenceOne,String childReferenceTwo,String childReferenceThree)
    {
        DatabaseReference dbRef = null;
        if (dbName.equals("app"))
        {
            dbRef = this.appDatabase.getReference(parentReference).child(childReferenceOne).child(childReferenceTwo).child(childReferenceThree);
            return dbRef;
        }
        else if (dbName.equals("clinic"))
        {
            dbRef = this.clinicDatabase.getReference(parentReference).child(childReferenceOne).child(childReferenceTwo).child(childReferenceThree);
            return dbRef;
        }
        return dbRef;
    }






}
