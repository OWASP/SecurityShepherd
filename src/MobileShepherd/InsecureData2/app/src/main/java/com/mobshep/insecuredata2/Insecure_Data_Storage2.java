package com.mobshep.insecuredata2;

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
import android.widget.Button;
import android.widget.EditText;
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

public class Insecure_Data_Storage2 extends Activity {

	SQLiteDatabase passwordDB = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ids);
		createDatabase();
        insertKey();

	}

	public void createDatabase() {
		try {
			passwordDB = this.openOrCreateDatabase("passwordDB", MODE_PRIVATE, null);
			passwordDB.execSQL("CREATE TABLE IF NOT EXISTS passwordDB " +
							"(id integer primary key, name VARCHAR, password VARCHAR);"
			);

			File database = getApplication().getDatabasePath("passwordDB.db");

			if (!database.exists()) {
				Toast.makeText(this, "Database Created", Toast.LENGTH_SHORT).show();
			} else
				Toast.makeText(this, "Database Missing", Toast.LENGTH_SHORT).show();

		} catch (Exception e) {
			Log.e("DB ERROR", "Error Creating Database");
		}
	}

	public void insertKey(){
		passwordDB.execSQL("INSERT INTO passwordDB (name, password) VALUES ('Admin','0e3a0c8c3a571a855c958813d9b851a1');");
	}

}
