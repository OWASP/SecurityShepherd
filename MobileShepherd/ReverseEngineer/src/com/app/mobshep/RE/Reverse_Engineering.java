package com.app.mobshep.RE;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TabHost;
import android.widget.Toast;
import android.widget.TabHost.TabSpec;


public class Reverse_Engineering extends Activity implements OnClickListener {

	TabHost th;
	EditText display;
	Button bLogin;
	EditText Password;
	EditText Username;
	
	String UsernameValue = "root";
	String PasswordValue = "NintendoMonster";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.reverse);
		th = (TabHost) findViewById(R.id.tabhost);

		bLogin = (Button) findViewById(R.id.bLogin);
		Username = (EditText) findViewById(R.id.etUsername);
		Password = (EditText) findViewById(R.id.etPassword);
		display = (EditText) findViewById(R.id.tvKey1);

		bLogin.setOnClickListener(this);

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
		specs.setIndicator("Key");
		th.addTab(specs);
	}

	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.bLogin:
			String CheckName = Username.getText().toString();
			String CheckPass = Password.getText().toString();
			if (CheckPass.contentEquals(PasswordValue) && CheckName.contentEquals(UsernameValue)) {
				display.setText("Keyish42BurningCarpets7755438273");
				Toast success = Toast.makeText(Reverse_Engineering.this, "Logged in! Key Revealed.",
						Toast.LENGTH_LONG);
				success.show();
			} else {
				Toast fail = Toast.makeText(Reverse_Engineering.this,
						"Invalid Credentials", Toast.LENGTH_SHORT);
				fail.show();
			}

		}

	}

}
