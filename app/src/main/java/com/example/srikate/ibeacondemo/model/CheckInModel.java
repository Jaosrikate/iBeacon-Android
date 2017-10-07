package com.example.srikate.ibeacondemo.model;

/**
 * Created by srikate on 10/5/2017 AD.
 */

public class CheckInModel {

    public String username;
    public String date;
    public String time;
    public String beaconID;
    public String minor;
    public String major;
    public LocationModel location;

    public LocationModel getLocation() {
        return location;
    }

    public String getUsername() {
        return username;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    public String getBeaconID() {
        return beaconID;
    }

    public String getMinor() {
        return minor;
    }

    public String getMajor() {
        return major;
    }


    public CheckInModel(String username, String date, String time, String beaconID, String minor, String major, LocationModel location) {
        this.username = username;
        this.date = date;
        this.time = time;
        this.beaconID = beaconID;
        this.minor = minor;
        this.major = major;
        this.location = location;
    }

    @Override
    public String toString() {
        return "CheckInModel{" +
                "username='" + username + '\'' +
                ", date='" + date + '\'' +
                ", time='" + time + '\'' +
                ", beaconID='" + beaconID + '\'' +
                ", minor='" + minor + '\'' +
                ", major='" + major + '\'' +
                ", location=" + location +
                '}';
    }
}
