package com.mobshep.sessionmanagement;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

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

public class MainActivity extends AppCompatActivity {

    Button getKey;
    EditText showKey;
    SharedPreferences storedPref;
    SharedPreferences.Editor toEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        showKey = (EditText) findViewById(R.id.etKey);

        storedPref = getSharedPreferences("Sessions", MODE_WORLD_READABLE);
        toEdit = storedPref.edit();
        toEdit.putString("SessionToken", "");
        toEdit.commit();

    }

    public void getKeyClicked(View v) {

        SharedPreferences prefs = this.getSharedPreferences("SessionToken", MODE_PRIVATE);
        String session = prefs.getString("SessionToken", null);

        if (session==null) {
            Toast noSession = Toast.makeText(MainActivity.this,
                    "You do not have an active session!", Toast.LENGTH_LONG);
            noSession.show();
        }

            else{
            // temp solution until SSO is implemented
            Toast gettingKey = Toast.makeText(MainActivity.this,
                    "Getting the key...", Toast.LENGTH_LONG);
            gettingKey.show();

            showKey.setText("BlueBanjosNewNachos");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {

            case R.id.action_settings:

                Intent goToSettings = new Intent(this, Preferences.class);
                startActivity(goToSettings);
                return true;

            case R.id.action_restart:

                storedPref = getSharedPreferences("Sessions", MODE_WORLD_READABLE);
                toEdit = storedPref.edit();
                toEdit.putString("SessionToken", "");
                toEdit.commit();

                break;

            case R.id.action_exit:

                this.finish();

                break;

            default:

        }
        return super.onOptionsItemSelected(item);
    }

}
