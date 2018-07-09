package com.mobshep.poorauthentication2;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
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

public class MainActivity extends ActionBarActivity {

    EditText Username, Password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DBHelper dbInstance;
        dbInstance = new DBHelper();
        dbInstance.populateTable(this,"");
        dbInstance.populateAnotherTable(this);

        referenceXML();
    }



    public void registerClicked(View v){
        Intent register = new Intent(this, Register.class);
        startActivity(register);
    }

    public void loginClicked(View v){

        String usernameInput = Username.getText().toString();
        String passwordInput = Password.getText().toString();

        //query db for row, if all fields satisfied, return key.
        DBHelper dbInstance;
        dbInstance = new DBHelper();

        if(dbInstance.queryData(this, usernameInput, passwordInput) == true){
            //authenticate user
            Intent authenticated = new Intent(this, Authenticated.class);
            startActivity(authenticated);
        }
        else{
            Toast invalid = Toast.makeText(MainActivity.this, "Invalid Credentials!", Toast.LENGTH_SHORT);
            invalid.show();
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

        //turn into switch

        if (id == R.id.action_license){
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
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    private void referenceXML() {
        Username = (EditText)findViewById(R.id.etUsername);
        Password = (EditText)findViewById(R.id.etPassword);
    }

}
