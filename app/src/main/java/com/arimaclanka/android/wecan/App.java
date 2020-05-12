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

package com.arimaclanka.android.wecan;

import android.app.Application;
import android.content.Intent;

import com.arimaclanka.android.wecan.activities.SplashActivity;
import com.arimaclanka.android.wecan.models.Me;
import com.arimaclanka.android.wecan.utils.Settings;
import com.onesignal.OneSignal;

import org.json.JSONException;

public class App extends Application {
    private Settings settings;
    private Me me;

    @Override
    public void onCreate() {
        super.onCreate();
//        **** Uncomment below part after changing OneSignal app id ****

/*        OneSignal.startInit(this)
                .inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)
                .setNotificationReceivedHandler(notificationReceivedHandler)
                .init();
                */
    }

    private OneSignal.NotificationReceivedHandler notificationReceivedHandler = notification -> {
        try {
            if (notification.payload.additionalData.getString("notification_type").equalsIgnoreCase("upload_request")) {
                getSettings().setUploadRequired(true);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    };

    public Settings getSettings() {
        if (settings == null) {
            settings = new Settings(getApplicationContext());
        }
        return settings;
    }

    public Me getMe() {
        return me;
    }

    public void setMe(Me me) {
        this.me = me;
    }

    public void clearData() {
        try {
            getSettings().setSecretKey("");
        } catch (Exception e) {
            e.printStackTrace();
        }
        getSettings().setAccessToken("");
        getSettings().setRefreshToken("");
        getSettings().setUserId("");
    }

    private void logout() {
        clearData();
        Intent intent = new Intent(this, SplashActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }
}
