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

import android.os.Bundle;
import android.view.View;

import com.arimaclanka.android.wecan.R;
import com.arimaclanka.android.wecan.utils.Alerts;
import com.arimaclanka.android.wecan.utils.DataUploader;

public class UploadActivity extends BaseActivity implements DataUploader.Callback {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);
    }

    public void onClick(View v) {
        if (v.getId() == R.id.ivBack) {
            onBackPressed();
        }
        if (v.getId() == R.id.btnUpload) {
            DataUploader.getInstance(this).setCallback(this);
            getProgressDialog().show();
            DataUploader.getInstance(this).upload();
        }
    }

    @Override
    public void onUploadComplete() {
        getProgressDialog().dismiss();
        getApp().getSettings().setUploadRequired(false);
        Alerts.showSuccess(this, getString(R.string.success), getString(R.string.data_upload_success_msg), (dialogInterface, i) -> finish());
    }

    @Override
    public void onUploadFailed(int code) {
        getProgressDialog().dismiss();
        Alerts.showError(this, getString(R.string.data_upload_failed_msg));
    }

    @Override
    public void onUploadCancelled(int code) {
        getProgressDialog().dismiss();
        if (code == 1) {
            getApp().getSettings().setUploadRequired(false);
            Alerts.showError(this, getString(R.string.no_data_to_upload_msg));
        } else {
            Alerts.showError(this, getString(R.string.data_upload_failed_msg));
        }
    }
}
