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
import android.view.View;
import android.widget.ImageView;

import com.arimaclanka.android.wecan.R;
import com.arimaclanka.android.wecan.utils.LocaleManager;

public class LanguageActivity extends BaseActivity {
    private ImageView btnLangSi;
    private ImageView btnLangTa;
    private ImageView btnLangEn;
    private String language = "en";
    private String mode = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_language);
        ImageView ivBack = findViewById(R.id.ivBack);
        btnLangSi = findViewById(R.id.btnLangSi);
        btnLangTa = findViewById(R.id.btnLangTa);
        btnLangEn = findViewById(R.id.btnLangEn);
        language = getApp().getSettings().getLanguage();
        changeLanguage();
        if (getIntent().getExtras() != null) {
            mode = getIntent().getExtras().getString("mode", "");
        }
        if (mode.equalsIgnoreCase("main")) {
            ivBack.setVisibility(View.VISIBLE);
        }
    }

    public void onClick(View v) {
        if (v.getId() == R.id.btnLangEn) {
            language = "en";
            changeLanguage();
            setResult(RESULT_OK);
        }
        if (v.getId() == R.id.btnLangSi) {
            language = "si";
            changeLanguage();
            setResult(RESULT_OK);
        }
        if (v.getId() == R.id.btnLangTa) {
            language = "ta";
            changeLanguage();
            setResult(RESULT_OK);
        }
        if (v.getId() == R.id.btnOk) {
            submit();
        }
        if (v.getId() == R.id.ivBack) {
            onBackPressed();
        }
    }

    private void changeLanguage() {
        if (language.equalsIgnoreCase("si")) {
            btnLangSi.setSelected(true);
            btnLangTa.setSelected(false);
            btnLangEn.setSelected(false);
        } else if (language.equalsIgnoreCase("ta")) {
            btnLangSi.setSelected(false);
            btnLangTa.setSelected(true);
            btnLangEn.setSelected(false);
        } else {
            btnLangSi.setSelected(false);
            btnLangTa.setSelected(false);
            btnLangEn.setSelected(true);
        }
    }

    private void submit() {
        LocaleManager.setNewLocale(this, language);
        if (mode.equalsIgnoreCase("main")) {
            finish();
        } else {
            Intent intent = new Intent(this, ConsentActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            finish();
        }
    }
}
