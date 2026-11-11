package org.owasp.mobileshepherd.ui.challenges.clientsideinjection.helpers;

import android.content.Context;
import net.zetetic.database.sqlcipher.SQLiteDatabase;
import net.zetetic.database.sqlcipher.SQLiteOpenHelper;

public class Challenge2DatabaseHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "challenge2_injection.db";
    private static final int DATABASE_VERSION = 1;

    static {
        System.loadLibrary("sqlcipher");
    }

    public Challenge2DatabaseHelper(Context context, String password) {
        super(context, DATABASE_NAME, password, null, DATABASE_VERSION, 0, null, null, false);
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
