package com.app.mobshep.csinjection3;

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
import android.widget.TextView;
import android.widget.Toast;

public class CSInjection3 extends Activity implements OnClickListener {

	TabHost th;
	Button Login;
	EditText username;
	EditText password;
	EditText key;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.broken);
		th = (TabHost) findViewById(R.id.tabhost);
		populateTable(this, "P93Eid3D33DE0ZanbffGp01Sirjw2");
		referenceXML();
		th.setup();

		TabSpec specs = th.newTabSpec("tag1");
		specs.setContent(R.id.tab1);
		specs.setIndicator("Login");
		th.addTab(specs);

		specs = th.newTabSpec("tag2");
		specs.setContent(R.id.tab2);
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
			
	
	
	
			String unsanitizeName = username.getText().toString();
			String unsanitizePass = password.getText().toString();
			
			String sanitizeName = unsanitizeName.replace("OR", "/* */");
			sanitizeName = sanitizeName.replace("or", "/* */");
			sanitizeName = sanitizeName.replace("Or", "/* */");
			sanitizeName = sanitizeName.replace("oR", "/* */");
			sanitizeName = sanitizeName.replace("SELECT", "/* */");
			sanitizeName = sanitizeName.replace("AND", "/* */");
			sanitizeName = sanitizeName.replace("UPDATE", "/* */");
			sanitizeName = sanitizeName.replace("DROP", "/* */");
			sanitizeName = sanitizeName.replace("1=1", "/* */");
			sanitizeName = sanitizeName.replace("1 = 1", "/* */");
			sanitizeName = sanitizeName.replace("'", "/* */");
			
			
			
			String sanitizePass = unsanitizePass.replace("OR", "/* */");
			sanitizePass = sanitizePass.replace("or", "/* */");
			sanitizePass = sanitizePass.replace("Or", "/* */");
			sanitizePass = sanitizePass.replace("oR", "/* */");
			sanitizePass = sanitizePass.replace("SELECT", "/* */");
			sanitizePass = sanitizePass.replace("AND", "/* */");
			sanitizePass = sanitizePass.replace("UPDATE", "/* */");
			sanitizePass = sanitizePass.replace("DROP", "/* */");
			sanitizePass = sanitizePass.replace("1=1", "/* */");
			sanitizePass = sanitizePass.replace("1 = 1", "/* */");
			sanitizeName = sanitizeName.replace("'", "/* */");

			

			if (login(sanitizeName, sanitizePass) == true) {
				key.setText("The Key is: BurpingChimneys.");
				Toast toast = Toast.makeText(CSInjection3.this, "Logged in as:" + sanitizeName,
						Toast.LENGTH_LONG);
				toast.show();
			}

			if (login(sanitizeName, sanitizePass) == false) {
				Toast toast = Toast.makeText(CSInjection3.this,
						"Invalid Credentials!", Toast.LENGTH_SHORT);
				toast.show();

			}

			if (sanitizeName.contentEquals("") || sanitizePass.contentEquals("")) {
				Toast toast2 = Toast.makeText(CSInjection3.this,
						"Empty Fields Detected.", Toast.LENGTH_SHORT);
				toast2.show();

			}

			else {

				Toast toast = Toast.makeText(CSInjection3.this,
						"Invalid Credentials, "  + sanitizeName, Toast.LENGTH_SHORT);
				toast.show();
			}
		}
	}

	private boolean login(String username, String password) {
		try {
			
			String dbPath = this.getDatabasePath("Users.db").getPath();

			try{
				
			
			SQLiteDatabase db = SQLiteDatabase.openDatabase(dbPath, "P93Eid3D33DE0ZanbffGpo101Sirjw2", null, SQLiteDatabase.NO_LOCALIZED_COLLATORS);

			String query = ("SELECT * FROM Users WHERE memName = '"
					+ username + "' AND memPass ='" + password + "'");
			Cursor cursor = db.rawQuery(query, null);
			
			if (cursor.getCount() <= 0) {
				return false;
			}

			}catch(Exception e){
				Toast error = Toast.makeText(CSInjection3.this, "An Error occured",
						Toast.LENGTH_SHORT);
				error.show();
			}
		} catch (SQLiteException e) {
			e.printStackTrace();
			Toast error = Toast.makeText(CSInjection3.this, "An Error occured",
					Toast.LENGTH_SHORT);
			error.show();
		}

		return true;

	}

	public void populateTable(Context context, String dbpassword) {
		try {
			SQLiteDatabase.loadLibs(context);

			String dbPath = context.getDatabasePath("Users.db").getPath();

			File dbPathFile = new File(dbPath);
			if (!dbPathFile.exists())
				dbPathFile.getParentFile().mkdirs();

			SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(dbPath,
					dbpassword, null);

			db.execSQL("DROP TABLE IF EXISTS Users");
			db.execSQL("CREATE TABLE Users(memID INTEGER PRIMARY KEY AUTOINCREMENT, memName TEXT, memAge INTEGER, memPass VARCHAR)");

			db.execSQL("INSERT INTO Users VALUES( 1,'Admin',20,'A3B922DF010PQSI827')");
			db.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Toast error = Toast.makeText(CSInjection3.this,
					"An error occurred.", Toast.LENGTH_LONG);
			error.show();

		}
	}
	
}
