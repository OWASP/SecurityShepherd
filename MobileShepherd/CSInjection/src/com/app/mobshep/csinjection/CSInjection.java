package com.app.mobshep.csinjection;

import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;
import android.widget.Toast;

public class CSInjection extends Activity implements OnClickListener {

	TabHost th;
	TextView Intro;
	Button Login;
	EditText username;
	EditText password;
	EditText key;
	int loginAttempts = 0;
	TextView hintView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.broken);
		th = (TabHost) findViewById(R.id.tabhost);
		populateTable();
		referenceXML();
		th.setup();

		// Set up each tab
		TabSpec specs = th.newTabSpec("tag1");
		specs.setContent(R.id.tab1);
		specs.setIndicator("Summary");
		th.addTab(specs);

		specs = th.newTabSpec("tag2");
		specs.setContent(R.id.tab2);
		specs.setIndicator("Login");
		th.addTab(specs);

		specs = th.newTabSpec("tag3");
		specs.setContent(R.id.tab3);
		specs.setIndicator("Key");
		th.addTab(specs);
	}

	private void referenceXML() {
		// TODO Auto-generated method stub
		Login = (Button) findViewById(R.id.bLogin);
		username = (EditText) findViewById(R.id.etName);
		password = (EditText) findViewById(R.id.etPass);
		key = (EditText) findViewById(R.id.etKey);
		hintView = (TextView) findViewById(R.id.hView);
		Login.setOnClickListener(this);

	}

	public void onClick(View arg0) {
		switch (arg0.getId()) {

		case (R.id.bLogin):

			String CheckName = username.getText().toString();
			String CheckPass = password.getText().toString();

			if (login(CheckName, CheckPass) == true) {
				key.setText("dnmwiuqodj72tf7gsvkjxq0jxnq9ws83.");
				Toast toast = Toast.makeText(CSInjection.this, "Logged in!",
						Toast.LENGTH_LONG);
				toast.show();
			}

			if (login(CheckName, CheckPass) == false) {
				Toast toast = Toast.makeText(CSInjection.this,
						"Invalid Credentials!", Toast.LENGTH_SHORT);
				toast.show();
				loginAttempts = loginAttempts + 1;
				checkAttempts();
			}

			if (CheckName.contentEquals("") || CheckPass.contentEquals("")) {
				Toast toast2 = Toast.makeText(CSInjection.this,
						"Empty Fields Detected.", Toast.LENGTH_SHORT);
				toast2.show();
				loginAttempts = loginAttempts + 1;
				checkAttempts();
			}

			else {

				Toast toast = Toast.makeText(CSInjection.this,
						"Invalid Credentials!", Toast.LENGTH_SHORT);
				toast.show();
				loginAttempts = loginAttempts + 1;
				checkAttempts();
			}
		}
	}

	private boolean login(String username, String password) {

		SQLiteDatabase db = openOrCreateDatabase("Members", MODE_PRIVATE, null);
		db = openOrCreateDatabase("Members", MODE_PRIVATE, null);

		String query = ("SELECT * FROM MEMBERS WHERE memName = '" + username
				+ "' AND memPass ='" + password + "'");
		Cursor cursor = db.rawQuery(query, null);
		if (cursor.getCount() <= 0) {
			return false;
		}
		return true;

	}

	public void populateTable() {
		try {

			SQLiteDatabase db = openOrCreateDatabase("Members", MODE_PRIVATE,
					null);
			db.execSQL("DROP TABLE IF EXISTS Members");
			db.execSQL("CREATE TABLE Members(memID INTEGER PRIMARY KEY AUTOINCREMENT, memName TEXT, memAge INTEGER, memPass VARCHAR)");

			db = openOrCreateDatabase("Members", MODE_PRIVATE, null);
			db.execSQL("INSERT INTO Members VALUES( 1,'Admin',20,'A3B922DF010PQSI827')");
			db.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Toast toast = Toast.makeText(CSInjection.this,
					"An error occurred.", Toast.LENGTH_LONG);
			toast.show();
		}
	}

	public void checkAttempts() {
		if (loginAttempts > 3) {
			// add a hint
			Toast toast = Toast.makeText(CSInjection.this,
					"A hint has appeared!", Toast.LENGTH_SHORT);
			toast.show();
			hintView.setVisibility(View.VISIBLE);
		}
	}
}
