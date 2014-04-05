package com.app.mobshep.BrokenCrypto;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;
import android.widget.Toast;

public class Broken_Cryptography extends Activity implements OnClickListener {

	TabHost th;
	TextView Intro;
	Button Login;
	TextView Packet;
	EditText username;
	EditText password;
	EditText key;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.broken);
		th = (TabHost) findViewById(R.id.tabhost);
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
		specs.setIndicator("Packet");
		th.addTab(specs);
		
		specs = th.newTabSpec("tag4");
		specs.setContent(R.id.tab4);
		specs.setIndicator("Key");
		th.addTab(specs);
	}

	private void referenceXML() {
		// TODO Auto-generated method stub
		Packet = (TextView) findViewById(R.id.etPacket);
		Login = (Button) findViewById(R.id.bLogin);
		username = (EditText)findViewById(R.id.etName);
		password = (EditText)findViewById(R.id.etPass);
		key = (EditText)findViewById(R.id.etKey);
		Login.setOnClickListener(this);
		

	}

	public void onClick(View arg0) {
		switch (arg0.getId()) {
		case (R.id.bLogin):

			String CheckName = username.getText().toString();
			String CheckPass = password.getText().toString();

			if (CheckName.contentEquals("James")
					&& CheckPass.contentEquals("RunningManTwo")) {
				key.setText("8f57addfc73e09b16d620a1967d74d19");
				Toast toast = Toast.makeText(Broken_Cryptography.this, "Logged in!.", Toast.LENGTH_LONG);
				toast.show();	
			}
			
			if (CheckName.contentEquals("")
					|| CheckPass.contentEquals("")) {
				Toast toast2 = Toast.makeText(Broken_Cryptography.this, "Empty Fields Detected.", Toast.LENGTH_LONG);
				toast2.show();
			}

			else{

				Toast toast = Toast.makeText(Broken_Cryptography.this,
						"Invalid Credentials!", Toast.LENGTH_LONG);
				toast.show();
			}
		}
	}
}
