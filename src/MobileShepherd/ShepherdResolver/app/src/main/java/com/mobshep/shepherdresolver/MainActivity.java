package com.mobshep.shepherdresolver;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;


public class MainActivity extends Activity {


    TextView session;
    SharedPreferences storedSession;
    SharedPreferences.Editor toEdit;


    // The URL used to target the content provider
    static final Uri CONTENT_URL =
            Uri.parse("content://com.mobshep.shepherdlogin.SessionProvider/data");

    // Provides access to other applications Content Providers
    ContentResolver resolver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        session = (TextView) findViewById(R.id.textView);

        resolver = getContentResolver();

        try{
        getSession();
        }catch(SQLiteException e) {
            Log.d("ShepherdResolver", "No Table Found!");
        }
        }


    public void getSession(){

        // Projection contains the columns we want
        String[] projection = new String[]{"id", "sessionValue"};


            // Pass the URL, projection
            Cursor cursor = resolver.query(CONTENT_URL, projection, "1", null, null);

        String sessionList = "";

        // Cycle through and display every row of data
        if(cursor.moveToFirst()){

            do{

                String id = cursor.getString(cursor.getColumnIndex("id"));
                String sessionValue = cursor.getString(cursor.getColumnIndex("sessionValue"));

                sessionList = sessionList + sessionValue + "\n";

            }while (cursor.moveToNext());
        }

        session.setText(sessionList);

        if (sessionList.equals("")){

            Intent launchIntent = getPackageManager().getLaunchIntentForPackage("com.mobshep.shepherdlogin");
            startActivity(launchIntent);

            this.finish();

        }

        storedSession = getSharedPreferences("session", MODE_PRIVATE);
        toEdit = storedSession.edit();
        toEdit.putString("session", sessionList);
        toEdit.commit();

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

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
