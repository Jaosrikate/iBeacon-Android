# iBeacon

Just install the apps in 2 devices for simulated the role of scanner and broadcaster :) 

Build from Android Studio 3.5 tested on Xiaomi MI MIX3 and Samsung galaxy S8

# App Demo


This app shows the example about iBeacon simulator is a company device (employer) and Scanner is an employee's device that check-in when arrived the area of the company.


iBeacon Simulator -  touch the iBeacon stone for start broadcasting

<img src="https://github.com/Jaosrikate/iBeacon-Android/blob/master/demo/simulatior.gif?raw=true" width="250">

iBeacon Scanner - Click the scan button for scan iBeacon (that broadcasting from this Apps.)

<img src="https://github.com/Jaosrikate/iBeacon-Android/blob/master/demo/scanner.gif?raw=true" width="250">


## Simulator
Referance Library : https://github.com/AltBeacon/android-beacon-library

```
//Init beacon device
beacon = new Beacon.Builder()
                .setId1(getString(R.string.beacon_uuid_simulator)) // UUID for beacon
                .setId2(getString(R.string.beacon_major_simulator)) // Major for beacon
                .setId3(getString(R.string.beacon_minor_simulator)) // Minor for beacon
                .setManufacturer(0x004C) // Radius Networks.0x0118  Change this for other beacon layouts//0x004C for iPhone
                .setTxPower(-56) // Power in dB
                .setDataFields(Arrays.asList(new Long[]{0l})) // Remove this for beacon layouts without d: fields
                .build();
```
Beacon layout list : https://beaconlayout.wordpress.com/
```
//set iBeacon layout
beaconTransmitter = new BeaconTransmitter (getActivity(), new BeaconParser()
                .setBeaconLayout ("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"));
```

```
//start and stop beacon trasmitter
if (beaconTransmitter.isStarted()) {
    beaconTransmitter.stopAdvertising();
} else {
    beaconTransmitter.startAdvertising(beacon, new AdvertiseCallback() {
      @Override
      public void onStartFailure(int errorCode) {
            Log.e(TAG, "Advertisement start failed with code: " + errorCode);
      }

      @TargetApi(Build.VERSION_CODES.LOLLIPOP)
      @Override
      public void onStartSuccess(AdvertiseSettings settingsInEffect) {
        Log.i(TAG, "Advertisement start succeeded." + settingsInEffect.toString());
      }
    });
}
```

## Scanner

Referance : https://github.com/kiteflo/iBeaconAndroidDemo/

Example Scanner : TimeAttendantFastFragment.java

```
        btManager = (BluetoothManager) getActivity().getSystemService(Context.BLUETOOTH_SERVICE);
        btAdapter = btManager.getAdapter();
```

```
\\init LE scanner for API Level >= 21
        if (Build.VERSION.SDK_INT >= 21) {
            mLEScanner = btAdapter.getBluetoothLeScanner();
            settings = new ScanSettings.Builder()
                    .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                    .build();
            filters = new ArrayList<>();
        }  
```
```
mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (Build.VERSION.SDK_INT < 21) {
                        Log.i(TAG, "runnable stop SDK_INT < 21");
                        btAdapter.stopLeScan(leScanCallback);
                    } else {
                        Log.i(TAG, "runnable stop SDK_INT >= 21");
                        mLEScanner.stopScan(mScanCallback);
                    }
                }
            }, SCAN_PERIOD);
if (Build.VERSION.SDK_INT < 21) {
     btAdapter.startLeScan(leScanCallback);
} else {
     mLEScanner.startScan(filters, settings, mScanCallback);
}
```
#### Scancallback - Retrieved ScanResult to Byte

```
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
```  
#### LeScanCallback
```
  private BluetoothAdapter.LeScanCallback leScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, final int rssi, final byte[] scanRecord) {
            findBeaconPattern(scanRecord);
        }
    };
```

#### Find Beacon pattern and Convert to a Beacon profile
```
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
        }
    }
 ```
