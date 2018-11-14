package com.kinstalk.her.cmccmode.data;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.util.Log;

import com.kinstalk.her.dialer.DialerApplication;
import com.kinstalk.her.dialer.database.DialerDatabaseHelper;

import java.util.List;

/**
 * create to use ContentProvider
 */
public class MyDBProviderHelper {
    private static final String TAG = "MyDBProviderHelper";

    public static int insertAllContacts(List<ContactInfo> contacts) {
        Log.i(TAG, "insertAllContacts: " + contacts);
        int status = 0;
        ContentResolver resolver = DialerApplication.getContext().getContentResolver();
        try {
            Log.i(TAG, "insertContacts");
            ContentValues values = new ContentValues();
            for (ContactInfo contact : contacts) {
                values.clear();
                values.put(DialerDatabaseHelper.AllContactsColumns.CONTACT_ID, contact.getContactId());
                values.put(DialerDatabaseHelper.AllContactsColumns.NICK_NAME, contact.getNickname());
                values.put(DialerDatabaseHelper.AllContactsColumns.SORT_KEY, contact.getSortKey());
                resolver.insert(DialerDatabaseHelper.AllContactsColumns.CONTENT_URI, values);
            }
        } catch (SQLException e) {
            Log.e(TAG, "insertAllContacts: exception:" + e);
            status = -1;
        }
        return status;
    }

    public static int clearContactsDB() {
        Log.i(TAG, "clearMyOwnContactsDB: ");
        int status = 0;
        ContentResolver resolver = DialerApplication.getContext().getContentResolver();
        try {
            resolver.delete(DialerDatabaseHelper.AllContactsColumns.CONTENT_URI,
                    DialerDatabaseHelper.AllContactsColumns.CONTACT_ID + "!=-1", null);
        } catch (SQLException e) {
            Log.e(TAG, "clearContactsDB: exception:" + e);
            e.printStackTrace();
            status = -1;
        }
        return status;
    }

    @SuppressLint("MissingPermission")
    public static void insertCallRecord(Context context, CallRecord record) {
        Log.i(TAG, "insertCallRecord: record:" + record);
        ContentValues values = new ContentValues();
        values.put(CallLog.Calls.CACHED_NAME, record.getmName());
        values.put(CallLog.Calls.NUMBER, record.getmNumber());
        //CallLog.Calls.OUTGOING_TYPE 外拨
        //CallLog.Calls.INCOMING_TYPE 接入
        //CallLog.Calls.MISSED_TYPE 未接
        values.put(CallLog.Calls.TYPE, record.getmCallLogType());
        values.put(CallLog.Calls.DATE, record.getmCallLogDate());
        values.put(CallLog.Calls.NEW, "0");// 0已看1未看 ,由于没有获取默认全为已读
        Log.e(TAG, "insertCallRecord: values:" + values.toString());
        Uri uri = context.getContentResolver().insert(CallLog.Calls.CONTENT_URI, values);
        Log.i(TAG, "insertCallRecord: uri:" + uri);
    }

    public static int clearSystemContactsDB() {
        Log.i(TAG, "clearSystemContactsDB: ");
        int status = 0;
        try {
            Uri uri = Uri.parse("content://com.android.contacts/raw_contacts");
            DialerApplication.getContext().getContentResolver().delete(uri, "_id!=-1", null);
        } catch (SQLException e) {
            Log.e(TAG, "clearContactsDB: exception:" + e);
            status = -1;
        }
        return status;
    }

    /**
     * 首先向RawContacts.CONTENT_URI执行一个空值插入，目的是获取系统返回的rawContactId
     * 后面插入data表的数据，只有执行空值插入，才能使插入的联系人在通讯录里可见
     */
    public static void insertContactToSystemDB(List<ContactInfo> contacts) {
        try {
            Log.i(TAG, "insertTestDataToContactsDB: contacts:" + contacts.toString());
            ContentValues values = new ContentValues();
            for (ContactInfo contact : contacts) {
                values.clear();
                Uri rawContactUri = DialerApplication.getContext().getContentResolver().insert(
                        ContactsContract.RawContacts.CONTENT_URI, values);
                long rawContactId = ContentUris.parseId(rawContactUri);
                //往data表入姓名数据
                values.clear();
                values.put(ContactsContract.Data.RAW_CONTACT_ID, rawContactId);
                values.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE);// 内容类型
                values.put(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME, contact.getNickname());
                DialerApplication.getContext().getContentResolver().insert(ContactsContract.Data.CONTENT_URI,
                        values);

                //往data表入电话数据
                values.clear();
                values.put(ContactsContract.Data.RAW_CONTACT_ID, rawContactId);
                values.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
                values.put(ContactsContract.CommonDataKinds.Phone.NUMBER, contact.getContactId());
                values.put(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE);
                DialerApplication.getContext().getContentResolver().insert(android.provider.ContactsContract.Data.CONTENT_URI, values);
            }
        } catch (SQLException e) {
            Log.e(TAG, "clearContactsDB: exception:" + e);
        }
    }

    /**
     * 根据contactId查找联系人
     *
     * @param context
     * @param contactId
     * @return
     */
    public static ContactInfo getContactByContactIdFromSystemDB(Context context, String contactId) {
        //uri=  content://com.android.contacts/data/phones/filter/#
        Cursor cursor = null;
        try {
//            Uri uri = Uri.parse("content://com.android.contacts/data/phones/filter/" + number);
            //先判断该number是否为contactId
            ContentResolver resolver = context.getContentResolver();
            cursor = resolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    new String[]{ContactsContract.Data.DISPLAY_NAME},
                    ContactsContract.CommonDataKinds.Phone.NUMBER + " = ?",
                    new String[]{contactId}, null); //从raw_contact表中返回display_name
            if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {
                String nickName = cursor.getString(0);
                ContactInfo contact = new ContactInfo(Integer.parseInt(contactId), nickName);
                return contact;
            }
        } catch (SQLException e) {
            Log.e(TAG, "clearContactsDB: exception:" + e);
            e.printStackTrace();
            return null;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }

    /**
     * 根据姓名获得contactInfo
     *
     * @param context
     * @param nickName
     * @return
     */
    public static ContactInfo getContactByNickName(Context context, String nickName) {
        Log.i(TAG, "getContactByNickName: nickName:" + nickName);
        Cursor cursor = null;
        ContactInfo contact = null;
        try {
            ContentResolver resolver = context.getContentResolver();
            //view_data表中displayName相同的的值会有两行，一行的data1是number，一行是name
            //根据mimeType区分。
            //我的selection语句是:display_name = 'nickName' & mimeType = 'phone'
            cursor = resolver.query(ContactsContract.Data.CONTENT_URI,
                    new String[]{"data1"},
                    ContactsContract.Data.DISPLAY_NAME + "= \'" + nickName + "\' and " +
                            ContactsContract.Data.MIMETYPE + "= \'" + ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE + "\'",
                    null, null);
            if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {
                int number = cursor.getInt(0);
                Log.i(TAG, "getContactByNickName: number:" + number);
                contact = new ContactInfo();
                contact.setNickname(nickName);
                contact.setContactId(number);
                return contact;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return contact;
    }

    /**
     * 根据row_contact_id找Contact
     *
     * @param context
     * @param id:row_contact_id
     * @return
     */
    public static ContactInfo getContactByRowContactIdFromSystemDB(Context context, int id) {
        //uri=  content://com.android.contacts/data/phones/filter/#
//        Uri uri = Uri.parse("content://com.android.contacts/data" + number);
        Cursor cursor = null;
        ContactInfo contact = null;
        try {
            ContentResolver resolver = context.getContentResolver();
            //从data表中返回display_name和contactId
            //因为data1表中的数据会根据mimeType改变，所以对于同一个row_contact_id，会找到两行数据。
            //一行data1是name，一行data1是number
            cursor = resolver.query(ContactsContract.Data.CONTENT_URI,
                    new String[]{"data1", ContactsContract.Data.MIMETYPE},
                    ContactsContract.Data.RAW_CONTACT_ID + "=" + id,
                    null, null);
            if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {
                contact = new ContactInfo();
                do {
                    String data1 = cursor.getString(0);
                    String mimeType = cursor.getString(1);
                    Log.i(TAG, "getContactByRowContactIdFromSystemDB: mimeType:" + mimeType + ",data1;" + data1);
                    if (ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE.equals(mimeType)) {
                        contact.setNickname(data1);
                    } else if (ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE.equals(mimeType)) {
                        contact.setContactId(Integer.parseInt(data1));
                    }
                } while (cursor.moveToNext());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return contact;
    }

    /**
     * 获得通话记录里最后一个contactInfo用于重拨
     *
     * @return
     */
    @SuppressLint("MissingPermission")
    public static ContactInfo getLastRecordContact(Context context) {
        Cursor cursor = null;
        ContactInfo contact = null;
        try {
            ContentResolver resolver = context.getContentResolver();
            cursor = resolver.query(CallLog.Calls.CONTENT_URI, new String[]{CallLog.Calls.CACHED_NAME, CallLog.Calls.NUMBER},
                    null, null, CallLog.Calls.DATE + " desc");
            if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {
                String name = cursor.getString(0);
                String number = cursor.getString(1);
                contact = new ContactInfo(Integer.parseInt(number), name);
                Log.i(TAG, "getLastRecordContact: contact:" + contact);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return contact;
    }

    public static String getNameByPinYin(String pinyin) {
        Log.i(TAG, "pinyin:" + pinyin);
        Cursor cursor = null;
        try {
            ContentResolver resolver = DialerApplication.getContext().getContentResolver();
            cursor = resolver.query(DialerDatabaseHelper.AllContactsColumns.CONTENT_URI,
                    new String[]{DialerDatabaseHelper.AllContactsColumns.SORT_KEY,
                            DialerDatabaseHelper.AllContactsColumns.NICK_NAME},
                    DialerDatabaseHelper.AllContactsColumns.SORT_KEY + "=?",
                    new String[]{pinyin}, null);
            if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {
                String nickName = cursor.getString(1);
                Log.i(TAG, "getNameByPinYin: nickNmae;" + nickName);
                return nickName;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }

    /**
     * 根据拼音从自己的数据库获得name和contactId
     * @param pinyin
     * @return
     */
    public static ContactInfo getContactInfoByPinYin(String pinyin) {
        Log.i(TAG, "getContactInfoByPinYin，pinyin:" + pinyin);
        Cursor cursor = null;
        try {
            ContentResolver resolver = DialerApplication.getContext().getContentResolver();
            cursor = resolver.query(DialerDatabaseHelper.AllContactsColumns.CONTENT_URI,
                    new String[]{DialerDatabaseHelper.AllContactsColumns.CONTACT_ID,
                            DialerDatabaseHelper.AllContactsColumns.NICK_NAME},
                    DialerDatabaseHelper.AllContactsColumns.SORT_KEY + "=?",
                    new String[]{pinyin}, null);
            if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {
                int contactId = cursor.getInt(0);
                String nickName = cursor.getString(1);
                ContactInfo contactInfo = new ContactInfo(contactId,nickName);
                Log.i(TAG, "getContactInfoByPinYin: nickNmae;" + contactInfo);
                return contactInfo;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }

    /**
     * 根据姓名获得contactInfo,从自己的数据库
     *
     * @param nickName
     * @return
     */
    public static ContactInfo getContactByNickNamFromSelfDB(String nickName) {
        Log.i(TAG, "getContactByNickNamFromSelfDB: nickName:" + nickName);
        Cursor cursor = null;
        ContactInfo contact = null;
        try {
            ContentResolver resolver = DialerApplication.getContext().getContentResolver();
            cursor = resolver.query(DialerDatabaseHelper.AllContactsColumns.CONTENT_URI,
                    new String[]{DialerDatabaseHelper.AllContactsColumns.CONTACT_ID},
                    DialerDatabaseHelper.AllContactsColumns.NICK_NAME + "=?",
                    new String[]{nickName}, null);
            if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {
                int number = cursor.getInt(0);
                contact = new ContactInfo();
                contact.setNickname(nickName);
                contact.setContactId(number);
                Log.i(TAG, "getContactByNickNamFromSelfDB: contact:" + contact);
                return contact;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return contact;
    }

    public static void setBindStatus(int flag) {
        Cursor cursor = null;
        try {
            ContentResolver resolver = DialerApplication.getContext().getContentResolver();
            ContentValues values = new ContentValues();
            values.put(DialerDatabaseHelper.DeviceStatusColumns.BIND_STATUS, flag);
            int i = resolver.update(DialerDatabaseHelper.DeviceStatusColumns.CONTENT_URI, values, null, null);
            Log.i(TAG, "setBindStatus,update index:" + i);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public static int getBindStatus() {
        Cursor cursor = null;
        int flag = 0;
        try {
            ContentResolver resolver = DialerApplication.getContext().getContentResolver();
            cursor = resolver.query(DialerDatabaseHelper.DeviceStatusColumns.CONTENT_URI,
                    new String[]{DialerDatabaseHelper.DeviceStatusColumns.BIND_STATUS},
                    null, null, null);
            if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {
                flag = cursor.getInt(0);
                Log.i(TAG, "getBindStatus: flag:" + flag);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return flag;
    }

    public static void setEnableStatus(int flag) {
        Cursor cursor = null;
        try {
            ContentResolver resolver = DialerApplication.getContext().getContentResolver();
            ContentValues values = new ContentValues();
            values.put(DialerDatabaseHelper.DeviceStatusColumns.ENABLED_STATUS, flag);
            int i = resolver.update(DialerDatabaseHelper.DeviceStatusColumns.CONTENT_URI, values, null, null);
            Log.i(TAG, "setEnableStatus,update index:" + i);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public static int getEnableStatus() {
        Cursor cursor = null;
        int flag = 0;
        try {
            ContentResolver resolver = DialerApplication.getContext().getContentResolver();
            cursor = resolver.query(DialerDatabaseHelper.DeviceStatusColumns.CONTENT_URI,
                    new String[]{DialerDatabaseHelper.DeviceStatusColumns.ENABLED_STATUS},
                    null, null, null);
            if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {
                flag = cursor.getInt(0);
                Log.i(TAG, "getEnableStatus: flag:" + flag);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return flag;
    }

    public static void setInCallStatus(int flag) {
        Cursor cursor = null;
        try {
            ContentResolver resolver = DialerApplication.getContext().getContentResolver();
            ContentValues values = new ContentValues();
            values.put(DialerDatabaseHelper.DeviceStatusColumns.IN_CALL, flag);
            int i = resolver.update(DialerDatabaseHelper.DeviceStatusColumns.CONTENT_URI, values, null, null);
            Log.i(TAG, "setInCallStatus,update index:" + i);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public static int getInCalleStatus() {
        Cursor cursor = null;
        int flag = 0;
        try {
            ContentResolver resolver = DialerApplication.getContext().getContentResolver();
            cursor = resolver.query(DialerDatabaseHelper.DeviceStatusColumns.CONTENT_URI,
                    new String[]{DialerDatabaseHelper.DeviceStatusColumns.IN_CALL},
                    null, null, null);
            if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {
                flag = cursor.getInt(0);
                Log.i(TAG, "getInCalleStatus: flag:" + flag);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return flag;
    }

    public static void setCallAlertingStatus(int flag) {
        Cursor cursor = null;
        try {
            ContentResolver resolver = DialerApplication.getContext().getContentResolver();
            ContentValues values = new ContentValues();
            values.put(DialerDatabaseHelper.DeviceStatusColumns.CALL_ALERTING, flag);
            int i = resolver.update(DialerDatabaseHelper.DeviceStatusColumns.CONTENT_URI, values, null, null);
            Log.i(TAG, "getCallAlertingStatus,update index:" + i);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public static int getCallAlertingStatus() {
        Cursor cursor = null;
        int flag = 0;
        try {
            ContentResolver resolver = DialerApplication.getContext().getContentResolver();
            cursor = resolver.query(DialerDatabaseHelper.DeviceStatusColumns.CONTENT_URI,
                    new String[]{DialerDatabaseHelper.DeviceStatusColumns.CALL_ALERTING},
                    null, null, null);
            if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {
                flag = cursor.getInt(0);
                Log.i(TAG, "getCallAlertingStatus: flag:" + flag);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return flag;
    }

    public static void setAppKeySecret(AppInfo appInfo) {
        Cursor cursor = null;
        try {
            ContentResolver resolver = DialerApplication.getContext().getContentResolver();
            ContentValues values = new ContentValues();
            values.put(DialerDatabaseHelper.DeviceStatusColumns.APP_KEY, appInfo.getAppKey());
            values.put(DialerDatabaseHelper.DeviceStatusColumns.APP_SECRET, appInfo.getAppsecret());
            int i = resolver.update(DialerDatabaseHelper.DeviceStatusColumns.CONTENT_URI, values, null, null);
            Log.i(TAG, "setAppKeySecret,update index:" + i);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public static AppInfo getAppKeySecret() {
        Cursor cursor = null;
        AppInfo appInfo = null;
        try {
            ContentResolver resolver = DialerApplication.getContext().getContentResolver();
            cursor = resolver.query(DialerDatabaseHelper.DeviceStatusColumns.CONTENT_URI,
                    new String[]{DialerDatabaseHelper.DeviceStatusColumns.APP_KEY,
                            DialerDatabaseHelper.DeviceStatusColumns.APP_SECRET},
                    null, null, null);
            if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {
                appInfo = new AppInfo(cursor.getString(0),cursor.getString(1));
                Log.i(TAG, "getAppKeySecret: appInfo:" + appInfo);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return appInfo;
    }

    public static void setHJGHLoginStatus(int flag) {
        Cursor cursor = null;
        try {
            ContentResolver resolver = DialerApplication.getContext().getContentResolver();
            ContentValues values = new ContentValues();
            values.put(DialerDatabaseHelper.DeviceStatusColumns.HJGH_LOGIN_STATUS, flag);
            int i = resolver.update(DialerDatabaseHelper.DeviceStatusColumns.CONTENT_URI, values, null, null);
            Log.i(TAG, "setHJGHLoginStatus,update index:" + i);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public static int getHJGHLoginStatus() {
        Cursor cursor = null;
        int flag = 0;
        try {
            ContentResolver resolver = DialerApplication.getContext().getContentResolver();
            cursor = resolver.query(DialerDatabaseHelper.DeviceStatusColumns.CONTENT_URI,
                    new String[]{DialerDatabaseHelper.DeviceStatusColumns.HJGH_LOGIN_STATUS},
                    null, null, null);
            if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {
                flag = cursor.getInt(0);
                Log.i(TAG, "getHJGHLoginStatus: flag:" + flag);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return flag;
    }
}
