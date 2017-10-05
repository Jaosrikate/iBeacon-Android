package com.example.srikate.ibeacondemo;

import android.app.Application;

import mobi.inthepocket.android.beacons.ibeaconscanner.IBeaconScanner;

/**
 * Created by srikate on 10/4/2017 AD.
 */

public class BeaconDemoApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // initialize In The Pockets iBeaconScanner
        IBeaconScanner.initialize(IBeaconScanner.newInitializer(this).build());
    }
}
