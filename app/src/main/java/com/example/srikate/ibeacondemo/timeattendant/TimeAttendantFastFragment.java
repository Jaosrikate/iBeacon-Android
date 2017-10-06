package com.example.srikate.ibeacondemo.timeattendant;

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
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.ekalips.fancybuttonproj.FancyButton;
import com.example.srikate.ibeacondemo.R;
import com.example.srikate.ibeacondemo.model.CheckInModel;
import com.example.srikate.ibeacondemo.utils.UiHelper;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static android.app.Activity.RESULT_OK;

/**
 * Created by srikate on 10/5/2017 AD.
 * Source : https://github.com/kiteflo/iBeaconAndroidDemo/blob/master/app/src/main/java/com/sobag/beaconplayground/MainActivity.java
 * required : targetSdkVersion 21
 */

@TargetApi(21)
public class TimeAttendantFastFragment extends Fragment {

    private static final String TAG = "TimeAttendantFast";

    private static final long SCAN_PERIOD = 10000;
    private BluetoothLeScanner mLEScanner;
    private BluetoothManager btManager;
    private BluetoothAdapter btAdapter;
    private Handler scanHandler;
    private Handler mHandler;
    private FancyButton checkInBtn;
    private Date date;
    private String dateTimeString;
    private String dateString;
    private String timeString;
    private boolean isShowDialog;
    private DatabaseReference databaseRef;

    private ScanSettings settings;
    private ArrayList<ScanFilter> filters;
    boolean isOnline;

    public static TimeAttendantFastFragment newInstance() {
        return new TimeAttendantFastFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isShowDialog = false;
        scanHandler = new Handler();

        mHandler = new Handler();
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

        // Write a message to the database
        databaseRef = FirebaseDatabase.getInstance().getReference();

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.beacon_scanner_fragment, container, false);
        checkInBtn = v.findViewById(R.id.checkinBtn);
        checkInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "Button is " + String.valueOf(checkInBtn.isExpanded()));
                if (checkInBtn.isExpanded()) {
                    if (getBlueToothOn()) {
                        startScan();
                    } else {
                        UiHelper.showInformationMessage(getActivity(), "Enable Bluetooth", "Please enable bluetooth before transmit iBeacon.",
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
                } else {
                    stopScan();
                }
            }
        });
        return v;
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


    private boolean getBlueToothOn() {
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

        final CheckInModel data = new CheckInModel("amonratk", dateString, timeString, uuid, String.valueOf(minor), String.valueOf(major));

        if (uuid.equals(getString(R.string.beacon_uuid).toUpperCase()) || uuid.equals(getString(R.string.beacon_uuid_simulator).toUpperCase())) {

            if (!isShowDialog) {
                UiHelper.showConfirmDialog(getContext(), "Check in at  " + getCurrentDateTime() + "\n\n" + "Beacon id : " + uuid, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (i == DialogInterface.BUTTON_POSITIVE) {

                            if (isFirebaseDBConnected()) {
                                databaseRef.child("time_attendant").child("962").setValue(data, new DatabaseReference.CompletionListener() {
                                    @Override
                                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                        if (databaseError != null) {
                                            Log.e(TAG, "Error save user : " + databaseError.getMessage());
                                        } else {
                                            Snackbar.make(checkInBtn, "Saved", Snackbar.LENGTH_LONG).show();
                                            isShowDialog = false;
                                        }
                                    }
                                });
                            } else {
                                UiHelper.showErrorMessage(getContext(), "Connection Problem. Please , Check your internet connection.");
                            }
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

    private boolean isFirebaseDBConnected() {

        isOnline = false;

        DatabaseReference connectedRef = FirebaseDatabase.getInstance().getReference(".info/connected");
        connectedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                boolean connected = snapshot.getValue(Boolean.class);
                isOnline = connected;
                if (connected) {
                    System.out.println("connected");
                } else {
                    System.out.println("not connected");
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                System.err.println("Listener was cancelled");
            }
        });
        return isOnline;
    }

    private String getCurrentDateTime() {
        date = Calendar.getInstance().getTime();

        dateString = DateFormat.getDateInstance().format(date);
        timeString = DateFormat.getTimeInstance().format(date);
        dateTimeString = timeString + " (" + dateString + ")";

        return dateTimeString;
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
        if (resultCode == RESULT_OK) {
            startScan();
        } else {
            Log.e(TAG, "result not ok");
        }
    }
}