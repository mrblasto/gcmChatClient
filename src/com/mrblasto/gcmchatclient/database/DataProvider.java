package com.mrblasto.gcmchatclient.database;

import android.content.*;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;
import com.mrblasto.gcmchatclient.ChatMessageListFragment;
import com.mrblasto.gcmchatclient.Common;

/**
 * Created by jeffreyfried on 4/4/15.
 * Based on DataProvider in http://www.appsrox.com/android/tutorials/instachat/2/#8
 */
public class DataProvider extends ContentProvider {
    public static final String COL_ID = "_id";

    public static String AUTHORITY = "com.mrblasto.gcmchatclient.provider";

    public static final String TABLE_MESSAGES = "messages";
    public static final String COL_MSG = "msg";
    public static final String COL_FROM = "fromName";
    public static final String COL_TO = "toName";
    public static final String COL_AT = "received";

    public static final String TABLE_PROFILES = "profiles";
    public static final String COL_NAME = "name";
//    public static final String COL_EMAIL = "email";
    public static final String COL_COUNT = "count";

    public static final Uri ALL_MESSAGES = Uri.parse("content://com.mrblasto.gcmchatclient.provider/messages");
    public static final Uri ALL_PROFILES = Uri.parse("content://com.mrblasto.gcmchatclient.provider/profiles");
    public static final Uri FILTERED_MESSAGES
            = Uri.parse("content://com.mrblasto.gcmchatclient.provider/messages/#");
    public static final Uri FILTERED_PROFILES = Uri.parse("content://com.mrblasto.gcmchatclient.provider/profiles/#");

    public static final int MESSAGES_ALLROWS = 1;
    public static final int MESSAGES_SINGLE_ROW = 2;
    public static final int PROFILE_ALLROWS = 3;
    public static final int PROFILE_SINGLE_ROW = 4;

    private DbHelper dbHelper;
    private Context context;

    private class DbHelper extends SQLiteOpenHelper {


        private static final String DATABASE_NAME = "gcmchatclient.db";
        private static final int DATABASE_VERSION = 8;

        public DbHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
            DataProvider.this.context = context;
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("create table " + TABLE_MESSAGES
                    + " (_id integer primary key autoincrement, msg text, toName text, fromName text, received datetime default current_timestamp);");
            db.execSQL("create table " + TABLE_PROFILES
                    + " (_id integer primary key autoincrement, name text unique, count integer default 0);");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("drop table if exists " + TABLE_MESSAGES);
            db.execSQL("drop table if exists " + TABLE_PROFILES);
            this.onCreate(db);
        }
    }

    @Override
    public boolean onCreate() {
        dbHelper = new DbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        Log.w("query", uri.toString());

        switch(uriMatcher.match(uri)) {
            case MESSAGES_ALLROWS:
            case PROFILE_ALLROWS:
                qb.setTables(getTableName(uri));
                break;

            case MESSAGES_SINGLE_ROW:
            case PROFILE_SINGLE_ROW:
                qb.setTables(getTableName(uri));
                qb.appendWhere("_id = " + uri.getLastPathSegment());
                break;

            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }

        Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, sortOrder);
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    private void insertOrUpdateProfileMessageCount(String name) {
        if(Common.getLastFragment().equals(ChatMessageListFragment.class.getName())
                && Common.getCurrentContact().equals(name)) {
            return; // don't update count if viewing messages
        }
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(TABLE_PROFILES);
        qb.appendWhere(COL_NAME + " = " + name);
        String [] projection = {COL_ID};
        Cursor c = db.rawQuery("SELECT " + COL_COUNT + " FROM " + TABLE_PROFILES
                                + " WHERE " + COL_NAME + " = ?", new String[]{name});

        ContentValues values = new ContentValues();

        if(c.getCount() > 0) {
            //db.execSQL("update profiles set count=count+1 where name = ?", new Object[]{name});
            c.moveToFirst();
            int count = c.getInt(c.getColumnIndex(COL_COUNT));
            count++;
            values.put(COL_COUNT, count);
            db.update(TABLE_PROFILES, values, "name = ?", new String[]{name});
        }
        else {
            values.put(COL_NAME, name);
            values.put(COL_COUNT, 1);
            db.insert(TABLE_PROFILES, null, values);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        long id;
        switch(uriMatcher.match(uri)) {
            case MESSAGES_ALLROWS:
                id = db.insertOrThrow(TABLE_MESSAGES, null, values);
                if (values.get(COL_TO) == null) {
                    //db.execSQL("update profiles set count=count+1 where name = ?", new Object[]{values.get(COL_FROM)});
                    this.insertOrUpdateProfileMessageCount(values.getAsString(COL_FROM));
                    // notify profile to update count.
                    getContext().getContentResolver().notifyChange(ALL_PROFILES, null);
                }
                break;

            case PROFILE_ALLROWS:
                id = db.insertOrThrow(TABLE_PROFILES, null, values);
                break;

            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }

        Uri insertUri = ContentUris.withAppendedId(uri, id);
        getContext().getContentResolver().notifyChange(insertUri, null);
        return insertUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        int count;
        switch(uriMatcher.match(uri)) {
            case MESSAGES_ALLROWS:
            case PROFILE_ALLROWS:
                count = db.delete(getTableName(uri), selection, selectionArgs);
                break;

            case MESSAGES_SINGLE_ROW:
            case PROFILE_SINGLE_ROW:
                count = db.delete(getTableName(uri), "_id = ?", new String[]{uri.getLastPathSegment()});
                break;

            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int count;
        switch(uriMatcher.match(uri)) {
            case MESSAGES_ALLROWS:
            case PROFILE_ALLROWS:
                count = db.update(getTableName(uri), values, selection, selectionArgs);
                break;

            case MESSAGES_SINGLE_ROW:
            case PROFILE_SINGLE_ROW:
                count = db.update(getTableName(uri), values, "_id = ?", new String[]{uri.getLastPathSegment()});
                break;

            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    private String getTableName(Uri uri) {
        switch(uriMatcher.match(uri)) {
            case MESSAGES_ALLROWS:
            case MESSAGES_SINGLE_ROW:
                return TABLE_MESSAGES;

            case PROFILE_ALLROWS:
            case PROFILE_SINGLE_ROW:
                return TABLE_PROFILES;
        }
        return null;
    }

    private static final UriMatcher uriMatcher;
    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI("com.mrblasto.gcmchatclient.provider", "messages", MESSAGES_ALLROWS);
        uriMatcher.addURI("com.mrblasto.gcmchatclient.provider", "messages/#", MESSAGES_SINGLE_ROW);
        uriMatcher.addURI("com.mrblasto.gcmchatclient.provider", "profiles", PROFILE_ALLROWS);
        uriMatcher.addURI("com.mrblasto.gcmchatclient.provider", "profiles/#", PROFILE_SINGLE_ROW);
    }

    public void resetDatabase() {
        dbHelper.close();
        dbHelper = new DbHelper(context);
    }
}
