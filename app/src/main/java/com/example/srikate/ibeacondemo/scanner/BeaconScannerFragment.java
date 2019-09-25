package com.example.srikate.ibeacondemo.scanner;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.RemoteException;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.srikate.ibeacondemo.R;
import com.tbruyelle.rxpermissions.RxPermissions;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.util.Collection;

import rx.Observer;

/**
 * Created by srikate on 10/4/2017 AD.
 * Source : https://github.com/AltBeacon/android-beacon-library
 */

public class BeaconScannerFragment extends Fragment implements BeaconConsumer {

    private BeaconManager beaconManager;
    private static String TAG = "BeaconScannerFragment";
    private Button checkinBtn;

    public static BeaconScannerFragment newInstance() {
        return new BeaconScannerFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        beaconManager = BeaconManager.getInstanceForApplication(getActivity());

        beaconManager.getBeaconParsers().add(new BeaconParser()
                .setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"));

        RxPermissions.getInstance(getContext())
                .request(Manifest.permission.ACCESS_COARSE_LOCATION)
                .subscribe(new Observer<Boolean>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(Boolean granted) {
                        checkinBtn.setEnabled(true);
                    }
                });


    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.beacon_scanner_fragment, container, false);
        checkinBtn = v.findViewById(R.id.checkinBtn);
        checkinBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(checkinBtn.isClickable()){
//                    checkinBtn.collapse();
                    checkinBtn.setClickable(true);
                    beaconManager.bind(BeaconScannerFragment.this);
                }else{
//                    checkinBtn.expand();
                    checkinBtn.setClickable(false);
                    beaconManager.unbind(BeaconScannerFragment.this);
                }

            }
        });
        return v;
    }

    @Override
    public void onBeaconServiceConnect() {
        final Region region = new Region("myBeaons", Identifier.parse(getString(R.string.beacon_uuid_simulator)), null, null);
        final Region region2 = new Region("myBeaons", Identifier.parse(getString(R.string.beacon_uuid)), null, null);

        beaconManager.setMonitorNotifier(new MonitorNotifier() {
            @Override
            public void didEnterRegion(Region region) {
                try {
                    Log.d(TAG, "didEnterRegion");
                    beaconManager.startRangingBeaconsInRegion(region);
                    beaconManager.startRangingBeaconsInRegion(region2);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void didExitRegion(Region region) {
                try {
                    Log.d(TAG, "didExitRegion");
                    beaconManager.stopRangingBeaconsInRegion(region);
                    beaconManager.stopRangingBeaconsInRegion(region2);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void didDetermineStateForRegion(int i, Region region) {

            }
        });

        beaconManager.setRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                for (Beacon oneBeacon : beacons) {
                    Log.d(TAG, "distance: " + oneBeacon.getDistance() + " id:" + oneBeacon.getId1() + "/" + oneBeacon.getId2() + "/" + oneBeacon.getId3());
                }
            }
        });

        try {
            beaconManager.startMonitoringBeaconsInRegion(region);
            beaconManager.startMonitoringBeaconsInRegion(region2);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }

    @Override
    public Context getApplicationContext() {
        return getActivity().getApplicationContext();
    }

    @Override
    public void unbindService(ServiceConnection serviceConnection) {
        Log.i(TAG, "unbinding service");
        getActivity().unbindService(serviceConnection);
    }

    @Override
    public boolean bindService(Intent intent, ServiceConnection serviceConnection, int i) {
        Log.i(TAG, "bind service");
        return getActivity().bindService(intent, serviceConnection, i);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        beaconManager.unbind(this);
    }
}
