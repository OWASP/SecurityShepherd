package com.mobshep.mobileshepherd;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.util.Random;


public class UnintendedDataLeakage2 extends MainActivity implements View.OnClickListener, NavigationView.OnNavigationItemSelectedListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.udl2_layout);
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



    public void lottoClicked(View v){
        Log.i("LOG", "Getting Winning Number from Server...");

        Log.i("LOG", "Input Field Blank, no message sent.");

        String yourNumber = getRandomNumber(11);

        Log.i("LOG", "  ༼ つ ◕_◕ ༽つ Winning number:627884736748 " + " is not equal to your number. "+ yourNumber);

        Toast loser = Toast.makeText(UnintendedDataLeakage2.this,
                "So Sorry.... You did not win today.", Toast.LENGTH_LONG);
        loser.show();

            }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent goToSettings = new Intent(this, Preferences.class);
            startActivity(goToSettings);
            return true;
        }


        if (id == R.id.action_exit){
            finish();
        }


        return super.onOptionsItemSelected(item);
    }



    private static Random random = new Random();

    public static String getRandomNumber(int digCount) {
        StringBuilder sb = new StringBuilder(digCount);
        for (int i = 0; i < digCount; i++)
            sb.append((char) ('0' + random.nextInt(10)));
        return sb.toString();
    }

    @Override
    public void onClick(View v) {

    }
}
