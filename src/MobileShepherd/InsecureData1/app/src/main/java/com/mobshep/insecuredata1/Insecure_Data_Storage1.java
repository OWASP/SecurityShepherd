package com.mobshep.insecuredata1;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.app.Activity;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

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

public class Insecure_Data_Storage1 extends Activity {

    SQLiteDatabase Users = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ids);
        createDatabase();
        insertData();
    }

    public void createDatabase() {
        try {
            Users = this.openOrCreateDatabase("Users", MODE_PRIVATE, null);
            Users.execSQL("CREATE TABLE IF NOT EXISTS Users " +
                            "(id integer primary key, name VARCHAR, password VARCHAR);"
            );

            File database = getApplication().getDatabasePath("Users.db");

            if (!database.exists()) {
                Toast.makeText(this, "Database Created", Toast.LENGTH_SHORT).show();
            } else
                Toast.makeText(this, "Database Missing", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Log.e("DB ERROR", "Error Creating Database");
        }
    }

    public void insertData(){

        Users.execSQL("DELETE FROM Users;");

        Users.execSQL("INSERT INTO Users (name, password) VALUES ('Tyrkyr','ZG9jaGRvY2hkb2No');");
        Users.execSQL("INSERT INTO Users (name, password) VALUES ('ToothBrush','MmNvb2w0dWxvbD8=');");
        Users.execSQL("INSERT INTO Users (name, password) VALUES ('TroolMann','QnJpZGdlcw==');");
        Users.execSQL("INSERT INTO Users (name, password) VALUES ('Patrick','ZGlub3NhdXI=');");
        Users.execSQL("INSERT INTO Users (name, password) VALUES ('bottles','cGFzc3dvcmQxMjM0');");
        Users.execSQL("INSERT INTO Users (name, password) VALUES ('Root','V2Fyc2hpcHNBbmRXcmVuY2hlcw==');");
    }

}
