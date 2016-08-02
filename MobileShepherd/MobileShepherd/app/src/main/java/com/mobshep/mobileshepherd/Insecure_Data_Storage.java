package com.mobshep.mobileshepherd;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;



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
        insertKey();
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

        if (id == R.id.action_disclaimer) {


            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                    this);

            // set title
            alertDialogBuilder.setTitle("Disclaimer");

            // set dialog message
            alertDialogBuilder
                    .setMessage("This App may collect logs via various methods. By using this App you agree to this.")
                    .setCancelable(false)
                    .setPositiveButton("OK",new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog,int id) {
                            // if this button is clicked, close
                            // current activity
                            dialog.cancel();
                        }
                    });

            // create alert dialog
            AlertDialog alertDialog = alertDialogBuilder.create();

            // show it
            alertDialog.show();

            return true;
        }

        if (id == R.id.action_exit) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_ids) {
            Intent gotoIDS = new Intent(Insecure_Data_Storage.this, Insecure_Data_Storage.class);
            startActivity(gotoIDS);
        } else if (id == R.id.nav_ids1) {
            Intent gotoIDS1 = new Intent(Insecure_Data_Storage.this, Insecure_Data_Storage1.class);
            startActivity(gotoIDS1);
        } else if (id == R.id.nav_ids2) {
            Intent gotoIDS2 = new Intent(Insecure_Data_Storage.this, Insecure_Data_Storage2.class);
            startActivity(gotoIDS2);
        } else if (id == R.id.nav_ids3) {
            Intent gotoIDS3 = new Intent(Insecure_Data_Storage.this, ids3Login.class);
            startActivity(gotoIDS3);
        }else if (id == R.id.nav_bc) {
            Intent gotoBC = new Intent(Insecure_Data_Storage.this, BrokenCrypto.class);
            startActivity(gotoBC);
        }else if (id == R.id.nav_bc1) {
            Intent gotoBC1 = new Intent(Insecure_Data_Storage.this, BrokenCrypto1.class);
            startActivity(gotoBC1);
        }else if (id == R.id.nav_bc2) {
            Intent gotoBC2 = new Intent(Insecure_Data_Storage.this, BrokenCrypto2.class);
            startActivity(gotoBC2);
        }else if (id == R.id.nav_bc3) {
            Intent gotoBC3 = new Intent(Insecure_Data_Storage.this, BrokenCrypto3.class);
            startActivity(gotoBC3);
        }else if (id == R.id.nav_csi) {
            Intent gotoCSI = new Intent(Insecure_Data_Storage.this, CSInjection.class);
            startActivity(gotoCSI);
        }else if (id == R.id.nav_csi1) {
            Intent gotoCSI1 = new Intent(Insecure_Data_Storage.this, CSInjection1.class);
            startActivity(gotoCSI1);
        }else if (id == R.id.nav_csi2) {
            Intent gotoCSI2 = new Intent(Insecure_Data_Storage.this, CSInjection2.class);
            startActivity(gotoCSI2);
        }if (id == R.id.nav_udl) {
            Intent gotoUDL = new Intent(Insecure_Data_Storage.this, UDataLeakage.class);
            startActivity(gotoUDL);
        }if (id == R.id.nav_udl1) {
            Intent gotoUDL1 = new Intent(Insecure_Data_Storage.this, UDataLeakage1.class);
            startActivity(gotoUDL1);
        }else if (id == R.id.nav_scoreboard) {
            //link to shepherd or webview?
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    public void insertKey(){
        Members.execSQL("DELETE FROM Members;");
        Members.execSQL("INSERT INTO Members (name, password) VALUES ('Admin','Battery777');");
    }

}