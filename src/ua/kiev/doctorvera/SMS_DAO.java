package ua.kiev.doctorvera;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

class SMS_DAO extends SQLiteOpenHelper {

    private final String LOG_TAG = "myLogs";
    private final String SMS_TABLE = "sms";
    private final String KEY_ID = "id";
    private final String KEY_PHONE_NUMBER = "phone_number";
    private final String KEY_SMS_TEXT = "sms_text";
    private final String KEY_SMS_STATE = "sms_state";
    private final String KEY_SMS_DATE_SENT = "sms_date_sent";
    private final String KEY_SMS_DATE_CHANGED = "sms_date_changed";
    private final String KEY_TRACKING_ID = "tracking_id";

    public SMS_DAO(Context context) {
        // конструктор суперкласса
        super(context, "DoctorVeraSMS", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(LOG_TAG, "--- onCreate database ---");


        // создаем таблицу с полями
        db.execSQL("create table " + SMS_TABLE + " ("
                + KEY_ID + " integer primary key autoincrement,"
                + KEY_PHONE_NUMBER + " text,"
                + KEY_SMS_TEXT + " text,"
                + KEY_SMS_STATE + " integer,"
                + KEY_SMS_DATE_SENT + " integer,"
                + KEY_SMS_DATE_CHANGED + " integer,"
                + KEY_TRACKING_ID + " integer" +
                ");");

        Cursor dbCursor = db.query(SMS_TABLE, null, null, null, null, null, null);
        String[] columnNames = dbCursor.getColumnNames();
        Log.d(LOG_TAG, "Created table, name:" + SMS_TABLE + ". Columns:");
        for (String value : columnNames) {
            Log.d(LOG_TAG, value);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(LOG_TAG, "--- onUpgrade database Not implemented ---");

    }

    // Adding new sms
    public long addSMS(SMS sms) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_PHONE_NUMBER, sms.getPhoneNumber()); // Phone Number
        values.put(KEY_SMS_TEXT, sms.getText()); // SMS Text
        values.put(KEY_SMS_STATE, sms.getState()); // SMS State
        values.put(KEY_SMS_DATE_SENT, sms.getDateSent().getTime()); // SMS Date Sent
        if (sms.getTrackingId() != null)
            values.put(KEY_TRACKING_ID, sms.getTrackingId()); // SMS TrackingId

        // Inserting Row
        long id = db.insert(SMS_TABLE, null, values);
        db.close(); // Closing database connection

        return id;
    }

    // Getting single sms
    public SMS getSMS(int id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(SMS_TABLE, new String[]{KEY_ID,
                        KEY_PHONE_NUMBER, KEY_SMS_TEXT, KEY_SMS_STATE, KEY_SMS_DATE_SENT, KEY_SMS_DATE_CHANGED, KEY_TRACKING_ID}, KEY_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null, null
        );
        if (cursor != null)
            cursor.moveToFirst();

        SMS sms = new SMS();
        assert cursor != null;
        sms.setId(Integer.parseInt(cursor.getString(0)));
        sms.setPhoneNumber(cursor.getString(1));
        sms.setText(cursor.getString(2));
        sms.setState(Byte.parseByte(cursor.getString(3)));
        sms.setDateSent(new Date(cursor.getLong(4)));
        if (cursor.getString(5) != null && !cursor.getString(5).isEmpty())
            sms.setDateChanged(new Date(cursor.getLong(5)));
        if (cursor.getString(6) != null && !cursor.getString(6).isEmpty())
            sms.setTrackingId(Long.parseLong(cursor.getString(6)));
        db.close();
        // return contact
        return sms;
    }

    // Getting All SMS
    public List<SMS> getAllSMS() {
        List<SMS> smsList = new ArrayList<SMS>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + SMS_TABLE;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                SMS sms = new SMS();
                sms.setId(Integer.parseInt(cursor.getString(0)));
                sms.setPhoneNumber(cursor.getString(1));
                sms.setText(cursor.getString(2));
                sms.setState(Byte.parseByte(cursor.getString(3)));
                sms.setDateSent(new Date(cursor.getLong(4)));
                if (cursor.getString(5) != null && !cursor.getString(5).isEmpty())
                    sms.setDateChanged(new Date(cursor.getLong(5)));
                if (cursor.getString(6) != null && !cursor.getString(6).isEmpty())
                    sms.setTrackingId(Long.parseLong(cursor.getString(6)));

                // Adding contact to list
                smsList.add(sms);
            } while (cursor.moveToNext());
        }
        db.close();
        // return contact list
        return smsList;
    }

    // Getting All SMS with specific state
    public List<SMS> getAllSMS(byte state) {
        List<SMS> smsList = new ArrayList<SMS>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(SMS_TABLE, new String[]{KEY_ID,
                        KEY_PHONE_NUMBER, KEY_SMS_TEXT, KEY_SMS_STATE, KEY_SMS_DATE_SENT, KEY_SMS_DATE_CHANGED, KEY_TRACKING_ID}, KEY_SMS_STATE + "=?",
                new String[]{String.valueOf(state)}, null, null, null, null
        );

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                SMS sms = new SMS();
                sms.setId(Integer.parseInt(cursor.getString(0)));
                sms.setPhoneNumber(cursor.getString(1));
                sms.setText(cursor.getString(2));
                sms.setState(Byte.parseByte(cursor.getString(3)));
                sms.setDateSent(new Date(cursor.getLong(4)));
                if (cursor.getString(5) != null && !cursor.getString(5).isEmpty())
                    sms.setDateChanged(new Date(cursor.getLong(5)));
                if (cursor.getString(6) != null && !cursor.getString(6).isEmpty())
                    sms.setTrackingId(Long.parseLong(cursor.getString(6)));

                // Adding contact to list
                smsList.add(sms);
            } while (cursor.moveToNext());
        }
        db.close();
        // return contact list
        return smsList;
    }

    // Getting SMS Count
    public int getSMSCount() {
        String countQuery = "SELECT  * FROM " + SMS_TABLE;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        cursor.close();
        db.close();
        return cursor.getCount();
    }

    // Getting SMS Count with current state
    public int getSMSCount(byte state) {
        //List<SMS> smsList = new ArrayList<SMS>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(SMS_TABLE, new String[]{KEY_ID,
                        KEY_PHONE_NUMBER, KEY_SMS_TEXT, KEY_SMS_STATE, KEY_SMS_DATE_SENT, KEY_SMS_DATE_CHANGED, KEY_TRACKING_ID}, KEY_SMS_STATE + "=?",
                new String[]{String.valueOf(state)}, null, null, null, null
        );
        db.close();
        return cursor.getCount();
    }

    // Updating single SMS
    public int updateSMS(SMS sms) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_PHONE_NUMBER, sms.getPhoneNumber()); // Phone Number
        values.put(KEY_SMS_TEXT, sms.getText()); // SMS Text
        values.put(KEY_SMS_STATE, sms.getState()); // SMS State
        values.put(KEY_SMS_DATE_SENT, sms.getDateSent().getTime()); // SMS Date Sent
        if (sms.getDateChanged() != null)
            values.put(KEY_SMS_DATE_CHANGED, sms.getDateChanged().getTime()); // SMS Date Changed
        if (sms.getTrackingId() != null)
            values.put(KEY_TRACKING_ID, sms.getTrackingId()); // SMS TrackingId
        // updating row
        return db.update(SMS_TABLE, values, KEY_ID + " = ?",
                new String[]{String.valueOf(sms.getId())});
    }

    // Deleting single contact
    public void deleteSMS(SMS sms) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(SMS_TABLE, KEY_ID + " = ?",
                new String[]{String.valueOf(sms.getId())});
        db.close();
    }

};
