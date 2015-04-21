package com.mobshep.PoorAuthentication;

import java.util.Random;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class Forgotton extends Activity {

	EditText question1, question2;
	TextView tempPass;
	static String tempPassVar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub

		super.onCreate(savedInstanceState);
		setContentView(R.layout.forgot);
		question1 = (EditText) findViewById(R.id.etQuestion1);
		question2 = (EditText) findViewById(R.id.etQuestion2);
		tempPass = (TextView) findViewById(R.id.tempPass);

	}

	public void cancel(View v) {
        Intent gotoMain = new Intent(Forgotton.this, PoorAuthentication.class);
		startActivity(gotoMain);
	}

	public void reset(View v) {
		String CheckQ1 = question1.getText().toString();
		String CheckQ2 = question2.getText().toString();

		if (CheckQ1.contentEquals("") || CheckQ2.contentEquals("")) {
			Toast empty = Toast.makeText(Forgotton.this,
					"Empty Fields Detected.", Toast.LENGTH_SHORT);
			empty.show();
		}

		if (CheckQ1.equalsIgnoreCase("Chicken")
				&& CheckQ2.equalsIgnoreCase("Meade")) {
			Toast reset = Toast.makeText(Forgotton.this, "Password Reset.",
					Toast.LENGTH_SHORT);
			reset.show();

			PoorAuthentication.passwordReset = true;

			tempPassVar = getRandomNumber(6);

			PoorAuthentication.tempPass = tempPassVar.toString();

			tempPass.setText("The temp password is:" + tempPassVar.toString());

		}

		else {
			Toast invalid = Toast.makeText(Forgotton.this, "Invalid answers.",
					Toast.LENGTH_SHORT);
			invalid.show();
		}

	}

	private static Random random = new Random();

	public static String getRandomNumber(int digCount) {
		StringBuilder sb = new StringBuilder(digCount);
		for (int i = 0; i < digCount; i++)
			sb.append((char) ('0' + random.nextInt(10)));
		return sb.toString();
	}

}
