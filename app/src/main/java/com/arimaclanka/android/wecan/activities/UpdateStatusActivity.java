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
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.arimaclanka.android.wecan.R;
import com.arimaclanka.android.wecan.models.Me;

public class UpdateStatusActivity extends BaseActivity {
    private LinearLayout btnNotInfected;
    private LinearLayout btnQuarantined;
    private LinearLayout btnInfected;
    private int status = 0;
    private String from = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_status);
        ImageView ivBack = findViewById(R.id.ivBack);
        btnNotInfected = findViewById(R.id.btnNotInfected);
        btnQuarantined = findViewById(R.id.btnQuarantined);
        btnInfected = findViewById(R.id.btnInfected);
        status = getApp().getMe().getState();
        if (status == 0) {
            status = 1;
        }
        try {
            if (getIntent().getExtras() != null) {
                from = getIntent().getExtras().getString("from");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (from != null && from.equalsIgnoreCase("dashboard")) {
            ivBack.setVisibility(View.VISIBLE);
        }
        changeStatus();
    }

    public void onClick(View v) {
        if (v.getId() == R.id.btnNotInfected) {
            status = 1;
            changeStatus();
        }
        if (v.getId() == R.id.btnQuarantined) {
            status = 2;
            changeStatus();
        }
        if (v.getId() == R.id.btnInfected) {
            status = 3;
            changeStatus();
        }
        if (v.getId() == R.id.btnUpdate) {
            updateStatus();
        }
        if (v.getId() == R.id.ivBack) {
            onBackPressed();
        }
    }

    private void changeStatus() {
        if (status == 2) {
            btnNotInfected.setSelected(false);
            btnQuarantined.setSelected(true);
            btnInfected.setSelected(false);
        } else if (status == 3) {
            btnNotInfected.setSelected(false);
            btnQuarantined.setSelected(false);
            btnInfected.setSelected(true);
        } else {
            btnNotInfected.setSelected(true);
            btnQuarantined.setSelected(false);
            btnInfected.setSelected(false);
        }
    }

    private void updateStatus() {
        setResult(RESULT_OK);
        //API Call: Update user's health status
        getProgressDialog().show();
        new Handler().postDelayed(this::onStatusUpdateComplete, 2000);
    }

    private void onStatusUpdateComplete() {
        getMe();
    }

    //API Call: Get user profile
    private void getMe() {
        new Handler().postDelayed(this::onMeDownloadComplete, 1000);

    }

    private void onMeDownloadComplete() {
        Me me = new Me();
        me.setState(1); //1 = Not infected status
        getApp().setMe(me);
        status = getApp().getMe().getState();
        if (status == 0) {
            status = 1;
        }
        getProgressDialog().dismiss();
        showNext();
    }

    private void showNext() {
        if (from.equalsIgnoreCase("dashboard")) {
            finish();
        } else {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }
}
