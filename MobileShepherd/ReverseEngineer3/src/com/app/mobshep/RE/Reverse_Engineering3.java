package com.app.mobshep.RE;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class Reverse_Engineering3 extends Activity {
	
	String LevelKey;
	EditText etKeyCheck;
//test
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.reverse);
	}	
	
	
	public void checkKey(View v){
		
		etKeyCheck = (EditText) findViewById(R.id.etSecret);
		
		StringBuilder theKey = new StringBuilder("Z"); 
		theKey.append("Y");
		
		String hash = "4399e0f52227dbab4d74bbd3dd2e3c4c";
		int len = hash.length();
		String slice1 = hash.substring(0, len/2);
		theKey.append(slice1);
		
		theKey.append("1");
		
		theKey.append("C");

		theKey.reverse();
		
		theKey.append("8");
		
		String checkKey = etKeyCheck.getText().toString();

		if (checkKey.contentEquals(theKey))
		{
		
			Toast toast = Toast.makeText(Reverse_Engineering3.this, "Valid Key",
					Toast.LENGTH_LONG);
			toast.show();
	
		}
		else 
		{
			Toast toast = Toast.makeText(Reverse_Engineering3.this, "Invalid Key",
					Toast.LENGTH_LONG);
			toast.show();
			
			Toast remove = Toast.makeText(Reverse_Engineering3.this, theKey,
					Toast.LENGTH_LONG);
			remove.show();
		}
	}
	


}
