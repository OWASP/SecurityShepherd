package com.mobshep.ITLS3;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.Settings;
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

@SuppressLint("NewApi")
public class InsufficientTLS3 extends Activity implements OnClickListener {

	Button save;
	Button send;
	EditText IP;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.configure);
		referenceXML();
		
		//these two lines are temporary and should NOT make it to the final app. Create an Async task for any future network activities
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy); 
		
		if (isNetworkAvailable() == false)
		{
			//There is no network....
		}

	}

	private void referenceXML() {
		// TODO Auto-generated method stub
		IP = (EditText) findViewById(R.id.etIP);
		send = (Button) findViewById(R.id.bSecret);
		save = (Button) findViewById(R.id.bSave);
		send.setOnClickListener(this);
		save.setOnClickListener(this);
	}

	public void onClick(View arg0) {
		switch (arg0.getId()) {

		case (R.id.bSave):

			break;

		case (R.id.bSecret):
			
			Toast sendingMessage = Toast.makeText(InsufficientTLS3.this,
					"Sent Message!", Toast.LENGTH_SHORT);
			sendingMessage.show();
			break;

		}
	}
	
	
	
	public boolean isNetworkAvailable() {
	    ConnectivityManager cm = (ConnectivityManager) 
	      getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo networkInfo = cm.getActiveNetworkInfo();
	    
	    // if no network is available networkInfo will be null
	    // otherwise check if we are connected
	    if (networkInfo != null && networkInfo.isConnected()) {
	        return true;
	    }
	    return false;
	} 
	
}


