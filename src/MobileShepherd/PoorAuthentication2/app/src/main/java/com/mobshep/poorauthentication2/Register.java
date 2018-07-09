package com.mobshep.poorauthentication2;

import android.app.Activity;
import android.database.sqlite.SQLiteException;
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

public class Register extends MainActivity {

    EditText Username, Password;
    Button Register;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_layout);
        referenceXML();
    }

    public void Register(View v){
    String usernameInput = Username.getText().toString();
    String passwordInput = Password.getText().toString();


    if (usernameInput.length() < 6){
        Toast length = Toast.makeText(Register.this, "Username must be at least 6 characters.", Toast.LENGTH_SHORT);
        length.show();
        return;
    }

    if (passwordInput.length() < 6){
        Toast length = Toast.makeText(Register.this, "Password must be at least 6 characters.", Toast.LENGTH_SHORT);
        length.show();
        return;
    }

    try {

        DBHelper dbInstance;
        dbInstance = new DBHelper();
        dbInstance.insertData(this, usernameInput, passwordInput);

        Toast insert = Toast.makeText(Register.this, "Registered!", Toast.LENGTH_SHORT);
        insert.show();
    }catch(SQLiteException s){
        Toast insert = Toast.makeText(Register.this, "Error:" + s.toString(), Toast.LENGTH_SHORT);
        insert.show();
    }
    }



    public void returnToLogin(View v){
        this.finish();
    }

    private void referenceXML(){
        Username = (EditText)findViewById(R.id.etUsername);
        Password = (EditText)findViewById(R.id.etPassword);
        Register = (Button)findViewById(R.id.bRegister);

    }

}
