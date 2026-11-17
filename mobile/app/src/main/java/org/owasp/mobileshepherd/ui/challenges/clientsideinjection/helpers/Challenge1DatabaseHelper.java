package org.owasp.mobileshepherd.ui.challenges.clientsideinjection.helpers;

import android.content.Context;
import net.zetetic.database.sqlcipher.SQLiteDatabase;
import net.zetetic.database.sqlcipher.SQLiteOpenHelper;

public class Challenge1DatabaseHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "challenge1_injection.db";
    private static final int DATABASE_VERSION = 1;

    static {
        System.loadLibrary("sqlcipher");
    }

    public Challenge1DatabaseHelper(Context context, String password) {
        super(context, DATABASE_NAME, password, null, DATABASE_VERSION, 0, null, null, false);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE accounts (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "username TEXT NOT NULL, " +
                "password TEXT NOT NULL, " +
                "role TEXT, " +
                "balance INTEGER DEFAULT 0)";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS accounts");
        onCreate(db);
    }
}
