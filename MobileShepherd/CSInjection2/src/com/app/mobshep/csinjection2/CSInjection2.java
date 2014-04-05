package com.app.mobshep.csinjection2;

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

public class CSInjection2 extends Activity implements OnClickListener {

	TabHost th;
	TextView Intro;
	Button Query;
	EditText etQuery;
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
		specs.setIndicator("Search");
		th.addTab(specs);
	}

	private void referenceXML() {
		// TODO Auto-generated method stub
		Query = (Button) findViewById(R.id.bQuery);
		etQuery = (EditText) findViewById(R.id.etQuery);
		Query.setOnClickListener(this);

	}

	public void onClick(View arg0) {
		switch (arg0.getId()) {

		case (R.id.bQuery):

			String getQuery = etQuery.getText().toString();

		if (getQuery == "Admin") {
			Toast denied = Toast.makeText(CSInjection2.this,
					"Cannot lookup that username.", Toast.LENGTH_LONG);
			denied.show();

			} else {
				doQuery(getQuery);
			}

		}
	}

	private void doQuery(String getQuery) {

		SQLiteDatabase db = openOrCreateDatabase("Members", MODE_PRIVATE, null);
		db = openOrCreateDatabase("Members", MODE_PRIVATE, null);

		String query = ("SELECT memName, memAge, DOB, Location, Team FROM MEMBERS WHERE memName = '"
				+ getQuery + "'");
		try {
			Cursor cursor = db.rawQuery(query, null);

			if (cursor != null && cursor.getCount() > 0)
				{cursor.moveToFirst();
			
			String output = " Name: " + cursor.getString(0) + " Age: "
					+ cursor.getString(1) + " DOB " + cursor.getString(2)
					+ " Location: " + cursor.getString(3) + " Team: "
					+ cursor.getString(4);
			
			etQuery.setText(output);

		}
			 else {
	                Toast.makeText(getApplicationContext(), "No Record Found", 1000).show();
	            }


		} catch (SQLiteException e) {
			Toast nouser = Toast.makeText(CSInjection2.this, "User not found.",Toast.LENGTH_SHORT);
			nouser.show();
		}

	

		//etQuery.setText(output);
		// return contact

	}

	public void populateTable() {
		try {

			SQLiteDatabase db = openOrCreateDatabase("Members", MODE_PRIVATE,
					null);
			db.execSQL("DROP TABLE IF EXISTS Members");
			db.execSQL("CREATE TABLE Members(memID INTEGER PRIMARY KEY AUTOINCREMENT, memName TEXT, memAge INTEGER, memPass VARCHAR, DOB TEXT, Location TEXT , Team TEXT)");

			db = openOrCreateDatabase("Members", MODE_PRIVATE, null);
			db.execSQL("INSERT INTO Members VALUES( 1,'Admin',20,'PurpleDonkeysSunday', '12/04/1994', 'Cornwall', 'Blue')");
			db.execSQL("INSERT INTO MEMBERS VALUES (2, 'User1', 0, 'hellodnmuiw', '12/12/1980', 'London', 'Red')");
			db.execSQL("INSERT INTO MEMBERS VALUES (3, 'sean', 24, 'qawsedrftgyh', '04/04/1980', 'Donegal', 'Red')");
			db.execSQL("INSERT INTO MEMBERS VALUES (4, 'kevin', 25, 'chelsea', 'NULL', 'Dublin', 'Blue')");
			db.execSQL("INSERT INTO MEMBERS VALUES (5, 'conor', 24, 'Password1234', 'NULL', 'Dublin', 'Red')");
			db.execSQL("INSERT INTO MEMBERS VALUES (6, 'peter', 29, 'Pa88w0rd', 'NULL', 'Instanbul', 'Blue')");
			db.execSQL("INSERT INTO MEMBERS VALUES (7, 'Napier', 18, 'password', 'NULL', 'New York', 'Red')");

			db.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Toast toast = Toast.makeText(CSInjection2.this,
					"An error occurred.", Toast.LENGTH_LONG);
			toast.show();
		}
	}

	public void checkAttempts() {
		if (loginAttempts > 3) {
			// add a hint
			Toast toast = Toast.makeText(CSInjection2.this,
					"A hint has appeared!", Toast.LENGTH_SHORT);
			toast.show();
			hintView.setVisibility(View.VISIBLE);
		}
	}
}
