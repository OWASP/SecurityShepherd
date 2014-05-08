package com.app.mobshep.IDS;

import android.app.Activity;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TabHost;
import android.widget.Toast;
import android.widget.TabHost.TabSpec;

public class Insecure_Data_Storage extends Activity {

	TabHost th;
	EditText keyView;
	Button insert;
	Button select;
	Button login;
	EditText password;
	EditText username;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ids);
		referenceXML();
		th.setup();
        populateTable();
        
  
        
		// Set up each tab
		TabSpec specs = th.newTabSpec("tag1");
		specs.setContent(R.id.tab1);
		specs.setIndicator("Login");
		th.addTab(specs);

		specs = th.newTabSpec("tag2");
		specs.setContent(R.id.tab2);
		specs.setIndicator("Key");
		th.addTab(specs);
	

		login.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// what to do if you login
				Toast toast = Toast.makeText(Insecure_Data_Storage.this,
						"Logging in...", Toast.LENGTH_SHORT);
				toast.show();

				String CheckName = username.getText().toString();
				String CheckPass = password.getText().toString();

				if (CheckName.contentEquals("") || CheckPass.contentEquals("")) {
					Toast toast2 = Toast.makeText(Insecure_Data_Storage.this,
							"Blank fields detected!", Toast.LENGTH_SHORT);
					toast2.show();
				}
				if (CheckName.contentEquals("Admin")
						&& CheckPass.contentEquals("OrcsExplosion")) {

					Toast toast3 = Toast.makeText(Insecure_Data_Storage.this,
							"Logged in!", Toast.LENGTH_SHORT);
					toast3.show();

					keyView.setText("" + "" + ""
							+ "The Key is Battery777");

				}

				if (CheckName.contentEquals("EpicTrees") || CheckName.contentEquals("GraveyBones") || CheckName.contentEquals("FallenComrade") || CheckName.contentEquals("IronFist") || CheckName.contentEquals("Jumper") || CheckName.contentEquals("99chips") || CheckName.contentEquals("RegularVeg"))
						{

					Toast locked = Toast.makeText(Insecure_Data_Storage.this,
							"That Account is locked.", Toast.LENGTH_SHORT);
					locked.show();

				}
				
				else {
					Toast toast4 = Toast.makeText(Insecure_Data_Storage.this,
							"Invalid Credentials!", Toast.LENGTH_SHORT);
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
			db.execSQL("INSERT INTO Members VALUES( 2,'Admin',45,'OrcsExplosion')");
			db.execSQL("INSERT INTO Members VALUES( 3,'GraveyBones',23,'super')");
			db.execSQL("INSERT INTO Members VALUES( 4,'EpicTrees',35,'root')");
			db.execSQL("INSERT INTO Members VALUES( 5,'FallenComrade',36,'password')");
			db.execSQL("INSERT INTO Members VALUES( 6,'Ironfist',82,'hd7832dh8d')");
			db.execSQL("INSERT INTO Members VALUES( 7,'Jumper',18,'bananarama')");
			db.execSQL("INSERT INTO Members VALUES( 8,'99chips',32,'catsrcool')");
			db.execSQL("INSERT INTO Members VALUES( 9,'regularVeg',40,'111111111111')");
			db.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Toast toast = Toast.makeText(Insecure_Data_Storage.this,
					"Data already inserted", Toast.LENGTH_LONG);
			toast.show();
		}
	}


	public void referenceXML() {
		keyView = (EditText) findViewById(R.id.tvKey2);
		th = (TabHost) findViewById(R.id.tabhost);
		login = (Button) findViewById(R.id.bLogin);
		username = (EditText) findViewById(R.id.etName);
		password = (EditText) findViewById(R.id.etPass);

	}

}
