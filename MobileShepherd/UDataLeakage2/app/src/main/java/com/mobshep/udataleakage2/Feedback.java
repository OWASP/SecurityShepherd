package com.mobshep.udataleakage2;


import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class Feedback extends MainActivity{

    private Button bFeedback;

    private EditText etFeedback;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sumbitfeedback);
        referenceXML();

    }

    public void feedbackSent(View v) {

        String feedback = etFeedback.getText().toString();

        if (feedback.contentEquals("")) {
            Toast blank = Toast.makeText(Feedback.this,
                    "Blank fields detected!", Toast.LENGTH_SHORT);
            blank.show();
        } else {
            Toast sent = Toast.makeText(Feedback.this,
                    "Feedback Sent!", Toast.LENGTH_SHORT);
            sent.show();

            etFeedback.setText("");
        }
    }


    private void referenceXML(){
        etFeedback = (EditText) findViewById(R.id.etFeedback);
        bFeedback = (Button) findViewById(R.id.bFeedback);
    }

}

