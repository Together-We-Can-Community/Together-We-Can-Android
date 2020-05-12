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

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.arimaclanka.android.wecan.bluetooth.Consts;
import com.arimaclanka.android.wecan.models.Entry;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "data.db";
    private static final int DATABASE_VERSION = 1;
    private static DatabaseHelper databaseHelper;

    public static DatabaseHelper getInstance(Context context) {
        if (databaseHelper == null) {
            databaseHelper = new DatabaseHelper(context);
        }
        return databaseHelper;
    }

    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL("CREATE TABLE IF NOT EXISTS entries (_id INTEGER PRIMARY KEY AUTOINCREMENT, userId TEXT, time TEXT, signature TEXT, model TEXT, createdAt TEXT, distance TEXT, status INTEGER)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        dropAllTables(database);
        onCreate(database);
    }

    private void dropAllTables(SQLiteDatabase database) {
        database.execSQL("DROP TABLE IF EXISTS entries");
    }

    public void clearAll() {
        try {
            SQLiteDatabase database = getReadableDatabase();
            database.execSQL("DELETE from entries");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public synchronized void addEntry(Entry item) {
        Entry lastEntry = getEntry(item.getUserId());
        try {
            if (lastEntry != null) {
                long diff = System.currentTimeMillis() - Long.parseLong(lastEntry.getCreatedAt());
                if (diff < (Consts.SCAN_INTERVAL - (10 * 1000))) {
                    return;
                }
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        SQLiteDatabase database = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("userId", item.getUserId());
        contentValues.put("time", item.getTimeCode());
        contentValues.put("signature", item.getSignature());
        contentValues.put("createdAt", item.getCreatedAt());
        contentValues.put("distance", item.getDistance());
        contentValues.put("model", item.getModel());
        contentValues.put("status", item.getStatus());
        database.insert("entries", null, contentValues);
    }

    public synchronized boolean setUploaded(int id) {
        SQLiteDatabase database = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("status", 2);
        String query = "_id = " + id;
        database.update("entries", contentValues, query, null);
        return true;
    }

    synchronized boolean deleteRecord(int id) {
        SQLiteDatabase database = getWritableDatabase();
        String query = "_id = " + id;
        database.delete("entries", query, null);
        return true;
    }

    private Entry getEntry(String userId) {
        Entry entry = null;
        try {
            SQLiteDatabase database = getReadableDatabase();
            Cursor cursor = database.rawQuery("select * from entries where userId = '" + userId + "' ORDER BY _id DESC LIMIT 1", null);
            if (cursor.moveToNext()) {
                entry = new Entry();
                entry.setId(cursor.getInt(cursor.getColumnIndex("_id")));
                entry.setUserId(cursor.getString(cursor.getColumnIndex("userId")));
                entry.setTimeCode(cursor.getString(cursor.getColumnIndex("time")));
                entry.setSignature(cursor.getString(cursor.getColumnIndex("signature")));
                entry.setModel(cursor.getString(cursor.getColumnIndex("model")));
                entry.setCreatedAt(cursor.getString(cursor.getColumnIndex("createdAt")));
                entry.setDistance(cursor.getString(cursor.getColumnIndex("distance")));
                entry.setStatus(cursor.getInt(cursor.getColumnIndex("status")));
                cursor.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return entry;
    }

    private Entry getEntry(long id) {
        Entry entry = null;
        try {
            SQLiteDatabase database = getReadableDatabase();
            Cursor cursor = database.rawQuery("select * from entries where _id = " + id, null);
            cursor.moveToNext();
            entry = new Entry();
            entry.setId(cursor.getInt(cursor.getColumnIndex("_id")));
            entry.setUserId(cursor.getString(cursor.getColumnIndex("userId")));
            entry.setTimeCode(cursor.getString(cursor.getColumnIndex("time")));
            entry.setSignature(cursor.getString(cursor.getColumnIndex("signature")));
            entry.setModel(cursor.getString(cursor.getColumnIndex("model")));
            entry.setCreatedAt(cursor.getString(cursor.getColumnIndex("createdAt")));
            entry.setDistance(cursor.getString(cursor.getColumnIndex("distance")));
            entry.setStatus(cursor.getInt(cursor.getColumnIndex("status")));
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return entry;
    }

    List<Entry> getPendingEntries() {
        try (SQLiteDatabase sqLiteDatabase = getReadableDatabase()) {
            List<Entry> entries = new ArrayList<>();
            Cursor cursor = sqLiteDatabase.rawQuery("select * from entries where status != 2 limit 800", null);
            while (cursor.moveToNext()) {
                Entry entry = new Entry();
                entry.setId(cursor.getInt(cursor.getColumnIndex("_id")));
                entry.setUserId(cursor.getString(cursor.getColumnIndex("userId")));
                entry.setTimeCode(cursor.getString(cursor.getColumnIndex("time")));
                entry.setSignature(cursor.getString(cursor.getColumnIndex("signature")));
                entry.setModel(cursor.getString(cursor.getColumnIndex("model")));
                entry.setCreatedAt(cursor.getString(cursor.getColumnIndex("createdAt")));
                entry.setDistance(cursor.getString(cursor.getColumnIndex("distance")));
                entry.setStatus(cursor.getInt(cursor.getColumnIndex("status")));
                entries.add(entry);
            }
            cursor.close();
            sqLiteDatabase.close();
            return entries;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<Entry> getAllEntries() {
        try (SQLiteDatabase sqLiteDatabase = getReadableDatabase()) {
            List<Entry> entries = new ArrayList<>();
            Cursor cursor = sqLiteDatabase.rawQuery("select * from entries", null);
            while (cursor.moveToNext()) {
                Entry entry = new Entry();
                entry.setId(cursor.getInt(cursor.getColumnIndex("_id")));
                entry.setUserId(cursor.getString(cursor.getColumnIndex("userId")));
                entry.setTimeCode(cursor.getString(cursor.getColumnIndex("time")));
                entry.setSignature(cursor.getString(cursor.getColumnIndex("signature")));
                entry.setModel(cursor.getString(cursor.getColumnIndex("model")));
                entry.setCreatedAt(cursor.getString(cursor.getColumnIndex("createdAt")));
                entry.setDistance(cursor.getString(cursor.getColumnIndex("distance")));
                entry.setStatus(cursor.getInt(cursor.getColumnIndex("status")));
                entries.add(entry);
            }
            cursor.close();
            sqLiteDatabase.close();
            return entries;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
