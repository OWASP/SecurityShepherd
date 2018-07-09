package com.mobshep.mobileshepherd;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class untrustedInput extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.untrusted_main_layout);

        Button bKeyActivity = (Button) findViewById(R.id.bGoToKey);
        bKeyActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Access Denied", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    public void goToSettings(){
        Intent goToSettings = new Intent(this, Preferences.class);
        startActivity(goToSettings);
    }
}
