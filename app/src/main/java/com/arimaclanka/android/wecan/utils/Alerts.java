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

package com.arimaclanka.android.wecan.utils;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.arimaclanka.android.wecan.R;

public class Alerts {
    private static androidx.appcompat.app.AlertDialog alertDialog;

    public static void showDefaultError(Context context) {
        if (context == null) {
            return;
        }
        String message = context.getString(R.string.msg_toast_generic_exception);
        showAlert(context, false, context.getString(R.string.error), message, context.getString(R.string.ok), null);
    }

    public static void showDefaultError(Context context, DialogInterface.OnClickListener onClickListener) {
        if (context == null) {
            return;
        }
        String message = context.getString(R.string.msg_toast_generic_exception);
        showAlert(context, false, context.getString(R.string.error), message, context.getString(R.string.ok), onClickListener);
    }

    public static void showSuccess(Context context, String message) {
        if (context == null) {
            return;
        }
        showAlert(context, true, "", message, context.getString(R.string.ok), null);
    }

    public static void showSuccess(Context context, String message, DialogInterface.OnClickListener onClickListener) {
        if (context == null) {
            return;
        }
        showAlert(context, true, "", message, context.getString(R.string.ok), onClickListener);
    }

    public static void showSuccess(Context context, String title, String message, DialogInterface.OnClickListener onClickListener) {
        if (context == null) {
            return;
        }
        showAlert(context, true, title, message, context.getString(R.string.ok), onClickListener);
    }

    public static void showError(Context context, String message) {
        if (context == null) {
            return;
        }
        showAlert(context, false, context.getString(R.string.error), message, context.getString(R.string.ok), null);
    }

    public static void showError(Context context, String message, DialogInterface.OnClickListener onClickListener) {
        if (context == null) {
            return;
        }
        showAlert(context, false, context.getString(R.string.error), message, context.getString(R.string.ok), onClickListener);
    }

    public static void showErrorNoButton(Context context, String message) {
        if (context == null) {
            return;
        }
        showAlert(context, false, context.getString(R.string.error), message, "", null);
    }

    public static void showError(Context context, String message, String positiveButton, DialogInterface.OnClickListener onClickListener) {
        if (context == null) {
            return;
        }
        showAlert(context, false, context.getString(R.string.error), message, positiveButton, onClickListener);
    }

    public static void showException(Context context, Exception e) {
        if (context == null) {
            return;
        }
        String message = context.getString(R.string.msg_toast_generic_exception);
        showAlert(context, false, context.getString(R.string.error), message, context.getString(R.string.ok), null);
    }

    public static void showException(Context context, Exception e, DialogInterface.OnClickListener onClickListener) {
        if (context == null) {
            return;
        }
        String message = context.getString(R.string.msg_toast_generic_exception);
        showAlert(context, false, context.getString(R.string.error), message, context.getString(R.string.retry), onClickListener);
    }

    public static void showAlert(Context context, boolean success, String title, String message, String positiveButton, final DialogInterface.OnClickListener onClickListener) {
        if (context == null) {
            return;
        }
        try {
            androidx.appcompat.app.AlertDialog.Builder alertDialogBuilder = new androidx.appcompat.app.AlertDialog.Builder(new ContextThemeWrapper(context, android.R.style.Theme_DeviceDefault_Light_Dialog));
            LayoutInflater inflater = LayoutInflater.from(context);
            View alertView = inflater.inflate(R.layout.dialog_message, null);
            alertDialogBuilder.setView(alertView);
            alertDialogBuilder.setCancelable(false);
            dismiss();
            alertDialog = alertDialogBuilder.create();
            alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            WindowManager.LayoutParams lp = alertDialog.getWindow().getAttributes();
            lp.dimAmount = 0.5f;
            alertDialog.getWindow().setAttributes(lp);
            TextView tvTitle = alertView.findViewById(R.id.tvTitle);
            tvTitle.setText(title);
            TextView tvMessage = alertView.findViewById(R.id.tvMessage);
            tvMessage.setText(message);
            TextView tvOk = alertView.findViewById(R.id.tvOk);
            if (positiveButton != null && positiveButton.length() > 0) {
                tvOk.setText(positiveButton);
                tvOk.setOnClickListener(v -> {
                    alertDialog.dismiss();
                    if (onClickListener != null) {
                        onClickListener.onClick(alertDialog, 0);
                    }
                });
                tvOk.setVisibility(View.VISIBLE);
            } else {
                tvOk.setVisibility(View.GONE);
            }
            alertDialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            alertDialog.setCanceledOnTouchOutside(false);
            alertDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            alertDialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void dismiss() {
        if (alertDialog != null) {
            alertDialog.dismiss();
        }
    }
}