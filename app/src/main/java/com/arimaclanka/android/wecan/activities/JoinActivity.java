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
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.arimaclanka.android.wecan.R;
import com.arimaclanka.android.wecan.utils.Alerts;

public class JoinActivity extends BaseActivity {
    private EditText etMobile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join);
        TextView tvCountryCode = findViewById(R.id.tvCountryCode);
        String countryCode = "+94";
        tvCountryCode.setText(countryCode);
        etMobile = findViewById(R.id.etMobile);
        etMobile.setOnEditorActionListener((textView, i, keyEvent) -> {
            if (i == EditorInfo.IME_ACTION_DONE) {
                requestOtp();
                return true;
            }
            return false;
        });
    }

    public void onClick(View v) {
        if (v.getId() == R.id.btnJoin) {
            requestOtp();
        }
    }

    private void requestOtp() {
        String mobile = etMobile.getText().toString().replace(" ", "");
        if (mobile.contains("+")) {
            Alerts.showError(this, "Invalid number");
            return;
        }
        if (mobile.startsWith("0")) {
            mobile = mobile.substring(1);
        }
        if (mobile.startsWith("00")) {
            mobile = mobile.substring(2);
        }
        if (mobile.length() != 9) {
            Alerts.showError(this, "Invalid number");
            return;
        }
        hideKeyboard();
        //API Call: request otp
        getProgressDialog().show();
        String finalMobile = mobile;
        new Handler().postDelayed(() -> onOtpRequestComplete(finalMobile), 1000);
    }

    private void onOtpRequestComplete(String mobileNumber) {
        getProgressDialog().dismiss();
        Intent intent = new Intent(this, VerifyActivity.class);
        intent.putExtra("mobile", mobileNumber);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        finish();
    }
}
