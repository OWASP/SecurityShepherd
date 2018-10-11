package com.mobshep.shepherdlogin;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import com.mobshep.shepherdlogin.SessionProvider.*;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.util.ArrayList;

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

public class LoggedIn extends Activity {

    SharedPreferences storedPref;
    SharedPreferences.Editor toEdit;

    Button submit;

    private static final String TAG = "LoggedIn";

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loggedin);
        submit = (Button)findViewById(R.id.bGetKey);
        checkNullSession();
    }

    public void submitClicked(View v) {

        String apiKey= "thisIsTheAPIKey";

        ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
        postParameters.add(new BasicNameValuePair("mobileKey", apiKey));

        try {

            SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
            String address = SP.getString("server_preference", "NA");

            String res = CustomHttpClient.executeHttpPost(address + "/vulMobileAPI", postParameters);

            JSONObject jObject = new JSONObject(res.toString());

            String response = jObject.getString("LEVELKEY");

            response = response.replaceAll("\\s+", "");

            Toast responseError = Toast.makeText(LoggedIn.this,
                    response.toString(), Toast.LENGTH_SHORT);
            responseError.show();

            if (res!=null) {
                storedPref = getSharedPreferences("Sessions", MODE_PRIVATE);
                toEdit = storedPref.edit();
                toEdit.putString("LEVELKEY", response);
                toEdit.commit();

            } else {
                Toast.makeText(getBaseContext(), "Invalid API Key!", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {

            Toast responseError = Toast.makeText(LoggedIn.this,
                    e.toString(), Toast.LENGTH_LONG);
            responseError.show();
        }

    }

    private void checkNullSession() {

        SharedPreferences prefs = this.getSharedPreferences("Sessions", MODE_PRIVATE);
        String sessionId = prefs.getString("sessionId", "null");

        if (sessionId.equals("null")){
            finish();
            Intent intent = new Intent(LoggedIn.this, MainActivity.class);
            startActivity(intent);
        }
    }

    public void logoutClicked(View v){

        storedPref = getSharedPreferences("Sessions", MODE_PRIVATE);
        toEdit = storedPref.edit();
        toEdit.clear();
        toEdit.commit();

        //delete * from sessions table
        SessionProvider.DatabaseHelper providerInstance;
        providerInstance = new DatabaseHelper(this);
        providerInstance.deleteData();
        checkNullSession();

    }
}