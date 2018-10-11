package com.mobshep.reverseengineer3;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

/**
 * This file is part of the Security Shepherd Project.
 * 
 * The Security Shepherd project is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.<br/>
 * 
 * The Security Shepherd project is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.<br/>
 * 
 * You should have received a copy of the GNU General Public License
 * along with the Security Shepherd project.  If not, see <http://www.gnu.org/licenses/>. 
 * 
 * @author Sean Duggan
 */

public class Reverse_Engineering4 extends Activity {
	
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
		
			Toast toast = Toast.makeText(Reverse_Engineering4.this, "Valid Key",
					Toast.LENGTH_LONG);
			toast.show();
	
		}
		else 
		{
			Toast toast = Toast.makeText(Reverse_Engineering4.this, "Invalid Key",
					Toast.LENGTH_LONG);
			toast.show();
			
			Toast remove = Toast.makeText(Reverse_Engineering4.this, theKey,
					Toast.LENGTH_LONG);
			remove.show();
		}
	}
	


}
