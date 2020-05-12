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

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.arimaclanka.android.wecan.App;
import com.arimaclanka.android.wecan.R;
import com.arimaclanka.android.wecan.activities.SplashActivity;
import com.arimaclanka.android.wecan.models.BleDevice;
import com.arimaclanka.android.wecan.models.Entry;
import com.arimaclanka.android.wecan.utils.DatabaseHelper;
import com.arimaclanka.android.wecan.utils.Logger;
import com.arimaclanka.android.wecan.utils.Utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.List;

public class BleService extends Service implements BleScannerCallback, BleAdvertiserCallback {
    private static final String TAG = "BleService";
    private static final String STOP_ACTION = "com.arimaclanka.android.wecan.bluetooth.BleService.STOP";
    private static final int NOTIFICATION_ID_1 = 100002;
    private static final String NOTIFICATION_CHANNEL_ID = "WeCan";
    private static final String NOTIFICATION_CHANNEL_NAME = "WeCan";
    private static final String NOTIFICATION_CHANNEL_DESC = "WeCan";
    private static final int REQ_NOTIFICATION_CLICK = 8755;
    private App app;
    private BleManager mBleManager;
    private BleAdvertiser mBleAdvertiser;
    private BleScanner mBleScanner;
    private DecimalFormat mDecimalFormat = new DecimalFormat("0.0000");

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        app = (App) getApplication();
        mDecimalFormat.setRoundingMode(RoundingMode.UP);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.getAction() != null && intent.getAction().equalsIgnoreCase(STOP_ACTION)) {
            stopSelf();
            return super.onStartCommand(intent, flags, startId);
        }
        Logger.d(TAG, "BLE service starting on " + Build.BRAND + " " + Build.MODEL);
        if (mBleManager == null) {
            mBleManager = new BleManager(this);
        }
        if (mBleManager.isBluetoothReady()) {
            try {
                mBleManager.initialize(app.getSettings().getUserId(), app.getSettings().getSecretKey());
                if (mBleScanner == null) {
                    mBleScanner = new BleScanner(mBleManager, this);
                }
                if (mBleAdvertiser == null) {
                    mBleAdvertiser = new BleAdvertiser(mBleManager, this);
                }
                mBleScanner.start();
                mBleAdvertiser.start();
                updateNotification(getString(R.string.scanning));
            } catch (Exception e) {
                e.printStackTrace();
                Logger.exception(TAG, e);
            }
        } else {
            if (BleManager.isBluetoothSupported()) {
                Logger.d(TAG, "Ble not supported");
                sendNotification(getString(R.string.service_notification_title), getString(R.string.scanner_not_running_service_msg));
            } else {
                Logger.d(TAG, "Bluetooth not ready");
                stopSelf();
            }
        }
        return START_STICKY;
    }

    public void sendNotification(String title, String message) {
        try {
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (Build.VERSION.SDK_INT >= 26) {
                NotificationChannel mChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, NOTIFICATION_CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
                if (notificationManager != null) {
                    notificationManager.createNotificationChannel(mChannel);
                }
            }
            Intent i = new Intent(this, SplashActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, REQ_NOTIFICATION_CLICK, i, PendingIntent.FLAG_UPDATE_CURRENT);
            Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                    .setSmallIcon(R.drawable.baseline_bluetooth_searching_white_24)
                    .setContentTitle(title)
                    .setAutoCancel(true)
                    .setSound(defaultSoundUri)
                    .setPriority(Notification.PRIORITY_MAX)
                    .setDefaults(Notification.DEFAULT_ALL)
                    .setContentIntent(pendingIntent);
            notificationBuilder.setContentText(message);
            if (notificationManager != null) {
                notificationManager.notify(1, notificationBuilder.build());
            }
        } catch (Exception e) {
            e.printStackTrace();
            Logger.exception(TAG, e);
        }
    }

    private void updateNotification(String message) {
        try {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
            builder.setContentTitle(getString(R.string.service_notification_title));
            builder.setContentText(message);
            builder.setSmallIcon(R.drawable.baseline_bluetooth_searching_white_24);

            Intent intentStop = new Intent(this, BleService.class);
            intentStop.setAction(STOP_ACTION);
            PendingIntent piStop = PendingIntent.getService(this, 0, intentStop, 0);
            builder.addAction(R.drawable.baseline_stop_black_24, getString(R.string.stop), piStop);

            Notification notification = builder.build();

            if (Build.VERSION.SDK_INT >= 26) {
                NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, NOTIFICATION_CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
                channel.setDescription(NOTIFICATION_CHANNEL_DESC);
                channel.setSound(null, null);
                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                if (notificationManager != null) {
                    notificationManager.createNotificationChannel(channel);
                }
            }
            startForeground(NOTIFICATION_ID_1, notification);
        } catch (Exception e) {
            e.printStackTrace();
            Logger.exception(TAG, e);
        }
    }

    @Override
    public void onScanningComplete(List<BleDevice> devices) {
        if (devices != null && devices.size() > 0) {
            long timestamp = System.currentTimeMillis();
            for (BleDevice device : devices) {
                try {
                    String data = device.getData();
                    if (data != null) {
                        Entry entry = new Entry();
                        String[] dataParts = data.split(":");
                        entry.setUserId(dataParts[0]);
                        entry.setTimeCode(dataParts[1]);
                        entry.setSignature(dataParts[2]);
                        entry.setCreatedAt(String.valueOf(timestamp));
                        BigDecimal distance = new BigDecimal(Utils.calculateDistance(device.getRssi(), device.getTxPower()));
                        entry.setDistance(mDecimalFormat.format(distance));
                        entry.setModel(device.getModel());
                        entry.setStatus(0);
                        DatabaseHelper.getInstance(BleService.this).addEntry(entry);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Logger.exception(TAG, e);
                }
            }
        }
    }

    @Override
    public synchronized void onBleDeviceDataReceived(BleDevice device) {
        try {
            long timestamp = System.currentTimeMillis();
            String data = device.getData();
            Entry entry = new Entry();
            String[] dataParts = data.split(":");
            entry.setUserId(dataParts[0]);
            entry.setTimeCode(dataParts[1]);
            entry.setSignature(dataParts[2]);
            entry.setModel(device.getModel());
            entry.setCreatedAt(String.valueOf(timestamp));
            BigDecimal distance = new BigDecimal(Utils.calculateDistance(device.getRssi(), device.getTxPower()));
            entry.setDistance(mDecimalFormat.format(distance));
            entry.setStatus(0);
            DatabaseHelper.getInstance(BleService.this).addEntry(entry);
        } catch (Exception e) {
            e.printStackTrace();
            Logger.exception(TAG, e);
        }
    }

    @Override
    public void onDestroy() {
        if (mBleScanner != null) {
            mBleScanner.stop();
        }
        if (mBleAdvertiser != null) {
            mBleAdvertiser.stop();
        }
        super.onDestroy();
    }

}
