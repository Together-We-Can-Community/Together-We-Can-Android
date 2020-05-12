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

package com.arimaclanka.android.wecan.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

@SuppressLint("ApplySharedPref")
public class Settings {
    private static final String CRYPTO_KEY = "settings_key";
    private static final String PREF_NAME = "com.arimaclanka.android.wecan.utils.settings";
    private Context context;

    public Settings(Context context) {
        this.context = context;
    }

    private SharedPreferences getPref() {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void setAccessToken(String value) {
        try {
            getPref().edit().putString("AccessToken", CryptoManager.newInstance(context, CRYPTO_KEY).encryptData(value)).commit();
        } catch (Exception e) {
            getPref().edit().putString("AccessToken", value).commit();
        }
    }

    public String getAccessToken() {
        try {
            return CryptoManager.newInstance(context, CRYPTO_KEY).decryptData(getPref().getString("AccessToken", ""));
        } catch (Exception e) {
            return getPref().getString("AccessToken", "");
        }
    }

    public void setRefreshToken(String value) {
        try {
            getPref().edit().putString("RefreshToken", CryptoManager.newInstance(context, CRYPTO_KEY).encryptData(value)).commit();
        } catch (Exception e) {
            getPref().edit().putString("RefreshToken", value).commit();
        }
    }

    public String getRefreshToken() {
        try {
            return CryptoManager.newInstance(context, CRYPTO_KEY).decryptData(getPref().getString("RefreshToken", ""));
        } catch (Exception e) {
            return getPref().getString("RefreshToken", "");
        }
    }

    public void setLanguage(String value) {
        getPref().edit().putString("Language", value).commit();
    }

    public String getLanguage() {
        String value = getPref().getString("Language", "en");
        return value;
    }

    public void setSecretKey(String value) throws Exception {
        getPref().edit().putString("SecretKey", CryptoManager.newInstance(context, CRYPTO_KEY).encryptData(value)).commit();

    }

    public String getSecretKey() throws Exception {
        return CryptoManager.newInstance(context, CRYPTO_KEY).decryptData(getPref().getString("SecretKey", ""));
    }

    public void setUploadRequired(boolean value) {
        getPref().edit().putBoolean("UploadRequired", value).commit();
    }

    public boolean getUploadRequired() {
        boolean value = getPref().getBoolean("UploadRequired", false);
        return value;
    }

    public void setUserId(String value) {
        try {
            getPref().edit().putString("UserId", CryptoManager.newInstance(context, CRYPTO_KEY).encryptData(value)).commit();
        } catch (Exception e) {
            getPref().edit().putString("UserId", value).commit();
        }
    }

    public String getUserId() {
        try {
            return CryptoManager.newInstance(context, CRYPTO_KEY).decryptData(getPref().getString("UserId", ""));
        } catch (Exception e) {
            return getPref().getString("UserId", "");
        }
    }
}

