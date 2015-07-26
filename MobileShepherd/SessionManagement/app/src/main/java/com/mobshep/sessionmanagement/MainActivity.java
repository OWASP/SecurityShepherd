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

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {

    Button getKey;
    EditText showKey;
    SharedPreferences storedPref;
    SharedPreferences.Editor toEdit;
    String serverKey="ea8acb8145f7e3c55e696e83d3746e327fed0bc8115554ca43bfe1fe2d295b94";

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


            ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
            postParameters.add(new BasicNameValuePair("getPoorSessionKey", serverKey));

            try {

                SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                String address = SP.getString("server_preference", "NA");

                String res = CustomHttpClient.executeHttpPost(address , postParameters);

                JSONObject jObject = new JSONObject(res);

                String response = (String) jObject.getString("JSESSIONID");

                System.out.println("SessionId: " + response);

                response = response.replaceAll("\\s+", "");

                Toast responseError = Toast.makeText(MainActivity.this,
                        response, Toast.LENGTH_SHORT);
                responseError.show();


                if (response.contains("Value ERROR")==true){
                    showKey.setText("Invalid Session Detected");
                }
                else {
                    showKey.setText(response);
                }

            } catch (Exception e) {

                Toast responseError = Toast.makeText(MainActivity.this,
                        e.toString(), Toast.LENGTH_LONG);
                responseError.show();

                showKey.setText(e.toString());

            }

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
