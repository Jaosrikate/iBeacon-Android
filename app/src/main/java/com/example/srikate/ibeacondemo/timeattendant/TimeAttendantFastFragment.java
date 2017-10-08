package com.example.srikate.ibeacondemo.timeattendant;

import android.Manifest;
import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ekalips.fancybuttonproj.FancyButton;
import com.example.srikate.ibeacondemo.R;
import com.example.srikate.ibeacondemo.model.CheckInModel;
import com.example.srikate.ibeacondemo.model.LocationModel;
import com.example.srikate.ibeacondemo.utils.GPSTracker;
import com.example.srikate.ibeacondemo.utils.UiHelper;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;

import static android.app.Activity.RESULT_OK;

/**
 * Created by srikate on 10/5/2017 AD.
 * Source : https://github.com/kiteflo/iBeaconAndroidDemo/blob/master/app/src/main/java/com/sobag/beaconplayground/MainActivity.java
 * required : targetSdkVersion 21
 */

@TargetApi(21)
public class TimeAttendantFastFragment extends Fragment implements View.OnClickListener, ResultCallback<LocationSettingsResult> {

    private static final String TAG = "TimeAttendantFast";

    private static final long SCAN_PERIOD = 10000;
    private BluetoothLeScanner mLEScanner;
    private BluetoothManager btManager;
    private BluetoothAdapter btAdapter;
    private Handler scanHandler;
    private Handler mHandler;
    private FancyButton checkInBtn;
    private TextView tvEmpID;
    private ImageView ivRandomEmp;
    private boolean isShowDialog;
    private DatabaseReference databaseRef;

    private ScanSettings settings;
    private ArrayList<ScanFilter> filters;
    private String employeeID;
    private GPSTracker gps;

    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    public static final int REQUEST_CHECK_SETTINGS = 14;


    public static TimeAttendantFastFragment newInstance() {
        return new TimeAttendantFastFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isShowDialog = false;
        scanHandler = new Handler();
        mHandler = new Handler();
        employeeID = getRandomID();

        settingBlueTooth();

        // Write a message to the database
        databaseRef = FirebaseDatabase.getInstance().getReference();

        settingLocationRequest();

        checkLocationPermission();

        if (isLocationEnabled()) {
            gps = new GPSTracker(getContext());
            Log.i("Location_Lat", getLat() + " " + getLon());
        } else {
            displayLocationSettingsRequest();
        }

    }

    private void settingBlueTooth() {
        // init BLE
        btManager = (BluetoothManager) getActivity().getSystemService(Context.BLUETOOTH_SERVICE);
        btAdapter = btManager.getAdapter();

        if (Build.VERSION.SDK_INT >= 21) {
            mLEScanner = btAdapter.getBluetoothLeScanner();
            settings = new ScanSettings.Builder()
                    .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                    .build();
            filters = new ArrayList<ScanFilter>();
        }
    }

    private void settingLocationRequest() {
        googleApiClient = new GoogleApiClient.Builder(getContext())
                .addApi(LocationServices.API).build();
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(10000 / 2);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.beacon_scanner_fragment, container, false);

        checkInBtn = view.findViewById(R.id.checkinBtn);
        tvEmpID = view.findViewById(R.id.tvEmpID);
        ivRandomEmp = view.findViewById(R.id.ivRandomEmp);

        checkInBtn.setOnClickListener(this);
        ivRandomEmp.setOnClickListener(this);

        setEmpID();

        Log.i("onViewCreate", "es");

        return view;
    }


    private void startScan() {
        checkInBtn.collapse();
        scanLeDevice(true);
    }

    private void stopScan() {
        checkInBtn.expand();
        scanLeDevice(false);
    }


    private void scanLeDevice(final boolean enable) {
        if (enable) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Log.i(TAG, "BLE stop scan");
                    if (Build.VERSION.SDK_INT < 21) {
                        Log.i(TAG, "runnable stop SDK_INT < 21");
                        btAdapter.stopLeScan(leScanCallback);
                    } else {
                        Log.i(TAG, "runnable stop SDK_INT >= 21");
                        mLEScanner.stopScan(mScanCallback);
                    }
                    checkInBtn.expand();
                    if (!isShowDialog)
                        Toast.makeText(getContext(), "Signal Not found. Please, Try again.", Toast.LENGTH_SHORT).show();
                }
            }, SCAN_PERIOD);
            Log.i(TAG, "BLE start scan");
            if (Build.VERSION.SDK_INT < 21) {
                Log.i(TAG, "start SDK_INT < 21");
                btAdapter.startLeScan(leScanCallback);
            } else {
                Log.i(TAG, "start SDK_INT >= 21");
                mLEScanner.startScan(filters, settings, mScanCallback);
            }
        } else {
            Log.i(TAG, "BLE stop scan");
            if (Build.VERSION.SDK_INT < 21) {
                Log.i(TAG, "stop SDK_INT < 21");
                btAdapter.stopLeScan(leScanCallback);
            } else {
                Log.i(TAG, "stop SDK_INT >= 21");
                mLEScanner.stopScan(mScanCallback);
            }
        }
    }

    private ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            Log.i(TAG, "callbackType " + String.valueOf(callbackType));
            byte[] scanRecord = result.getScanRecord().getBytes();
            findBeaconPattern(scanRecord);
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            for (ScanResult sr : results) {
                Log.i(TAG, "ScanResult - Results" + sr.toString());
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            Log.e(TAG, "Scan Failed Error Code: " + errorCode);
        }
    };


    private boolean isBlueToothOn() {
        return btAdapter != null && btAdapter.isEnabled();
    }


    private BluetoothAdapter.LeScanCallback leScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, final int rssi, final byte[] scanRecord) {
            findBeaconPattern(scanRecord);
        }
    };

    private void findBeaconPattern(byte[] scanRecord) {
        int startByte = 2;
        boolean patternFound = false;
        while (startByte <= 5) {
            if (((int) scanRecord[startByte + 2] & 0xff) == 0x02 && //Identifies an iBeacon
                    ((int) scanRecord[startByte + 3] & 0xff) == 0x15) { //Identifies correct data length
                patternFound = true;
                break;
            }
            startByte++;
        }

        if (patternFound) {
            //Convert to hex String
            byte[] uuidBytes = new byte[16];
            System.arraycopy(scanRecord, startByte + 4, uuidBytes, 0, 16);
            String hexString = bytesToHex(uuidBytes);

            //UUID detection
            String uuid = hexString.substring(0, 8) + "-" +
                    hexString.substring(8, 12) + "-" +
                    hexString.substring(12, 16) + "-" +
                    hexString.substring(16, 20) + "-" +
                    hexString.substring(20, 32);

            // major
            final int major = (scanRecord[startByte + 20] & 0xff) * 0x100 + (scanRecord[startByte + 21] & 0xff);

            // minor
            final int minor = (scanRecord[startByte + 22] & 0xff) * 0x100 + (scanRecord[startByte + 23] & 0xff);

            Log.i(TAG, "UUID: " + uuid + "\\nmajor: " + major + "\\nminor" + minor);
            foundBeacon(uuid, major, minor);
        }
    }

    private void foundBeacon(String uuid, int major, int minor) {

        final LocationModel locationModel = new LocationModel(getLat(), getLon());

        final CheckInModel data = new CheckInModel("amonratk", getDateString(), getTimeString(), uuid, String.valueOf(minor), String.valueOf(major), locationModel);

        if (uuid.equals(getString(R.string.beacon_uuid).toUpperCase())
                || uuid.equals(getString(R.string.beacon_uuid_simulator).toUpperCase())) {
            Log.e(TAG, "isShowDialog : " + isShowDialog);
            if (!isShowDialog) {
                UiHelper.showConfirmDialog(getContext(), "Time Attendant Confirmation", "Check In at  " + getCurrentDateTime(), false, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (i == DialogInterface.BUTTON_POSITIVE) {
                            saveToFirebase(data);
                        } else {
                            isShowDialog = false;
                        }
                    }
                });

                isShowDialog = true;
            }
            stopScan();
        } else {
            Log.i(TAG, "Its not TISCO Beacon");
        }
    }

    private void saveToFirebase(CheckInModel data) {
        databaseRef
                .child("time_attendant")
                .child(employeeID)
                .child(String.valueOf(getDate()))
                .setValue(data, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(final DatabaseError databaseError, DatabaseReference databaseReference) {
                        if (databaseError != null) {
                            Log.i(TAG, "Error save user : " + databaseError.getDetails());
                            isShowDialog = false;
                        } else {
                            Snackbar.make(checkInBtn, "Saved", Snackbar.LENGTH_LONG).show();
                            isShowDialog = false;
                        }
                    }

                });
    }

    private String getCurrentDateTime() {
        return getTimeString() + " (" + getDateString() + ")";
    }

    private Date getDate() {
        return Calendar.getInstance().getTime();
    }

    private String getDateString() {
        return DateFormat.getDateInstance().format(getDate());
    }

    private String getTimeString() {
        return DateFormat.getTimeInstance().format(getDate());
    }

    /**
     * bytesToHex method
     */
    static final char[] hexArray = "0123456789ABCDEF".toCharArray();

    private static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        scanHandler.removeCallbacksAndMessages(null);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == 1) {
            settingBlueTooth();
            startScan();
        } else if (resultCode == RESULT_OK && requestCode == 14) {
            gps = new GPSTracker(getContext());
        } else {
            Log.e(TAG, "result not ok");
        }
    }

    public String getRandomID() {
        Random rn = new Random();
        String department = String.valueOf(rn.nextInt(15) + 1);
        String id = String.valueOf(rn.nextInt(999) + 1);

        String departmentPadding = String.format("%02d", Integer.parseInt(department));
        String idPadding = String.format("%03d", Integer.parseInt(id));
        return departmentPadding + "-" + idPadding;
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.checkinBtn: {
                checkInBtnClicked();
                break;
            }
            case R.id.ivRandomEmp: {
                setEmpID();
                break;
            }
        }
    }

    private void setEmpID() {
        employeeID = getRandomID();
        tvEmpID.setText(employeeID);
    }

    private void checkInBtnClicked() {
        Log.i(TAG, "Button is " + String.valueOf(checkInBtn.isExpanded()));
        if (checkInBtn.isExpanded()) {

            boolean isValid = true;

            if (!isLocationEnabled()) {
                isValid = false;
                displayLocationSettingsRequest();
            }

            if (!isBlueToothOn()) {
                isValid = false;
                UiHelper.showInformationMessage(getActivity(), "Enable Bluetooth", "Please enable bluetooth before check in.",
                        false, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (i == DialogInterface.BUTTON_POSITIVE) {
                                    Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                                    startActivityForResult(enableIntent, 1);
                                }
                            }
                        });
            }

            if (!isOnline()) {
                isValid = false;
                UiHelper.showInformationMessage(getContext(), "No Internet Access.", "Please , check your connection.", false);
            }

            if (isValid) {
                gps = new GPSTracker(getContext());
                startScan();
            }

        } else {
            stopScan();
        }
    }

    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    private String getLat() {
        if (gps != null) {
            double latitude = gps.getLatitude();
            return String.valueOf(latitude);
        } else {
            return null;
        }
    }

    private String getLon() {
        if (gps != null) {
            double latitude = gps.getLongitude();
            return String.valueOf(latitude);
        } else {
            return null;
        }
    }


    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(getContext())
                        .setTitle(R.string.title_location_permission)
                        .setMessage(R.string.text_location_permission)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(getActivity(),
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION);
                            }
                        })
                        .create()
                        .show();


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            Log.i(TAG, "persmission granted");
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(getContext(),
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        //Request location updates:
//                        locationManager.requestLocationUpdates(provider, 400, 1, this);/
                        gps = new GPSTracker(getContext());
                    }

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.

                }
                return;
            }

        }
    }

    private boolean isLocationEnabled() {
        int locationMode = 0;
        String locationProviders;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            try {
                locationMode = Settings.Secure.getInt(getContext().getContentResolver(), Settings.Secure.LOCATION_MODE);

            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
                return false;
            }

            return locationMode != Settings.Secure.LOCATION_MODE_OFF;

        } else {
            locationProviders = Settings.Secure.getString(getContext().getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            return !TextUtils.isEmpty(locationProviders);
        }
    }

    private synchronized void displayLocationSettingsRequest() {

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);

        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
        result.setResultCallback(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        googleApiClient.connect();
    }

    @Override
    public void onResult(@NonNull LocationSettingsResult locationSettingsResult) {
        final Status status = locationSettingsResult.getStatus();
        switch (status.getStatusCode()) {
            case LocationSettingsStatusCodes.SUCCESS:
                Log.i(TAG, "All location settings are satisfied.");
                break;
            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                Log.i(TAG, "Location settings are not satisfied. Show the user a dialog to upgrade location settings ");

                try {
                    // Show the dialog by calling startResolutionForResult(), and check the result
                    // in onActivityResult().
                    status.startResolutionForResult(getActivity(), REQUEST_CHECK_SETTINGS);
                } catch (IntentSender.SendIntentException e) {
                    Log.i(TAG, "PendingIntent unable to execute request.");
                }
                break;
            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                Log.i(TAG, "Location settings are inadequate, and cannot be fixed here. Dialog not created.");
                break;
        }
    }
}