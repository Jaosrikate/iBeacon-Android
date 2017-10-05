//package com.example.srikate.ibeacondemo;
//
//import android.annotation.TargetApi;
//import android.bluetooth.BluetoothAdapter;
//import android.bluetooth.BluetoothDevice;
//import android.bluetooth.BluetoothGatt;
//import android.bluetooth.BluetoothManager;
//import android.bluetooth.le.AdvertiseCallback;
//import android.bluetooth.le.AdvertiseSettings;
//import android.bluetooth.le.BluetoothLeScanner;
//import android.bluetooth.le.ScanCallback;
//import android.content.Context;
//import android.content.DialogInterface;
//import android.content.Intent;
//import android.content.pm.PackageManager;
//import android.os.Build;
//import android.os.Handler;
//import android.os.RemoteException;
//import android.support.v7.app.AppCompatActivity;
//import android.os.Bundle;
//import android.util.Log;
//import android.view.View;
//import android.widget.Button;
//import android.widget.ImageView;
//import android.widget.Toast;
//
//import org.altbeacon.beacon.Beacon;
//import org.altbeacon.beacon.BeaconConsumer;
//import org.altbeacon.beacon.BeaconManager;
//import org.altbeacon.beacon.BeaconParser;
//import org.altbeacon.beacon.BeaconTransmitter;
//import org.altbeacon.beacon.Identifier;
//import org.altbeacon.beacon.MonitorNotifier;
//import org.altbeacon.beacon.RangeNotifier;
//import org.altbeacon.beacon.Region;
//
//import java.util.Arrays;
//import java.util.Calendar;
//import java.util.Collection;
//import java.util.Date;
//
//
///**
// * Created by srikate on 10/5/2017 AD.
// */
//
//public class MainActivity2 extends AppCompatActivity implements View.OnClickListener, BeaconConsumer {
//
//    private static final String TAG = MainActivity.class.getSimpleName ();
//    private BluetoothManager btManager;
//    private BluetoothAdapter btAdapter;
//    private BluetoothLeScanner btLeScanner;
//    private BluetoothGatt btGatt;
//    private Handler scanHandler = new Handler ();
//    private int scan_interval_ms = 5000;
//    private boolean isScanning = false;
//    private ScanCallback mScanCallback;
//    private Beacon beacon;
//    private BeaconParser beaconParser;
//    private BeaconTransmitter beaconTransmitter;
//    private BeaconManager beaconManager;
//    private ImageView beaconSwitch;
//    private Date date;
//    private boolean isShow;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate (savedInstanceState);
//        setContentView (R.layout.activity_main);
//
//        date = Calendar.getInstance ().getTime ();
//        isShow = false;
//        // Initializes Bluetooth adapter.
//        btManager = (BluetoothManager) getSystemService (Context.BLUETOOTH_SERVICE);
//        btAdapter = btManager.getAdapter ();
//        btLeScanner = btAdapter.getBluetoothLeScanner ();
//
//        Button checkinBtn = (Button) findViewById (R.id.checkinBtn);
//        ImageView beaconBtn = (ImageView) findViewById (R.id.beaconBtn);
//        beaconSwitch = (ImageView) findViewById (R.id.beaconSwitch);
//
//        checkinBtn.setOnClickListener (this);
//        beaconBtn.setOnClickListener (this);
//
//        setupBeacon ();
//    }
//
//    private void setupBeacon() {
//        beacon = new Beacon.Builder ()
//                .setId1 ("2f234454-cf6d-4a0f-adf2-f4911ba9ffa6") // UUID for beacon
//                .setId2 ("12") // Major for beacon
//                .setId3 ("14") // Minor for beacon
//                .setManufacturer (0x004C) // Radius Networks.0x0118  Change this for other beacon layouts//0x004C for iPhone
//                .setTxPower (-56) // Power in dB
//                .setDataFields (Arrays.asList (new Long[]{0l})) // Remove this for beacon layouts without d: fields
//                .build ();
//
//        beaconParser = new BeaconParser ()
//                .setBeaconLayout ("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24");
//
//        beaconTransmitter = new BeaconTransmitter (getApplicationContext (), beaconParser);
//
//        beaconManager = BeaconManager.getInstanceForApplication (MainActivity.this);
//        // To detect proprietary beacons, you must add a line like below corresponding to your beacon
//        // type.  Do a web search for "setBeaconLayout" to get the proper expression.
//        beaconManager.getBeaconParsers ().add (beaconParser);
////        beaconManager.bind (this);
//    }
//
//    private boolean isBluetoothLEAvailable() {
//        return btAdapter != null && getPackageManager ().hasSystemFeature (PackageManager.FEATURE_BLUETOOTH_LE);
//    }
//
//    private boolean getBlueToothOn() {
//        return btAdapter != null && btAdapter.isEnabled ();
//    }
//
//
//    private Runnable scanRunnable = new Runnable () {
//        @Override
//        public void run() {
//
//            if (isScanning) {
//                if (btAdapter != null) {
//                    Log.i (TAG, "btAdapter !null isScan");
//                    btAdapter.stopLeScan (leScanCallback);
//                } else {
//                    Log.i (TAG, "btAdapter null isScan");
//                }
//            } else {
//                if (btAdapter != null) {
//                    Log.i (TAG, "btAdapter !null !isScan");
//                    btAdapter.startLeScan (leScanCallback);
//                } else {
//                    Log.i (TAG, "btAdapter null !isScan");
//                }
//            }
//
//            isScanning = !isScanning;
//
//            scanHandler.postDelayed (this, scan_interval_ms);
//        }
//    };
//
//
//    private BluetoothAdapter.LeScanCallback leScanCallback = new BluetoothAdapter.LeScanCallback () {
//        @Override
//        public void onLeScan(final BluetoothDevice device, final int rssi, final byte[] scanRecord) {
//            int startByte = 2;
//            boolean patternFound = false;
//            while (startByte <= 5) {
//                Log.i (TAG, "strat byte");
//                if (((int) scanRecord[startByte + 2] & 0xff) == 0x02 && //Identifies an iBeacon
//                        ((int) scanRecord[startByte + 3] & 0xff) == 0x15) { //Identifies correct data length
//                    patternFound = true;
//                    break;
//                }
//                startByte++;
//            }
//
//            if (patternFound) {
//                //Convert to hex String
//                byte[] uuidBytes = new byte[16];
//                System.arraycopy (scanRecord, startByte + 4, uuidBytes, 0, 16);
//                String hexString = bytesToHex (uuidBytes);
//
//                //UUID detection
//                String uuid = hexString.substring (0, 8) + "-" +
//                        hexString.substring (8, 12) + "-" +
//                        hexString.substring (12, 16) + "-" +
//                        hexString.substring (16, 20) + "-" +
//                        hexString.substring (20, 32);
//
//                // major
//                final int major = (scanRecord[startByte + 20] & 0xff) * 0x100 + (scanRecord[startByte + 21] & 0xff);
//
//                // minor
//                final int minor = (scanRecord[startByte + 22] & 0xff) * 0x100 + (scanRecord[startByte + 23] & 0xff);
//
//                Log.i (TAG, "UUID: " + uuid + "\\nmajor: " + major + "\\nminor" + minor);
//            } else {
//                Log.i (TAG, "not found");
//            }
//
//        }
//    };
//
//    /**
//     * bytesToHex method
//     */
//    static final char[] hexArray = "0123456789ABCDEF".toCharArray ();
//
//    private static String bytesToHex(byte[] bytes) {
//        char[] hexChars = new char[bytes.length * 2];
//        for (int j = 0; j < bytes.length; j++) {
//            int v = bytes[j] & 0xFF;
//            hexChars[j * 2] = hexArray[v >>> 4];
//            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
//        }
//        return new String (hexChars);
//    }
//
//    @Override
//    public void onClick(View view) {
//        int id = view.getId ();
//        switch (id) {
//            case R.id.checkinBtn:
//                onCheckInClicked ();
//                break;
//            case R.id.beaconBtn:
//                onBeaconClicked ();
//                break;
//        }
//    }
//
//    private void onBeaconClicked() {
//        if (getBlueToothOn ()) {
//            Log.i (TAG, "isBlueToothOn");
//            transmitIBeacon ();
//        } else if (!isBluetoothLEAvailable ()) {
//            UiHelper.showMessage (MainActivity.this, "Bluetooth not available on your device");
//        } else {
//            Log.i (TAG, "BlueTooth is off");
//            UiHelper.showInformationMessage (MainActivity.this, "Enable Bluetooth", "Please enable bluetooth before transmit iBeacon.",
//                    false, new DialogInterface.OnClickListener () {
//                        @Override
//                        public void onClick(DialogInterface dialogInterface, int i) {
//                            if (i == DialogInterface.BUTTON_POSITIVE) {
//                                Intent enableIntent = new Intent (BluetoothAdapter.ACTION_REQUEST_ENABLE);
//                                startActivityForResult (enableIntent, 1);
//                            }
//                        }
//                    });
//        }
//    }
//
//    private void transmitIBeacon() {
//        boolean isSupported = false;
//        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
//            isSupported = btAdapter.isMultipleAdvertisementSupported ();
//            if (isSupported) {
//
//                Log.v (TAG, "is support advertistment");
//                if (beaconTransmitter.isStarted ()) {
//                    beaconTransmitter.stopAdvertising ();
//                    beaconSwitch.setImageDrawable (getDrawable (R.drawable.off_icon));
//                } else {
//                    beaconTransmitter.startAdvertising (beacon, new AdvertiseCallback () {
//
//                        @Override
//                        public void onStartFailure(int errorCode) {
//                            Log.e (TAG, "Advertisement start failed with code: " + errorCode);
//                        }
//
//                        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
//                        @Override
//                        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
//                            Log.i (TAG, "Advertisement start succeeded." + settingsInEffect.toString ());
//                        }
//                    });
//                    beaconSwitch.setImageDrawable (getDrawable (R.drawable.on_icon));
//                }
//            } else {
//                UiHelper.showMessage (MainActivity.this, "Your device is not support leBluetooth.");
//            }
//        } else {
//            UiHelper.showMessage (MainActivity.this, "Your device is not support leBluetooth.");
//        }
//    }
//
//    private void onCheckInClicked() {
//        if (getBlueToothOn ()) {
//            Log.i (TAG, "isBlueToothOn");
//            checkin ();
//        } else if (!isBluetoothLEAvailable ()) {
//            UiHelper.showMessage (MainActivity.this, "Bluetooth not available on your device");
//        } else {
//            Log.i (TAG, "BlueTooth is off");
//            UiHelper.showInformationMessage (MainActivity.this, "Enable Bluetooth", "Please enable bluetooth before check in.",
//                    false, new DialogInterface.OnClickListener () {
//                        @Override
//                        public void onClick(DialogInterface dialogInterface, int i) {
//                            if (i == DialogInterface.BUTTON_POSITIVE) {
//                                Intent enableIntent = new Intent (BluetoothAdapter.ACTION_REQUEST_ENABLE);
//                                startActivityForResult (enableIntent, 0);
//                            }
//                        }
//                    });
//        }
//    }
//
//    private void checkin() {
//        Log.i (TAG, "checking in");
//        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
//            beaconManager.bind (this);
//            UiHelper.showLoading (MainActivity.this);
//        } else {
//            scanHandler.post (scanRunnable);
//        }
//    }
//
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult (requestCode, resultCode, data);
//        if (resultCode == RESULT_OK && requestCode == 0) {
//            checkin ();
//        } else if (resultCode == RESULT_OK && requestCode == 1) {
//            transmitIBeacon ();
//        } else {
//            Log.e (TAG, "resulit not ok");
//        }
//    }
//
//    @Override
//    public void onBeaconServiceConnect() {
//        beaconManager.addMonitorNotifier (new MonitorNotifier () {
//            @Override
//            public void didEnterRegion(Region region) {
//                Log.i (TAG, "I just saw an beacon for the first time! UUID : " + region.getId1 ().toString () + " minor : " + region.getId2 () + " major : " + region.getId3 ());
//                if (region.getId1 ().toString ().toUpperCase ().equals (getString (R.string.beacon_uuid_2)) && !isShow) {
//                    UiHelper.dismissLoading ();
//                    runOnUiThread (new Runnable () {
//                        @Override
//                        public void run() {
//                            UiHelper.showCustomConfirmDialog (MainActivity.this, "Do you want to Stamp Time at  " + date, new DialogInterface.OnClickListener () {
//                                @Override
//                                public void onClick(DialogInterface dialogInterface, int i) {
//                                    if (i == DialogInterface.BUTTON_POSITIVE) {
//                                        Toast.makeText (MainActivity.this, "Call service", Toast.LENGTH_LONG).show ();
//                                        isShow = false;
//                                    }
//                                }
//                            });
//                        }
//                    });
//                    isShow = true;
//                }
//            }
//
//            @Override
//            public void didExitRegion(Region region) {
//                Log.i (TAG, "I no longer see an beacon");
//                UiHelper.dismissLoading ();
//                runOnUiThread (new Runnable () {
//                    @Override
//                    public void run() {
//                        Toast.makeText (MainActivity.this, "Beacon signal is lost", Toast.LENGTH_LONG).show ();
//                    }
//                });
//            }
//
//            @Override
//            public void didDetermineStateForRegion(int state, Region region) {
//                Log.i (TAG, "I have just switched from seeing/not seeing beacons: " + state);
//            }
//        });
//
//        beaconManager.addRangeNotifier (new RangeNotifier () {
//            @Override
//            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
//                if (beacons.size () > 0) {
//                    Log.i (TAG, "The first beacon I see is about " + beacons.iterator ().next ().getDistance () + " meters away. major : " + region.getId2 ());
//                }
//            }
//        });
//
//        try {
//            Region region1 = new Region ("myIdentifier1", Identifier.parse (getString (R.string.beacon_uuid_2)), Identifier.parse ("12"), Identifier.parse ("14"));
//            Region region2 = new Region ("myIdentifier2", Identifier.parse (getString (R.string.beacon_uuid_1)), null, null);
//            beaconManager.startMonitoringBeaconsInRegion (region1);
//            beaconManager.startMonitoringBeaconsInRegion (region2);
//        } catch (RemoteException e) {
//        }
//    }
//
//    @Override
//    protected void onDestroy() {
//        super.onDestroy ();
//        beaconManager.unbind (this);
//    }
//}
