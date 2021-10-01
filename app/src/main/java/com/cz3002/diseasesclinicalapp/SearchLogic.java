package com.cz3002.diseasesclinicalapp;
import android.content.Context;

import com.google.maps.android.data.geojson.GeoJsonFeature;
import com.google.maps.android.data.geojson.GeoJsonLayer;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

public class SearchLogic {

        private Context searchLogicContext;
        public SearchLogic(Context context) {
            this.searchLogicContext = context;
        }


        String[] getClinicNames() {
            ClinicLogic clinicLogic = new ClinicLogic(searchLogicContext);
            String[] allNames = new String[5];
            int i = 0;
            ArrayList<JSONObject> all_clinic_data = clinicLogic.getClinics();
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

        public void findClinicSearch(String selectedClinic) {
            ClinicLogic clinicLogic = new ClinicLogic(searchLogicContext);
            ArrayList<JSONObject> all_clinic_data = clinicLogic.getClinics();
            for (JSONObject a : all_clinic_data) {
                try {
                    String clinic_name = a.getString("name");
                    if (clinic_name.equals(selectedClinic)) {
                        System.out.println("True");
                        break;
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        }
    }

