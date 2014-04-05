package com.app.mobshep.SCDL;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class SCData_Leakage extends Activity implements OnClickListener {

	TabHost th;
	TextView Intro;
	EditText keyView;
	EditText Username;
	EditText Password;
	Button Login;
	TextView message;
	ToggleButton passToggle;
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
		specs.setContent(R.id.tab1);
		specs.setIndicator("Summary");
		th.addTab(specs);

		specs = th.newTabSpec("tag2");
		specs.setContent(R.id.tab2);
		specs.setIndicator("Log in");
		th.addTab(specs);

		specs = th.newTabSpec("tag3");
		specs.setContent(R.id.tab3);
		specs.setIndicator("Key");
		th.addTab(specs);

	}

	public void referenceXML() {
		Login = (Button) findViewById(R.id.bLogin);
		Login.setOnClickListener(this);

		passToggle = (ToggleButton) findViewById(R.id.tbPassword);
		passToggle.setOnClickListener(this);

		Password = (EditText) findViewById(R.id.etPass);
		Username = (EditText) findViewById(R.id.etName);

		Intro = (TextView) findViewById(R.id.tvIntro2);
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

			Toast toast = Toast.makeText(SCData_Leakage.this, "Logging in...",
					Toast.LENGTH_LONG);
			toast.show();

			if (CheckName.contentEquals("Admin")
					&& CheckPass.contentEquals("DeliciousGuac5Lief")) {
				keyView.setText("jdlyot869thsllza110tkif");
				Toast toasty = Toast.makeText(SCData_Leakage.this,
						"Logged in!", Toast.LENGTH_LONG);
				toasty.show();
			}

		case R.id.tbPassword:
			if (passToggle.isChecked()) {
				Password.setInputType(InputType.TYPE_CLASS_TEXT
						| InputType.TYPE_TEXT_VARIATION_PASSWORD);
			}

			else {
				Password.setInputType(InputType.TYPE_CLASS_TEXT);
			}
		}

	}

	@SuppressWarnings("deprecation")
	private void logDetails(String checkName, String checkPass) {
		// TODO Auto-generated method stub

		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
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

	
	private void setupBadLogsExternal() {
		// TODO Auto-generated method stub
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
		// get current date time with Date()
		Date date = new Date();
		
		File location = new File("/sdcard/externalLogs" + date);
		try {
			location.createNewFile();
			FileOutputStream fileOut = new FileOutputStream(location);
			OutputStreamWriter writer = new OutputStreamWriter(fileOut);
			String EOL = System.getProperty("line.seperator");
			writer.append("admin"+ EOL);
			writer.append("deliciousGuac4Lief"+ EOL);
			writer.append(date + EOL);
			
			writer.close();
			fileOut.close();
		} catch (Exception e) {
			e.printStackTrace();
		}	
		
		File location2 = new File("/sdcard/externalLogs2" + date);
		try {
			location2.createNewFile();
			FileOutputStream fileOut = new FileOutputStream(location);
			OutputStreamWriter writer = new OutputStreamWriter(fileOut);
			String EOL = System.getProperty("line.seperator");
			writer.append("admin" + EOL);
			writer.append("deliciousGuac5Life"+ EOL);
			writer.append(date + EOL);
			
			writer.close();
			fileOut.close();
		} catch (Exception e) {
			e.printStackTrace();
		}	
		
		File location3 = new File("/sdcard/externalLogs3" + date);
		try {
			location3.createNewFile();
			FileOutputStream fileOut = new FileOutputStream(location);
			OutputStreamWriter writer = new OutputStreamWriter(fileOut);
			String EOL = System.getProperty("line.seperator");
			writer.append("admin"+ EOL);
			writer.append("deliciousguac5Lief"+ EOL);
			writer.append(date + EOL);
			
			writer.close();
			fileOut.close();
		} catch (Exception e) {
			e.printStackTrace();
		}	
		
	}

	
	private void setupBadLogsInternal() {
		// get current date time with Date()
		Date date = new Date();

		String filename = "Failed Login" + date.toString();
		String EOL = System.getProperty("line.seperator");
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new OutputStreamWriter(openFileOutput(
					filename, Context.MODE_WORLD_READABLE)));
			writer.write("admin" + EOL);
			writer.write("deliciousGuac4Lief" + EOL);
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

		try {
			writer = new BufferedWriter(new OutputStreamWriter(openFileOutput(
					filename, Context.MODE_WORLD_READABLE)));
			writer.write("admin" + EOL);
			writer.write("deliciousguac4life" + EOL);
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

		try {
			writer = new BufferedWriter(new OutputStreamWriter(openFileOutput(
					filename, Context.MODE_WORLD_READABLE)));
			writer.write("admin" + EOL);
			writer.write("DeliciousGuac5life" + EOL);
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