package com.mobshep.mobileshepherd;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Random;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * This file is part of the Security Shepherd Project.
 *
 * The Security Shepherd project is free software: you can redistribute it
 * and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.<br/>
 *
 * The Security Shepherd project is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.<br/>
 *
 * You should have received a copy of the GNU General Public License along with
 * the Security Shepherd project. If not, see <http://www.gnu.org/licenses/>.
 *
 * @author Sean Duggan
 */

public class poorAuth extends Activity implements OnClickListener {

	Button bLogin;
	Button bForgot;
	EditText username;
	String usernameVar = "Jack";
	EditText password;
	static String tempPass;
	static Boolean passwordReset = false;

	private static final String TAG = "MyActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub

		super.onCreate(savedInstanceState);
		setContentView(R.layout.pa_login_content);
		referenceXML();

		logDetails("chicken");
		logDetails("meade");
		//store secret questions locally.
	}

	private void referenceXML() {
		// TODO Auto-generated method stub
		bLogin = (Button) findViewById(R.id.bLogin);
		bForgot = (Button) findViewById(R.id.bForgot);
		username = (EditText) findViewById(R.id.etName);
		password = (EditText) findViewById(R.id.etPass);
		bForgot.setOnClickListener(this);
		bLogin.setOnClickListener(this);

	}

	public void onClick(View arg0) {
		switch (arg0.getId()) {

		case (R.id.bForgot):
			Intent gotoForgot = new Intent("com.mobshep.mobileshepherd.poorAuth_Reset");
			startActivity(gotoForgot);
			break;

		case (R.id.bLogin):

			String CheckName = username.getText().toString();
			String CheckPass = password.getText().toString();

			Log.d(TAG, "temp pass = " + poorAuth_Reset.tempPassVar
					+ "passwordReset = " + passwordReset);

			if (passwordReset == true)

				if (CheckName.equals(usernameVar)) {

					if (CheckPass.equals(tempPass)) {
						Toast loggedIn = Toast.makeText(poorAuth.this,
								"Logged in!", Toast.LENGTH_LONG);
						loggedIn.show();

						Intent loggedInIntent = new Intent(
								"com.mobshep.mobileshepherd.PoorAuth_Main");
						startActivity(loggedInIntent);

					}

				} else {
					Toast locked = Toast.makeText(poorAuth.this,
							"Invalid Password!", Toast.LENGTH_SHORT);
					locked.show();

					Log.d(TAG, "The password is " + tempPass);
				}

			else {
				Toast locked = Toast.makeText(poorAuth.this,
						"You're account has been locked!", Toast.LENGTH_SHORT);
				locked.show();
				break;
			}

			if (CheckName.contentEquals("") || CheckPass.contentEquals("")) {
				Toast empty = Toast.makeText(poorAuth.this,
						"Empty Fields Detected.", Toast.LENGTH_SHORT);
				empty.show();
			}

			if (CheckName.equals("Jack") == false
					|| CheckPass.equals(poorAuth_Reset.tempPassVar) == false
					|| passwordReset == false) {
				Toast invalid = Toast.makeText(poorAuth.this,
						"Invalid Credentials!", Toast.LENGTH_SHORT);
				invalid.show();

				Log.d(TAG, CheckName);
				Log.d(TAG, CheckPass);

				Log.d(TAG, "temp pass = " + poorAuth_Reset.tempPassVar
						+ "passwordReset = " + passwordReset);
				break;
			}
			break;
		}
	}

	private void logDetails(String content) {
		// TODO Auto-generated method stub


		Random rand = new Random(5);

		String filename = "Log" + rand;
		String EOL = System.getProperty("line.seperator");
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new OutputStreamWriter(openFileOutput(
					filename, Context.MODE_WORLD_READABLE)));
			writer.write(content + EOL);
			writer.write(EOL);
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
