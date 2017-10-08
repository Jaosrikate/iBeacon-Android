package com.example.srikate.ibeacondemo.model;

/**
 * Created by srikate on 10/7/2017 AD.
 */

public class LocationModel {

    public String lat;
    public String lon;

    public LocationModel(String location_lat, String location_long) {
        this.lat = location_lat;
        this.lon = location_long;
    }

    @Override
    public String toString() {
        return "Location{" +
                "lat='" + lat + '\'' +
                ", location_long='" + lon + '\'' +
                '}';
    }
}
