package com.mobshep.mobileshepherd;

import android.content.ContentValues;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;


public class providerLeakage extends AppCompatActivity {

    EditText keyEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.provider_content);

        keyEditText = (EditText) findViewById(R.id.keyEditText);
    }

    public void addKey(View view) {

        String key = keyEditText.getText().toString();

        ContentValues values = new ContentValues();
        values.put(com.mobshep.mobileshepherd.SecretProvider.key, key);

        // Provides access to other applications Content Providers
        Uri uri = getContentResolver().insert(com.mobshep.mobileshepherd.SecretProvider.CONTENT_URL, values);

        Toast.makeText(getBaseContext(), "New Key Added", Toast.LENGTH_LONG)
                .show();
    }

}
