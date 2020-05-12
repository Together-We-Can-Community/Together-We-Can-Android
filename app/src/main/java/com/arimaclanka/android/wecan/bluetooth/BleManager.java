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

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import com.arimaclanka.android.wecan.utils.Logger;
import com.arimaclanka.android.wecan.utils.Signature;
import com.arimaclanka.android.wecan.utils.Utils;

public class BleManager {
    private static final String TAG = "BleManager";
    private Context mContext;
    private BluetoothManager mBluetoothManager;
    private String mUserId;
    private String mUserSecretKey;
    private String mAdvertisingPayload = "";
    private long mLastPayloadGeneratedTime = 0;

    public BleManager(Context context) {
        this.mContext = context;
        mBluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
    }

    public void initialize(String userId, String userSecretKey) {
        this.mUserId = userId;
        this.mUserSecretKey = userSecretKey;
    }

    public static boolean isBluetoothSupported() {
        return BluetoothAdapter.getDefaultAdapter() != null;
    }

    public static boolean isBluetoothEnabled() {
        if (isBluetoothSupported()) {
            return BluetoothAdapter.getDefaultAdapter().isEnabled();
        } else {
            return false;
        }
    }

    public static boolean hasPermission(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    return context.checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED;
                } else {
                    return true;
                }
            } else {
                return false;
            }
        } else {
            return true;
        }
    }

    public static boolean isBluetoothReady(Context context) {
        return isBluetoothSupported() && isBluetoothEnabled() && hasPermission(context);
    }

    public boolean isBluetoothReady() {
        return isBluetoothSupported() && isBluetoothEnabled() && hasPermission(getContext());
    }


    public BluetoothManager getBluetoothManager() {
        return mBluetoothManager;
    }

    public Context getContext() {
        return mContext;
    }

    public String getAdvertisingPayload() {
/*      Generates advertising payload. This payload contain user's anonymous id, device time, and calculated signature. Signature get changed time to time
        and this ensure authenticity of data. Originally this payload was fully encrypted using a public key, but it started giving issues with low end
        devices due to the size of payload. So we have changed the payload this way. */
        try {
            long diff = System.currentTimeMillis() - mLastPayloadGeneratedTime;
            if (diff > Consts.ADV_PAYLOAD_LIFE) {
                mLastPayloadGeneratedTime = System.currentTimeMillis();
                String timeStamp = String.valueOf(mLastPayloadGeneratedTime / 1000);
                String data = mUserId + ":" + timeStamp;
                String signature = Signature.calculateSignature(mUserSecretKey, data);
                String model;
                if (Build.MODEL.length() > 12) {
                    model = Build.MODEL.substring(0, 12);
                } else {
                    model = Build.MODEL;
                }
                model = model.replace(" ", "").replace(":", "");
                String checkSum = String.valueOf(Utils.calculateCheckSum(data));
                mAdvertisingPayload = mUserId + ":" + timeStamp + ":" + signature + ":" + model;
                return mAdvertisingPayload;
            } else {
                return mAdvertisingPayload;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Logger.exception(TAG, e);
            return null;
        }
    }
}
