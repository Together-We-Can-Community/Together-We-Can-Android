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

package com.arimaclanka.android.wecan.activities;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;

import com.arimaclanka.android.wecan.R;
import com.arimaclanka.android.wecan.bluetooth.BleManager;
import com.arimaclanka.android.wecan.utils.Alerts;

public class BluetoothActivity extends BaseActivity {
    private static final int PERMISSION_REQUEST_LOCATION = 1;
    private static final int PERMISSION_REQUEST_BACKGROUND_LOCATION = 2;
    private static final int REQ_ENABLE_BLUETOOTH = 52;
    private Button btnSetup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);
        btnSetup = findViewById(R.id.btnSetup);
        checkBluetooth();
    }

    private void checkBluetooth() {
        if (BleManager.isBluetoothReady(this)) {
            btnSetup.setText(getString(R.string.ble_ready));
        } else {
            btnSetup.setText(getString(R.string.ble_not_ready));
        }
    }

    public void onClick(View v) {
        if (v.getId() == R.id.btnSetup) {
            if (BleManager.isBluetoothReady(this)) {
                showNext();
            } else {
                if (BleManager.isBluetoothSupported()) {
                    setupBluetooth();
                } else {
                    Alerts.showSuccess(this, getString(R.string.bluetooth), getString(R.string.ble_not_supported), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            showNext();
                        }
                    });
                }
            }
        }
    }

    private void setupBluetooth() {
        if (!BleManager.hasPermission(this)) {
            requestPermission();
            return;
        }
        if (BleManager.isBluetoothSupported()) {
            if (!BleManager.isBluetoothEnabled()) {
                turnOnBluetooth();
                return;
            }
        }
        checkBluetooth();
    }

    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    if (this.checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        requestPermissions(new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION}, PERMISSION_REQUEST_BACKGROUND_LOCATION);
                    }
                }
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_BACKGROUND_LOCATION},
                            PERMISSION_REQUEST_BACKGROUND_LOCATION);
                } else {
                    requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_LOCATION);
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_REQUEST_LOCATION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    setupBluetooth();
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle(R.string.functionality_limited);
                    builder.setMessage(R.string.perm_not_granted_msg);
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.show();
                }
                return;
            }
            case PERMISSION_REQUEST_BACKGROUND_LOCATION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    setupBluetooth();
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle(R.string.functionality_limited);
                    builder.setMessage(R.string.perm_not_granted_msg);
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.show();
                }
            }
        }
    }

    private void turnOnBluetooth() {
        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(intent, REQ_ENABLE_BLUETOOTH);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQ_ENABLE_BLUETOOTH && resultCode == Activity.RESULT_OK) {
            setupBluetooth();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void showNext() {
        Intent intent = new Intent(this, JoinActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        finish();
    }
}
