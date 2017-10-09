# iBeacon

Example android application for iBeacon simulator and scanner.

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
