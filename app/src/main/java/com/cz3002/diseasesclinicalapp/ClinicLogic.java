package com.cz3002.diseasesclinicalapp;

import android.content.Context;

import com.google.android.gms.maps.model.Marker;
import com.google.maps.android.data.geojson.GeoJsonFeature;
import com.google.maps.android.data.geojson.GeoJsonLayer;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

public class ClinicLogic {
    private Context clinicDataContext;
    public ClinicLogic(Context context) {
        this.clinicDataContext = context;
    }
    //public static ArrayList<JSONObject> allClinics = new ArrayList<>();
    DistanceLogic distanceLogic = new DistanceLogic();

    ArrayList<JSONObject> getClinics() {
        ArrayList<JSONObject> allClinics = new ArrayList<JSONObject>();
        try {
            GeoJsonLayer clinicLayer = new GeoJsonLayer(MapsActivity.mMap, R.raw.clinics_geo, clinicDataContext);

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
                double point = distanceLogic.calculateDist(lati, longit);
                String nameOfClinic = clinic.getString("name");


                double minlati = (double) min.get("lati");
                double minlongi = (double) min.get("lati");
                double currentMin = distanceLogic.calculateDist(minlati, minlongi);

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
