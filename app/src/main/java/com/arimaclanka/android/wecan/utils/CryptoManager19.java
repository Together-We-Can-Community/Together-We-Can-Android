package com.arimaclanka.android.wecan.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.security.KeyPairGeneratorSpec;
import android.util.Base64;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.security.Key;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Calendar;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.security.auth.x500.X500Principal;

public class CryptoManager19 extends CryptoManager {
    private static final String AndroidKeyStore = "AndroidKeyStore";
    private static final String SHARED_PREFERENCE_NAME = "com.etihad.guest.android.security";
    private static final String AES_MODE = "AES/GCM/NoPadding";
    private static final String RSA_MODE = "RSA/NONE/PKCS1Padding";
    private KeyStore keyStore;
    private String IV = "randomrandom";

    CryptoManager19(Context context, String keyAlias) throws Exception {
        super(context, keyAlias);
        initialize();
    }

    private void initialize() throws Exception {
        keyStore = KeyStore.getInstance(AndroidKeyStore);
        keyStore.load(null);
        if (!keyStore.containsAlias(keyAlias)) {
            Calendar start = Calendar.getInstance();
            Calendar end = Calendar.getInstance();
            end.add(Calendar.YEAR, 30);
            KeyPairGeneratorSpec spec = new KeyPairGeneratorSpec.Builder(getContext())
                    .setAlias(keyAlias)
                    .setSubject(new X500Principal("CN=" + keyAlias))
                    .setSerialNumber(BigInteger.TEN)
                    .setStartDate(start.getTime())
                    .setEndDate(end.getTime())
                    .build();
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA", AndroidKeyStore);
            kpg.initialize(spec);
            kpg.generateKeyPair();
        }
        generateKey();
    }

    private byte[] rsaEncrypt(byte[] secret) throws Exception {
        KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry) keyStore.getEntry(keyAlias, null);
        Cipher inputCipher = Cipher.getInstance(RSA_MODE, "AndroidOpenSSL");
        inputCipher.init(Cipher.ENCRYPT_MODE, privateKeyEntry.getCertificate().getPublicKey());
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        CipherOutputStream cipherOutputStream = new CipherOutputStream(outputStream, inputCipher);
        cipherOutputStream.write(secret);
        cipherOutputStream.close();
        return outputStream.toByteArray();
    }

    private byte[] rsaDecrypt(byte[] encrypted) throws Exception {
        KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry) keyStore.getEntry(keyAlias, null);
        Cipher output = Cipher.getInstance(RSA_MODE, "AndroidOpenSSL");
        output.init(Cipher.DECRYPT_MODE, privateKeyEntry.getPrivateKey());
        CipherInputStream cipherInputStream = new CipherInputStream(
                new ByteArrayInputStream(encrypted), output);
        ArrayList<Byte> values = new ArrayList<>();
        int nextByte;
        while ((nextByte = cipherInputStream.read()) != -1) {
            values.add((byte) nextByte);
        }

        byte[] bytes = new byte[values.size()];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = values.get(i);
        }
        return bytes;
    }

    private void generateKey() throws Exception {
        SharedPreferences pref = getContext().getSharedPreferences(SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE);
        String encryptedKeyB64 = pref.getString(keyAlias + "pref", null);
        if (encryptedKeyB64 == null) {
            byte[] key = new byte[16];
            SecureRandom secureRandom = new SecureRandom();
            secureRandom.nextBytes(key);
            byte[] encryptedKey = rsaEncrypt(key);
            encryptedKeyB64 = Base64.encodeToString(encryptedKey, Base64.DEFAULT);
            SharedPreferences.Editor edit = pref.edit();
            edit.putString(keyAlias + "pref", encryptedKeyB64);
            edit.commit();
        }
    }


    private Key getSecretKey() throws Exception {
        SharedPreferences pref = getContext().getSharedPreferences(SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE);
        String encodedKey = pref.getString(keyAlias + "pref", null);
        byte[] encryptedKey = Base64.decode(encodedKey, Base64.DEFAULT);
        byte[] key = rsaDecrypt(encryptedKey);
        return new SecretKeySpec(key, "AES");
    }

    @Override
    public String encryptData(String inputData) throws Exception {
        Cipher c = Cipher.getInstance(AES_MODE, "BC");
        IvParameterSpec iv = new IvParameterSpec(IV.getBytes());
        c.init(Cipher.ENCRYPT_MODE, getSecretKey(), iv);
        byte[] encodedBytes = c.doFinal(inputData.getBytes());
        return Base64.encodeToString(encodedBytes, Base64.DEFAULT);
    }

    @Override
    public String decryptData(String encryptedData) throws Exception {
        byte[] decodedData = Base64.decode(encryptedData, Base64.DEFAULT);
        Cipher c = Cipher.getInstance(AES_MODE, "BC");
        IvParameterSpec iv = new IvParameterSpec(IV.getBytes());
        c.init(Cipher.DECRYPT_MODE, getSecretKey(), iv);
        byte[] decryptedData = c.doFinal(decodedData);
        return new String(decryptedData);
    }
}
