package com.example.srikate.ibeacondemo.scanner;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.ekalips.fancybuttonproj.FancyButton;
import com.example.srikate.ibeacondemo.R;
import com.example.srikate.ibeacondemo.utils.UiHelper;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by srikate on 10/5/2017 AD.
 * Source : https://github.com/kiteflo/iBeaconAndroidDemo/blob/master/app/src/main/java/com/sobag/beaconplayground/MainActivity.java
 * required : targetSdkVersion 21
 */

public class TimeAttendantFastFragment extends Fragment {

    private static final String LOG_TAG = "TimeAttendantFast";

    private BluetoothManager btManager;
    private BluetoothAdapter btAdapter;
    private Handler scanHandler;
    private Runnable scanRunnable;
    private int scan_interval_ms = 5000;
    private boolean isScanning = false;
    private FancyButton checkinBtn;
    private Date date;
    private String dateString;
    private boolean isShowDialog;

    public static TimeAttendantFastFragment newInstance() {
        return new TimeAttendantFastFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isShowDialog = false;
        scanHandler = new Handler();
        // init BLE
        btManager = (BluetoothManager) getActivity().getSystemService(Context.BLUETOOTH_SERVICE);
        btAdapter = btManager.getAdapter();
        date = Calendar.getInstance().getTime();
        dateString = DateFormat.getTimeInstance().format(date) + " (" + DateFormat.getDateInstance().format(date) + ")";
        initRunnable();
    }

    private void initRunnable() {
        scanRunnable = new Runnable() {
            @Override
            public void run() {

                if (isScanning) {
                    if (btAdapter != null) {
                        btAdapter.stopLeScan(leScanCallback);
                    }
                } else {
                    if (btAdapter != null) {
                        btAdapter.startLeScan(leScanCallback);
                    }
                }

                isScanning = !isScanning;
                scanHandler.postDelayed(this, scan_interval_ms);
            }
        };
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.beacon_scanner_fragment, container, false);
        checkinBtn = v.findViewById(R.id.checkinBtn);
        checkinBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkinBtn.isExpanded()) {
                    checkinBtn.collapse();
                    scanHandler.post(scanRunnable);
                } else {
                    scanHandler.removeCallbacksAndMessages(null);
                    checkinBtn.expand();
                }

            }
        });
        return v;
    }

    // ------------------------------------------------------------------------
    // Inner classes
    // ------------------------------------------------------------------------

    private BluetoothAdapter.LeScanCallback leScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, final int rssi, final byte[] scanRecord) {
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

                Log.i(LOG_TAG, "UUID: " + uuid + "\\nmajor: " + major + "\\nminor" + minor);
                if (uuid.equals(getString(R.string.beacon_uuid).toUpperCase()) || uuid.equals(getString(R.string.beacon_uuid_simulator).toUpperCase())) {


                    if (!isShowDialog) {
                        UiHelper.showConfirmDialog(getContext(), "Check in at  " + dateString + "\n\n" + "bacon id : " + uuid, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (i == DialogInterface.BUTTON_POSITIVE) {
                                    Toast.makeText(getContext(), "call service", Toast.LENGTH_LONG).show();
                                }
                            }
                        });

                        isShowDialog = true;
                    }


                    checkinBtn.expand();
                    scanHandler.removeCallbacksAndMessages(null);
                    scanHandler.removeCallbacks(scanRunnable);
                    btAdapter = null;
                }
            }

        }
    };

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
}
