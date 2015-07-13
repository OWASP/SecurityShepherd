package com.mobshep.sessionmanagement;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity {

    Button getKey;
    EditText showKey;
    SharedPreferences storedPref;
    SharedPreferences.Editor toEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        storedPref = getSharedPreferences("Sessions", MODE_WORLD_READABLE);
        toEdit = storedPref.edit();
        toEdit.putString("SessionToken", "null");
        toEdit.commit();

    }


    public void getKeyClicked(View v) {

        SharedPreferences prefs = this.getSharedPreferences("SessionToken", MODE_PRIVATE);
        String session = prefs.getString("SessionToken", null);

        if (session == "null") {
            Toast noSession = Toast.makeText(MainActivity.this,
                    "You do not have an active session!", Toast.LENGTH_LONG);
            noSession.show();
        }

            else{
            Toast gettingKey = Toast.makeText(MainActivity.this,
                    "Getting the key...", Toast.LENGTH_LONG);
            gettingKey.show();
            }

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

        switch (id) {

            case R.id.action_settings:

                Intent goToSettings = new Intent(this, Preferences.class);
                startActivity(goToSettings);
                return true;

            case R.id.action_exit:

                this.finish();

                break;

            default:

        }
        return super.onOptionsItemSelected(item);

    }

}
