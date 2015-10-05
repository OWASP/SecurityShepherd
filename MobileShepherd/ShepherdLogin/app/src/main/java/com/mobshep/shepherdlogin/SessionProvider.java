package com.mobshep.shepherdlogin;


import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.SyncStateContract;
import android.widget.Toast;

import java.util.HashMap;

/**
 * This file is part of the Security Shepherd Project.
 *
 * The Security Shepherd project is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.<br/>
 *
 * The Security Shepherd project is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.<br/>
 *
 * You should have received a copy of the GNU General Public License
 * along with the Security Shepherd project.  If not, see <http://www.gnu.org/licenses/>.
 *
 * @author Sean Duggan
 */

public class SessionProvider extends ContentProvider {

    static final String PROVIDER_NAME = "com.mobshep.shepherdlogin.SessionProvider";

    static final String URL = "content://" + PROVIDER_NAME + "/data";
    static final Uri CONTENT_URL = Uri.parse(URL);


    static final String id = "id";
    static final String sessionValue= "sessionValue";
    static final int uriCode = 1;

    private static HashMap<String, String> values;

    // Used to match uris with Content Providers
    static final UriMatcher uriMatcher;

    protected SQLiteDatabase sqlDB;
    static final String DATABASE_NAME = "session";
    static final String TABLE_NAME = "sessions";
    static final int DATABASE_VERSION = 1;
    static final String CREATE_DB_TABLE = " CREATE TABLE " + TABLE_NAME
            + " (id INTEGER PRIMARY KEY AUTOINCREMENT, "
            + " sessionValue TEXT NOT NULL);";


    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(PROVIDER_NAME, "data", uriCode);
    }

    @Override
    public boolean onCreate() {

        DatabaseHelper dbHelper = new DatabaseHelper(getContext());
        sqlDB = dbHelper.getWritableDatabase();

        if (sqlDB != null) {
            return true;
        }
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        // Used to create a SQL query
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

        // Set table to query
        queryBuilder.setTables(TABLE_NAME);

        // Used to match uris with Content Providers
        switch (uriMatcher.match(uri)) {
            case uriCode:

                // A projection map maps from passed column names to database column names
                queryBuilder.setProjectionMap(values);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        // Cursor provides read and write access to the database
        Cursor cursor = queryBuilder.query(sqlDB, projection, selection, selectionArgs, null,
                null, null);

        // Register to watch for URI changes
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    // Handles requests for the MIME type (Type of Data) of the data at the URI
    @Override
    public String getType(Uri uri) {

        // Used to match uris with Content Providers
        switch (uriMatcher.match(uri)) {

            case uriCode:
                return "vnd.android.cursor.dir/data";

            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
    }

    // Used to insert a new row into the provider
    // Receives the URI (Uniform Resource Identifier) for the Content Provider and a set of values
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        //delete any previous sessions
        try{
       sqlDB.delete(TABLE_NAME, "id > ? ", new String[]{"0"});
        DatabaseHelper dbHelper = new DatabaseHelper(getContext());
        sqlDB = dbHelper.getWritableDatabase();
        }catch(SQLiteException e) {

        }

        // Gets the row id after inserting a map with the keys representing the the column
        // names and their values. The second attribute is used when you try to insert
        // an empty row
        long rowID = sqlDB.insert(TABLE_NAME, null, values);

        // Verify a row has been added
        if (rowID > 0) {

            // Append the given id to the path and return a Builder used to manipulate URI
            // references
            Uri _uri = ContentUris.withAppendedId(CONTENT_URL, rowID);

            // getContentResolver provides access to the content model
            // notifyChange notifies all observers that a row was updated
            getContext().getContentResolver().notifyChange(_uri, null);

            // Return the Builder used to manipulate the URI
            return _uri;
        }
        Toast.makeText(getContext(), "Row Insert Failed", Toast.LENGTH_LONG).show();
        return null;
    }

    // Deletes a row or a selection of rows
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int rowsDeleted = 0;

        // Used to match uris with Content Providers
        switch (uriMatcher.match(uri)) {
            case uriCode:
                rowsDeleted = sqlDB.delete(TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        // getContentResolver provides access to the content model
        // notifyChange notifies all observers that a row was updated
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsDeleted;
    }

    // Used to update a row or a selection of rows
    // Returns to number of rows updated
    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int rowsUpdated = 0;

        // Used to match uris with Content Providers
        switch (uriMatcher.match(uri)) {
            case uriCode:

                // Update the row or rows of data
                rowsUpdated = sqlDB.update(TABLE_NAME, values, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        // getContentResolver provides access to the content model
        // notifyChange notifies all observers that a row was updated
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsUpdated;

    }



    public static class DatabaseHelper extends SQLiteOpenHelper {
        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase sqlDB) {
            sqlDB.execSQL(CREATE_DB_TABLE);
        }

        // Recreates the table when the database needs to be upgraded
        @Override
        public void onUpgrade(SQLiteDatabase sqlDB, int oldVersion, int newVersion) {
            sqlDB.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            onCreate(sqlDB);
        }

        public void deleteData(){
            SQLiteDatabase sqlDB = getWritableDatabase();
            sqlDB.execSQL("DELETE FROM " + TABLE_NAME);
            sqlDB.close();
        }

    }
}


