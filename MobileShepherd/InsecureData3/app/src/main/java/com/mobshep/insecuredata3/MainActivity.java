package com.mobshep.insecuredata3;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
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

public class MainActivity extends Activity{

    Button bSubmit;
    String username, password;
    EditText etUsername, etPassword;
    SharedPreferences storedPref;
    Editor toEdit;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        referenceXML();

        sharedPrefernces();
    }


    public void sharedPrefernces() {

        storedPref = getSharedPreferences("AppData", MODE_WORLD_READABLE);
        toEdit = storedPref.edit();
        toEdit.putString("Origin", "Europe");
        toEdit.putString("DOB", "12/12/1980" );
        toEdit.putString("Root","True" );
        toEdit.commit();


        storedPref = getSharedPreferences("Saved Data", MODE_WORLD_READABLE);
        toEdit = storedPref.edit();
        toEdit.putString("Username", "Tony");
        toEdit.putString("Password", "qazwsx4562");
        toEdit.commit();

    }


    public void onDestroy(){

        super.onDestroy();
    }


    public void loginClicked(View v){
        username = etUsername.getText().toString();
        password = etPassword.getText().toString();


        SharedPreferences prefs = this.getSharedPreferences("Saved Data", MODE_PRIVATE);
        String username = prefs.getString("Username", null);
        String password = prefs.getString("Password", null);

        if ((etUsername.getText().toString().equals(username) == true) && (etPassword.getText().toString().equals(password) == true)){
            Toast loggedIn = Toast.makeText(MainActivity.this,
                    "Logged in!", Toast.LENGTH_LONG);
            loggedIn.show();

            Intent goToLogin = new Intent(this, LoggedIn.class);
            startActivity(goToLogin);
            finish();

        }else{
            Toast error = Toast.makeText(MainActivity.this,
                    "Invalid Credentials!", Toast.LENGTH_LONG);
            error.show();
        }

    }

    public void referenceXML() {
        bSubmit = (Button) findViewById(R.id.submit);
        etUsername = (EditText) findViewById(R.id.etUsername);
        etPassword = (EditText) findViewById(R.id.etPassword);
    }
}