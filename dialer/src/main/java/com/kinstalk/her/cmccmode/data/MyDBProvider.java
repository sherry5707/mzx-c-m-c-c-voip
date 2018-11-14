package com.kinstalk.her.cmccmode.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Binder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.kinstalk.her.dialer.database.DialerDatabaseHelper;
import com.kinstalk.her.dialerbind.ObjectFactory;

public class MyDBProvider extends ContentProvider {
    private static final String TAG = "MyDBProvider";

    /**
     * The authority for the all contacts provider
     */
    public static final String AUTHORITY = ObjectFactory.getMyDBProviderAuthority();
    /**
     * A content:// style uri to the authority for the all contacts provider
     */
    public static final Uri AUTHORITY_URI = Uri.parse("content://" + AUTHORITY);

    private static final int ALL_CONTACTS = 1;
//    private static final int HISTORY_CALL_RECORDS = 2;
    private static final int DEVICE_STATUS = 3;
    private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sURIMatcher.addURI(AUTHORITY, DialerDatabaseHelper.AllContactsColumns.ALLCONTACTS_TABLE, ALL_CONTACTS);
//        sURIMatcher.addURI(AUTHORITY, DialerDatabaseHelper.HistoryCallRecordsColumns.HISTORY_CALLRECORDS_TABLE,
//                HISTORY_CALL_RECORDS);
        sURIMatcher.addURI(AUTHORITY, DialerDatabaseHelper.DeviceStatusColumns.DEVICESTATUS_TABLE,
                DEVICE_STATUS);
    }

    public static DialerDatabaseHelper mDialerDatabaseHelper;

    @Override
    public boolean onCreate() {
        mDialerDatabaseHelper = DialerDatabaseHelper.getInstance(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sort) {
        Log.d(TAG, "query");
        SQLiteDatabase db = mDialerDatabaseHelper.getReadableDatabase();
        int match = sURIMatcher.match(uri);
        Cursor cursor = null;
        switch (match) {
            case ALL_CONTACTS: {
                cursor = db.query(DialerDatabaseHelper.AllContactsColumns.ALLCONTACTS_TABLE, projection,
                        selection, selectionArgs, null,
                        null, sort);
                break;
            }
//            case HISTORY_CALL_RECORDS:
//                break;
            case DEVICE_STATUS:
                cursor = db.query(DialerDatabaseHelper.DeviceStatusColumns.DEVICESTATUS_TABLE, projection,
                        selection, selectionArgs, null,
                        null, sort);
                break;
            default:
                Log.d(TAG, "querying unknown URI: " + uri);
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        if (cursor != null) {
            cursor.setNotificationUri(getContext().getContentResolver(), uri);
            Log.d(TAG, "created cursor " + cursor + " on behalf of " + Binder.getCallingPid());
        } else {
            Log.d(TAG, "query failed in qchat_provider database");
        }
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        SQLiteDatabase db = mDialerDatabaseHelper.getReadableDatabase();
        long id = 0;
        final int match = sURIMatcher.match(uri);
        switch (match) {
            case ALL_CONTACTS:
                id = db.replace(DialerDatabaseHelper.AllContactsColumns.ALLCONTACTS_TABLE, null, contentValues);
                break;
//            case HISTORY_CALL_RECORDS:
//                id = db.insert(DialerDatabaseHelper.AllContactsColumns.ALLCONTACTS_TABLE, null, contentValues);
//                break;
            case DEVICE_STATUS:
                id = db.insert(DialerDatabaseHelper.DeviceStatusColumns.DEVICESTATUS_TABLE, null, contentValues);
                break;
            default:
                Log.i(TAG, "inserting unknown/invalid URI: " + uri);
                throw new UnsupportedOperationException("Cannot insert URI: " + uri);
        }
        if (id < 0) {
            Log.d(TAG, "couldn't insert into qchat_provider database");
            return null;
        } else {
            notifyContentChanged(uri, match);
        }
        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int delete(Uri uri, String where, String[] whereArgs) {
        Log.d(TAG, "delete");

        int count = -1;
        try {
            SQLiteDatabase db = mDialerDatabaseHelper.getWritableDatabase();
            final int match = sURIMatcher.match(uri);
            switch (match) {
                case ALL_CONTACTS: {
                    count = db.delete(DialerDatabaseHelper.AllContactsColumns.ALLCONTACTS_TABLE, where,
                            whereArgs);
                    break;
                }
//                case HISTORY_CALL_RECORDS: {
//                    count = db.delete(DialerDatabaseHelper.HistoryCallRecordsColumns.HISTORY_CALLRECORDS_TABLE, where,
//                            whereArgs);
//                    break;
//                }
                case DEVICE_STATUS: {
                    count = db.delete(DialerDatabaseHelper.DeviceStatusColumns.DEVICESTATUS_TABLE, where,
                            whereArgs);
                    break;
                }
                default:
                    Log.d(TAG, "deleting unknown/invalid URI: " + uri);
                    throw new UnsupportedOperationException("Cannot delete URI: " + uri);
            }
            if (count > 0) { // && ((where != null)|| (whereArgs!=null))) {
                notifyContentChanged(uri, match);
                Log.i(TAG, "delete count" + count);
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
        return count;
    }

    @Override
    public int update(Uri uri, ContentValues values, String where, String[] whereArgs) {
        Log.d(TAG, "update");

        SQLiteDatabase db = mDialerDatabaseHelper.getWritableDatabase();
        int count = -1;
        final int match = sURIMatcher.match(uri);
        switch (match) {
            case ALL_CONTACTS: {
                count = db.update(DialerDatabaseHelper.AllContactsColumns.ALLCONTACTS_TABLE, values, where,
                        whereArgs);
                break;
            }
//            case HISTORY_CALL_RECORDS:
//                break;
            case DEVICE_STATUS: {
                count = db.update(DialerDatabaseHelper.DeviceStatusColumns.DEVICESTATUS_TABLE, values, where,
                        whereArgs);
                break;
            }
            default:
                Log.d(TAG, "updating unknown/invalid URI: " + uri);
                throw new UnsupportedOperationException("Cannot update URI: " + uri);
        }
        if (count > 0) {
            notifyContentChanged(uri, match);
        }
        return count;
    }

    /**
     * Notify of a change through URIs
     *
     * @param uri      URI for the changed record(s)
     * @param uriMatch the match ID from {@link #sURIMatcher}
     */
    private void notifyContentChanged(final Uri uri, int uriMatch) {
        Long recordId = null;
        Uri uriToNotify = null;
        switch (uriMatch) {
            case ALL_CONTACTS: {
                uriToNotify = DialerDatabaseHelper.AllContactsColumns.CONTENT_URI;
                break;
            }
//            case HISTORY_CALL_RECORDS: {
//                uriToNotify = DialerDatabaseHelper.HistoryCallRecordsColumns.CONTENT_URI;
//                break;
//            }
            case DEVICE_STATUS: {
                uriToNotify = DialerDatabaseHelper.DeviceStatusColumns.CONTENT_URI;
                break;
            }
            default:
                Log.d(TAG, "updating unknown/invalid URI: " + uri);
                throw new UnsupportedOperationException("Cannot update URI: " + uri);
        }

        if (recordId != null) {
            uriToNotify = ContentUris.withAppendedId(uriToNotify, recordId);
        }
        getContext().getContentResolver().notifyChange(uriToNotify, null);
    }
}
