package com.mobshep.poorauthentication2;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;


public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DBHelper dbInstance;
        dbInstance = new DBHelper();
        dbInstance.populateTable(this,"");


    }

    public void registerClicked(View v){
        Intent register = new Intent(this, Register.class);
        startActivity(register);
    }

    public void loginClicked(View v){
        //query db for row, if all fields satisfied, return key.

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

        //turn into switch

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        if (id == R.id.action_license){

        }

        if (id == R.id.action_exit){

        }

        return super.onOptionsItemSelected(item);
    }
}
