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

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.os.Handler;
import android.os.ParcelUuid;

import com.arimaclanka.android.wecan.models.BleDevice;
import com.arimaclanka.android.wecan.utils.Logger;

import java.security.SecureRandom;
import java.util.Arrays;

public class BleAdvertiser {
    private static final String TAG = "BleAdvertiser";
    private BleManager mBleManager;
    private BluetoothGattCharacteristic mDataReadCharacteristic;
    private AdvertiseData mAdvData;
    private AdvertiseData mAdvScanResponse;
    private AdvertiseSettings mAdvSettings;
    private BluetoothLeAdvertiser mAdvertiser;
    private BluetoothGattServer mGattServer;
    private BleAdvertiserCallback mBleAdvertiserCallback;
    private Handler mHandlerAdvPayloadTimer = new Handler();

    public BleAdvertiser(BleManager bleManager, BleAdvertiserCallback bleAdvertiserCallback) {
        this.mBleManager = bleManager;
        this.mBleAdvertiserCallback = bleAdvertiserCallback;
        BluetoothGattService mService = new BluetoothGattService(Consts.SERVICE_UUID, BluetoothGattService.SERVICE_TYPE_PRIMARY);
        mDataReadCharacteristic = new BluetoothGattCharacteristic(Consts.READ_CHARACTERISTIC_UUID, BluetoothGattCharacteristic.PROPERTY_READ,
                BluetoothGattCharacteristic.PERMISSION_READ);
        mService.addCharacteristic(mDataReadCharacteristic);
        BluetoothGattCharacteristic mDataWriteCharacteristic = new BluetoothGattCharacteristic(Consts.WRITE_CHARACTERISTIC_UUID, BluetoothGattCharacteristic.PROPERTY_WRITE,
                BluetoothGattCharacteristic.PERMISSION_WRITE);
        mService.addCharacteristic(mDataWriteCharacteristic);
        mAdvSettings = new AdvertiseSettings.Builder()
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
                .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM)
                .setConnectable(true)
                .build();
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[3];
        random.nextBytes(bytes);
        mAdvData = new AdvertiseData.Builder()
                .setIncludeTxPowerLevel(true)
                .setIncludeDeviceName(false)
                .addServiceUuid(new ParcelUuid(Consts.SERVICE_UUID))
                .addManufacturerData(5000, bytes) //This will ensure iOS uniquely detect Android device
                .build();
        mAdvScanResponse = new AdvertiseData.Builder()
                .setIncludeDeviceName(true)
                .build();
        mGattServer = mBleManager.getBluetoothManager().openGattServer(mBleManager.getContext(), mGattServerCallback);
        mGattServer.addService(mService);
        mAdvertiser = mBleManager.getBluetoothManager().getAdapter().getBluetoothLeAdvertiser();
        setAdvertisingPayload();
    }

    public void setAdvertisingPayload() {
        mDataReadCharacteristic.setValue(mBleManager.getAdvertisingPayload());
    }

    public void start() {
        setAdvertisingPayload();
        mAdvertiser.startAdvertising(mAdvSettings, mAdvData, mAdvScanResponse, mAdvCallback);
        startAdvPayloadChangeTimer();
    }

    private void startAdvPayloadChangeTimer() {
        mHandlerAdvPayloadTimer.postDelayed(mAdvPayloadChangeCallback, Consts.ADV_PAYLOAD_LIFE + (60 * 1000));
    }

    private void stopAdvPayloadChangeTimer() {
        mHandlerAdvPayloadTimer.removeCallbacks(mAdvPayloadChangeCallback);
    }

    private Runnable mAdvPayloadChangeCallback = this::changeAdvPayload;

    private void changeAdvPayload() {
        stopAdvPayloadChangeTimer();
        setAdvertisingPayload();
        startAdvPayloadChangeTimer();
    }

    private final AdvertiseCallback mAdvCallback = new AdvertiseCallback() {
        @Override
        public void onStartFailure(int errorCode) {
            super.onStartFailure(errorCode);
            switch (errorCode) {
                case ADVERTISE_FAILED_ALREADY_STARTED:
                    break;
                case ADVERTISE_FAILED_DATA_TOO_LARGE:
                    Logger.e(TAG, "Advertiser starting failed: Data too large");
                    break;
                default:
                    Logger.e(TAG, "Advertiser starting failed: unhandled error: " + errorCode);
            }
        }

        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            super.onStartSuccess(settingsInEffect);
            Logger.d(TAG, "Broadcasting");
        }
    };

    @SuppressWarnings("FieldCanBeLocal")
    private BluetoothGattServerCallback mGattServerCallback = new BluetoothGattServerCallback() {

        @Override
        public void onCharacteristicReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicReadRequest(device, requestId, offset, characteristic);
            byte[] value = characteristic.getValue();
            try {
                String strData = new String(value);
                Logger.d(TAG, "onCharacteristicReadRequest: " + strData);
            } catch (Exception e) {
                e.printStackTrace();
                Logger.exception(TAG, e);
            }
            value = Arrays.copyOfRange(value, offset, value.length);
            mGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, value);
        }

        @Override
        public void onCharacteristicWriteRequest(BluetoothDevice device, int requestId, BluetoothGattCharacteristic characteristic, boolean preparedWrite, boolean responseNeeded, int offset, byte[] value) {
            super.onCharacteristicWriteRequest(device, requestId, characteristic, preparedWrite, responseNeeded, offset, value);
            String strData = "";
            try {
                strData = new String(value);
                Logger.d(TAG, "onCharacteristicWriteRequest: " + strData);
            } catch (Exception e) {
                e.printStackTrace();
                Logger.exception(TAG, e);
            }
            try {
                BleDevice bleDevice = new BleDevice();
                String[] data = strData.split(":");
                if (data.length == 6) {
                    bleDevice.setData(data[0] + ":" + data[1] + ":" + data[2]);
                    bleDevice.setModel(data[3]);
                    bleDevice.setRssi(Integer.parseInt(data[4]));
                    bleDevice.setTxPower(Integer.parseInt(data[5]));
                    bleDevice.setBluetoothDevice(device);
                    if (mBleAdvertiserCallback != null) {
                        mBleAdvertiserCallback.onBleDeviceDataReceived(bleDevice);
                    }
                } else {
                    Logger.d(TAG, "onCharacteristicWriteRequest: data parse failed");
                }
            } catch (Exception e) {
                e.printStackTrace();
                Logger.exception(TAG, e);
            }
            mGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, value);
        }
    };

    public void stop() {
        if (mAdvertiser != null) {
            mAdvertiser.stopAdvertising(mAdvCallback);
        }
        stopAdvPayloadChangeTimer();
    }

}
