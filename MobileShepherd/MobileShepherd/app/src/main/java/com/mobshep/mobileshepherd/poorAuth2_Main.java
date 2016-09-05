package com.mobshep.mobileshepherd;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import java.io.*;

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

public class poorAuth2_Main extends Activity {


    private static final String[] authCodes = {"784921", "425925", "257943", "524215", "624665"};

    EditText username, password;
    Button login;
    AutoCompleteTextView authCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        referenceXML();
        String destinationDir = this.getFilesDir().getParentFile().getPath() + "/cache/";

        String destinationPath = destinationDir + "usedAuthCodes";

        File f = new File(destinationPath);

        if (!f.exists()) {
            File directory = new File(destinationDir);
            directory.mkdirs();

            try {
                copyCache(getBaseContext().getAssets().open("usedAuthCodes"),
                        new FileOutputStream(destinationPath));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, authCodes);
        authCode.setAdapter(adapter);
    }

    public void loginClicked(View v) {


        if (checkTheCode() == true) {
            Intent goToKey = new Intent(this, poorAuth2_LoggedIn.class);
            startActivity(goToKey);
        } else {
            showError();
        }

    }


    private void showError() {
        Toast loginFailed = Toast.makeText(poorAuth2_Main.this,
                "Invalid Credentials!", Toast.LENGTH_LONG);
        loginFailed.show();

    }


    public boolean checkTheCode() {

        String codeString = authCode.getText().toString();

        if (codeString.equals("")) {
            Toast.makeText(this, "Blank Fields Detected", Toast.LENGTH_SHORT).show();
            return false;
        }

        int codeEntered = Integer.parseInt(codeString);

        //rule 1: auth code must be an odd number
        //rule 2: auth code must contain a 2
        //rule 3: auth code must contain a 4
        //rule 4: must be a six digit number
        //rule 5: number must not have been used before

        boolean isNumberOdd = false;
        boolean contains2 = false;
        boolean contains4 = false;
        boolean isSixNumbers = false;

        int mod = codeEntered % 2;
        {

            if (mod != 0) {

                isNumberOdd = true;
                Log.i("LOG", "Number is odd...");
                isNumberOdd=true;

            } else {
                return false;
            }

            if (String.valueOf(codeString).contains("2")) {

                contains2 = true;
                Log.i("LOG", "Number contains 2...");

            } else {
                return false;
            }

            if (String.valueOf(codeString).contains("4")) {
                contains4 = true;
                Log.i("LOG", "Number contains 4...");
            } else {
                return false;
            }

            if ((authCode.length() == 6)) {
                isSixNumbers = true;
                Log.i("LOG", "Number contains six digits...");
            } else {
                return false;
            }

            if (isNumberOdd == true && contains2 == true && contains4 == true && isSixNumbers == true) {
                return true;
            } else {
                return false;
            }
        }
    }
    public void referenceXML() {


        username = (EditText) findViewById(R.id.etName);
        username.setEnabled(false);
        password = (EditText) findViewById(R.id.etPass);
        password.setEnabled(false);
        login = (Button) findViewById(R.id.bLogin);
        authCode = (AutoCompleteTextView) findViewById(R.id.etCode);


    }


    public void copyCache(InputStream iStream, OutputStream oStream)
            throws IOException {
        byte[] buffer = new byte[1024];
        int i;
        while ((i = iStream.read(buffer)) > 0) {
            oStream.write(buffer, 0, i);
        }
        iStream.close();
        oStream.close();
    }


}
