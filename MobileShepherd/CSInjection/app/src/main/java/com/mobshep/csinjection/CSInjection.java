package com.mobshep.csinjection;

import java.io.File;
import java.io.IOException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;
import android.widget.Toast;

import net.sqlcipher.database.SQLiteDatabase;

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

public class CSInjection extends Activity implements OnClickListener {

	TabHost th;
	Button Login;
	EditText username;
	TextView loginTitle;
	EditText password;
	EditText key;
	String dbPass = "37e44d547f20a9f3ca9ac7d625486b7b";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub

		super.onCreate(savedInstanceState);
		setContentView(R.layout.csi);
		th = (TabHost) findViewById(R.id.tabhost);
		populateTable(this, "dbPass");
		generateKey(this, "dbPass");
		referenceXML();
		th.setup();

		TabSpec specs = th.newTabSpec("tag1");
		specs.setContent(R.id.tab2);
		specs.setIndicator("Login");
		th.addTab(specs);

		specs = th.newTabSpec("tag2");
		specs.setContent(R.id.tab3);
		specs.setIndicator("Key");
		th.addTab(specs);
	}

	private void referenceXML() {
		// TODO Auto-generated method stub		
		Login = (Button) findViewById(R.id.bLogin);
		// Login.setFilterTouchesWhenObscured(true);
		username = (EditText) findViewById(R.id.etName);
		password = (EditText) findViewById(R.id.etPass);
		key = (EditText) findViewById(R.id.etKey);
		Login.setOnClickListener(this);
		loginTitle = (TextView) findViewById(R.id.tvTitle);
		
		th = (TabHost) findViewById(R.id.tabhost);		

	}

	public void onClick(View arg0) {
		switch (arg0.getId()) {

		case (R.id.bLogin):

			String CheckName = username.getText().toString();
			String CheckPass = password.getText().toString();

			try {
				if (login(CheckName, CheckPass) == true) {
					outputKey(this, dbPass);
					Toast login = Toast.makeText(CSInjection.this,
							"Logged in!", Toast.LENGTH_LONG);
					login.show();

				}
			} catch (IOException e1) {
				Toast error = Toast.makeText(CSInjection.this,
						"An error occurred!", Toast.LENGTH_LONG);
				error.show();
			}

			try {
				if (login(CheckName, CheckPass) == false) {
					Toast invalid = Toast.makeText(CSInjection.this,
							"Invalid Credentials!", Toast.LENGTH_SHORT);
					invalid.show();

				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			if (CheckName.contentEquals("") || CheckPass.contentEquals("") || CheckPass.contentEquals("A3B922DF010PQSI827")) {
				Toast empty = Toast.makeText(CSInjection.this,
						"Empty Fields Detected.", Toast.LENGTH_SHORT);
				empty.show();

			}
		}
	}

	private boolean login(String username, String password) throws IOException {
try{
		try {
			String dbPath = this.getDatabasePath("Members.db").getPath();

			SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(dbPath,
					dbPass, null);

			String query = ("SELECT * FROM MEMBERS WHERE memName='" + username
					+ "' AND memPass = '" + password + "';");

			Cursor cursor = db.rawQuery(query, null);
			if (cursor != null) {
				if (cursor.getCount() <= 0) {
					cursor.close();
					return false;
				}
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			Toast error = Toast.makeText(CSInjection.this,
					"An error occurred.", Toast.LENGTH_LONG);
			error.show();
			key.getText().clear();
			key.setHint("The key is only shown to authenticated users.");
			return false;
		}
			
		} catch (SQLiteException e) {
			Toast error = Toast.makeText(CSInjection.this,
					"An database error occurred.", Toast.LENGTH_LONG);
			error.show();
		}

		return true;

	}

	public void populateTable(Context context, String password) {
		try {
			try {
				SQLiteDatabase.loadLibs(context);

				String dbPath = context.getDatabasePath("Members.db").getPath();

				File dbPathFile = new File(dbPath);
				if (!dbPathFile.exists())
					dbPathFile.getParentFile().mkdirs();

				SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(dbPath,
						dbPass, null);

				db.execSQL("DROP TABLE IF EXISTS Members");
				db.execSQL("CREATE TABLE Members(memID INTEGER PRIMARY KEY AUTOINCREMENT, memName TEXT, memAge INTEGER, memPass VARCHAR)");

				db.execSQL("INSERT INTO Members VALUES( 1,'Admin',20,'A3B922DF010PQSI827')");

			} catch (Exception e) {
				// TODO Auto-generated catch block
				Toast error = Toast.makeText(CSInjection.this,
						"An error occurred.", Toast.LENGTH_LONG);
				error.show();

			}

		} catch (SQLiteException e) {
			Toast error = Toast.makeText(CSInjection.this,
					"An database error occurred.", Toast.LENGTH_LONG);
			error.show();
		}
	}

	public void outputKey(Context context, String password) {
		SQLiteDatabase.loadLibs(context);

		String dbPath = context.getDatabasePath("key.db").getPath();

		SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(dbPath, dbPass,
				null);

		String query = ("SELECT * FROM key;");

		Cursor cursor = db.rawQuery(query, null);

		if (cursor != null) {

			try {
				if (cursor.moveToFirst())
					key.setText(cursor.getString(0));
			} finally {
				cursor.close();

			}
		}
	}

	public void generateKey(Context context, String password) {
		try {
			try {
				SQLiteDatabase.loadLibs(context);

				String dbPath = context.getDatabasePath("key.db").getPath();

				File dbPathFile = new File(dbPath);
				if (!dbPathFile.exists())
					dbPathFile.getParentFile().mkdirs();

				SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(dbPath,
						dbPass, null);

				db.execSQL("DROP TABLE IF EXISTS key");
				db.execSQL("CREATE TABLE key(key VARCHAR)");

				db.execSQL("INSERT INTO key VALUES('The Key is VolcanicEruptionsAbruptInterruptions.')");

			} catch (Exception e) {
				// TODO Auto-generated catch block
				Toast error = Toast.makeText(CSInjection.this,
						"An error occurred.", Toast.LENGTH_LONG);
				error.show();

			}

		} catch (SQLiteException e) {
			Toast error = Toast.makeText(CSInjection.this,
					"An database error occurred.", Toast.LENGTH_LONG);
			error.show();
		}
	}


}
