package com.mobshep.poorauthentication2;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
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

public class Authenticated extends Activity {
    EditText Key;
    Button getKey;
    Button insertKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.authenticated_layout);
        referenceXML();
    }


    public void insertKey(View v){
        DBHelper dbInstance;
        dbInstance = new DBHelper();

        String userInput = Key.getText().toString();

        dbInstance.insertKey(this, userInput);

        Toast insert = Toast.makeText(Authenticated.this, "Data Inserted!", Toast.LENGTH_SHORT);
        insert.show();
    }

    public void retrieveKey(View v){
        DBHelper dbInstance;
        dbInstance = new DBHelper();

        Toast get = Toast.makeText(Authenticated.this, "Getting Data...", Toast.LENGTH_SHORT);
        get.show();

        Key.setText(dbInstance.outputKey(this));

    }

    private void referenceXML(){
        Key = (EditText)findViewById(R.id.etKey);
        getKey = (Button)findViewById(R.id.bGetKey);
        insertKey = (Button)findViewById(R.id.bInsertKey);
    }

    @Override
    protected void onDestroy(){
    super.onDestroy();

       //close db

    }
}
