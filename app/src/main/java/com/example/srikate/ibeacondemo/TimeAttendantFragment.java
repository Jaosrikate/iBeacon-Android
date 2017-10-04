package com.example.srikate.ibeacondemo;

import android.Manifest;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;


import com.tbruyelle.rxpermissions.RxPermissions;

import mobi.inthepocket.android.beacons.ibeaconscanner.Beacon;
import mobi.inthepocket.android.beacons.ibeaconscanner.Error;
import mobi.inthepocket.android.beacons.ibeaconscanner.IBeaconScanner;
import rx.Observer;

/**
 * Created by srikate on 10/4/2017 AD.
 */

public class TimeAttendantFragment extends Fragment implements IBeaconScanner.Callback {

    private Button checkinBtn;
    private Button cancelBtn;

    public static TimeAttendantFragment newInstance() {
        return new TimeAttendantFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        IBeaconScanner.getInstance().setCallback(this);

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.time_atten_fragment, container, false);

        checkinBtn = v.findViewById(R.id.checkinBtn);
        cancelBtn = v.findViewById(R.id.stopBtn);
        checkinBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                IBeaconScanner.getInstance().startMonitoring(Beacon.newBuilder()
                        .setUUID(getString(R.string.beacon_uuid))
                        .setMajor(Integer.valueOf(0))
                        .setMinor(Integer.valueOf(0))
                        .build());
            }
        });

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                IBeaconScanner.getInstance().stop();
            }
        });

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
                        if (granted) {
                            checkinBtn.setEnabled(true);
                        } else {
                            checkinBtn.setEnabled(false);
                            // Oh no permission denied
                        }
                    }
                });

        return v;
    }

    @Override
    public void didEnterBeacon(Beacon beacon) {
        final String logMessage = String.format("Entered beacon with UUID %s and major %s and minor %s.", beacon.getUUID(), beacon.getMajor(), beacon.getMinor());
        Toast.makeText(getContext(), logMessage, Toast.LENGTH_LONG).show();
    }

    @Override
    public void didExitBeacon(Beacon beacon) {
        final String logMessage = String.format("Exited beacon with UUID %s and major %s and minor %s.", beacon.getUUID(), beacon.getMajor(), beacon.getMinor());
        Toast.makeText(getContext(), logMessage, Toast.LENGTH_LONG).show();
    }

    @Override
    public void monitoringDidFail(Error error) {
        Toast.makeText(getContext(), "Could not scan due to " + error.name(), Toast.LENGTH_LONG).show();
    }

    private void beaconsEnabled(final boolean isEnabled) {
        cancelBtn.setEnabled(isEnabled);
        checkinBtn.setEnabled(isEnabled);
    }
}
