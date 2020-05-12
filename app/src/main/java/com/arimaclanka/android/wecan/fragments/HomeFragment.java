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

package com.arimaclanka.android.wecan.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.arimaclanka.android.wecan.BuildConfig;
import com.arimaclanka.android.wecan.R;
import com.arimaclanka.android.wecan.activities.MainActivity;
import com.arimaclanka.android.wecan.activities.UploadActivity;
import com.arimaclanka.android.wecan.bluetooth.BleManager;

import java.util.Objects;

import pl.droidsonroids.gif.GifImageView;

public class HomeFragment extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener, View.OnClickListener {
    private static final int REQ_UPLOAD = 84;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView tvMyStatus;
    private LinearLayout layoutCurrentStatus;
    private ProgressBar progressBar;
    private TextView tvTotalCases;
    private TextView tvNewCases;
    private TextView tvCountInHospitals;
    private TextView tvDeaths;
    private TextView tvRecovered;
    private RelativeLayout layoutUploadMsg;
    private GifImageView gifImageView;

    public HomeFragment() {

    }

    public static HomeFragment newInstance() {
        return new HomeFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(this);
        tvMyStatus = view.findViewById(R.id.tvMyStatus);
        layoutCurrentStatus = view.findViewById(R.id.layoutCurrentStatus);
        progressBar = view.findViewById(R.id.progressBar);
        tvTotalCases = view.findViewById(R.id.tvTotalCases);
        tvNewCases = view.findViewById(R.id.tvNewCases);
        tvCountInHospitals = view.findViewById(R.id.tvCountInHospitals);
        tvDeaths = view.findViewById(R.id.tvDeaths);
        tvRecovered = view.findViewById(R.id.tvRecovered);
        layoutUploadMsg = view.findViewById(R.id.layoutUploadMsg);
        gifImageView = view.findViewById(R.id.gifImageView);
        view.findViewById(R.id.btnChange).setOnClickListener(this);
        view.findViewById(R.id.btnUpload).setOnClickListener(this);
        view.findViewById(R.id.layoutUploadMsg).setOnClickListener(this);
        view.findViewById(R.id.btnShare).setOnClickListener(this);
        updateStatus();
        getHpbData(false);
        return view;
    }

    @Override
    public void setUserVisibleHint(boolean visible) {
        super.setUserVisibleHint(visible);
        if (visible && isResumed()) {
            onResume();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!getUserVisibleHint()) {
            return;
        }
        updateStatus();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btnChange) {
            ((MainActivity) Objects.requireNonNull(getActivity())).showChangeStatus();
        }
        if (v.getId() == R.id.btnUpload) {
            showUploadScreen();
        }
        if (v.getId() == R.id.layoutUploadMsg) {
            showUploadScreen();
        }
        if (v.getId() == R.id.btnShare) {
            try {
                String url = "https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID;
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_msg, url));
                sendIntent.setType("text/plain");
                Intent shareIntent = Intent.createChooser(sendIntent, "Share");
                startActivity(shareIntent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void updateStatus() {
        try {
            if (BleManager.isBluetoothReady(getActivity())) {
                gifImageView.setImageResource(R.drawable.anim_bluetooth_on);
            } else {
                gifImageView.setImageResource(R.drawable.anim_bluetooth_off);
            }
            if (getApp().getMe().getState() == 2) {
                tvMyStatus.setText(getString(R.string.quarantined));
                tvMyStatus.setTextColor(ContextCompat.getColor(Objects.requireNonNull(getActivity()), R.color.orange_text));
            } else if (getApp().getMe().getState() == 3) {
                tvMyStatus.setText(getString(R.string.infected));
                tvMyStatus.setTextColor(ContextCompat.getColor(Objects.requireNonNull(getActivity()), R.color.red_text));
            } else {
                tvMyStatus.setText(getString(R.string.not_infected));
                tvMyStatus.setTextColor(ContextCompat.getColor(Objects.requireNonNull(getActivity()), R.color.green_text));
            }
            if (getApp().getSettings().getUploadRequired()) {
                layoutUploadMsg.setVisibility(View.VISIBLE);
            } else {
                layoutUploadMsg.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showUploadScreen() {
        Intent intent = new Intent(getActivity(), UploadActivity.class);
        startActivityForResult(intent, REQ_UPLOAD);
        Objects.requireNonNull(getActivity()).overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    public void onRefresh() {
        getHpbData(true);
    }

    //API Call: fetch Covid-19 current status from Health Promotion Bureau API
    private void getHpbData(boolean silent) {
        if (!silent) {
            progressBar.setVisibility(View.VISIBLE);
        }
        new Handler().postDelayed(this::onHpbDataDownloadComplete, 2000);

    }

    private void onHpbDataDownloadComplete() {
        tvTotalCases.setText(String.valueOf(0));
        tvNewCases.setText(String.valueOf(0));
        tvCountInHospitals.setText(String.valueOf(0));
        tvDeaths.setText(String.valueOf(0));
        tvRecovered.setText(String.valueOf(0));
        layoutCurrentStatus.setVisibility(View.VISIBLE);
        swipeRefreshLayout.setRefreshing(false);
        progressBar.setVisibility(View.GONE);
    }
}
