        package com.mobshep.template;
        import android.content.Intent;
        import android.content.Context;
        import android.content.DialogInterface;
        import android.app.AlertDialog;
        import android.os.Bundle;
        import android.support.v7.app.ActionBarActivity;
        import android.view.Menu;
        import android.view.MenuItem;
        import android.view.View;
        import android.view.View.OnClickListener;
        import android.widget.Button;
        import android.widget.EditText;
        import android.widget.TabHost;
        import android.widget.TabHost.TabSpec;
        import android.widget.TextView;
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

//By default Android Application class extend Activity

//OnClickListener is implemented for use of buttons and functionality which occurs with them. However this can be replaced with the Android OnClick function which requires less code.

public class template extends ActionBarActivity implements OnClickListener {

	/* This is a template app for use when creating a new Mobile Shepherd Lesson or Challenge */

	/* This app uses a tabhost, these are useful when working on a small screen as it allows for multiple tabs for a user to navigate through*/

    TabHost mobileTabs;
    Button Login;
   // Button Config;
    EditText username;
    EditText password;
    EditText key;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);

        //This line defines which layout this activity uses.
        setContentView(R.layout.mobilelayout);
        mobileTabs = (TabHost) findViewById(R.id.tabhost);

        referenceXML(); // Link up Java -> XML references + declare
        // onClickListeners.

        mobileTabs.setup();

		/* Set up each tab, the tags are defined in the layout.xml, found in the res/layout folder. */
        TabSpec specs = mobileTabs.newTabSpec("tag1");
        specs.setContent(R.id.tab1);
        specs.setIndicator("Tab 1");
        mobileTabs.addTab(specs);

        specs = mobileTabs.newTabSpec("tag2");
        specs.setContent(R.id.tab2);
        specs.setIndicator("Tab 2");
        mobileTabs.addTab(specs);

        specs = mobileTabs.newTabSpec("tag3");
        specs.setContent(R.id.tab3);
        specs.setIndicator("Tab 3");
        mobileTabs.addTab(specs);

    }


    public void onClick(View arg0) {

        switch (arg0.getId()) {

            case (R.id.bLogin):

                String CheckName = username.getText().toString();
                String CheckPass = password.getText().toString();

			/*A hard coded login if statement, this app could be used as a reverse engineer lesson.*/

                if (CheckName.contentEquals("user")
                        && CheckPass.contentEquals("pass")) {
                    key.setText("Key is revealed...");

                    //A toast is a simple pop up message, define context, text and length
                    Toast reveal = Toast.makeText(template.this, "Logged in!",
                            Toast.LENGTH_LONG);
                    reveal.show();
                }

                if (CheckName.contentEquals("") || CheckPass.contentEquals("")) {
                    Toast empty = Toast.makeText(template.this,
                            "Empty Fields Detected.", Toast.LENGTH_LONG);
                    empty.show();
                }

                else {

                    Toast invalid = Toast.makeText(template.this,
                            "Invalid Credentials!", Toast.LENGTH_LONG);
                    invalid.show();
                }
        }
    }

	/*All of the xml references are here, if an element has functionality tied to it,
	 *  it requires an onClickListener like shown below. Otherwise the findViewById will
	 *  suffice (for things like static textviews)*/

    private void referenceXML() {
        // TODO Auto-generated method stub
        Login = (Button) findViewById(R.id.bLogin);
        username = (EditText) findViewById(R.id.etName);
        password = (EditText) findViewById(R.id.etPass);
        key = (EditText) findViewById(R.id.etKey);
        Login.setOnClickListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent goToSettings = new Intent(this, Preferences.class);
            startActivity(goToSettings);
            return true;
        }

        if (id == R.id.action_license) {


            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                    this);

            // set title
            alertDialogBuilder.setTitle("License");

            // set dialog message
            alertDialogBuilder
                    .setMessage("This App is part of the Security Shepherd Project. The Security Shepherd project is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version. The Security Shepherd project is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details. You should have received a copy of the GNU General Public License along with the Security Shepherd project.  If not, see http://www.gnu.org/licenses.")
                    .setCancelable(false)
                    .setPositiveButton("OK",new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog,int id) {
                            // if this button is clicked, close
                            // current activity
                            dialog.cancel();
                        }
                    });

            // create alert dialog
            AlertDialog alertDialog = alertDialogBuilder.create();

            // show it
            alertDialog.show();

            return true;
        }

        if (id == R.id.action_exit){
            finish();
        }


        return super.onOptionsItemSelected(item);
    }

}

