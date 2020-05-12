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
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.arimaclanka.android.wecan.R;
import com.arimaclanka.android.wecan.bluetooth.BleService;
import com.arimaclanka.android.wecan.fragments.AssistanceFragment;
import com.arimaclanka.android.wecan.fragments.HomeFragment;

import java.util.Objects;

public class MainActivity extends BaseActivity {
    private static final int REQ_LANGUAGE_CHANGE = 5488;
    private static final int REQ_STATUS_CHANGE = 866;
    private DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        drawerLayout = findViewById(R.id.drawerLayout);
        try {
            startService(new Intent(this, BleService.class));
        } catch (Exception e) {
            e.printStackTrace();
        }
        showHomeFragment();
    }

    public void onClick(View v) {
        if (v.getId() == R.id.ivMenu) {
            drawerLayout.openDrawer(GravityCompat.START);
        }
        if (v.getId() == R.id.ivClose) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }
        if (v.getId() == R.id.btnMenuHome) {
            showHomeFragment();
            drawerLayout.closeDrawer(GravityCompat.START);
        }
        if (v.getId() == R.id.btnMenuGetAssistance) {
            showAssistanceFragment();
            drawerLayout.closeDrawer(GravityCompat.START);
        }
        if (v.getId() == R.id.btnMenuAssistance) {

        }
        if (v.getId() == R.id.btnMenuSelfCheck) {

        }
        if (v.getId() == R.id.btnMenuMedicine) {

        }
        if (v.getId() == R.id.btnMenuFAQ) {
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("https://www.hpb.health.gov.lk/en/covid-19"));
                startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
            }
            drawerLayout.closeDrawer(GravityCompat.START);
        }
        if (v.getId() == R.id.btnMenuLanguage) {
            Intent intent = new Intent(this, LanguageActivity.class);
            intent.putExtra("mode", "main");
            startActivityForResult(intent, REQ_LANGUAGE_CHANGE);
        }
    }

    public void showChangeStatus() {
        Intent intent = new Intent(this, UpdateStatusActivity.class);
        intent.putExtra("from", "dashboard");
        startActivityForResult(intent, REQ_STATUS_CHANGE);
        Objects.requireNonNull(this).overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQ_LANGUAGE_CHANGE && resultCode == RESULT_OK) {
            Intent intent = new Intent(this, SplashActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        }
        if (requestCode == REQ_STATUS_CHANGE && resultCode == RESULT_OK) {
            if (getApp().getMe().getState() == 3) {
                Intent intent = new Intent(this, UploadActivity.class);
                startActivity(intent);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void showHomeFragment() {
        try {
            if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(0, R.anim.slide_out_left, R.anim.slide_in_left, R.anim.slide_out_right);
        transaction.replace(R.id.fragmentHolder, HomeFragment.newInstance());
        transaction.commit();
    }

    public void showAssistanceFragment() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_in_left, R.anim.slide_out_right);
        transaction.addToBackStack(null);
        transaction.replace(R.id.fragmentHolder, AssistanceFragment.newInstance());
        transaction.commit();
    }
}
