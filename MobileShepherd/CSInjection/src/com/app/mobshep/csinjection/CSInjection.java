package com.app.mobshep.csinjection;

import java.io.File;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import net.sqlcipher.database.*;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.Toast;

public class CSInjection extends Activity implements OnClickListener {

	TabHost th;
	Button Login;
	EditText username;
	EditText password;
	EditText key;
	String dbPass = "37e44d547f20a9f3ca9ac7d625486b7b";


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		

		super.onCreate(savedInstanceState);
		setContentView(R.layout.broken);
		th = (TabHost) findViewById(R.id.tabhost);
		populateTable(this, "dbPass");
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

	}

	public void onClick(View arg0) {
		switch (arg0.getId()) {

		case (R.id.bLogin):

			String CheckName = username.getText().toString();
			String CheckPass = password.getText().toString();

			if (login(CheckName, CheckPass) == true) {
				key.setText("The Key is: VolcanicEruptionsAbruptInterruptions.");
				Toast toast = Toast.makeText(CSInjection.this, "Logged in!",
						Toast.LENGTH_LONG);
				toast.show();
			}

			if (login(CheckName, CheckPass) == false) {
				Toast toast = Toast.makeText(CSInjection.this,
						"Invalid Credentials!", Toast.LENGTH_SHORT);
				toast.show();

			}

			if (CheckName.contentEquals("") || CheckPass.contentEquals("")) {
				Toast toast2 = Toast.makeText(CSInjection.this,
						"Empty Fields Detected.", Toast.LENGTH_SHORT);
				toast2.show();

			}

			else {

				Toast toast = Toast.makeText(CSInjection.this,
						"Invalid Credentials!", Toast.LENGTH_SHORT);
				toast.show();

			}
		}
	}

	private boolean login(String username, String password) {
		try {
			String dbPath = this.getDatabasePath("Members.db").getPath();

			SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(dbPath,
					dbPass, null);

			String query = ("SELECT * FROM MEMBERS WHERE memName = '"
					+ username + "' AND memPass ='" + password + "'");
			Cursor cursor = db.rawQuery(query, null);

			if (cursor.getCount() <= 0) {
				return false;
			}

		} catch (SQLiteException e) {
			e.printStackTrace();
			Toast error = Toast.makeText(CSInjection.this, "An Error occured",
					Toast.LENGTH_SHORT);
			error.show();
			
			
		}

		return true;

	}

	public void populateTable(Context context, String password) {
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
			db.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Toast error = Toast.makeText(CSInjection.this,
					"An error occurred.", Toast.LENGTH_LONG);
			error.show();

		}
	}

}
