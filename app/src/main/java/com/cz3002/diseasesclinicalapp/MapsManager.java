package com.cz3002.diseasesclinicalapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;

import androidx.annotation.DrawableRes;
import androidx.core.content.ContextCompat;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.maps.android.data.geojson.GeoJsonFeature;
import com.google.maps.android.data.geojson.GeoJsonLayer;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

public class MapsManager {

    private Context MapsManagerContext;
    public MapsManager(Context context) {
        this.MapsManagerContext = context;
    }

    BitmapDescriptor bitmapDescriptorFromVector(Context context, @DrawableRes int vectorResId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    String[] getClinicNames() {
        //MapsManagerContext clinicLogic = new clinicLogic(MapsManagerContext);
        String[] allNames = new String[5];
        int i = 0;
        ArrayList<JSONObject> all_clinic_data = getClinics();
        try {

            for (JSONObject c : all_clinic_data) {
                String clinicName = c.getString("name");

                allNames[i] = clinicName;
                i++;

            }
            return allNames;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return allNames;
    }

    public double calculateDist(double x, double y) {
        Location markerLoc = new Location("Marker");
        markerLoc.setLatitude(x);
        markerLoc.setLongitude(y);

        Location currentLoc = new Location("Current");
        //currentLoc.setLatitude(MapsActivity.liveLocation.latitude);
        //currentLoc.setLongitude(MapsActivity.liveLocation.longitude);
        currentLoc.setLatitude(1.3333);
        currentLoc.setLongitude(103.6808);
        double distance = currentLoc.distanceTo(markerLoc);
        return distance;
    }

    ArrayList<JSONObject> getClinics() {
        ArrayList<JSONObject> allClinics = new ArrayList<JSONObject>();
        try {
            GeoJsonLayer clinicLayer = new GeoJsonLayer(MapsActivity.mMap, R.raw.clinics_geo, MapsManagerContext);

            for (GeoJsonFeature feature : clinicLayer.getFeatures()) {
                JSONObject clinic = new JSONObject();
                String clinicName = feature.getProperty("name");
                String clinicLatStr = feature.getProperty("lat");
                String clinicLngStr = feature.getProperty("long");
                double clinicLat = Double.parseDouble(clinicLatStr);
                double clinicLong = Double.parseDouble(clinicLngStr);
                //System.out.println(clinicName);

                clinic.put("name", clinicName);
                clinic.put("lati", clinicLat);
                clinic.put("longi", clinicLong);

                allClinics.add(clinic);
            }
            return allClinics;


        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return allClinics;
    }


    ArrayList<JSONObject> getNearestClinics(int n) throws JSONException {
        ArrayList<JSONObject> nearestClinics = new ArrayList<JSONObject>();
        ArrayList<JSONObject> allClinics = getClinics();
        boolean repeated;
        int index;
        int indexClinic = 0;

        for (int i = 0; i < n; i++) {
            index = -1;
            repeated = false;
            JSONObject min = allClinics.get(0);
            for (JSONObject clinic : allClinics) {
                index++;
                double lati = (double) clinic.get("lati");
                double longit = (double) clinic.get("longi");
                double point = calculateDist(lati, longit);
                String nameOfClinic = clinic.getString("name");


                double minlati = (double) min.get("lati");
                double minlongi = (double) min.get("longi");
                double currentMin = calculateDist(minlati, minlongi);

                if (point < currentMin) {
                    for (JSONObject a : nearestClinics) {
                        if (a.get("name").equals(nameOfClinic)) {
                            repeated = true;
                            break;
                        }
                    }
                    if (repeated == false) {
                        min = clinic;
                        indexClinic = index;
                    }
                }
            }
            nearestClinics.add(min);
            allClinics.remove(indexClinic);
        }

        return nearestClinics;
    }


}
