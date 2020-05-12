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

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.TextView;

import com.arimaclanka.android.wecan.R;
import com.arimaclanka.android.wecan.models.Me;
import com.arimaclanka.android.wecan.utils.Alerts;
import com.arimaclanka.android.wecan.utils.Logger;
import com.chaos.view.PinView;

public class VerifyActivity extends BaseActivity {
    private PinView pinView;
    private String mobileNumber = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getIntent().getExtras() != null) {
            mobileNumber = getIntent().getExtras().getString("mobile");
            setContentView(R.layout.activity_verify);
            TextView tvOtpMessage = findViewById(R.id.tvOtpMessage);
            tvOtpMessage.setText(getString(R.string.otp_message, mobileNumber));
            pinView = findViewById(R.id.pinView);
            pinView.addTextChangedListener(textWatcher);
        } else {
            Alerts.showDefaultError(this, (dialogInterface, i) -> finish());
        }
    }

    private TextWatcher textWatcher = new TextWatcher() {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            try {
                if (s.length() == 4) {
                    login();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    public void onClick(View v) {
        if (v.getId() == R.id.tvResend) {

        }
        if (v.getId() == R.id.btnVerify) {
            login();
        }
    }


    private void login() {
        if (pinView.getText() == null) {
            return;
        }
        if (pinView.getText().length() == 0) {
            Alerts.showError(this, "Enter valid code");
            return;
        }
        //API Call: login
        String code = pinView.getText().toString();
        hideKeyboard();
        getProgressDialog().show();
        new Handler().postDelayed(this::onLoginComplete, 1000);
    }

    private void onLoginComplete() {
        String access_token = "sample_access_token";
        getApp().getSettings().setAccessToken(access_token);
        getKey();
    }

    //API Call: Get user's secret key
    private void getKey() {
        new Handler().postDelayed(this::onKeyDownloadComplete, 1000);
    }

    private void onKeyDownloadComplete() {
        String secret = "123456"; //Secret key retrieved from backend. This key use to calculate ble payload signature
        try {
            getApp().getSettings().setSecretKey(secret);
            getMe();
        } catch (Exception e) {
            Logger.e("VerifyActivity", "Secret key error: " + e.getMessage());
            clearData();
            getProgressDialog().dismiss();
            Alerts.showError(this, "Failed to load required keys from server!");
        }
    }

    //API Call: Get user profile
    private void getMe() {
        new Handler().postDelayed(this::onMeDownloadComplete, 1000);
    }

    private void onMeDownloadComplete() {
        Me me = new Me();
        me.setState(1); //1 = Not infected status
        String uId = "AnonUser1234"; //Anonymous user id
        getApp().getSettings().setUserId(uId);
        getApp().setMe(me);
        getProgressDialog().dismiss();
        showNext();
    }

    private void showNext() {
        Intent intent = new Intent(this, UpdateStatusActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        finish();
    }
}
