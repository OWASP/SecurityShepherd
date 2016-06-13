package com.mobshep.mobileshepherd;

import java.io.File;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.Toast;


/**
 * This file is part of the Security Shepherd Project.
 * 
 * The Security Shepherd project is free software: you can redistribute it
 * and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.<br/>
 * 
 * The Security Shepherd project is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.<br/>
 * 
 * You should have received a copy of the GNU General Public License along with
 * the Security Shepherd project. If not, see <http://www.gnu.org/licenses/>.
 * 
 * @author Sean Duggan
 */

public class Insecure_Data_Storage extends MainActivity {

    SQLiteDatabase Members = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        createDatabase();
        insertKey();
        setContentView(R.layout.ids);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    public void createDatabase() {
        try {
            Members = this.openOrCreateDatabase("Members", MODE_PRIVATE, null);
            Members.execSQL("CREATE TABLE IF NOT EXISTS Members " +
                            "(id integer primary key, name VARCHAR, password VARCHAR);"
            );

            File database = getApplication().getDatabasePath("Members.db");

            if (!database.exists()) {
                Toast.makeText(this, "Database Created", Toast.LENGTH_SHORT).show();
            } else
                Toast.makeText(this, "Database Missing", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Log.e("DB ERROR", "Error Creating Database");
        }
    }

    public void insertKey(){
        Members.execSQL("DELETE FROM Members;");
        Members.execSQL("INSERT INTO Members (name, password) VALUES ('Admin','Battery777');");
    }

}