package com.app.mobshep.IDS2;

import android.app.Activity;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TabHost;
import android.widget.Toast;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;

public class Insecure_Data_Storage2 extends Activity {

	int loginAttempts = 0;
	TabHost th;
	TextView Intro;
	EditText keyView;
	Button insert;
	Button select;
	Button login;
	EditText password;
	EditText username;
	TextView message;
	TextView hintView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ids);
		referenceXML();
		th.setup();

		// Set up each tab
		TabSpec specs = th.newTabSpec("Tab 1");
		specs.setContent(R.id.tab1);
		specs.setIndicator("Summary"); // give the tab a name
		th.addTab(specs);

		specs = th.newTabSpec("Tab 2");
		specs.setContent(R.id.tab3);
		specs.setIndicator("Key");
		th.addTab(specs);

		specs = th.newTabSpec("Tab 3");
		specs.setContent(R.id.tab4);
		specs.setIndicator("Login");
		th.addTab(specs);
		
		populateTable();

		login.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// what to do if you login
				Toast toast = Toast.makeText(Insecure_Data_Storage2.this,
						"Logging in...", Toast.LENGTH_SHORT);
				toast.show();

				String CheckName = username.getText().toString();
				String CheckPass = password.getText().toString();

				if (CheckName.contentEquals("") || CheckPass.contentEquals("")) {
					Toast toast2 = Toast.makeText(Insecure_Data_Storage2.this,
							"Blank fields detected!", Toast.LENGTH_SHORT);
					loginAttempts = loginAttempts + 1;
					checkAttempts();
					toast2.show();
				}
				if (CheckName.contentEquals("Chris")
						&& CheckPass.contentEquals("monkey")) {

					Toast toast3 = Toast.makeText(Insecure_Data_Storage2.this,
							"Logged in!", Toast.LENGTH_SHORT);
					toast3.show();

					keyView.setText("" + "" + ""
							+ "18e7ab691d0cd167dc6c680fe6d8010bc0df19da");

				}

				else {
					Toast toast4 = Toast.makeText(Insecure_Data_Storage2.this,
							"Invalid Credentials!", Toast.LENGTH_SHORT);
					loginAttempts = loginAttempts + 1;
					checkAttempts();
					toast4.show();

				}
			}
		});
	}

	public void populateTable() {
		try {

			SQLiteDatabase db = openOrCreateDatabase("Members", MODE_PRIVATE,
					null);
			db.execSQL("DROP TABLE IF EXISTS Members");
			db.execSQL("CREATE TABLE Members(memID INTEGER PRIMARY KEY AUTOINCREMENT, memName TEXT, memAge INTEGER, memPass VARCHAR)");

			db = openOrCreateDatabase("Members", MODE_PRIVATE, null);
			db.execSQL("INSERT INTO Members VALUES( 5,'Chris',23,'bW9ua2V5')");
			db.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Toast toast = Toast.makeText(Insecure_Data_Storage2.this,
					"Data already inserted", Toast.LENGTH_LONG);
			toast.show();
		}
	}

	public void checkAttempts() {
		if (loginAttempts > 4) {
			// add a hint
			Toast toast = Toast.makeText(Insecure_Data_Storage2.this,
					"A hint has appeared!", Toast.LENGTH_SHORT);
			toast.show();
			hintView.setVisibility(View.VISIBLE);
		}
	}

	public void referenceXML() {
		Intro = (TextView) findViewById(R.id.tvIntro2);
		keyView = (EditText) findViewById(R.id.tvKey2);
		th = (TabHost) findViewById(R.id.tabhost);
		login = (Button) findViewById(R.id.bLogin);
		username = (EditText) findViewById(R.id.etName);
		password = (EditText) findViewById(R.id.etPass);
		hintView = (TextView) findViewById(R.id.hintView);

	}

}
