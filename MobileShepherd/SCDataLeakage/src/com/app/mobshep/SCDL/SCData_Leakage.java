package com.app.mobshep.SCDL;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.R.color;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;
import android.widget.Toast;

public class SCData_Leakage extends Activity implements OnClickListener {

	TabHost th;
	EditText keyView;
	EditText Username;
	EditText Password;
	Button Login;
	TextView message;
	DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.scdl);
		referenceXML();
		th.setup();
		

		// Set up each tab
		TabSpec specs = th.newTabSpec("tag1");
		specs.setContent(R.id.tab2);
		specs.setIndicator("Log in");
		th.addTab(specs);

		specs = th.newTabSpec("tag2");
		specs.setContent(R.id.tab3);
		specs.setIndicator("Key");
		th.addTab(specs);

	}

	public void referenceXML() {
		Login = (Button) findViewById(R.id.bLogin);
		Login.setOnClickListener(this);
		//Login.setBackgroundColor(color.background_light);
		
		Password = (EditText) findViewById(R.id.etPass);
		Username = (EditText) findViewById(R.id.etName);

		keyView = (EditText) findViewById(R.id.tvKey2);

		th = (TabHost) findViewById(R.id.tabhost);

		setupBadLogsExternal();
		setupBadLogsInternal();

	}


	@Override
	public void onClick(View arg0) {
		switch (arg0.getId()) {
		case (R.id.bLogin):
			// Take screenshot
			String CheckName = Username.getText().toString();
			String CheckPass = Password.getText().toString();

			logDetails(CheckName, CheckPass);
			logButton("Login");

			Toast toast = Toast.makeText(SCData_Leakage.this, "Logging in...",
					Toast.LENGTH_LONG);
			toast.show();

			if (CheckName.contentEquals("Admin")
					&& CheckPass.contentEquals("DeliciousGuac5Lief")) {
				keyView.setText("SilentButSteadyRedLed");
				Toast toasty = Toast.makeText(SCData_Leakage.this,
						"Logged in!", Toast.LENGTH_LONG);
				toasty.show();
			}
			
			else if (CheckName.contentEquals("") || CheckPass.contentEquals(""))
			{
				Toast blanks = Toast.makeText(SCData_Leakage.this, "Blank Fields Detected.", Toast.LENGTH_SHORT);
				blanks.show();
			}
			
			else {
				Toast invalid = Toast.makeText(SCData_Leakage.this, "Invalid Credentials!", Toast.LENGTH_SHORT);
				invalid.show();
			}
		}


	}

	@SuppressWarnings("deprecation")
	private void logDetails(String checkName, String checkPass) {
		// TODO Auto-generated method stub

		//DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
		// get current date time with Date()
		Date date = new Date();

		String filename = "Failed Login" + date.toString();
		String EOL = System.getProperty("line.seperator");
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new OutputStreamWriter(openFileOutput(
					filename, Context.MODE_WORLD_READABLE)));
			writer.write(checkName + EOL);
			writer.write(checkPass + EOL);
			writer.write(date + EOL);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

	}
	
	@SuppressWarnings("deprecation")
	private void logButton(String buttonName) {
		// TODO Auto-generated method stub

		Date date = new Date();

		String filename = "Failed Login" + date.toString();
		String EOL = System.getProperty("line.seperator");
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new OutputStreamWriter(openFileOutput(
					filename, Context.MODE_WORLD_READABLE)));
			writer.write(buttonName + "button clicked at:" + EOL);
			writer.write(date + EOL);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

	}

	
	private void setupBadLogsExternal() {
		// TODO Auto-generated method stub
		Date date = new Date();
		
		File location = new File("/sdcard/externalLogs" + date);
		try {
			location.createNewFile();
			FileOutputStream fileOut = new FileOutputStream(location);
			OutputStreamWriter writer = new OutputStreamWriter(fileOut);
			String EOL = System.getProperty("line.seperator");
			writer.append("Admin"+ EOL);
			writer.append("DeliciousGuac5Lief"+ EOL);
			writer.append(date + EOL);
			
			writer.close();
			fileOut.close();
		} catch (Exception e) {
			e.printStackTrace();
		}	
		
		
	}

	
	@SuppressWarnings("deprecation")
	private void setupBadLogsInternal() {
		// get current date time with Date()
		Date date = new Date();

		String filename = "Failed Login" + date.toString();
		String EOL = System.getProperty("line.seperator");
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new OutputStreamWriter(openFileOutput(
					filename, Context.MODE_WORLD_READABLE)));
			writer.write("Admin" + EOL);
			writer.write("DeliciousGuac5Lief" + EOL);
			writer.write(date + EOL);

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException e) {
					e.printStackTrace();
				}

			}
		}

	}

}