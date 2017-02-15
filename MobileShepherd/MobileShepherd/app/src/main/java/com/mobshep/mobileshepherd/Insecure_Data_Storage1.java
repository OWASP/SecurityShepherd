package com.mobshep.mobileshepherd;

import java.io.File;
import java.io.IOException;
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

public class Insecure_Data_Storage1 extends MainActivity implements NavigationView.OnNavigationItemSelectedListener{

    private static String DB_PATH = "/data/data/com.mobshep.mobileshepherd/databases/InsecureDataStorage1/";
    private static String DB_NAME = "Users";

    SQLiteDatabase Users = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setTitle(R.string.ids1);
        setContentView(R.layout.ids_layout);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        createDatabase();
        initialiseTable();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
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
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_ids) {
            Intent gotoIDS = new Intent(Insecure_Data_Storage1.this, Insecure_Data_Storage.class);
            startActivity(gotoIDS);
        } else if (id == R.id.nav_ids1) {
            Intent gotoIDS1 = new Intent(Insecure_Data_Storage1.this, Insecure_Data_Storage1.class);
            startActivity(gotoIDS1);
        } else if (id == R.id.nav_ids2) {
            Intent gotoIDS2 = new Intent(Insecure_Data_Storage1.this, Insecure_Data_Storage2.class);
            startActivity(gotoIDS2);
        } else if (id == R.id.nav_ids3) {
            Intent gotoIDS3 = new Intent(Insecure_Data_Storage1.this, ids3Login.class);
            startActivity(gotoIDS3);
        }else if (id == R.id.nav_bc) {
            Intent gotoBC = new Intent(Insecure_Data_Storage1.this, BrokenCrypto.class);
            startActivity(gotoBC);
        }else if (id == R.id.nav_bc1) {
            Intent gotoBC1 = new Intent(Insecure_Data_Storage1.this, BrokenCrypto1.class);
            startActivity(gotoBC1);
        }else if (id == R.id.nav_bc2) {
            Intent gotoBC2 = new Intent(Insecure_Data_Storage1.this, BrokenCrypto2.class);
            startActivity(gotoBC2);
        }else if (id == R.id.nav_bc3) {
            Intent gotoBC3 = new Intent(Insecure_Data_Storage1.this, BrokenCrypto3.class);
            startActivity(gotoBC3);
        }else if (id == R.id.nav_csi) {
            Intent gotoCSI = new Intent(Insecure_Data_Storage1.this, CSInjection.class);
            startActivity(gotoCSI);
        }else if (id == R.id.nav_csi1) {
            Intent gotoCSI1 = new Intent(Insecure_Data_Storage1.this, CSInjection.class);
            startActivity(gotoCSI1);
        }else if (id == R.id.nav_csi2) {
            Intent gotoCSI2 = new Intent(Insecure_Data_Storage1.this, CSInjection.class);
            startActivity(gotoCSI2);
        } if (id == R.id.nav_csi2) {
            Intent gotoCSI2 = new Intent(Insecure_Data_Storage1.this, CSInjection.class);
            startActivity(gotoCSI2);
        }if (id == R.id.nav_udl) {
            Intent gotoUDL = new Intent(Insecure_Data_Storage1.this, UDataLeakage.class);
            startActivity(gotoUDL);
        }if (id == R.id.nav_udl1) {
            Intent gotoUDL1 = new Intent(Insecure_Data_Storage1.this, UDataLeakage1.class);
            startActivity(gotoUDL1);
        }if (id == R.id.nav_pl) {
            Intent gotoPL = new Intent(Insecure_Data_Storage1.this, providerLeakage.class);
            startActivity(gotoPL);
        }else if (id == R.id.nav_scoreboard) {
            //link to shepherd or webview?
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    public void createDatabase() {
        try {
            String path = DB_PATH + DB_NAME;
            Users = this.openOrCreateDatabase(path, MODE_PRIVATE, null);
            Users.execSQL("CREATE TABLE IF NOT EXISTS Users " +
                            "(id integer primary key, name VARCHAR, password VARCHAR);"
            );

            File database = getApplication().getDatabasePath("Users.db");

            if (!database.exists()) {
                Toast.makeText(this, "Database Created", Toast.LENGTH_SHORT).show();
            } else
                Toast.makeText(this, "Database Missing", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Log.e("DB ERROR", "Error Creating Database");
        }
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
            Users = this.openOrCreateDatabase(path, MODE_PRIVATE, null);

            //get the base64 functionality
            annoyingObfuscationUtil util = new annoyingObfuscationUtil();

            SQLiteStatement stmt = Users.compileStatement("INSERT INTO Users (name, password) VALUES (?,?);");
            stmt.bindString(1, user);
            stmt.bindString(2, util.Obfuscation1(pass));
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


    public void initialiseTable(){

        Users.execSQL("DELETE FROM Users;");

        Users.execSQL("INSERT INTO Users (name, password) VALUES ('Tyrkyr','ZG9jaGRvY2hkb2No');");
        Users.execSQL("INSERT INTO Users (name, password) VALUES ('ToothBrush','MmNvb2w0dWxvbD8=');");
        Users.execSQL("INSERT INTO Users (name, password) VALUES ('TroolMann','QnJpZGdlcw==');");
        Users.execSQL("INSERT INTO Users (name, password) VALUES ('Patrick','ZGlub3NhdXI=');");
        Users.execSQL("INSERT INTO Users (name, password) VALUES ('bottles','cGFzc3dvcmQxMjM0');");
        Users.execSQL("INSERT INTO Users (name, password) VALUES ('Root','V2Fyc2hpcHNBbmRXcmVuY2hlcw==');");
    }

}
