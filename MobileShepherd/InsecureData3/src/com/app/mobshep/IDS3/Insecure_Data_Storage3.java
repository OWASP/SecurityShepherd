package com.app.mobshep.IDS3;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.app.Activity;
import android.database.DatabaseUtils;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TabHost;
import android.widget.Toast;
import android.widget.TabHost.TabSpec;

public class Insecure_Data_Storage3 extends Activity {

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

		// Set up each tab
		TabSpec specs = th.newTabSpec("Tab 1");

		specs.setContent(R.id.tab1);
		specs.setIndicator("Login");
		th.addTab(specs);
		
		specs = th.newTabSpec("Tab 2");
		specs.setContent(R.id.tab2);
		specs.setIndicator("Key");
		th.addTab(specs);
		
		
		String destinationDir = "/data/data/" +getPackageName() + "/databases";
		
		String destinationPath = destinationDir + "PasswordDB";
		
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
				Toast toast = Toast.makeText(Insecure_Data_Storage3.this,
						"Logging in...", Toast.LENGTH_SHORT);
				toast.show();

				String CheckName = username.getText().toString();
				String CheckPass = password.getText().toString();

				DatabaseUtils.sqlEscapeString(CheckName);
				DatabaseUtils.sqlEscapeString(CheckPass);
				
				if (CheckName.contentEquals("") || CheckPass.contentEquals("")) {
					Toast toast2 = Toast.makeText(Insecure_Data_Storage3.this,
							"Blank fields detected!", Toast.LENGTH_SHORT);
					toast2.show();
				}
				
				else {
					Toast toast4 = Toast.makeText(Insecure_Data_Storage3.this,
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
		keyView = (EditText) findViewById(R.id.tvKey2);
		th = (TabHost) findViewById(R.id.tabhost);
		login = (Button) findViewById(R.id.bLogin);
		username = (EditText) findViewById(R.id.etName);
		password = (EditText) findViewById(R.id.etPass);

	}

}
