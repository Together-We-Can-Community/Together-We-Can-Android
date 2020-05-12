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

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;

import com.arimaclanka.android.wecan.models.Entry;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataUploader {
    private static final String TAG = "DataUploader";
    private Context context;
    @SuppressLint("StaticFieldLeak")
    private static DataUploader dataUploader;
    private boolean processing = false;
    private Callback callback;
    private String userId;

    public interface Callback {
        void onUploadComplete();

        void onUploadFailed(int code);

        void onUploadCancelled(int code);
    }

    private DataUploader(Context context) {
        this.context = context.getApplicationContext();
        userId = new Settings(context).getUserId();
    }

    public static DataUploader getInstance(Context context) {
        if (dataUploader == null) {
            dataUploader = new DataUploader(context);
        }
        return dataUploader;
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    public void upload() {
        process(true);
    }

    private void process(boolean initial) {
        if (processing) {
            return;
        }
        processing = true;
        try {
            final List<Entry> entries = DatabaseHelper.getInstance(context).getPendingEntries();
            if (entries != null && entries.size() > 0) {
                final Map<String, ArrayList<Entry>> entryGroups = new HashMap<>();
                for (Entry entry : entries) {
                    Logger.d(TAG, "Upload: " + entry.toString());
                    ArrayList<Entry> group = entryGroups.get(entry.getCreatedAt());
                    if (group == null) {
                        group = new ArrayList<>();
                        group.add(entry);
                        entryGroups.put(entry.getCreatedAt(), group);
                    } else {
                        group.add(entry);
                    }
                }
                //JSON Payload
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("exposed_by", userId);
                JSONArray jsonData = new JSONArray();
                for (Map.Entry<String, ArrayList<Entry>> pair : entryGroups.entrySet()) {
                    JSONObject jsonGroup = new JSONObject();
                    jsonGroup.put("time", Long.parseLong(pair.getKey()));
                    JSONArray jsonExposedTo = new JSONArray();
                    ArrayList<Entry> list = pair.getValue();
                    for (Entry entry : list) {
                        JSONObject jsonEntry = new JSONObject();
                        jsonEntry.put("scan_data", entry.getUserId() + ":" + entry.getTimeCode() + ":" + entry.getSignature());
                        jsonEntry.put("distance", entry.getDistance());
                        jsonExposedTo.put(jsonEntry);
                    }
                    jsonGroup.put("exposedTo", jsonExposedTo);
                    jsonData.put(jsonGroup);
                }
                jsonObject.put("Data", jsonData);
                jsonObject.put("platform", "android");

                //API Call: submit data
                new Handler().postDelayed(() -> onUpdateComplete(entryGroups), 1000);
            } else {
                processing = false;
                if (initial) {
                    if (callback != null) callback.onUploadCancelled(1);
                } else {
                    if (callback != null) callback.onUploadComplete();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            processing = false;
            if (callback != null) callback.onUploadFailed(-3);
        }
    }

    private void onUpdateComplete(Map<String, ArrayList<Entry>> entryGroups) {
        for (Map.Entry<String, ArrayList<Entry>> pair : entryGroups.entrySet()) {
            ArrayList<Entry> list = pair.getValue();
            for (Entry entry : list) {
                DatabaseHelper.getInstance(context).deleteRecord(entry.getId());
            }
        }
        Logger.d(TAG, "Entry: onUpdateComplete=true");
        processing = false;
        process(false);
    }
}
