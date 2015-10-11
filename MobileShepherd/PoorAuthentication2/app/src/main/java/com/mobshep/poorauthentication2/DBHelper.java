package com.mobshep.poorauthentication2;

import android.content.Context;
import android.database.sqlite.SQLiteException;
import android.util.Log;
import android.widget.Toast;

import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteStatement;


import java.io.File;
import java.sql.PreparedStatement;

/**
 * Created by sean on 11/10/2015.
 */
public class DBHelper {

    protected void createKeyTable(Context context)
    {

        SQLiteDatabase.loadLibs(context);

        String dbPath = context.getDatabasePath("Users.db").getPath();
        SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(dbPath,
                "", null);
    }

    public void insertData(Context context, String Username, String Password){
        SQLiteDatabase.loadLibs(context);

        String dbPath = context.getDatabasePath("Users.db").getPath();
        SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(dbPath,
                "", null);

        SQLiteStatement stmt = db.compileStatement("INSERT INTO Users (username,password) VALUES(?,?)");
        stmt.bindString(1, Username);
        stmt.bindString(2, Password);
        stmt.execute();
    }

    public void populateTable(Context context, String password) {
        try {
            try {
                SQLiteDatabase.loadLibs(context);

                String dbPath = context.getDatabasePath("Users.db").getPath();

                File dbPathFile = new File(dbPath);
                if (!dbPathFile.exists())
                    dbPathFile.getParentFile().mkdirs();

                SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(dbPath,
                        "", null);

                db.execSQL("DROP TABLE IF EXISTS Users");
                db.execSQL("CREATE TABLE Users(memID INTEGER PRIMARY KEY AUTOINCREMENT, username TEXT, password VARCHAR)");


            } catch (Exception e) {
                // TODO Auto-generated catch block
                Log.d("DBHelper", "The following error occured:" + e.getMessage());

            }

        } catch (SQLiteException e) {
            Log.i ("", "An database error occurred.");
        }
    }
}
