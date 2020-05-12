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
import android.text.Html;
import android.view.View;
import android.widget.TextView;

import com.arimaclanka.android.wecan.R;

public class ConsentActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_consent);
        TextView tvClick = findViewById(R.id.tvClick);
        tvClick.setText(Html.fromHtml(getString(R.string.click_here_for_more_details)));
    }

    public void onClick(View v) {
        if (v.getId() == R.id.tvClick) {
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("https://developer.android.com/guide/topics/connectivity/bluetooth-le#permissions"));
                startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (v.getId() == R.id.btnAgree) {
            showNext();
        }
        if (v.getId() == R.id.btnLanguage) {
            showLanguage();
        }
    }

    private void showNext() {
        Intent intent = new Intent(this, BluetoothActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        finish();
    }

    private void showLanguage() {
        Intent intent = new Intent(this, LanguageActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        finish();
    }
}
