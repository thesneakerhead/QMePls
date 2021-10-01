package com.cz3002.diseasesclinicalapp;

import android.location.Location;

public class DistanceLogic {

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
}

