package com.mobshep.mobileshepherd;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
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

public class ids3Login extends MainActivity{

    Button bSubmit;
    String username, password;
    EditText etUsername, etPassword;
    SharedPreferences storedPref;
    Editor toEdit;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ids3login_layout);
        referenceXML();
        sharedPrefernces();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

    }


    public void sharedPrefernces() {

        storedPref = getSharedPreferences("AppData", MODE_PRIVATE);
        toEdit = storedPref.edit();
        toEdit.putString("Origin", "Europe");
        toEdit.putString("DOB", "12/12/1980" );
        toEdit.putString("Root","True" );
        toEdit.commit();


        storedPref = getSharedPreferences("Saved Data", MODE_PRIVATE);
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
            Toast loggedIn = Toast.makeText(ids3Login.this,
                    "Logged in!", Toast.LENGTH_LONG);
            loggedIn.show();

            Intent goToLogin = new Intent(this, ids3Authenticated.class);
            startActivity(goToLogin);
            finish();

        }else{
            Toast error = Toast.makeText(ids3Login.this,
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