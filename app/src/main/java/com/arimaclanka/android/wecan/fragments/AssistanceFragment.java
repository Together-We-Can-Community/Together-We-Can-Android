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

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.arimaclanka.android.wecan.R;
import com.arimaclanka.android.wecan.utils.Alerts;

public class AssistanceFragment extends BaseFragment implements View.OnClickListener {
    private EditText etName;
    private EditText etMobile;
    private EditText etAddress;
    private EditText etRequest;
    private Spinner spType;
    private TextView tvQuotaStatus;

    public AssistanceFragment() {

    }

    public static AssistanceFragment newInstance() {
        return new AssistanceFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_assistance, container, false);
        etName = view.findViewById(R.id.etName);
        etMobile = view.findViewById(R.id.etMobile);
        etAddress = view.findViewById(R.id.etAddress);
        etRequest = view.findViewById(R.id.etRequest);
        spType = view.findViewById(R.id.spType);
        tvQuotaStatus = view.findViewById(R.id.tvQuotaStatus);
        view.findViewById(R.id.btnSubmit).setOnClickListener(this);
        getQuota();
        return view;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btnSubmit) {
            request();
        }
    }

    //API Call: get daily request quota
    private void getQuota() {
        getProgressDialog().show();
        new Handler().postDelayed(this::onQuotaDownloadComplete, 2000);
    }

    private void onQuotaDownloadComplete() {
        int quota = 5; // Sample quota value
        if (quota > 0) {
            tvQuotaStatus.setText(getString(R.string.quota_limit_msg, quota));
        } else {
            tvQuotaStatus.setText(getString(R.string.quota_limit__reached_msg, quota));
        }
        getProgressDialog().dismiss();
    }

    private void request() {
        if (etName.getText().length() == 0) {
            Alerts.showError(getActivity(), getString(R.string.name_required));
            return;
        }
        if (etMobile.getText().length() == 0) {
            Alerts.showError(getActivity(), getString(R.string.mobile_required));
            return;
        }
        if (etAddress.getText().length() == 0) {
            Alerts.showError(getActivity(), getString(R.string.address_required));
            return;
        }
        if (etRequest.getText().length() == 0) {
            Alerts.showError(getActivity(), getString(R.string.message_required));
            return;
        }

        //API Call: Send support request
        getProgressDialog().show();
        new Handler().postDelayed(this::onQuotaDownloadComplete, 2000);

    }

    private void onRequestComplete() {
        getProgressDialog().dismiss();
        if (spType.getSelectedItemPosition() == 0) {
            Alerts.showSuccess(getActivity(), getString(R.string.successful), getString(R.string.get_assistance_success_msg_food), (dialogInterface, i) -> getQuota());
        } else if (spType.getSelectedItemPosition() == 1) {
            Alerts.showSuccess(getActivity(), getString(R.string.successful), getString(R.string.get_assistance_success_msg_medicine), (dialogInterface, i) -> getQuota());
        } else {
            Alerts.showSuccess(getActivity(), getString(R.string.successful), getString(R.string.get_assistance_success_msg_other), (dialogInterface, i) -> getQuota());
        }
        clear();
    }

    private void clear() {
        try {
            etName.setText("");
            etMobile.setText("");
            etAddress.setText("");
            etRequest.setText("");
            spType.setSelection(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
