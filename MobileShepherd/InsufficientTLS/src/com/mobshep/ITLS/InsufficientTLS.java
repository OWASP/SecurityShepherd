package com.mobshep.ITLS;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.StrictMode;
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
public class InsufficientTLS extends Activity {

	Button send;
	Button method1, method2, method3;
	EditText IP;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		setContentView(R.layout.configure);
		referenceXML();

		// these two lines are temporary and should NOT make it to the final
		// app. Create an Async task for any future network activities
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
				.permitAll().build();
		StrictMode.setThreadPolicy(policy);

		if (isNetworkAvailable() == false) {
			// There is no network....
		}

	}

	private void referenceXML() {
		// TODO Auto-generated method stub
		send = (Button) findViewById(R.id.bSecret);
		method1 = (Button) findViewById(R.id.method1);
		method2 = (Button) findViewById(R.id.method2);
		
	}

	public void method1() {

		HttpClient client = new DefaultHttpClient();
		HttpPost post = new HttpPost("www.google.com");

		List<NameValuePair> pairs = new ArrayList<NameValuePair>();
		pairs.add(new BasicNameValuePair("key1", "value1"));
		pairs.add(new BasicNameValuePair("key2", "value2"));
		try {
			post.setEntity(new UrlEncodedFormEntity(pairs));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			HttpResponse response = client.execute(post);
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// Create a new HttpClient and Post Header
		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost = new HttpPost("http://www.google.ie");

		try {
			// Add your data
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
			nameValuePairs.add(new BasicNameValuePair("id", "12345"));
			nameValuePairs.add(new BasicNameValuePair("stringdata",
					"This is a duck. Look at it quack."));
			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

			// Execute HTTP Post Request
			HttpResponse response = httpclient.execute(httppost);

		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
		} catch (IOException e) {
			// TODO Auto-generated catch block
		}
	}
	
	public void method2() {
		
		//Create a json object and put Key value pairs as mapped by server.
		
		JSONObject jsonobj = new JSONObject();
		try {
			jsonobj.put("email", "a@b.com");
			jsonobj.put("old_passw", "306");
			jsonobj.put("use_id", "123");
			jsonobj.put("new_passw", "456");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//Create http client and post URL.
		DefaultHttpClient httpclient = new DefaultHttpClient();
		HttpPost httppostreq = new HttpPost("www.google.ie");
		
		//Create a String entity. String entity is appended to the url in a format that is required in HTTP POST.


		try {
			StringEntity se = new StringEntity(jsonobj.toString());
			se.setContentType("application/json;charset=UTF-8");
			se.setContentEncoding((Header) new BasicHeader(HTTP.CONTENT_TYPE,"application/json;charset=UTF-8"));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	

	public boolean isNetworkAvailable() {
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = cm.getActiveNetworkInfo();

		// if no network is available networkInfo will be null
		// otherwise check if we are connected
		if (networkInfo != null && networkInfo.isConnected()) {
			return true;
		}
		return false;
	}


	

	public void method3() {

	}

}
