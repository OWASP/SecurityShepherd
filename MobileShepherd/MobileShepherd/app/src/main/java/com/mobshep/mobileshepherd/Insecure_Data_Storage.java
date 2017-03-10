package com.mobshep.mobileshepherd;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import java.io.IOException;


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

public class Insecure_Data_Storage extends MainActivity implements NavigationView.OnNavigationItemSelectedListener{


    private static String DB_PATH = "/data/data/com.mobshep.mobileshepherd/databases/InsecureDataStorage/";
    private static String DB_NAME = "Members";
    SQLiteDatabase Members = null;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        createDatabase();
        try {
            InsertData("Admin","Battery777");
        } catch (IOException e) {
            e.printStackTrace();
        }
        setTitle(R.string.ids);
        setContentView(R.layout.ids_layout);
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

    public void createDatabase() {
        try {

            String path = DB_PATH + DB_NAME;
            Members = this.openOrCreateDatabase(path, MODE_PRIVATE, null);
            Members.execSQL("CREATE TABLE IF NOT EXISTS Members " +
                            "(id integer primary key, name VARCHAR, password VARCHAR);"
            );

        } catch (Exception e) {
            Log.e("DB ERROR", "Error Creating Database");
        }

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent goToSettings = new Intent(this, Preferences.class);
            startActivity(goToSettings);
            return true;
        }



        if (id == R.id.action_exit) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }


    public void submitPressed(View v) throws IOException {

        EditText username = (EditText) findViewById(R.id.etName);
        EditText password = (EditText) findViewById(R.id.etPass);

        String user = username.getText().toString();
        String pass = password.getText().toString();

        InsertData(user,pass);


    }

    private void InsertData(String user, String pass) throws IOException {

        try {
            String path = DB_PATH + DB_NAME;
            Members = this.openOrCreateDatabase(path, MODE_PRIVATE, null);

            SQLiteStatement stmt = Members.compileStatement("INSERT INTO Members (name, password) VALUES (?,?);");
            stmt.bindString(1, user);
            stmt.bindString(2, pass);
            stmt.execute();

            Snackbar insert = Snackbar.make(findViewById(android.R.id.content), "Data Inserted!", Snackbar.LENGTH_LONG);
            insert.show();

            EditText username = (EditText) findViewById(R.id.etName);
            EditText password = (EditText) findViewById(R.id.etPass);

            username.setText("");
            password.setText("");

        } catch (Exception e) {
            Log.e("DB ERROR", "Error Inserting into Database");

            Snackbar error = Snackbar.make(findViewById(android.R.id.content), "Could not Insert Data.", Snackbar.LENGTH_LONG);
            error.show();
        }
    }

}