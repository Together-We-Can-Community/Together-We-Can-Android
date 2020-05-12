/*
  Copyright (c) 2020, Arimac Lanka (PVT) Ltd.

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
*/

package com.arimaclanka.android.wecan.bluetooth;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.os.Handler;
import android.os.ParcelUuid;

import androidx.annotation.NonNull;

import com.arimaclanka.android.wecan.models.BleDevice;
import com.arimaclanka.android.wecan.utils.Logger;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import no.nordicsemi.android.support.v18.scanner.BluetoothLeScannerCompat;
import no.nordicsemi.android.support.v18.scanner.ScanCallback;
import no.nordicsemi.android.support.v18.scanner.ScanFilter;
import no.nordicsemi.android.support.v18.scanner.ScanRecord;
import no.nordicsemi.android.support.v18.scanner.ScanResult;
import no.nordicsemi.android.support.v18.scanner.ScanSettings;

public class BleScanner {
    private static final String TAG = "BleScanner";
    private BleManager mBleManager;
    private BluetoothLeScannerCompat mBluetoothLeScanner;
    private BleScannerCallback mBleScannerCallback;
    private Handler mHandlerScanDuration = new Handler();
    private Handler mHandlerScanInterval = new Handler();
    private Handler mHandlerDiscoveryTimeout = new Handler();
    private List<BleDevice> mDevices = new ArrayList<>();
    private BluetoothGatt mBluetoothGatt;
    private int mCurrentIndex = -1;
    private boolean isDiscovering = false;

    public BleScanner(BleManager bleManager, BleScannerCallback bleScannerCallback) {
        this.mBleManager = bleManager;
        this.mBleScannerCallback = bleScannerCallback;
        mBluetoothLeScanner = BluetoothLeScannerCompat.getScanner();
    }

    private void startScanIntervalTimer() {
        stopScanIntervalTimer();
        mHandlerScanInterval.postDelayed(mScanIntervalCallback, Consts.SCAN_INTERVAL);
    }

    private void stopScanIntervalTimer() {
        mHandlerScanInterval.removeCallbacks(mScanIntervalCallback);
    }

    private Runnable mScanIntervalCallback = this::startScanning;

    public void start() {
        startScanning();
    }

    private void startScanning() {
        Logger.d(TAG, "startScanning");
        stopScanIntervalTimer();
        mCurrentIndex = -1;
        mDevices.clear();
        ScanSettings settings = new ScanSettings.Builder()
                .setLegacy(false)
                .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
                .setUseHardwareBatchingIfSupported(true)
                .build();
        List<ScanFilter> filters = new ArrayList<>();

     /* While operating in the background, iOS removes service UUIDs from advertising packet and place it in a special overflow area. This makes it only detectable by
        another iOS device. But during our research we found that in Android it's still detectable if we do wildcard scan (Without specifying a service uuid, which will
        return all the devices). But Android doesn't allow you to run wildcard scan in the background. To overcome this, we have combined the following scan filters,
            1. Scan using service UUID
            2. Scan using Apple manufacturer id (76) with or without manufacturer data

        This method has its own cons like it returns all the Apple devices (Macbooks, watch etc) around you, but we have have come to a conclusion that this way is
        better than requesting the iOS user to keep the app open at all times.
     */

        //Apple manufacturer id based scan filters
        try {
            //Load scan filters from Firebase remote config
            JSONObject jsonObject = new JSONObject(FirebaseRemoteConfig.getInstance().getString("android_scanner_config"));
            JSONArray jsonArray = jsonObject.getJSONArray("filters");
            int count = jsonArray.length();
            for (int i = 0; i < count; i++) {
                String filter = jsonArray.getString(i);
                if (filter.equalsIgnoreCase("")) {
                    filters.add(new ScanFilter.Builder().setManufacturerData(76, new byte[]{}).build());
                    Logger.i(TAG, "Scan filter: *");
                } else {
                    filters.add(new ScanFilter.Builder().setManufacturerData(76, filter.getBytes()).build());
                    Logger.i(TAG, "Scan filter: " + filter);
                }
            }
        } catch (Exception e) {
            Logger.exception(TAG, e);
        }
        //In case remote config doesn't give filters app goes to default Apple filter
        if (filters.size() == 0) {
            filters.add(new ScanFilter.Builder().setManufacturerData(76, new byte[]{}).build());
            Logger.i(TAG, "Scan filter: default");
        }
        //Service UUID based scan filter
        filters.add(new ScanFilter.Builder().setServiceUuid(new ParcelUuid(Consts.SERVICE_UUID)).build());

        mBluetoothLeScanner.startScan(filters, settings, callback);
        startScanDurationTimer();
    }

    private void startScanDurationTimer() {
        stopScanDurationTimer();
        mHandlerScanDuration.postDelayed(mScanDurationCallback, Consts.SCAN_DURATION);
    }

    private void stopScanDurationTimer() {
        mHandlerScanDuration.removeCallbacks(mScanDurationCallback);
    }

    private Runnable mScanDurationCallback = this::stopScanning;

    private void stopScanning() {
        stopScanDurationTimer();
        try {
            mBluetoothLeScanner.stopScan(callback);
        } catch (Exception e) {
            e.printStackTrace();
            Logger.exception(TAG, e);
        }
        processNextDevice();
    }

    private ScanCallback callback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, @NonNull ScanResult result) {
            super.onScanResult(callbackType, result);
            addDevice(result);
        }

        @Override
        public void onBatchScanResults(@NonNull List<ScanResult> results) {
            super.onBatchScanResults(results);
            for (ScanResult result : results) {
                addDevice(result);
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            Logger.e(TAG, "onScanFailed : Code=" + errorCode);
        }
    };

    private synchronized void addDevice(ScanResult result) {
        int existingId = -1;
        int count = mDevices.size();
        for (int i = 0; i < count; i++) {
            BleDevice bleDevice = mDevices.get(i);
            if (bleDevice.getBluetoothDevice().getAddress().equalsIgnoreCase(result.getDevice().getAddress())) {
                existingId = i;
                break;
            }
        }
        if (existingId == -1) {
            BleDevice device = new BleDevice();
            device.setBluetoothDevice(result.getDevice());
            device.setRssi(result.getRssi());
            device.setTxPower(result.getTxPower());
            device.setPriority(getPriority(result.getScanRecord()));
            mDevices.add(device);
            Logger.d(TAG, "New device added " + device.getBluetoothDevice().getAddress());
        } else {
            mDevices.get(existingId).setRssi(result.getRssi());
            mDevices.get(existingId).setTxPower(result.getTxPower());
        }
    }

    private int getPriority(ScanRecord record) {
        //Set priority value to detected device
        try {
            if (record != null) {
                //Devices which contains our service UUID gets highest priority
                if (record.getServiceUuids() != null && record.getServiceUuids().contains(new ParcelUuid(Consts.SERVICE_UUID))) {
                    return 10;
                }
                //Devices with Apple manufacturer id and manufacturer data begins with value "1" gets the next priority
                byte[] data = record.getManufacturerSpecificData(76);
                if (data != null && data.length > 0) {
                    if (data[0] == 1) {
                        return 8;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 1;
    }

    private static class SortByPriority implements Comparator<BleDevice> {

        @Override
        public int compare(BleDevice lhs, BleDevice rhs) {
            return Integer.compare(rhs.getPriority(), lhs.getPriority());
        }
    }

    private void processNextDevice() {
        stopDiscoveryTimeout();
        //Sort the device list according to given priority
        Collections.sort(mDevices, new SortByPriority());
        if (!isDiscovering) {
            mCurrentIndex = mCurrentIndex + 1;
            if (mCurrentIndex < mDevices.size()) {
                isDiscovering = true;
                BleDevice device = mDevices.get(mCurrentIndex);
                mBluetoothGatt = device.getBluetoothDevice().connectGatt(mBleManager.getContext(), false, mBluetoothGattCallback);
                startDiscoveryTimeout();
            } else {
                submitDetectedDevices();
            }
        }
    }

    private void startDiscoveryTimeout() {
        stopDiscoveryTimeout();
        mHandlerDiscoveryTimeout.postDelayed(mDiscoveryTimeoutCallback, Consts.DISCOVERY_TIMEOUT);
    }

    private void stopDiscoveryTimeout() {
        mHandlerDiscoveryTimeout.removeCallbacks(mDiscoveryTimeoutCallback);
    }

    private void restartDiscoveryTimeout() {
        mHandlerDiscoveryTimeout.removeCallbacks(mDiscoveryTimeoutCallback);
    }

    private Runnable mDiscoveryTimeoutCallback = () -> {
        Logger.d(TAG, "Discovery timed out");
        finishDiscovery();
    };

    private final BluetoothGattCallback mBluetoothGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Logger.d(TAG, "Connected with : " + gatt.getDevice().getAddress());
                mBluetoothGatt.requestMtu(256);
            }
        }

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            super.onMtuChanged(gatt, mtu, status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                mBluetoothGatt.discoverServices();
            } else {
                Logger.e(TAG, "onMtuChanged : failed");
                finishDiscovery();
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            BluetoothGattService bluetoothGattService = null;
            //Check whether device has our service
            for (BluetoothGattService service : gatt.getServices()) {
                if ((service == null) || (service.getUuid() == null)) {
                    continue;
                }
                if (Consts.SERVICE_UUID.toString().equalsIgnoreCase(service.getUuid().toString())) {
                    bluetoothGattService = service;
                }
            }
            if (bluetoothGattService != null) {
                gatt.readCharacteristic(bluetoothGattService.getCharacteristics().get(0));
            } else {
                Logger.d(TAG, "Service not found on " + gatt.getDevice().getAddress());
                finishDiscovery();
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                try {
                    if (Consts.READ_CHARACTERISTIC_UUID.toString().equalsIgnoreCase(characteristic.getUuid().toString())) {
                        String value = characteristic.getStringValue(0);
                        Logger.d(TAG, "Data read : " + value);
                        try {
                            String[] data = value.split(":");
                            if (data.length == 4) {
                                mDevices.get(mCurrentIndex).setData(data[0] + ":" + data[1] + ":" + data[2]);
                                mDevices.get(mCurrentIndex).setModel(data[3]);
                            } else {
                                Logger.d(TAG, "Data read: data parse failed");
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            Logger.e(TAG, "Data read error : " + e.getMessage());
                        }
                        restartDiscoveryTimeout();
                        writeValue(gatt.getDevice().getAddress());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Logger.exception(TAG, e);
                }
            }
        }

        private void writeValue(String address) {
            BleDevice bleDevice = null;
            for (BleDevice device : mDevices) {
                if (device.getBluetoothDevice().getAddress().equalsIgnoreCase(address)) {
                    bleDevice = device;
                    break;
                }
            }
            try {
                if (bleDevice != null) {
                    BluetoothGattService service = mBluetoothGatt.getService(Consts.SERVICE_UUID);
                    BluetoothGattCharacteristic characteristic = service.getCharacteristic(Consts.WRITE_CHARACTERISTIC_UUID);
                    String data = mBleManager.getAdvertisingPayload() + ":" + bleDevice.getRssi() + ":" + bleDevice.getTxPower();
                    characteristic.setValue(data);
                    mBluetoothGatt.writeCharacteristic(characteristic);
                    Logger.d(TAG, "Data sent : " + data);
                }
            } catch (Exception e) {
                e.printStackTrace();
                Logger.e(TAG, "Data sent error : " + e.getMessage());
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            finishDiscovery();
        }
    };

    private void finishDiscovery() {
        if (mBluetoothGatt != null) {
            mBluetoothGatt.disconnect();
            mBluetoothGatt.close();
        }
        mBluetoothGatt = null;
        isDiscovering = false;
        processNextDevice();
    }

    private void submitDetectedDevices() {
        if (mBleScannerCallback != null) {
            mBleScannerCallback.onScanningComplete(mDevices);
        }
        mDevices = new ArrayList<>();
        startScanIntervalTimer();
    }

    public void stop() {
        stopScanIntervalTimer();
        stopScanDurationTimer();
        stopScanning();
    }

}
