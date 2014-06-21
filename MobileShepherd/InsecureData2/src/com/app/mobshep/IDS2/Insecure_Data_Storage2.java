package com.app.mobshep.IDS2;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.DatabaseUtils;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TabHost;
import android.widget.Toast;
import android.widget.TabHost.TabSpec;

public class Insecure_Data_Storage2 extends Activity {

	Button insert;
	Button select;
	Button login;
	EditText password;
	EditText username;

private static final int MY_NOTIFICATION_ID = 1;
	
	private int mNotificationCount;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ids);
		referenceXML();
  
			
			
			String destinationDir = "/data/data/" +getPackageName() + "/databases/";			
			String destinationPath = destinationDir + "Members";
			
			File f = new File(destinationPath);
			
			if (!f.exists()){
				File directory = new File(destinationDir);
				directory.mkdirs();
				//assets members.db -> /databases/
				
				try{
					copyDatabase(getBaseContext().getAssets().open("Members"), new FileOutputStream(destinationPath));
				}catch(FileNotFoundException e){
					e.printStackTrace();
				}catch(IOException e){
					e.printStackTrace();
				}
			}
	
		

		login.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// what to do if you login
				Toast toast = Toast.makeText(Insecure_Data_Storage2.this,
						"Logging in...", Toast.LENGTH_SHORT);
				toast.show();

				String CheckName = username.getText().toString();
				String CheckPass = password.getText().toString();

				DatabaseUtils.sqlEscapeString(CheckName);
				DatabaseUtils.sqlEscapeString(CheckPass);
				
				
				
				if (CheckName.contentEquals("") || CheckPass.contentEquals("")) {
					Toast toast2 = Toast.makeText(Insecure_Data_Storage2.this,
							"Blank fields detected!", Toast.LENGTH_SHORT);
					toast2.show();

					
				}

			if (CheckName.contentEquals("Root")
					|| CheckName.contentEquals("user1")
					|| CheckName.contentEquals("bottles")
					|| CheckName.contentEquals("Patrick")
					|| CheckName.contentEquals("Troolman")
					|| CheckName.contentEquals("Toothbrush")
					|| CheckName.contentEquals("Tyrkyr")) {

				Toast locked = Toast.makeText(Insecure_Data_Storage2.this,
						"That Account is locked.", Toast.LENGTH_SHORT);
				locked.show();			

			}
			
				else {
					Toast toast4 = Toast.makeText(Insecure_Data_Storage2.this,
							"Invalid Credentials!", Toast.LENGTH_SHORT);
					toast4.show();

				}
			}
		});
	}

	

	public void copyDatabase(InputStream iStream, OutputStream oStream)
			throws IOException {
		byte[] buffer = new byte[1024];
		int i;
		while ((i = iStream.read(buffer)) > 0) {
				oStream.write(buffer, 0 , i);
		}
		iStream.close();
		oStream.close();
		
	}

	public void referenceXML() {
		login = (Button) findViewById(R.id.bLogin);
		username = (EditText) findViewById(R.id.etName);
		password = (EditText) findViewById(R.id.etPass);

	}

}
