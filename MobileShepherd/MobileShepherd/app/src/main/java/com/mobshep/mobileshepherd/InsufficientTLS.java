package com.mobshep.mobileshepherd;



import android.app.Activity;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.view.View;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class InsufficientTLS extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.untrusted_main_layout);
    }

    public void getKey(View v) {
        JSONParser JSONParse = new JSONParser();
        //JSONParse.execute();
        try {
            Snackbar.make(v, JSONParse.execute().get(), Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    public class JSONParser extends AsyncTask<String, Void, String> {

        String apiKey="d73959d5dd1dda7ccf675f7f883d6acb6737d5fb";

        @Override
        protected String doInBackground(String... params) {
            try {

                ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
                postParameters.add(new BasicNameValuePair("mobileKey", apiKey));

                SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                String address = SP.getString("server_preference", "NA");

                String res = CustomHttpClient.executeHttpPost(address + "/ITLSMobileAPI", postParameters);

                JSONObject jObject = new JSONObject(res.toString());

                String response = jObject.getString("LEVELKEY");

                response = response.replaceAll("\\s+", "");

                if (response != null) {
                    return response.toString();
                }
                if (response == null) {
                    return "Invalid API Key!";
                }
            } catch (Exception e) {
                return e.toString();
            }
            return null;
        }

    }

}

