package com.mobshep.mobileshepherd;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.content.Intent;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
       checkAddress();


        if (internetStatus() == false){
            Snackbar.make(null, "Internet Access is required for some challenges", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    private void checkAddress() {
        SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        String address = SP.getString("server_preference", "NA");

        Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "Server Address:" + address, Snackbar.LENGTH_LONG);
        snackbar.show();

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


    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_ids) {
            Intent gotoIDS = new Intent(MainActivity.this, Insecure_Data_Storage.class);
            startActivity(gotoIDS);
        } else if (id == R.id.nav_ids1) {
            Intent gotoIDS1 = new Intent(MainActivity.this, Insecure_Data_Storage1.class);
            startActivity(gotoIDS1);
        } else if (id == R.id.nav_ids2) {
            Intent gotoIDS2 = new Intent(MainActivity.this, Insecure_Data_Storage2.class);
            startActivity(gotoIDS2);
        } else if (id == R.id.nav_ids3) {
           Intent gotoIDS3 = new Intent(MainActivity.this, ids3Login.class);
           startActivity(gotoIDS3);
        }else if (id == R.id.nav_bc) {
            Intent gotoBC = new Intent(MainActivity.this, BrokenCrypto.class);
            startActivity(gotoBC);
        }else if (id == R.id.nav_bc1) {
            Intent gotoBC1 = new Intent(MainActivity.this, BrokenCrypto1.class);
            startActivity(gotoBC1);
        }else if (id == R.id.nav_bc2) {
            Intent gotoBC2 = new Intent(MainActivity.this, BrokenCrypto2.class);
            startActivity(gotoBC2);
        }else if (id == R.id.nav_bc3) {
            Intent gotoBC3 = new Intent(MainActivity.this, BrokenCrypto3.class);
            startActivity(gotoBC3);
        }else if (id == R.id.nav_csi) {
            Intent gotoCSI = new Intent(MainActivity.this, CSInjection.class);
            startActivity(gotoCSI);
        }else if (id == R.id.nav_csi1) {
            Intent gotoCSI1 = new Intent(MainActivity.this, CSInjection1.class);
            startActivity(gotoCSI1);
        }else if (id == R.id.nav_csi2) {
            Intent gotoCSI2 = new Intent(MainActivity.this, CSInjection2.class);
            startActivity(gotoCSI2);
        }if (id == R.id.nav_udl) {
            Intent gotoUDL = new Intent(MainActivity.this, UDataLeakage.class);
            startActivity(gotoUDL);
        }if (id == R.id.nav_udl1) {
            Intent gotoUDL1 = new Intent(MainActivity.this, UDataLeakage1.class);
            startActivity(gotoUDL1);
        }if (id == R.id.nav_udl2) {
            Intent gotoUDL2 = new Intent(MainActivity.this, UnintendedDataLeakage2.class);
            startActivity(gotoUDL2);
        }if (id == R.id.nav_pa) {
            Intent gotoPA = new Intent(MainActivity.this, poorAuth.class);
            startActivity(gotoPA);
        }if (id == R.id.nav_pa1) {
            Intent gotoPA1 = new Intent(MainActivity.this, poorAuth.class);
            startActivity(gotoPA1);
        }if (id == R.id.nav_pa2) {
            Intent gotoPA2 = new Intent(MainActivity.this, poorAuth2_Main.class);
            startActivity(gotoPA2);
        }if (id == R.id.nav_ui) {
            Intent gotoUI = new Intent(MainActivity.this, untrustedInput.class);
            startActivity(gotoUI);
        }if (id == R.id.nav_pl) {
            Intent gotoPL = new Intent(MainActivity.this, providerLeakage.class);
            startActivity(gotoPL);
        }else if (id == R.id.nav_scoreboard) {
            openScoreBoardNavBar();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public boolean internetStatus(){
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(this.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();

    }

    public void openScoreBoard(View v){
        SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        String address = SP.getString("server_preference", "NA");

        if (address == null){
            Snackbar.make(null, "You need to add the server address in settings.", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }
        else if (address != null) {

            Uri uri = Uri.parse(address);
            Intent intentURI = new Intent(Intent.ACTION_VIEW, uri);

            if (intentURI.resolveActivity(getPackageManager()) != null) {

                try {
                    startActivity(intentURI);
                }catch (IllegalStateException e) {
                    e.printStackTrace();
                    Snackbar.make(null, "Invalid URL entered.", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            } else {
                Snackbar.make(null, "Play Store required to open links", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        }
    }

    public void openScoreBoardNavBar(){
        SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        String address = SP.getString("server_preference", "NA");

        if (address == null){
            Snackbar.make(null, "You need to add the server address in settings.", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }
        else if (address != null) {

            Uri uri = Uri.parse(address);
            Intent intentURI = new Intent(Intent.ACTION_VIEW, uri);

            if (intentURI.resolveActivity(getPackageManager()) != null) {

                try {
                    startActivity(intentURI);
                }catch (IllegalStateException e) {
                    e.printStackTrace();
                    Snackbar.make(null, "Invalid URL entered.", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            } else {
                Snackbar.make(null, "Play Store required to open links", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        }
    }

//TODO Implement Sessions
/*
    public boolean checkSession(){

        SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        String sesh = SP.getString("session", "null");


        if (sesh.equals("null")) {
            return false;
        }
        else{
            return true;
        }
    }
*/
}
