package com.example.srikate.ibeacondemo.timeattendant;

import android.Manifest;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;


import com.example.srikate.ibeacondemo.R;
import com.example.srikate.ibeacondemo.utils.UiHelper;
import com.google.android.material.snackbar.Snackbar;
import com.tbruyelle.rxpermissions.RxPermissions;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

import mobi.inthepocket.android.beacons.ibeaconscanner.Beacon;
import mobi.inthepocket.android.beacons.ibeaconscanner.Error;
import mobi.inthepocket.android.beacons.ibeaconscanner.IBeaconScanner;
import rx.Observer;

/**
 * Created by srikate on 10/4/2017 AD.
 * Source : https://github.com/inthepocket/ibeacon-scanner-android
 */

public class TimeAttendantFragment extends Fragment implements IBeaconScanner.Callback {

    private Button checkinBtn;
    private Beacon beacon1;
    private Beacon beacon2;
    private Date date;
    private String dateString;

    public static TimeAttendantFragment newInstance() {
        return new TimeAttendantFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        IBeaconScanner.getInstance().setCallback(this);
        setupBeacon();
        date = Calendar.getInstance().getTime();
        dateString = DateFormat.getTimeInstance().format(date) + " (" + DateFormat.getDateInstance().format(date) + ")";

    }

    private void setupBeacon() {
        beacon1 = Beacon.newBuilder()
                .setUUID(getString(R.string.beacon_uuid))
                .setMajor(Integer.valueOf(getString(R.string.beacon_major)))
                .setMinor(Integer.valueOf(getString(R.string.beacon_minor)))
                .build();
        beacon2 = Beacon.newBuilder()
                .setUUID(getString(R.string.beacon_uuid_simulator))
                .setMajor(Integer.valueOf(getString(R.string.beacon_major_simulator)))
                .setMinor(Integer.valueOf(getString(R.string.beacon_minor_simulator)))
                .build();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.time_atten_fragment, container, false);

        checkinBtn = v.findViewById(R.id.checkinBtn);
        checkinBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                if (((FancyButton) view).isExpanded()) {
//                    ((FancyButton) view).collapse();
//                    IBeaconScanner.getInstance().
                    IBeaconScanner.getInstance().startMonitoring(beacon1);
                    IBeaconScanner.getInstance().startMonitoring(beacon2);
//                } else {
//                    stopScanner();
//                }


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
                        beaconsEnabled(granted);
                    }
                });

        return v;
    }

    private void stopScanner() {
//        checkinBtn.expand();
        IBeaconScanner.getInstance().stop();
        IBeaconScanner.getInstance().stopMonitoring(beacon1);
        IBeaconScanner.getInstance().stopMonitoring(beacon2);
    }

    @Override
    public void didEnterBeacon(Beacon beacon) {
        stopScanner();
        UiHelper.showConfirmDialog(getContext(), null, "Check in at  " + dateString + "\n\n" + "bacon id : " + beacon.getUUID(), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (i == DialogInterface.BUTTON_POSITIVE) {
                    Toast.makeText(getContext(), "call service", Toast.LENGTH_LONG).show();
                }
            }
        });
//        final String logMessage = String.format("Entered beacon with UUID %s and major %s and minor %s.", beacon.getUUID(), beacon.getMajor(), beacon.getMinor());
//        if (getActivity() != null)
//            Toast.makeText(getContext(), logMessage, Toast.LENGTH_LONG).show();
    }

    @Override
    public void didExitBeacon(Beacon beacon) {
        stopScanner();
        final String logMessage = String.format("Exited beacon with UUID %s and major %s and minor %s.", beacon.getUUID(), beacon.getMajor(), beacon.getMinor());
        Snackbar.make(checkinBtn, logMessage, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void monitoringDidFail(Error error) {
        stopScanner();
        Snackbar.make(checkinBtn, "Could not scan due to " + error.name(), Snackbar.LENGTH_LONG).show();
    }

    private void beaconsEnabled(final boolean isEnabled) {
        checkinBtn.setEnabled(isEnabled);
    }

    @Override
    public void onPause() {
        super.onPause();
        IBeaconScanner.getInstance().stop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
