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

import androidx.fragment.app.Fragment;

import com.arimaclanka.android.wecan.App;
import com.arimaclanka.android.wecan.utils.CustomProgressDialog;

import java.util.Objects;

public class BaseFragment extends Fragment {

    private App app;
    private CustomProgressDialog progressDialog;

    public App getApp() {
        if (app == null) {
            app = (App) Objects.requireNonNull(getActivity()).getApplication();
        }
        return app;
    }

    public CustomProgressDialog getProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new CustomProgressDialog(getActivity());
        }
        return progressDialog;
    }
}
