package org.owasp.mobileshepherd.ui.challenges.clientsideinjection.helpers;

import android.content.Context;
import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteOpenHelper;

public class Challenge2DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "challenge2_injection.db";
    private static final int DATABASE_VERSION = 1;

    public Challenge2DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        SQLiteDatabase.loadLibs(context);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createProductsTable = "CREATE TABLE products (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT NOT NULL, " +
                "category TEXT, " +
                "price REAL, " +
                "stock INTEGER DEFAULT 0)";
        db.execSQL(createProductsTable);

        String createSecretsTable = "CREATE TABLE secrets (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "secret_key TEXT NOT NULL, " +
                "secret_value TEXT NOT NULL)";
        db.execSQL(createSecretsTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS products");
        db.execSQL("DROP TABLE IF EXISTS secrets");
        onCreate(db);
    }
}
