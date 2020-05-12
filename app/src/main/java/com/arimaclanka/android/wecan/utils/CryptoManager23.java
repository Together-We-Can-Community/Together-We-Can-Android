package com.arimaclanka.android.wecan.utils;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;

import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.GCMParameterSpec;

@TargetApi(Build.VERSION_CODES.M)
public class CryptoManager23 extends CryptoManager {
    private static final String AndroidKeyStore = "AndroidKeyStore";
    private static final String AES_MODE = "AES/GCM/NoPadding";
    private KeyStore keyStore;
    private String IV = "RandomRandom";

    CryptoManager23(Context context, String keyAlias) throws Exception {
        super(context, keyAlias);
        initialize(false);
    }

    CryptoManager23(Context context, String keyAlias, boolean requireUserAuth) throws Exception {
        super(context, keyAlias);
        initialize(requireUserAuth);
    }

    private void initialize(boolean requireUserAuth) throws Exception {
        keyStore = KeyStore.getInstance(AndroidKeyStore);
        keyStore.load(null);
        if (!keyStore.containsAlias(keyAlias)) {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, AndroidKeyStore);
            KeyGenParameterSpec.Builder builder = new KeyGenParameterSpec.Builder(keyAlias, KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT);
            builder.setBlockModes(KeyProperties.BLOCK_MODE_GCM);
            builder.setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE);
            builder.setRandomizedEncryptionRequired(false);
            if (requireUserAuth) {
                builder.setUserAuthenticationRequired(true);
            }
            keyGenerator.init(builder.build());
            keyGenerator.generateKey();
        }
    }

    private Key getSecretKey() throws UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException {
        return keyStore.getKey(keyAlias, null);
    }

    @Override
    public String encryptData(String inputData) throws Exception {
        Cipher c = Cipher.getInstance(AES_MODE);
        c.init(Cipher.ENCRYPT_MODE, getSecretKey(), new GCMParameterSpec(128, IV.getBytes()));
        byte[] encodedBytes = c.doFinal(inputData.getBytes());
        return Base64.encodeToString(encodedBytes, Base64.DEFAULT);
    }

    @Override
    public String decryptData(String encryptedData) throws Exception {
        byte[] decodedData = Base64.decode(encryptedData, Base64.DEFAULT);
        Cipher c = Cipher.getInstance(AES_MODE);
        c.init(Cipher.DECRYPT_MODE, getSecretKey(), new GCMParameterSpec(128, IV.getBytes()));
        byte[] decryptedData = c.doFinal(decodedData);
        return new String(decryptedData);
    }
}
