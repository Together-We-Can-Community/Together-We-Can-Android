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

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.arimaclanka.android.wecan.R;
import com.arimaclanka.android.wecan.models.Me;
import com.arimaclanka.android.wecan.utils.Alerts;
import com.arimaclanka.android.wecan.utils.Utils;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;

public class SplashActivity extends BaseActivity {
    private GifImageView gifImageView;
    private ImageView ivHand;
    private RelativeLayout layoutLogo;
    private FirebaseRemoteConfig firebaseRemoteConfig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        gifImageView = findViewById(R.id.gifImageView);
        ivHand = findViewById(R.id.ivHand);
        layoutLogo = findViewById(R.id.layoutLogo);

//        **** Uncomment below part after adding valid google-services.json file ****

/*      firebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(3600)
                .build();
        firebaseRemoteConfig.setConfigSettingsAsync(configSettings);
        firebaseRemoteConfig.setDefaultsAsync(R.xml.config_defaults);

        */

        layoutLogo.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                layoutLogo.getViewTreeObserver().removeOnPreDrawListener(this);
                layoutLogo.setTranslationY(300);
                ivHand.setTranslationY(500);
                return false;
            }
        });
        startSplashAnimation();
    }

    private void startSplashAnimation() {
        new Handler().postDelayed(this::checkConnectivity, 2000);
        try {
            GifDrawable gifDrawable = new GifDrawable(getResources(), R.drawable.splash_animation);
            gifDrawable.setLoopCount(1);
            gifImageView.setImageDrawable(gifDrawable);
        } catch (Exception e) {
            e.printStackTrace();
        }
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;

        ObjectAnimator anim1 = ObjectAnimator.ofFloat(layoutLogo, "translationY", height, 0);
        anim1.setDuration(1100);
        ObjectAnimator anim2 = ObjectAnimator.ofFloat(ivHand, "translationY", height, 0);
        anim2.setDuration(1000);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.play(anim1).with(anim2);
        animatorSet.start();
    }

    private void checkConnectivity() {
        if (Utils.checkConnected(this)) {
            loadConfig();
        } else {
            Alerts.showError(this, getString(R.string.msg_toast_no_internet));
        }
    }

    private void loadConfig() {
        try {
            firebaseRemoteConfig.fetchAndActivate().addOnCompleteListener(this, task -> checkAppVersion());
        } catch (Exception e) {
            e.printStackTrace();
            checkAppVersion();
        }
    }

    //API Call: Version check
    private void checkAppVersion() {
        new Handler().postDelayed(this::onVersionCheckComplete, 1000);
    }


    private void onVersionCheckComplete() {
        boolean updateRequired = false;
        if (updateRequired) {
            Alerts.showSuccess(this, getString(R.string.update), getString(R.string.update_msg), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    openPlayStore();
                    finish();
                }
            });
            return;
        }
        checkMe();
    }

    private void openPlayStore() {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + getPackageName())));
        } catch (ActivityNotFoundException e1) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + getPackageName())));
        }
        finish();
    }


    private void checkMe() {
        if (getApp().getSettings().getAccessToken().length() > 0) {
            getMe();
        } else {
            new Handler().postDelayed(this::showSetup, 2000);
        }
    }

    //API Call: Get user profile
    private void getMe() {
        new Handler().postDelayed(this::onMeDownloadComplete, 2000);
    }

    private void onMeDownloadComplete() {
        Me me = new Me();
        me.setState(1); //1 = Not infected status
        getApp().setMe(me);
        showMain();
    }

    private void showSetup() {
        Intent intent = new Intent(this, LanguageActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        finish();
    }

    private void showMain() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        finish();
    }
}
