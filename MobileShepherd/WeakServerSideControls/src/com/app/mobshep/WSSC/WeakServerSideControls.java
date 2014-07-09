package com.app.mobshep.WSSC;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

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

public class WeakServerSideControls extends Activity implements OnClickListener {

	TabHost th;
	Button Login;
	Button Config;
	EditText username;
	EditText password;
	EditText key;
	String shepherdAddress = null ;
	String shepherdDBAddress = null;

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
		specs.setContent(R.id.tab2);
		specs.setIndicator("Tab 2");
		th.addTab(specs);

		specs = th.newTabSpec("tag2");
		specs.setContent(R.id.tab3);
		specs.setIndicator("Tab 3");
		th.addTab(specs);
	}

	private void referenceXML() {
		// TODO Auto-generated method stub
		Login = (Button) findViewById(R.id.bLogin);
		Config=(Button)findViewById(R.id.bConfig);
		username = (EditText)findViewById(R.id.etName);
		password = (EditText)findViewById(R.id.etPass);
		key = (EditText)findViewById(R.id.etKey);
		Login.setOnClickListener(this);
		Config.setOnClickListener(this);
		

	}

	public void onClick(View arg0) {
		switch (arg0.getId()) {
		
		case (R.id.bConfig):
			
			//Intent preferenceIntent = new Intent(getActivity(), Preferences.class);
			//startActivity(preferenceIntent);
			
			Intent gotoMain = new Intent(this, Preferences.class);
			startActivity(gotoMain);
			
			break;
		
		case (R.id.bLogin):

			String CheckName = username.getText().toString();
			String CheckPass = password.getText().toString();

			if (CheckName.contentEquals("user")
					&& CheckPass.contentEquals("pass")) {
				key.setText("Key is revealed.");
				Toast toast = Toast.makeText(WeakServerSideControls.this, "Logged in!", Toast.LENGTH_LONG);
				toast.show();	
			}
			
			if (CheckName.contentEquals("")
					|| CheckPass.contentEquals("")) {
				Toast toast2 = Toast.makeText(WeakServerSideControls.this, "Empty Fields Detected.", Toast.LENGTH_LONG);
				toast2.show();
			}

			else{

				Toast toast = Toast.makeText(WeakServerSideControls.this,
						"Invalid Credentials!", Toast.LENGTH_LONG);
				toast.show();
			}
		}
	}
	
	public void makeGetRequest(String url)
	{
		JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.GET, url, null,
			    new Response.Listener<JSONObject>() 
			    {
			        @Override
			        public void onResponse(JSONObject response) {   
			                        // display response     
			            Log.d("Response", response.toString());
			        }
			    }, 
			    new Response.ErrorListener() 
			    {
			         @Override
			         public void onErrorResponse(VolleyError error) {            
			            Log.d("Error.Response", response);
			       }
			    }
			);
			 
			// add it to the RequestQueue   
			queue.add(getRequest);
	}
	
}
