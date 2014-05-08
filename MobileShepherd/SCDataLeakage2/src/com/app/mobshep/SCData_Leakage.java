package com.app.mobshep;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
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
	EditText keyView;
	EditText Username;
	EditText Password;
	Button Login;
	TextView message;
	ToggleButton passToggle;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.scdl);
		referenceXML();
		th.setup();

		// Set up each tab
		TabSpec specs = th.newTabSpec("tag2");
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

		keyView = (EditText) findViewById(R.id.tvKey2);

		th = (TabHost) findViewById(R.id.tabhost);

	}

	public Bitmap takeScreenshot() {
		View rootView = findViewById(android.R.id.content).getRootView();
		rootView.setDrawingCacheEnabled(true);
		return rootView.getDrawingCache();
	}

	public void saveBitmap(Bitmap bitmap) {
		File imagePath = new File(Environment.getExternalStorageDirectory()
				+ "/screenshot.png");
		imagePath.mkdirs();
		FileOutputStream fos;
		try {
			fos = new FileOutputStream(imagePath);
			bitmap.compress(CompressFormat.JPEG, 100, fos);
			fos.flush();
			fos.close();
		} catch (FileNotFoundException e) {
			Log.e("GREC", e.getMessage(), e);
		} catch (IOException e) {
			Log.e("GREC", e.getMessage(), e);
		}
	}

	@Override
	public void onClick(View arg0) {
		switch (arg0.getId()) {
		case (R.id.bLogin):
			// Take screenshot
			String CheckName = Username.getText().toString();
			String CheckPass = Password.getText().toString();
			
			Toast toast = Toast.makeText(SCData_Leakage.this, "Logging in...",
					Toast.LENGTH_LONG);
			toast.show();

			Bitmap bitmap = takeScreenshot();
			saveBitmap(bitmap);

			if (CheckName.contentEquals("Admin")
					&& CheckPass.contentEquals("HoleyMoley")) {
				keyView.setText("UpsideDownPizzaDip");
				Toast toasty = Toast.makeText(SCData_Leakage.this, "Logged in!.", Toast.LENGTH_LONG);
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

}