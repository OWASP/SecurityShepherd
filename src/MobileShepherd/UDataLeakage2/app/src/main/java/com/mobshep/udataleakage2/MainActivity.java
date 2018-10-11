package com.mobshep.udataleakage2;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.util.Random;


public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.functions);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.menu_main, menu);
            return true;
        }

    public void lottoClicked(View v){
        Log.i("LOG", "Getting Winning Number from Server...");

        Log.i("LOG", "Input Field Blank, no message sent.");

        String yourNumber = getRandomNumber(11);

        Log.i("LOG", "  ༼ つ ◕_◕ ༽つ Winning number:627884736748 " + " is not equal to your number. "+ yourNumber);

        Toast loser = Toast.makeText(MainActivity.this,
                "So Sorry.... You did not win today.", Toast.LENGTH_LONG);
        loser.show();

            }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_license) {

            Log.i("LOG", "License MenuItem Selected.");

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                    this);

            // set title
            alertDialogBuilder.setTitle("License");

            // set dialog message
            alertDialogBuilder
                    .setMessage("This App is part of the Security Shepherd Project. The Security Shepherd project is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version. The Security Shepherd project is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details. You should have received a copy of the GNU General Public License along with the Security Shepherd project.  If not, see http://www.gnu.org/licenses.")
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

        if (id == R.id.action_exit){
            Log.i("LOG", "Exit MenuItem Selected.");

            finish();
        }


        if (id == R.id.action_feedback)
        {
            Log.i("LOG", "Feedback MenuItem Selected.");


            Intent goToFeedback = new Intent(this, Feedback.class);
            startActivity(goToFeedback);
            finish();
            return true;
        }

        if (id == R.id.action_query)
        {
            Log.i("LOG", "Query MenuItem Selected.");

            Intent goToQuery = new Intent(this, Query.class);
            startActivity(goToQuery);
            finish();
            return true;
        }


        if (id == R.id.action_main)
        {
            Log.i("LOG", "Lotto MenuItem Selected.");

            Intent goToMain = new Intent(this, MainActivity.class);
            startActivity(goToMain);
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
}
