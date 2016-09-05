package com.mobshep.mobileshepherd;

import java.io.File;
import java.io.IOException;

import net.sqlcipher.database.SQLiteDatabase;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
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

public class CSInjection2 extends MainActivity implements OnClickListener {

    TabHost th;
    Button Login;
    EditText username;
    EditText password;
    EditText key;
    String dbPassword = "n483jd843hyj23kd032kjdw92";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.csi2_layout);
        th = (TabHost) findViewById(R.id.tabhost);
        populateTable(this, dbPassword);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        referenceXML();
        th.setup();
        generateKey(this, dbPassword);

        TabSpec specs = th.newTabSpec("tag1");
        specs.setContent(R.id.tab1);
        specs.setIndicator("Login");
        th.addTab(specs);

        specs = th.newTabSpec("tag2");
        specs.setContent(R.id.tab2);
        specs.setIndicator("Key");
        th.addTab(specs);
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
            Intent gotoIDS = new Intent(CSInjection2.this, Insecure_Data_Storage.class);
            startActivity(gotoIDS);
        } else if (id == R.id.nav_ids1) {
            Intent gotoIDS1 = new Intent(CSInjection2.this, Insecure_Data_Storage1.class);
            startActivity(gotoIDS1);
        } else if (id == R.id.nav_ids2) {
            Intent gotoIDS2 = new Intent(CSInjection2.this, Insecure_Data_Storage2.class);
            startActivity(gotoIDS2);
        } else if (id == R.id.nav_ids3) {
            Intent gotoIDS3 = new Intent(CSInjection2.this, ids3Login.class);
            startActivity(gotoIDS3);
        }else if (id == R.id.nav_bc) {
            Intent gotoBC = new Intent(CSInjection2.this, BrokenCrypto.class);
            startActivity(gotoBC);
        }else if (id == R.id.nav_bc1) {
            Intent gotoBC1 = new Intent(CSInjection2.this, BrokenCrypto1.class);
            startActivity(gotoBC1);
        }else if (id == R.id.nav_bc2) {
            Intent gotoBC2 = new Intent(CSInjection2.this, BrokenCrypto2.class);
            startActivity(gotoBC2);
        }else if (id == R.id.nav_bc3) {
            Intent gotoBC3 = new Intent(CSInjection2.this, BrokenCrypto3.class);
            startActivity(gotoBC3);
        }else if (id == R.id.nav_csi) {
            Intent gotoCSI = new Intent(CSInjection2.this, CSInjection.class);
            startActivity(gotoCSI);
        }else if (id == R.id.nav_csi1) {
            Intent gotoCSI1 = new Intent(CSInjection2.this, CSInjection.class);
            startActivity(gotoCSI1);
        }else if (id == R.id.nav_csi2) {
            Intent gotoCSI2 = new Intent(CSInjection2.this, CSInjection.class);
            startActivity(gotoCSI2);
        } if (id == R.id.nav_csi2) {
            Intent gotoCSI2 = new Intent(CSInjection2.this, CSInjection.class);
            startActivity(gotoCSI2);
        }if (id == R.id.nav_udl) {
            Intent gotoUDL = new Intent(CSInjection2.this, UDataLeakage.class);
            startActivity(gotoUDL);
        }if (id == R.id.nav_udl1) {
            Intent gotoUDL1 = new Intent(CSInjection2.this, UDataLeakage1.class);
            startActivity(gotoUDL1);
        }else if (id == R.id.nav_scoreboard) {
            //link to shepherd or webview?
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void referenceXML() {
        // TODO Auto-generated method stub
        Login = (Button) findViewById(R.id.bLogin);
        // Login.setFilterTouchesWhenObscured(true);
        username = (EditText) findViewById(R.id.etName);
        password = (EditText) findViewById(R.id.etPass);
        key = (EditText) findViewById(R.id.etKey);
        Login.setOnClickListener(this);

    }

    public void onClick(View arg0) {
        switch (arg0.getId()) {

            case (R.id.bLogin):

                String Name = username.getText().toString();
                String Pass = password.getText().toString();

                Name = sanitizeValue(Name);
                Pass = sanitizeValue(Pass);

                try {
                    if (login(Name, Pass) == true) {
                        outputKey(this, dbPassword);
                        Toast toast = Toast.makeText(CSInjection2.this,
                                "Logged in!", Toast.LENGTH_LONG);
                        toast.show();

                    }
                } catch (IOException e1) {
                    Toast toast = Toast.makeText(CSInjection2.this,
                            "An error occurred!", Toast.LENGTH_LONG);
                    toast.show();
                }

                try {
                    if (login(Name, Pass) == false) {
                        Toast toast = Toast.makeText(CSInjection2.this,
                                "Invalid Credentials, " + Name,
                                Toast.LENGTH_LONG);
                        toast.show();

                    }
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                if (Name.contentEquals("")
                        || Pass.contentEquals("")) {
                    Toast toast2 = Toast.makeText(CSInjection2.this,
                            "Empty Fields Detected.", Toast.LENGTH_SHORT);
                    toast2.show();

                }

        }

    }

    private boolean login(String username, String password) throws IOException {
        try{
            try {
                String dbPath = this.getDatabasePath("Users.db").getPath();

                SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(dbPath,
                        dbPassword, null);

                String query = ("SELECT * FROM Users WHERE memName='" + username
                        + "' AND memPass = '" + password + "';");

                Cursor cursor = db.rawQuery(query, null);
                if (cursor != null) {
                    if (cursor.getCount() <= 0) {
                        return false;

                    }
                }

            } catch (Exception e) {
                // TODO Auto-generated catch block
                Toast error = Toast.makeText(CSInjection2.this,
                        "An error occurred.", Toast.LENGTH_LONG);
                error.show();
                key.getText().clear();
                key.setHint("The key is only shown to authenticated users.");
                return false;
            }

        } catch (SQLiteException e) {
            Toast error = Toast.makeText(CSInjection2.this,
                    "An database error occurred.", Toast.LENGTH_LONG);
            error.show();
        }

        return true;

    }

    public void populateTable(Context context, String dbPassword) {
        try {

            try {
                SQLiteDatabase.loadLibs(context);

                String dbPath = context.getDatabasePath("Users.db").getPath();

                File dbPathFile = new File(dbPath);
                if (!dbPathFile.exists())
                    dbPathFile.getParentFile().mkdirs();

                SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(dbPath,
                        dbPassword, null);

                db.execSQL("DROP TABLE IF EXISTS Users");
                db.execSQL("CREATE TABLE Users(memID INTEGER PRIMARY KEY AUTOINCREMENT, memName TEXT, memAge INTEGER, memPass VARCHAR)");

                db.execSQL("INSERT INTO Users VALUES( 1,'Admin',20,'49c3a17ad8d8ccd93885e6a28661480d')");
            } catch (Exception e) {
                // TODO Auto-generated catch block
                Toast error = Toast.makeText(CSInjection2.this,
                        "An error occurred, table not initialized.", Toast.LENGTH_LONG);
                error.show();

            }

        } catch (SQLiteException e) {
            Toast error = Toast.makeText(CSInjection2.this,
                    "An database error occurred.", Toast.LENGTH_LONG);
            error.show();
        }
    }

    public void outputKey(Context context, String password) {
        SQLiteDatabase.loadLibs(context);

        String dbPath = context.getDatabasePath("key.db").getPath();

        SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(dbPath, dbPassword,
                null);

        String query = ("SELECT * FROM key3;");

        Cursor cursor = db.rawQuery(query, null);

        if (cursor != null) {

            try {
                if (cursor.moveToFirst())
                    key.setText(cursor.getString(0));
            } finally {
                cursor.close();

            }
        }
    }

    public void generateKey(Context context, String password) {
        try {
            try {
                SQLiteDatabase.loadLibs(context);

                String dbPath = context.getDatabasePath("key3.db").getPath();

                File dbPathFile = new File(dbPath);
                if (!dbPathFile.exists())
                    dbPathFile.getParentFile().mkdirs();

                SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(dbPath,
                        dbPassword, null);

                db.execSQL("DROP TABLE IF EXISTS key3");
                db.execSQL("CREATE TABLE key3(key VARCHAR)");

                db.execSQL("INSERT INTO key3 VALUES('The Key is BurpingChimneys.')");

            } catch (Exception e) {
                // TODO Auto-generated catch block
                Toast error = Toast.makeText(CSInjection2.this,
                        "An error occurred, key was not generated", Toast.LENGTH_LONG);
                error.show();

            }

        } catch (SQLiteException e) {
            Toast error = Toast.makeText(CSInjection2.this,
                    "An database error occurred.", Toast.LENGTH_LONG);
            error.show();
        }
    }

    public String sanitizeValue (String input){
        input = input.replace("OR", "");
        input = input.replace("or", "");
        input = input.replace("SELECT", "");
        input = input.replace("AND", "");
        input = input.replace("UPDATE", "");
        input = input.replace("DROP", "");
        input = input.replace("0", "");
        input = input.replace("1", "");
        input = input.replace("2", "");
        input = input.replace("3", "");
        input = input.replace("4", "");
        input = input.replace("5", "");
        input = input.replace("6", "");
        input = input.replace("7", "");
        input = input.replace("8", "");
        input = input.replace("9", "");
        input = input.replace ("--", "");

        input = input + " '" ;


        return input;
    }


}
