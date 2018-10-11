package com.mobshep.udataleakage2;


import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class Query extends MainActivity{

    private Button bQuery;

    private EditText etQuery;

    protected void onCreate(Bundle savedInstanceState) {
        Log.i("LOG", "Started Query Activity.");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.submitquery);
        Log.d("DEBUG", "Entered onCreate Method.");
        referenceXML();

    }

    public void querySent(View v) {

        String feedback = etQuery.getText().toString();

        if (feedback.contentEquals("")) {
            Toast blank = Toast.makeText(Query.this,
                    "Blank fields detected!", Toast.LENGTH_SHORT);
            Log.i("LOG", "Input Field Blank, no message sent.");
            blank.show();
        } else {
            Toast sent = Toast.makeText(Query.this,
                    "Query Sent!", Toast.LENGTH_SHORT);
            sent.show();
            Log.i("LOG", "Sending Message...");
            Log.i("LOG", "Server not found, no message sent.");
            etQuery.setText("");
        }
    }


    private void referenceXML(){
        Log.i("LOG", "Assigning XML values to Java variables.");
        etQuery = (EditText) findViewById(R.id.etQuery);
        bQuery = (Button) findViewById(R.id.bQuery);
    }

}

