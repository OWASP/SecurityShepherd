package com.mobshep.weakserversidecontrols;

import java.util.ArrayList;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class LoginLayout extends Activity {
    EditText un,pw;
    TextView error;
    Button ok;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        un=(EditText)findViewById(R.id.et_un);
        pw=(EditText)findViewById(R.id.et_pw);
        ok=(Button)findViewById(R.id.btn_login);
        error=(TextView)findViewById(R.id.tv_error);

        ok.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub

                ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
                postParameters.add(new BasicNameValuePair("username", un.getText().toString()));
                postParameters.add(new BasicNameValuePair("password", pw.getText().toString()));

                String response = null;
                try {
                    response = CustomHttpClient.executeHttpPost("<target page url>", postParameters);
                    String res=response.toString();
                    res= res.replaceAll("\\s+","");
                    if(res.equals("1"))
                        error.setText("Correct Username or Password");
                    else
                        error.setText("Sorry!! Incorrect Username or Password");
                } catch (Exception e) {
                    un.setText(e.toString());
                }

            }
        });
    }
}