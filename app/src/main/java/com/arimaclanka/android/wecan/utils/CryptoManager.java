package com.arimaclanka.android.wecan.utils;

import android.content.Context;
import android.os.Build;

public abstract class CryptoManager {
    private Context context;
    String keyAlias;

    CryptoManager(Context context, String keyAlias) throws Exception {
        this.context = context;
        this.keyAlias = keyAlias;
    }

    public static CryptoManager newInstance(Context context, String keyAlias) throws Exception {
        if (Build.VERSION.SDK_INT >= 23) {
            return new CryptoManager23(context, keyAlias);
        } else {
            return new CryptoManager19(context, keyAlias);
        }
    }

    protected Context getContext() {
        return context;
    }

    public abstract String encryptData(String inputData) throws Exception;

    public abstract String decryptData(String encryptedData) throws Exception;
}
