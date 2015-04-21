package com.mobshep.insecuredata4;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class SharedPreferencesActivity extends Activity{

    Button bSubmit;
    String username, password;
    EditText etUsername, etPassword;
    SharedPreferences storedPref;
    Editor toEdit;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        referenceXML();
    }


    public void sharedPrefernces() {

        storedPref = getSharedPreferences("Login Credentials", MODE_WORLD_READABLE);
        toEdit = storedPref.edit();
        toEdit.putString("Username", username);
        toEdit.putString("Password", password);
        toEdit.commit();

    }


    public void loginClicked(View v){
        username = etUsername.getText().toString();
        password = etPassword.getText().toString();
        sharedPrefernces();
    }


    public void referenceXML() {
        bSubmit = (Button) findViewById(R.id.submit);
        etUsername = (EditText) findViewById(R.id.etUsername);
        etPassword = (EditText) findViewById(R.id.etPassword);
    }
}